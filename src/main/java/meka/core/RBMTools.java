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

import rbms.*;
import weka.core.*;
import weka.classifiers.multilabel.*;
import java.util.*;

public abstract class RBMTools {

	/**
	 * TuneRBM - Tune an RBM.
	 * 1. make a 5:1 split of the training data
	 * 2. using batches of 10, or L, tune LEARNING_RATE, MOMENTUM, COST @ 30 epochs / batch
	 * 		- labels distributed as evenly as possible
	 * L. now update the RBM with the validation split.
	 * OR: retrain on the full set until the error is the same
	 * ?. decrease the learning rate
	 * ?. do a few final passes with everything
	 *
	 * @todo	include Y
	 * @note	uses a certain 'train' routine (not necassaryily with batch)
	 */
	public static String[] tuneRBM(MultilabelClassifier h, Instances D) {
		int L = D.classIndex();
		int d = D.numAttributes() - L;
		// 1. make 5:1 split
		int N = D.numInstances();
		int N_train = N*4/5;
		D.randomize(D.getRandomNumberGenerator(0)); // <-- RANDOMIZE (if not i.i.d. like SCENE !!)
		Instances D_train = new Instances(D,0,N_train);
		Instances D_test  = new Instances(D,N_train,N-N_train);
		// 2. for each parameter combination, train the RBM for 30 epochs / batch
		//int HiddenUnits[] = new int[]{20,30,40,80}; // + 120?
		int HiddenUnits[] = new int[]{30,60,128,240}; // + 120?
		double LearningRate[] = new double[]{0.1, 0.01, 0.001};
		double Momentum[] = new double[]{0.2, 0.4, 0.8};
		//double Momentum[] = new double[]{0.2, 0.4, 0.6, 0.8};
		RBM rbm;

		double score = 0.0;
		String combo[] = new String[]{};

		for (int H : HiddenUnits) { // @todo, not sure if we need to tune this 
			for (double m : Momentum) {
				for (double r : LearningRate) {
					rbm = new RBM();
					rbm.setH(H);
					rbm.setE(100);
					rbm.setLearningRate(r);
					rbm.setMomentum(m);
					try {
						rbm.train(MLUtils.getXfromD(D_train));
						Instances ZY_train = MLUtils.replaceZasAttributes(new Instances(D_train),rbm.getZ(MLUtils.getXfromD(D_train)),L);
						Instances ZY_test = MLUtils.replaceZasAttributes(new Instances(D_test),rbm.getZ(MLUtils.getXfromD(D_test)),L);
						Result result = Evaluation.evaluateModel(h,ZY_train,ZY_test,"PCut1"); 
						if (result.output.get("Accuracy") > score) {
							score = result.output.get("Accuracy");
							combo = rbm.getOptions();
							System.out.println("ACCURACY [h="+H+",m="+m+",l="+r+"]: "+result.output.get("Accuracy"));
						}
					} catch(Exception e) {
						System.err.println(" Some kind of error. ");
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
		}
		return combo;
	}

	/**
	 * TrainRBM - Train an RBM.
	 * 1.	tune params (above)
	 * 2.	retrain on full set until error at least as low as that obtained during parameter tuning above
	 */
	public static void trainRBM(MultilabelClassifier h, Instances D, HashMap<String,Double> params) {
		// train everything
	}

	/**
	 * TrainRBM - Train an RBM with a curriculum.
	 * 1.	tune params (above)
	 * 2. 	do 5xCV ... each instance has a score averaged across 4 runs.
	 * 3. 	use info about easiness to order the microbatches of L instances
	 * 4. 	train first batch on all epochs, second batch on all epochs, etc
	 * 2.	retrain on full set until error at least as low as that obtained during parameter tuning above
	 *
	 */
	public static void trainRBM(MultilabelClassifier h, Instances D, HashMap<String,Double> params, String Eval) {
		// 
	}
}
