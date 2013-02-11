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

/**
 * BRUpdateable.java - Updateable BR.
 * 
 * The Binary Relevance Method Updateable (must be given an UpdateableClassifier base classifier)
 * @see BR.java
 * @author 		Jesse Read (jesse@tsc.uc3m.es)
 * @version 	September, 2011
 */
import weka.classifiers.UpdateableClassifier;
import weka.core.Instance;
import weka.core.MLUtils;
import weka.core.WindowIncrementalEvaluator;

public class BRUpdateable extends BR implements UpdateableClassifier {

	/** for serialization. */
  	private static final long serialVersionUID = 6705611077773512052L;


	@Override
	public String globalInfo() {
		return "Updateable BR\nMust be run with an Updateable base classifier.";
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {

		int L = x.classIndex();
		// turn x into [x_1,...,x_L]

		if(getDebug()) System.out.print("-: Updating "+L+" models");

		for(int j = 0; j < m_MultiClassifiers.length; j++) {
			Instance x_j = (Instance)x.copy();
			x_j.setDataset(null);
			x_j = MLUtils.keepAttributesAt(x_j,new int[]{j},L);
			x_j.setDataset(m_InstancesTemplate);
			((UpdateableClassifier)m_MultiClassifiers[j]).updateClassifier(x_j);
		}

		if(getDebug()) System.out.println(":- ");
	}

	public static void main(String args[]) {
		IncrementalEvaluation.runExperiment(new BRUpdateable(),args);
	}

}
