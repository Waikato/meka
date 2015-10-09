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

import weka.core.*;
import java.util.*;

/**
 * MLEvalUtils - Utility functions for Evaluation.
 * @see meka.core.Metrics
 * @author 	Jesse Read
 * @version	March 2014
 */
public abstract class MLEvalUtils {

	/**
	 * GetThreshold - Get a threshold from a Threshold OPtion string 'top'.
	 * @param	Y	label space; for calculating a threshold with PCut
	 * @param	D	training data; for calculating a threshold with PCut
	 * @param	top Threshold OPtion (either "PCut1", "PCutL" or a real value e.g. "0.5" or L real values e.g. "[0.1, 0.2, 0.8]" for L = 3
	 */
	public static String getThreshold(ArrayList<double[]> Y, Instances D, String top) throws Exception {
		if (top.equals("PCut1") || top.equals("c")) {			// Proportional Cut threshold (1 general threshold)
			return String.valueOf(ThresholdUtils.calibrateThreshold(Y,MLUtils.labelCardinality(D)));
		}	
		else if (top.equals("PCutL") || top.equals("C")) {		// Proportional Cut thresholds (one for each Label)
			return Arrays.toString(ThresholdUtils.calibrateThresholds(Y,MLUtils.labelCardinalities(D)));
		}
		else {
			// Set our own threshold (we assume top = "0.5" or top = "[0.1,...,0.3]" (we make no checks here!)
			return top;
		}
	}

	/**
	 * GetMLStats - Given predictions and corresponding true values and a threshold string, retreive statistics.
	 * @param	Rpred	predictions (may be real-valued confidences)
	 * @param	Y   	corresponding true values
	 * @param	t		a threshold string, e.g. "0.387"
	 * @param	vop		the verbosity option, e.g. "5"
	 * @return	        the evaluation statistics
	 */
	public static HashMap<String,Object> getMLStats(double Rpred[][], int Y[][], String t, String vop) {
		double ts[] = ThresholdUtils.thresholdStringToArray(t,Y[0].length);
		return getMLStats(Rpred,Y,ts,vop);
	}

	/**
	 * GetMLStats - Given predictions and corresponding true values and a threshold string, retreive statistics.
	 * @param	Rpred	predictions (may be double-valued confidences in the multi-label case)
	 * @param	Y   	corresponding true values
	 * @param	t		a vector of thresholds, e.g. [0.1,0.1,0.1] or [0.1,0.5,0.4,0.001]
	 * @return	    the evaluation statistics
	 */
	public static HashMap<String,Object> getMLStats(double Rpred[][], int Y[][], double t[], String vop) {

		int N = Y.length; 
		int L = Y[0].length;

		int V = MLUtils.getIntegerOption(vop,1); // default 1

		int Ypred[][] = ThresholdUtils.threshold(Rpred,t);

		HashMap<String,Object> results = new LinkedHashMap<String,Object>();

		results.put("Number of test instances (N)"			,(int)N);
		results.put("Accuracy"			,Metrics.P_Accuracy(Y,Ypred));
		results.put("Jaccard index"		,Metrics.P_Accuracy(Y,Ypred));
		results.put("Hamming score"		,Metrics.P_Hamming(Y,Ypred));
		results.put("Exact match"		,Metrics.P_ExactMatch(Y,Ypred));

		if (V > 1) {

			results.put("Jaccard distance"	,Metrics.L_JaccardDist(Y,Ypred));
			results.put("Hamming loss"		,Metrics.L_Hamming(Y,Ypred));
			results.put("ZeroOne loss"		,Metrics.L_ZeroOne(Y,Ypred));
			results.put("Harmonic score"	,Metrics.P_Harmonic(Y,Ypred));
			results.put("One error"			,Metrics.L_OneError(Y,Rpred));
			results.put("Rank loss"			,Metrics.L_RankLoss(Y,Rpred));
			results.put("Avg precision"		,Metrics.P_AveragePrecision(Y,Rpred));
			results.put("Log Loss (lim. L)"	,Metrics.L_LogLossL(Y,Rpred));
			results.put("Log Loss (lim. D)"	,Metrics.L_LogLossD(Y,Rpred));
			//if (V > 3) {
			//	results.put("Precision"		    ,Metrics.P_Precision(Y,Ypred));
			//	results.put("Recall"			,Metrics.P_Recall(Y,Ypred));
			//}
			results.put("F1 (micro averaged)"				,Metrics.P_FmicroAvg(Y,Ypred));
			results.put("F1 (macro averaged by example)"	,Metrics.P_FmacroAvgD(Y,Ypred));
			results.put("F1 (macro averaged by label)"		,Metrics.P_FmacroAvgL(Y,Ypred));
			results.put("AUPRC (macro averaged)"		    ,Metrics.P_macroAUPRC(Y,Rpred));
			results.put("AUROC (macro averaged)"		    ,Metrics.P_macroAUROC(Y,Rpred));
			// This will not be displayed to text output, rather as a graph
			results.put("Curve Data"		                ,Metrics.curveData(Y,Rpred));
			//results.put("Macro Curve Data"		            ,Metrics.curveDataMacroAveraged(Y,Rpred));
			results.put("Micro Curve Data"		            ,Metrics.curveDataMicroAveraged(Y,Rpred));

			if (V > 2) {
				results.put("Label indices              "	,A.make_sequence(L));
				double HL[] = new double[L];
				double HA[] = new double[L];
				double Pr[] = new double[L];
				double Re[] = new double[L];
				for(int j = 0; j < L; j++) {
					HL[j] = Metrics.P_Hamming(Y,Ypred,j);
					HA[j] = Metrics.P_Harmonic(Y,Ypred,j);
					Pr[j] = Metrics.P_Precision(Y,Ypred,j);
					Re[j] = Metrics.P_Recall(Y,Ypred,j);
				}
				results.put("Accuracy (per label)"	        ,HL);
				if (V > 3) {
					results.put("Harmonic (per label)"	    ,HA);
					results.put("Precision (per label)"	    ,Pr);
					results.put("Recall (per label)"		,Re);
				}
			}

			if (V > 2) {
				results.put("Empty labelvectors (predicted)"	,MLUtils.emptyVectors(Ypred));
				results.put("Label cardinality (predicted)"		,MLUtils.labelCardinality(Ypred));
				results.put("Levenshtein distance", Metrics.L_LevenshteinDistance(Y, Ypred));
				if (V > 3) {
					// Label cardinality
					results.put("Label cardinality (difference)"		,MLUtils.labelCardinality(Y)-MLUtils.labelCardinality(Ypred));
					double diff_LC[] = new double[L];
					double true_LC[] = new double[L];
					double pred_LC[] = new double[L];
					for(int j = 0; j < L; j++) {
						diff_LC[j] = MLUtils.labelCardinality(Y,j) - MLUtils.labelCardinality(Ypred,j);
						true_LC[j] = MLUtils.labelCardinality(Y,j);
						pred_LC[j] = MLUtils.labelCardinality(Ypred,j);
					}
					results.put("avg. relevance (test set)"		,true_LC);
					results.put("avg. relevance (predicted)     "		,pred_LC);
					results.put("avg. relevance (difference)     "	,diff_LC);
				}
			}
		}
		return results;
	}

	/**
	 * GetMTStats - Given multi-target predictions and corresponding true values, retreive evaluation statistics.
	 * @param	Rpred	predictions
	 * @param	Y	    corresponding true values
	 * @return	        the evaluation statistics
	 */
	public static HashMap<String,Object> getMTStats(double Rpred[][], int Y[][], String vop) {

		// just a question of rounding for now, could use A.toIntArray(..)
		int Ypred[][] = ThresholdUtils.round(Rpred);

		int N = Y.length;
		int L = Y[0].length;
		int V = MLUtils.getIntegerOption(vop,1); // default 1

		HashMap<String,Object> output = new LinkedHashMap<String,Object>();
		output.put("N(test)"            ,(double)N);
		output.put("L"					,(double)L);
		output.put("Hamming score"		,Metrics.P_Hamming(Y,Ypred));
		output.put("Exact match"		,Metrics.P_ExactMatch(Y,Ypred));

		if (V > 1) {
			output.put("Hamming loss"		,Metrics.L_Hamming(Y,Ypred));
			output.put("ZeroOne loss"		,Metrics.L_ZeroOne(Y,Ypred));
		}
		if (V > 2) {
			output.put("Levenshtein distance", Metrics.L_LevenshteinDistance(Y, Ypred));

			double HL[] = new double[L];
			for(int j = 0; j < L; j++) {
				HL[j] = Metrics.P_Hamming(Y,Ypred,j);
			}
			output.put("Label indices              "	,A.make_sequence(L));
			output.put("Accuracy (per label)"	        ,HL);
		}
		if (V > 3) {
			//output.put("Levenshtein distance", Metrics.L_LevenshteinDistance(Y, Ypred));
		}
		return output;
	}

	/**
	 * Combine Predictions - Combine together various results (for example, from cross-validation)
	 * into one, simply by appending predictions and true values together, and averaging together their 'vals'.
	 * @param folds	an array of Results
	 * @return a combined Result
	 */
	public static Result combinePredictions(Result folds[]) { 
		Result r = new Result();

		// set info
		r.info = folds[0].info;

		// append all predictions and true values
		for(int f = 0; f < folds.length; f++) {
			r.predictions.addAll(folds[f].predictions);
			r.actuals.addAll(folds[f].actuals);
		}

		r.vals = folds[0].vals;
		// average all vals
		for(String metric : folds[0].vals.keySet()) {
			if (folds[0].vals.get(metric) instanceof Double) {
				double values[] = new double[folds.length];
				for(int i = 0; i < folds.length; i++) {
					values[i] = (Double)folds[i].vals.get(metric);
				}
				r.vals.put(metric,Utils.mean(values));
			}
		}

		return r;
	}

	/**
	 * AverageResults - Create a Result with the average of an array of Results by taking the average +/- standand deviation.
	 * @param	folds	array of Results (e.g., from CV-validation)
	 * @return	A result reporting the average of these folds.
	 */
	@Deprecated
	public static Result averageResults(Result folds[]) { 
		Result r = new Result();
		// info (should be the same across folds).
		r.info = folds[0].info;
		// for output ..
		for(String metric : folds[0].output.keySet()) {
			if (folds[0].output.get(metric) instanceof Double) {
				double values[] = new double[folds.length];
				for(int i = 0; i < folds.length; i++) {
					values[i] = (Double)folds[i].output.get(metric);
				}
				String avg_sd = Utils.doubleToString(Utils.mean(values),5,3)+" +/- "+Utils.doubleToString(Math.sqrt(Utils.variance(values)),5,3);
				r.output.put(metric,avg_sd);
			}
			else if (folds[0].output.get(metric) instanceof Integer) {
				// TODO combine with previous clause
				double values[] = new double[folds.length];
				for(int i = 0; i < folds.length; i++) {
					values[i] = (Integer)folds[i].output.get(metric);
				}
				String avg_sd = Utils.doubleToString(Utils.mean(values),5,3)+" +/- "+Utils.doubleToString(Math.sqrt(Utils.variance(values)),5,3);
				r.output.put(metric,avg_sd);
			}
			else if (folds[0].output.get(metric) instanceof double[]) {
				double avg[] = new double[((double[])folds[0].output.get(metric)).length];
				for(int i = 0; i < folds.length; i++) {
					for(int j = 0; j < avg.length; j++) {
						avg[j] = avg[j] + ((double[])folds[i].output.get(metric))[j] * 1./folds.length;
					}
				}
				r.output.put(metric,avg);
			}
			/*
			else if (folds[0].output.get(metric) instanceof int[]) {
				int avg[] = new int[((int[])folds[0].output.get(metric)).length];
				for(int i = 0; i < folds.length; i++) {
					for(int j = 0; j < avg.length; j++) {
						avg[j] = avg[j] + ((int[])folds[i].output.get(metric))[j];
					}
				}
				for(int j = 0; j < avg.length; j++) {
					avg[j] = avg[j] / avg.length;
				}
				r.output.put(metric,avg);
			}
			*/
		}
		// and now for 'vals' ..
		for(String metric : folds[0].vals.keySet()) {
			if (folds[0].vals.get(metric) instanceof Double) {
				double values[] = new double[folds.length];
				for(int i = 0; i < folds.length; i++) {
					values[i] = (Double)folds[i].vals.get(metric);
				}
				String avg_sd = Utils.doubleToString(Utils.mean(values),5,3)+" +/- "+Utils.doubleToString(Math.sqrt(Utils.variance(values)),5,3);
				r.vals.put(metric,avg_sd);
			}
		}

		if (r.getInfo("Type").equalsIgnoreCase("MLi")) {
			// Also display across time ...
			r.output.put("Window indices"	,A.make_sequence(folds.length));
			for(String metric : folds[0].output.keySet()) {
				if (folds[0].output.get(metric) instanceof Double) {
					double values[] = new double[folds.length];
					for(int i = 0; i < folds.length; i++) {
						values[i] = (Double)folds[i].output.get(metric);
					}
					r.output.put(""+metric+" per window",values);
				}
				else if (folds[0].output.get(metric) instanceof Integer) {
					int values[] = new int[folds.length];
					for(int i = 0; i < folds.length; i++) {
						values[i] = (Integer)folds[i].output.get(metric);
					}
					r.output.put(""+metric+" per window",values);
				}
			}
		}

		r.setInfo("Type","CV");
		return r;
	}

	/**
	 * Main - can use this function for writing tests during development.
	 * @param	args	command line arguments
	 */
	public static void main(String args[]) {
	}

}
