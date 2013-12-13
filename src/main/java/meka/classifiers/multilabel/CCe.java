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
import java.util.*;
import java.io.Serializable;

/**
 * CCe - CC extended.
 * Allows for:<br>
 * <ul>
 * <li> <code>y[] = distributionForInstance(x,path)</code>  force CC down this 'path' (p(y|path) where y[] is the actual classification</li>
 * <li> <code>y[] = sampleForInstance(x)</code>				sample the chain / classify stochastically, given x; y ~ h(x)</li>
 * <li> <code>w[] = getConfidences()</code>				    w[] holds the posterior probs, p(Y=y|x) after one of the above</li>
 * </ul>
 * This class will eventually replace CC.java
 * .
 * @author	Jesse Read
 * @version December 2013
 */
public class CCe extends MultilabelClassifier implements Randomizable {

	protected CNode nodes[] = null;
	//protected meka.classifiers.multilabel.CCe.Link root = null;

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
		}

		if(getDebug()) System.out.print(":- Chain (");
		nodes = new CNode[L];
		int pa[] = new int[]{};
		for(int j : m_Chain) {
			if (getDebug()) 
				System.out.println("\t node h_"+j+" : P(y_"+j+" | x_[:], y_"+Arrays.toString(pa)+")");
			nodes[j] = new CNode(j, null, pa);
			nodes[j].build(D, m_Classifier);
			pa = A.add(pa,j);
		}
		if (getDebug()) System.out.println(" ) -:");

		// to store posterior probabilities (confidences)
		confidences = new double[L];
	}

	private double confidences[] = null;

	/**
	 * GetConfidences.
	 * Get the posterior probabilities, i.e., confidences with which the previous x was predicted 
	 * (must call distributionForInstance first!).
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
	 * @param	r	Random 
	 */
	public double[] sampleForInstance(Instance x, Random r) throws Exception {
		int L = x.classIndex();
		double y[] = new double[L];

		for(int j : m_Chain) {
			y[j] = nodes[j].sample((Instance)x.copy(),y,r); 
		}

		return y;
	}

	// fast version, templates already prepared
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

	/*
	 * *NEW*
	 * transform CC
	public Instance[] transformInstance(Instance x) throws Exception {
		//System.out.println("CHAIN : "+Arrays.toString(this.getChain()));
		int L = x.classIndex();
		Instance x_copy[] = new Instance[L];
		root.transform(x,x_copy);
		return x_copy;
	}
	*/

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

	/**
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
	 * Rebuild.
	 * For efficiency reasons, we may want to rebuild part of the chain (which differs with nchain).
	 * If chain[] = [1,2,3,4] and nchain[] = [1,2,4,3] we only need to rebuild the final two links.
	 * @note This function does a lot more than necessary, but I was looking into improving CC at some point.
	 * @param	nchain	the new chain
	 * @param	D		the original training data
	 */
	public void rebuildClassifier(int nchain[], Instances D) throws Exception {
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CCe(),args);
	}
}
