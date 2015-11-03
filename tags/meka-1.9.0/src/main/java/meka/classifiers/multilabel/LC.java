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

import java.util.HashMap;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Drawable;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;

import meka.core.MultiLabelDrawable;
import meka.core.PSUtils;

/**
 * LC.java - The LC (Label Combination) aka LP (Laber Powerset) Method.
 * Treats each label combination as a single class in a multi-class learning scheme. The set of possible values of each class is the powerset of labels.
 * This code was rewritten at some point.
 * See also <i>LP</i> from the <a href=http://mulan.sourceforge.net>MULAN</a> framework.
 * @version June 2014
 * @author 	Jesse Read
 */
public class LC extends ProblemTransformationMethod implements OptionHandler, MultiLabelDrawable {

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

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		//if there is only one class (as for e.g. in some hier. mtds) predict it
		if(L == 1) return new double[]{1.0};

		Instance x_ = PSUtils.convertInstance(x,L,m_InstancesTemplate); //convertInstance(x,L);
		x_.setDataset(m_InstancesTemplate);

		//Get a classification
		double y[] = new double[x_.numClasses()];

		y[(int)m_Classifier.classifyInstance(x_)] = 1.0;

		return PSUtils.convertDistribution(y,L,m_InstancesTemplate);
	}

	/**
	 * Returns the type of graph representing
	 * the object.
	 *
	 * @return the type of graph representing the object (label index as key)
	 */
	public Map<Integer,Integer> graphType() {
		Map<Integer,Integer>	result;

		result = new HashMap<Integer,Integer>();

		if (getClassifier() != null) {
			if (getClassifier() instanceof Drawable) {
				result.put(0, ((Drawable) getClassifier()).graphType());
			}
		}

		return result;
	}

	/**
	 * Returns a string that describes a graph representing
	 * the object. The string should be in XMLBIF ver.
	 * 0.3 format if the graph is a BayesNet, otherwise
	 * it should be in dotty format.
	 *
	 * @return the graph described by a string (label index as key)
	 * @throws Exception if the graph can't be computed
	 */
	public Map<Integer,String> graph() throws Exception {
		Map<Integer,String>		result;

		result = new HashMap<Integer,String>();

		if (getClassifier() != null) {
			if (getClassifier() instanceof Drawable) {
				result.put(0, ((Drawable) getClassifier()).graph());
			}
		}

		return result;
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
		ProblemTransformationMethod.evaluation(new LC(), args);
	}

}
