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

package meka.classifiers.multilabel.meta;

import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.core.MLUtils;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;

/**
 * CM.java - Classification Maximization using any multi-label classifier.
 *
 * A specified multi-label classifier is built on the training data. This model is then used to classify unlabelled data (e.g., the test data)
 * The classifier is then retrained with all data, and the cycle continues ... (for I iterations).
 *
 * @version July, 2014
 * @author 	Jesse Read
 * @see 	EM
 */
public class CM extends EM {

	private static final long serialVersionUID = -6297505619194774433L;

	@Override
	public String globalInfo() {
		return "Train a classifier using labelled and unlabelled data (semi-supervised) using Classification Expectation algorithm (a hard version of EM). Unlike EM, can use any classifier here, not necessarily one which gives good probabilistic output.";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);

		if (getDebug()) 
			System.out.println("Initial build ...");
	  	
		m_Classifier.buildClassifier(D); 

		Instances DA = MLUtils.combineInstances(D,D_);

		if (getDebug()) 
			System.out.print("Performing "+m_I+" 'CM' Iterations: [");
		for(int i = 0; i < m_I; i++) {
			if (getDebug())
				System.out.print(".");
			// classification
			updateWeights((ProblemTransformationMethod)m_Classifier, DA);
			// maximization (of parameters)
			m_Classifier.buildClassifier(DA);
		}
		System.out.println("]");
	}

	@Override
	protected void updateWeights(ProblemTransformationMethod h, Instances D) throws Exception {
		for(Instance x : D) {
			double y[] = h.distributionForInstance(x);
			for(int j = 0; j < y.length; j++) {
				x.setValue(j,(y[j] < 0.5) ? 0. : 1.);
			}
		}
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new CM(), args);
	}

}
