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

package meka.classifiers.multiltarget.incremental;

import meka.classifiers.incremental.IncrementalEvaluation;
import meka.classifiers.multitarget.CR;
import meka.classifiers.multitarget.IncrementalMultiTargetClassifier;
import meka.core.MLUtils;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.trees.HoeffdingTree;
import weka.core.Instance;

/**
 * CRUpdateable.java - Updateable CR.
 * Must be run with an UpdateableClassifier base classifier.
 * @see CR
 * @author 		Joerg Wicker
 * @version 	December, 2017
 */
public class CRUpdateable extends CR implements IncrementalMultiTargetClassifier {

	/** for serialization. */
  	private static final long serialVersionUID = 6705611077773512052L;

	@Override
	public String globalInfo() {
		return "Updateable CR\nMust be run with an Updateable base classifier.";
	}

	public CRUpdateable() {
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

		if(getDebug()) System.out.print("-: Updating "+L+" models");

		for(int j = 0; j < L; j++) {
			Instance x_j = (Instance)x.copy();
			x_j.setDataset(null);
			x_j = MLUtils.keepAttributesAt(x_j,new int[]{j},L);
			x_j.setDataset(m_InstancesTemplates[j]);
			((UpdateableClassifier)m_MultiClassifiers[j]).updateClassifier(x_j);
		}

		if(getDebug()) System.out.println(":- ");
	}

	public static void main(String args[]) {
		IncrementalEvaluation.runExperiment(new CRUpdateable(),args);
	}

}
