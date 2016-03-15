/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package meka.classifiers.multilabel;

import meka.classifiers.multitarget.MultiTargetClassifier;
import meka.core.MLEvalUtils;
import meka.core.MLUtils;
import meka.core.Result;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;

/**
 * Evaluation.java - Evaluation functionality.
 * @author 		Jesse Read
 * @version 	March 2014
 */
public class Evaluation {

	/**
	 * RunExperiment - Build and evaluate a model with command-line options.
	 * @param	h		multi-label classifier
	 * @param	options	command line options
	 */
	public static void runExperiment(MultiLabelClassifier h, String options[]) throws Exception {

		// Help
		if(Utils.getOptionPos('h',options) >= 0) {
			System.out.println("\nHelp requested");
			Evaluation.printOptions(h.listOptions());
			return;
		}

		h.setOptions(options);

		if (h.getDebug()) System.out.println("Loading and preparing dataset ...");

		// Load Instances from a file
		Instances D_train = loadDataset(options);

		Instances D_full = D_train;

		// Try extract and set a class index from the @relation name
		MLUtils.prepareData(D_train);

		// Override the number of classes with command-line option (optional)
		if(Utils.getOptionPos('C',options) >= 0) {
			int L = Integer.parseInt(Utils.getOption('C',options));
			D_train.setClassIndex(L);
		}

		// We we still haven't found -C option, we can't continue (don't know how many labels)
		int L = D_train.classIndex();
		if(L <= 0) {
			throw new Exception("[Error] Number of labels not specified.\n\tYou must set the number of labels with the -C option, either inside the @relation tag of the Instances file, or on the command line.");
			// apparently the dataset didn't contain the '-C' flag, check in the command line options ...
		}


		// Randomize (Instances) 
		int seed = (Utils.getOptionPos('s',options) >= 0) ? Integer.parseInt(Utils.getOption('s',options)) : 0;
		if(Utils.getFlag('R',options)) {
			D_train.randomize(new Random(seed));
		}
		boolean Threaded =false;
		if(Utils.getOptionPos("Thr",options) >= 0) {
			Threaded = Utils.getFlag("Thr",options);
		}

		// Verbosity Option
		String voption = "1";
		if (Utils.getOptionPos("verbosity",options) >= 0) {
			voption = Utils.getOption("verbosity",options);
		}

		// Save for later?
		//String fname = null;
		//if (Utils.getOptionPos('f',options) >= 0) {
		//	fname = Utils.getOption('f',options);
		//}
		// Dump for later?
		String dname = null;
		if (Utils.getOptionPos('d',options) >= 0) {
			dname = Utils.getOption('d',options);
		}
		// Load from file?
		String lname = null;
		Instances dataHeader = null;
		if (Utils.getOptionPos('l',options) >= 0) {
			lname = Utils.getOption('l',options);
			Object[] data = SerializationHelper.readAll(lname);
			h = (MultiLabelClassifier)data[0];
			if (data.length > 1)
				dataHeader = (Instances) data[1];
			//Object o[] = SerializationHelper.readAll(lname);
			//h = (MultilabelClassifier)o[0];
		}

		try {

			Result r = null;

			// Threshold OPtion
			String top = "PCut1"; // default
			if (Utils.getOptionPos("threshold",options) >= 0)
				top = Utils.getOption("threshold",options);

			if(Utils.getOptionPos('x',options) >= 0) {
				// CROSS-FOLD-VALIDATION

				int numFolds = MLUtils.getIntegerOption(Utils.getOption('x',options),10); // default 10
				// Check for remaining options
				Utils.checkForRemainingOptions(options);
				r = Evaluation.cvModel(h,D_train,numFolds,top,voption);
				System.out.println(r.toString());
			}
			else {
				// TRAIN-TEST SPLIT

				Instances D_test = null;

				if(Utils.getOptionPos('T',options) >= 0) {
					// load separate test set
					try {
						D_test = loadDataset(options,'T');
						MLUtils.prepareData(D_test);
					} catch(Exception e) {
						throw new Exception("[Error] Failed to Load Test Instances from file.", e);
					}
				}
				else {
					// split training set into train and test sets
						// default split
					int N_T = (int)(D_train.numInstances() * 0.60);
					if(Utils.getOptionPos("split-percentage",options) >= 0) {
						// split by percentage
						double percentTrain = Double.parseDouble(Utils.getOption("split-percentage",options));
						N_T = (int)Math.round((D_train.numInstances() * (percentTrain/100.0)));
					}
					else if(Utils.getOptionPos("split-number",options) >= 0) {
						// split by number
						N_T = Integer.parseInt(Utils.getOption("split-number",options));
					}

					int N_t = D_train.numInstances() - N_T;
					D_test = new Instances(D_train,N_T,N_t);
					D_train = new Instances(D_train,0,N_T);

				}

				// Invert the split?
				if(Utils.getFlag('i',options)) { //boolean INVERT 			= Utils.getFlag('i',options);
					Instances temp = D_test;
					D_test = D_train;
					D_train = temp;
				}

				// Check for remaining options
				Utils.checkForRemainingOptions(options);

				if (h.getDebug()) System.out.println(":- Dataset -: "+MLUtils.getDatasetName(D_train)+"\tL="+L+"\tD(t:T)=("+D_train.numInstances()+":"+D_test.numInstances()+")\tLC(t:T)="+Utils.roundDouble(MLUtils.labelCardinality(D_train,L),2)+":"+Utils.roundDouble(MLUtils.labelCardinality(D_test,L),2)+")");

				if (lname != null) {
					// h is already built, and loaded from a file, test it!
					r = testClassifier(h, D_test);

					String t = top;

					if (top.startsWith("PCut")) {
						// if PCut is specified we need the training data,
						// so that we can calibrate the threshold!
						t = MLEvalUtils.getThreshold(r.predictions,D_train,top);
					}
					r = evaluateModel(h,D_test,t,voption);
				}
				else {
				    //check if train and test set size are > 0
				    if(D_train.numInstances() > 0 &&
				       D_test.numInstances() > 0){
					  if(Threaded){
					      r = evaluateModelM(h,D_train,D_test,top,voption);
					  }else{
					    
					      r = evaluateModel(h,D_train,D_test,top,voption);
					  }
				    } else {
					// otherwise just train on full set. Maybe better throw an exception.
					h.buildClassifier(D_full);

				    }
				}
							
				// @todo, if D_train==null, assume h is already trained
				if(D_train.numInstances() > 0 &&
				       D_test.numInstances() > 0){
				    System.out.println(r.toString());
				}
			}

			// Save model to file?
			if (dname != null) {
				dataHeader = new Instances(D_train, 0);
				SerializationHelper.writeAll(dname, new Object[]{h, dataHeader});
			}

		} catch(Exception e) {
			e.printStackTrace();
			Evaluation.printOptions(h.listOptions());
			System.exit(1);
		}

		System.exit(0);
	}


	/**
	 * IsMT - see if dataset D is multi-target (else only multi-label)
	 * @param	D	data
	 * @return	true iff D is multi-target only (else false)
	 */
	public static boolean isMT(Instances D) {
		int L = D.classIndex();
		for(int j = 0; j < L; j++) {
			if (D.attribute(j).isNominal()) {
				// Classification
				if (D.attribute(j).numValues() > 2) {
					// Multi-class
					return true;
				}
			}
			else {
				// Regression?
				System.err.println("[Warning] Found a non-nominal class -- not sure how this happened?");
			}
		}
		return false;
	}

	/**
	 * EvaluateModel - Build model 'h' on 'D_train', test it on 'D_test', threshold it according to 'top', using default verbosity option.
	 * @param	h		a multi-dim. classifier
	 * @param	D_train	training data
	 * @param	D_test 	test data
	 * @param	top    	Threshold OPtion (pertains to multi-label data only)
	 * @return	Result	raw prediction data with evaluation statistics included.
	 */
	public static Result evaluateModel(MultiLabelClassifier h, Instances D_train, Instances D_test, String top) throws Exception {
		return Evaluation.evaluateModel(h,D_train,D_test,top,"1");
	}

	/**
	 * EvaluateModel - Build model 'h' on 'D_train', test it on 'D_test', threshold it according to 'top', verbosity 'vop'.
	 * @param	h		a multi-dim. classifier
	 * @param	D_train	training data
	 * @param	D_test 	test data
	 * @param	top    	Threshold OPtion (pertains to multi-label data only)
	 * @param	vop    	Verbosity OPtion (which measures do we want to calculate/output)
	 * @return	Result	raw prediction data with evaluation statistics included.
	 */
	public static Result evaluateModel(MultiLabelClassifier h, Instances D_train, Instances D_test, String top, String vop) throws Exception {
		Result r = evaluateModel(h,D_train,D_test);
		if (h instanceof MultiTargetClassifier || isMT(D_test)) {
			r.setInfo("Type","MT");
		}
		else if (h instanceof MultiLabelClassifier) {
			r.setInfo("Type","ML");
			r.setInfo("Threshold",MLEvalUtils.getThreshold(r.predictions,D_train,top)); // <-- only relevant to ML (for now), but we'll put it in here in any case
		}
		r.setInfo("Verbosity",vop);
		r.output = Result.getStats(r, vop);
		return r;
	}

	/**
	 * EvaluateModel - Assume 'h' is already built, test it on 'D_test', threshold it according to 'top', verbosity 'vop'.
	 * @param	h		a multi-dim. classifier
	 * @param	D_test 	test data
	 * @param	tal    	Threshold VALUES (not option)
	 * @param	vop    	Verbosity OPtion (which measures do we want to calculate/output)
	 * @return	Result	raw prediction data with evaluation statistics included.
	 */
	public static Result evaluateModel(MultiLabelClassifier h, Instances D_test, String tal, String vop) throws Exception {
		Result r = testClassifier(h,D_test);
		if (h instanceof MultiTargetClassifier || isMT(D_test)) {
			r.setInfo("Type","MT");
		}
		else if (h instanceof MultiLabelClassifier) {
			r.setInfo("Type","ML");
		}
		r.setInfo("Threshold",tal);
		r.setInfo("Verbosity",vop);
		r.output = Result.getStats(r, vop);
		return r;
	}

	/**
	 * CVModel - Split D into train/test folds, and then train and evaluate on each one.
	 * @param	h		 a multi-output classifier
	 * @param	D      	 test data Instances
	 * @param	numFolds number of folds of CV
	 * @param	top    	 Threshold OPtion (pertains to multi-label data only)
	 * @return	Result	raw prediction data with evaluation statistics included.
	 */
	public static Result cvModel(MultiLabelClassifier h, Instances D, int numFolds, String top) throws Exception {
		return cvModel(h,D,numFolds,top,"1");
	}

	/**
	 * CVModel - Split D into train/test folds, and then train and evaluate on each one.
	 * @param	h		 a multi-output classifier
	 * @param	D      	 test data Instances
	 * @param	numFolds number of folds of CV
	 * @param	top    	 Threshold OPtion (pertains to multi-label data only)
	 * @param	vop    	Verbosity OPtion (which measures do we want to calculate/output)
	 * @return	Result	raw prediction data with evaluation statistics included.
	 */
	public static Result cvModel(MultiLabelClassifier h, Instances D, int numFolds, String top, String vop) throws Exception {
		Result r_[] = new Result[numFolds];
		for(int i = 0; i < numFolds; i++) {
			Instances D_train = D.trainCV(numFolds,i);
			Instances D_test = D.testCV(numFolds,i);
			if (h.getDebug()) System.out.println(":- Fold ["+i+"/"+numFolds+"] -: "+MLUtils.getDatasetName(D)+"\tL="+D.classIndex()+"\tD(t:T)=("+D_train.numInstances()+":"+D_test.numInstances()+")\tLC(t:T)="+Utils.roundDouble(MLUtils.labelCardinality(D_train,D.classIndex()),2)+":"+Utils.roundDouble(MLUtils.labelCardinality(D_test,D.classIndex()),2)+")");
			r_[i] = evaluateModel(h, D_train, D_test); // <-- should not run stats yet!
		}
		Result r = MLEvalUtils.combinePredictions(r_);
		if (h instanceof MultiTargetClassifier || isMT(D)) {
			r.setInfo("Type","MT-CV");
		}
		else if (h instanceof MultiLabelClassifier) {
			r.setInfo("Type","ML-CV");
			try {
				r.setInfo("Threshold",String.valueOf(Double.parseDouble(top)));
			} catch(Exception e) {
				System.err.println("[WARNING] Automatic threshold calibration not currently enabled for cross-fold validation, setting threshold = 0.5.\n");
				r.setInfo("Threshold",String.valueOf(0.5));
			}
		}
		r.setInfo("Verbosity",vop);
		r.output = Result.getStats(r, vop);
		// Need to reset this because of CV
		r.setValue("Number of training instances",D.numInstances());
		r.setValue("Number of test instances",D.numInstances());
		return r;
	}

	/**
	 * EvaluateModel - Build model 'h' on 'D_train', test it on 'D_test'.
	 * Note that raw multi-label predictions returned in Result may not have been thresholded yet.
	 * However, data statistics, classifier info, and running times are inpregnated into the Result here.
	 * @param	h		a multi-dim. classifier
	 * @param	D_train	training data
	 * @param	D_test 	test data
	 * @return	raw prediction data (no evaluation yet)
	 */
	public static Result evaluateModel(MultiLabelClassifier h, Instances D_train, Instances D_test) throws Exception {

		long before = System.currentTimeMillis();
		// Set test data as unlabelled data, if SemisupervisedClassifier
		if (h instanceof SemisupervisedClassifier) { 
			((SemisupervisedClassifier)h).introduceUnlabelledData(MLUtils.setLabelsMissing(new Instances(D_test)));
		}
		// Train
		h.buildClassifier(D_train);
		long after = System.currentTimeMillis();

		//System.out.println(":- Classifier -: "+h.getClass().getName()+": "+Arrays.toString(h.getOptions()));

		// Test
		long before_test = System.currentTimeMillis();
		Result result = testClassifier(h,D_test);
		long after_test = System.currentTimeMillis();

		result.setValue("Number of training instances",D_train.numInstances());
		result.setValue("Number of test instances",D_test.numInstances());
		result.setValue("Label cardinality (train set)",MLUtils.labelCardinality(D_train));
		result.setValue("Label cardinality (test set)",MLUtils.labelCardinality(D_test));

		result.setValue("Build Time",(after - before)/1000.0);
		result.setValue("Test Time",(after_test - before_test)/1000.0);
		result.setValue("Total Time", (after_test - before) / 1000.0);

		result.setInfo("Classifier",h.getClass().getName());
		result.setInfo("Options",Arrays.toString(h.getOptions()));
		result.setInfo("Additional Info",h.toString());
		result.setInfo("Dataset",MLUtils.getDatasetName(D_train));
		result.setInfo("Number of labels (L)",String.valueOf(D_train.classIndex()));
		//result.setInfo("Maxfreq_set",MLUtils.mostCommonCombination(D_train,result.L));

		String model = h.getModel();
		if (model.length() > 0)
			result.setModel("Model",h.getModel());

		return result;
	}

/* allow threaded evaluation of model,
 * all instances are passed to the classifier then they are gathered in results,
 * for short datasets the overhead might be significant
 */
	public static Result evaluateModelM(MultiLabelClassifier h, Instances D_train, Instances D_test, String top, String vop) throws Exception {
		// Train
				long before = System.currentTimeMillis();
				/*if (h instanceof SemisupervisedClassifier) { // *NEW* for semi-supervised 
					((SemisupervisedClassifier)h).setUnlabelledData(MLUtils.setLabelsMissing(new Instances(D_test)));
				}*/
				h.buildClassifier(D_train);
				long after = System.currentTimeMillis();

				//System.out.println(":- Classifier -: "+h.getClass().getName()+": "+Arrays.toString(h.getOptions()));

				// Test
				long before_test = System.currentTimeMillis();
				Result result = testClassifierM(h,D_test);
				long after_test = System.currentTimeMillis();

				result.setValue("N_train",D_train.numInstances());
				result.setValue("N_test",D_test.numInstances());
				result.setValue("LCard_train",MLUtils.labelCardinality(D_train));
				result.setValue("LCard_test",MLUtils.labelCardinality(D_test));

				result.setValue("Build_time",(after - before)/1000.0);
				result.setValue("Test_time",(after_test - before_test)/1000.0);
				result.setValue("Total_time",(after_test - before)/1000.0);

				result.setInfo("Classifier_name",h.getClass().getName());
				result.setInfo("Classifier_ops",Arrays.toString(h.getOptions()));
				result.setInfo("Classifier_info",h.toString());
				result.setInfo("Dataset_name",MLUtils.getDatasetName(D_train));
				//result.setInfo("Maxfreq_set",MLUtils.mostCommonCombination(D_train,result.L));

		if (h instanceof MultiTargetClassifier || isMT(D_test)) {
			result.setInfo("Type","MT");
		}
		else if (h instanceof MultiLabelClassifier) {
			result.setInfo("Type","ML");
		}
		result.setInfo("Threshold",MLEvalUtils.getThreshold(result.predictions,D_train,top)); // <-- only relevant to ML (for now), but we'll put it in here in any case
		result.setInfo("Verbosity",vop);
		result.output = Result.getStats(result, vop);
		return result;
	}

	/**
	 * TestClassifier - test classifier h on D_test
	 * @param	h		a multi-dim. classifier, ALREADY BUILT
	 * @param	D_test 	test data
	 * @return	Result	with raw prediction data ONLY
	 */
	public static Result testClassifier(MultiLabelClassifier h, Instances D_test) throws Exception {

		int L = D_test.classIndex();
		Result result = new Result(D_test.numInstances(),L);

		if(h.getDebug()) System.out.print(":- Evaluate ");
		for (int i = 0, c = 0; i < D_test.numInstances(); i++) {

			if(h.getDebug()) { int t = i*50/D_test.numInstances(); if(t > c) { System.out.print("#"); c = t; } }

			// No cheating allowed; clear all class information
			AbstractInstance x = (AbstractInstance)((AbstractInstance) D_test.instance(i)).copy(); 
			for(int v = 0; v < D_test.classIndex(); v++) 
				x.setValue(v,0.0);

			// Get and store ranking
			double y[] = h.distributionForInstance(x);
			// Cut off any [no-longer-needed] probabalistic information from MT classifiers.
			if (h instanceof MultiTargetClassifier)
				y = Arrays.copyOf(y,L);

			// Store the result
			result.addResult(y,D_test.instance(i));
		}
		if(h.getDebug()) System.out.println(":-");

		/*
		if(h.getDebug()) {

			for(int i = 0; i < result.size(); i++) {
				System.out.println("\t"+Arrays.toString(result.rowTrue(i))+" vs "+Arrays.toString(result.rowRanking(i)));
			}
		}
		*/

		return result;
	}
    /**
     *Test Classifier but threaded (Multiple)     
     * @param	h		a multi-dim. classifier, ALREADY BUILT (threaded, implements MultiLabelThreaded)
     * @param	D_test 	test data
     * @return	Result	with raw prediction data ONLY
    */
	public static Result testClassifierM(MultiLabelClassifier h, Instances D_test) throws Exception {

		int L = D_test.classIndex();
		Result result = new Result(D_test.numInstances(),L);
		if(h.getDebug()) System.out.print(":- Evaluate ");
		if(h instanceof MultiLabelClassifierThreaded){
		((MultiLabelClassifierThreaded)h).setThreaded(true);
		double y[][] = ((MultiLabelClassifierThreaded)h).distributionForInstanceM(D_test);

		for (int i = 0, c = 0; i < D_test.numInstances(); i++) {
			// Store the result
			result.addResult(y[i],D_test.instance(i));
		}
		if(h.getDebug()) System.out.println(":-");

		/*
		if(h.getDebug()) {

			for(int i = 0; i < result.size(); i++) {
				System.out.println("\t"+Arrays.toString(result.rowActual(i))+" vs "+Arrays.toString(result.rowRanking(i)));
			}


		}
		*/
		}
		return result;
	}

	/**
	 * GetDataset - load a dataset, given command line options specifying an arff file, and set the class index correctly to indicate the number of labels.
	 * @param	options	command line options
	 * @param	T		set to 'T' if we want to load a test file
	 * @return	An Instances representing the dataset
	public static Instances getDataset(String options[], char T) throws Exception {
		Instances D = loadDataset(options, T);
		setClassesFromOptions(D,MLUtils.getDatasetOptions(D));
		return D;
	}
	*/

	/**
	 * GetDataset - load a dataset, given command line options specifying an arff file, and set the class index correctly to indicate the number of labels.
	 * @param	options	command line options
	 * @return	An Instances representing the dataset
	public static Instances getDataset(String options[]) throws Exception {
		return getDataset(options,'t');
	}
	*/

	/**
	 * loadDataset - load a dataset, given command line option '-t' specifying an arff file.
	 * @param	options	command line options, specifying dataset filename
	 * @return	the dataset
	 */
	public static Instances loadDataset(String options[]) throws Exception {
		return loadDataset(options,'t');
	}

	/**
	 * loadDataset - load a dataset, given command line options specifying an arff file.
	 * @param	options	command line options, specifying dataset filename
	 * @param	T		set to 'T' if we want to load a test file (default 't': load train or train-test file)
	 * @return	the dataset
	 */
	public static Instances loadDataset(String options[], char T) throws Exception {

		Instances D = null;
		String filename = Utils.getOption(T, options);

		// Check for filename
		if (filename == null || filename.isEmpty())
			throw new Exception("[Error] You did not specify a dataset!");

		// Check for existence of file
		File file = new File(filename);
		if (!file.exists())
			throw new Exception("[Error] File does not exist: " + filename);
		if (file.isDirectory())
			throw new Exception("[Error] "+filename+ " points to a directory!");

		try {
			DataSource source = new DataSource(filename);
			D = source.getDataSet();
		} catch(Exception e) {
			e.printStackTrace();
			throw new Exception("[Error] Failed to load Instances from file '"+filename+"'.");
		}

		return D;
	}

	/*
	 * GetL - get number of labels (option 'C' from options 'options').
	private static int getL(String options[]) throws Exception {
		return (Utils.getOptionPos('C', options) >= 0) ? Integer.parseInt(Utils.getOption('C',options)) : 0;
	}
	*/
	

	/*
	 * SetClassesFromOptions - set the class index correctly in a dataset 'D', given command line options 'options'.
	 * <br>
	 * NOTE: there is a similar function in Exlorer.prepareData(D) but that function can only take -C from the dataset options.
	 * <br>
	 * TODO: replace the call to Exlorer.prepareData(D) with this method here (use the name 'prepareData' -- it souds better).
	public static void setClassesFromOptions(Instances D, String options[]) throws Exception {
		try {
			// get L
			int L = getL(options);
			// if negative, then invert first
			if ( L < 0) {
				L = -L;
				D = F.mulan2meka(D,L);
			}
			// set L
			D.setClassIndex(L);
		} catch(Exception e) {
			e.printStackTrace();
			throw new Exception ("[Error] Failed to Set Classes from options. You must supply the number of labels either in the @Relation Name of the dataset or on the command line using the option: -C <num. labels>");
		}
	}
	*/

	public static void printOptions(Enumeration e) {

		// Evaluation Options
		StringBuffer text = new StringBuffer();
		text.append("\n\nEvaluation Options:\n\n");
		text.append("-h\n");
		text.append("\tOutput help information.\n");
		text.append("-t <name of training file>\n");
		text.append("\tSets training file.\n");
		text.append("-T <name of test file>\n");
		text.append("\tSets test file.\n");
		text.append("-x <number of folds>\n");
		text.append("\tDo cross-validation with this many folds.\n");
		text.append("-R\n");
		text.append("\tRandomize the order of instances in the dataset.\n");
		text.append("-split-percentage <percentage>\n");
		text.append("\tSets the percentage for the train/test set split, e.g., 66.\n");
		text.append("-split-number <number>\n");
		text.append("\tSets the number of training examples, e.g., 800\n");
		text.append("-i\n");
		text.append("\tInvert the specified train/test split.\n");
		text.append("-s <random number seed>\n");
		text.append("\tSets random number seed (use with -R, for different CV or train/test splits).\n");
		text.append("-threshold <threshold>\n");
		text.append("\tSets the type of thresholding; where\n\t\t'PCut1' automatically calibrates a threshold (the default);\n\t\t'PCutL' automatically calibrates one threshold for each label;\n\t\tany number, e.g. '0.5', specifies that threshold.\n");
		text.append("-C <number of labels>\n");
		text.append("\tSets the number of target variables (labels) to assume (indexed from the beginning).\n");
		//text.append("-f <results_file>\n");
		//text.append("\tSpecify a file to output results and evaluation statistics into.\n");
		text.append("-d <classifier_file>\n");
		text.append("\tSpecify a file to dump classifier into.\n");
		text.append("-l <classifier_file>\n");
		text.append("\tSpecify a file to load classifier from.\n");
		text.append("-verbosity <verbosity level>\n");
		text.append("\tSpecify more/less evaluation output\n");
		// Multilabel Options
		text.append("\n\nClassifier Options:\n\n");
		while (e.hasMoreElements()) {
			Option o = (Option) (e.nextElement());
			text.append("-"+o.name()+'\n');
			text.append(""+o.description()+'\n');
		}

		System.out.println(text);
	}

}
