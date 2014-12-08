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

import weka.core.Instance;
import meka.core.PSUtils;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

/**
 * PSt.java -  Pruned Sets with a a threshold so as to be able to predict sets not seen in the training set.
 * <br>
 * See: Jesse Read. <i>A Pruned Problem Transformation Method for Multi-label Classification</i>. In Proc. of the NZ Computer Science Research Student Conference. Christchurch, New Zealand (2008).
 * @see PS
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class PSt extends PS implements TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = -792705184263116856L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"Pruned Sets with a a threshold so as to be able to predict sets not seen in the training set."
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		
		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Jesse Read");
		result.setValue(Field.TITLE, "A Pruned Problem Transformation Method for Multi-label Classification");
		result.setValue(Field.BOOKTITLE, "NZ Computer Science Research Student Conference. Christchurch, New Zealand");
		result.setValue(Field.YEAR, "2008");
		
		return result;
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		// if there is only one class (as for e.g. in some hier. mtds) predict it
		if(L == 1) return new double[]{1.0};

		Instance x_ = PSUtils.convertInstance(x,L,m_InstancesTemplate); //convertInstance(x,L);
		//x_.setDataset(m_InstancesTemplate);

		// Get a classification
		return PSUtils.recombination_t(m_Classifier.distributionForInstance(x_),L,m_InstancesTemplate);
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new PSt(),args);
	}

}
