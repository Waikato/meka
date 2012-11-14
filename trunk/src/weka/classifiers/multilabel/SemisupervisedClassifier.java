package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.core.*;
import java.util.*;
import java.text.*;

/**
 *  Multilabel Semisupervised Classifier.
 *  ...
 */

public interface SemisupervisedClassifier {

	void setUnlabelledData(Instances D);

}
