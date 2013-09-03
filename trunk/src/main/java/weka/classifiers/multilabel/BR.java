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
 * BR.java - The Binary Relevance Method.
 * The standard baseline Binary Relevance method (BR) -- create a binary problems for each label and learn a model for them individually.
 * See also <i>BR</i> from the <a href=http://mulan.sourceforge.net>MULAN</a> framework
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import meka.core.MLUtils;
import weka.core.RevisionUtils;

public class BR extends MultilabelClassifier {

	/** for serialization. */
	private static final long serialVersionUID = -5390512540469007904L;
	
	protected Classifier m_MultiClassifiers[] = null;
	protected Instances m_InstancesTemplates[] = null; 

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"The Binary Relevance Method.\n"
				+ "See also MULAN framework:\n"
				+ "http://mulan.sourceforge.net";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);
	  	
		int L = D.classIndex();

		if(getDebug()) System.out.print("Creating "+L+" models ("+m_Classifier.getClass().getName()+"): ");
		m_MultiClassifiers = AbstractClassifier.makeCopies(m_Classifier,L);
		m_InstancesTemplates = new Instances[L];

		for(int j = 0; j < L; j++) {

			//Select only class attribute 'j'
			Instances D_j = MLUtils.keepAttributesAt(new Instances(D),new int[]{j},L);
			D_j.setClassIndex(0);

			//Build the classifier for that class
			m_MultiClassifiers[j].buildClassifier(D_j);
			if(getDebug()) System.out.print(" " + (D_j.classAttribute().name()));

			m_InstancesTemplates[j] = new Instances(D_j, 0);
		}
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex(); 

		double y[] = new double[L];

		for (int j = 0; j < L; j++) {
			Instance x_j = (Instance)x.copy();
			x_j.setDataset(null);
			x_j = MLUtils.keepAttributesAt(x_j,new int[]{j},L);
			x_j.setDataset(m_InstancesTemplates[j]);
			//y[j] = m_MultiClassifiers[j].classifyInstance(x_j);
			y[j] = m_MultiClassifiers[j].distributionForInstance(x_j)[1];
		}

		return y;
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new BR(),args);
	}

}
