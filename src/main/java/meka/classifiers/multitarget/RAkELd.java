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

package meka.classifiers.multitarget;

import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.core.*;
import meka.filters.multilabel.SuperNodeFilter;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.*;

import java.util.*;

/**
 * RAkELd - Multi-target Version of RAkELd.
 * Extends NSR just like the multi-label version extends PS.
 * @see		meka.classifiers.multilabel.RAkELd
 * @author 	Jesse Read 
 * @version June 2016
 */
public class RAkELd extends NSR {

	/** for serialization. */
	private static final long serialVersionUID = -6208388889440497990L;

	protected Classifier m_Classifiers[] = null;
	protected Instances m_InstancesTemplates[] = null;
	int m_K = 3;
	int m_M = 10;
	protected int kMap[][] = null;
	protected int vMap[][][] = null; // TODO use this to speed things up


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
		/*
		NOTE: This is a slow way of doing things at the moment, making use of multitarget.SCC functionality,
		even though multilabel.RAkELd is not a meta multi-label classifier.
		 */

		int L = D.classIndex();
		int N = D.numInstances();
		Random r = new Random(m_S);

		// Note: a slightly round-about way of doing it:
		int num = (int)Math.ceil(L / m_K);
		kMap = SuperLabelUtils.generatePartition(A.make_sequence(L),num,r,true);
		m_M = kMap.length;
		vMap = new int[m_M][][];
		m_Classifiers = AbstractClassifier.makeCopies(m_Classifier,m_M);
		m_InstancesTemplates = new Instances[m_M];

		if (getDebug())
			System.out.println("Building "+m_M+" models of "+m_K+" partitions:");

		D = SuperLabelUtils.SLTransformation(D, kMap, m_P, m_N);

		for(int i = 0; i < m_M; i++) {

			/*
			if (getDebug()) 
				System.out.println("\tpartitioning model "+(i+1)+"/"+m_M+": "+Arrays.toString(kMap[i])+", P="+m_P+", N="+m_N);

			Instances D_i = SuperLabelUtils.makePartitionDataset(D,kMap[i],m_P,m_N);
			*/

			Instances D_i = F.keepLabels(D,D.classIndex(),new int[]{i});
			D_i.setClassIndex(0);

			//vMap[i] = SuperLabelUtils.extractValues(D_i);

			if (getDebug()) 
				System.out.println("\tbuilding model "+(i+1)+"/"+m_M+": "+Arrays.toString(kMap[i]));

			m_Classifiers[i].buildClassifier(D_i);
			m_InstancesTemplates[i] = new Instances(D_i,0);

		}

	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		HashMap<Integer,Double> votes[] = new HashMap[L];
		for(int j = 0; j < L; j++) {
			votes[j] = new HashMap<Integer,Double>();
		}

		for(int m = 0; m < m_M; m++) {

			// Transform instance
			Instance x_m = PSUtils.convertInstance(x, L, m_InstancesTemplates[m]);
			x_m.setDataset(m_InstancesTemplates[m]);

			// Get a meta classification
			int yp_j = (int)m_Classifiers[m].classifyInstance(x_m);        // e.g., 2

			int values[] = SuperLabelUtils.decodeValue(m_InstancesTemplates[m].classAttribute().value(yp_j));

			int k_indices[] = SuperLabelUtils.decodeClass(m_InstancesTemplates[m].classAttribute().name());

			// Vote with classification
            for(int j_k = 0; j_k < k_indices.length; j_k++) {
				//int i = k_indices[j_k];			// original indices
				int j = kMap[m][j_k];				// original indices
				Double score = votes[j].get(values[j_k]);
				votes[j].put(values[j_k],(score == null) ? 1. : score + 1.);
			}

		}

		double y[] = SuperLabelUtils.convertVotesToDistributionForInstance(votes);

		return y;
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
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new RAkELd(), args);
	}

}
