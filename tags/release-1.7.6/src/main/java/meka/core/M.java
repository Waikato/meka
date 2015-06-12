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

import Jama.Matrix;
import java.util.*;

/**
 * M.java - Handy matrix operations (on 2D arrays[][])
 */
public abstract class M {

	/**
	 * GetCol - return the k-th column of M (as a vector).
	 */
	public static double[] getCol(double[][] M, int k) {
        double[] col_k = new double[M.length];
        for (int i = 0; i < M.length; i++) {
            col_k[i] = M[i][k];
        }
        return col_k;
    }
    
	/**
	 * GetCol - return the k-th column of M (as a vector).
	 */
	public static int[] getCol(int[][] M, int k) {
        int[] col_k = new int[M.length];
        for (int i = 0; i < M.length; i++) {
            col_k[i] = M[i][k];
        }
        return col_k;
    }
    
	public static double[][] addBias(double[][] M) {
		final double[][] C = new double[M.length][M[0].length+1];
		for (int i = 0; i < M.length; i++) {
			C[i][0] = 1.0;
			for(int j = 0; j < M[i].length; j++) {
				C[i][j+1] = M[i][j];
			}
		}
        return C;
    }

	public static Matrix addBias(Matrix M) {
		double[][] M_ = M.getArray();
		final double[][] C = new double[M_.length][M_[0].length+1];
		for (int i = 0; i < M_.length; i++) {
			C[i][0] = 1.0;
			for(int j = 0; j < M_[i].length; j++) {
				C[i][j+1] = M_[i][j];
			}
		}
		return new Matrix(C);
    }

	public static double[][] removeBias(double[][] M) {
		final double[][] C = new double[M.length][M[0].length-1];
		for (int i = 0; i < M.length; i++) {
			for(int j = 1; j < M[i].length; j++) {
				C[i][j-1] = M[i][j];
			}
		}
        return C;
    }

	/**
	 * Multiply - multiply each value in A[][] by constant K.
	 */
	public static double[][] multiply(final double[][] A, double K) {

        final double[][] C = new double[A.length][A[0].length];

		for (int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++) {
					C[i][j] = A[i][j] * K;
			}
		}
        return C;
    }

	/**
	 * ToString - return a String representation (to adp decimal places).
	 */
	public static String toString(double M_[][], int adp) {
		int width = adp > 0 ? adp + 2 : 0;
		StringBuilder sb = new StringBuilder();  
		for(int j = 0; j < M_.length; j++) {
			for(int k = 0; k < M_[j].length; k++) {
			//sb.append(Utils.doubleToString(v[k],w,adp));
				double d = M_[j][k];
				String num = String.format("%6.2f", d);
				if (adp == 0) // cheap override
					num = String.format("%2.0f", d);
				sb.append(num);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * ToString - return a String representation.
	 */
	public static String toString(double M_[][]) {
		return toString(M_,2);
		//sb.append(A.toString(s.predictions.get(i),2));
	}

	/**
	 * ToString - return a String representation.
	 */
	public static String toString(int M_[][]) {
		StringBuilder sb = new StringBuilder();  
		for(int j = 0; j < M_.length; j++) {
			for(int k = 0; k < M_[j].length; k++) {
				String num = String.format("%5d", M_[j][k]);
				sb.append(num);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * ToString - return a String representation of 'M', in Matlab format, called 'name'.
	 */
	public static String toString(double M[][], String name) {
		StringBuilder sb = new StringBuilder(name+" = [\n");  
		for(int j = 0; j < M.length; j++) {
			for(int k = 0; k < M[j].length; k++) {
				sb.append(String.format("%6.2f ", M[j][k]));
			}
			sb.append(";\n");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Threshold - apply threshold t to matrix P[][].
	 */
	public static final double[][] threshold(double P[][], double t) {
		double X[][] = new double[P.length][P[0].length];
		for(int i = 0; i < P.length; i++) {
			for(int j = 0; j < P[i].length; j++) {
				X[i][j] = (P[i][j] > t) ? 1. : 0.;
			}
		}
		return X;
	}

	/**
	 * Flatten - turn Matrix [0 1; 2 3] into vector [0 1 2 3].
	 */
	public static int[] flatten(int M[][]) {
		int v[] = new int[M.length * M[0].length];
		int k = 0;
		for(int i = 0; i < M.length; i++) {
			for(int j = 0; j < M[i].length; j++) {
				v[k++] = M[i][j];
			}
		}
		return v;
	}

	public static double[][] subtract(double[][] A, double[][] B) {
		//if (A.length != bRows) // no can do
		//	throw new IllegalArgumentException(" A.cols ("+aCols+") != B.rows ("+bRows+") ");
        double[][] C = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++ ) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        return C;
    }

	// absolute value
	public static double[][] abs(double[][] A) {
		double[][] C = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++ ) {
                C[i][j] = Math.abs(A[i][j]);
            }
        }
        return C;
	}

	// squared sum
	public static double SS(double M[][]) {
		double sum = 0;
        for (int i = 0; i < M.length; i++) {
			for (int j = 0; j < M[i].length; j++) {
				sum += M[i][j];
			}
        }
        return sum;
	}

	/**
	 * Sigmoid / Logistic function
	 */
	public static final double sigma(double a) {
		return 1.0/(1.0+Math.exp(-a));
	}

	/**
	 * Sigmoid function applied to vector
	 */
	public static final double[] sigma(double v[]) {
		double u[] = new double[v.length];
		for(int j = 0; j < v.length; j++) {
			u[j] = sigma(v[j]);
		}
		return u;
	}

	/**
	 * Sigmoid function applied to matrix (2D array)
	 */
	public static final double[][] sigma(double A[][]) {
		double X[][] = new double[A.length][A[0].length];
		for(int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++) {
				X[i][j] = sigma(A[i][j]);
			}
		}
		return X;
	}

	/**
	 * Sigmoid function applied to Matrix
	 */
	public static final Matrix sigma(Matrix A) {
		return new Matrix(sigma(A.getArray()));
	}

	/**
	 * Derivative of the sigmoid function applied to scalar
	 */
	public static final double dsigma(double a) {
		double s = sigma(a);
		return s * (1. - s);
	}

	/**
	 * Derivative of the sigmoid function applied to vector
	 */
	public static final double[] dsigma(double v[]) {
		double u[] = new double[v.length];
		for(int j = 0; j < v.length; j++) {
			u[j] = dsigma(v[j]);
		}
		return u;
	}

	/**
	 * Derivative of the sigmoid function applied to Matrix
	 */
	public static final double[][] dsigma(double A[][]) {
		double X[][] = new double[A.length][A[0].length];
		for(int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++) {
				X[i][j] = dsigma(A[i][j]);
			}
		}
		return X;
	}

	/**
	 * Derivative of the sigmoid function applied to Jama Matrix
	 */
	public static final Matrix dsigma(Matrix A) {
		double A_[][] = A.getArray();
		double X[][] = new double[A_.length][A_[0].length];
		for(int i = 0; i < A_.length; i++) {
			for(int j = 0; j < A_[i].length; j++) {
				X[i][j] = dsigma(A_[i][j]);
			}
		}
		return new Matrix(X);
	}

	/**
	 * Deep Copy - Make a deep copy of M[][].
	 */
	public static int[][] deep_copy(int M[][]) { 
		int[][] C = new int[M.length][];
		for(int i = 0; i < C.length; i++) {
			C[i] = Arrays.copyOf(M[i],M[i].length);
		}
		return C;
	}

}


