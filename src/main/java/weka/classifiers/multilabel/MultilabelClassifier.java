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
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;

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
	public abstract String globalInfo();

	@Override
	public String toString() {
		return "";
	}

	public Instances getTemplate() {
		return m_InstancesTemplate;
	}
	
	@Override
	public Capabilities getCapabilities() {
	  Capabilities	result;
	  
	  result = super.getCapabilities();
	  
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
	 * To be deprecated, and replaced with runClassifier(h,args)
	 */
	public static void evaluation(MultilabelClassifier h, String args[]) {
		runClassifier(h,args);
	}

	/**
	 * runClassifier. 
	 * Called by classifier's main() method upon initialisation from the command line. 
	 * @param	h		A classifier
	 * @param	args	Command-line options.
	 */
	public static void runClassifier(MultilabelClassifier h, String args[]) {
		try {
			 Evaluation.runExperiment(h,args);
		} catch(Exception e) {
			System.err.println("\n"+e);
			//e.printStackTrace();
			Evaluation.printOptions(h.listOptions());
		}
	}

}
