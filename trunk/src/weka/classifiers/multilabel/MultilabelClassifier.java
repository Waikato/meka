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
import weka.core.Instance;
import weka.core.Instances;

/**
 *  Multilabel Classifier.
 *  All problem transformation methods need a base (single-label) classifier supplied.
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

	public abstract void buildClassifier(Instances trainingSet) throws Exception;

	@Override
	public abstract double[] distributionForInstance(Instance i) throws Exception;

	// The new version of WEKA insists on this
	@Override
	public String getRevision() {
		return null;
	}

	// to be deprecated
	public static void evaluation(MultilabelClassifier h, String args[]) {
		runClassifier(h,args);
	}

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
