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
import weka.classifiers.Classifier; // for 'LEAD' method
import weka.classifiers.functions.SMO; // for 'LEAD' method
import meka.classifiers.multilabel.*; // for 'LEAD' method

import java.util.HashMap;
import java.util.Arrays;
import java.util.Random;

/**
 * StatUtils - Helpful statistical functions.
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version	March 2013 - Multi-target Compatible
 */
public abstract class StatUtils {

	//
	// EMPIRICAL DISTRIBUTIONS
	//

	/**
	 * P - Empirical prior.
	 * @param	Y[][]	label matrix
	 * @param	x	 	label values
	 * @return 	[P(Y_1==x[1]), P(Y_2==x[2]), ..., P(Y_L==x[L])]
	 */
	public static double[] P(double Y[][], int x[]) {
		int L = x.length;
		return P(Y,MLUtils.gen_indices(L),x);
	}

	/**
	 * P - Empirical prior.
	 * @param	Y[][]	label matrix
	 * @param	x	 	label values
	 * @param	j		label indices
	 * @return 	[P(Y_j[1]==x[1]), P(Y_j[2]==x[2]), ..., P(Y_j[L]==x[L])]
	 */
	public static double[] P(double Y[][], int j[], int x[]) {
		int L = j.length;
		double p[] = new double[L];
		for(int j_ = 0; j_ < L; j_++) {
			p[j_] = p(Y,j[j_],x[j_]);
		}
		return p;
	}

	/**
	 * p - Empirical prior.
	 * In the multi-label case, k in {0,1}
	 * @param	Y[][]	label matrix
	 * @param	j		label index
	 * @param	x	 	label value
	 * @return 	P(Y_j==k) in Y.
	 */
	public static double p(double Y[][], int j, int k) {
		int N = Y.length;
		double p = 0.0001;
		for(int i = 0; i < N; i++) {
			if ((int)Math.round(Y[i][j]) == k) {
				p += 1.0;
			}
		}
		return p/N;
	}

	/**
	 * p - Empirical prior.
	 * In the multi-label case, k in {0,1}
	 * @param	D    	Instances
	 * @param	j		label index
	 * @param	j_ 		label value
	 * @return 	P(Y_j==j_) in D.
	 */
	public static double p(Instances D, int j, int j_) {
		return p(MLUtils.getYfromD(D),j,j_);
	}

	/**
	 * P - Empirical joint.
	 * Multi-target friendly.
	 * @param	Y   label matrix
	 * @param	j	1st label index
	 * @param	v 	1st label value
	 * @param	k	2nd label index
	 * @param	w 	2nd label value
	 * @return 	P(Y_j = v, Y_k = w) in Y.
	 */
	public static double P(double Y[][], int j, int v, int k, int w) {
		int N = Y.length;
		double p = 0.0001;
		for(int i = 0; i < N; i++) {
			if (((int)Math.round(Y[i][j]) == v) && ((int)Math.round(Y[i][k]) == w))
				p += 1.0;
		}
		return p/N;
	}

	/**
	 * p - Empirical joint.
	 * Multi-target friendly.
	 * @param	D       Instances
	 * @param	j	1st label index
	 * @param	v 	1st label value
	 * @param	k	2nd label index
	 * @param	w 	2nd label value
	 * @return 	P(Y_j = v, Y_k = w) in D.
	 */
	public static double P(Instances D, int j, int v, int k, int w) {
		return P(MLUtils.getYfromD(D),j,v,k,w);
	}

	/**
	 * Delta(x_1,x_2,x_3 = v_1,v_2,v_3) for j = 1,2,3, k = 1,2,3.
	 */
	private static boolean match(Instance x, int indices[], int values[]) {
		for(int j = 0; j < indices.length; j++) {
			int v = (int)Math.round(x.value(indices[j]));
			if (v != values[j]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * P - Empirical joint.
	 * Multi-target friendly.
	 * @param	D       Instances
	 * @param	j[]		label indices, e.g., 1,2,3
	 * @param	v[]		label values, e.g., 0,0,1
	 * @return 	P(x_1,x_2,x_3 = v_1,v_2,v_3) for j = 1,2,3 in D
	 */
	public static double P(Instances D, int j[], int v[]) {
		int N = D.numInstances();
		int n = 0;
		for (Instance x : D) {
			if (match(x,j,v))
				n++;
		}
		return Math.max(0.0001,(double)n/N);
	}

	/**
	 * jPMF - Joint PMF.
	 * @return the joint PMF of the j-th and k-th labels in D.
	 */
	public static double[][] jPMF(Instances D, int j, int k) {
		double JOINT[][] = new double[D.attribute(j).numValues()][D.attribute(k).numValues()];
		int N = D.numInstances();
		for(int i = 0; i < N; i++) {
			int v_j = (int)Math.round(D.instance(i).value(j));
			int v_k = (int)Math.round(D.instance(i).value(k));
			JOINT[v_j][v_k] += (1.0 / (double)N);
		}
		return JOINT;
	}

	/**
	 * Joint Distribution.
	 * @return the joint PMF of the j-th and k-th and lthlabels in D.
	 */
	public static double[][][] jPMF(Instances D, int j, int k, int l) {
		double JOINT[][][] = new double[D.attribute(j).numValues()][D.attribute(k).numValues()][D.attribute(l).numValues()];
		int N = D.numInstances();
		for(int i = 0; i < N; i++) {
			int v_j = (int)Math.round(D.instance(i).value(j));
			int v_k = (int)Math.round(D.instance(i).value(k));
			int v_l = (int)Math.round(D.instance(i).value(l));
			JOINT[v_j][v_k][v_l] += (1.0 / (double)N);
		}
		return JOINT;
	}

	/**
	 * GetP - Get a pairwise empirical joint-probability matrix P[][] from dataset D.
	 * @note multi-label only
	 */
	public static double[][] getP(Instances D) {
		double N = (double)D.numInstances();
		int L = D.classIndex();
		double P[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			P[j][j] = p(D,j,1);
			for(int k = j+1; k < L; k++) {
				P[j][k] = P(D,j,1,k,1);
			}
		}
		return P;
	}

	/**
	 * GetApproxP - A fast version of getC(D), based on frequent sets.
	 * Actually, if we don't prune, this is not even approximate -- it is the real empirical P.
	 */
	public static int[][] getApproxC(Instances D) {
		int N = D.numInstances();
		int L = D.classIndex();
		int C[][] = new int[L][L];
		// @todo, can prune here to make even faster by pruning this.
		HashMap<LabelSet,Integer> map = MLUtils.countCombinationsSparse(D,L);

		for (LabelSet y : map.keySet()) {
			int c = map.get(y);
			for(int j = 0; j < y.indices.length; j++) {
				int j_ = y.indices[j];
				C[j_][j_] += c;
				for(int k = j+1; k < y.indices.length; k++) {
					int k_ = y.indices[k];
					C[j_][k_] += c;
				}
			}
		}

		return C;
	}

	/**
	 * GetApproxP - A fast version of getP(D), based on frequent sets.
	 * Actually, if we don't prune, this is not even approximate -- it is the real empirical P.
	 */
	public static double[][] getApproxP(Instances D) {
		int N = D.numInstances();
		int L = D.classIndex();
		double P[][] = new double[L][L];
		// @todo, can prune here to make even faster by pruning this.
		HashMap<LabelSet,Integer> map = MLUtils.countCombinationsSparse(D,L);

		for (LabelSet y : map.keySet()) {
			for(int j = 0; j < y.indices.length; j++) {
				int y_j = y.contains(j) ? 1 : 0;
				if (y_j > 0) {
					P[j][j] += (double)y_j;                           // C[j==1] ++
					for(int k = j+1; k < y.indices.length; k++) {
						int y_k = y.contains(j) ? 1 : 0;
						P[j][k] += (double)y_k;                       // C[j==1,k==1] ++
					}
				}
			}
		}

		// @todo use getP(C,N) instead
		for(int j = 0; j < L; j++) {
			P[j][j] = Math.max(P[j][j]/(double)N,0.0001);
			for(int k = j+1; k < L; k++) {
				P[j][k] = Math.max(P[j][k]/(double)N,0.0001);
			}
		}

		return P;
	}

	public static double[][] getP(int C[][], int N) {
		int L = C.length;
		double P[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			P[j][j] = Math.max(C[j][j]/(double)N,0.0001);
			for(int k = j+1; k < L; k++) {
				P[j][k] = Math.max(C[j][k]/(double)N,0.0001);
			}
		}
		return P;
	}

	/**
	 * GetC - Get pairwise co-ocurrence counts from the training data D.
	 * @note multi-label only
	 * @return 	C[][] where C[j][k] is the number of times where Y[i][j] = 1 and y[i][k] = 1 over all i = 1,...,N
	 */
	public static int[][] getC(Instances D) {

		int L = D.classIndex();
		int N = D.numInstances();

		int C[][] = new int[L][L];

		for(int i = 0; i < N; i++) {
			for(int j = 0; j < L; j++) {
				C[j][j] += (int)D.instance(i).value(j);                                            // C[j==1] ++
				for(int k = j+1; k < L; k++) {
					C[j][k] += (D.instance(i).value(j) + D.instance(i).value(k) >= 2.0) ? 1 : 0;   // C[j==1,k==1] ++
				}
			}
		}
		return C;
	}

	/**
	 * I - Mutual Information I(y_j;y_k).
	 * multi-label only -- count version
	 * @param	C[][]	count matrix
	 * @param	j		j-th label index
	 * @param	k		k-th label index
	 * @param	Ncount	number of instances in the training set
	 * @return H(Y_j|Y_k)
	 */
	public static double I(int C[][], int j, int k, int Ncount) {

		double N = (double)Ncount;
		double N_j = Math.max(C[j][j],0.0001);
		double N_k = Math.max(C[k][k],0.0001);

		double p_5 = (N - N_j);
		double p_6 = (N - N_k);
		double p_7 = (N - (N_j + N_k));

		return 1.0 / N * (
			- p_5 * Math.log( p_5 )
			- p_6 * Math.log( p_6 )
			+ p_7 * Math.log( p_7 )
			+ N * Math.log( N )
			);

	}

	/**
	 * H - Conditional Entropy H(y_j|y_k).
	 * multi-label only
	 * @param	C[][]	count matrix
	 * @param	j		j-th label index
	 * @param	k		k-th label index
	 * @param	Ncount	number of instances in the training set
	 * @return H(Y_j|Y_k)
	 */
	public static double H(int C[][], int j, int k, int Ncount) {

		double N = (double)Ncount;
		double N_j = Math.max(C[j][j],0.0001);
		double N_k = Math.max(C[k][k],0.0001);
		double N_jk = Math.max(C[j][k],0.0001);

		double p_1 = (N + N_jk - (N_j + N_k));
		double p_2 = (N_k - N_jk);
		double p_3 = (N_j - N_jk);
		double p_5 = (N - N_j);

		return -1.0 / N * (
		    p_1 * Math.log( p_1 ) 
			+ p_2 * Math.log( p_2 )
			+ p_3 * Math.log( p_3 )
			+ N_jk * Math.log( N_jk )
			- p_5 * Math.log( p_5 ) 
			- N_j * Math.log( N_j )
			);
	}

	/**
	 * I - Mutual Information -- fast version, must calcualte P[][] = getP(D) first.
	 * multi-label only
	 * @todo -- check this 
	 * @return I(Y_j;Y_k)
	public static double I(double P[][], int j, int k) {
		double p_j = P[j][j];
		double p_k = P[j][k];
		double p_jk = P[j][k];
		return p_jk * Math.log ( p_jk / ( p_j * p_k) );
	}
	*/

	/**
	 * I - Mutual Information -- fast version, must calcualte P[][] = getP(D) first.
	 * @see #I(P,j,k)
	 * @return I[][]
	 */
	public static double[][] I(double P[][]) {
		int L = P.length;
		double M[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				M[j][k] = I(P,j,k);
			}
		}
		return M;
	}

	/**
	 * I - Mutual Information.
	 * @note binary only
	 * @return I(Y_j;Y_k) in dataset D.
	 */
	public static double I(double P[][], int j, int k) {
		double I = 0.0;
		double p_x = P[j][j];
		double p_y = P[k][k];
		double p_xy = P[j][k];
		I += p_xy * Math.log ( p_xy / ( p_x * p_y) );
		I += (1.-p_xy) * Math.log ( (1.-p_xy) / ( (1.-p_x) * (1.-p_y)) );
		return I;
	}

	/**
	 * I - Mutual Information.
	 * @note Multi-target friendly (does not assume binary labels).
	 * @note a bit slow
	 * @return I(Y_j;Y_k) in dataset D.
	 */
	public static double I(Instances D, int j, int k) {
		double I = 0.0;
		for(int x = 0; x < D.attribute(j).numValues(); x++) {
			double p_x = p(D,j,x);
			for(int y = 0; y < D.attribute(k).numValues(); y++) {
				double p_y = p(D,k,y);
				double p_xy = P(D,j,x,k,y);
				I += p_xy * Math.log ( p_xy / ( p_x * p_y) );
			}
		}
		return I;
	}

	/**
	 * I - Get an Unconditional Depndency Matrix.
	 * (Works for both ML and MT data).
	 * @param	D	dataset
	 * @param	L	number of labels
	 * @return a L*L matrix representing Unconditional Depndence.
	 */
	public static double[][] I(Instances D, int L) {
		double M[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				// get I(Y_j;X_k)
				M[j][k] = I(D,j,k);
			}
		}
		return M;
	}

	/** Critical value used for Chi^2 test. */
	public static final double CRITICAL[] = new double[]{0.,2.706, 4.605, 6.251, 7.779};      // P == 0.10

	/**
	 * Chi^2 - Do the chi-squared test on the j-th and k-th labels in Y.
	 * @NOTE multi-label only! @TODO Use enumerateValues() !!!
	 * If they are correlated, this means unconditional dependence!
	 * @return	The chi-square statistic for labels j and k in Y.
	 */
	public static double chi2 (Instances Y,int j,int k) {
		// H_0 : p(Y_j,Y_k) = p(Y_j)p(Y_k)

		double chi2 = 0.0;
		for(int j_ = 0; j_ < 2; j_++) {
			for(int k_ = 0; k_ < 2; k_++) {
				double E = p(Y,j,j_) * p(Y,k,k_); 			// Expected vaule P(Y_j = j_)P(Y_k = k_)
				double O = P(Y,j,j_,k,k_);					// Observed value P(Y_j = j_, Y_k = k_)
				chi2 += ( ((O - E) * (O - E)) / E );
			}
		}
		return chi2;
	}

	/**
	 * Chi^2 - Do the chi-squared test on all pairs of labels.
	 * @see #chi2(D,j,k)
	 * @param	D	dataset
	 * @return	The chi-square statistic matrix X
	 */
	public static double[][] chi2 (Instances D) {
		int L = D.classIndex();
		double X[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				X[j][k] = chi2(D,j,k);
			}
		}
		return X;
	}

	/**
	 * Chi^2 - Chi-squared test.
	 * If they are correlated, this means unconditional dependence!
	 * @param	M[][][]			measured joint  P(Y_1,Y_2)      
	 * @param	Exp[][][]		expect joint 	P(Y_1)P(Y_2)	given null hypothesis
	 * @return	The chi-square statistic for labels j and k in Y; normalized by critical value.
	 */
	public static double[][] chi2 (double M[][][], double Exp[][][]) {

		int K = M.length;
		int L = M[0].length;
		int DoF = K - 1;

		double V[][] = new double[L][L];

		for(int i = 0; i < K; i++) {
			for(int j = 0; j < L; j++) {
				for(int k = j+1; k < L; k++) {
					double J = M[i][j][k];		// actual (joint) 			p(e==e)
					double E = Exp[i][j][k];	// expected (prior*prior)	
					V[j][k] += ( ((J - E) * (J - E)) / E );
				}
			}
		}
		//System.out.println(weka.core.M.toString((double[][])V));
		// offset
		double p = CRITICAL[DoF];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				V[j][k] -= p;
			}
		}
		return V;
	}

	/**
	 * MargDepMatrix - Get an Unconditional Depndency Matrix.
	 * @param	D	dataset
	 * @param	op	how we will measure the dependency
	 * @return a L*L matrix representing Unconditional Depndence.
	 */
	public static double[][] margDepMatrix(Instances D, String op) {

		int L = D.classIndex();
		int N = D.numInstances();

		// Simple Co-occurence counts
		if (op.equals("C")) {
			int C[][] = getApproxC(D);
			double P[][] = getP(C,N);
			return P;
		}
		// Mutual information -- complete / multi-target capable
		if (op.equals("I")) {
			return I(D,L);
		}
		// Mutual information -- binary (multi-label) approximation
		if (op.equals("Ib")) {
			int C[][] = getC(D);
			//System.out.println(""+M.toString(C));
			double P[][] = getP(C,N);
			//System.out.println(""+M.toString(P));
			return I(P);
		}
		// Mutual information -- fast binary (multi-label) approximation
		if (op.equals("Ibf")) {
			int C[][] = getApproxC(D);
			//System.out.println(""+M.toString(C));
			double P[][] = getP(C,N);
			//System.out.println(""+M.toString(P));
			return I(P);
		}
		// Conditional information -- binary (multi-label)
		if (op.equals("H")) {
			int C[][] = getC(D);
			return H(C,N);
		}
		// Conditional information -- fast binary (multi-label) approximation
		if (op.equals("H")) {
			int C[][] = getApproxC(D);
			return H(C,N);
		}
		// Chi-squared
		if (op.equals("X")) {
			return chi2(D);
		}
		// Frequencies (cheap)
		if (op.equals("F")) {
			double F[][] = F(D);
			//System.out.println(""+M.toString(F));
			return F;
		}
		/*
		if (op == "C") {
			return getC(D);
		}
		*/
		System.err.println("No operation found; Using empty!");

		return new double[L][L];
	}

	/**
	 * I - Get a Mutual Information Matrix.
	 */
	public static double[][] I(int C[][], int N) {
		int L = C.length;
		double M[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				M[j][k] = I(C,j,k,N);
			}
		}
		return M;
	}

	/**
	 * H - Get a Conditional Entropy Matrix.
	 */
	public static double[][] H(int C[][], int N) {
		int L = C.length;
		double M[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				M[j][k] = H(C,j,k,N);
			}
		}
		return M;
	}

	/**
	 * H - Get a Conditional Entropy Matrix.
	 */
	public static double[][] H(Instances D) {
		int C[][] = getC(D);
		return H(C, D.classIndex());
	}

	private static double f (Instances Y,int j,int k) {

		double E = p(Y,j,1) * p(Y,k,1); 			// Expected vaule P(Y_j = j_)P(Y_k = k_)
		double O = P(Y,j,1,k,1);					// Observed value P(Y_j = j_, Y_k = k_)
		return E/O;
	}

	/**
	 * F - Relative frequency matrix (between p(j),p(k) and p(j,k)) in dataset D.
	 */
	public static double[][] F(Instances D) {
		int L = D.classIndex();
		double M[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				M[j][k] = Math.abs(1. - f(D,j,k));
			}
		}
		return M;
	}

	// A bit of a useless function -- get rid of it somehow?
	private static double[] fillError(Result result, int L) {

		double Yprob[][] = result.allPredictions(); 
		int Ytrue[][] = result.allActuals(); 
		double ts[] = ThresholdUtils.thresholdStringToArray(result.getInfo("Threshold"),L); // <-- @TODO should not assume this for multi-target
		int Ypred[][] = ThresholdUtils.threshold(Yprob,ts);

		double E[] = new double[L];
		for(int j = 0; j < L; j++) {
			//E[j] = 1.0 - result.output.get("Accuracy["+j+"]");
			E[j] = Metrics.P_Hamming(Ytrue,Ypred,j);
		}
		return E;
	}

	/**
	 * CondDepMatrix - Get a Conditional Depndency Matrix.
	 * @version My version, based on Zhang's 'LEAD' approach:<br> 
	 * the probability of labels j and k both getting errors on the same instance is L_loss(j)*L_loss(k)
	 * if the actual co-occurence is otherwise. 
	 * @version note: currently we are only looking at two kinds: are the scores correlated or not
	 * @version H0: the correlated scores == score*score
	 * @param	D	dataset
	 * @return a L*L matrix of Unconditional Depndence.
	 */
	public static double[][] condDepMatrix(Instances D, Result result) {

		int L = D.classIndex();
		int N = D.numInstances();
		double T[][] = MLUtils.getYfromD(D);						// OUTPUT (TEACHER)
		double Y[][] = M.threshold(result.allPredictions(),0.5);	// OUTPUT (PREDICTED)
		result.output = Result.getStats(result,"6");	            // <-- high verbosity, because we need individual accuracies				
		double E[] = fillError(result, L);							// ERRORS (EXPECTED)
		double F[][][] = new double[3][L][L];						// ERRORS (ACTUAL)
		// Find the actual co-occurence ...
		for(int i = 0; i < N; i++) {
			int y[] = A.toIntArray(Y[i],0.5); 				// predicted
			int t[] = A.toIntArray(T[i],0.5);					// actual (teacher)
			for(int j = 0; j < L; j++) {
				for(int k = j+1; k < L; k++) {
					if (y[j] != t[j] && y[k] != t[k]) {
						// if j incorrect and k also ...
						F[0][j][k]++;								// error type 0
					}
					else if (y[j] == t[j] && t[k] == y[k]) {
						// both are correct
						F[2][j][k]++;								// error type 2
					}
					else {
						// if only one is correct
						F[1][j][k]++;								// error type 1
					}
				}
			}
		}

		// UnNormalize with the Expected error
		double E_norm[][][] = new double[3][L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				E_norm[0][j][k] = N * (E[j] * E[k]);
				E_norm[2][j][k] = N * ((1.0 - E[k]) * (1.0 - E[j]));
				E_norm[1][j][k] = N * ( (E[j] * (1.0 - E[k])) + (1.0 - E[j]) * E[k]);
			}
		}
		return StatUtils.chi2(F,E_norm);
	}

	/**
	 * LEAD. 
	 * Do the chi-squared LEAD test on all labels in D.
	 * We would expect the 3 kinds of error to be uncorrelacted.
	 * However, if they are significantly correlated, this means that there is conditional dependence!
	 */
	public static double[][] LEAD2 (Instances D, Result result) {

		int L = D.classIndex();
		int N = D.numInstances();
		double Y[][] = MLUtils.getYfromD(D);						// Real
		double Y_[][] = M.threshold(result.allPredictions(),0.5);	// Predicted
		// Error
		double E[][] = M.subtract(Y,Y_); 
		// Expected (for each j)
		double X[][] = new double[L][L];

		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				for(int v : new int[]{0,1,-1}) { 
					double p_j = p(E,j,v);								// prior
					double p_k = p(E,k,v);								// prior
					double p_jk = P(E,j,v,k,v);							// joint
					double Exp = p_j * p_k;									// expected
					//System.out.println("v = "+v);
					//System.out.println("p_j "+p_j);
					//System.out.println("p_k "+p_k);
					//System.out.println("p_jk"+p_jk);
					X[j][k] += ( ((p_jk - Exp) * (p_jk - Exp)) / Exp );		// calc.
				}
				//System.out.println(""+X[j][k]);
				X[j][k] -= CRITICAL[1];
			}
		}
		return X;
	}

	/**
	 * LEAD - Performs LEAD on dataset 'D', with corresponding gresult 'R', and dependency measurement type 'MDType'.
	 */
	public static double[][] LEAD (Instances D, Result R, String MDType) {

		int L = D.classIndex();
		int N = D.numInstances();

		// Extract true labels from D, predicted labels from R
		double Ytrue[][] = MLUtils.getYfromD(D);						// True
		double Ypred[][] = M.threshold(R.allPredictions(),0.5);			// Predicted

		// Make Error matrix
		double E[][] = M.abs(M.subtract(Ytrue,Ypred)); 

		// Replace labels with errors
		Instances D_E = MLUtils.replaceZasClasses(new Instances(D),E,L);

		// Pass through any measure of marginal dependence
		return StatUtils.margDepMatrix(D_E,MDType);
	}

	public static double[][] LEAD (Instances D, Result result) {
		return LEAD(D,result,"I");
	}


	/**
	 * LEAD - Performs LEAD on dataset 'D', using BR with base classifier 'h', under random seed 'r'.
	 * @warning : changing this method will affect the perfomance of e.g., BCC -- on the other hand the original BCC paper did not use LEAD, so don't worry.
	 */
	public static double[][] LEAD(Instances D, Classifier h, Random r)  throws Exception {
		Instances D_r = new Instances(D);
		D_r.randomize(r);
		Instances D_train = new Instances(D_r,0,D_r.numInstances()*60/100);
		Instances D_test = new Instances(D_r,D_train.numInstances(),D_r.numInstances()-D_train.numInstances());
		BR br = new BR();
		br.setClassifier(h);
		Result result = Evaluation.evaluateModel((MultilabelClassifier)br,D_train,D_test,"PCut1","1"); 
		return LEAD2(D_test,result);
	}

	public static double[][] LEAD(Instances D, Classifier h, Random r, String MDType)  throws Exception {
		Instances D_r = new Instances(D);
		D_r.randomize(r);
		Instances D_train = new Instances(D_r,0,D_r.numInstances()*60/100);
		Instances D_test = new Instances(D_r,D_train.numInstances(),D_r.numInstances()-D_train.numInstances());
		BR br = new BR();
		br.setClassifier(h);
		Result result = Evaluation.evaluateModel((MultilabelClassifier)br,D_train,D_test,"PCut1","1"); 

		return LEAD(D_test, result, MDType);
	}

	/**
	 * Main - do some tests.
	 */
	public static void main(String args[]) throws Exception {
		Instances D = Evaluation.loadDataset(args);
		MLUtils.prepareData(D);
		int L = D.classIndex();

		double CD[][] = null;

		if (args[2].equals("L")) {
			String I = "I";
			if (args.length >= 3) 
				I = args[3];
			CD = StatUtils.LEAD(D, new SMO(), new Random(), I);
		}
		else {
			CD = StatUtils.margDepMatrix(D,args[2]);
		}
		System.out.println(M.toString(CD,"M"+args[2]));
	}

}
