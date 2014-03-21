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

/**
 * PMCC.java - Previously called EMCC.java; selecting the top M chains at training time, and using them in inference (instead of just one).
 * Here we use MCCtemp instead of MCC, and CCe instead of CC! -- will have to sort this out at some point!
 * This may be broken now, since we change CCe, which changed MCC, which broke MCCtemp. Extending MCC will work but will be less fast.
 * @TODO, use some kind of voting process as well
 *
 * @see weka.classifiers.multilabel.MCC.java
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version	June 2013
 */
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

public class PMCC extends MCC { 

	protected int m_M = 0;
	protected int m_O = 0;
	protected double m_Beta = 0.03;

	protected CCe h[] = null;
	protected double[] w = null;

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
	protected static CCe getClosest(HashMap<String,CCe> map, String sequence) {
		int score = -1;
		String best = sequence;
		for (String key : map.keySet()) {
			int score_ = matchedUpto(key,sequence,",");
			if (score_ > score) {
				score = score_;
				best = key;
			}
		}
		System.out.println("found "+best+" closest to "+sequence);
		return map.get(best);
	}

	/**
	 * RebuildCC - rebuild a classifier chain 'h_old' to have a new sequence 's_new'.
	 */
	protected CCe rebuildCC(CCe h_old, int s_new[], Instances D) throws Exception {

		// make a deep copy
		CCe h = (CCe)AbstractClassifier.makeCopy(h_old);

		// rebuild this chain
		h.rebuildClassifier(s_new,new Instances(D));
		return h;
	}

	/** 
	 * BuildCC - Build a CC on sequence 's'. 
	 * Build h_{s} : X -> Y
	 */
	protected CCe buildCC(int s[], Instances D) throws Exception {

		// a new classifier chain
		CCe h = new CCe();

		// build this chain
		h.setChain(s);
		h.setClassifier(m_Classifier);
		h.buildClassifier(new Instances(D));
		return h;
	}

	/**
	 * pi - swap elements in s, depending on iteration t.
	 * @todo - make faster! should be able to avoid second normalization ...
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
		int j = A.rndsrc(p,r); 
		System.out.println("elect j="+j+" from pmf: "+A.toString(p));

		// blank out the j-th entry, and renormalize, now select k
		p[j] = 0.0;
		Utils.normalize(p);
		int k = A.rndsrc(p,r); 
		System.out.println("elect k="+k+" from pmf: "+A.toString(p));

		// swap j and k
		return A.swap(s,j,k);
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {

		HashMap<String,CCe> TheMap = new HashMap<String,CCe>();

		r = new Random(m_S);

		// Variables

		int L = D.classIndex(); 
		int N = D.numInstances();
		int d = D.numAttributes()-L;

		h = new CCe[m_M];
		w = new double[m_M];
		//int s[][] = new int[m_M][L]; // for interest's sake

		if (m_Is > 0) {

			// Make CC
			int s[] = MLUtils.gen_indices(L); 
			MLUtils.randomize(s,r);
			h[0] = buildCC(Arrays.copyOf(s,s.length),D); // @todo move into setChain(..)
			w[0] = payoff(h[0],new Instances(D));
			TheMap.put(Arrays.toString(s),h[0]);			// save a copy
			//s[0] = s_;
			if(getDebug()) System.out.println("s[0] = "+Arrays.toString(s));

			for(int t = 0; t < m_Is; t++) {

				// propose a chain s' ~ pi(s'|s) 
				int s_[] = (m_O > 0) ? 
					  pi(Arrays.copyOf(s,s.length),r,t,m_Beta)	  :	// default cond. option - with temperature
					  A.swap(Arrays.copyOf(s,s.length),r) ;	        // special simple option - swap two elements

				// build h' with sequence s'
				CCe h_ = rebuildCC(getClosest(TheMap,Arrays.toString(s_)),s_,D);
				TheMap.put(Arrays.toString(s_), h_);

				// rate h' (by its performance on the training data)
				double w_ = payoff(h_,new Instances(D));

				// accept h' weighted more than the weakest h in the population
				int m = Utils.sort(w)[0]; // (min index)
				if (w_ > w[m]) {
					w[m] = w_;
					h[m] = h_;
					if (getDebug()) System.out.println(" accepted h["+m+"] = "+w[m]);
					s = s_;
				}
			}
			if (getDebug()) System.out.println("---");
		}
		else {
			throw new Exception("This classifier must be run with a population. Use MCC if you don't want this.");
		}

		// normalise weights
		Utils.normalize(w);
	}

	/**
	 * RandomSearch - Inference. 
	 * This is like Simulated Annealing without temperature.
	 * @param	h[]		a population of classifier chains
	 * @param	h_w[] 	weights associated with h[]
	 * @param	x   	the Instance to perform inference on (i.e, p(y|x))
	 * @param	T		number of iterations
	 * @param	r		random number generator
	 * @param	y0[]	initial guess for y
	 * @return	the classification y
	 */
	public static double[] RandomSearch(CCe h[], double h_weights[], Instance x, int T, Random r, double y0[]) throws Exception {

		double y[] = Arrays.copyOf(y0,y0.length); 					// prior y
		double w  = A.product(h[0].probabilityForInstance(x,y));	// weight

		y = Arrays.copyOf(y,y.length);
		for(int t = 0; t < T; t++) {
			// choose h[i] according to w[i]
			int m = A.rndsrc(h_weights,r);
			// propose y' by sampling i.i.d.
			double y_[] = h[m].sampleForInstance(x,r); 	       
			// weight y' as w'
			double w_  = A.product(h[m].getConfidences());
			// accept ? 
			if (w_ > w) {
				w = w_;
				y = y_;
			}
		}
		return y;
	}

	/**
	 * RandomSearch. 
	 * Simulated Annealing without temperature.
	 */
	public static double[] RandomSearch(CCe h[], double ww[], Instance x, int T, Random r) throws Exception {

		return RandomSearch(h,ww,x,T,r,h[0].distributionForInstance(x));
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		if (m_Iy <= 0) {
			// 0 iterations of inference, just return the greedy classification of CC
			return h[0].distributionForInstance(x);
		}
		else { 
			// MC inference (we could do PCC's Bayes-optimal inference here if L < 10, ... but we don't)
			return RandomSearch(h,w,(Instance)x.copy(),m_Iy,r);
		}
	}

	protected int m_S = 0;

	@Override
	public void setSeed(int s) {
		m_S = s;
	}

	@Override
	public int getSeed() {
		return m_S;
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tThe population size (of chains)\n\tdefault: "+m_M, "M", 1, "-M <value>"));
		newVector.addElement(new Option("\tThe option for changing the sequences ~p(.|s)      \n\tdefault: "+m_O, "O", 1, "-O <value>"));
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

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new PMCC(),args);
	}

}

