package weka.classifiers.multilabel;

import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.multilabel.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.unsupervised.instance.*;
import weka.filters.*;
import java.util.*;
import java.io.*;

/**
 * Evaluation.
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class Evaluation {

	public static void runExperiment(MultilabelClassifier PTX, String args[]) throws Exception {

		PTX.setOptions(args);

		Store r = evaluateModel(PTX,args);
		if(r == null) {									// Error
			throw new Exception("[Error] Weka Exception - Null Result -  Failed to Evaluate Model !");
		}
		else if (Utils.getOptionPos('f',args) >= 0) { 	// Save
			Store.writeStoreToFile(r,Utils.getOption('f',args));
		}
		else {											// Print
			r.calculate(r);
			System.out.println(r);
		}
	}

	/**
	 * Build and evaluate a multi-label model.
	 * with train and test split pre-supplied.
	 */
	public static Store evaluateModel(MultilabelClassifier PTX, Instances train, Instances test) throws Exception {


		try {

			// Train
			long before = System.currentTimeMillis();
			PTX.buildClassifier(train);
			long after = System.currentTimeMillis();

			//System.out.println(":- Classifier -: "+PTX.getClass().getName()+": "+Arrays.toString(PTX.getOptions()));

			// Test
			long before_test = System.currentTimeMillis();
			Store result = evaluateClassifier(PTX,test);
			long after_test = System.currentTimeMillis();

			result.setValue("Build_time",(double)(after - before)/1000.0);
			result.setValue("Test_time",(double)(after_test - before_test)/1000.0);


			result.setInfo("Classifier_name",PTX.getClass().getName());
			result.setInfo("Classifier_info",Arrays.toString(PTX.getOptions()));
			result.setInfo("Dataset_name",MLUtils.getDatasetName(train));
			result.extractStats(result,train,test); 

			return result;

		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Build and evaluate a multi-label model.
	 * With command-line options.
	 */
	public static Store evaluateModel(MultilabelClassifier PTX, String options[]) throws Exception {

		//Get Debug/Verbosity/Output Level
		boolean INVERT 			= Utils.getFlag('i',options);

		//Load Instances
		Instances allInstances = null;
		try {
			String filename = Utils.getOption('t', options);
			allInstances = new Instances(new BufferedReader(new FileReader(filename)));
		} catch(IOException e) {
			e.printStackTrace();
			throw new Exception("[Error] Failed to Load Instances from file");
		}

		//Concatenate the Options in the @relation name (in format 'dataset-name: <options>') to the cmd line options
		String doptions[] = null;
		try {
			doptions = MLUtils.getDatasetOptions(allInstances);
		} catch(Exception e) {
			throw new Exception("[Error] Failed to Set Options from @Relation Name");
		}

		//Set Options from the command line, any leftover options will most likely be used in the code that follows
		try {
			int c = Integer.parseInt(Utils.getOption('C',doptions));
			// if negative, then invert ...
			if ( c < 0) {
				c = -c;
				allInstances = MLUtils.switchAttributes(allInstances,c);
			}
			// end
			allInstances.setClassIndex(c);
		} catch(Exception e) {
			System.err.println("[Error] Failed to Set Options from Command Line -- Check\n\t The spelling of the SL classifier\n\t That an option isn't on the wrong side of the '--'");
			System.exit(1);
		}

		//Check for the essential -C option. If still nothing set, we can't continue
		if(allInstances.classIndex() < 0) 
			throw new Exception("You must supply the number of labels either in the @Relation Name or on the command line: -C <num> !");

		//Set Range
		if(Utils.getOptionPos('p',options) >= 0) {
			try {
				String range = Utils.getOption('p',options);
				System.out.println("Selecting Range "+range+"");
				RemoveRange remove = new RemoveRange();  
				remove.setInstancesIndices(range);
				remove.setInvertSelection(true);
				remove.setInputFormat(allInstances);
				allInstances = Filter.useFilter(allInstances, remove);
			} catch(Exception e) {
				throw new Exception("Failed to Remove Range");
			}
		}

		//Randomize
		if(Utils.getOptionPos('R',options) >= 0) {
			int seed = 0;
			if (Utils.getOptionPos('s',options) >= 0) {
				seed = Integer.parseInt(Utils.getOption('s',options));
			}
			Random random = new Random(seed);
			allInstances.randomize(random);
		}

		options = Utils.splitOptions(Utils.joinOptions(options) + "," + Utils.joinOptions(doptions)+", ");

		try {

			// Get Split
			int TRAIN, TEST;
			if(Utils.getOptionPos("split-percentage",doptions) >= 0) {
				double percentTrain = Double.parseDouble(Utils.getOption("split-percentage",doptions));
				TRAIN = (int)Math.round((allInstances.numInstances() * (percentTrain/100.0)));
			}
			else if(Utils.getOptionPos("split-number",doptions) >= 0) {
				TRAIN = Integer.parseInt(Utils.getOption("split-number",doptions));
				System.out.println("TRAIN="+TRAIN);
			}
			else { // Defaults
				TRAIN = (int)(allInstances.numInstances() * 0.60);
			}

			TEST = allInstances.numInstances() - TRAIN;

			Instances train = new Instances(allInstances,0,TRAIN);
			train.setClassIndex(allInstances.classIndex());
			Instances test = new Instances(allInstances,TRAIN,TEST);
			test.setClassIndex(allInstances.classIndex());

			//Invert the split?
			if(INVERT) {
				Instances holder = test;
				test = train;
				train = holder;
			}

			if (PTX.getDebug()) System.out.println(":- Dataset -: "+MLUtils.getDatasetName(allInstances)+"\tL="+allInstances.classIndex()+"\tD(t:T)=("+train.numInstances()+":"+test.numInstances()+")\tLC(t:T)="+Utils.roundDouble(MLUtils.labelCardinality(train,allInstances.classIndex()),2)+":"+Utils.roundDouble(MLUtils.labelCardinality(test,allInstances.classIndex()),2)+")");

			// Final Clean

			allInstances.delete();

			return evaluateModel(PTX,train,test);

		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Evaluate a multi-label model.
	 */
	public static Store evaluateClassifier(MultilabelClassifier PTX, Instances test) throws Exception {

		Store record = new Store(test.numInstances(),test.classIndex());

		if(PTX.getDebug()) System.out.print(":- Evaluate ");
		for (int i = 0, c = 0; i < test.numInstances(); i++) {

			if(PTX.getDebug()) { int t = i*50/test.numInstances(); if(t > c) { System.out.print("#"); c = t; } }

			// No cheating allowed; clear all class information
			AbstractInstance copy = (AbstractInstance)((AbstractInstance) test.instance(i)).copy(); 
			for(int v = 0; v < test.classIndex(); v++) 
				copy.setValue(v,0.0);

			// Get and store ranking
			double d[] = PTX.distributionForInstance(copy);

			// Store the result
			record.addResult(d,test.instance(i));
		}
		if(PTX.getDebug()) System.out.println(":-");

		return record;
	}

	public static void printOptions(Enumeration e) {

		// Evaluation Options
		StringBuffer text = new StringBuffer();
		text.append("\n\nEvaluation Options:\n\n");
		text.append("-t\n");
		text.append("\tSpecify the dataset (required)\n");
		text.append("-p\n");
		text.append("\tSpecify a range in the dataset (@see weka.core.Range)\n");
		text.append("-R\n");
		text.append("\tRandomise the dataset (done after a range is removed, but before the train/test split)\n");
		text.append("-split-percentage\n");
		text.append("\tSpecify a training split by percentage (default 60) maximum 100\n");
		text.append("-split-number\n");
		text.append("\tSpecify a training split by number (e.g. 870) instead of percentage\n");
		text.append("-i\n");
		text.append("\tInvert the specified train/test split\n");
		// Multilabel Options
		text.append("\n\nMultilabel Options:\n\n");
		while (e.hasMoreElements()) {
			Option o = (Option) (e.nextElement());
			text.append("-"+o.name()+'\n');
			text.append(""+o.description()+'\n');
		}

		System.out.println(""+text);
	}

}
