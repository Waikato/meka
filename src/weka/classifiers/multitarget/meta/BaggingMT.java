package weka.classifiers.multitarget.meta;

import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.multilabel.meta.*;
import weka.classifiers.multilabel.*;
import weka.classifiers.multitarget.*;
import java.util.*;

/**
 * The Multi-Target Version of FastBaggingML.
 * It takes votes using the confidence outputs of the base classifier.
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version	March 2012
 */

public class BaggingMT extends BaggingML implements MultiTargetClassifier {

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		HashMap<Integer,Double> votes[] = new HashMap[L];
		for(int j = 0; j < L; j++) {
			votes[j] = new HashMap<Integer,Double>();
		}

		double y[] = new double[L];

		for(int m = 0; m < m_NumIterations; m++) {
			double c[] = ((MultilabelClassifier)m_Classifiers[m]).distributionForInstance(x);
			// votes[j] = votes[j] + P(j|x)		@TODO: only if c.length > L
			for(int j = 0; j < L; j++) {
				Double w = votes[j].containsKey((int)c[j]) ? votes[j].get((int)c[j]) + c[j+L] : c[j+L];
				votes[j].put((int)c[j] , w);
			}
		}

		for(int j = 0; j < L; j++) {
			// get the class with max weight
			y[j] = (Integer)MLUtils.maxItem(votes[j]);
		}

		return y;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new BaggingMT(),args);
	}

}
