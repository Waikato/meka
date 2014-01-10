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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.io.File;

import meka.classifiers.multitarget.MultiTargetClassifier;
import weka.core.AbstractInstance;
import weka.core.Instance;
import weka.core.Instances;
import meka.core.MLEvalUtils;
import meka.core.MLUtils;
import weka.core.Option;
import weka.core.Randomizable;
import meka.core.Result;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveRange;

/**
 * Evaluation.java - Evaluation functionality.
 * @author 		Jesse Read (jmr30@cs.waikato.ac.nz)
 * @version 	November 2012
 */
public class Evaluation {

	/**
	 * RunExperiment - Build and evaluate a model with command-line options.
	 */
	public static void runExperiment(MultilabelClassifier h, String options[]) throws Exception {

		// Help
		if(Utils.getOptionPos('h',options) >= 0) {
			System.out.println("\nHelp requested");
			Evaluation.printOptions(h.listOptions());
			return;
		}

		h.setOptions(options);

		//Load Instances
		Instances allInstances = getDataset(options);
		int L = allInstances.classIndex();
		boolean cSwitch = (L < 0); // because we will need to do this for the test instances also

		//Check for the essential -C option. If still nothing set, we can't continue
		//if(allInstances.classIndex() < 0) 
		//	throw new Exception(");

		// Seed
		int seed = (Utils.getOptionPos('s',options) >= 0) ? Integer.parseInt(Utils.getOption('s',options)) : 0;

		// Randomize (Instances) 
		if(Utils.getOptionPos('R',options) >= 0) {
			boolean R = Utils.getFlag('R',options);
			allInstances.randomize(new Random(seed));
		}

		// Randomize (Model)
		if (h instanceof Randomizable) {
			((Randomizable)h).setSeed(seed + 1); // (@NOTE because previously we were using seed '1' as the default in BaggingML, we want to maintain reproducibility of older results with the same seed).
		}

		String voption = "1";
		if (Utils.getOptionPos("verbosity",options) >= 0) {
			voption = Utils.getOption("verbosity",options);
		}

		// Save later?
		String fname = null;
		if (Utils.getOptionPos('f',options) >= 0) {
			fname = Utils.getOption('f',options);
		}

		try {

			Result r = null;

			// Threshold OPtion
			String top = "PCut1"; // default
			if (Utils.getOptionPos("threshold",options) >= 0)
				top = Utils.getOption("threshold",options);

			// Get Split
			if(Utils.getOptionPos('x',options) >= 0) {

				// CROSS-FOLD-VALIDATION
				int numFolds = MLUtils.getIntegerOption(Utils.getOption('x',options),10); // default 10
				// Check for remaining options
				Utils.checkForRemainingOptions(options);
				Result fold[] = Evaluation.cvModel(h,allInstances,numFolds,top,voption);
				r = MLEvalUtils.averageResults(fold);
				System.out.println(r.toString());
				if (fname != null) {
					for(int i = 0; i < fold.length; i++) {
						Result.writeResultToFile(fold[i],fname+"."+i);
					}
				}
			}
			else {
				int TRAIN = -1;
				if(Utils.getOptionPos('T',options) >= 0) {
					// split by train / test files
					TRAIN = allInstances.numInstances();
					// load test set
					Instances testInstances = null;
					try {
						String filename = Utils.getOption('T', options);
						testInstances = DataSource.read(filename);
						if (cSwitch) {// we have to switch these attributes also
							MLUtils.switchAttributes(testInstances,allInstances.classIndex());
						}
						for(Instance x : testInstances) {
							x.setDataset(allInstances);
							allInstances.add(x);
						}
					} catch(Exception e) {
						throw new Exception("[Error] Failed to Load Test Instances from file.", e);
					}
				}
				else if(Utils.getOptionPos("split-percentage",options) >= 0) {
					// split by percentage
					double percentTrain = Double.parseDouble(Utils.getOption("split-percentage",options));
					TRAIN = (int)Math.round((allInstances.numInstances() * (percentTrain/100.0)));
				}
				else if(Utils.getOptionPos("split-number",options) >= 0) {
					// split by number
					TRAIN = Integer.parseInt(Utils.getOption("split-number",options));
				}
				else {
					// default split
					TRAIN = (int)(allInstances.numInstances() * 0.60);
				}

				int TEST = allInstances.numInstances() - TRAIN;
				Instances train = new Instances(allInstances,0,TRAIN);
				train.setClassIndex(allInstances.classIndex());
				Instances test = new Instances(allInstances,TRAIN,TEST);
				test.setClassIndex(allInstances.classIndex());

				// Invert the split?
				if(Utils.getFlag('i',options)) { //boolean INVERT 			= Utils.getFlag('i',options);
					Instances temp = test;
					test = train;
					train = temp;
				}

				// We're going to do parameter tuning
				/*
				if(Utils.getOptionPos('u',options) >= 0) {
					double percentageSplit = Double.parseDouble(Utils.getOption('u',options));
					TRAIN = (int)(train.numInstances() * percentageSplit);
					TEST = train.numInstances() - TRAIN;
					train = new Instances(train,0,TRAIN);
					test = new Instances(train,TRAIN,TEST);
				}
				*/

				// Check for remaining options
				Utils.checkForRemainingOptions(options);

				if (h.getDebug()) System.out.println(":- Dataset -: "+MLUtils.getDatasetName(allInstances)+"\tL="+allInstances.classIndex()+"\tD(t:T)=("+train.numInstances()+":"+test.numInstances()+")\tLC(t:T)="+Utils.roundDouble(MLUtils.labelCardinality(train,allInstances.classIndex()),2)+":"+Utils.roundDouble(MLUtils.labelCardinality(test,allInstances.classIndex()),2)+")");

				r = evaluateModel(h,train,test,top,voption);
				System.out.println(r.toString());

			}

			// Save ranking data?

			if (fname != null) {
				Result.writeResultToFile(r,fname);
			}

		} catch(Exception e) {
			//System.out.println(e);
			e.printStackTrace();
			Evaluation.printOptions(h.listOptions());
			System.exit(1);
		}

		System.exit(0);
	}


	/**
	 * IsMT - see if dataset D is multi-dimensional (else only multi-label)
	 * @param	D	data
	 * @return	true iff D is multi-dimensional only (else false)
	 */
	public static boolean isMT(Instances D) {
		int L = D.classIndex();
		for(int j = 0; j < L; j++) {
			if (D.attribute(j).isNominal()) {
				if (D.attribute(j).numValues() > 2) {
					return true;
				}
			}
			else {
				System.err.println("wtf?");
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
	public static Result evaluateModel(MultilabelClassifier h, Instances D_train, Instances D_test, String top) throws Exception {
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
	public static Result evaluateModel(MultilabelClassifier h, Instances D_train, Instances D_test, String top, String vop) throws Exception {
		Result r = evaluateModel(h,D_train,D_test);
		if (h instanceof MultiTargetClassifier || isMT(D_test)) {
			r.setInfo("Type","MT");
		}
		else if (h instanceof MultilabelClassifier) {
			r.setInfo("Threshold",MLEvalUtils.getThreshold(r.predictions,D_train,top));
			r.setInfo("Type","ML");
		}
		r.setInfo("Verbosity",vop);
		r.output = Result.getStats(r, vop);
		return r;
	}

	/**
	 * CVModel - Split D into train/test folds, and then train and evaluate on each one.
	 * @param	h		 a multi-dim. classifier
	 * @param	D      	 data
	 * @param	numFolds test data
	 * @param	top    	 Threshold OPtion (pertains to multi-label data only)
	 * @return	an array of 'numFolds' Results
	 */
	public static Result[] cvModel(MultilabelClassifier h, Instances D, int numFolds, String top) throws Exception {
		return cvModel(h,D,numFolds,top,"1");
	}

	/**
	 * CVModel - Split D into train/test folds, and then train and evaluate on each one.
	 * @param	h		 a multi-dim. classifier
	 * @param	D      	 data
	 * @param	numFolds test data
	 * @param	top    	 Threshold OPtion (pertains to multi-label data only)
	 * @param	vop    	Verbosity OPtion (which measures do we want to calculate/output)
	 * @return	an array of 'numFolds' Results
	 */
	public static Result[] cvModel(MultilabelClassifier h, Instances D, int numFolds, String top, String vop) throws Exception {
		Result r[] = new Result[numFolds];
		for(int i = 0; i < numFolds; i++) {
			Instances D_train = D.trainCV(numFolds,i);
			Instances D_test = D.testCV(numFolds,i);
			if (h.getDebug()) System.out.println(":- Fold ["+i+"/"+numFolds+"] -: "+MLUtils.getDatasetName(D)+"\tL="+D.classIndex()+"\tD(t:T)=("+D_train.numInstances()+":"+D_test.numInstances()+")\tLC(t:T)="+Utils.roundDouble(MLUtils.labelCardinality(D_train,D.classIndex()),2)+":"+Utils.roundDouble(MLUtils.labelCardinality(D_test,D.classIndex()),2)+")");
			r[i] = evaluateModel(h, D_train, D_test, top, vop);
		}
		return r;
	}

		// RUN
	/*

		Result result = null;
		try {
			result = evaluateModel(h,args);
		} catch(Exception e) {
			System.err.println("\nMeka Exception: "+e+".");
			Evaluation.printOptions(h.listOptions());
			return;
		}
		*/


	/**
	 * EvaluateModel - Build model 'h' on 'D_train', test it on 'D_test'.
	 * Note that raw multi-label predictions returned in Result may not have been thresholded yet.
	 * However, data statistics, classifier info, and running times are inpregnated into the Result here.
	 * @param	h		a multi-dim. classifier
	 * @param	D_train	training data
	 * @param	D_test 	test data
	 * @return	raw prediction data (no evaluation yet)
	 */
	public static Result evaluateModel(MultilabelClassifier h, Instances D_train, Instances D_test) throws Exception {

		// Train
		long before = System.currentTimeMillis();
		if (h instanceof SemisupervisedClassifier) { // *NEW* for semi-supervised 
			((SemisupervisedClassifier)h).setUnlabelledData(MLUtils.setLabelsMissing(new Instances(D_test)));
		}
		h.buildClassifier(D_train);
		long after = System.currentTimeMillis();

		//System.out.println(":- Classifier -: "+h.getClass().getName()+": "+Arrays.toString(h.getOptions()));

		// Test
		long before_test = System.currentTimeMillis();
		Result result = testClassifier(h,D_test);
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

		return result;
	}

	/**
	 * TestClassifier - test classifier h on D_test
	 * @param	h		a multi-dim. classifier, ALREADY PREVIOUSLY TRAINED
	 * @param	D_test 	test data
	 * @return	Result	with raw prediction data ONLY
	 */
	public static Result testClassifier(MultilabelClassifier h, Instances D_test) throws Exception {

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

		if(h.getDebug()) {

			for(int i = 0; i < result.size(); i++) {
				System.out.println("\t"+Arrays.toString(result.rowActual(i))+" vs "+Arrays.toString(result.rowRanking(i)));
			}


		}

		return result;
	}

	/**
	 * GetDataset - load a dataset, given command line options specifying an arff file, and set the class index correctly to indicate the number of labels.
	 * @param	options	command line options
	 * @return	An Instances representing the dataset
	 */
	public static Instances getDataset(String options[]) throws Exception {
		Instances D = loadDatasetFromOptions(options);
		setClassesFromOptions(D,MLUtils.getDatasetOptions(D));
		return D;
	}

	/**
	 * LoadDatasetFromOptions - load a dataset, given command line options specifying an arff file.
	 * @param	options	command line options
	 * @return	An Instances representing the dataset
	 */
	public static Instances loadDatasetFromOptions(String options[]) throws Exception {

		Instances D = null;
		String filename = Utils.getOption('t', options);

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

	/**
	 * SetClassesFromOptions - set the class index correctly in a dataset D, given command line options 'options'.
	 * @note: there is a similar function in Exlorer.prepareData(D) but that function can only take -C from the dataset options.
	 * @todo: replace the call to Exlorer.prepareData(D) with this method here (use the name 'prepareData' -- it souds better).
	 */
	public static void setClassesFromOptions(Instances D, String options[]) throws Exception {
		try {
			// get L
			int L = (Utils.getOptionPos('C', options) >= 0) ? Integer.parseInt(Utils.getOption('C',options)) : Integer.parseInt(Utils.getOption('c',options));
			// if negative, then invert first
			if ( L < 0) {
				L = -L;
				D = MLUtils.switchAttributes(D,L);
			}
			// set L
			D.setClassIndex(L);
		} catch(Exception e) {
			e.printStackTrace();
			//System.err.println("[Error] Failed to Set Options from Command Line -- Check\n\t the spelling of the base classifier;\n \t that options are specified in the correct order (respective to  the '--' divider); and\n\t that the class index is set properly.");
			//System.exit(1);
			throw new Exception ("[Error] Failed to Set Classes from options. You must supply the number of labels either in the @Relation Name of the dataset or on the command line using the option: -C <num. labels>");
		}
	}

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
		// DEPRECATED (use filter instead)
		//text.append("-p\n");
		//text.append("\tSpecify a range in the dataset (@see weka.core.Range)\n");
		text.append("-R\n");
		text.append("\tRandomise the dataset (done after a range is removed, but before the train/test split)\n");
		text.append("-split-percentage <percentage>\n");
		text.append("\tSets the percentage for the train/test set split, e.g., 66.\n");
		text.append("-split-number <number>\n");
		text.append("\tSets the number of training examples, e.g., 800.\n");
		text.append("-i\n");
		text.append("\tInvert the specified train/test split\n");
		text.append("-s <random number seed>\n");
		text.append("\tSets random number seed.");
		text.append("-threshold <threshold>\n");
		text.append("\tSets the type of thresholding; where 'PCut1' automatically calibrates a threshold (the default); 'PCutL' automatically calibrates one threshold for each label; and any double number, e.g. '0.5', specifies that threshold.\n");
		text.append("-C <number of target attributes>\n");
		text.append("\tSets the number of target attributes to expect (indexed from the beginning).\n");
		text.append("-f <results_file>\n");
		text.append("\tSpecify a file to output results and evaluation statistics into.\n");
		text.append("-verbosity <verbosity level>\n");
		text.append("\tSpecify more/less evaluation.\n");
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
