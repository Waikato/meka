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

package weka.core;

import weka.core.*;
import java.util.*;

public abstract class MLEvalUtils {

	public static String getThreshold(ArrayList<double[]> Y, Instances D, String top) throws Exception {
		if (top.equals("PCut1")) {			// Proportional Cut threshold (1 general threshold)
			return String.valueOf(MLEvalUtils.calibrateThreshold(Y,MLUtils.labelCardinality(D)));
		}	
		else if (top.equals("PCutL")) {		// Proportional Cut thresholds (one for each Label)
			return Arrays.toString(MLEvalUtils.calibrateThresholds(Y,MLUtils.labelCardinalities(D)));
		}
		else { 								// Set our own threshold (we assume top = "0.5" or top = "[0.1,...,0.3]"
											// (we make no checks here!)
			//return String.valueOf(Double.parseDouble(top));
			return top;
		}
	}

	/**
	 * Calibrate a threshold.
	 * Based upon a supplied label cardinality from the training set.
	 */
	public static double calibrateThreshold(ArrayList<double[]> Y, double LC_train) { 
		int N = Y.size();
		ArrayList<Double> big = new ArrayList<Double>();
		for(double y[] : Y) {
			for (double y_ : y) {
				big.add(y_);
			}
		}
		Collections.sort(big);

		//System.out.println("we will take = "+(int)Math.round(LC_train * (double)N));
		int i = big.size() - (int)Math.round(LC_train * (double)N);

		//System.out.println("from "+i);
		if (N == big.size()) { // special cases
			if (i+1 == N) // only one!
				return (big.get(N-2)+big.get(N-1)/2.0);
			if (i+1 >= N) // zero!
				return 1.0;
			else
				return Math.max(((double)(big.get(i)+big.get(i+1))/2.0), 0.00001);
		}
		/*
		//System.out.println(""+big);
		//System.out.println("N = "+N);
		//System.out.println("big.size = "+big.size());
		*/
		//System.out.println("LCard ("+LC_train+") between "+i+" and "+(Math.max(i+1,N-1)));
		//if (i == big.size) // we have to set the threshold very low
		//	return 0.00001
		return Math.max(((double)(big.get(i)+big.get(Math.max(i+1,N-1))))/2.0 , 0.00001);
	}

	/**
	 * Calibrate the threshold[] vector.
	 * Based upon a supplied label cardinality from the training set.
	 * Assumes that each double[] is of length L;
	 */
	public static double[] calibrateThresholds(ArrayList<double[]> Y, double LC_train[]) { 

		int L = LC_train.length;
		double t[] = new double[L];

		ArrayList<double[]> Y_[] = new ArrayList[Y.size()];
		for(int j = 0; j < L; j++) {
			Y_[j] = new ArrayList<double[]>();
		}

		for(double y[] : Y) {
			for(int j = 0; j < L; j++) {
				Y_[j].add(new double[]{y[j]});
			}
		}

		for(int j = 0; j < L; j++) {
			t[j] = calibrateThreshold(Y_[j],LC_train[j]);
		}

		return t;
	}

	public static HashMap<String,Double> getMLStats(ArrayList<double[]> Rankings, ArrayList<int[]> Actuals, String t) {
		double ts[] = null;
		if (t.startsWith("[")) {
			ts = MLUtils.toDoubleArray(t);							// threshold vector       [t1 t2 ... tL]]
		}
		else {
			ts = new double[Rankings.iterator().next().length];
			Arrays.fill(ts,Double.parseDouble(t));					// make a threshold vector [t t t ... t]
		}
		return getMLStats(Rankings,Actuals,ts);
	}

	/**
	 * Calculate Performance Measures. 
	 * A threshold is supplied. Rankings.size() must equal Actuals.size(), and they must correspond.
	 */
	public static HashMap<String,Double> getMLStats(ArrayList<double[]> Rankings, ArrayList<int[]> Actuals, double t[]) {

		double N = Rankings.size();
		int L = Rankings.iterator().next().length;
		int fp = 0, tp = 0, tn = 0, fn = 0;
		int p_sum_total = 0, r_sum_total = 0;
		double log_loss_D = 0.0, log_loss_L = 0.0;
		int set_empty_total = 0, set_inter_total = 0; 
		int exact_match = 0, one_error = 0, coverage = 0; 
		double accuracy = 0.0, f1_macro_D = 0.0, f1_macro_L = 0.0;
		int hloss_total = 0;
		int[] o_tp = new int[L], o_fp = new int[L], o_fn = new int[L], o_tn = new int[L];
		//double average_accuracy_online = 0.0;


		for(int i = 0; i < N; i++) {
			double ranking[] = Rankings.get(i);
			int actual[] = Actuals.get(i);

			int pred[] = new int[actual.length];
			for(int j = 0; j < L; j++) {
				pred[j] = (ranking[j] >= t[j]) ? 1 : 0;
			}

			//System.out.println("act"+Arrays.toString(actual));
			//System.out.println("prd"+Arrays.toString(pred));

			// calculate
			int p_sum = 0, r_sum = 0;
			int set_union = 0;
			int set_inter = 0;
			int doc_inter = 0;
			int doc_union = 0;
			for(int j = 0; j < L; j++) {
				int p = pred[j];
				int R = actual[j];
				if (p==1) {
					p_sum++;
					// predt 1, real 1
					if(R==1) {
						r_sum++;
						tp++;
						o_tp[j]++;			// f1 macro (L)
						set_inter++;
						set_union++;
					}
					// predt 1, real 0
					else {
						fp++;
						o_fp[j]++;			// f1 macro (L)
						hloss_total++;
						set_union++;
					}
				}
				else {                      
					// predt 0, real 1
					if(R==1) { 
						r_sum++;
						fn++;
						o_fn[j]++;			// f1 macro (L)
						hloss_total++;
						set_union++;
					}
					// predt 0, real 0
					else {   
						tn++;
						o_tn[j]++;			// f1 macro (L)
					}
				}

				// log losses: 
				log_loss_D += calcLogLoss((double)R,ranking[j],Math.log(N));
				log_loss_L += calcLogLoss((double)R,ranking[j],Math.log(L));
			}

			set_inter_total += set_inter;

			p_sum_total += p_sum; 
			r_sum_total += r_sum;

			if(set_union > 0)	//avoid NaN
				accuracy += ((double)set_inter / (double)set_union);
			//System.out.println(""+set_inter+","+set_union);

			if (p_sum <= 0)	//empty set
				set_empty_total ++;

			// exact match (eval. by example)
			if(set_inter == set_union)
				exact_match++;

			// f1 macro average by example
			if (p_sum > 0 && r_sum > 0 && set_inter > 0) {
				double prec = (double)set_inter / (double)p_sum;
				double rec = (double)set_inter / (double)r_sum;
				if (prec > 0 || rec > 0) {
					f1_macro_D += ((2.0 * prec * rec) / (prec + rec));
				}
			}

			//one error: how many times the top ranked label is NOT in the label set
			if(actual[Utils.maxIndex(ranking)] <= 0)
				one_error++;

		}

		double fms[] = new double[L];                                                                                                                        
		for (int j = 0; j < L; j++) {                                                                                                                        
			// micro average
			if(o_tp[j] <= 0)                                                                                             
				fms[j] = 0.0;                                                                                                                                  
			else {                                                                                                                                             
				double prec = (double)o_tp[j] / ((double)o_tp[j]+(double)o_fp[j]);                                                          
				double recall = (double)o_tp[j] / ((double)o_tp[j]+(double)o_fn[j]);                                                          
				fms[j] = 2 * ((prec*recall) / (prec+recall));                                                                                                                  
			}                                                                                                                                                  
		}               

		double precision = (double)set_inter_total / (double)p_sum_total;
		double recall = (double)set_inter_total / (double)r_sum_total;

		// @ temp
		double a[] = new double[L];
		for(int j = 0; j < L; j++) {
			a[j] = (o_tp[j] + o_tn[j]) / (double)N;
		}
		System.out.println("Individual accuracies: "+Arrays.toString(a));
		// @ temp
		
		HashMap<String,Double> results = new LinkedHashMap<String,Double>();
		results.put("N"					,N);
		results.put("L"					,(double)L);
		results.put("Accuracy"			,(accuracy/N));
		results.put("H_loss"			,((double)hloss_total/((double)N*(double)L)));
		results.put("H_acc"				,1.0-((double)hloss_total/((double)N*(double)L)));
		results.put("Exact_match"		,((double)exact_match/N));
		results.put("ZeroOne_loss"		,1.0-((double)exact_match/N));
		//results.put("LCard_diff"		,Math.abs((((double)p_sum_total/N)-(double)r_sum_total/N)));
		//results.put("Coverage"			,((double)coverage/N));
		results.put("One_error"			,((double)one_error/N));
		results.put("LogLossD"			,(log_loss_D/N));
		results.put("LogLossL"			,(log_loss_L/N));
		//results.put("EmptyAccuracy"		,(accuracy/(N-set_empty_total)));
		//results.put("EmptyMacroF1"		,f1_macro_D/(N-(double)set_empty_total));
		//results.put("Build_time"		,s.vals.get("Build_time"));
		//results.put("Test_time"		,s.vals.get("Test_time"));
		//results.put("Total_time"		,s.vals.get("Build_time") + s.vals.get("Test_time"));
		//results.put("TPR"				,(double)tp / (double)(tp + fn));
		//results.put("FPR"				,(double)fp / (double)(fp + tn));
		results.put("Precision"			,precision);
		results.put("Recall"			,recall);
		results.put("F1_micro"			,(2.0 * precision * recall) / (precision + recall));
		results.put("F1_macro_D"		,(f1_macro_D/N));
		results.put("F1_macro_L"		,(Utils.sum(fms)/(double)L));
		results.put("EmptyVectors"		,(double)set_empty_total/(double)N);
		results.put("LCard_pred"		,(double)p_sum_total/N);
		results.put("LCard_real"		,(double)r_sum_total/N);
		//results.put("Threshold"        ,t[0]);	// only stores the first threshold
		//results.put("AUPRC"        		,0.0); // "@see (`Hierarchical Multi-label Classification' by Vens et al, 2008);

		return results;

	}

	public static double calcLogLoss(double R, double P, double C) {
		// base 2 ?
		double ans = Math.min(Utils.eq(R,P) ? 0.0 : -( (R * Math.log(P)) + ((1.0 - R) * Math.log(1.0 - P)) ),C);
		return (Double.isNaN(ans) ? 0.0 : ans);
	}

	public static HashMap<String,Double> getMTStats(ArrayList<double[]> Rankings, ArrayList<int[]> Actuals) {

		double N = Rankings.size();
		int L = Actuals.iterator().next().length;
		double h_loss = 0.0, e_loss = 0.0;
		double h_acc[] = new double[L]; // new
		for (int i = 0; i < N; i++) {

			double y_pred[] = Rankings.get(i);
			int y_real[] = Actuals.get(i);

			double loss = 0.0;
			for(int j = 0; j < L; j++) {
				double l = (y_real[j] != (int)Math.round(y_pred[j])) ? 1.0 : 0.0;
				loss += l;
				h_acc[j] += l;
			}
			h_loss += loss;
			e_loss += (loss > 0.0) ? 1.0 : 0.0;


			//System.out.print(" eval : "+MLUtils.toBitString(y_real));
			//System.out.print(" vs " +MLUtils.toBitString(y_pred));
			//System.out.println(" : "+loss);
		}

		HashMap<String,Double> output = new LinkedHashMap<String,Double>();
		output.put("N"					,N);
		output.put("L"					,(double)L);
		output.put("L_loss"			,((double)h_loss/((double)N*(double)L)));
		output.put("L_acc"				,1.0-((double)h_loss/((double)N*(double)L)));
		output.put("E_loss"			,(e_loss/N));
		output.put("E_acc"				,1.0-(e_loss/N));
		for(int j = 0; j < L; j++) {
			output.put("L"+j+"_acc"				,1.0-(h_acc[j]/N));
		}
		return output;
	}

	public static void main(String args[]) {
		//for(int i = 0; i < ; i++) {
		//}
		//System.out.println(""+Arrays.toString(invert(new int[]{5,2},6)));
	}

}
