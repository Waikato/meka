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

import java.util.HashSet;
import java.util.Set;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import meka.core.MLUtils;
import meka.core.PSUtils;
import meka.core.LabelSet;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * LC.java - The LC (Label Combination) aka LP (Laber Powerset) Method.
 * Treats each label combination as a single class in a multi-class learning scheme. The set of possible values of each class is the powerset of labels.
 * This code was rewritten at some point.
 * See also <i>LP</i> from the <a href=http://mulan.sourceforge.net>MULAN</a> framework.
 * @version June 2014
 * @author 	Jesse Read
 */
public class LC extends MultilabelClassifier implements OptionHandler {

	/** for serialization. */
	private static final long serialVersionUID = -2726090581435923988L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"LC aka LP (Laber Powerset) Method.\nTreats each label combination as a single class in a multi-class learning scheme. The set of possible values of each class is the powerset of labels.\n"
				+ "See also LP from MULAN:\n"
				+ "http://mulan.sourceforge.net";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		int L = D.classIndex();

		// Transform Instances
		if(getDebug()) System.out.print("Transforming Instances ...");
		Instances D_ = PSUtils.LCTransformation(D,L);
		m_InstancesTemplate = new Instances(D_,0);

		// Set Info ; Build Classifier
		info = "K = "+m_InstancesTemplate.attribute(0).numValues() + ", N = "+D_.numInstances();
		if(getDebug()) System.out.print("Building Classifier ("+info+"), ...");
		m_Classifier.buildClassifier(D_);
		if(getDebug()) System.out.println("Done");
	}

	/**
	 * Convert Instance - Convert e.g., [1,2,3] to [13,2]
	 * @param	x	original Instance (e.g., [1,2,3],x)
	 * @param	L 	the number of labels
	 * @return	converted Instance (e.g., [13,2],x)
	 */
	public Instance convertInstance(Instance x, int L) {
		Instance x_ = (Instance) x.copy(); 
		x_.setDataset(null);
		for (int i = 0; i < L; i++)
			x_.deleteAttributeAt(0);
		x_.insertAttributeAt(0);
		x_.setDataset(m_InstancesTemplate);
		return x_;
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		//if there is only one class (as for e.g. in some hier. mtds) predict it
		if(L == 1) return new double[]{1.0};

		Instance x_ = convertInstance(x,L);
		x_.setDataset(m_InstancesTemplate);

		//Get a classification
		double y[] = new double[x_.numClasses()];

		y[(int)m_Classifier.classifyInstance(x_)] = 1.0;

		return PSUtils.convertDistribution(y,L,m_InstancesTemplate);
	}

	private String info = "";

	public String toString() {
		return info;
	}

	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new LC(),args);
	}

}
