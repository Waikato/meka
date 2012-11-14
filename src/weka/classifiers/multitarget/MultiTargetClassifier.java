package weka.classifiers.multitarget;

import weka.classifiers.*;
import weka.classifiers.multilabel.*;
import weka.classifiers.meta.*;
import weka.core.*;
import java.util.*;
import java.text.*;

/**
 *  Multi-target Classifier.
 *  To implement this interface, it is also necessary to extend MultilabelClassifier.
 *  Implementing this interface only signals to the Evaluator that we are dealing with multi-target data, 
 *  and a different evaluation output is made. Training and Classification is the same, using the 
 *  methods <i>buildClassifier(Instances)</i> and <i>distributionForInstance(Instance)</i> except that
 *  the latter may return a vector of L*2 doubles instead of L. The extra values are probabalistic 
 *  information that may be used by e.g. ensemble classifiers.
 *
 * 	@author 	Jesse Read (jesse@tsc.uc3m.es)
 * 	@version	January 2012
 */

public interface MultiTargetClassifier {

	/*
	 * Everything is the same as MultilabelClassifier except for the Evaluation
	 */
}
