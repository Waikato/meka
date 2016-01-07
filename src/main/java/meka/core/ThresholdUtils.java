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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * ThresholdUtils - Helpful functions for calibrating thresholds.
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version	March 2013
 */
public abstract class ThresholdUtils {

	/**
	 * ThresholdStringToArray - parse a threshold option string to an array of L thresholds (one for each label variable).
	 */
	public static double[] thresholdStringToArray(String top, int L) {
		if (top.startsWith("[")) {
			//if (L != 
			return MLUtils.toDoubleArray(top);							// threshold vector       [t1 t2 ... tL]]
		}
		else {
			double t[] = new double[L];
			Arrays.fill(t,Double.parseDouble(top));					// make a threshold vector [t t t ... t]
			return t;
		}
	}

	/**
	 * CalibrateThreshold - Calibrate a threshold using PCut: the threshold which results in the best approximation of the label cardinality of the training set.
	 * @param	Y			labels
	 * @param	LC_train	label cardinality of the training set
	 */
	public static double calibrateThreshold(ArrayList<double[]> Y, double LC_train) { 

		if (Y.size() <= 0) 
			return 0.5;

		int N = Y.size();
		ArrayList<Double> big = new ArrayList<Double>();
		for(double y[] : Y) {
			for (double y_ : y) {
				big.add(y_);
			}
		}
		Collections.sort(big);

		int i = big.size() - (int)Math.round(LC_train * (double)N);
		
		if (N == big.size()) { // special cases
			if (i+1 == N) // only one!
				return (big.get(N-2)+big.get(N-1)/2.0);
			if (i+1 >= N) // zero!
				return 1.0;
			else
			    return Math.max(((double)(big.get(i)+big.get(i+1))/2.0), 0.00001);
		}

		return Math.max(((double)(big.get(i)+big.get(Math.max(i+1,N-1))))/2.0 , 0.00001);
	}

	/**
	 * CalibrateThreshold - Calibrate a vector of thresholds (one for each label) using PCut: the threshold t[j] which results in the best approximation of the frequency of the j-th label in the training data.
	 * @param	Y			labels
	 * @param	LC_train	average frequency of each label
	 */
	public static double[] calibrateThresholds(ArrayList<double[]> Y, double LC_train[]) { 

		int L = LC_train.length;
		double t[] = new double[L];

		ArrayList<double[]> Y_[] = new ArrayList[L];
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

	/**
	 * Threshold - returns the labels after the prediction-confidence vector is passed through a vector of thresholds.
	 * @param	Rpred	label confidence predictions in [0,1]
	 * @param	t			threshold for each label
	 */
	public static final int[][] threshold(double Rpred[][], double t[]) {
		int Ypred[][] = new int[Rpred.length][Rpred[0].length];
		for(int i = 0; i < Rpred.length; i++) {
			for(int j = 0; j < Rpred[i].length; j++) {
				Ypred[i][j] = (Rpred[i][j] >= t[j]) ? 1 : 0;
			}
		}
		return Ypred;
	}

	/**
	 * Threshold - returns the labels after the prediction-confidence vector is passed through threshold.
	 * @param	Rpred	label confidence predictions in [0,1]
	 * @param	t			threshold
	 */
	public static final int[][] threshold(double Rpred[][], double t) {
		int Ypred[][] = new int[Rpred.length][Rpred[0].length];
		for(int i = 0; i < Rpred.length; i++) {
			for(int j = 0; j < Rpred[i].length; j++) {
				Ypred[i][j] = (Rpred[i][j] >= t) ? 1 : 0;
			}
		}
		return Ypred;
	}

	/**
	 * Threshold - returns the labels after the prediction-confidence vector is passed through threshold(s).
	 * @param	rpred	label confidence predictions in [0,1]
	 * @param	ts		threshold String
	 */
	public static final int[] threshold(double rpred[], String ts) {
		int L = rpred.length;
		double t[] = thresholdStringToArray(ts,L);
		int ypred[] = new int[L];
		for(int j = 0; j < L; j++) {
			ypred[j] = (rpred[j] >= t[j]) ? 1 : 0;
		}
		return ypred;
	}

	/**
	 * Round - simply round numbers (e.g., 2.0 to 2) -- for multi-target data (where we don't *yet* use a threshold).
	 * @param	Rpred	class predictions in [0,1,...,K]
	 * @return  integer representation of the predictions
	 */
	public static final int[][] round(double Rpred[][]) {
		int Ypred[][] = new int[Rpred.length][Rpred[0].length];
		for(int i = 0; i < Rpred.length; i++) {
			for(int j = 0; j < Rpred[i].length; j++) {
				Ypred[i][j] = (int)Math.round(Rpred[i][j]);
			}
		}
		return Ypred;
	}

	

}
