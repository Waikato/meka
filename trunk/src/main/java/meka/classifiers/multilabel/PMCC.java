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
import weka.classifiers.functions.*;
import meka.classifiers.multitarget.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.supervised.attribute.*;
import weka.attributeSelection.*;
import weka.filters.*;
import weka.core.*;
import meka.core.*;
import java.util.*;

/**
 * PMCC.java - Like MCC but selects the top M chains at training time, and uses all them at test time (using Monte Carlo sampling -- this is not a typical majority-vote ensemble method).
 *
 * NOTE: this implementation used to be faster, because the chain was only rebuilt from the first node which was different -- this is no longer the case.
 *
 * @see #weka.classifiers.multilabel.MCC
 * @author Jesse Read
 * @version	Sep 2014
 */
public class PMCC extends MCC { 

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
	 * BuildCC - Build a CC of chain-order 's'. 
	 */
	protected CC buildCC(int s[], Instances D) throws Exception {

		// a new classifier chain
		CC h = new CC();

		// build this chain
		h.setChain(s);
		h.setClassifier(m_Classifier);
		h.buildClassifier(new Instances(D));
		return h;
	}

	/**
	 * pi - proposal distribution; swap elements in s, depending on iteration t (temperature).
	 * TODO - make faster!
	 * @param	s[]		a chain sequence
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

		if (m_Is > m_M) {

			//HashMap<String,CC> id2cc = new HashMap<String,CC>();

			// Make CC
			int s[] = MLUtils.gen_indices(L); 
			MLUtils.randomize(s,m_R);
			h[0] = buildCC(Arrays.copyOf(s,s.length),D); // @todo move into setChain(..)
			w[0] = likelihood(h[0],D);
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
				double w_ = likelihood(h_,D);

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
			System.err.println("[Error] Number of chains evaluated (Is) shoulld be at least as greater than the population selected (M), and greater than 0.");
			super.buildClassifier(D);
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

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tThe population size (of chains) -- should be smaller than the total number of chains evaluated (Is) \n\tdefault: "+m_M, "M", 1, "-M <value>"));
		newVector.addElement(new Option("\tUse temperature: cool the chain down over time (from the beginning of the chain) -- can be faster\n\tdefault: "+m_O+" (no temperature)", "O", 1, "-O <value>"));
		newVector.addElement(new Option("\tIf using O = 1 for temperature, this sets the Beta constant      \n\tdefault: "+m_Beta, "B", 1, "-B <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		m_M = (Utils.getOptionPos('M',options) >= 0) ? Integer.parseInt(Utils.getOption('M', options)) : m_M;
		m_O = (Utils.getOptionPos('O',options) >= 0) ? Integer.parseInt(Utils.getOption('O', options)) : m_O;
		m_Beta = (Utils.getOptionPos('B',options) >= 0) ? Double.parseDouble(Utils.getOption('B', options)) : m_Beta;
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		ArrayList<String> result;
	  	result = new ArrayList<String>(Arrays.asList(super.getOptions()));
	  	result.add("-M");
	  	result.add("" + m_M);
		result.add("-O");
	  	result.add("" + m_O);
		result.add("-B");
	  	result.add("" + m_Beta);
		return result.toArray(new String[result.size()]);
	}

	/** Set the population size */
	public void setM(int M) {
		m_M = M;
	}

	/** Get the population size */
	public int getM() {
		return m_M;
	}

	@Override
	public String globalInfo() {
		return "PMCC - Like MCC but selects the top M chains at training time, and uses all them at test time (using Monte Carlo sampling -- this is not a typical majority-vote ensemble method). For more information see:\n" + getTechnicalInformation().toString();
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new PMCC(),args);
	}

}

