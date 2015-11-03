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

import meka.core.A;
import meka.core.MLUtils;
import meka.core.OptionUtils;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

import java.util.*;

/**
 * PMCC.java - Like MCC but creates a population of M chains at training time (from Is <i>candidate</i> chains, using Monte Carlo sampling), and uses this population for inference at test time; If you are looking for a 'more typical' majority-vote ensemble method, use something like EnsembleML or BaggingML with MCC.
 *
 * <p>
 * <b>NOTE:</b> this implementation was faster, because the chain was only rebuilt from the first node which was different -- this is no longer the case (due to updates to way classifier chains works, using the CNode class).
 * </p>
 *
 * @see meka.classifiers.multilabel.MCC
 * @author Jesse Read
 * @version	Sep 2014
 */
public class PMCC extends MCC {

	private static final long serialVersionUID = 1999206808758133267L;
	protected int m_M = 10;
	protected int m_O = 0;
	protected double m_Beta = 0.03;

	protected CC h[] = null;
	protected double[] w = null;

	public PMCC() {
		// a new default
		super.setChainIterations(50);
	}

	/**
	 * MatchedUpTo - returns the index i of the first character which differs between two strings.
	 * TODO this is a generic function, could go somewhere else into utils.
	 */
	private static int matchedUpto(String s1, String s2, String DELIM) {
		String s_1[] = s1.split(DELIM);
		String s_2[] = s2.split(DELIM);
		int i = 0;
		while (i < s_1.length && i < s_2.length && s_1[i].equals(s_2[i]))
			i++;
		return i;
	}

	/**
	 * GetClosest - returns the 'CC' in 'map' which is built on the sequence most matched to 'sequence'.
	 */
	protected static CC getClosest(HashMap<String,CC> map, String sequence) {
		int score = -1;
		String best = sequence;
		for (String key : map.keySet()) {
			int score_ = matchedUpto(key,sequence,",");
			if (score_ > score) {
				score = score_;
				best = key;
			}
		}
		return map.get(best);
	}

	/**
	 * RebuildCC - rebuild a classifier chain 'h_old' to have a new sequence 's_new'.
	 */
	protected CC rebuildCC(CC h_old, int s_new[], Instances D) throws Exception {

		// make a deep copy
		CC h = (CC)AbstractClassifier.makeCopy(h_old);

		// rebuild this chain
		h.rebuildClassifier(s_new,new Instances(D));
		return h;
	}

	/** 
	 * BuildCC - Build a CC of chain-order 's' on dataset 'D'. 
	 */
	protected CC buildCC(int s[], Instances D) throws Exception {

		// a new classifier chain
		CC h = new CC();

		// build this chain
		h.prepareChain(s);
		h.setClassifier(m_Classifier);
		h.buildClassifier(new Instances(D));
		return h;
	}

	/**
	 * pi - proposal distribution; swap elements in s, depending on iteration t (temperature).
	 * <br>
	 * TODO - make faster!
	 * @param	s		a chain sequence
	 * @param	r  		a random number generator
	 * @param	t   	the current iteration
	 * @return 	s' ~ p(s'|s)
	 */
	public static int[] pi(int s[], Random r, int t, double beta) {
		int L = s.length;

		System.out.println("--- t = "+t+" , Beta = "+beta + "---");

		// select some entry j
		double p[] = new double[s.length];
		for(int j = 0; j < L; j++) {
			p[j] = Math.pow((1./L),beta * t / (1+j));
		}
		Utils.normalize(p);
		int j = A.samplePMF(p,r); 
		System.out.println("elect j="+j+" from pmf: "+A.toString(p));

		// blank out the j-th entry, and renormalize, now select k
		p[j] = 0.0;
		Utils.normalize(p);
		int k = A.samplePMF(p,r); 
		System.out.println("elect k="+k+" from pmf: "+A.toString(p));

		// swap j and k
		return A.swap(s,j,k);
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {

		m_R = new Random(m_S);

		// Variables

		int L = D.classIndex(); 
		int N = D.numInstances();
		int d = D.numAttributes()-L;

		h = new CC[m_M];
		w = new double[m_M];
		//int s[][] = new int[m_M][L]; // for interest's sake

		if (m_Is >= m_M) {

			//HashMap<String,CC> id2cc = new HashMap<String,CC>();

			// Make CC
			int s[] = MLUtils.gen_indices(L); 
			MLUtils.randomize(s,m_R);
			h[0] = buildCC(Arrays.copyOf(s,s.length),D); // @todo move into setChain(..)
			w[0] = payoff(h[0],D);
			//id2cc.put(Arrays.toString(s),h[0]);			// save a copy
			//s[0] = s_;
			if(getDebug()) System.out.println("s[0] = "+Arrays.toString(s));

			for(int t = 0; t < m_Is; t++) {

				// propose a chain s' ~ pi(s'|s) 
				int s_[] = (m_O > 0) ? 
					  pi(Arrays.copyOf(s,s.length),m_R,t,m_Beta)	  :	// default cond. option - with temperature
					  A.swap(Arrays.copyOf(s,s.length),m_R) ;	        // special simple option - swap two elements

				// build h' with sequence s'
				//CC h_ = rebuildCC(getClosest(id2cc,Arrays.toString(s_)),s_,D);
				CC h_ = buildCC(Arrays.copyOf(s_,s_.length),D);
				//id2cc.put(Arrays.toString(s_), h_);

				// rate h' (by its performance on the training data)
				double w_ = payoff(h_,D);

				// accept h' weighted more than the weakest h in the population
				int min = Utils.sort(w)[0]; // (min index)
				if (w_ > w[min]) {
					w[min] = w_;
					h[min] = h_;
					if (getDebug()) System.out.println(" accepted h_ with score "+w_+" > "+w[min]);
					s = s_;
				}
				else
					if (getDebug()) System.out.println(" DENIED h_ with score "+w_+" !> score "+w[min]);
			}
			if (getDebug()) System.out.println("---");

			// normalise weights
			Utils.normalize(w);
		}
		else {
			throw new Exception("[Error] Number of chains evaluated (Is) should be at least as great as the population selected (M), and always greater than 0.");
		}

	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		// Start with a good guess
		int max = Utils.maxIndex(w);
		double y[] = h[max].distributionForInstance(x);
		double wm  = A.product(h[max].probabilityForInstance(x,y));	

		for(int t = 0; t < m_Iy; t++) {
			// m ~ p(m|w) 
			int m = A.samplePMF(w,m_R);
			// y ~ p(y|x,m)
			double y_[] = h[m].sampleForInstance(x,m_R); 	       // <-- TODO: can do this faster, see #MCC
			// w = prod_j p(y[j]|x,m)
			double w_  = A.product(h[m].getConfidences());
			// accept ? 
			if (w_ > wm) {
				wm = w_;
				y = y_;
			}
		}

		return y;
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tThe population size (of chains) -- should be smaller than the total number of chains evaluated (Is) \n\tdefault: 10", "M", 1, "-M <value>"));
		result.addElement(new Option("\tUse temperature: cool the chain down over time (from the beginning of the chain) -- can be faster\n\tdefault: 0 (no temperature)", "O", 1, "-O <value>"));
		result.addElement(new Option("\tIf using O = 1 for temperature, this sets the Beta constant      \n\tdefault: 0.03", "B", 1, "-B <value>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setM(OptionUtils.parse(options, 'M', 10));
		setO(OptionUtils.parse(options, 'O', 0));
		setBeta(OptionUtils.parse(options, 'B', 0.03));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'M', getM());
		OptionUtils.add(result, 'O', getO());
		OptionUtils.add(result, 'B', getBeta());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	/** Set the temperature factor  */
	public void setBeta(double t) {
		m_Beta = t;
	}

	/** Get the temperature factor */
	public double getBeta() {
		return m_Beta;
	}

	public String betaTipText() {
		return "Sets the temperature factor.";
	}

	/** Set the temperature switch  */
	public void setO(int t) {
		m_O = t;
	}

	/** Get the temperature switch */
	public int getO() {
		return m_O;
	}

	public String oTipText() {
		return "Sets the temperature switch.";
	}

	/** Set the population size */
	public void setM(int M) {
		m_M = M;
	}

	/** Get the population size */
	public int getM() {
		return m_M;
	}

	public String mTipText() {
		return "Sets the population size.";
	}

	@Override
	public String globalInfo() {
		return "PMCC - Like MCC but selects the top M chains at training time, and uses all them at test time (using Monte Carlo sampling -- this is not a typical majority-vote ensemble method). For more information see:\n" + getTechnicalInformation().toString();
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new PMCC(), args);
	}

}

