package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * MajorityLabelsetUpdateable.java - Updateable MajorityLabelset.
 * @see MajorityLabelset.java
 * @author 		Jesse Read (jesse@tsc.uc3m.es)
 * @version 	September, 2011
 */
public class MajorityLabelsetUpdateable extends MajorityLabelset implements UpdateableClassifier {

	@Override
	public String globalInfo() {
		return "Updateable Majority Labelset Classifier";
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {
		int L = x.classIndex();
		super.updateCount(x,L);
	}

	public static void main(String args[]) {
		WindowIncrementalEvaluator.evaluation(new MajorityLabelsetUpdateable(),args);
	}

}
