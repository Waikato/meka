package weka.classifiers.multitarget;

/**
 * The Classifier Chains (CC) method.
 * Multi-target version of the BR-based CC method (directly applicable).
 * @see 	weka.classifiers.multilabel.CC
 * @version	Jan 2012
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 */
import weka.classifiers.*;
import weka.classifiers.multilabel.*;
import weka.core.*;
import java.util.*;

public class CC extends weka.classifiers.multilabel.CC implements MultiTargetClassifier {

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		double y_long[] = Arrays.copyOf(super.distributionForInstance(x),L*2);
		Arrays.fill(y_long,L,y_long.length,1.0);
		return y_long;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new weka.classifiers.multitarget.CC(),args);
	}
}
