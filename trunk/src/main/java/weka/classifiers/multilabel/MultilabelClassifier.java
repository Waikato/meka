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

import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.UpdateableClassifier;
import weka.core.WindowIncrementalEvaluator;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;

/**
 *  MultilabelClassifier.java - A Multilabel Classifier.
 * 	@author Jesse Read (jmr30@cs.waikato.ac.nz)
 */

public abstract class MultilabelClassifier extends SingleClassifierEnhancer {

	/** for serialization. */
	private static final long serialVersionUID = 1713843369766127169L;
	
	/** A Template for Problem Transformations */
	protected Instances m_InstancesTemplate;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	public String globalInfo() {
		// highly recommended to overwrite this method!
		return "A multi-label classifier";
	}

	@Override
	public String toString() {
		return "";
	}

	public Instances getTemplate() {
		return m_InstancesTemplate;
	}

	/**
	 * TestCapabilities.
	 * Make sure the training data is suitable.
	 * @param D	the data
	 */
	public void testCapabilities(Instances D) throws Exception {
		// get the classifier's capabilities, enable all class attributes and do the usual test
		Capabilities cap = getCapabilities();
		cap.enableAllClasses();
		//getCapabilities().testWithFail(D);
		// get the capabilities again, test class attributes individually
		int L = D.classIndex();
		for(int j = 0; j < L; j++) {
			Attribute c = D.attribute(j);
			cap.testWithFail(c,true);
		}
	}

	@Override
	public Capabilities getCapabilities() {
	  Capabilities	result;
	  
	  result = super.getCapabilities();
	  
	  //result.enable(Capability.NUMERIC_CLASS);
	  result.disable(Capability.NUMERIC_CLASS);
	  result.disable(Capability.DATE_CLASS);
	  result.disable(Capability.STRING_CLASS);
	  result.disable(Capability.RELATIONAL_CLASS);
	  
	  return result;
	}

	@Override
	public abstract void buildClassifier(Instances trainingSet) throws Exception;

	@Override
	public abstract double[] distributionForInstance(Instance i) throws Exception;

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	/**
	 * Evaluation. 
	 * Use runClassifier(MultilabelClassifier,String[]) instead.
	 */
	@Deprecated
	public static void evaluation(MultilabelClassifier h, String args[]) {
		runClassifier(h,args);
	}

	/**
	 * Creates a given number of deep copies of the given multi-label classifier using serialization.
	 *
	 * @param model the classifier to copy
	 * @param num the number of classifier copies to create.
	 * @return an array of classifiers.
	 * @exception Exception if an error occurs
	 */
	public static MultilabelClassifier[] makeCopies(MultilabelClassifier model, int num) throws Exception {

		if (model == null) {
			throw new Exception("No model classifier set");
		}
		MultilabelClassifier classifiers[] = new MultilabelClassifier[num];
		SerializedObject so = new SerializedObject(model);
		for(int i = 0; i < classifiers.length; i++) {
			classifiers[i] = (MultilabelClassifier) so.getObject();
		}
		return classifiers;
	}

	/**
	 * runClassifier. 
	 * Called by classifier's main() method upon initialisation from the command line. 
	 * @param	h		A classifier
	 * @param	args	Command-line options.
	 */
	public static void runClassifier(MultilabelClassifier h, String args[]) {
		try {
			if (h instanceof UpdateableClassifier) 
				WindowIncrementalEvaluator.evaluation(h,args);
			else
				Evaluation.runExperiment(h,args);
		} catch(Exception e) {
			System.err.println("\n"+e);
			//e.printStackTrace();
			Evaluation.printOptions(h.listOptions());
		}
	}

}
