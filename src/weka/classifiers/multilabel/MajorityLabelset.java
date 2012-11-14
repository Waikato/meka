package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * MajorityLabelset.
 * 
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 * @version October 2011
 */
public class MajorityLabelset extends MultilabelClassifier {

	protected double prediction[] = null;
	protected HashMap<String,Double> classFreqs = new HashMap<String,Double>();

	protected double maxValue = 0.0;

	protected void updateCount(Instance x, int L) {
		String y = MLUtils.toBitString(x,L);

		if (classFreqs.containsKey(y)) {
			double freq = classFreqs.get(y)+x.weight();
			classFreqs.put(y, freq);
			if (maxValue < freq) {
				maxValue = freq;
				this.prediction = MLUtils.fromBitString(y);
			}
		} else {
			classFreqs.put(y, x.weight());
		}
	}

	/**
	 * Build Classifier.
	 */
	public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();
		this.prediction = new double[L];

		for(int i = 0; i < D.numInstances(); i++) {
			updateCount(D.instance(i),L);
		}

	}

	public double[] distributionForInstance(Instance test) throws Exception {
		return prediction;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new MajorityLabelset(),args);
	}

}
