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
 * @author	Jesse Read (jesse@tsc.uc3m.es)
 * @version March 2013
 */
public class CCe extends MultilabelClassifier implements Randomizable {

	protected meka.classifiers.multilabel.CCe.Link root = null;

	protected class Link implements Serializable {

		protected Link next = null;
		private AbstractClassifier classifier = null;
		public Instances _template = null;
		private int index = -1;
		private int exl[]; 
		private int map[]; // new
		private int pa[];  // new, the inverse of exl
		private int j = 0; 

		// s = [3,2,4,5,1], j = 2, D
		public Link(int s[], int j, Instances D) throws Exception {
			buildLink(s,j,D);
			j++;
			if(j < s.length) 
				next = new Link(s, j, D);
		}

		// for efficiency
		private void rebuildLink(int s_[], int j, Instances D) throws Exception {
			rebuildLink(s_,j,D,false);
		}

		// for efficiency
		public void rebuildLink(int s_[], int j, Instances D, boolean b) throws Exception {
			if (s_[j] == this.index && next !=null && !b) {
				buildLink(s_,j,D);
				next.rebuildLink(s_,++j,D,false);
			}
			else {
				this.classifier = null;
				buildLink(s_,j,D);
				if(j+1 < s_.length) 
					next.rebuildLink(s_,++j,D,true);
			}
		}

		public void buildLink(int s[], int j, Instances D) throws Exception {
			this.j = j;			// 2

			this.index = s[j];	// s_j

			if(getDebug()) System.out.print(" "+s[j]);

			this.exl = Arrays.copyOfRange(s,j+1,s.length);		// remove  [5,1]
			Arrays.sort(this.exl); 								// sort to [1,5]
			this.pa = MLUtils.invert(this.exl,D.classIndex()); 		// list of parents (note: includes this.index!)
			this.map = makeMap(this.index,this.pa,D.classIndex());

			Instances D_j = CCUtils.linkTransform(D,j,this.index,this.exl);

			if (this.classifier == null) {
				this.classifier = (AbstractClassifier)AbstractClassifier.forName(getClassifier().getClass().getName(),((AbstractClassifier)getClassifier()).getOptions());
				this.classifier.buildClassifier(D_j);
			}
			else  {
				// Skip ... (already built)
			}

			_template = new Instances(D_j,0);
			D_j = null;
		}

		protected Classifier getLinkClassifier() {
			return classifier;
		}

		// Greedy Classification, at the end of the chain the classification is in 'x' ...
		protected void classify(Instance x) throws Exception {

			// cut out irrelevant attributes
			Instance copy = CCUtils.linkTransformation(x,this.exl,this._template);

			// round
			for(int k = 0; k < this.j; k++) {
				copy.setValue(j,Math.round(copy.value(k)));
			}

			//set class          
			double dist[] = this.classifier.distributionForInstance(copy);  
			int max_index = (int)Utils.maxIndex(dist);		// v = max_index(dist)
			confidences[this.index] = dist[max_index];		// w_j = dist[v] = p(y_j == v)
			x.setValue(this.index,max_index);			// y_j = 0

			//carry on
			if (next!=null) next.classify(x);
		}

		// Greedy Sampling, at the end of the chain the classification is in 'x'.
		protected void sample(Instance x, Random r) throws Exception {

			// cut out irrelevant attributes
			Instance copy = CCUtils.linkTransformation(x,this.exl,this._template);

			// the pmf for the K classes, e.g. pmf( {1,2,3} ) = [0.3,0.3,0.4] ; pdf( {0,1} ) = [0.1,0.9]
			double dist[] = this.classifier.distributionForInstance(copy);  
			// the chosen value k \in {1,2,3}, e.g. 3 ; k \in {0,1}, e.g. 1
			int k = A.rndsrc(dist,r);	
			// dist[k]
			confidences[this.index] = dist[k];		// w_j = dist[v] = p(y_j == v)
			x.setValue(this.index,k);			// y_j = 0

			//carry on
			if (next!=null) next.sample(x,r);
		}

		/**
		 * Map - return the index of parent ('k') at the ('index')-th node
		 * @param	k		index
		 * @param	keep	all the indicies (we are keeping)
		 * @param	L		number of indices in total
		 * @return	the 	map
		 */
		private int[] makeMap(int k, int keep[], int L) {


			//int keep[] = MLUtils.invert(excl,L); 	// list of parents
			//keep = A.add(keep,this.index);		// keep all parents and self!
			Arrays.sort(keep);
			int map[] = new int[L];
			for(int j = 0; j < L; j++) {
				map[j] = Arrays.binarySearch(keep,j);
			}
			//System.out.println("  map: "+Arrays.toString(map));
			return map;
		}

		public Instance sample(Instance x[], Random r, double vals[]) throws Exception {
			int L = vals.length;

			//System.out.println("exl = "+Arrays.toString(this.exl));

			// WE WILL HAVE TO FILL IN ALL THE INDICES HERE FOR X[i+1]
			for(int k : pa) {
				// value
				double v = vals[k];
				// index (here) where to put 'v'
				int k_ = map[k];
				// set
				x[this.index].setValue(k_,v);			// y_j = 0
			}
			int k_ = map[this.index];
			x[this.index].setMissing(k_);			// y_j = 0

			//System.out.println("x = "+x[this.index]);

			// MAKE SURE TO SELECT THE CORRECT INDEX HERE 
			double dist[] = this.classifier.distributionForInstance(x[this.index]);  
			int k = A.rndsrc(dist,r);	
			// dist[k]
			confidences[this.index] = dist[k];		// w_j = dist[v] = p(y_j == v)
			// cfication
			vals[this.index] = k;
			x[this.index].setValue(this.index,(double)k);			// y_j = 0

			//carry on
			if (next!=null) return next.sample(x,r,vals);
			else return x[this.index];
		}
		protected void transform(Instance x, Instance x_copy[]) throws Exception {
			//System.out.println("i = "+this.index);
			//System.out.println("j = "+this.j);
			x_copy[this.index] = CCUtils.linkTransformation(x,this.exl,this._template);

			if (next!=null) next.transform(x,x_copy);
		}

		// Probability of y|x (y is set into 'x' here), set into 'confidences'.
		protected void probability(Instance x) throws Exception {

			// cut out irrelevant attributes
			Instance copy = CCUtils.linkTransformation(x,this.exl,this._template);

			// classify
			int v = (int)Math.round(x.value(this.index));					// v = path[this.index] = 0
			double dist[] = this.classifier.distributionForInstance(copy);  // 
			confidences[this.index] = dist[v];								// w_j = dist[v] = p(y_j == v)
			//y[this.index] = Utils.maxIndex(dist);							// y_j = max_index(dist)     <-- THE CLASSIFICATION!!!
			 																//       (we used to pass this along as next.probability(x,y))
			//carry on
			if (next!=null) next.probability(x);
		}

		public String toString() {
			return (next == null) ? String.valueOf(this.index) : String.valueOf(this.index)+">"+next.toString();
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

		int indices[] = getChain();
		if (indices == null) {
			indices = MLUtils.gen_indices(L);
			MLUtils.randomize(indices,new Random(m_S));
			setChain(indices);
		}
		if(getDebug()) System.out.print(":- Chain (");
		root = new meka.classifiers.multilabel.CCe.Link(indices,0,D);
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
		root.classify(x);
		return MLUtils.toDoubleArray(x);
	}

	/**
	 * SampleForInstance.
	 * predict y[j] stochastically rather than deterministically (as with distributionForInstance(Instance x)).
	 * @param	x	test Instance
	 * @param	r	Random 
	 */
	public double[] sampleForInstance(Instance x, Random r) throws Exception {
		int L = x.classIndex();
		//System.out.println("CHAIN : "+Arrays.toString(this.getChain()));
		//System.out.print("est: "+Arrays.toString(MLUtils.toDoubleArray(x))+" @ ");
		//System.out.println(Arrays.toString(confidences));
		root.sample(x,r);
		return MLUtils.toDoubleArray(x);
	}
	// fast version, templates already prepared
	public double[] sampleForInstance(Instance x[], Random r) throws Exception {
		/*
		for(int i = 0; i < x.length; i++) {
			System.out.println("x["+i+"] = "+x[i]);
		}
		*/
		//int L = x[0].classIndex();
		int L = x.length;
		double vals[] = new double[L];
		// CHECK THIS FUNCTION IF NOT WORKING
		Instance res = root.sample(x,r,vals);
		// MAKE SURE TO SELECT THE COORECT INDEX HERE
		/*
		System.out.println("est: "+Arrays.toString(MLUtils.toDoubleArray(res,L)));
		System.out.print("est: "+Arrays.toString(vals)+" @ ");
		*/
		//System.out.println(Arrays.toString(confidences));
		//return confidences; //MLUtils.toDoubleArray(x[x.length-1]);
		return MLUtils.toDoubleArray(res,L);
	}

	/*
	 * *NEW*
	 * transform CC
	 */
	public Instance[] transformInstance(Instance x) throws Exception {
		//System.out.println("CHAIN : "+Arrays.toString(this.getChain()));
		int L = x.classIndex();
		Instance x_copy[] = new Instance[L];
		root.transform(x,x_copy);
		return x_copy;
	}

	/**
	 * ProbabilityForInstance - P(y|x).
	 * Force our way down 'path' (y). For example p (y=1010|x) = [0.9,0.8,0.1,0.2]
	 * @param	x		test Instance
	 * @param	path	the path we want to go down
	 * @return	The probabilities associated with this path
	 */
	public double[] probabilityForInstance(Instance x, double path[]) throws Exception {
		int L = x.classIndex();
		// set path 'path' into 'x'
		CCUtils.setPath(x,path);
		// run through chain
		root.probability(x);
		// return p(path|x)
		return getConfidences();
	}

	/**
	 * ProbabilityForInstance.
	 * Force our way down 'path' where 'path' = x[0],...,x[L] (set into x before calling this function).
	 * @param	x		test Instance
	 */
	public double[] probabilityForInstance(Instance x) throws Exception {
		// run through chain
		root.probability(x);
		// return p(path|x)
		return getConfidences();
	}
	
	/**
	 * Rebuild.
	 * For efficiency reasons, we may want to rebuild part of the chain (which differs with nchain).
	 * If chain[] = [1,2,3,4] and nchain[] = [1,2,4,3] we only need to rebuild the final two links.
	 * @note This function does a lot more than necessary, but I was looking into improving CC at some point.
	 * @param	nchain	the new chain
	 * @param	D		the original training data
	 */
	public void rebuildClassifier(int nchain[], Instances D) throws Exception {

		int chain[] = getChain();
		int L = chain.length;

		System.out.println("we have a chain: "+Arrays.toString(chain));
		System.out.println("rebuild ------>: "+Arrays.toString(nchain));

		if (root == null) {
			System.out.println("Not built yet ...");
			return;
		}

		//Link link = root;
		root.rebuildLink(nchain,0,D);

		setChain(nchain);

		confidences = new double[L];
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CCe(),args);
	}
}
