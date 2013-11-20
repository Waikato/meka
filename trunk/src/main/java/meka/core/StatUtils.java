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

import rbms.M;

/**
 * StatUtils - Helpful statistical functions.
 */
public abstract class StatUtils {

	// @@TODO RENAME (AND PURGE MANY OF) THESE FUNCTIONS URGENTLY!
	//
	
	//
	// THE EMPIRICAL PROBABILITY DISTRIBUTIONS
	//

	/**
	 * P.
	 * same as below, but assuming that x[0,...,L] refers to indices j[0,...,L]
	 */
	public static double[] priors(double Y[][], int x[]) {
		int L = x.length;
		return priors(Y,MLUtils.gen_indices(L),x);
	}

	/**
	 * [P(j[1]==x[1]), P(j[2]==x[2]), ..., P(j[L]==x[L])]
	 * @param	j	indices
	 * @param	x	values
	 */
	public static double[] priors(double Y[][], int j[], int x[]) {
		int L = j.length;
		double p[] = new double[L];
		for(int j_ = 0; j_ < L; j_++) {
			p[j_] = prior(Y,j[j_],x[j_]);
		}
		return p;
	}

	/**
	 * P(Y_j==k) in Y.
	 * In the multi-label case, k in {0,1}
	 */
	public static double prior(double Y[][], int j, int k) {
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
	 * P(Y_j==k) in D.
	 */
	public static double prior(Instances D, int j, int k) {
		return prior(MLUtils.getYfromD(D),j,k);
	}

	/**
	 * P(Y_j = v, Y_k = w) in Y.
	 * Multi-target friendly.
	 */
	public static double prior(double Y[][], int j, int v, int k, int w) {
		int N = Y.length;
		double p = 0.0001;
		for(int i = 0; i < N; i++) {
			if (((int)Math.round(Y[i][j]) == v) && ((int)Math.round(Y[i][k]) == w))
				p += 1.0;
		}
		return p/N;
	}

	/**
	 * P(Y_j = v, Y_k = w) in D.
	 */
	public static double prior(Instances D, int j, int v, int k, int w) {
		return prior(MLUtils.getYfromD(D),j,v,k,w);
	}

	/**
	 * Delta(x_1,x_2,x_3 = v_1,v_2,v_3) for j = 1,2,3, k = 1,2,3.
	 */
	public static boolean match(Instance x, int indices[], int values[]) {
		for(int j = 0; j < indices.length; j++) {
			int v = (int)Math.round(x.value(indices[j]));
			if (v != values[j]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Joint Distribution. 
	 * Multi-target friendly.
	 * @param	indices[]	e.g., 1,2,3
	 * @param	values[]	e.g., 0,0,1
	 * @return 	P(x_1,x_2,x_3 = v_1,v_2,v_3) for j = 1,2,3, k = 1,2,3.
	 */
	public static double joint(Instances D, int indices[], int values[]) {
		int N = D.numInstances();
		int n = 0;
		for (Instance x : D) {
			if (match(x,indices,values))
				n++;
		}
		return Math.max(0.0001,(double)n/N);
	}

	/**
	 * Joint Distribution.
	 * Returns the joint distribution of the jth and kth labels in D.
	 */
	public static double[][] jointDistribution(Instances D, int j, int k) {
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
	 * Returns the joint distribution of the jth and kth and lth labels in D.
	 */
	public static double[][][] jointDistribution(Instances D, int j, int k, int l) {
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
	 * I - Information Gain. 
	 * Multi-target friendly (does not assume binary labels).
	 * @return I(Y_j;Y_k) in dataset D.
	 */
	public static double I(Instances D, int j, int k) {
		double I = 0.0;
		for(int x = 0; x < D.attribute(j).numValues(); x++) {
			double p_x = prior(D,j,x);
			for(int y = 0; y < D.attribute(k).numValues(); y++) {
				double p_y = prior(D,k,y);
				double p_xy = prior(D,j,x,k,y);
				I += p_xy * Math.log ( p_xy / ( p_x * p_y) );
			}
		}
		return I;
	}

	/**
	 * Chi^2 - Do the chi-squared test on all pairs of labels in D.
	 * @return	A table X with all the pairwise Chi^2 values.
	 */
	public static double[][] chi2 (Instances D) {
		int L = D.classIndex();
		double X[][] = new double[L][L];
		for(int j = 0; j < D.classIndex(); j++) {
			for(int k = j+1; k < D.classIndex(); k++) {
				X[j][k] = StatUtils.chi2(D,j,k);
				//System.out.println("X^2 = "+StatUtils.chi2(D,j,k));
			}
		}
		return X;
	}

	/**
	 * Chi^2 - Do the chi-squared test on the jth and kth labels in Y.
	 * @NOTE multi-label only! @TODO Use enumerateValues() !!!
	 * If they are correlated, this means unconditional dependence!
	 * @return	The chi-square statistic for labels j and k in Y.
	 */
	public static double chi2 (Instances Y,int j,int k) {
		// H_0 : p(Y_j,Y_k) = p(Y_j)p(Y_k)

		double chi2 = 0.0;
		for(int j_ = 0; j_ < 2; j_++) {
			for(int k_ = 0; k_ < 2; k_++) {
				double E = prior(Y,j,j_) * prior(Y,k,k_); 		// Expected vaule P(Y_j = j_)P(Y_k = k_)
				double O = prior(Y,j,j_,k,k_);					// Observed value P(Y_j = j_, Y_k = k_)
				chi2 += ( ((O - E) * (O - E)) / E );
			}
		}
		return chi2;
	}

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
	 * CHI-SQUARED.
	 * Calculate X where X[j][k] is a positive number.
	 * If X[j][k] > CRITICAL[DoF] then the difference is significant (reject the NULL hypothesis).
	 * Normalise X to V.
	 * If V[j][k] > 0 then the difference is significant (reject the NULL hypothesis).
	 * @return	X	
	 */
	public static double[][] chi2 (double M[][], double Exp[][], int DoF) {
		int L = M.length;
		double V[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				double J = M[j][k];		// actual (joint) 			p(e==e)
				double E = Exp[j][k];	// expected (prior*prior)	
				System.out.println("J = "+J);
				System.out.println("J = "+E);
				V[j][k] += ( ((J - E) * (J - E)) / E );
				J = 1.0-M[j][k];		// actual (joint) 			p(e!=e)
				E = 1.0-Exp[j][k];		// expected (prior*prior)	
				V[j][k] += ( ((J - E) * (J - E)) / E );
			}
		}
		System.out.println(rbms.M.toString((double[][])V));
		// offset
		double p = CRITICAL[DoF];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				V[j][k] -= p;
			}
		}
		return V;
	}

	//public static final double CRITICAL[] = new double[]{0.,0.455, 1.386, 2.366, 3.357}; // P == 0.50  
	//public static final double CRITICAL[] = new double[]{0.,1.642, 3.219, 4.642, 5.989}; // P == 0.20  
  	public static final double CRITICAL[] = new double[]{0.,2.706, 4.605, 6.251, 7.779}; // P == 0.10
	//	public static final double CRITICAL[] = new double[]{0.,3.841, 4.591, 7.815, 9.488}; // P == 0.05
//	public static final double CRITICAL[] = new double[]{0.,6.635, 9.210, 11.245,13.277}; // P == 0.01
	//public static final double CRITICAL[] = new double[]{0.,10.827, 13.815, 16.268,18.465}; // P == 0.001

	public static double[][] chi2 (double M[][], double E, int DoF) {
		int L = M.length;
		double V[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				double J = M[j][k];
				V[j][k] += ( ((J - E) * (J - E)) / E );
			}
		}
		// offset
		double p = CRITICAL[DoF];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				V[j][k] -= p;
			}
		}
		return V;
	}


	public static double[][] getUnconditionalDependencies(Instances D) {
		return getUnconditionalDependencies(D,D.classIndex());
	}

	/**
	 * GetUnconditionalDependencies - Get an Unconditional Depndency Matrix.
	 * (Works for both ML and MT data).
	 * @param	D	dataset
	 * @param	L	number of labels
	 * @return a L*L matrix representing Unconditional Depndence.
	 */
	public static double[][] getUnconditionalDependencies(Instances D, int L) {
		double M[][] = new double[L][L];
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				// get I(Y_j;X_k)
				M[j][k] = I(D,j,k);
			}
		}
		return M;
	}

	// A bit of a useless function -- get rid of it somehow?
	private static double[] fillError(Result result, int L) {
		double E[] = new double[L];
		for(int j = 0; j < L; j++) {
			E[j] = 1.0 - result.output.get("L"+j+"_acc");
		}
		return E;
	}

	/**
	 * LEAD. 
	 * MYVERSION.
	 * the probability of labels j and k both getting errors on the same instance is L_loss(j)*L_loss(k)
	 * if the actual co-occence is otherwise, 
	 * @version note: currently we are only looking at two kinds: are the scores correlated or not
	 * @version H0: the correlated scores == score*score
	 */
	public static double[][] LEAD (Instances D, Result result) {

		int L = D.classIndex();
		int N = D.numInstances();
		double T[][] = MLUtils.getYfromD(D);						// OUTPUT (TEACHER)
		double Y[][] = M.threshold(result.allPredictions(),0.5);	// OUTPUT (PREDICTED)
		result.output = Result.getStats(result);					
		double E[] = fillError(result, L);							// ERRORS (EXPECTED)
		double F[][][] = new double[3][L][L];						// ERRORS (ACTUAL)
		// Find the actual co-occurence ...
		for(int i = 0; i < N; i++) {
			int y[] = MLUtils.toIntArray(Y[i],0.5); 				// predicted
			int t[] = MLUtils.toIntArray(T[i],0.5);					// actual (teacher)
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
					double p_j = prior(E,j,v);								// prior
					double p_k = prior(E,k,v);								// prior
					double p_jk = prior(E,j,v,k,v);							// joint
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

}
