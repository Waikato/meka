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
import weka.core.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

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

	public int L = 0;

	public ArrayList<double[]> predictions = null;
	// TODO, store in sparse fashion with either LabelSet or LabelVector
	public ArrayList<int[]> actuals = null;

	public HashMap<String,Double> output = new LinkedHashMap<String,Double>();
	public HashMap<String,String> info = new LinkedHashMap<String,String>();
	public HashMap<String,Double> vals = new LinkedHashMap<String,Double>();

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

	public int size() {
		return predictions.size();
	}

	@Override
	public String toString() {
		String resultString = "";
		if (info.containsKey("Verbosity")) { 
			int V = MLUtils.getIntegerOption(info.get("Verbosity"),1);
			if ( V > 4) {
				// output everything
				resultString = Result.getResultAsString(this,V-5);
			}

		}
		// output the stats in general
		return resultString + MLUtils.hashMapToString(info) + "\n" + MLUtils.hashMapToString(output,3) + "\n" + MLUtils.hashMapToString(vals,3);
	}

	/**
	 * AddResult - Add an entry.
	 */
	public void addResult(double pred[], Instance real) {
		predictions.add(pred);
		actuals.add(MLUtils.toIntArray(real,pred.length));
	}

	/**
	 * RowActual - Retrive the true values for the i-th instance.
	 */
	public int[] rowActual(int i) {
		return actuals.get(i);
	}

	/**
	 * RowRanking - Retrive the prediction confidences for the i-th instance.
	 * <br>
	 * TODO rename to rowConfidence
	 */
	public double[] rowRanking(int i) {
		return predictions.get(i);
	}

	/**
	 * RowPrediction - Retrive the predicted values for the i-th instance according to threshold t.
	 */
	public int[] rowPrediction(int i, double t) {
		return MLUtils.toIntArray(rowRanking(i),t);
	}

	/**
	 * RowPrediction - Retrive the predicted values for the i-th instance according to pre-calibrated/chosen threshold.
	 */
	public int[] rowPrediction(int i) {
		return ThresholdUtils.threshold(rowRanking(i),info.get("Threshold"));
	}

	/**
	 * AllPredictions - Retrive all prediction confidences in an L * N matrix.
	 */
	public double[][] allPredictions() {
		double Y[][] = new double[predictions.size()][];
		for(int i = 0; i < predictions.size(); i++) {
			Y[i] = rowRanking(i);
		}
		return Y;
	}

	/**
	 * AllPredictions - Retrive all predictions (according to threshold t) in an L * N matrix.
	 */
	public int[][] allPredictions(double t) {
		int Y[][] = new int[predictions.size()][];
		for(int i = 0; i < predictions.size(); i++) {
			Y[i] = rowPrediction(i,t);
		}
		return Y;
	}

	/**
	 * AllActuals - Retrive all true predictions in an L x N matrix.
	 */
	public int[][] allActuals() {
		int Y[][] = new int[actuals.size()][];
		for(int i = 0; i < actuals.size(); i++) {
			Y[i] = rowActual(i);
		}
		return Y;
	}

	/**
	 * AddValue.
	 * Add v to an existing metric value.
	 */
	public void addValue(String metric, double v) {
		Double freq = vals.get(metric);
		vals.put(metric,(freq == null) ? v : freq + v);
	}

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
	public double getValue(String metric) {
		return vals.get(metric);
	}

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

	// ********************************************************************************************************
	//                     STATIC METHODS
	// ********************************************************************************************************
	//

	/**
	 * GetStats.
	 * Return the evaluation statistics given predictions and real values stored in r.
	 * In the multi-label case, a Threshold category must exist, containing a string defining the type of threshold we want to use/calibrate.
	 */
	public static HashMap<String,Double> getStats(Result r, String vop) {
		if (r.getInfo("Type").equalsIgnoreCase("MT"))
			return MLEvalUtils.getMTStats(r.allPredictions(),r.allActuals(), vop);
		else 
			return MLEvalUtils.getMLStats(r.allPredictions(), r.allActuals(), r.getInfo("Threshold"), vop);
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
	 * GetResultAsString - print out each prediction in a Result (to a certain number of decimal points) along with its true labelset.
	 */
	public static String getResultAsString(Result s, int adp) {
		StringBuilder sb = new StringBuilder();
		double N = (double)s.predictions.size();
		sb.append("|==== PREDICTIONS (N="+N+") =====>\n");
		for(int i = 0; i < N; i++) {
			sb.append("|");
			sb.append(Utils.doubleToString((i+1),5,0));
			sb.append(" ");
			if (adp == 0 && !s.getInfo("Type").equalsIgnoreCase("MT")) {
				LabelSet y = new LabelSet(MLUtils.toIndicesSet(s.actuals.get(i)));
				sb.append(y).append(" ");
				LabelSet ypred = new LabelSet(MLUtils.toIndicesSet(s.rowPrediction(i)));
				sb.append(ypred).append("\n");
			}
			else {
				sb.append(A.toString(s.actuals.get(i)));
				sb.append(" ");
				sb.append(A.toString(s.predictions.get(i),adp));
				sb.append("\n");
			}
		}
		sb.append("|==============================<\n");
		return sb.toString();
	}

}

