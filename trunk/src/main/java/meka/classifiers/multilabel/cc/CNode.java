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

package meka.classifiers.multilabel.cc;

import weka.classifiers.*;
import weka.classifiers.functions.*; // @temp, for tests
import weka.core.*;
import meka.core.A;
import meka.core.F;
import meka.core.MLUtils;
import java.util.*;
import java.io.*; // @temp, for tests
import java.io.Serializable;

/**
 * CNode.java - A Classifier Node class (for CC-like clasifiers).
 * @author	Jesse Read
 * @version June 2013
 */
public class CNode implements Serializable {

	private int j = -1;
	private int d = -1;
	private int inX[] = null;
	private int paY[] = null;
	private Instances T = null;
	private Instance t_ = null;
	private Classifier h = null;
	private int map[] = null;

	/**
	 * CNode - A Node 'j', taking inputs from all parents inX and paY.
	 * @param	j		the label index of this node
	 * @param	inX		attribute indices going into this node
	 * @param	paX		label indices going into this node
	 */
	public CNode(int j, int inX[], int paY[]) {
		this.j = j;
		this.inX = inX;
		this.paY = paY; 
	}

	/** getParentsY - get the parents (indices) of this node */
	public int[] getParentsY() {
		return paY;
	}

	/**
	 * Transform - transform dataset D for this node.
	 * 0. Get, e.g.,                                        paY = [1,4]
	 * 1. remove everything except parents and self!, e.g., keep = [1,4,3]
	 * @todo: keep a handy template of the data for 0 parents, 1 parent, 2 parents ??
	 */
	public Instances transform(Instances D) throws Exception {
		int L = D.classIndex();
		d = D.numAttributes() - L;
		int keep[] = A.append(this.paY,j);		// keep all parents and self!
		Arrays.sort(keep);
		int remv[] = A.invert(keep,L); 	// i.e., remove the rest < L
		Arrays.sort(remv);
		map = new int[L];
		for(int j = 0; j < L; j++) {
			map[j] = Arrays.binarySearch(keep,j);
		}
		Instances D_ = F.remove(new Instances(D),remv, false); 
		D_.setClassIndex(map[this.j]);
		return D_;
	}

	/**
	 * Build - Create dataset D_j, and train a 'hTemplate'-type classifier on it.
	 * The dataset should have class as index 'j', and remove all indices less than L *not* in paY.
	 */
	public void build(Instances D, Classifier H) throws Exception {
		// transform data
		T = transform(D);
		// build SLC 'h'
		h = AbstractClassifier.makeCopy(H);
		h.buildClassifier(T);
		// save templates
		//t_ = new SparseInstance(T.numAttributes());
		//t_.setDataset(T);
		//t_.setClassMissing();								// [?,x,x,x]
		T.clear();
	}

	/**
	 * p( y_j = 1 | x , y_pred )
	 */
	public double[] distribution(Instance x, double ypred[]) throws Exception {
		Instance x_ = transform(x,ypred);
		return h.distributionForInstance(x_);
	}

	/** distributionT - same as distribution(Instance, double[]), but the Instance is pre-transformed. */
	public double[] distributionT(Instance x_) throws Exception {
		return h.distributionForInstance(x_);
	}

	/**
	 * y_j ~ p( y_i | x , y_pred )
	 */
	public double sample(Instance x, double ypred[], Random r) throws Exception {
		double p[] = distribution(x, ypred);
		return A.samplePMF(p,r);
	}

	/**
	 * Transform - turn [y1,y2,y3,x1,x2] into [y1,y2,x1,x2].
	 */
	public Instance transform(Instance x, double ypred[]) throws Exception {
		x = (Instance)x.copy();
		int L = x.classIndex();
		int L_c = (paY.length + 1);
		x.setDataset(null);
		for(int j = 0; j < (L - L_c); j++) {
			x.deleteAttributeAt(0);
		}
		for(int pa : paY) {
			//System.out.println("x_["+map[pa]+"] <- "+ypred[pa]);
			x.setValue(map[pa],ypred[pa]);
		}
		x.setDataset(T);
		x.setClassMissing();
		return x;
	}

	/*
	public Instance transformOLD(Instance x, double ypred[]) throws Exception {

		//if (t_ != null) return t_;

		int L = x.classIndex();
		double array[] = x.toDoubleArray();  				// [y,c,y,x,x,x]
		double array_[] = new double[d + paY.length + 1];	// [_,_,_,_]
		for(int pa : paY) {
			//System.out.println("x_["+map[pa]+"] <- "+ypred[pa]);
			array_[map[pa]] = ypred[pa];
		}
		System.arraycopy(array,L,array_,paY.length+1,d);	// [_,x,x,x]
		Instance x_ = new SparseInstance(1.,array_);
		x_.setDataset(T);
		x_.setClassMissing();								// [?,x,x,x]

		//t_ = x_;
		return x_;
	}
	*/

	/* 
	 * @TODO	I thought this would be faster, but apparenty not
	public Instance transform(Instance x, double ypred[]) throws Exception {
		int L = x.classIndex();
		int L_ = paY.length + 1;
		t_ = MLUtils.copyValues(t_,x,L,L_);
		for(int pa : paY) {
			t_.setValue(map[pa],ypred[pa]);
		}
		//x_.setDataset(T);
		t_.setClassMissing();
		//System.out.println("x_ = "+MLUtils.toBitString(x_,L_));
		return t_;
	}
	*/

	/**
	 * y_i = argmax_{y_i = 0,1,...} p( y_i | x , y_pred )
	 */
	public double classify(Instance x, double ypred[]) throws Exception {
		Instance x_ = transform(x,ypred);
		/*
		int L = x.classIndex();
		double array[] = x.toDoubleArray();  				// [y,c,y,x,x,x]
		double array_[] = new double[d + paY.length + 1];	// [_,_,_,_]
		System.arraycopy(array,L,array_,paY.length+1,d);	// [_,x,x,x]
		DenseInstance x_ = new DenseInstance(1,array_);
		x_.setDataset(T);
		x_.setClassMissing();								// [?,x,x,x]
		*/
		//Instance x_ = this.transform(x, ypred);
		//System.out.println("we want to classify: "+x_+" at node "+j+ " -> "+Arrays.toString(h.distributionForInstance(x_)));
		//for(int i = 0; i < 10; i++) {
		//	System.out.println("i_"+i+" = "+Arrays.toString(h.distributionForInstance(x_)));
		//}
		//System.out.println(""+Arrays.toString(h.distributionForInstance(T.firstInstance())));
		return Utils.maxIndex(h.distributionForInstance(x_));
	}

	/**
	 * Transform.
	 * @param	D		original Instances
	 * @param	c		to be the class Attribute
	 * @param	pa_c	the parent indices of c
	 * @return	new Instances T
	 */
	public static Instances transform(Instances D, int c, int pa_c[]) throws Exception {
		int L = D.classIndex();
		int keep[] = A.append(pa_c,c);			// keep all parents and self!
		Arrays.sort(keep);
		int remv[] = A.invert(keep,L); 	// i.e., remove the rest < L
		Arrays.sort(remv);
		Instances T = F.remove(new Instances(D),remv, false); 
		int map[] = new int[L];
		for(int j = 0; j < L; j++) {
			map[j] = Arrays.binarySearch(keep,j);
		}
		T.setClassIndex(map[c]);
		return T;
	}

	/**
	 * Main - run some tests.
	 */
	public static void main(String args[]) throws Exception {
		Instances D = new Instances(new FileReader(args[0]));
		Instance x = D.lastInstance();
		D.remove(D.numInstances()-1);
		int L = Integer.parseInt(args[1]);
		D.setClassIndex(L);
		double y[] = new double[L];
		Random r = new Random();
		int s[] = new int[]{1,0,2};
		int PA_J[][] = new int[][]{
			{},{},{0,1},
		};

		//MLUtils.randomize(s,r);
		// MUST GO IN TREE ORDER !!
		for(int j : s) {
			int pa_j[] = PA_J[j];
			System.out.println("PARENTS = "+Arrays.toString(pa_j));
			//MLUtils.randomize(pa_j,r);
			System.out.println("**** TRAINING ***");
			CNode n = new CNode(j,null,pa_j);
			n.build(D,new SMO());
			/*
			 */
			//Instances D_ = n.transform(D);
			//n.T = D_;
			System.out.println("============== D_"+j+" / class = "+n.T.classIndex()+" =");
			System.out.println(""+n.T);
			System.out.println("**** TESTING ****");
			/*
			Instance x_ = MLUtils.setTemplate(x,(Instance)D_.firstInstance().copy(),D_);
			for(int pa : pa_j) {
				//System.out.println(""+map[pa]);
				x_.setValue(n.map[pa],y[pa]);
			}
			//x_.setDataset(T);
			x_.setClassMissing();
			 */
			//n.T = D_;
			Instance x_ = n.transform(x,y);
			System.out.println(""+x_);
			y[j] = 1;
		}
	}

}

