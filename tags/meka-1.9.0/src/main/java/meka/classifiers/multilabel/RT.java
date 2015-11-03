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

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;

/**
 * RT.java - The 'Ranking + Threshold' classifier. 
 * Duplicates each multi-labelled example, and assigns one of the labels (only) to each copy; then trains a regular multi-class base classifier.
 * At test time, a <i>threshold</i> separates relevant from irrelevant labels using the posterior for each class value (i.e., label).
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 * @version 2010
 */
public class RT extends ProblemTransformationMethod {

	/** for serialization. */
	private static final long serialVersionUID = 7348139531854838421L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "Duplicates each multi-labelled example, and assigns one of the labels (only) to each copy; then trains a regular multi-class base classifier.\n"+
				 "At test time, a threshold separates relevant from irrelevant labels using the posterior for each class value (i.e., label).";
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
			classes.addElement("C"+j);

		//Add the new class attribute
		D_.insertAttributeAt(new Attribute("ClassY",classes),0);
		D_.setClassIndex(0);

		//Loop through D again
		for (int i = 0; i < D.numInstances(); i++) {
			for (int j = 0; j < L; j++) {
				if((int)D.instance(i).value(j) > 0) {
					// make a copy here ...
					Instance x_ = (Instance)D.instance(i).copy();
					x_.setDataset(null);
					// make it multi-class, and set the appropriate class value ...
					for (int k = 1; k < L; k++)
						x_.deleteAttributeAt(1); 
					x_.setDataset(D_);
					x_.setClassValue(j); // (*) this just ponts to the right index
					D_.add(x_);
				}
			}
		}

		//Save the template
		m_InstancesTemplate = new Instances(D_,0);

		//Build
		if(getDebug())  System.out.println("Building classifier "+m_Classifier.getClass().getName()+" on "+D_.numInstances()+" instances (originally "+D.numInstances()+")");
		m_Classifier.buildClassifier(D_);

	}

	/**
	 * ConvertInstance - Convert an Instance to multi-class format by deleting all but one of the label attributes.
	 * @param	x	incoming Instance
	 * @return	the converted Instance
	 */
	public Instance convertInstance(Instance x) {

		int L = x.classIndex();

		//Copy the original instance
		Instance x_ = (Instance) x.copy(); 
		x_.setDataset(null);

		//Delete all class attributes
		for (int i = 0; i < L; i++)
			x_.deleteAttributeAt(0);

		//Add one of those class attributes at the begginning
		x_.insertAttributeAt(0);

		//Hopefully setting the dataset will configure that attribute properly
		x_.setDataset(m_InstancesTemplate);

		return x_;
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		return m_Classifier.distributionForInstance(convertInstance(x));
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new RT(), args);
	}
}
