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

import meka.core.*;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.*;

/**
 * RAkELd - RAndom partition of labELs; like RAkEL but labelsets are disjoint / 
 * non-overlapping.
 *
 * As in <code>RAkEL</code>, <code>k</code> still indicates the size of 
 * partitions, however, note that anything more than L/2 (for L labels) just 
 * causes the classifier to default to <code>PS</code>, because it is not 
 * possible form more than one labelset of size L/2 from L labels.
 *
 * For example, for 6 labels, a possibility where k=2 are the labelsets  
 * <code>[1,3,4]</code> and <code>[0,2,5]</code> (indices 0,...,5). 
 * 
 * Note that the number of partitions (in this case, 2) is interpreted 
 * automatically (unlike in <code>RAkEL</code> where this (the 
 * <code>M</code> parameter is open). 
 *
 * @see		RAkEL
 * @author 	Jesse Read 
 * @version September 2015
 */
public class RAkELd extends PS implements TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = -6208388889440497990L;

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
		return "Takes RAndom partition of labELs; like RAkEL but labelsets are disjoint / non-overlapping subsets.";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();
		int N = D.numInstances();
		Random random = new Random(m_S);

		// Note: a slightly roundabout way of doing it:
		int num = (int)Math.ceil(L / m_K);
		kMap = SuperLabelUtils.generatePartition(A.make_sequence(L),num,random,true);
		m_M = kMap.length;
		m_Classifiers = AbstractClassifier.makeCopies(m_Classifier,m_M);
		m_InstancesTemplates = new Instances[m_M];

		if (getDebug())
			System.out.println("Building "+m_M+" models of "+m_K+" partitions:");

		for(int i = 0; i < m_M; i++) {

			if (getDebug()) 
				System.out.println("\tpartitioning model "+(i+1)+"/"+m_M+": "+Arrays.toString(kMap[i])+", P="+m_P+", N="+m_N);
			Instances D_i = SuperLabelUtils.makePartitionDataset(D,kMap[i],m_P,m_N);
			if (getDebug()) 
				System.out.println("\tbuilding model "+(i+1)+"/"+m_M+": "+Arrays.toString(kMap[i]));

			m_Classifiers[i].buildClassifier(D_i);
			m_InstancesTemplates[i] = new Instances(D_i,0);

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
			Instance x_m = PSUtils.convertInstance(x, L, m_InstancesTemplates[m]);
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
	 * mapBack: returns the original indices (encoded in the class attribute).
	 */
	private int[] mapBack(Instances template, int i) {
		try {
			return MLUtils.toIntArray(template.classAttribute().value(i));
		} catch(Exception e) {
			return new int[]{};
		}
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
	@Override
	public Map<Integer,String> graph() throws Exception {
		Map<Integer,String>		result;

		result = new HashMap<Integer,String>();

		for (int i = 0; i < m_Classifiers.length; i++) {
			if (m_Classifiers[i] != null) {
				if (m_Classifiers[i] instanceof Drawable) {
					result.put(i, ((Drawable) m_Classifiers[i]).graph());
				}
			}
		}

		return result;
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

	/**
	 * Get the k parameter (the size of each partition).
	 */
	public int getK() {
		return m_K;
	}

	/**
	 * Sets the k parameter (the size of each partition)
	 */
	public void setK(int k) {
		m_K = k;
	}

	public String kTipText() {
		return "The number of labels in each partition -- should be 1 <= k < (L/2) where L is the total number of labels.";
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\t"+kTipText(), "k", 1, "-k <num>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setK(OptionUtils.parse(options, 'k', 3));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'k', getK());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		TechnicalInformation	additional;
		
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Grigorios Tsoumakas, Ioannis Katakis, Ioannis Vlahavas");
		result.setValue(Field.TITLE, "Random k-Labelsets for Multi-Label Classification");
		result.setValue(Field.JOURNAL, "IEEE Transactions on Knowledge and Data Engineering");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.VOLUME, "23");
		result.setValue(Field.NUMBER, "7");
		result.setValue(Field.PAGES, "1079--1089");
		
		additional = new TechnicalInformation(Type.INPROCEEDINGS);
		additional.setValue(Field.AUTHOR, "Jesse Read, Antti Puurula, Albert Bifet");
		additional.setValue(Field.TITLE, "Classifier Chains for Multi-label Classification");
		result.setValue(Field.BOOKTITLE, "ICDM'14: International Conference on Data Mining (ICDM 2014). Shenzen, China.");
		result.setValue(Field.PAGES, "941--946");
		result.setValue(Field.YEAR, "2014");

		result.add(additional);
    
		return result;
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new RAkELd(), args);
	}

}
