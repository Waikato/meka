package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * PSt. Pruned Sets with Thresholding.
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class PSt extends PS {

	public double[] convertDistribution(double r[], int c) {
		double newr[] = new double[c];
		for(int i = 0; i < r.length; i++) {
			double d[] = MLUtils.fromBitString(m_InstancesTemplate.classAttribute().value(i));
			for(int j = 0; j < d.length; j++) {
				newr[j] += (d[j] * r[i]);
			}
		}
		try {
			Utils.normalize(newr);
		} catch(Exception e) {
			newr = new double[c];
		}
		return newr;
	}

	public double[] distributionForInstance(Instance TestInstance) throws Exception {

		int c = TestInstance.classIndex();

		//if there is only one class (as for e.g. in some hier. mtds) predict it
		if(c == 1) return new double[]{1.0};

		Instance mlInstance = convertInstance(TestInstance,c);
		mlInstance.setDataset(m_InstancesTemplate);

		//Get a classification
		return convertDistribution(m_Classifier.distributionForInstance(mlInstance),c);
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new PSt(),args);
	}

}
