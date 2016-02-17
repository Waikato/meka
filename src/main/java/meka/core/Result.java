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

package meka.core;

import weka.core.Instance;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Result - Stores predictions alongside true labels, for evaluation. 
 * For more on the evaluation and threshold selection implemented here; see: 
 * <p>
 * Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer (2011).<br>
 * Jesse Read, <i>Scalable Multi-label Classification</i>. PhD Thesis, University of Waikato, Hamilton, New Zealand (2010).<br>
 * </p>
 * @author 	Jesse Read
 * @version	March 2012 - Multi-target Compatible
 */
public class Result implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The number of label (target) variables in the problem */
	public int L = 0;

	public ArrayList<double[]> predictions = null;
	// TODO, store in sparse fashion with either LabelSet or LabelVector
	public ArrayList<int[]> actuals = null;

	public HashMap<String,String> info = new LinkedHashMap<String,String>();  // stores general dataset/classifier info
	public HashMap<String,Object> output = new LinkedHashMap<String,Object>();// stores predictive evaluation statistics
	public HashMap<String,Object> vals = new LinkedHashMap<String,Object>();  // stores non-predictive evaluation stats
	public HashMap<String,String> model = new LinkedHashMap<String,String>(); // stores the model itself

	public Result() {
		predictions = new ArrayList<double[]>();
		actuals = new ArrayList<int[]>();
	}

	public Result(int L) {
		predictions = new ArrayList<double[]>();
		actuals = new ArrayList<int[]>();
		this.L = L;
	}

	public Result(int N, int L) {
		predictions = new ArrayList<double[]>(N);
		actuals = new ArrayList<int[]>(N);
		this.L = L;
	}

	/** The number of value-prediction pairs stared in this Result */
	public int size() {
		return predictions.size();
	}

	/**
	 * Provides a nice textual output of all evaluation information.
	 * @return	String representation
	 */
	@Override
	public String toString() {

		StringBuilder resultString = new StringBuilder();
		if (info.containsKey("Verbosity")) {
			int V = MLUtils.getIntegerOption(info.get("Verbosity"),1);

			if ( V > 4) {
				resultString.append("== Individual Errors\n\n");
				// output everything
				resultString.append(Result.getResultAsString(this,V-5) + "\n\n");
			}
		}
		// output the stats in general
		if (model.size() > 0)
			resultString.append("== Model info\n\n" + MLUtils.hashMapToString(model));
		resultString.append("== Evaluation Info\n\n" + MLUtils.hashMapToString(info));
		resultString.append("\n\n== Predictive Performance\n\n" + MLUtils.hashMapToString(output,3));
		String note = "";
		if (info.containsKey("Type") && info.get("Type").endsWith("CV")) {
			note = " (averaged across folds)";
		}
		resultString.append("\n\n== Additional Measurements"+note+"\n\n" + MLUtils.hashMapToString(vals,3));


		resultString.append("\n\n");
		return resultString.toString();
	}

	/**
	 * AddResult - Add an entry.
	 * @param pred	predictions
	 * @param real  an instance containing the true label values
	 */
	public void addResult(double pred[], Instance real) {
		predictions.add(pred);
		actuals.add(MLUtils.toIntArray(real,pred.length));
		
	}

	/**
	 * RowActual - Retrieve the true values for the i-th instance.
	 */
	public int[] rowTrue(int i) {
		return actuals.get(i);
	}

	/**
	 * RowConfidence - Retrieve the prediction confidences for the i-th instance.
	 */
	public double[] rowConfidence(int i) {
		return predictions.get(i);
	}

	/**
	 * RowPrediction - Retrieve the predicted values for the i-th instance according to threshold t.
	 */
	public int[] rowPrediction(int i, double t) {
		return A.toIntArray(rowConfidence(i), t);
	}

	/**
	 * RowPrediction - Retrieve the predicted values for the i-th instance according to pre-calibrated/chosen threshold.
	 */
	public int[] rowPrediction(int i) {
		String t = info.get("Threshold");
		if (t != null) {
			// For multi-label data, should know about a threshold first
			return ThresholdUtils.threshold(rowConfidence(i), t);
		}
		else {
			// Probably multi-target data (no threshold allowed)
			return A.toIntArray(rowConfidence(i));
		}
	}

	/**
	 * ColConfidence - Retrieve the prediction confidences for the j-th label (column).
	 * Similar to M.getCol(Y,j)
	 */
	public double[] colConfidence(int j) {
		double y[] = new double[predictions.size()];
		for(int i = 0; i < predictions.size(); i++) {
			y[i] = rowConfidence(i)[j];
		}
		return y;
	}

	/**
	 * AllPredictions - Retrieve all prediction confidences in an L * N matrix (2d array).
	 */
	public double[][] allPredictions() {
		double Y[][] = new double[predictions.size()][];
		for(int i = 0; i < predictions.size(); i++) {
			Y[i] = rowConfidence(i);
		}
		return Y;
	}

	/**
	 * AllPredictions - Retrieve all predictions (according to threshold t) in an L * N matrix.
	 */
	public int[][] allPredictions(double t) {
		int Y[][] = new int[predictions.size()][];
		for(int i = 0; i < predictions.size(); i++) {
			Y[i] = rowPrediction(i,t);
		}
		return Y;
	}

	/**
	 * AllTrueValues - Retrieve all true values in an L x N matrix.
	 */
	public int[][] allTrueValues() {
		int Y[][] = new int[actuals.size()][];
		for(int i = 0; i < actuals.size(); i++) {
			Y[i] = rowTrue(i);
		}
		return Y;
	}

	/*
	 * AddValue.
	 * Add v to an existing metric value.
	public void addValue(String metric, double v) {
		Double freq = (Double)vals.get(metric);
		vals.put(metric,(freq == null) ? v : freq + v);
	}
	*/

	/**
	 * Return the set of metrics for which measurements are available.
	 */
	public Set<String> availableMetrics() {
		return output.keySet();
	}

	/**
	 * Set the measurement for metric 'metric'.
	 */
	public void setMeasurement(String metric, Object stat) { output.put(metric,stat); }

    /**
     * Retrieve the measurement for metric 'metric'.
     */
	public Object getMeasurement(String metric) { return output.get(metric); }

	/**
	 * SetValue.
	 * Add an evaluation metric and a value for it.
	 */
	public void setValue(String metric, double v) {
		vals.put(metric,v);
	}

	/**
	 * AddValue.
	 * Retrieve the value for metric 'metric'
	 */
	public Object getValue(String metric) { return vals.get(metric); }

	/**
	 * SetInfo.
	 * Set a String value to an information category.
	 */
	public void setInfo(String cat, String val) {
		info.put(cat,val);
	}

	/**
	 * GetInfo.
	 * Get the String value of category 'cat'.
	 */
	public String getInfo(String cat) {
		return info.get(cat);
	}

	/**
	 * Set a model string.
	 */
	public void setModel(String key, String val) {
		model.put(key, val);
	}

	/**
	 * Get the model value.
	 */
	public String getModel(String key) {
		return model.get(key);
	}

	// ********************************************************************************************************
	//                     STATIC METHODS
	// ********************************************************************************************************
	//

	/**
	 * GetStats.
	 * Return the evaluation statistics given predictions and real values stored in r.
	 * In the multi-label case, a Threshold category must exist, containing a string defining the type of threshold we want to use/calibrate.
	 */
	public static HashMap<String,Object> getStats(Result r, String vop) {
		if (r.getInfo("Type").startsWith("MT"))
			return MLEvalUtils.getMTStats(r.allPredictions(),r.allTrueValues(), vop);
		else 
			return MLEvalUtils.getMLStats(r.allPredictions(), r.allTrueValues(), r.getInfo("Threshold"), vop);
	}

	/**
	 * GetResultAsString - print out each prediction in a Result along with its true labelset.
	 */
	public static String getResultAsString(Result s) {
		return getResultAsString(s,3);
	}

	/**
	 * WriteResultToFile -- write a Result 'result' out in plain text format to file 'fname'.
	 * @param result Result
	 * @param fname file name
	 */
	public static void writeResultToFile(Result result, String fname) throws Exception {
		PrintWriter outer = new PrintWriter(new BufferedWriter(new FileWriter(fname)));
		outer.write(result.toString());
		outer.close();
	} 

	/**
	 * Convert a list of Results into an Instances.
	 * @param results An ArrayList of Results
	 * @return	Instances
	 */
	public static Instances getResultsAsInstances(ArrayList<HashMap<String,Object>> metrics) {

		HashMap<String,Object> o_master = metrics.get(0);
		ArrayList<Attribute> attInfo = new ArrayList<Attribute>();
		for (String key : o_master.keySet())  {
			if (o_master.get(key) instanceof Double) {
				//System.out.println("key="+key);
				attInfo.add(new Attribute(key));
			}
		}

		Instances resultInstances = new Instances("Results",attInfo,metrics.size());

		for (HashMap<String,Object> o : metrics) {
			Instance rx = new DenseInstance(attInfo.size());
			for (Attribute att : attInfo) {
				String name = att.name();
				rx.setValue(att,(double)o.get(name));
			}
			resultInstances.add(rx);
		}

		//System.out.println(""+resultInstances);
		return resultInstances;

	}

	/**
	 * GetResultAsString - print out each prediction in a Result (to a certain number of decimal points) along with its true labelset.
	 */
	public static String getResultAsString(Result result, int adp) {
		StringBuilder sb = new StringBuilder();
		double N = (double)result.predictions.size();
		sb.append("|==== PREDICTIONS (N="+N+") =====>\n");
		for(int i = 0; i < N; i++) {
			sb.append("|");
			sb.append(Utils.doubleToString((i+1),5,0));
			sb.append(" ");
			//System.out.println(""+result.info.get("Threshold"));
			//System.out.println("|"+A.toString(result.rowPrediction(i)));
			//System.out.println("|"+MLUtils.toIndicesSet(result.rowPrediction(i)));
			if (adp == 0 && !result.getInfo("Type").equalsIgnoreCase("MT")) {
				LabelSet y = new LabelSet(MLUtils.toIndicesSet(result.actuals.get(i)));
				sb.append(y).append(" ");
				LabelSet ypred = new LabelSet(MLUtils.toIndicesSet(result.rowPrediction(i)));
				sb.append(ypred).append("\n");
			}
			else {
				sb.append(A.toString(result.actuals.get(i))).append(" ");
				sb.append(A.toString(result.predictions.get(i),adp)).append("\n");
			}
		}
		sb.append("|==============================<\n");
		return sb.toString();
	}

}

