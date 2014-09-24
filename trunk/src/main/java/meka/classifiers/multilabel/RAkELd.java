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

import weka.classifiers.*;
import weka.classifiers.meta.*;
import meka.classifiers.multilabel.*;
import weka.core.*;
import meka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * RAkELd - Takes RAndom partition of labELs; like RAkEL but labelsets are disjoint / non-overlapping subsets.
 * Thus, <code>m<\code> is set automatically to the <i>number</i> of partitions (<code>k</code> still indicates the size of partitions, although anything more than L/2 doesn't make much sense).
 * @see		RAkEL
 * @author 	Jesse Read 
 * @version June 2014
 */
public class RAkELd extends RAkEL {

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

			//int L_i = kMap[i].length;
			//int remv[] = MLUtils.invert(kMap[i],L); 	// i.e., remove the rest < L
			//Instances D_i = F.remove(new Instances(D),remv,false);
			//System.out.println(""+D_i);
			//D_i.setClassIndex(L_i);
			//m_Classifiers[i].buildClassifier(D_i);
			//D_i.clear();
			//m_InstancesTemplates[i] = D_i;
		}

	}

	/*
	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();
		//int d = x.numAttributes() - L;
		double y[] = new double[L];

		for(int i = 0; i < m_M; i++) {

			Instance x_m = RAkEL.convertInstance(x,L,m_InstancesTemplates[m]);
			x_m.setDataset(m_InstancesTemplates[m]);

			// Get a meta classification
			int i_m = (int)m_Classifiers[m].classifyInstance(x_m);        // e.g., 2
			int k_indices[] = mapBack(m_InstancesTemplates[m],i_m); // e.g., [3,8]

			Instance x_ = MLUtils.setTemplate(x, m_InstancesTemplates[i].instance(0), m_InstancesTemplates[i]);
			System.out.println(""+m_InstancesTemplates[i]);
			x_.setDataset(m_InstancesTemplates[i]);

			*
			int L_i = kMap[i].length;
			double array[] = x.toDoubleArray();  
			double array_[] = new double[d + L_i];	
			System.arraycopy(array,L,array_,L_i,d);	
			Instance x_ = new SparseInstance(1.,array_);
			x_.setDataset(m_InstancesTemplates[i]);
			x_ = m_InstancesTemplates[i].instance(0);

			public static final Instance setTemplate(Instance x, Instance x_template, Instances D_template) {
			*
			double y_i[] = ((MultilabelClassifier)m_Classifier).distributionForInstance(x_);
		}

		return new double[L];

	}
	*/

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new RAkELd(),args);
	}

}
