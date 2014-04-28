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
import java.util.Iterator;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import meka.core.MLUtils;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * LC.java - The LC (Label Combination) aka LP (Laber Powerset) Method.
 * Treats each label combination as a single class in a multi-class learning scheme. The set of possible values of each class is the powerset of labels.
 * See also <i>LP</i> from the <a href=http://mulan.sourceforge.net>MULAN</a> framework.
 * @see 	meka.classifiers.multilabel.LC
 * @version January 2009
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
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

		//Create a nominal class attribute of all (existing) possible combinations of labels as possible values
		FastVector ClassValues = new FastVector(L);
		HashSet<String> UniqueValues = new HashSet<String>();
		for (int i = 0; i < D.numInstances(); i++) {
			UniqueValues.add(MLUtils.toBitString(D.instance(i),L));
		}
		Iterator<String> it = UniqueValues.iterator();
		while (it.hasNext()) {
			ClassValues.addElement(it.next());
		}
		Attribute Y_new = new Attribute("Class", ClassValues);

		//Filter Remove all class attributes
		Remove FilterRemove = new Remove();
		FilterRemove.setAttributeIndices("1-"+L);
		FilterRemove.setInputFormat(D);
		Instances NewTrain = Filter.useFilter(D, FilterRemove);

		//Insert new special attribute (which has all possible combinations of labels) 
		NewTrain.insertAttributeAt(Y_new, 0);
		NewTrain.setClassIndex(0);

		//Add class values
		for (int i = 0; i < NewTrain.numInstances(); i++) {
			String comb = MLUtils.toBitString(D.instance(i),L);
			NewTrain.instance(i).setClassValue(comb);
		}

		// keep the header of new dataset for classification
		m_InstancesTemplate = new Instances(NewTrain, 0);

		// build classifier on new dataset
		// Info
		if(getDebug()) System.out.println("("+m_InstancesTemplate.attribute(0).numValues()+" classes, "+NewTrain.numInstances()+" ins. )");
		if(getDebug()) System.out.print("Building Classifier "+m_Classifier.getClass()+" with "+ClassValues.size()+" possible classes .. ");
		m_Classifier.buildClassifier(NewTrain);
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

	/**
	 * Convert Distribution - Given the posterior across combinations, return the distribution across labels.
	 * @param	p[]	the posterior of the super classes (combinations), e.g., P([1,3],[2]) = [1,0]
	 * @param	L 	the number of labels
	 * @return	the distribution across labels, e.g., P(1,2,3) = [1,0,1]
	 */
	public double[] convertDistribution(double p[], int L) {
		
		double y[] = new double[L];

		int i = Utils.maxIndex(p);

		double d[] = MLUtils.fromBitString(m_InstancesTemplate.classAttribute().value(i));
		for(int j = 0; j < d.length; j++) {
			if(d[j] > 0.0)
				y[j] = 1.0;
		}

		return y;
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

	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new LC(),args);
	}

}
