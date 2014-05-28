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

package meka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.functions.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.supervised.attribute.*;
import weka.attributeSelection.*;
import weka.filters.*;
import weka.core.*;
import meka.core.A;
import weka.core.TechnicalInformation.*;
import java.util.*;

/**
 * PCC.java - (Bayes Optimal) Probabalistic Classifier Chains.
 * Exactly like CC at build time, but explores all possible paths as inference at test time (hence, 'Bayes optimal'). <br>
 * This version is multi-target capable. <br>
 * See: Cheng et al, "Bayes Optimal Multi-label Classification via Probabalistic Classifier Chains", ICML 2010.
 *
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version	November 2012
 */
public class PCC extends CCe implements TechnicalInformationHandler{ // MT Capable

	/**
	 * Push - increment y[0] until = K[0], then reset and start with y[0], etc ...
	 * @return	True if finished
	 */
	private boolean push(double y[], int K[], int j) {
		if (j >= y.length) {
			return true;
		}
		else if (y[j] < K[j]-1) {
			y[j]++;
			return false;
		}
		else {
			y[j] = 0.0;
			return push(y,K,++j);
		}
	}

	/**
	 * GetKs - return [K_1,K_2,...,K_L] where each Y_j \in {1,...,K_j}.
	 * @param	D	a dataset
	 */
	private static int[] getKs(Instances D) {
		int L = D.classIndex();
		int K[] = new int[L];
		for(int k = 0; k < L; k++) {
			K[k] = D.attribute(k).numValues();
		}
		return K;
	}

	@Override
	public double[] distributionForInstance(Instance xy) throws Exception {

		int L = xy.classIndex(); 

		double y[] = new double[L];
		double w  = 0.0;

		/*
		 * e.g. K = [3,3,5]
		 * we push y_[] from [0,0,0] to [2,2,4] over all necessary iterations.
		 */
		int K[] = getKs(xy.dataset());
		if (getDebug()) 
			System.out.println("K[] = "+Arrays.toString(K));
		double y_[] = new double[L]; 

		int i = 0;
		for(i = 0; i < 1000000; i++) { // limit to 1m
			//System.out.println(""+i+" "+Arrays.toString(y_));
			double w_  = A.product(super.probabilityForInstance(xy,y_));
			if (w_ > w) {
				if (getDebug()) System.out.println("y' = "+Arrays.toString(y_)+", :"+w_);
				y = Arrays.copyOf(y_,y_.length);
				w = w_;
			}
			if (push(y_,K,0))
				break;
		}
		if (getDebug())
			System.out.println("Tried all "+(i+1)+" combinations.");

		return y;
	}

	@Override
	public String globalInfo() {
		return "Probabalistic Classifier Chains. " + "For more information see:\n" + getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		
		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Weiwei Cheng and Krzysztof Dembczynsky and Eyke Hullermeier");
		result.setValue(Field.TITLE, "Bayes Optimal Multi-label Classification via Probabalistic Classifier Chains");
		result.setValue(Field.BOOKTITLE, "ICML '10: 27th International Conference on Machine Learning");
		result.setValue(Field.YEAR, "2010");

		return result;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new PCC(),args);
	}

}
