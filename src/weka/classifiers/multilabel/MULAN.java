package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.core.*;
import java.util.*;
import java.io.*;

import mulan.evaluation.*;
import mulan.data.*; 
import mulan.classifier.*;
import mulan.classifier.lazy.*;
import mulan.classifier.meta.*;
import mulan.classifier.transformation.*;
import mulan.classifier.neural.*;
import mulan.evaluation.*;

/**
 * MULAN.
 *
 * Meta Classifier for wrapping around <a href=http://mulan.sourceforge.net>MULAN</a> classifiers. 
 * The classifiers are instantiated here with suitable parameters.
 *
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class MULAN extends MultilabelClassifier {

	MultiLabelLearner m_MULAN = null;
	//int m_K = 10, m_M = 10;
	String m_MethodString = "";

	public Enumeration listOptions() {

		Vector newVector = new Vector();
		//newVector.addElement(new Option("\tK Parameter\n\tdefault: "+m_K, "K", 1, "-K <value>"));
		//newVector.addElement(new Option("\tM Parameter\n\tdefault: "+m_M, "M", 1, "-M <value>"));
		//newVector.addElement(new Option("\tS Parameter\n\tdefault: "+m_M, "S", 1, "-S <value>"));
		newVector.addElement(new Option("\tAlgorithm Name\n\t {RAkEL1, RAkEL2, MLkNN, IBLR_ML, HOMER.{CLUS/RAND}.{BR/LP}, CLR, MC-Copy, IncludeLabels MC-Ignore MLStacking}", "S", 1, "-S <value>"));
		String[] methodsToCompare = { };

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	public void setOptions(String[] options) throws Exception {

		m_MethodString = ""+Utils.getOption('S', options);

		//try {
			//m_K = Integer.parseInt(Utils.getOption('K', options));
		//} catch(Exception e) {
			//if(getDebug()) System.err.println("Using default K = "+m_K);
		//}
//
		//try {
			//m_M = Integer.parseInt(Utils.getOption('M', options));
		//} catch(Exception e) {
			//if(getDebug()) System.err.println("Using default M = "+m_M);
		//}

		super.setOptions(options);
	}

	/**
	 * Get Options.
	 */
	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 2];
		int current = 0;
		//options[current++] = "-K";
		//options[current++] = "" + m_K;
		//options[current++] = "-M";
		//options[current++] = "" + m_M;
		options[current++] = "-S";
		options[current++] = "" + m_MethodString;
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;

	}

	public void buildClassifier(Instances instances) throws Exception {

		Random r = instances.getRandomNumberGenerator(0);
		String name = "temp_"+MLUtils.getDatasetName(instances)+"_"+r.nextLong()+".arff";
		System.out.println("name = "+name);
		int c = instances.classIndex();

		long before = System.currentTimeMillis();

		// rename attributes, because MULAN doesn't deal well with hypens etc
		for(int i = c; i < instances.numAttributes(); i++) {
			instances.renameAttribute(i,"a"+i);
		}
		// ALWAYS WRITE IF WE ARE DOING FOLDS !
		//if (!(new File(name)).exists()) {
		m_InstancesTemplate = switchAttributes(new Instances(instances),c);
		BufferedWriter writer = new BufferedWriter(new FileWriter(name));
		writer.write(m_InstancesTemplate.toString());
		writer.flush();
		writer.close();
			/*
		}
		else {
			System.err.println("THE TEMPORARY FILE ALREADY EXISTS");
		}
		try {
			MultiLabelInstances temp = new MultiLabelInstances(name,c); 
		} catch(Exception e) {
			System.err.println("-- Error --");
			e.printStackTrace();
			System.exit(1);
		}
			 */
		MultiLabelInstances train = new MultiLabelInstances(name,c); 
		try {
			((File)new File(name)).delete();
		} catch(Exception e) {
			System.err.println("-- Error -- : Failed to delete file: "+name);
			e.printStackTrace();
			System.exit(1);
		}

		long after = System.currentTimeMillis();
		System.err.println("Note: discount "+((double)(after - before)/1000.0)+ " seconds from this build time");

		m_InstancesTemplate = new Instances(train.getDataSet(),0);

		//m_InstancesTemplate.delete();
		if (m_MethodString.equals("BR"))
			m_MULAN = new BinaryRelevance(m_Classifier);
		else if (m_MethodString.equals("LP"))
			m_MULAN = new LabelPowerset(m_Classifier);
		else if (m_MethodString.equals("CLR"))
			m_MULAN = new CalibratedLabelRanking(m_Classifier);
		else if (m_MethodString.equals("RAkEL1")) {
			m_MULAN = new RAkEL(new LabelPowerset(m_Classifier),10,c/2);
			System.out.println("m=10,k="+(c/2));
		}
		else if (m_MethodString.equals("RAkEL2")) {
			m_MULAN = new RAkEL(new LabelPowerset(m_Classifier),2*c,3);
			System.out.println("m="+(c*2)+",k=3");
		}
		else if (m_MethodString.equals("MLkNN"))
			m_MULAN = new MLkNN(10,1.0);
		else if (m_MethodString.equals("IBLR_ML"))
			m_MULAN = new IBLR_ML(10);
		else if (m_MethodString.equals("BPMLL")) //BPMLL is run withthe number of hidden units equal to 20% of the input units.
			m_MULAN = new BPMLL();
		else if (m_MethodString.equals("HOMER.RAND.LP"))
			m_MULAN = new HOMER(new LabelPowerset(m_Classifier), 3, HierarchyBuilder.Method.Random);
		else if (m_MethodString.equals("HOMER.RAND.BR"))
			m_MULAN = new HOMER(new BinaryRelevance(m_Classifier), 3, HierarchyBuilder.Method.Random);
		else if (m_MethodString.equals("HOMER.CLUS.LP"))
			m_MULAN = new HOMER(new LabelPowerset(m_Classifier), 3, HierarchyBuilder.Method.Clustering);
		else if (m_MethodString.equals("HOMER.CLUS.BR"))
			m_MULAN = new HOMER(new BinaryRelevance(m_Classifier), 3, HierarchyBuilder.Method.Clustering);
		else throw new Exception ("Could not find MULAN Classifier by that name: "+m_MethodString);

		m_MULAN.build(train);
	}

	/**
	 * Move Label Attributes From Beginning To End of Attribute Space. 
	 * Necessary because MULAN assumes label attributes are at the end, not the beginning.
	 * (the extra time for this process is not counted in any build time analysis of published work).
	 */
	public static final Instances switchAttributes(Instances instances, int N) {
		for(int j = 0; j < N; j++) {
			System.out.print(".");
			//instances.insertAttributeAt(new Attribute(instances.attribute(0).name()+"-"),instances.numAttributes());
			instances.insertAttributeAt(instances.attribute(0).copy(instances.attribute(0).name()+"-"),instances.numAttributes());
			for(int i = 0; i < instances.numInstances(); i++) {
				instances.instance(i).setValue(instances.numAttributes()-1,instances.instance(i).value(0));
			}
			instances.deleteAttributeAt(0);
		}
		System.out.println("");
		return instances;
	}

	public static final Instance switchAttributes(Instance instance, int N) {
		instance.setDataset(null);
		for(int j = 0; j < N; j++) {
			instance.insertAttributeAt(instance.numAttributes());
			instance.deleteAttributeAt(0);
		}
		return instance;
	}

	public double[] distributionForInstance(Instance instance) throws Exception {
		int c = instance.classIndex();
		Instance test = switchAttributes((Instance)instance.copy(),c); 
		test.setDataset(m_InstancesTemplate);
		double d[] = new double[c];
		try {
			d = m_MULAN.makePrediction(test).getConfidences();
		} catch(Exception e) {
			System.err.println("MULAN Error");
			System.err.println(":"+Arrays.toString(d));
		}
		return d;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new MULAN(),args);
	}

}
