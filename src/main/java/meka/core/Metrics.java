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
	 * P_TruePositives - 1 and supposed to be 1.
	 */
	public static double P_TruePositives(int y[], int ypred[]) {
		//return Utils.sum(A.AND(y,ypred));
		int s = 0;
		for(int j = 0; j < y.length; j++) {
			if (ypred[j] == 1)
				if (y[j] == 1)
					s++;
		}
		return s;
	}

	/**
	 * P_FalsePositives - 1 but supposed to be 0.
	 */
	public static double P_FalsePositives(int y[], int ypred[]) {
		int s = 0;
		for(int j = 0; j < y.length; j++) {
			if (ypred[j] == 1)
				if (y[j] == 0)
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
			if (ypred[j] == 0)
				if (y[j] == 0)
					s++;
		}
		return s;
	}

	/**
	 * P_FalseNegatives - 0 but supposed to be 1.
	 */
	public static double P_FalseNegatives(int y[], int ypred[]) {
		int s = 0;
		for(int j = 0; j < y.length; j++) {
			if (ypred[j] == 0)
				if (y[j] == 1)
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
		return tp / (tp + fp);
	}

	/**
	 * P_Recall - (retrieved AND relevant) / relevant
	 */
	public static double P_Recall(int y[], int ypred[]) {
		double tp = P_TruePositives(y,ypred);
		double fn = P_FalseNegatives(y,ypred);
		return tp / (tp + fn);
	}

	/*
	public static double P_Recall(int Y[][], int YPred[][]) {
		return P_Recall(flatten(Y),flatten(YPRed));
	}
	*/

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

	public static double P_FmicroAvg(int Y[][], int Ypred[][]) {
		double precision = P_Precision(M.flatten(Y),M.flatten(Ypred));
		double recall = P_Recall(M.flatten(Y),M.flatten(Ypred));
		return (2.0 * precision * recall) / (precision + recall);
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
	 * The Jaccard index is also averaed this way.
	 */
	public static double P_FmacroAvgD(int Y[][], int Ypred[][]) {

		int N = Y.length;
		double F1_macro_D = 0;

		for(int i = 0; i < N; i++) {
			double prec = P_Precision(Y[i],Ypred[i]);
			double rec = P_Recall(Y[i],Ypred[i]);
			if (prec > 0 || rec > 0) {
				F1_macro_D += ((2.0 * prec * rec) / (prec + rec));
			}
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

   // @TODO check
   public double P_Product(int Y[][], double P[][]) {

		int N = Y.length;

		double s = 1.; 

		for(int i = 0; i < N; i++) {
			int y[] = Y[i];
			double p[] = P[i];     
			s *= A.product(p);
		}

		return s;
	}

   // @TODO check
   public double P_LogSum(int Y[][], double P[][]) {

		int N = Y.length;

		double s = 0.; 

		for(int i = 0; i < N; i++) {
			int y[] = Y[i];
			double p[] = P[i];     
			s += Math.log(A.product(p));
		}

		return s;
	}

   // @TODO check
   public double P_Avg_Sum(int Y[][], double P[][]) {

		int N = Y.length;

		double s = 0.; 

		for(int i = 0; i < N; i++) {
			int y[] = Y[i];
			double p[] = P[i];     
			s += A.sum(p);
		}

		return s / N;
   }
}
