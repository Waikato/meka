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

import meka.core.SuperLabelUtils;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;
import weka.core.RevisionUtils;

import java.util.Arrays;

/**
 * HASEL - Partitions labels into subsets based on the dataset defined hierarchy.
 * Note: assuming that a <code>.</code> (fullstop/period) in the attribute names defines hierarchical branches, e.g., <code>Europe.Spain</code>.
 * @author 	Jesse Read 
 * @version June 2014
 */
public class HASEL extends RAkELd {

	/** for serialization. */
	private static final long serialVersionUID = -6208388889440497988L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "Partitions labels into subsets based on the dataset defined hierarchy (assuming that a '.' in the attribute names defines hierarchical branches, e.g., \"Europe.Spain\").";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();
		int N = D.numInstances();

		// Get partition from dataset hierarchy
		kMap = SuperLabelUtils.getPartitionFromDatasetHierarchy(D); 
		m_M = kMap.length;
		m_Classifiers = AbstractClassifier.makeCopies(m_Classifier,m_M);
		m_InstancesTemplates = new Instances[m_M];

		for(int i = 0; i < m_M; i++) {

			if (getDebug()) 
				System.out.println("Building model "+(i+1)+"/"+m_M+": "+Arrays.toString(kMap[i]));
			Instances D_i = SuperLabelUtils.makePartitionDataset(D,kMap[i]);
			m_Classifiers[i].buildClassifier(D_i);
			m_InstancesTemplates[i] = new Instances(D_i,0);
		}

	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new HASEL(), args);
	}

}
