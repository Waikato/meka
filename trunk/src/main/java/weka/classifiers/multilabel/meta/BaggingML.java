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

package weka.classifiers.multilabel.meta;

import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Randomizable;
import weka.core.RevisionUtils;

/**
 * BaggingML.java - Combining several multi-label classifiers using Bootstrap AGGregatING.
 * Uses Instance weights instead of Instance duplications.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */

public class BaggingML extends MultilabelMetaClassifier {

	/** for serialization. */
	private static final long serialVersionUID = -6208337124420497991L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"Combining several multi-label classifiers using Bootstrap AGGregatING.\n"
				+ "Uses Instance weights instead of Instance duplications.";
	}

	public BaggingML() {
		// default for Bagging
		this.m_BagSizePercent = 100;
	}

	@Override
	public void buildClassifier(Instances train) throws Exception {
	  	testCapabilities(train);
	  	
		if (getDebug()) System.out.print("-: Models: ");

		train = new Instances(train);
		//m_Classifiers = (MultilabelClassifier[]) AbstractClassifier.makeCopies(m_Classifier, m_NumIterations);
		m_Classifiers = MultilabelClassifier.makeCopies((MultilabelClassifier)m_Classifier, m_NumIterations);

		for(int i = 0; i < m_NumIterations; i++) {
			Random r = new Random(m_Seed+i);
			Instances bag = new Instances(train,0);
			if (m_Classifiers[i] instanceof Randomizable) ((Randomizable)m_Classifiers[i]).setSeed(m_Seed+i);
			if(getDebug()) System.out.print(""+i+" ");

			int ixs[] = new int[train.numInstances()];
			for(int j = 0; j < ixs.length; j++) {
				ixs[r.nextInt(ixs.length)]++;
			}
			for(int j = 0; j < ixs.length; j++) {
				if (ixs[j] > 0) {
					Instance instance = train.instance(j);
					instance.setWeight(ixs[j]);
					bag.add(instance);
				}
			}

			m_Classifiers[i].buildClassifier(bag);
		}
		if (getDebug()) System.out.println(":-");
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new BaggingML(),args);
	}

}
