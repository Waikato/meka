package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.core.*;
import java.util.*;
import java.text.*;

/**
 *  Multilabel Classifier.
 *  All problem transformation methods need a base (single-label) classifier supplied.
 * 	@author Jesse Read (jmr30@cs.waikato.ac.nz)
 */

public abstract class MultilabelClassifier extends SingleClassifierEnhancer {

	/** A Template for Problem Transformations */
	protected Instances m_InstancesTemplate;

	public String toString() {
		return this.getClass().getName()+":"+m_Classifier.getClass().getName();
	}

	public Instances getTemplate() {
		return m_InstancesTemplate;
	}

	public abstract void buildClassifier(Instances trainingSet) throws Exception;

	public abstract double[] distributionForInstance(Instance i) throws Exception;

	// The new version of WEKA insists on this
	public String getRevision() {
		return null;
	}

	public static void evaluation(MultilabelClassifier PTX, String args[]) {
		try {
			 Evaluation.runExperiment(PTX,args);
		} catch(Exception e) {
			System.err.println("Evaluation exception ("+e+"); failed to run experiment");
			e.printStackTrace();
			Evaluation.printOptions(PTX.listOptions());
		}
	}

}
