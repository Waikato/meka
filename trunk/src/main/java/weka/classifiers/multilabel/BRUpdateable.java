package weka.classifiers.multilabel;

/**
 * BRUpdateable.java - Updateable BR.
 * 
 * The Binary Relevance Method Updateable (must be given an UpdateableClassifier base classifier)
 * @see BR.java
 * @author 		Jesse Read (jesse@tsc.uc3m.es)
 * @version 	September, 2011
 */
import weka.classifiers.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import weka.core.*;
import java.util.*;

public class BRUpdateable extends BR implements UpdateableClassifier {

	@Override
	public String globalInfo() {
		return "Updateable BR\nMust be run with an Updateable base classifier.";
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {

		int L = x.classIndex();
		// turn x into [x_1,...,x_L]

		if(getDebug()) System.out.print("-: Updating "+L+" models");

		for(int j = 0; j < m_MultiClassifiers.length; j++) {
			Instance x_j = (Instance)x.copy();
			x_j.setDataset(null);
			x_j = MLUtils.keepAttributesAt(x_j,new int[]{j},L);
			x_j.setDataset(m_InstancesTemplate);
			((UpdateableClassifier)m_MultiClassifiers[j]).updateClassifier(x_j);
		}

		if(getDebug()) System.out.println(":- ");
	}


	public static void main(String args[]) {
		WindowIncrementalEvaluator.evaluation(new BRUpdateable(),args);
	}

}
