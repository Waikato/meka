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

import meka.core.MLUtils;
import meka.core.OptionUtils;
import meka.core.PSUtils;
import meka.core.SuperLabelUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.*;

/**
 * RAkEL.java - Draws M subsets of size k from the set of labels, and trains PS upon each one, then combines label votes from these PS classifiers to get a label-vector prediction. The original RAkEL by Tsoumakas et al. was a meta method, typically taking LC (aka LP) as a classifier; this implementation is more like a combination between RAkEL and PS, making it potentially very fast (recall that PS defaults to LC with 0 pruning).
 *
 * See also <i>RAkEL</i> from the <a href=http://mulan.sourceforge.net>MULAN</a> framework.
 * @author 	Jesse Read
 * @version June 2014
 */

public class RAkEL extends PS {

	/** for serialization. */
	private static final long serialVersionUID = -6208337124440497991L;

	protected Classifier m_Classifiers[] = null;
	protected Instances m_InstancesTemplates[] = null; 
	int m_K = 3;
	int m_M = 10;
	protected int kMap[][] = null;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "Draws M subsets of size k from the set of labels, and trains PS upon each one, then combines label votes from the PS classifiers to get a label-vector prediction.";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);

		int L = D.classIndex();
		Random r = new Random(m_S);


		if (getDebug())
			System.out.println("Building "+m_M+" models of "+m_K+" random subsets:");

		m_InstancesTemplates = new Instances[m_M];
		kMap = new int[m_M][m_K];
		m_Classifiers = AbstractClassifier.makeCopies(m_Classifier,m_M);
		for(int i = 0; i < m_M; i++) {
			kMap[i] = SuperLabelUtils.get_k_subset(L,m_K,r);
			if (getDebug()) 
				System.out.println("\tmodel "+(i+1)+"/"+m_M+": "+Arrays.toString(kMap[i])+", P="+m_P+", N="+m_N);
			Instances D_i = SuperLabelUtils.makePartitionDataset(D,kMap[i],m_P,m_N);
			m_Classifiers[i].buildClassifier(D_i);
			m_InstancesTemplates[i] = new Instances(D_i,0);
		}
	}

	private int[] mapBack(Instances template, int i) {
		try {
			return MLUtils.toIntArray(template.classAttribute().value(i));
		} catch(Exception e) {
			return new int[]{};
		}
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		// If there is only one label, predict it
		//if(L == 1) return new double[]{1.0};

		double y[] = new double[L];
		//int c[] = new int[L]; // to scale it between 0 and 1

		for(int m = 0; m < m_M; m++) {

			// Transform instance
			Instance x_m = PSUtils.convertInstance(x,L,m_InstancesTemplates[m]);
			x_m.setDataset(m_InstancesTemplates[m]);

			// Get a meta classification
			int i_m = (int)m_Classifiers[m].classifyInstance(x_m);        // e.g., 2
			int k_indices[] = mapBack(m_InstancesTemplates[m],i_m); // e.g., [3,8]

			// Vote with classification
			for (int i : k_indices) {
				int index = kMap[m][i];
				y[index] += 1.;
			}

		}

		return y;
	}

	/** 
	 * GetK - Get the k parameter (size of partitions).
	 */
	public int getK() {
		return m_K;
	}

	/** 
	 * SetP - Sets the k parameter (size of partitions)
	 */
	public void setK(int k) {
		m_K = k;
	}

	public String kTipText() {
		return "The number of labels in each subset (must be at least 1 and less than the number of labels) ";
	}

	/** 
	 * GetM - Get the M parameter (number of subsets).
	 */
	public int getM() {
		return m_M;
	}

	/** 
	 * SetM - Sets the M parameter (number of subsets)
	 */
	public void setM(int M) {
		m_M = M;
	}

	public String mTipText() {
		return "The number of subsets (to run in ensemble)";
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		TechnicalInformation	additional;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Grigorios Tsoumakas and Ioannis Katakis and Ioannis Vlahavas");
		result.setValue(Field.TITLE, "Random k-Labelsets for Multi-Label Classification");
		result.setValue(Field.JOURNAL, "IEEE Transactions on Knowledge and Data Engineering");
		result.setValue(Field.VOLUME, "99");
		result.setValue(Field.NUMBER, "1");
		result.setValue(Field.YEAR, "2010");

		additional = new TechnicalInformation(Type.INPROCEEDINGS);
		additional.setValue(Field.AUTHOR, "Jesse Read, Antti Puurula, Albert Bifet");
		additional.setValue(Field.TITLE, "Multi-label Classification with Meta-labels");
		additional.setValue(Field.BOOKTITLE, "International Conference on Data Mining");
		additional.setValue(Field.YEAR, "2014");

		result.add(additional);
    
		return result;
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tSets M (default 10): the number of subsets", "M", 1, "-M <num>"));
		result.addElement(new Option("\tSets k (default 3): the size of partitions.", "k", 1, "-k <num>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setK(OptionUtils.parse(options, 'k', 3));
		setM(OptionUtils.parse(options, 'M', 10));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'k', getK());
		OptionUtils.add(result, 'M', getM());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	@Override
	public String toString() {
		if (kMap == null)
			return "No model built yet";
		StringBuilder s = new StringBuilder("{");
		for(int k = 0; k < m_M; k++) {
			s.append(Arrays.toString(kMap[k]));
		}
		return s.append("}").toString();
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new RAkEL(), args);
	}

}

