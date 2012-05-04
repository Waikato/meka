package weka.classifiers.multitarget;

/**
 * CR. The Class-Relevance Method.
 * (The generalised, multi-target version of the Binary Relevance (BR) method).
 * @see		BR.java
 * @version	Jan 2012
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 */
import weka.classifiers.*;
import weka.classifiers.multilabel.*;
import weka.core.*;
import java.util.*;

public class CR extends weka.classifiers.multilabel.BR implements MultiTargetClassifier {

	Instances m_Templates[] = null; // TEMPLATES

	@Override
	public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();

		if(getDebug()) System.out.print("Creating "+L+" models ("+m_Classifier.getClass().getName()+"): ");
		m_MultiClassifiers = AbstractClassifier.makeCopies(m_Classifier,L);
		m_Templates = new Instances[L];

		for(int j = 0; j < L; j++) {

			//Select only class attribute 'j'
			m_Templates[j] = MLUtils.keepAttributesAt(new Instances(D),new int[]{j},L);
			m_Templates[j].setClassIndex(0);

			//Build the classifier for that class
			m_MultiClassifiers[j].buildClassifier(m_Templates[j]);
			if(getDebug()) System.out.print(" " + (m_Templates[j].classAttribute().name()));

			m_Templates[j] = new Instances(m_Templates[j], 0);
		}
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex(); 

		double y[] = new double[L*2];

		for (int j = 0; j < L; j++) {
			Instance x_j = (Instance)x.copy();
			x_j.setDataset(null);
			x_j = MLUtils.keepAttributesAt(x_j,new int[]{j},L);
			x_j.setDataset(m_Templates[j]);
			double w[] = m_MultiClassifiers[j].distributionForInstance(x_j); // e.g. [0.1, 0.8, 0.1]
			y[j] = Utils.maxIndex(w);									     // e.g. 1
			y[L+j] = w[(int)y[j]];											 // e.g. 0.8
		}

		System.out.println("y = "+Arrays.toString(y));

		return y;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CR(),args);
	}

}
