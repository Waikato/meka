package meka.classifiers.multilabel;

import weka.classifiers.*;
//import weka.classifiers.meta.*;
import meka.classifiers.multilabel.*;
import meka.classifiers.multilabel.cc.CNode;
import meka.classifiers.multilabel.cc.Trellis;
import weka.core.*;
import meka.core.A;
import meka.core.F;
import meka.core.MLUtils;
import meka.core.StatUtils;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;
import java.io.Serializable;

/**
 * CT. Classifier Trellis.
 * @author	Jesse Read
 * @version Feb 2013
 */
public class CT extends MCC {

	protected int m_Width = -1;
	protected int m_Connectivity = 1;

	protected String m_DependencyPayoff = "Ibf";

	Trellis trel = null;

	protected CNode nodes[] = null;

	private String info = "";

	public String toString() {
		return info;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		int L = D.classIndex();
		int d = D.numAttributes()-L;
		r = new Random(getSeed());

		if (m_Width < 0)
			m_Width = (int)Math.sqrt(L);

		nodes = new CNode[L];
		/*
		 * Make the Trellis.
		 */
		if (getDebug())
			System.out.println("Make Trellis");
		int indices[] = A.make_sequence(L);
		A.shuffle(indices, new Random(getSeed()));
		trel = new Trellis(indices, m_Width, m_Connectivity);

		//double w = payoff(D, indices,trellis);
		//System.out.println("payoff = "+w+" : "+Arrays.toString(indices));

		/*
		 * Check Payoff
		 * A:
		 * 1. pick two nodes j and k
		 * 2. weight the neighbourhoods of j and k
		 * 3. sum this weight
		 * 4. swap nodes j and k
		 * 5. weight the neighbourhoods of j and k
		 * 6. sum this weight
		 * 7. replace if better than current
		 *
		 * B: 
		 * 0. start inserting node Y_1 at [0,0]
		 * 1. pick next slot n
		 * 2. rank rest of nodes k based on I[pa(n)][k]
		 * 3. insert best node k in slot n
		 * 4. repeat from 1.
		System.out.println("Measure Payoff x 10");
		for(int i = 0; i < 10; i++) {
			A.shuffle(indices, new Random(getSeed()));
			int trellis_[][] = makeTrellis(indices, m_Width, m_Connectivity);
			double w_ = payoff(D, indices,trellis_);
			System.out.println("payoff = "+w_+" : "+Arrays.toString(indices));
		}
		*/
		//////////////////////////
		// place highest nodes 
		// 3                      <--- active row
		// 2
		//                             choose best connected of 3,2
		// 3--2                        place it were sensible, i.e., underneath if possible, else to the right
		// 2                           or
		//////////////////////////
		
		long start = System.currentTimeMillis();

		if (m_Is > 0) {

			/*
			 * Get dependency Matrix
			 */
			double I[][] =  StatUtils.margDepMatrix(D,m_DependencyPayoff);
			if (getDebug()) 
				System.out.println("Got "+m_DependencyPayoff+" Matrix:"+(System.currentTimeMillis() - start)/1000.0);
			//System.out.println("Got I"+M.toString(I)+"\n:"+(System.currentTimeMillis() - start)/1000.0);

			int Y[] = new int[L];

			/*
			 * Make list of indices
			 */
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int i : indices) {
				list.add(new Integer(i));
			}

			/*
			 * Take first index, and proceed
			 */
			Y[0] = list.remove(r.nextInt(L)); 
			if (getDebug()) 
				System.out.print(" "+String.format("%4d", Y[0]));
			// @todo: update(I,j_0) to make faster
			for(int j = 1; j < L; j++) {
				double max_w = -1.;
				int j_ = -1;
				for(int j_prop : list) {
					double w = trel.weight(Y,j,j_prop,I);
					if (w >= max_w) {
						max_w = w;
						j_ = j_prop;
					}
				}
				list.remove(new Integer(j_));
				if (getDebug()) {
					System.out.print(" "+String.format("%4d", j_));
					if (j % m_Width == 0) 
						System.out.println();
				}
				Y[j] = j_;
				// @todo: update(I,j_), because it will be a parent now
			}

			trel = new Trellis(Y, m_Width, m_Connectivity);
		}
		//if (getDebug())
		//	System.out.println("==================\n"+trel.toString()+"\n=================");

		info = String.valueOf((System.currentTimeMillis() - start)/1000.0);

		if (getDebug()) System.out.println("\nTrellis built in: "+info+"s");
		/*
		Y[0] = j_max;
		Y[j+WIDTH] = k_max;       // underneath
		prev = 0;
		for(int j = 2; j < L; j++) {
			j_max = Y[prev];
			k_max = maxIndex(I,j_max); // <-- limit the possible choices here
			int underneath = trel.under(k_max);
			if (underneath > 0 && Y[underneath] > 0)
				Y[underneath] = k_max;
			else {
				Y[prev+1] = j;
				prev = prev+1;
				if (prev == WIDTH) // check for end
					prev = 0;
			}
			I[j_max][k_max] = 0.;
		}

		//trel.swap(2,3);
		for(int i = 0; i < m_Is; i++) {
			int maxi[] = maxIndices(I);
			System.out.println(""+Arrays.toString(maxi));
			trel.putTogether(maxi[0],maxi[1]);
			System.out.println(""+trel.toString());
			I[maxi[0]][maxi[1]] = 0.0;
		}
		*/

		/*
		double w = trel.weight(D); //+ trel.weight(k);
		if (getDebug()) System.out.println("w = "+w);
		for(int i = 0; i < m_Is; i++) {
			int j = r.nextInt(trel.L);
			int k = r.nextInt(trel.L);
			trel.swap(j,k);
			double w_ = trel.weight(D); //+ trel.weight(k);
			if (getDebug()) System.out.println("w_ = "+w_);
			if (w_ > w) {
				if (getDebug()) System.out.println("accept");
				w = w_;
			}
			else 
				trel.swap(j,k);
		}
		*/

		/*
		 * Build Trellis
		 */
		if (getDebug())
			System.out.println("Build Trellis");

		for(int jv : trel.indices) {
			if (getDebug()) {
				System.out.print(" > "+jv);
				//System.out.println("Build Node h_"+jv+"] : P(y_"+jv+" | x_[1:d], y_"+Arrays.toString(trel.trellis[jv])+")");
			}
			nodes[jv] = new CNode(jv, null, trel.trellis[jv]);
			nodes[jv].build(D,m_Classifier);
		}

	}

	public static double payoff(Instances D, int indices[], int trellis[][]) {
		double payoff = 0.0;
		for(int j = 0; j < trellis.length; j++) {
			int jv = indices[j];
			for(int k = 0; k < trellis[j].length; k++) {
				payoff += StatUtils.I(D,jv,k);
			}
		}
		return payoff;
	}

	/**
	 * RandomSearch. With Prior. 
	 * @NOTE: taken from MCC -- the same, but with CT instead of CCe AND MODIFIED
	 */
	public static double[] RandomSearch(CT h, Instance x, int T, Random r, double y0[]) throws Exception {

		double y[] = Arrays.copyOf(y0,y0.length); 				// prior y
		double w  = A.product(y);	// p(y|x)

		y = Arrays.copyOf(y,y.length);
		for(int t = 0; t < T; t++) {
			// propose y' by sampling i.i.d.
			double y_[] = h.sampleForInstance(x,r); 	       
			// rate y' as w'
			double w_  = A.product(y);
			// accept ? 
			if (w_ > w) {
				w = w_;
				y = y_;
				//if (getDebug()) System.out.println(" y' = "+MLUtils.toBitString(y_) +" w' = "+w_+ " (accept!)"); 
			}
		}
		return y;
	}

	public double[] RandomSearch(CT h, Instance x, int T, Random r) throws Exception {
		return RandomSearch(h,x,T,r,classifyForInstance(x));
	}

	public double[] classifyForInstance(Instance x) throws Exception {

		int L = x.classIndex();
		double y0[] = new double[L];

		for(int jv : trel.indices) {// = 0; j < L; j++) {
			y0[jv] = nodes[jv].classify(x,y0);
		}

		return y0;
	}

	public double[] sampleForInstance(Instance x, Random r) throws Exception {

		int L = x.classIndex();
		double y[] = new double[L];

		for(int jv : trel.indices) {// = 0; j < L; j++) {
			y[jv] = nodes[jv].sample(x,y,r);
		}

		return y;
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		return RandomSearch(this,x,m_Iy,new Random(getSeed()));
	}

	/*
	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();
		double y[] = new double[L];

		for(int j = 0; j < L; j++) {
			y[j] = nodes[j].classify(x,y);
		}

		return y;
	}
	*/

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tThe width of the trellis.\n\tdefault: "+m_Width, "H", 1, "-H <value>"));
		newVector.addElement(new Option("\tThe density/type of the trellis.\n\tdefault: "+m_Connectivity+"\n\trange: 0-3 (0=BR)", "L", 1, "-L <value>"));
		newVector.addElement(new Option("\tThe dependency payoff function.\n\tdefault: "+m_DependencyPayoff+"\n\t", "d", 1, "-d <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		m_Width = (Utils.getOptionPos('H',options) >= 0) ? Integer.parseInt(Utils.getOption('H', options)) : m_Width;
		if (getDebug()) System.out.println("WIDTH SET AS: "+m_Width);
		m_Connectivity = (Utils.getOptionPos('L',options) >= 0) ? Integer.parseInt(Utils.getOption('L', options)) : m_Connectivity;
		if (getDebug()) System.out.println("Trellis Type: "+m_Connectivity);
		m_DependencyPayoff = (Utils.getOptionPos('d',options) >= 0) ? Utils.getOption('d', options) : m_DependencyPayoff;
		if (getDebug()) System.out.println("Dependency Type: "+m_DependencyPayoff);

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		ArrayList<String> result;
	  	result = new ArrayList<String>(Arrays.asList(super.getOptions()));
	  	result.add("-H");
	  	result.add("" + m_Width);
		result.add("-L");
	  	result.add("" + m_Connectivity);
		result.add("-d");
	  	result.add("" + m_DependencyPayoff);
		return result.toArray(new String[result.size()]);
	}

	// complement([1,2,3],5) -> [0,4]
	// N.B. this already exists as 'invert' in MLUtils
	private static final int[] complement(int indices[], int L) {
		int sindices[] = Arrays.copyOf(indices,indices.length);
		Arrays.sort(sindices);
		int inverted[] = new int[L-sindices.length];
		for(int j = 0,i = 0; j < L; j++) {
			if (Arrays.binarySearch(sindices,j) < 0) {
				inverted[i++] = j;
			}
		}
		return inverted;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CT(),args);
	}
}
