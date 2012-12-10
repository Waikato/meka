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
import weka.core.Instances;
import weka.core.Randomizable;
import weka.core.RevisionUtils;

/**
 * BaggingMLdup.java - A version of BaggingML where Instances are duplicated instead of assigned higher weighs.
 * Duplicates Instances instead of assigning higher weights -- should work for methods that do not handle weights at all.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class BaggingMLdup extends MultilabelMetaClassifier {

	/** for serialization. */
	private static final long serialVersionUID = -5606278379913020097L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"Combining several multi-label classifiers using Bootstrap AGGregatING.\n"
				+ "Duplicates Instances instead of assigning higher weights -- should work for methods that do not handle weights at all.";
	}
	
	@Override
	public void buildClassifier(Instances train) throws Exception {
	  	testCapabilities(train);
	  	
		if (getDebug()) System.out.print("-: Models: ");

		//m_Classifiers = (MultilabelClassifier[]) AbstractClassifier.makeCopies(m_Classifier, m_NumIterations);
		m_Classifiers = MultilabelClassifier.makeCopies((MultilabelClassifier)m_Classifier, m_NumIterations);

		for(int i = 0; i < m_NumIterations; i++) {
			Random r = new Random(m_Seed+i);
			Instances bag = new Instances(train,0);
			if (m_Classifiers[i] instanceof Randomizable) ((Randomizable)m_Classifiers[i]).setSeed(m_Seed+i);
			if(getDebug()) System.out.print(""+i+" ");

			int bag_no = (m_BagSizePercent*train.numInstances()/100);
			//System.out.println(" bag no: "+bag_no);
			while(bag.numInstances() < bag_no) {
				bag.add(train.instance(r.nextInt(train.numInstances())));
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
		MultilabelClassifier.evaluation(new BaggingMLdup(),args);
	}

}
