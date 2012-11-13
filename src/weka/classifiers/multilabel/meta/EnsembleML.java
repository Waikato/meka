package weka.classifiers.multilabel.meta;

import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.multilabel.*;
import java.util.*;

/**
 * Combining several multi-label classifiers in a simple-subset ensemble.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */

public class EnsembleML extends MultilabelMetaClassifier {

	/**
	 * Build Classifier
	 */
	public void buildClassifier(Instances train) throws Exception {

		if (getDebug()) System.out.print("-: Models: ");

		m_Classifiers = AbstractClassifier.makeCopies(m_Classifier, m_NumIterations);
		int sub_size = (int)(train.numInstances()*m_BagSizePercent/100);
		for(int i = 0; i < m_NumIterations; i++) {
			if(getDebug()) System.out.print(""+i+" ");
			if (m_Classifiers[i] instanceof Randomizable) ((Randomizable)m_Classifiers[i]).setSeed(i);
			train.randomize(new Random(m_Seed+i));
			Instances sub_train = new Instances(train,0,sub_size);
			m_Classifiers[i].buildClassifier(sub_train);
		}

		if (getDebug()) System.out.println(":-");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new EnsembleML(),args);
	}

}
