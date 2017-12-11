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

import meka.classifiers.incremental.IncrementalEvaluation;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializedObject;

/**
 *  A Multilabel Classifier.
 * 	@author Jesse Read
 *  @version Jan 2015
 */

public abstract class AbstractMultiLabelClassifier extends AbstractClassifier implements MultiLabelClassifier {

	/** for serialization. */
	private static final long serialVersionUID = 1713843369736127215L;
	
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

	/**
	 * Returns a string representation of the model.
	 *
	 * @return      the model
	 */
	public String getModel() {
		return "";
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
	public abstract void buildClassifier(Instances trainingSet) throws Exception;

	@Override
	public abstract double[] distributionForInstance(Instance i) throws Exception;

	/**
	 * Creates a given number of deep copies of the given multi-label classifier using serialization.
	 *
	 * @param model the classifier to copy
	 * @param num the number of classifier copies to create.
	 * @return an array of classifiers.
	 * @exception Exception if an error occurs
	 */
	public static MultiLabelClassifier[] makeCopies(MultiLabelClassifier model, int num) throws Exception {

		if (model == null) {
			throw new Exception("No model classifier set");
		}
		MultiLabelClassifier classifiers[] = new MultiLabelClassifier[num];
		SerializedObject so = new SerializedObject(model);
		for(int i = 0; i < classifiers.length; i++) {
			classifiers[i] = (MultiLabelClassifier) so.getObject();
		}
		return classifiers;
	}

	/**
	 * Called by classifier's main() method upon initialisation from the command line. 
	 * TODO: In the future Use runClassifier(h,args) directly, and depreciated this function.
	 * @param	h		A classifier
	 * @param	args	Command-line options.
	 */
	public static void evaluation(MultiLabelClassifier h, String args[]) {
		runClassifier(h,args);
	}

	/**
	 * Called by classifier's main() method upon initialisation from the command line. 
	 * @param	h		A classifier
	 * @param	args	Command-line options.
	 */
	public static void runClassifier(MultiLabelClassifier h, String args[]) {
			if (h instanceof UpdateableClassifier) {
				try {
					IncrementalEvaluation.runExperiment(h,args);
				} catch(Exception e) {
					System.err.println("\n"+e);
					//e.printStackTrace();
					IncrementalEvaluation.printOptions(h.listOptions());
				}
			}
			else {
				try {
					Evaluation.runExperiment(h,args);
				} catch(Exception e) {
					System.err.println("\n"+e);
					//e.printStackTrace();
					Evaluation.printOptions(h.listOptions());
				}
			}
	}

}
