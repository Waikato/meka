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

package meka.classifiers.multilabel.incremental;

import weka.classifiers.UpdateableClassifier;
import weka.core.Instance;
import meka.classifiers.multilabel.RT;

/**
 * RTUpdateable.java - Updateable RT.
 * Must be given an UpdateableClassifier base classifier.
 * @see RT
 * @author 	Jesse Read
 * @version October, 2011
 */
public class RTUpdateable extends RT implements UpdateableClassifier {

	/** for serialization. */
  	private static final long serialVersionUID = 3766003607269541755L;

	@Override
	public String globalInfo() {
		return "Updateable RT\nMust be run with an Updateable base classifier.";
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {

		int L = x.classIndex();

		for (int j = 0; j < L; j++) {
			if(x.value(j) > 0.0) {
				Instance x_j = convertInstance(x);
				x_j.setClassValue(j);
				((UpdateableClassifier)m_Classifier).updateClassifier(x_j);
			}
		}
	}

	public static void main(String args[]) {
		IncrementalEvaluation.runExperiment(new RTUpdateable(),args);
	}

}
