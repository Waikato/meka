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

package weka.classifiers.multitarget;

/**
 * CR. The Class-Relevance Method.
 * (The generalised, multi-target version of the Binary Relevance (BR) method).
 * @see		BR.java
 * @version	Jan 2012
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 */
import weka.classifiers.AbstractClassifier;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MLUtils;
import weka.core.Utils;

public class CR extends weka.classifiers.multilabel.BR implements MultiTargetClassifier {

	/** for serialization. */
	private static final long serialVersionUID = 1627371180786293843L;
	
	protected Instances m_Templates[] = null; // TEMPLATES

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"The Class-Relevance Method.\n"
				+ "(The generalised, multi-target version of the Binary Relevance (BR) method).";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();

		if(getDebug()) System.out.print("Creating "+L+" models ("+m_Classifier.getClass().getName()+"): ");
		m_MultiClassifiers = AbstractClassifier.makeCopies(m_Classifier,L);
		m_Templates = new Instances[L];

		for(int j = 0; j < L; j++) {

			//Select only class attribute 'j'
			m_Templates[j] = MLUtils.keepAttributesAt(new Instances(D),new int[]{j},L);
			m_Templates[j].setClassIndex(0);

			//Build the classifier for that class
			m_MultiClassifiers[j].buildClassifier(m_Templates[j]);
			if(getDebug()) System.out.print(" " + (m_Templates[j].classAttribute().name()));

			m_Templates[j] = new Instances(m_Templates[j], 0);
		}
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex(); 

		double y[] = new double[L*2];

		for (int j = 0; j < L; j++) {
			Instance x_j = (Instance)x.copy();
			x_j.setDataset(null);
			x_j = MLUtils.keepAttributesAt(x_j,new int[]{j},L);
			x_j.setDataset(m_Templates[j]);
			double w[] = m_MultiClassifiers[j].distributionForInstance(x_j); // e.g. [0.1, 0.8, 0.1]
			y[j] = Utils.maxIndex(w);									     // e.g. 1
			y[L+j] = w[(int)y[j]];											 // e.g. 0.8
		}

		return y;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CR(),args);
	}

}
