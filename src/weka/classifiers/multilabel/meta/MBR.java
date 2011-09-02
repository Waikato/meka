package weka.classifiers.multilabel.meta;

import weka.classifiers.multilabel.*;
import weka.classifiers.*;
import weka.core.*;
import java.util.*;

/**
 * BR stacked with feature outputs.
 * described in ``Discriminative Methods for Multi-labeled Classification'' by Godbole and Sarawagi 
 * 
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class MBR extends MultilabelClassifier {

	protected BR m_BASE = null;
	protected BR m_META = null;

	public void buildClassifier(Instances data) throws Exception {

		int c = data.classIndex();

		//BASE

		if (getDebug()) System.out.println(" Build BR Base ("+c+" models)");
		m_BASE = (BR)AbstractClassifier.forName(getClassifier().getClass().getName(),((AbstractClassifier)getClassifier()).getOptions());
		m_BASE.buildClassifier(data);

		//META

		if (getDebug()) System.out.println(" Prepare Meta data           ");
		Instances meta_data = new Instances(data);

		FastVector BinaryClass = new FastVector(c);
		BinaryClass.addElement("0");
		BinaryClass.addElement("1");

		for(int i = 0; i < c; i++) {
			meta_data.insertAttributeAt(new Attribute("metaclass"+i,BinaryClass),c);
		}

		for(int i = 0; i < data.numInstances(); i++) {
			double cfn[] = m_BASE.distributionForInstance(data.instance(i));
			for(int a = 0; a < cfn.length; a++) {
				meta_data.instance(i).setValue(a+c,cfn[a]);
			}
		}

		meta_data.setClassIndex(c);
		m_InstancesTemplate = new Instances(meta_data, 0);

		if (getDebug()) System.out.println(" Build BR Meta ("+c+" models)");

		//m_META = (BR)Classifier.forName(getClassifier().getClass().getName(),getClassifier().getOptions());
		m_META = (BR)AbstractClassifier.forName(getClassifier().getClass().getName(),((AbstractClassifier)getClassifier()).getOptions());
		m_META.buildClassifier(meta_data);

	}

	public double[] distributionForInstance(Instance instance) throws Exception {

		int c = instance.classIndex();

		double result[] = m_BASE.distributionForInstance(instance);

		instance.setDataset(null);

		for (int i = 0; i < c; i++) {
			instance.insertAttributeAt(c);
		}

		instance.setDataset(m_InstancesTemplate);

		for (int i = 0; i < c; i++) {
			instance.setValue(c+i,result[i]);
		}

		return m_META.distributionForInstance(instance);
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new MBR(),args);
	}

}
