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

import meka.classifiers.multilabel.cc.CNode;
import meka.core.A;
import meka.core.MLUtils;
import meka.core.MultiLabelDrawable;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.*;

/**
 * CC.java - The Classifier Chains Method. Like BR, but label outputs become new inputs for the next classifiers in the chain.
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
public class CC extends ProblemTransformationMethod
		implements Randomizable, TechnicalInformationHandler, MultiLabelDrawable {

	private static final long serialVersionUID = -4115294965331340629L;

	protected CNode nodes[] = null;

	protected int m_S = 0;

	protected Random m_R = null;

	protected int m_Chain[] = null;

	public void setChain(int chain[]) {
		m_Chain = Arrays.copyOf(chain,chain.length);
	}

	public int[] retrieveChain() {
		return m_Chain;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);

		int L = D.classIndex();
		m_R = new Random(m_S);

		int indices[] = retrieveChain();
		if (indices == null) {
			indices = A.make_sequence(L);
			MLUtils.randomize(indices,m_R);
			setChain(indices);
			if(getDebug()) 
				System.out.println("Chain s="+Arrays.toString(indices));
		}

		/*
		 * make a classifier node for each label, taking the parents of all previous nodes
		 */
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
	 * @param	r	Random 			&lt;- TODO probably can use this.m_R instead
	 */
	public double[] sampleForInstance(Instance x, Random r) throws Exception {
		int L = x.classIndex();
		double y[] = new double[L];

		for(int j : m_Chain) {
			double p[] = nodes[j].distribution(x, y);
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
	 * SampleForInstance - given an Instance template for each label, and a Random.
	 * @param	t_	Instance templates (pre-transformed) using #getTransformTemplates(x)
	 */
	public double[] sampleForInstanceFast(Instance t_[], Random r) throws Exception {

		int L = t_.length;
		double y[] = new double[L];

		for(int j : m_Chain) {
			double p[] = nodes[j].distribution(t_[j],y);               // e.g., [0.4, 0.6]
			y[j] = A.samplePMF(p,r);                                   // e.g., 0
			confidences[j] = p[(int)y[j]];                             // e.g., 0.4
			nodes[j].updateTransform(t_[j],y); 						   // need to update the transform #SampleForInstance(x,r)
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
	 * <br>
	 * TODO rename distributionForPath ? and simplify like distributionForInstance ?
	 * <br>
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
			double d[] = nodes[j].distribution((Instance)x.copy(),path);  // <-- posterior distribution
			int k = (int)Math.round(path[j]);                             // <-- value of interest
			p[j] = d[k];                                                  // <-- p(y_j==k) i.e., 'confidence'
			//y[j] = path[j];
		}

		return p;
	}
	
	/**
	 * Rebuild - NOT YET IMPLEMENTED.
	 * For efficiency reasons, we may want to rebuild part of the chain.
	 * If chain[] = [1,2,3,4] and new_chain[] = [1,2,4,3] we only need to rebuild the final two links.
	 * @param	new_chain	the new chain
	 * @param	D			the original training data
	 */
	public void rebuildClassifier(int new_chain[], Instances D) throws Exception {
	}

	@Override
	public int getSeed() {
		return m_S;
	}

	@Override
	public void setSeed(int s) {
		m_S = s;
	}

	public String seedTipText() {
		return "The seed value for randomizing the data.";
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tThe seed value for randomization\n\tdefault: 0", "S", 1, "-S <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements())
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		String	tmpStr;

		tmpStr = Utils.getOption('S', options);
		if (tmpStr.length() > 0)
			setSeed(Integer.parseInt(tmpStr));
		else
			setSeed(0);

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		ArrayList<String> result;
		result = new ArrayList<String>();
		result.add("-S");
		result.add("" + getSeed());
		result.addAll(Arrays.asList(super.getOptions()));
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "Classifier Chains. " + "For more information see:\n" + getTechnicalInformation().toString();
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

	/**
	 * Returns the type of graph representing
	 * the object.
	 *
	 * @return the type of graph representing the object (label index as key)
	 */
	public Map<Integer,Integer> graphType() {
		Map<Integer,Integer>	result;
		int						i;

		result = new HashMap<Integer,Integer>();

		if (nodes != null) {
			for (i = 0; i < nodes.length; i++) {
				if (nodes[i].getClassifier() instanceof Drawable) {
					result.put(i, ((Drawable) nodes[i].getClassifier()).graphType());
				}
			}
		}

		return result;
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
	public Map<Integer,String> graph() throws Exception {
		Map<Integer,String>		result;
		int						i;

		result = new HashMap<Integer,String>();

		if (nodes != null) {
			for (i = 0; i < nodes.length; i++) {
				if (nodes[i].getClassifier() instanceof Drawable) {
					result.put(i, ((Drawable) nodes[i].getClassifier()).graph());
				}
			}
		}

		return result;
	}

	/**
	 * Returns a string representation of the model.
	 *
	 * @return      the model
	 */
	public String getModel() {
		StringBuilder   result;
		int             i;

		if (nodes == null)
			return "No model built yet";

		result = new StringBuilder();
		for (i = 0; i < nodes.length; i++) {
			if (i > 0)
				result.append("\n\n");
			result.append(getClass().getName() + ": Node #" + (i+1) + "\n\n");
			result.append(nodes[i].getClassifier().toString());
		}

		return result.toString();
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new CC(), args);
	}
}
