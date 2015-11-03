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

import java.util.Random;

import meka.classifiers.multilabel.ProblemTransformationMethod;
import weka.core.Instances;
import weka.core.Randomizable;
import weka.core.RevisionUtils;

/**
 * EnsembleML.java - Combines several multi-label classifiers in a simple-subset ensemble.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */

public class EnsembleML extends MetaProblemTransformationMethod {

	/** for serialization. */
	private static final long serialVersionUID = 835659467275068411L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "Combining several multi-label classifiers in a simple-subset ensemble.";
	}

	@Override
	public void buildClassifier(Instances train) throws Exception {
	  	testCapabilities(train);
	  	
		if (getDebug()) System.out.print("-: Models: ");

		train = new Instances(train);
		m_Classifiers = ProblemTransformationMethod.makeCopies((ProblemTransformationMethod) m_Classifier, m_NumIterations);
		int sub_size = (train.numInstances()*m_BagSizePercent/100);
		for(int i = 0; i < m_NumIterations; i++) {
			if(getDebug()) System.out.print(""+i+" ");
			if (m_Classifiers[i] instanceof Randomizable) ((Randomizable)m_Classifiers[i]).setSeed(i);
			train.randomize(new Random(m_Seed+i));
			Instances sub_train = new Instances(train,0,sub_size);
			m_Classifiers[i].buildClassifier(sub_train);
		}

		if (getDebug()) System.out.println(":-");
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new EnsembleML(), args);
	}

}
