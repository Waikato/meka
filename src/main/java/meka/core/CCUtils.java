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

package meka.core;

import meka.classifiers.multilabel.CC;
import weka.classifiers.Classifier;
import weka.core.*;
import java.util.*;

/**
 * CCUtils.java - Handy Utils for working with Classifier Chains (and Trees and Graphs)
 * @author Jesse Read 
 * @version June 2014
 */
public abstract class CCUtils {

	/** 
	 * BuildCC - Given a base classifier 'g', build a new CC classifier on data D, given chain order 'chain'.
	 */
	public static CC buildCC(int chain[], Instances D, Classifier g) throws Exception {

		// a new classifier chain
		CC h = new CC();

		// build this chain
		h.setChain(chain);
		h.setClassifier(g);
		h.buildClassifier(new Instances(D));
		return h;
	}

	/*
			 * Use this code to try ALL possible combinations
			 *
	public static int[] ChainSearchOptimal(Instances D_train, Instances D_test, Classifier c) {
		String perms[] = MLUtils.permute(MLUtils.toBitString(MLUtils.gen_indices(L)));
		for (int t = 0; t < perms.length; t++) {
			int s_[] = A.string2IntArray(perms[t]);
			System.out.println("proposing s' = perm["+t+"] = "+Arrays.toString(s_));
		}
	}
	*/

	/*
	 * RandomSearch -  Basically Simulated Annealing without temperature, starting from y0[].
	public static double[] RandomSearch(CC h, Instance x, int T, Random r, double y0[]) throws Exception {

		double y[] = Arrays.copyOf(y0,y0.length); 				// prior y
		double w  = A.product(h.probabilityForInstance(x,y));	// p(y|x)

		Instance t_[] = h.getTransformTemplates(x);

		//System.out.println("----");
		//System.out.println("p0("+Arrays.toString(y)+") = "+Arrays.toString(h.getConfidences())+", w="+w);
		for(int t = 0; t < T; t++) {
			double y_[] = h.sampleForInstanceFast(t_,r); 	    // propose y' by sampling i.i.d.
			double p_[] = h.getConfidences();                   //
			double w_  = A.product(h.getConfidences()); 		// rate y' as w'  --- @TODO allow for command-line option
			//System.out.println("p("+Arrays.toString(y_)+") = "+Arrays.toString(p_)+", w="+w_);
			if (w_ > w) { 										// accept ? 
				//if (getDebug()) System.out.println("y' = "+Arrays.toString(y_)+", :"+w_);
				w = w_;
				y = y_;
				//System.out.println("* ACCEPT *");
			}
		}
		return y;
	}
	*/

	/*
	// An old slower version of the above
	@Deprecated
	public static double[] RandomSearchOLD(CC h, Instance x, int T, Random r, double y0[]) throws Exception {

		double y[] = Arrays.copyOf(y0,y0.length); 				// prior y
		double w  = A.product(h.probabilityForInstance(x,y));	// p(y|x)
		//System.out.println("y0="+Arrays.toString(y)+"\tw0="+w);

		//if (getDebug()) System.out.println(" y0 = "+MLUtils.toBitString(y) +" w0 = "+w);
		y = Arrays.copyOf(y,y.length);
		for(int t = 0; t < T; t++) {
			// propose y' by sampling i.i.d.
			double y_[] = h.sampleForInstance(x,r); 	       
			// rate y' as w'
			double w_  = A.product(h.getConfidences());
			//System.out.println("y_="+Arrays.toString(y_)+"\tw_="+w_);
			// accept ? 
			if (w_ > w) {
				w = w_;
				y = y_;
				//if (getDebug()) System.out.println(" y' = "+MLUtils.toBitString(y_) +" w' = "+w_+ " (accept!)"); 
			}
		}
		return y;
	}
	*/

	/*
	 * RandomSearch - Basically Simulated Annealing without temperature.
	public static double[] RandomSearch(CC h, Instance x, int T, Random r) throws Exception {

		return RandomSearch(h,x,T,r,h.distributionForInstance(x));
	}
	*/

	/**
	 * SetPath - set 'path[]' into the first L attributes of Instance 'xy'.
	 * @param	xy		an Example (x,y)
	 * @param	path	a label vector
	 */
	public static void setPath(Instance xy, double path[]) {
		int L = xy.classIndex(); // = path.length
		for(int j = 0; j < L; j++) {
			xy.setValue(j,(int)Math.round(path[j])); 	 // x = x + path_j
		}
	}

	/**
	 * LinkTransform - prepare 'D' for training at a node 'j' of the chain, by excluding 'exl'.
	 * @param	D		dataset
	 * @param	j		index of the label of this node
	 * @param	exl		indices of labels which are NOT parents of j
	 * @return	the transformed dataset (which can be used as a template)
	 */
	public static Instances linkTransform(Instances D, int j, int exl[]) {
		Instances D_j = new Instances(D);
		D_j.setClassIndex(-1); 
		// delete all the attributes (and track where our index ends up)
		int ndx = j;
		for(int i = exl.length-1; i >= 0; i--) {
			D_j.deleteAttributeAt(exl[i]);
			if (exl[i] < ndx)
				ndx--; 
		}
		D_j.setClassIndex(ndx); 
		return D_j;
	}

	/**
	 * LinkTransform - prepare 'x' for testing at a node 'j' of the chain, by excluding 'exl'.
	 * @param	x		instance
	 * @param	exl		indices of labels which are NOT parents of j
	 * @param	_D		the dataset template to use
	 * @return	the transformed instance
	 */
	public static Instance linkTransformation(Instance x, int excl[], Instances _D) {
		// copy
		Instance copy = (Instance)x.copy();
		copy.setDataset(null);

		// delete attributes we don't need
		for(int i = excl.length-1; i >= 0; i--) {
			copy.deleteAttributeAt(excl[i]);
		}

		//set template
		copy.setDataset(_D);

		return copy;
	}

}
