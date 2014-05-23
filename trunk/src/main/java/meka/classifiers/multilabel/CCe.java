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
import meka.classifiers.multilabel.cc.CNode;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import java.util.*;
import java.io.Serializable;

/**
 * CCe - CC extended. CC has been used and compared to in several papers, so I have kept that class as it was, 
 * and introduced this extended class, which has some additional functionality needed for e.g., MCC, PCC.
 * This class will eventually replace CC.java, if results remain identical.
 * 
 * @see meka.classifiers.multilabel.CC
 * @author	Jesse Read
 * @version December 2013
 */
public class CCe extends MultilabelClassifier implements Randomizable, TechnicalInformationHandler {

	protected CNode nodes[] = null;

	protected int m_S = 0;
	protected Random m_R = null;

	@Override
	public void setSeed(int s) {
		m_S = s;
	}

	@Override
	public int getSeed() {
		return m_S;
	}

	protected int m_Chain[] = null;

	public void setChain(int chain[]) {
		m_Chain = Arrays.copyOf(chain,chain.length);
	}

	public int[] getChain() {
		return m_Chain;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);
		int L = D.classIndex();
		m_R = new Random(m_S);

		int indices[] = getChain();
		if (indices == null) {
			indices = MLUtils.gen_indices(L);
			MLUtils.randomize(indices,m_R);
			setChain(indices);
			if(getDebug()) 
				System.out.println("Chain s="+Arrays.toString(indices));
		}

		if(getDebug()) System.out.print(":- Chain (");
		nodes = new CNode[L];
		int pa[] = new int[]{};
		for(int j : m_Chain) {
			if (getDebug()) 
				System.out.print(" "+D.attribute(j).name());
				//System.out.println("\t node h_"+j+" : P(y_"+j+" | x_[:], y_"+Arrays.toString(pa)+")");
			nodes[j] = new CNode(j, null, pa);
			nodes[j].build(D, m_Classifier);
			pa = A.append(pa,j);
		}
		if (getDebug()) System.out.println(" ) -:");

		// to store posterior probabilities (confidences)
		confidences = new double[L];
	}

	protected double confidences[] = null;

	/**
	 * GetConfidences - get the posterior probabilities of the previous prediction (after calling distributionForInstance(x)).
	 */
	public double[] getConfidences() {
		return confidences;
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		double y[] = new double[L];

		for(int j : m_Chain) {
			// h_j : x,pa_j -> y_j
			y[j] = nodes[j].classify((Instance)x.copy(),y); 
		}

		return y;
	}

	/**
	 * SampleForInstance.
	 * predict y[j] stochastically rather than deterministically (as with distributionForInstance(Instance x)).
	 * @param	x	test Instance
	 * @param	r	Random 			<- @TODO probably can use this.m_R instead
	 */
	public double[] sampleForInstance(Instance x, Random r) throws Exception {
		int L = x.classIndex();
		double y[] = new double[L];

		for(int j : m_Chain) {
			double p[] = nodes[j].distribution(x, y);                // @todo copy necessary?
			y[j] = A.samplePMF(p,r);
			confidences[j] = p[(int)y[j]];
		}

		return y;
	}

	/**
	 * SampleForInstance - NOT YET IMPLEMENTED (do it yourself for now).
	 * fast version, templates already prepared
	 */
	public double[] sampleForInstance(Instance x[], Random r) throws Exception {
		return null;

//		/*
//		for(int i = 0; i < x.length; i++) {
//			System.out.println("x["+i+"] = "+x[i]);
//		}
//		*/
//		//int L = x[0].classIndex();
//		int L = x.length;
//		double vals[] = new double[L];
//		// CHECK THIS FUNCTION IF NOT WORKING
//		Instance res = root.sample(x,r,vals);
//		// MAKE SURE TO SELECT THE COORECT INDEX HERE
//		/*
//		System.out.println("est: "+Arrays.toString(MLUtils.toDoubleArray(res,L)));
//		System.out.print("est: "+Arrays.toString(vals)+" @ ");
//		*/
//		//System.out.println(Arrays.toString(confidences));
//		//return confidences; //MLUtils.toDoubleArray(x[x.length-1]);
//		return MLUtils.toDoubleArray(res,L);
	}

	/**
	 * TransformInstances - this function is DEPRECATED.
	 * this function preloads the instances with the correct class labels ... to make the chain much faster,
	 * but CNode does not yet have this functionality ... need to do something about this!
	 */
	public Instance[] transformInstance(Instance x) throws Exception {
		return null;
		/*
		//System.out.println("CHAIN : "+Arrays.toString(this.getChain()));
		int L = x.classIndex();
		Instance x_copy[] = new Instance[L];
		root.transform(x,x_copy);
		return x_copy;
		*/
	}

	/**
	 * ProbabilityForInstance - P(y=1|x).
	 * Force our way down 'path' (y). For example p (y=1010|x) = [0.9,0.8,0.1,0.2]
	 * @param	x		test Instance
	 * @param	path	the path we want to go down
	 * @return	The probabilities associated with this path
	 */
	public double[] probabilityForInstance(Instance x, double path[]) throws Exception {
		int L = x.classIndex();
		double p[] = new double[L];

		for(int j : m_Chain) {
			// h_j : x,pa_j -> y_j
			double d[] = nodes[j].distribution((Instance)x.copy(),path); 
			int k = (int)Math.round(path[j]);
			p[j] = d[k];
			//y[j] = path[j];
		}

		return p;
	}

	/*
	 * ProbabilityForInstance.
	 * Force our way down 'path' where 'path' = x[0],...,x[L] (set into x before calling this function).
	 * @param	x		test Instance
	public double[] probabilityForInstance(Instance x) throws Exception {
		// run through chain
		root.probability(x);
		// return p(path|x)
		return getConfidences();
	}
	*/
	
	/**
	 * Rebuild - NOT YET IMPLEMENTED.
	 * For efficiency reasons, we may want to rebuild part of the chain (which differs with nchain).
	 * If chain[] = [1,2,3,4] and nchain[] = [1,2,4,3] we only need to rebuild the final two links.
	 * @note This function does a lot more than necessary, but I was looking into improving CC at some point.
	 * @param	nchain	the new chain
	 * @param	D		the original training data
	 */
	public void rebuildClassifier(int nchain[], Instances D) throws Exception {
	}

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"The Classifier Chains Method."
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		TechnicalInformation	additional;
		
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank");
		result.setValue(Field.TITLE, "Classifier Chains for Multi-label Classification");
		result.setValue(Field.JOURNAL, "Machine Learning Journal");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.VOLUME, "85");
		result.setValue(Field.NUMBER, "3");
		result.setValue(Field.PAGES, "333-359");
		
		additional = new TechnicalInformation(Type.INPROCEEDINGS);
		additional.setValue(Field.AUTHOR, "Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank");
		additional.setValue(Field.TITLE, "Classifier Chains for Multi-label Classification");
		additional.setValue(Field.BOOKTITLE, "20th European Conference on Machine Learning (ECML 2009). Bled, Slovenia, September 2009");
		additional.setValue(Field.YEAR, "2009");

		result.add(additional);
    
		return result;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CCe(),args);
	}
}
