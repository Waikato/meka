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

package weka.classifiers.multitarget.meta;

import java.util.HashMap;

import weka.classifiers.multilabel.MultilabelClassifier;
import weka.classifiers.multilabel.meta.EnsembleML;
import weka.classifiers.multitarget.MultiTargetClassifier;
import weka.core.Instance;
import meka.core.MLUtils;
import weka.core.RevisionUtils;

/**
 * The Multi-Target Version of EnsembleML.
 * It takes votes using the confidence outputs of the base classifier.
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version	Sepetember 2012
 */

public class EnsembleMT extends EnsembleML implements MultiTargetClassifier {

	/** for serialization. */
	private static final long serialVersionUID = 1213045324147680550L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"The Multi-Target Version of EnsembleML.\n"
				+ "It takes votes using the confidence outputs of the base classifier.";
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		HashMap<Integer,Double> votes[] = new HashMap[L];
		for(int j = 0; j < L; j++) {
			votes[j] = new HashMap<Integer,Double>();
		}

		double y[] = new double[L];

		for(int m = 0; m < m_NumIterations; m++) {
			double c[] = ((MultilabelClassifier)m_Classifiers[m]).distributionForInstance(x);
			// votes[j] = votes[j] + P(j|x)		@TODO: only if c.length > L
			for(int j = 0; j < L; j++) {
				Double w = votes[j].containsKey((int)c[j]) ? votes[j].get((int)c[j]) + c[j+L] : c[j+L];
				votes[j].put((int)c[j] , w);
			}
		}

		for(int j = 0; j < L; j++) {
			// get the class with max weight
			y[j] = (Integer)MLUtils.maxItem(votes[j]);
		}

		return y;
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new EnsembleMT(),args);
	}

}

