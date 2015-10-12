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
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;

/**
 * Metrics.java - Evaluation Metrics. 
 * <p>L_ are loss/error measures (less is better)</p>
 * <p>P_ are payoff/accuracy measures (higher is better).</p>
 * For more on the evaluation and threshold selection implemented here, see
 * <br> Jesse Read, <i>Scalable Multi-label Classification</i>. PhD Thesis, University of Waikato, Hamilton, New Zealand (2010).
 * @author 		Jesse Read (jesse@tsc.uc3m.es)
 * @version 	Feb 2013
 */
public abstract class Metrics {

	/** Exact Match, i.e., 1 - [0/1 Loss]. */
	public static double P_ExactMatch(int Y[][], int Ypred[][]) {
		return 1. - L_ZeroOne(Y,Ypred);
	}

	/** 0/1 Loss. */
	public static double L_ZeroOne(int y[], int ypred[]) {
		int L = y.length;
		for(int j = 0; j < L; j++) {
			if (y[j] != ypred[j])
				return 1.;
		}
		return 0.;
	}

	/** 0/1 Loss. */
	public static double L_ZeroOne(int Y[][], int Ypred[][]) {
		int N = Y.length;
		double loss = 0.0;
		for(int i = 0; i < Y.length; i++) {
			loss += L_ZeroOne(Y[i],Ypred[i]);
		}
		return loss/(double)N;
	}

	/** Hamming loss. */
	public static double L_Hamming(int y[], int ypred[]) {
		int L = y.length;
		double loss = 0.0;
		for(int j = 0; j < L; j++) {
			if (y[j] != ypred[j])
				loss++;
		}
		return loss/(double)L;
	}

	/** Hamming loss. */
	public static double L_Hamming(int Y[][], int Ypred[][]) {
		int N = Y.length;
		double loss = 0.0;
		for(int i = 0; i < Y.length; i++) {
			loss += L_Hamming(Y[i],Ypred[i]);
		}
		return loss/(double)N;
	}

	/** Hamming score aka label accuracy. */
	public static double P_Hamming(int Y[][], int Ypred[][]) {
		return 1. - L_Hamming(Y,Ypred);
	}

	/** Hamming score aka label accuracy. */
	public static double P_Hamming(int Y[][], int Ypred[][], int j) {
		int y_j[] = M.getCol(Y,j);
		int ypred_j[] = M.getCol(Ypred,j);
		return 1. - L_Hamming(y_j,ypred_j);
	}

	/** Harmonic Accuracy. Multi-label only. */
	public static double P_Harmonic(int y[], int ypred[]) {
		int L = y.length;
		double acc[] = new double[2];
		double N[] = new double[2];
		for(int j = 0; j < L; j++) {
			N[y[j]]++;
			if (y[j] == ypred[j])
				acc[y[j]]++;
		}
		for(int v = 0; v < 2; v++) {
			acc[v] = acc[v] / N[v];
		}
		return 2. / ((1. / acc[0]) + (1. / acc[1]));
	}

	/** Harmonic Accuracy -- for the j-th label. Multi-label only. */
	public static double P_Harmonic(int Y[][], int Ypred[][], int j) {
		int y_j[] = M.getCol(Y,j);
		int ypred_j[] = M.getCol(Ypred,j);
		return P_Harmonic(y_j,ypred_j);
	}

	/** Harmonic Accuracy -- average over all labels. Multi-label only. */
	public static double P_Harmonic(int Y[][], int Ypred[][]) {
		int N = Y.length;
		double loss = 0.0;
		for(int i = 0; i < Y.length; i++) {
			loss += P_Harmonic(Y[i],Ypred[i]);
		}
		return loss/(double)N;
	}

	/** Jaccard Index -- often simply called multi-label 'accuracy'. Multi-label only. */
	public static double P_Accuracy(int y[], int ypred[]) {
		int L = y.length;
		int set_union = 0;
		int set_inter = 0;
		for(int j = 0; j < L; j++) {
			if (y[j] == 1 || ypred[j] == 1)
				set_union++; 
			if (y[j] == 1 && ypred[j] == 1)
				set_inter++; 
		}
		// = intersection / union; (or, if both sets are empty, then = 1.)
		return (set_union > 0) ? (double)set_inter / (double)set_union : 1.0; 
	}

	/** Jaccard Index -- often simply called multi-label 'accuracy'. Multi-label only. */
	public static double P_Accuracy(int Y[][], int Ypred[][]) {
		int N = Y.length;
		double accuracy = 0.0;
		for(int i = 0; i < Y.length; i++) {
			accuracy += P_Accuracy(Y[i],Ypred[i]);
		}
		return accuracy/(double)N;
	}

	/** Jaccard Index -- often simply called multi-label 'accuracy'. Multi-label only. */
	public static double P_JaccardIndex(int Y[][], int Ypred[][]) {
		return P_Accuracy(Y,Ypred);
	}

	/** Jaccard Distance -- the loss version of Jaccard Index */
	public static double L_JaccardDist(int Y[][], int Ypred[][]) {
		return 1. - P_Accuracy(Y,Ypred);
	}

	/**
	 * L_LogLoss - the log loss between real-valued confidence rpred and true prediction y.
	 * @param	y		label
	 * @param	rpred	prediction (confidence)
	 * @param	C		limit (maximum loss of log(C))
	 * @return 	Log loss
	 */
	public static double L_LogLoss(double y, double rpred, double C) {
		// base 2 ?
		double ans = Math.min(Utils.eq(y,rpred) ? 0.0 : -( (y * Math.log(rpred)) + ((1.0 - y) * Math.log(1.0 - rpred)) ),C);
		return (Double.isNaN(ans) ? 0.0 : ans);
	}

	/**
	 * L_LogLoss - the log loss between real-valued confidences Rpred and true predictions Y with a maximum penalty based on the number of labels L [Important Note: Earlier versions of Meka only normalised by N, and not N*L as here].
	 */
	public static double L_LogLossL(int Y[][], double Rpred[][]) {
		int N = Y.length;
		int L = Y[0].length;
		return L_LogLoss(Y,Rpred,Math.log((double)L)) / ((double)N * (double)L);
	}

	/**
	 * L_LogLoss - the log loss between real-valued confidences Rpred and true predictions Y with a maximum penalty based on the number of examples D [Important Note: Earlier versions of Meka only normalised by N, and not N*L as here].
	 */
	public static double L_LogLossD(int Y[][], double Rpred[][]) {
		int N = Y.length;
		int L = Y[0].length;
		return L_LogLoss(Y,Rpred,Math.log((double)N)) / ((double)N * (double)L);
	}

	/**
	 * L_LogLoss - the log loss between real-valued confidences Rpred and true predictions Y with a maximum penalty C [Important Note: Earlier versions of Meka only normalised by N, and not N*L as here].
	 */
	public static double L_LogLoss(int Y[][], double Rpred[][], double C) {
		double loss = 0.0;
		for(int i = 0; i < Y.length; i++) {
			for(int j = 0; j < Y[i].length; j++) {
				loss += L_LogLoss(Y[i][j],Rpred[i][j],C);
			}
		}
		return loss;
	}

	/**
	 * P_TruePositives - 1 and supposed to be 1 (the intersection, i.e., logical AND).
	 */
	public static double P_TruePositives(int y[], int ypred[]) {
		//return Utils.sum(A.AND(y,ypred));
		int s = 0;
		for(int j = 0; j < y.length; j++) {
			if (ypred[j] == 1 && y[j] == 1)
					s++;
		}
		return s;
	}

	/**
	 * P_FalsePositives - 1 but supposed to be 0 (the length of y \ ypred).
	 */
	public static double P_FalsePositives(int y[], int ypred[]) {
		int s = 0;
		for(int j = 0; j < y.length; j++) {
			if (ypred[j] == 1 && y[j] == 0)
				s++;
		}
		return s;
	}

	/**
	 * P_TrueNegatives - 0 and supposed to be 0.
	 */
	public static double P_TrueNegatives(int y[], int ypred[]) {
		int s = 0;
		for(int j = 0; j < y.length; j++) {
			if (ypred[j] == 0 && y[j] == 0)
				s++;
		}
		return s;
	}

	/**
	 * P_FalseNegatives - 0 but supposed to be 1 (the length of ypred \ y).
	 */
	public static double P_FalseNegatives(int y[], int ypred[]) {
		int s = 0;
		for(int j = 0; j < y.length; j++) {
			if (ypred[j] == 0 && y[j] == 1)
				s++;
		}
		return s;
	}

	/**
	 * P_Precision - (retrieved AND relevant) / retrieved
	 */
	public static double P_Precision(int y[], int ypred[]) {
		double tp = P_TruePositives(y,ypred);
		double fp = P_FalsePositives(y,ypred);
		if (tp == 0.0 && fp == 0.0)
			return 0.0;
		return tp / (tp + fp);
	}

	/**
	 * P_Recall - (retrieved AND relevant) / relevant
	 */
	public static double P_Recall(int y[], int ypred[]) {
		double tp = P_TruePositives(y,ypred);
		double fn = P_FalseNegatives(y,ypred);
		if (tp == 0.0 && fn == 0.0)
			return 0.0;
		return tp / (tp + fn);
	}

	/**
	 * F1 - the F1 measure for two sets.
	 */
	public static double F1(int s1[], int s2[]) {
		double p = P_Precision(s1,s2);
		double r = P_Recall(s1,s2);
		if ( p == 0.0 && r == 0.0)
			return 0.0;
		return 2. * p * r / (p + r);
	}

	/*
	public static double P_Recall(int Y[][], int YPred[][]) {
		return P_Recall(flatten(Y),flatten(YPRed));
	}
	*/

	/**
	 * P_Precision - (retrieved AND relevant) / retrieved
	 */
	public static double P_PrecisionMacro(int Y[][], int Ypred[][]) {

		int L = Y[0].length;
		double m = 0.0;
		for (int j = 0; j < L; j++) {
			m += (P_Precision(M.getCol(Y,j),M.getCol(Ypred,j)) * 1./L);
		}

		return m;
	}

	/**
	 * P_Recall - (retrieved AND relevant) / relevant
	 */
	public static double P_RecallMacro(int Y[][], int Ypred[][]) {

		int L = Y[0].length;
		double m = 0.0;
		for (int j = 0; j < L; j++) {
			m += (P_Recall(M.getCol(Y,j),M.getCol(Ypred,j)) * 1./L);
		}

		return m;
	}

	/**
	 * P_Precision - (retrieved AND relevant) / retrieved
	 */
	public static double P_PrecisionMicro(int Y[][], int Ypred[][]) {
		return P_Precision(M.flatten(Y),M.flatten(Ypred));
	}

	/**
	 * P_Recall - (retrieved AND relevant) / relevant
	 */
	public static double P_RecallMicro(int Y[][], int Ypred[][]) {
		return P_Recall(M.flatten(Y),M.flatten(Ypred));
	}
	/**
	 * P_Precision - (retrieved AND relevant) / retrieved
	 */
	public static double P_Precision(int Y[][], int Ypred[][], int j) {
		return P_Precision(M.getCol(Y,j),M.getCol(Ypred,j));
		//int retrieved = M.sum(M.sum(Ypred));
		//int correct = M.sum(M.sum(M.multiply(Y,Ypred)));
		//return (double)correct / (double)predicted;
	}

	/**
	 * P_Recall - (retrieved AND relevant) / relevant
	 */
	public static double P_Recall(int Y[][], int Ypred[][], int j) {
		return P_Recall(M.getCol(Y,j),M.getCol(Ypred,j));
		//int relevant = M.sum(M.sum(Y));
		//int correct = M.sum(M.sum(M.multiply(Y,Ypred)));
		//return (double)correct / (double)relevant;
	}

	/**
	 * P_FmicroAvg - Micro Averaged F-measure (F1, as if all labels in the dataset formed a single vector)
	 */
	public static double P_FmicroAvg(int Y[][], int Ypred[][]) {
		return F1(M.flatten(Y),M.flatten(Ypred));
		//double precision = P_Precision(M.flatten(Y),M.flatten(Ypred));
		//double recall = P_Recall(M.flatten(Y),M.flatten(Ypred));
		//return (2.0 * precision * recall) / (precision + recall);
	}

	
	/**
	 * F-Measure Macro Averaged by L - The 'standard' macro average.
	 */
	public static double P_FmacroAvgL(int Y[][], int Ypred[][]) {

		int L = Y[0].length;

		double TP[] = new double[L];
		double FP[] = new double[L];
		double FN[] = new double[L];
		double F[] = new double[L];                                                                                                                        

		for (int j = 0; j < L; j++) {                                                                                                                        
			int y_j[] = M.getCol(Y,j);
			int ypred_j[] = M.getCol(Ypred,j);
			TP[j] = P_TruePositives(y_j,ypred_j);
			FP[j] = P_FalsePositives(y_j,ypred_j);
			FN[j] = P_FalseNegatives(y_j,ypred_j);
			if (TP[j] <= 0)
				F[j] = 0.0;                                                                                                                                  
			else {
				double prec = (double)TP[j] / ((double)TP[j]+(double)FP[j]);                                                          
				double recall = (double)TP[j] / ((double)TP[j]+(double)FN[j]);                                                          
				F[j] = 2 * ((prec*recall) / (prec+recall));                                                                                                                  
			}                                                                                                                                                  
		}

		return (double) A.sum(F) / (double) L;

	}

	/**
	 * F-Measure Macro Averaged by D - The F-measure macro averaged by example.
	 * The Jaccard index is also averaged this way.
	 */
	public static double P_FmacroAvgD(int Y[][], int Ypred[][]) {

		int N = Y.length;

		double F1_macro_D = 0.0;
		for(int i = 0; i < N; i++) {
			F1_macro_D += F1(Y[i],Ypred[i]);
		}

		return F1_macro_D / (double)N;
	}

	/**
	 * OneError - 
	 */
	public static double L_OneError(int Y[][], double Rpred[][]) {

		int N = Y.length;
		int one_error = 0;

		for(int i = 0; i < N; i++) {
			if(Y[i][Utils.maxIndex(Rpred[i])] <= 0)
				one_error++;
		}
		return (double)one_error/(double)N;
	}

	public static double P_AveragePrecision(int Y[][], double Rpred[][]) {
		double loss = 0.0;
		for(int i = 0; i < Y.length; i++) {
			loss += P_AveragePrecision(Y[i],Rpred[i]);
		}
		return loss/(double)Y.length;
	}

	public static double P_AveragePrecision(int y[], double rpred[]) {
		int r[] = Utils.sort(rpred);
		return P_AveragePrecision(y,r);
	}

	/**
	 * Average Precision - computes for each relevant label the percentage of relevant labels among all labels that are ranked before it.
	 * @param	y	0/1 labels         [0,   0,   1   ] (true labels)
	 * @param	r	ranking position   [1,   2,   0   ]
	 * @return	Average Precision
	 */
	public static double P_AveragePrecision(int y[], int r[]) {

        double avg_prec = 0;

        int L = y.length;

        List<Integer> ones = new ArrayList<Integer>();
        for (int j = 0; j < L; j++) {
            if (y[j] == 1) {
                ones.add(j);
            }
        }

        if (ones.size() <= 0) 
			return 1.0;

		for (int j : ones) {
			// 's' = the percentage of relevant labels ranked before 'j'
			double s = 0.0;
			for (int k : ones) {
				if (r[k] <= r[j]) {
					s++; 
				}
			}
			         // 's' divided by the position of 'j'
			avg_prec += (s / (1. + r[j]));
		}
		avg_prec /= ones.size();
		return avg_prec;
    }

	public static double L_RankLoss(int Y[][], double Rpred[][]) {
		double loss = 0.0;
		for(int i = 0; i < Y.length; i++) {
			loss += L_RankLoss(Y[i],Rpred[i]);
		}
		return loss/(double)Y.length;
	}

	public static double L_RankLoss(int y[], double rpred[]) {
		int r[] = Utils.sort(rpred);
		return L_RankLoss(y,r);
	}

	/**
	 * Rank Loss - the average fraction of labels which are not correctly ordered.
	 * Thanks to Noureddine Yacine NAIR BENREKIA for providing bug fix for this.
	 * @param	y	0/1 labels         [0,   0,   1   ]
	 * @param	r	ranking position   [1,   2,   0   ]
	 * @return	Ranking Loss
     */
   public static double L_RankLoss(int y[], int r[]) {
	   int L = y.length;
	   ArrayList<Integer> tI = new ArrayList<Integer>();
	   ArrayList<Integer> fI = new ArrayList<Integer>();
	   for (int j = 0; j < L; j++) {
		   if (y[j] == 1) {
			   tI.add(j);
		   } else {
			   fI.add(j);
		   }
	   }

	   if (!tI.isEmpty() && !fI.isEmpty()) {
		   int c = 0; 
		   for (int k : tI) {
			   for (int l : fI) {
				   if (position(k,r) < position(l,r)) {
					   c++;
				   }
			   }
		   }
		   return (double) c / (double)(tI.size() * fI.size());
	   } else {
		   return 0.0;
	   }
   }

   private static int position(int index, int r[]) {
	   int i = 0;
	   while (r[i]!=index)
		   i++;
	   return i;
   }

   /** Calculate AUPRC: Area Under the Precision-Recall curve. */
   public static double P_macroAUPRC(int Y[][], double P[][]) {

	   int L = Y[0].length;
	   double AUC[] = new double[L];
	   for(int j = 0; j < L; j++) {
		   ThresholdCurve curve = new ThresholdCurve();
		   Instances result = curve.getCurve(MLUtils.toWekaPredictions(M.getCol(Y,j),M.getCol(P,j)));
		   AUC[j] = ThresholdCurve.getPRCArea(result);
	   }
	   return Utils.mean(AUC);
   }

   /** Calculate AUROC: Area Under the ROC curve. */
   public static double P_macroAUROC(int Y[][], double P[][]) {

	   int L = Y[0].length;
	   double AUC[] = new double[L];
	   for(int j = 0; j < L; j++) {
		   ThresholdCurve curve = new ThresholdCurve();
		   Instances result = curve.getCurve(MLUtils.toWekaPredictions(M.getCol(Y,j),M.getCol(P,j)));
		   AUC[j] = ThresholdCurve.getROCArea(result);
	   }
	   return Utils.mean(AUC);
   }

	/** Get Data for Plotting PR and ROC curves. */
	public static Instances curveDataMicroAveraged(int Y[][], double P[][]) {
		int y[] = M.flatten(Y);
		double p[] = M.flatten(P);
		ThresholdCurve curve = new ThresholdCurve();
		return curve.getCurve(MLUtils.toWekaPredictions(y,p));
	}

	/** Get Data for Plotting PR and ROC curves. */
    public static Instances curveDataMacroAveraged(int Y[][], double P[][]) {

		// Note: 'Threshold' contains the probability threshold that gives rise to the previous performance values.

		Instances curveData[] = curveData(Y,P);
		int L = curveData.length;

		Instances avgCurve = new Instances(curveData[0],0);
		int D = avgCurve.numAttributes();

		for (double t = 0.0; t < 1.; t+=0.01) {
			Instance x = (Instance)curveData[0].instance(0).copy();
			//System.out.println("x1\n"+x);
			for(int j = 0; j < L; j++) {
				int i = ThresholdCurve.getThresholdInstance(curveData[j],t);
				if (j==0) {
					// reset
					for (int a = 0; a < D; a++) {
						x.setValue(a,curveData[j].instance(i).value(a) * 1./L);
					}
				}
				else {
					// add
					for (int a = 0; a < D; a++) {
						double v = x.value(a);
						x.setValue(a,v + curveData[j].instance(i).value(a) * 1./L);
					}
				}
			}
			//System.out.println("x2\n"+x);
			avgCurve.add(x);
		}

		/*
		System.out.println(avgCurve);
		System.exit(1);

		// Average everything
		for (int i = 0; i < avgCurve.numInstances(); i++) {
			for(int j = 0; j < L; j++) {
				for (int a = 0; a < D; a++) {
					double o = avgCurve.instance(i).value(a);
					avgCurve.instance(i).setValue(a, o / L);
				}
			}
		}
		*/
		return avgCurve;
	}

	/** Get Data for Plotting PR and ROC curves. */
	public static Instances curveData(int y[], double p[]) {

		ThresholdCurve curve = new ThresholdCurve();
		return curve.getCurve(MLUtils.toWekaPredictions(y,p));
	}

   /** Get Data for Plotting PR and ROC curves. */
   public static Instances[] curveData(int Y[][], double P[][]) {

	   int L = Y[0].length;
	   Instances curveData[] = new Instances[L];
	   for(int j = 0; j < L; j++) {
		   curveData[j] = curveData(M.getCol(Y, j), M.getCol(P, j));
	   }
	   return curveData;
   }

	/** Levenshtein Distance. Multi-target compatible */
	public static double L_LevenshteinDistance(int Y[][], int P[][]) {
		double loss = 0.;
		int N = Y.length;
		for(int i = 0; i < N; i++) {
			loss += L_LevenshteinDistance(Y[i],P[i]);
		}
		return loss / (double)N;
	}

	/** Levenshtein Distance divided by the number of labels. Multi-target compatible */
	public static double L_LevenshteinDistance(int y[], int p[]) {
		int L = y.length;
		return (getLevenshteinDistance(y, p) / (double)L);
	}

	/*
	 * Levenshtein Distance.
	 * Given true labels y, and pRedicted labels r.
	 * based on http://www.merriampark.com/ldjava.htm
	 */
	private static int getLevenshteinDistance(int y[], int r[]) {

		int n = y.length;
		int m = r.length;

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		if (n > m) {
			final int[] tmp = y;
			y = r;
			r = tmp;
			n = m;
			m = r.length;
		}

		int p[] = new int[n + 1];
		int d[] = new int[n + 1];
		int _d[];

		int i;
		int j;

		int r_j;

		int cost;

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			r_j = r[j - 1];
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = y[i - 1] == r_j ? 0 : 1;
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			_d = p;
			p = d;
			d = _d;
		}

		return p[n];
	}



	/** Log Likelihood */
	public double P_LogLikelihood(int y[], double p[]) {
		int L = y.length;

	   double l = 0.0;                              // likelihood
	   for(int j = 0; j < L; j++) {
		   // independence assumption
		   l += Math.log( Math.pow(p[j],y[j]) * Math.pow(1.-p[j],1-y[j]) ); // multi-label only
	   }
	   return l;
   }

   /** MSE */
   public double L_MSE(int y[], double p[]) {
	   int L = y.length;
	   double l[] = new double[L];					// likelihood
	   for(int j = 0; j < L; j++) {
		   // independence assumption
		   l[j] = Math.pow(p[j] - (double)y[j],2);	// MSE
	   }
	   return A.product(l);
   }

   /** MAE */
   public double L_MAE(int y[], double p[]) {
	   int L = y.length;
	   double l[] = new double[L];					// likelihood
	   for(int j = 0; j < L; j++) {
		   // independence assumption
		   l[j] = Math.abs(p[j] - (double)y[j]);	// MAE
	   }
	   return A.product(l);
   }

   /** Product 
   public double P_Product(int Y[][], double P[][]) {

		int N = Y.length;

		double s = 1.; 

		for(int i = 0; i < N; i++) {
			s *= L_MAE(Y[i],P[i]);
		}

		return s;
		}*/

   /** Log Sum 
   public double P_LogSum(int Y[][], double P[][]) {

		int N = Y.length;

		double s = 0.; 

		for(int i = 0; i < N; i++) {
			s += Math.log(A.product(L_MAE(Y[i],P[i])));
		}

		return s;
		}*/

   /** Avg Sum 
   public double P_Avg_Sum(int Y[][], double P[][]) {

		int N = Y.length;

		double s = 0.; 

		for(int i = 0; i < N; i++) {
			s += L_MAE(Y[i],P[i]);
		}

		return s / N;
		}*/

   /**
	* Do some tests.
	*/
   public static void main(String args[]) {
	   int Y[][] = new int[][] {
		   // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 
		   {0,1,1,0,0,0,0,0,0,0,0,0,0,0,0},
		   {0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
		   {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0},
		   {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0},
	//	   {1,2},
	//	   {3,4,5},
	//	   {6},
	//	   {7}
	   };
	   double P[][] = new double[][] {
		   // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 
		   {0,0.7,0.8,0.9,0,0,0,0,0,0.7,0,0,0,0,0},
			   {0,0,0,0.6,0.7,0,0,0,0,0,0,0,0,0,0},
			   {0,0,0,0,0,0,0.8,0,0,0,0,0,0.8,0,0},
			   {0,0.7,0,0,0,0,0,0,0,0,0,0,0,0,0},
	   };
	   int Ypred[][] = new int[][] {
		 // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 
		   {0,1,1,1,0,0,0,0,0,1,0,0,0,0,0},
		   {0,0,0,1,1,0,0,0,0,0,0,0,0,0,0},
		   {0,0,0,0,0,0,1,0,0,0,0,0,1,0,0},
		   {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
		   //{1,2,3,9},
		   //{3,4},
		   //{6,12},
		   //{1}
	   };
	   System.out.println("0.533333333... = "+P_FmacroAvgD(Y,Ypred));
	   System.out.println("LD = "+L_LevenshteinDistance(Y,Ypred));
	   System.out.println("MA = \n"+curveDataMacroAveraged(Y,P));
	   //System.out.println("\nMi = \n"+curveDataMicroAveraged(Y,P));
   }
}
