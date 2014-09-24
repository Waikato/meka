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
 * CC.java - The Classifier Chains Method.
 * <br>
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer. Vol. 85(3), pp 333-359. (May 2011).
 * <br>
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. In Proc. of 20th European Conference on Machine Learning (ECML 2009). Bled, Slovenia, September 2009.
 * <br>
 *
 * Note that the code was reorganised substantially since earlier versions, to accomodate additional functionality needed for e.g., MCC, PCC.
 * 
 * @author	Jesse Read
 * @version December 2013
 */
public class CC extends MultilabelClassifier implements Randomizable, TechnicalInformationHandler {

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
			indices = A.make_sequence(L);
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
	 * GetTransformTemplates - pre-transform the instance x, to make things faster.
	 * @return	the templates
	 */
	public Instance[] getTransformTemplates(Instance x) throws Exception {
		int L = x.classIndex();
		Instance t_[] = new Instance[L];
		double ypred[] = new double[L];
		for(int j : m_Chain) {
			t_[j] = this.nodes[j].transform(x,ypred);
		}
		return t_;
	}

	/**
	 * SampleForInstance - NOT YET IMPLEMENTED (do it yourself for now).
	 * If we are going to sample many times for each instance, it makes sense to transform the instance first, here, instead of doing it each time.
	 * Call #transformTemplates(x) first to get the templates.
	 * @param	t_	Instance templates (pre-transformed)
	 */
	public double[] sampleForInstanceFast(Instance t_[], Random r) throws Exception {

		int L = t_.length;
		double y[] = new double[L];

		for(int j : m_Chain) {
			//setLabelsInto(t_[j],y);
			double p[] = nodes[j].distributionT(t_[j]);                // @todo copy necessary?
			y[j] = A.samplePMF(p,r);
			confidences[j] = p[(int)y[j]];
		}

		return y;
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
	 * ProbabilityForInstance - Force our way down the imposed 'path'. 
	 * For example p (y=1010|x) = [0.9,0.8,0.1,0.2]. If the product = 1, this is probably the correct path!
	 * @param	x		test Instance
	 * @param	path	the path we want to go down
	 * @return	the probabilities associated with this path: [p(Y_1==path[1]|x),...,p(Y_L==path[L]|x)]
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
		MultilabelClassifier.evaluation(new CC(),args);
	}
}
