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

import weka.core.*;
import meka.core.*;
import weka.classifiers.*;
import meka.gui.explorer.*;
import meka.classifiers.multilabel.*;
import java.util.*;
import java.io.*;

/**
 * IncrementalEvaluation.java - For Evaluating Incremental (Updateable) Classifiers.
 * @author 		Jesse Read (jesse@tsc.uc3m.es)
 * @version 	Feb 2013
 */
public class IncrementalEvaluation {

	/**
	 * RunExperiment - Build and evaluate a model with command-line options.
	 * @param	h			a multi-label updateable classifier
	 * @param	args[]		classifier + dataset options
	 */
	public static void runExperiment(MultilabelClassifier h, String args[]) {
		try {
			h.setOptions(args);
			IncrementalEvaluation.evaluateModel(h,args);
		} catch(Exception e) {
			System.err.println("Evaluation exception ("+e+"); failed to run experiment");
			e.printStackTrace();
			printOptions(h.listOptions());
		}
	}

	/**
	 * EvaluateModel - Build and evaluate.
	 * @param	h			a multi-label updateable classifier
	 * @param	options[]	dataset options (classifier options should already be set)
	 * @return	The evaluation Result
	 */
	public static Result evaluateModel(MultilabelClassifier h, String options[]) throws Exception {

		// Load Instances, ...
		Instances D = Evaluation.getDataset(options);

		// Set the number of windows (batches) @todo move below combining options?
		int nWin = 20;
		try {
			nWin = (Utils.getOptionPos('B',options) >= 0) ? Integer.parseInt(Utils.getOption('B',options)) : nWin;
		} catch(Exception e) {
			throw new Exception("[Error] Failed to parse option B");
		}

		// Partially labelled ?
		double rLabeled = 1.0; 
		try {
			rLabeled = (Utils.getOptionPos("semisupervised", options) >= 0) ? Double.parseDouble(Utils.getOption("semisupervised", options)) : rLabeled;
		} catch(IOException e) {
			throw new Exception("[Error] Failed to parse option S");
		}

		// Get Threshold
		String Top = (Utils.getOptionPos("threshold",options) >= 0) ? Utils.getOption("threshold",options) : "PCut1";

		// Get Verbosity (do we want to see everything?)
		String Vop = (Utils.getOptionPos("verbosity",options) >= 0) ? Utils.getOption("verbosity",options) : "3";

		if (h.getDebug()) System.out.println(":- Dataset -: "+MLUtils.getDatasetName(D)+"\tL="+D.classIndex()+"");

		Result results[] = evaluateModel(h,D,nWin,rLabeled,Top,Vop);
		
		// Return the final evaluation window.
		return results[results.length-1];
	}

	private static String measures[] = new String[]{"Accuracy", "Exact match", "Hamming score", "Build_time", "Total_time"};

	/**
	 * EvaluateModel - over 20 windows.
	 */
	public static Result[] evaluateModel(MultilabelClassifier h, Instances D) throws Exception {
		return evaluateModel(h,D,20,1.0,"PCut1","3");
	}

	/**
	 * EvaluateModel - Evaluate a multi-label data-stream model over a moving window.
	 * The window is sampled every N/numWindows instances, for a total of numWindows windows.
	 */
	public static Result[] evaluateModel(MultilabelClassifier h, Instances D, int numWindows, double rLabeled, String Top, String Vop) throws Exception {

		if (h.getDebug())
			System.out.println(":- Classifier -: "+h.getClass().getName()+": "+Arrays.toString(h.getOptions()));

		int N = D.numInstances();
		int L = D.classIndex();

		Result results[] = new Result[numWindows-1];		// we don't record the results from the initial window

		long train_time = 0;
		long test_time = 0;

		int windowSize = (int) Math.floor(D.numInstances() / (double)numWindows);

		if (rLabeled * windowSize < 1.)
			throw new Exception ("[Error] The ratio of labelled instances ("+rLabeled+") is too small given the window size!");

		double nth = 1. / rLabeled; // label every nth example

		Instances D_init = new Instances(D,0,windowSize); 	// initial window

		if (h.getDebug()) {
			System.out.println("Training classifier on initial window ...");
		}
		train_time = System.currentTimeMillis();
		h.buildClassifier(D_init); 										// initial classifir
		train_time = System.currentTimeMillis() - train_time;
		System.out.println("Done (in "+(train_time/1000.0)+" s)");
		D = new Instances(D,windowSize,D.numInstances()-windowSize); 	// the rest (after the initial window)

		double t[] = new double[L];
		Arrays.fill(t,0.5);

		// @todo move into function
		int V = MLUtils.getIntegerOption(Vop,3);
		if (V > 3) {
			System.out.println("--------------------------------------------------------------------------------");
			System.out.print("#"+Utils.padLeft("w",6)+" "+Utils.padLeft("n",6));
			for (String m : measures) {
				System.out.print(" ");
				System.out.print(Utils.padLeft(m,12));
			}
			System.out.println("");
			System.out.println("--------------------------------------------------------------------------------");
		}

		int i = 0;
		for (int w = 0; w < numWindows-1; w++) {

			results[w] = new Result(L);
			results[w].setInfo("Supervision",String.valueOf(rLabeled));

			int n = 0;
			test_time = 0;
			train_time = 0;
			for(int c = 0; i < (w*windowSize)+windowSize; i++) {

				Instance x = D.instance(i);
				AbstractInstance x_ = (AbstractInstance)((AbstractInstance) x).copy(); 		// copy 
				// (we can't clear the class values because certain classifiers need to know how well they're doing
				// -- just trust that there's no cheating!)
				//for(int j = 0; j < L; j++)  
				//	x_.setValue(j,0.0);
				
				if ( rLabeled < 0.5 && (i % (int)(1/rLabeled) == 0) || ( rLabeled >= 0.5 && (i % (int)(1./(1.-rLabeled)) != 0 )) ) {
					// LABELLED - Test & record prediction 
					long before_test = System.currentTimeMillis();
					double y[] = h.distributionForInstance(x_);
					long after_test = System.currentTimeMillis();
					test_time += (after_test-before_test); // was +=
					results[w].addResult(y,x);
					n++;
				}
				else {
					// UNLABELLED
					x = MLUtils.setLabelsMissing(x,L);
				}

				// UPDATE (The classifier will have to decide if it wants to deal with unlabelled instances.)
				long before = System.currentTimeMillis();
				((UpdateableClassifier)h).updateClassifier(x);
				long after = System.currentTimeMillis();
				train_time += (after-before); // was +=
			}

			// calculate results
			results[w].setInfo("Type","ML");
			results[w].setInfo("Threshold", Arrays.toString(t));
			results[w].output = Result.getStats(results[w],"2");
			results[w].output.put("Test_time",(test_time)/1000.0);
			results[w].output.put("Build_time",(train_time)/1000.0);
			results[w].output.put("Total_time",(test_time+train_time)/1000.0);

			// Display results (to CLI)
			System.out.print("#"+Utils.doubleToString((double)w+1,6,0)+" "+Utils.doubleToString((double)n,6,0));
			n = 0;
			for (String m : measures) {
				System.out.print(" ");
				System.out.print(Utils.doubleToString(results[w].output.get(m),12,4));
			} System.out.println("");

			// Calibrate threshold for next window
			if (Top.equals("PCutL")) {
				t = ThresholdUtils.calibrateThresholds(results[w].predictions,MLUtils.labelCardinalities(results[w].actuals));
			}
			else {
				Arrays.fill(t,ThresholdUtils.calibrateThreshold(results[w].predictions,MLUtils.labelCardinality(results[w].allActuals())));
			}
		}

		if (h.getDebug()) {
			System.out.println("--------------------------------------------------------------------------------");
			//System.out.println("Average results are as follows:\n");
		}
		Result avg = MLEvalUtils.averageResults(results);
		// @todo put in earlier?
		avg.setInfo("Classifier_name",h.getClass().getName());
		avg.setInfo("Classifier_ops",Arrays.toString(h.getOptions()));
		avg.setInfo("Classifier_info",h.toString());
		avg.setInfo("Dataset_name",MLUtils.getDatasetName(D));
		System.out.println(avg);

		// @todo in the future, will want to return all results ... but was having problems with this ...
		//return results;
		return new Result[]{avg};
	}

	public static void printOptions(Enumeration e) {

		// Evaluation Options
		StringBuffer text = new StringBuffer();
		text.append("\n\nEvaluation Options:\n\n");
		text.append("-t\n");
		text.append("\tSpecify the dataset (required)\n");
		text.append("-B <number of windows>\n");
		text.append("\tSets the number of windows (batches) for evalutation; default: 20.\n");
		text.append("-semisupervised <ratio labelled>\n");
		text.append("\tSets the ratio of labelled instances; default: 1.0.\n");
		text.append("-threshold <threshold>\n");
		text.append("\tSets the type of thresholding; where 'PCut1' automatically calibrates a threshold (the default); 'PCutL' automatically calibrates one threshold for each label; and any double number, e.g. '0.5', specifies that threshold.\n");
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
