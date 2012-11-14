package weka.classifiers.multilabel.meta;

import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.multilabel.*;
import java.util.*;

/**
 * Bagging Method 'quick' : an option to downsize the attribute space randomly for each ensemble member
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */

public class BaggingMLq extends MultilabelMetaClassifier {

	int m_AttSizePercent = 50;

	ArrayList m_Indices[] = null;
	Instances m_Dsets[] = null;

	public void buildClassifier(Instances train) throws Exception {

		m_Indices = new ArrayList[m_NumIterations];
		m_Dsets = new Instances[m_NumIterations];

		if (getDebug()) System.out.print("-: Models: ");

		m_Classifiers = AbstractClassifier.makeCopies(m_Classifier,m_NumIterations);

		for(int i = 0; i < m_NumIterations; i++) {
			Random r = new Random(m_Seed+i);
			// DOWNSIZE ATTRIBUTE SPACE
			m_Indices[i] = new ArrayList<Integer>();
			Instances trainCut = new Instances(train);
			trainCut.setClassIndex(-1);
			double d = (double)m_AttSizePercent / 100.0;
			for(int a = trainCut.numAttributes()-1; a >= train.classIndex(); a--) {
				if (r.nextDouble() > d && !m_Indices[i].contains(a)) {
					m_Indices[i].add((Integer)a);
				}
			}
			for(Object a : m_Indices[i]) {
				trainCut.deleteAttributeAt((int)(Integer)a);
			}
			Instances bag = new Instances(trainCut,0);
			m_Dsets[i] = bag;
			// END 
			if (m_Classifiers[i] instanceof Randomizable) ((Randomizable)m_Classifiers[i]).setSeed(m_Seed+i);
			if(getDebug()) System.out.print(""+i+" ");

			int ixs[] = new int[trainCut.numInstances()];
			for(int j = 0; j < ixs.length; j++) {
				ixs[r.nextInt(ixs.length)]++;
			}
			for(int j = 0; j < ixs.length; j++) {
				if (ixs[j] > 0) {
					Instance instance = trainCut.instance(j);
					instance.setWeight((double)ixs[j]);
					bag.add(instance);
				}
			}

			//
			bag.setClassIndex(train.classIndex());
			m_Classifiers[i].buildClassifier(bag);
		}
		if (getDebug()) System.out.println(":-");
	}


	public double[] distributionForInstance(Instance instance) throws Exception {

		double r[] = new double[instance.classIndex()];

		for(int i = 0; i < m_NumIterations; i++) {
			// DOWNSIZE ATTRIBUTE SPACE
			Instance copy = (Instance) instance.copy(); 
			copy.setDataset(null);
			for(Object a : m_Indices[i]) 
				copy.deleteAttributeAt((int)(Integer)a);
			copy.setDataset(m_Dsets[i]);
			//
			double d[] = ((MultilabelClassifier)m_Classifiers[i]).distributionForInstance(copy);
			for(int j = 0; j < d.length; j++) {
				r[j] += d[j];
			}
		}

		return r;
	}

	public Enumeration listOptions() {
		Vector newVector = new Vector();
		newVector.addElement(new Option("\t@Size of attribute space, as a percentage of total attribute space size (default "+m_AttSizePercent+")", "A", 1, "-A <size percentage>"));
		Enumeration enu = super.listOptions();
		while (enu.hasMoreElements()) {
			newVector.addElement(enu.nextElement());
		}
		return newVector.elements();
	}

	public void setOptions(String[] options) throws Exception {
		try { m_AttSizePercent = Integer.parseInt(Utils.getOption('A',options)); } catch(Exception e) { }
		super.setOptions(options);
	}

	public String [] getOptions() {
		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 2];
		int current = 0;
		options[current++] = "-A";
		options[current++] = String.valueOf(m_BagSizePercent);
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new BaggingMLq(),args);
	}

}
