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

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MLUtils;
import weka.core.RevisionUtils;

/**
 * RT.java - Use a multi-class classifier as a multi-label classifier by <i>Ranking</i> outputs and using a <i>Threshold</i>.
 * <br>
 * See also from the <a href=http://mulan.sourceforge.net>MULAN</a> framework.
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class RT extends MultilabelClassifier {

	/** for serialization. */
	private static final long serialVersionUID = 7348139531854838421L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"Use a multi-class classifier as a multi-label classifier by 'Ranking' outputs and using a 'Threshold'.\n"
				+ "See also MULAN framework:\n"
				+ "http://mulan.sourceforge.net";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		int L = D.classIndex();

		//Create header
		Instances D_ = new Instances(D,0,0);

		//Delete the old class attributes
		for (int j = 0; j < L; j++)
			D_.deleteAttributeAt(0); 

		//Make the new class attribute
		FastVector classes = new FastVector(L);
		for (int j = 0; j < L; j++)
			classes.addElement(MLUtils.toBitString(j,L)); //(*) e.g. 00100 where j,N == 2,5

		//Add the new class attribute
		D_.insertAttributeAt(new Attribute("class",classes),0);
		D_.setClassIndex(0);

		//Loop through D again
		for (int i = 0; i < D.numInstances(); i++) {
			for (int j = 0; j < L; j++) {
				if((int)D.instance(i).value(j) > 0) {
					Instance inew = (Instance)D.instance(i).copy();
					inew.setDataset(null);
					for (int k = 1; k < L; k++)
						inew.deleteAttributeAt(1); 
					inew.setDataset(D_);
					inew.setClassValue(j); // (*) this just ponts to the right index
					D_.add(inew);
				}
			}
		}

		//Save the template
		m_InstancesTemplate = new Instances(D_,0);

		//Build
		if(getDebug())  System.out.println("Building classifier "+m_Classifier.getClass().getName()+" with "+D_.numInstances()+" instances");
		m_Classifier.buildClassifier(D_);

	}

	/**
	 * ConvertInstance - Convert an Instance to multi-class format by deleting all but one of the label attributes.
	 * @param	test	incoming Instance
	 * @return	the converted Instance
	 */
	public Instance convertInstance(Instance test) {

		int L = test.classIndex();

		//Copy the original instance
		Instance real = (Instance) test.copy(); 
		real.setDataset(null);

		//Delete all class attributes
		for (int i = 0; i < L; i++)
			real.deleteAttributeAt(0);

		//Add one of those class attributes at the begginning
		real.insertAttributeAt(0);

		//Hopefully setting the dataset will configure that attribute properly
		real.setDataset(m_InstancesTemplate);

		return real;
	}

	@Override
	public double[] distributionForInstance(Instance test) throws Exception {
		return m_Classifier.distributionForInstance(convertInstance(test));
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new RT(),args);
	}
}
