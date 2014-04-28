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

import meka.classifiers.multilabel.CCe;
import weka.classifiers.Classifier;
import weka.core.*;
import java.util.*;

public abstract class CCUtils {

	/* 
	 * Build h_{s} : X -> Y
	 */
	public static CCe buildCC(int chain[], Instances D, Classifier g) throws Exception {

		// a new classifier chain
		CCe h = new CCe();

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

	/**
	 * RandomSearch. Basically Simulated Annealing without temperature. Start searching from y0[]
	 */
	public static double[] RandomSearch(CCe h, Instance x, int T, Random r, double y0[]) throws Exception {

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

	/**
	 * RandomSearch. Basically Simulated Annealing without temperature.
	 */
	public static double[] RandomSearch(CCe h, Instance x, int T, Random r) throws Exception {

		return RandomSearch(h,x,T,r,h.distributionForInstance(x));
	}

	/**
	 * SetPath - set 'path[]' into the first L attributos of Instance 'xy'.
	 * @param	xy		an Instance
	 * @param	path	a label vector
	 */
	public static void setPath(Instance xy, double path[]) {
		int L = xy.classIndex(); // = path.length
		for(int j = 0; j < L; j++) {
			xy.setValue(j,(int)Math.round(path[j])); 	 // x = x + path_j
		}
	}

	/**
	 * LinkTransform - prepare 'D' for training at node 'j' of the chain, using index 'idx'.
	 * j isn't necessary in this function!
	 * @return	the transformed dataset (which can be used as a template)
	 */
	public static Instances linkTransform(Instances D, int j, int idx, int exl[]) {
		Instances D_j = new Instances(D);
		D_j.setClassIndex(-1); 
		// delete all the attributes (and track where our index ends up)
		int ndx = idx;
		for(int i = exl.length-1; i >= 0; i--) {
			D_j.deleteAttributeAt(exl[i]);
			if (exl[i] < ndx)
				ndx--; 
		}
		D_j.setClassIndex(ndx); 
		return D_j;
	}

	public static Instance linkTransformation(Instance x, int excl[], Instances _template) {
		// copy
		Instance copy = (Instance)x.copy();
		copy.setDataset(null);

		// delete attributes we don't need
		for(int i = excl.length-1; i >= 0; i--) {
			copy.deleteAttributeAt(excl[i]);
		}

		//set template
		copy.setDataset(_template);

		return copy;
	}

}
