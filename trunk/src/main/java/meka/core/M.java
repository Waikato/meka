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


