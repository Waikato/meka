package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * RTUpdateable.java - Updateable RT.
 * Must be given an UpdateableClassifier base classifier.
 * @see RT.java
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 * @version October, 2011
 */
public class RTUpdateable extends RT implements UpdateableClassifier {

	@Override
	public String globalInfo() {
		return "Updateable RT\nMust be run with an Updateable base classifier.";
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {

		int L = x.classIndex();

		for (int j = 0; j < L; j++) {
			if(x.value(j) > 0.0) {
				Instance x_j = convertInstance(x);
				x_j.setClassValue((double)j);
				((UpdateableClassifier)m_Classifier).updateClassifier(x_j);
			}
		}
	}

	public static void main(String args[]) {
		WindowIncrementalEvaluator.evaluation(new RTUpdateable(),args);
	}

}
