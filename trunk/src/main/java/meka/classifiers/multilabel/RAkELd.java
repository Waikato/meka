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
import meka.core.SuperLabelUtils;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

import java.util.Arrays;
import java.util.Random;

/**
 * RAkELd - Takes RAndom partition of labELs; like RAkEL but labelsets are disjoint / non-overlapping subsets.
 * Thus, <code>m</code> is set automatically to the <i>number</i> of partitions (<code>k</code> still indicates the size of partitions, although anything more than L/2 doesn't make much sense -- this defaults to <code>PS</code>).
 * @see		RAkEL
 * @author 	Jesse Read 
 * @version June 2014
 */
public class RAkELd extends RAkEL implements TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = -6208388889440497990L;

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
		Random r = new Random(m_S);

		// Note: a slightly round-about way of doing it:
		int num = (int)Math.ceil(L / m_K);
		kMap = SuperLabelUtils.generatePartition(MLUtils.gen_indices(L),num,r,true); 
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
	public String kTipText() {
		return "The number of labels in each partition -- should be 1 <= k < (L/2) where L is the total number of labels.";
	}

	@Override
	public String mTipText() {
		return "This option is set automatically to the number of partitions (L/k)";
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
