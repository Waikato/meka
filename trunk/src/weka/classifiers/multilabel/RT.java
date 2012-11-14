package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * RT.
 *
 * see also from the <a href=http://mulan.sourceforge.net>MULAN</a> framework
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class RT extends MultilabelClassifier {

	/**
	 * Build Classifier.
	 */
	public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();

		//Create header
		Instances D_ = new Instances(D,0,0);

		//Delete the old class attributes
		for (int j = 0; j < L; j++)
			D_.deleteAttributeAt(0); 

		//Make the new class attribute
		FastVector classes = new FastVector(L);
		for (int j = 0; j < L; j++)
			classes.addElement(MLUtils.toBitString(j,L)); //(*) e.g. 00100 where j,N == 2,5

		//Add the new class attribute
		D_.insertAttributeAt(new Attribute("class",classes),0);
		D_.setClassIndex(0);

		//Loop through D again
		for (int i = 0; i < D.numInstances(); i++) {
			for (int j = 0; j < L; j++) {
				if((int)D.instance(i).value(j) > 0) {
					Instance inew = (Instance)D.instance(i).copy();
					inew.setDataset(null);
					for (int k = 1; k < L; k++)
						inew.deleteAttributeAt(1); 
					inew.setDataset(D_);
					inew.setClassValue((double)j); // (*) this just ponts to the right index
					D_.add(inew);
				}
			}
		}

		//Save the template
		m_InstancesTemplate = new Instances(D_,0);

		//Build
		if(getDebug())  System.out.println("Building classifier "+m_Classifier.getClass().getName()+" with "+D_.numInstances()+" instances");
		m_Classifier.buildClassifier(D_);

	}

	public Instance convertInstance(Instance test) {

		int L = test.classIndex();

		//Copy the original instance
		Instance real = (Instance) test.copy(); 
		real.setDataset(null);

		//Delete all class attributes
		for (int i = 0; i < L; i++)
			real.deleteAttributeAt(0);

		//Add one of those class attributes at the begginning
		real.insertAttributeAt(0);

		//Hopefully setting the dataset will configure that attribute properly
		real.setDataset(m_InstancesTemplate);

		return real;
	}

	public double[] distributionForInstance(Instance test) throws Exception {
		return m_Classifier.distributionForInstance(convertInstance(test));
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new RT(),args);
	}
}
