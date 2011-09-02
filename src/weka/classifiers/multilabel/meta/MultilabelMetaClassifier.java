package weka.classifiers.multilabel.meta;

import weka.classifiers.multilabel.*;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.core.*;
import java.util.*;
import java.text.*;

/**
 * For Ensembles of Multi-label Methods.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public abstract class MultilabelMetaClassifier extends MultilabelClassifier {

	public Classifier m_Classifiers[] = null;
	public int m_Seed = 1;
	public int m_NumIterations = 10;
	public int m_BagSizePercent = 67;

	public double[] distributionForInstance(Instance instance) throws Exception {

		double r[] = new double[instance.classIndex()];

		for(int i = 0; i < m_NumIterations; i++) {
			double d[] = m_Classifiers[i].distributionForInstance(instance);
			for(int j = 0; j < d.length; j++) {
				r[j] += d[j];
			}
		}

		// turn votes into a [0,1] confidence for each label
		for(int j = 0; j < r.length; j++) {
			r[j] = r[j]/m_NumIterations;
		}

		return r;
	}

	public Enumeration listOptions() {
		Vector newVector = new Vector();
		newVector.addElement(new Option("\t@Sets the number of models (default "+m_NumIterations+")", "I", 1, "-I <num>"));
		newVector.addElement(new Option("\t@Size of each bag, as a percentage of total training size (default "+m_BagSizePercent+")", "P", 1, "-P <size percentage>"));
		newVector.addElement(new Option("\t@Random number seed for sampling (default "+m_Seed+")", "S", 1, "-S <seed>"));

		Enumeration enu = super.listOptions();
		while (enu.hasMoreElements()) {
			newVector.addElement(enu.nextElement());
		}

		return newVector.elements();
	}

	public void setOptions(String[] options) throws Exception {
		try { m_Seed = Integer.parseInt(Utils.getOption('S',options)); } catch(Exception e) { }
		try { m_NumIterations = Integer.parseInt(Utils.getOption('I',options)); } catch(Exception e) { }
		try { m_BagSizePercent = Integer.parseInt(Utils.getOption('P',options)); } catch(Exception e) { }
		super.setOptions(options);
	}

	public String [] getOptions() {
		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 6];
		int current = 0;
		options[current++] = "-S";
		options[current++] = String.valueOf(m_Seed);
		options[current++] = "-I";
		options[current++] = String.valueOf(m_NumIterations);
		options[current++] = "-P";
		options[current++] = String.valueOf(m_BagSizePercent);
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;
	}

}
