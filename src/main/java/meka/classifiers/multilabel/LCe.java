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
 * LCe.java - The LC (Label Combination) aka LP (Laber Powerset) Method.
 * Treats each label combination as a single class in a multi-class learning scheme. The set of possible values of each class is the powerset of labels.
 * NOTE: Rewritten from LC, since LC was behaving strangely under different versions of java. Replace LC.java with this one when ready.
 * See also <i>LP</i> from the <a href=http://mulan.sourceforge.net>MULAN</a> framework.
 * @see 	meka.classifiers.multilabel.LC
 * @version June 2014
 * @author 	Jesse Read
 */
public class LCe extends MultilabelClassifier implements OptionHandler {

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

		if(getDebug()) System.out.print("Converting Instances ...");
		Instances D_ = convertInstances(D,L);

		// build classifier on new dataset
		// Info
		info = "K = " + m_InstancesTemplate.attribute(0).numValues() + ", N = "+D_.numInstances();
		if(getDebug()) System.out.print("Building Classifier ("+info+"), ...");
		m_Classifier.buildClassifier(D_);
		if(getDebug()) System.out.println("Done");

	}

	public Instances convertInstances(Instances D, int L) throws Exception {

		// Gather combinations
		Set<LabelSet> distinctCombinations = PSUtils.countCombinationsSparse(D,L).keySet();

		// Create class attribute
		FastVector ClassValues = new FastVector(L);
		for(LabelSet y : distinctCombinations)
			ClassValues.addElement(y.toString());
		Attribute NewClass = new Attribute("Class", ClassValues);

		// Filter Remove all class attributes
		Remove FilterRemove = new Remove();
		FilterRemove.setAttributeIndices("1-"+L);
		FilterRemove.setInputFormat(D);
		Instances D_ = Filter.useFilter(D, FilterRemove);

		// Insert new special attribute (which has all possible combinations of labels) 
		D_.insertAttributeAt(NewClass,0);
		D_.setClassIndex(0);

		// Add class values
		for (int i = 0; i < D_.numInstances(); i++) {
			LabelSet y_i = new LabelSet(MLUtils.toSparseIntArray(D.instance(i),L));
			D_.instance(i).setClassValue(y_i.toString());
		}

		m_InstancesTemplate = new Instances(D_, 0);

		return D_;

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

	/**
	 * Convert Distribution - Given the posterior across combinations, return the distribution across labels.
	 * @param	p[]	the posterior of the super classes (combinations), e.g., P([1,3],[2]) = [1,0]
	 * @param	L 	the number of labels
	 * @return	the distribution across labels, e.g., P(1,2,3) = [1,0,1]
	 */
	public double[] convertDistribution(double p[], int L) {
		
		double y[] = new double[L];

		int i = Utils.maxIndex(p);

		double d[] = toDoubleArray(m_InstancesTemplate.classAttribute().value(i),L);
		for(int j = 0; j < d.length; j++) {
			if(d[j] > 0.0)
				y[j] = 1.0;
		}

		return y;
	}

	protected static final double[] toDoubleArray(String labelSet, int L) {

		int set[] = (labelSet.length() <= 2) ? new int[]{} : MLUtils.toIntArray(labelSet);
		//StringBuffer y = new StringBuffer(L);
		double y[] = new double[L];
		//for(int j = 0; j < L; j++) {
		//	y.append("0");
		//}
		for(int j : set) {
			//y.setCharAt(j,'1');
			y[j] = 1.;
		}
		return y;
		//return y.toString();
	}


	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		//if there is only one class (as for e.g. in some hier. mtds) predict it
		if(L == 1) return new double[]{1.0};

		Instance slInstance = convertInstance(x,L);
		slInstance.setDataset(m_InstancesTemplate);

		//Get a classification
		double y[] = new double[slInstance.numClasses()];

		y[(int)m_Classifier.classifyInstance(slInstance)] = 1.0;

		return convertDistribution(y,L);
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
		MultilabelClassifier.evaluation(new LCe(),args);
	}

}
