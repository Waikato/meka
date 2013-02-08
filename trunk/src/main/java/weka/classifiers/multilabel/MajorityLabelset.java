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

package weka.classifiers.multilabel;

import java.util.HashMap;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.MLUtils;
import weka.core.RevisionUtils;

/**
 * MajorityLabelset.
 * 
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 * @version October 2011
 */
public class MajorityLabelset extends MultilabelClassifier {

	/** for serialization. */
	private static final long serialVersionUID = -5932291001079843869L;
	
	protected double prediction[] = null;
	protected HashMap<String,Double> classFreqs = new HashMap<String,Double>();

	protected double maxValue = 0.0;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "Majority Labelset Classifier: Always predict the combination of labels which occurs most frequently in the training set.";
	}

	protected void updateCount(Instance x, int L) {
		String y = MLUtils.toBitString(x,L);

		if (classFreqs.containsKey(y)) {
			double freq = classFreqs.get(y)+x.weight();
			classFreqs.put(y, freq);
			if (maxValue < freq) {
				maxValue = freq;
				this.prediction = MLUtils.fromBitString(y);
			}
		} else {
			classFreqs.put(y, x.weight());
		}
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);
	  	
		int L = D.classIndex();
		this.prediction = new double[L];

		for(int i = 0; i < D.numInstances(); i++) {
			updateCount(D.instance(i),L);
		}

	}

	@Override
	public double[] distributionForInstance(Instance test) throws Exception {
		return prediction;
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new MajorityLabelset(),args);
	}

}
