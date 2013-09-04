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

import meka.classifiers.multilabel.NN.*;
import weka.core.*;
import meka.core.*;
import rbms.M;
import java.util.*;
import Jama.Matrix;

/**
 * BPNN.java - Back Propagation Neural Network.
 * This is a standard back-propagated Neural Network with multiple outputs that correspond to multiple labels.
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version March 2013
*/

public class BPNN extends AbstractNeuralNet {

	protected Random r = null;
	public Matrix W[] = null;
	protected Matrix dW_[] = null;

	public BPNN() {
		// different default for now
		m_E = 100;			
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);

		double X_[][] = MLUtils.getXfromD(D);
		double Y_[][] = MLUtils.getYfromD(D);

		// @todo, parameterize this
		int h[] = new int[]{m_H};

		init(X_,Y_,h); 
		train(X_,Y_,m_E);
	}

	@Override
	public double[] distributionForInstance(Instance xy) throws Exception {
		double x[] = MLUtils.getxfromInstance(xy);
		return popy(x);
	}

	/**
	 * Init - Initialize a BPNN with input X, output Y, and weight matrices W.
	 * You may initilise W outside this class, but 
	 * 		W.length must = number of hidden layers; and 
	 * 		W.getRowDimension(), W.getColumnDimension() must correspond correctly.
	 */
	public void init(double[][] X_, double[][] Y_, Matrix W[]) throws Exception {

		r = new Random(0);

		this.W = W;								// weights

		this.dW_ = new Matrix[W.length];		// weight deltas

		for(int i = 0; i < this.dW_.length; i++) {
			this.dW_[i] = new Matrix(W[i].getRowDimension(),W[i].getColumnDimension(),0.0);
		}

	}

	/**
	 * Init - Initialize a BPNN with input X, output Y, to be of H.length hidden layers.
	 * W will be initialised for you randomly.
	 */
	public void init(double[][] X_, double[][] Y_, int H[]) throws Exception {

		int L = Y_[0].length;
		int d = X_[0].length;

		if (getDebug()) {
			System.out.println("Initializing "+(H.length)+" hidden Layers ...");
			System.out.println("d = "+d);
			System.out.println("L = "+L);
		}

		Matrix W[] = new Matrix[H.length+1];

		for(int n = 0; n < H.length; n++) {
			int h = H[n];
			if (getDebug()) System.out.println("W["+n+"] = "+(d+1)+" x "+h);
			W[n] 	 = Matrix.random(d+1,h).plusEquals(new Matrix(d+1,h,-0.5)).timesEquals(0.1);
			d = h;
		}
		W[H.length] = Matrix.random(d+1,L).plusEquals(new Matrix(d+1,L,-0.5)).timesEquals(0.1);
		if (getDebug()) System.out.println("W["+H.length+"] = "+(d+1)+" x "+L);

		init(X_, Y_, W); 

	}

	public double train(double X_[][], double Y_[][]) throws Exception {
		return train(X_,Y_,m_E);
	}

	/**
	 * Train - Train for I iterations.
	 * I is not necessarily m_E (yet)!
	 */
	public double train(double[][] X_, double[][] Y_, int I) throws Exception {
		if (getDebug()) {
			System.out.println("BPNN train; For "+I+" epochs ...");
		}
		int N = X_.length;
		boolean breakEarly = (I < 0) ? true : false;
		I = Math.abs(I);
		double E_ = Double.MAX_VALUE;
		double E = 0.0;
		for(int e = 0; e < I; e++) {
			E = update(X_,Y_);
			if (breakEarly && E > E_) {
				if (getDebug()) System.out.println(" early stopped at epcho "+e+" ... ");
				break; 	// positive gradient
			}
			E_ = E;
		}
		if (getDebug()) System.out.println("Done.");
		return E;
	}

	/**
	 * Train - Train for 1 iterations.
	public double update(double x_[], double y_[]) throws Exception {
		return update(new double[][]{x_},new double[][]{y_});
	}
	 */

	/**
	 * Train - Train for 1 epoch.
	 */
	public double update(double X_[][], double Y_[][]) throws Exception {
		int N = X_.length;
		double E = 0.0;
		for(int i = 0; i < N; i++) {
			E += this.backPropagate(new double[][]{X_[i]},new double[][]{Y_[i]});
		}
		return E;
	}

	/*
	// @todo need this function ? 
	// like popy but a stochastic selection
	public double[] geny(double x_[]) {
		return genY(new double[][]{x_})[0];
	}

	// @todo need this function ? 
	// like popY but a stochastic selection
	public double[][] genY(double X_[][]) {
		Matrix Z[] = forwardPass(X_);
		int n = Z.length-1;
		double Z_[][] = Z[n].copy().getArray();
		return M.threshold(Z_,r);
	}
	*/

	/**
	 * Forward Pass - Given input x_, get output y_.
	 * @param	y_	input
	 * @return  y_	output
	 */
	public double[] popy(double x_[]) {
		return popY(new double[][]{x_})[0];
	}

	/**
	 * Forward Pass - Given input X_, get output Y_.
	 * @param	X_	input
	 * @return  Y_	output
	 */
	public double[][] popY(double X_[][]) {
		Matrix Z[] = forwardPass(X_);
		int n = Z.length-1;
		return Z[n].getArray();
	}

	/**
	 * Forward Pass - Given input X_, get output of all layers Z[0]...
	 * @param	X_	input
	 * @return  Y 	output
	 */
	public Matrix[] forwardPass(double X_[][]) {

		int nW = W.length; // number of weight matrices
		Matrix Z[] = new Matrix[nW+1];

		// input activations
		Z[0] = new Matrix(M.addBias(X_));

		// hidden layer(s)
		for(int i = 1; i < Z.length; i++) {
			Matrix A_z = Z[i-1].times(W[i-1]);									// 					A = X * W1 		= Z[n-1] * W[n-1]	 
			Z[i] = M.sigma(A_z);
			Z[i] = M.addBias(Z[i]);											// ACTIVATIONS      Z[n] = sigma(A)	=  
		}

		// output layer
		Matrix A_y = Z[nW-1].times(W[nW-1]);			// 					A = X * W1 		= Z[n-1] * W[n-1]	 
		Z[nW] = M.sigma(A_y);					// ACTIVATIONS      Z[n] = sigma(A)	=  

		return Z;
	}

	/**
	 * Back Propagate - Do one round of Back Propagation on batch X_,Y_.
	 * @param	X_	input
	 * @param	Y_	teacher values
	 */
	public double backPropagate(double[][] X_, double[][] Y_) throws Exception {

		int N = X_.length;				// batch size
		int L = Y_[0].length;			// num. of labels
		int nW = W.length;				// num. of weight matrices

		Matrix T = new Matrix(Y_);					// TARGETS

		/*
		   1. FORWARD PROPAGATION. 
		   Forward-propagate X through the neural net to produce Z_1, Z_2, ..., Y.
		 */

		//Matrix X = new Matrix(M.addBias(X_));		// INPUT 
		Matrix Z[] = forwardPass(X_);				// ALL LAYERS

		/*
		   2. BACKWARD PROPAGATION. 
		   Propagate the errors backward through the neural net.
		 */

		Matrix dZ[] = new Matrix[nW+1]; // *new*

		// Error terms (output)
		Matrix E_y = T.minus(Z[nW]);												// ERROR

		dZ[nW] = M.dsigma(Z[nW]).arrayTimes(E_y);

		// Error terms (hidden) *NEW*
		for(int i = nW-1; i > 0; i--) {
			Matrix E = dZ[i+1].times(W[i].transpose());
			dZ[i] = M.dsigma(Z[i]).arrayTimes(E); 
			dZ[i] = new Matrix(M.removeBias(dZ[i].getArray()));
		}

		// Error terms (hidden)
		//Matrix E_z = dY.times(W[1].transpose());
		//Matrix dZ = M.dsigma(Z[1]).arrayTimes(E_z); 
		//dZ = new Matrix(M.removeBias(dZ.getArray()));

		// Weight derivatives
		Matrix dW[] = new Matrix[nW];
		for(int i = 0; i < nW; i++) {
			dW[i] = (Z[i].transpose().times(m_R).times(dZ[i+1])) .plus (dW_[i].times(m_M));
		}

		// Weight update
		for(int i = 0; i < nW; i++) {
			//W[i] = W[i].plusEquals(dW[i]);
			W[i].plusEquals(dW[i]);
		}

		// Update momentum records
		dW_ = dW;

		//double SSE = (E_y.transpose().times(E_y)).trace();					// SUM of SQUARE ERROR (faster?)
		double SSE = E_y.normF();												// SQRT of SUM of SQUARE ERROR (not the sqrt is not necessary, thus the following line should also suffice)
		return SSE;
	}

	public static void main(String args[]) throws Exception {
		MultilabelClassifier.evaluation(new BPNN(),args);
	}
}
