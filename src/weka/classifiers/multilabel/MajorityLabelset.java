package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * MajorityLabelset.
 * 
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 * @version May 2010
 */
public class MajorityLabelset extends MultilabelClassifier {

	// todo: should be protected

	protected double prediction[] = null;
	protected HashMap<String,Double> classFreqs = new HashMap<String,Double>();

	protected double maxValue = 0.0;
	protected String maxClass = null;

	protected void updateCount(Instance x, int L) {
		String c = MLUtils.toBitString(x,L);

		if (classFreqs.containsKey(c)) {
			double freq = classFreqs.get(c)+x.weight();
			classFreqs.put(c, freq);
			if (maxValue < freq) {
				maxValue = freq;
				maxClass = c;
				this.prediction = MLUtils.fromBitString(maxClass);
			}
		} else {
			classFreqs.put(c, x.weight());
		}
	}

	/**
	 * Build Classifier.
	 */
	public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();
		maxClass = MLUtils.toBitString(-1,L);

		for(int i = 0; i < D.numInstances(); i++) {
			updateCount(D.instance(i),L);
		}

		this.prediction = MLUtils.fromBitString(maxClass);
	}

	public double[] distributionForInstance(Instance test) throws Exception {

		return prediction;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new MajorityLabelset(),args);
	}

}
