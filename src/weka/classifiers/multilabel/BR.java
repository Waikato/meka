package weka.classifiers.multilabel;

/**
 * BR.
 *
 * The Binary Relevance Method
 * see also `BR' from the <a href=http://mulan.sourceforge.net>MULAN</a> framework
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
import weka.classifiers.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import weka.core.*;
import java.util.*;

public class BR extends MultilabelClassifier {

	protected Classifier m_MultiClassifiers[] = null;

	@Override
	public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();

		if(getDebug()) System.out.print("Creating "+L+" models ("+m_Classifier.getClass().getName()+"): ");
		m_MultiClassifiers = AbstractClassifier.makeCopies(m_Classifier,L);

		Instances D_j = null;

		for(int j = 0; j < L; j++) {

			//Select only class attribute 'j'
			D_j = MLUtils.keepAttributesAt(new Instances(D),new int[]{j},L);
			D_j.setClassIndex(0);

			//Build the classifier for that class
			m_MultiClassifiers[j].buildClassifier(D_j);
			if(getDebug()) System.out.print(" " + (D_j.classAttribute().name()));

		}

		m_InstancesTemplate = new Instances(D_j, 0);

	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex(); 

		double y[] = new double[L];

		for (int j = 0; j < L; j++) {
			Instance x_j = (Instance)x.copy();
			x_j.setDataset(null);
			x_j = MLUtils.keepAttributesAt(x_j,new int[]{j},L);
			x_j.setDataset(m_InstancesTemplate);
			//y[j] = m_MultiClassifiers[j].classifyInstance(x_j);
			y[j] = m_MultiClassifiers[j].distributionForInstance(x_j)[1];
		}

		return y;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new BR(),args);
	}

}
