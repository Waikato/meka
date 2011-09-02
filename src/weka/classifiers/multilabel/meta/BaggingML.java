package weka.classifiers.multilabel.meta;

import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.multilabel.*;
import java.util.*;

/**
 * Combining several multi-label classifiers using Bootstrap AGGregatING.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class BaggingML extends MultilabelMetaClassifier {

	public void buildClassifier(Instances train) throws Exception {

		if (getDebug()) System.out.print("-: Models: ");

		m_Classifiers = AbstractClassifier.makeCopies(m_Classifier, m_NumIterations);
		for(int i = 0; i < m_NumIterations; i++) {
			Random r = new Random(m_Seed+i);
			Instances bag = new Instances(train,0);
			if (m_Classifiers[i] instanceof Randomizable) ((Randomizable)m_Classifiers[i]).setSeed(m_Seed+i);
			if(getDebug()) System.out.print(""+i+" ");

			int bag_no = (m_BagSizePercent*train.numInstances()/100);
			//System.out.println(" bag no: "+bag_no);
			while(bag.numInstances() < bag_no) {
				bag.add(train.instance(r.nextInt(train.numInstances())));
			}
			m_Classifiers[i].buildClassifier(bag);
		}
		if (getDebug()) System.out.println(":-");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new BaggingML(),args);
	}

}
