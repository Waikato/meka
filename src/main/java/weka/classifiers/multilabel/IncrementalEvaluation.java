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

package weka.classifiers.multilabel;

import weka.core.*;
import weka.classifiers.*;
import meka.gui.explorer.*;
import weka.classifiers.multilabel.*;
import java.util.*;
import java.io.*;

/**
 * IncrementalEvaluation.java - For Evaluating Incremental (Updateable) Classifiers.
 * @author 		Jesse Read (jesse@tsc.uc3m.es)
 * @version 	Feb 2013
 */
public class IncrementalEvaluation {

	/*
	 * RunExperiment.
	 * Build and evaluate a model with command-line options.
	@Deprecated
	public static void evaluation(MultilabelClassifier h, String args[]) {
		try {
			 IncrementalEvaluation.runExperiment(h,args);
		} catch(Exception e) {
			System.err.println("Evaluation exception ("+e+"); failed to run experiment");
			e.printStackTrace();
			IncrementalEvaluation.printOptions(h.listOptions());
		}
	}
	*/

	/**
	 * RunExperiment - Build and evaluate a model with command-line options.
	 * @param	h			a multi-label updateable classifier
	 * @param	args[]		classifier + dataset options
	 */
	public static void runExperiment(MultilabelClassifier h, String args[]) {
		try {
			h.setOptions(args);
			evaluateModel(h,args);
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
		Instances D = Evaluation.loadDatasetFromOptions(options);
		try {
			// Set C option from Data 
			Explorer.prepareData(D);
		} catch(Exception e) {
			System.err.println("[Warning] Oops, didn't find the -C option int he dataset, looking in command-line options...");
			try {
				Evaluation.setClassesFromOptions(D,options);
			} catch(Exception e2) {
				e2.printStackTrace();
				throw new Exception("[Error] ]You must supply the number of labels either in the @Relation tag or on the command line: -C <num> !");
			}
		}

		// Set the number of windows (batches) @todo move below combining options?
		int nWin = 20;
		try {
			if(Utils.getOptionPos('B',options) >= 0) {
				nWin = Integer.parseInt(Utils.getOption('B', options));
			}
		} catch(IOException e) {
			e.printStackTrace();
			throw new Exception("[Error] Failed to parse option B, using default: B = "+nWin);
		}

		// Get the Options in the @relation name (in format 'dataset-name: <options>')
		String doptions[] = null;
		try {
			doptions = MLUtils.getDatasetOptions(D);
		} catch(Exception e) {
			throw new Exception("[Error] Failed to Set Options from @Relation Name");
		}

		// Partially labelled ?
		double rLabeled = 1.0; 
		try {
			if(Utils.getOptionPos("semisupervised", options) >= 0) {
				rLabeled = Double.parseDouble(Utils.getOption("semisupervised", options));
			}
		} catch(IOException e) {
			e.printStackTrace();
			throw new Exception("[Error] Failed to parse option S, using default: S = "+rLabeled+" ("+(rLabeled*100.0)+"% labelled)");
		}

		options = Utils.splitOptions(Utils.joinOptions(options) + "," + Utils.joinOptions(doptions)+", ");

		if (h.getDebug()) System.out.println(":- Dataset -: "+MLUtils.getDatasetName(D)+"\tL="+D.classIndex()+"");

		Result results[] = evaluateModel(h,D,nWin,rLabeled);

		// Return the final evaluation window.
		return results[results.length-1];
	}

	static String measures[] = new String[]{"Accuracy", "Exact_match", "H_acc", "Build_time", "Total_time"};

	/**
	 * EvaluateModel - over 20 windows.
	 */
	public static Result[] evaluateModel(MultilabelClassifier h, Instances D) throws Exception {
		return evaluateModel(h,D,20,1.0);
	}

	/**
	 * EvaluateModel - Evaluate a multi-label data-stream model over a moving window.
	 * The window is sampled every N/numWindows instances, for a total of numWindows windows.
	 */
	public static Result[] evaluateModel(MultilabelClassifier h, Instances D, int numWindows, double rLabeled) throws Exception {

		if (h.getDebug())
			System.out.println(":- Classifier -: "+h.getClass().getName()+": "+Arrays.toString(h.getOptions()));

		int L = D.classIndex();
		Result results[] = new Result[numWindows-1];		// we don't record the results from the initial window
		results[0] = new Result(L);
		results[0].setInfo("Supervision",String.valueOf(rLabeled));
		int windowSize = (int)Math.floor(D.numInstances() * rLabeled / (double)numWindows);
		Instances D_init = new Instances(D,0,windowSize); 	// initial window
		h.buildClassifier(D_init); 							// initial classifir
		double t = 0.5;										// initial threshold
		Random r = new Random(0); 							// for partially-labelled / semi-supervised

		long train_time = 0;
		long test_time = 0;

		// @todo move into function
		if (h.getDebug()) {
			System.out.println("----------------------------------------");
			System.out.print("#"+Utils.padLeft("i",6)+" , "+Utils.padLeft("w",6));
			for (String m : measures) {
				System.out.print(" , ");
				System.out.print(Utils.padLeft(m,12));
			}
			System.out.println("");
			System.out.println("----------------------------------------");
		}

		int w_num = 0;
		D = new Instances(D,windowSize,D.numInstances()-windowSize); // the rest (after the initial window)
		int i = 0;
		for (Instance x : D) {
			//Instance x = D.instance(i);
			AbstractInstance x_ = (AbstractInstance)((AbstractInstance) x).copy(); 		// copy 
																						// but don't clear the values, we may need this for ADWIN
			//for(int j = 0; j < L; j++)  
			//	x_.setValue(j,0.0);

			boolean unlabeled = false;
			if (r.nextDouble() > rLabeled) {
				// unlabel this instance
				for(int j = 0; j < L; j++) {
					x.setMissing(j);
				}
				unlabeled = true;
			}
			else {
				// test & record prediction 
				long before_test = System.currentTimeMillis();
				double y[] = h.distributionForInstance(x_);
				long after_test = System.currentTimeMillis();
				test_time += (after_test-before_test);
				results[w_num].addResult(y,x);
				i++;
			}

			//if (!unlabeled) {
				// update 
				long before = System.currentTimeMillis();
				((UpdateableClassifier)h).updateClassifier(x);
				long after = System.currentTimeMillis();
				train_time += (after-before);
			//}

			// evaluate every windowSize-th instance
			if (i == windowSize) {
				if (h.getDebug()) 
					System.out.print("#"+Utils.doubleToString((double)i*w_num,6,0)+" , "+Utils.doubleToString((double)w_num,6,0));
				i = 0;
				// calculate results
				results[w_num].setInfo("Type","ML");
				results[w_num].setInfo("Threshold", String.valueOf(t));
				results[w_num].output = Result.getStats(results[w_num]);
				//HashMap<String,Double> o = MLEvalUtils.getMLStats(results[w_num].predictions,results[w_num].actuals,String.valueOf(t));
				results[w_num].output.put("Test_time",(test_time)/1000.0);
				results[w_num].output.put("Build_time",(train_time)/1000.0);
				results[w_num].output.put("Total_time",(test_time+train_time)/1000.0);

				// display results (to CLI)
				if (h.getDebug()) {
					for (String m : measures) {
						System.out.print(" , ");
						System.out.print(Utils.doubleToString(results[w_num].output.get(m),12,4));
					}
					System.out.println("");
				}

				// set threshold for next window
				t = MLEvalUtils.calibrateThreshold(results[w_num].predictions,results[w_num].output.get("LCard_real"));
				w_num++;
				if (w_num < results.length) {
					results[w_num] = new Result(L);
				}
				else
					break;
			}

		}

		if (h.getDebug()) {
			System.out.println("----------------------------------------");
			System.out.println("Average results are as follows:\n");
			Result avg = MLEvalUtils.averageResults(results);
			System.out.println(avg);
		}

		return results;
	}

	public static void printOptions(Enumeration e) {

		// Evaluation Options
		StringBuffer text = new StringBuffer();
		text.append("\n\nEvaluation Options:\n\n");
		text.append("-t\n");
		text.append("\tSpecify the dataset (required)\n");
		text.append("-B <number of windows>\n");
		text.append("\tSets the number of windows (batches) for evalutation; default: 20.\n");
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
