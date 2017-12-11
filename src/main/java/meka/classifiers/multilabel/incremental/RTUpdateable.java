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

import meka.classifiers.incremental.IncrementalEvaluation;
import meka.classifiers.multilabel.IncrementalMultiLabelClassifier;
import meka.classifiers.multilabel.RT;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.trees.HoeffdingTree;
import weka.core.Instance;

/**
 * RTUpdateable.java - Updateable RT.
 * Must be given an UpdateableClassifier base classifier.
 * @see RT
 * @author 	Jesse Read
 * @version October, 2011
 */
public class RTUpdateable extends RT implements IncrementalMultiLabelClassifier {

	/** for serialization. */
  	private static final long serialVersionUID = 3766003607269541755L;

	@Override
	public String globalInfo() {
		return "Updateable RT\nMust be run with an Updateable base classifier.";
	}

	public RTUpdateable() {
		// default classifier for GUI
		this.m_Classifier = new HoeffdingTree();
	}

	@Override
	protected String defaultClassifierString() {
		// default classifier for CLI
		return "weka.classifiers.trees.HoeffdingTree";
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
