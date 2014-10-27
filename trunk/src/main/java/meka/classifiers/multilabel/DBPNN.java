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

import java.util.*;
import Jama.Matrix;
import rbms.RBM;
import rbms.DBM;
import weka.core.*;
import meka.core.*;
import weka.classifiers.rules.*;
import weka.classifiers.functions.*;
import meka.classifiers.multilabel.NN.*;
import meka.classifiers.multilabel.meta.DeepML;
import meka.classifiers.multilabel.CC;
import meka.classifiers.multilabel.meta.BaggingML;

/**
 * DBPNN.java - Deep Back-Propagation Neural Network.
 * Use an RBM to pre-train the network, then plug in BPNN.
 *
 * @see BPNN
 * @author Jesse Read
 * @version December 2012
 */
public class DBPNN extends AbstractDeepNeuralNet  { 

	protected RBM dbm = null;
	protected long rbm_time = 0;

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);

		// Extract variables

		int L = D.classIndex();
		int d = D.numAttributes()-L;
		double X_[][] = MLUtils.getXfromD(D);
		double Y_[][] = MLUtils.getYfromD(D);

		// Build an RBM
		if (getDebug()) System.out.println("Build RBM(s) ... ");

		dbm = new DBM(this.getOptions());
		dbm.setE(m_E);
		((DBM)dbm).setH(m_H, m_N);

		long before = System.currentTimeMillis();
		dbm.train(X_,m_H); // batch train
		rbm_time = System.currentTimeMillis() - before;

		if (getDebug()) {
			Matrix tW[] = dbm.getWs();
			System.out.println("X = \n"+M.toString(X_));
			for(int l = 0; l < tW.length; l++) {
				System.out.println("W = \n"+M.toString(tW[l].getArray()));
			}
			System.out.println("Y = \n"+M.toString(Y_));
		}

		/* Trim W's: instead of (d+1 x h+1), they become (d+1, h)
		      wwb      ww                                                     
		      wwb      ww                                                     
		      wwb  ->  ww                                                     
		      wwb      ww                                                     
		      bbb                                                             
			  (this is because RBMs go both ways -- have biases both ways -- whereas BP only goes up)
			  TODO the best thing would be to keep different views of the same array ...
		  */
		                                                            
		Matrix W[] = trimBiases(dbm.getWs());

		// Back propagate with batch size of 1 to fine tune the DBM into a supervised DBN
		if (m_Classifier instanceof BPNN) {
			if (getDebug())
				System.out.println("You have chosen to use BPNN (very good!)");
		}
		else {
			System.err.println("[Warning] Was expecting BPNN as the base classifier (will set it now, with default parameters) ...");
			m_Classifier = new BPNN();
		}

		int i_Y = W.length-1; 																		// the final W
		W[i_Y] = RBM.makeW(W[i_Y].getRowDimension()-1,W[i_Y].getColumnDimension()-1,new Random(1)); 	// 
		((BPNN)m_Classifier).setWeights(W,L); // this W will be modified
		((BPNN)m_Classifier).train(X_,Y_);    // could also have called buildClassifier(D)

		/*
		for(int i = 0; i < 1000; i++) {
			double E = ((BPNN)m_Classifier).update(X_,Y_);
			//double Ypred[][] = ((BPNN)m_Classifier).popY(X_);
			System.out.println("i="+i+", MSE="+E);
		}
		*/

		if (getDebug()) {
			Matrix tW[] = W;
			//System.out.println("X = \n"+M.toString(X_));
			System.out.println("W = \n"+M.toString(tW[0].getArray()));
			System.out.println("W = \n"+M.toString(tW[1].getArray()));
			double Ypred[][] = ((BPNN)m_Classifier).popY(X_);
			System.out.println("Y = \n"+M.toString(M.threshold(Ypred,0.5)));
			//System.out.println("Z = \n"+M.toString(M.threshold(Z,0.5)));
		}
	}

	@Override
	public double[] distributionForInstance(Instance xy) throws Exception {
		return m_Classifier.distributionForInstance(xy);
	}

	protected static Matrix trimBiases(Matrix A) {
		double M_[][] = A.getArray();
		return new Matrix(M.removeBias(M_));
		//return new Matrix(M.getArray(),M.getRowDimension(),M.getColumnDimension()-1); // ignore last column
	}

	protected static Matrix[] trimBiases(Matrix M[]) {
		for(int i = 0; i < M.length; i++) {
			M[i] = trimBiases(M[i]);
		}
		return M;
	}

	public static void main(String args[]) throws Exception {
		MultilabelClassifier.evaluation(new DBPNN(),args);
	}

}

