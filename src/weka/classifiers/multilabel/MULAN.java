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
	String m_MethodString = "";

	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tAlgorithm Name\n\t {RAkEL1, RAkEL2, MLkNN, IBLR_ML, HOMER.{CLUS/RAND}.{BR/LP}, CLR, MC-Copy, IncludeLabels MC-Ignore MLStacking}", "S", 1, "-S <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	public void setOptions(String[] options) throws Exception {

		m_MethodString = Utils.getOption('S', options);
		super.setOptions(options);
	}

	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 2];
		int current = 0;
		options[current++] = "-S";
		options[current++] =  m_MethodString;
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;

	}

	public void buildClassifier(Instances instances) throws Exception {

		long before = System.currentTimeMillis();

		Random r = instances.getRandomNumberGenerator(0);
		String name = "temp_"+MLUtils.getDatasetName(instances)+"_"+r.nextLong()+".arff";
		System.out.println("Using temporary file: "+name);
		int L = instances.classIndex();

		// rename attributes, because MULAN doesn't deal well with hypens etc
		for(int i = L; i < instances.numAttributes(); i++) {
			instances.renameAttribute(i,"a_"+i);
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(name));
		m_InstancesTemplate = switchAttributes(new Instances(instances),L);
		writer.write(m_InstancesTemplate.toString());
		writer.flush();
		writer.close();
		MultiLabelInstances train = new MultiLabelInstances(name,L); 
		try {
			((File)new File(name)).delete();
		} catch(Exception e) {
			System.err.println("[Error] Failed to delete temporary file: "+name+". You may want to delete it manually.");
		}

		long after = System.currentTimeMillis();

		System.err.println("[Note] Discount "+((double)(after - before)/1000.0)+ " seconds from this build time");

		m_InstancesTemplate = new Instances(train.getDataSet(),0);

		//m_InstancesTemplate.delete();
		if (m_MethodString.equals("BR"))
			m_MULAN = new BinaryRelevance(m_Classifier);
		else if (m_MethodString.equals("LP"))
			m_MULAN = new LabelPowerset(m_Classifier);
		else if (m_MethodString.equals("CLR"))
			m_MULAN = new CalibratedLabelRanking(m_Classifier);
		else if (m_MethodString.equals("RAkEL1")) {
			m_MULAN = new RAkEL(new LabelPowerset(m_Classifier),10,L/2);
			System.out.println("m=10,k="+(L/2));
		}
		else if (m_MethodString.equals("RAkEL2")) {
			m_MULAN = new RAkEL(new LabelPowerset(m_Classifier),2*L,3);
			System.out.println("m="+(L*2)+",k=3");
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
		int L = instance.classIndex();
		Instance x = switchAttributes((Instance)instance.copy(),L); 
		x.setDataset(m_InstancesTemplate);
		double y[] = new double[L];
		try {
			y = m_MULAN.makePrediction(x).getConfidences();
		} catch(Exception e) {
			System.err.println("MULAN Error");
			System.err.println(":"+Arrays.toString(y));
		}
		return y;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new MULAN(),args);
	}

}
