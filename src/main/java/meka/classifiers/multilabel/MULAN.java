/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package meka.classifiers.multilabel;

import meka.core.F;
import meka.core.MLUtils;
import meka.core.OptionUtils;
import mulan.classifier.MultiLabelLearner;
import mulan.classifier.lazy.IBLR_ML;
import mulan.classifier.lazy.MLkNN;
import mulan.classifier.meta.HOMER;
import mulan.classifier.meta.HierarchyBuilder;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.neural.BPMLL;
import mulan.classifier.transformation.BinaryRelevance;
import mulan.classifier.transformation.CalibratedLabelRanking;
import mulan.classifier.transformation.ClassifierChain;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.MultiLabelInstances;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * MULAN.java - A wrapper for MULAN classifiers <a href=http://mulan.sourceforge.net>MULAN</a>. 
 * <br>
 * The classifiers are instantiated with suitable parameters.
 *
 * @version	June 2014
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class MULAN extends ProblemTransformationMethod {

	/** for serialization. */
	private static final long serialVersionUID = 1720289364996202350L;

	protected static Random m_r = new Random(0);

	protected MultiLabelLearner m_MULAN = null;

	//HOMER.Random.3.LabelPowerset
	private String MethodSelection = "{BR, LP, CLR, RAkELn, MLkNN, IBLR_ML, BPMLL, HOMER.type.numPartitions.pt}\n" +
		"\twhere n=1 is with (m=10,k=L/2), n=2 is with (m=L*2,k=3); and\n\twhere type \\in {BalancedClustering,Clustering,Random}, pt \\in {BinaryRelevance,LabelPowerset,ClassifierChain, numPartitions \\in [1,2,3,4,...]}.";

	protected String m_MethodString = "RAkEL1";

	/**
	 * Description to display in the GUI.
	 *
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return
				"A wrapper for MULAN classifiers.\n"
				+ "http://mulan.sourceforge.net";
	}

	/**
	 * Set a prescribed MULAN classifier configuration.
	 * @param	m	key; one of: BR, LP, CLR, RAkEL1, RAkEL2, MLkNN, IBLR_ML, BPMLL (you can add more in the MULAN.java code)
	 */
	public void setMethod(String m) {
		m_MethodString = m;
	}

	public String getMethod() {
		return m_MethodString;
	}

	public String methodTipText() {
		return "Any of "+MethodSelection+". If you wish to add more, you will have to add code to the buildClassifier(Instances) function in MULAN.java";
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tMethod Name\n\tdefault: RAkEL1", "S", 1, "-S <value>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setMethod(OptionUtils.parse(options, 'S', "RAkEL1"));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'S', getMethod());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);

	}

	/**
	 * Returns a random long value for the temp filename.
	 *
	 * @return the long value
	 */
	protected synchronized long nextLong() {
		return m_r.nextLong();
	}

	@Override
	public void buildClassifier(Instances instances) throws Exception {
	  	testCapabilities(instances);

		long before = System.currentTimeMillis();
		if (getDebug()) System.err.print(" moving target attributes to the beginning ... ");

		String name = "temp_"+MLUtils.getDatasetName(instances)+"_"+System.currentTimeMillis() + "_"+nextLong()+".arff";
		System.err.println("Using temporary file: "+name);
		int L = instances.classIndex();

		// rename attributes, because MULAN doesn't deal well with hypens etc
		for(int i = L; i < instances.numAttributes(); i++) {
			instances.renameAttribute(i,"a_"+i);
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(name));
		m_InstancesTemplate = F.meka2mulan(new Instances(instances),L);
		writer.write(m_InstancesTemplate.toString());
		writer.flush();
		writer.close();
		MultiLabelInstances train = new MultiLabelInstances(name,L); 
		try {
			new File(name).delete();
		} catch(Exception e) {
			System.err.println("[Error] Failed to delete temporary file: "+name+". You may want to delete it manually.");
		}

		if (getDebug()) System.out.println(" done ");
		long after = System.currentTimeMillis();

		System.err.println("[Note] Discount "+((after - before)/1000.0)+ " seconds from this build time");

		m_InstancesTemplate = new Instances(train.getDataSet(),0);

		System.out.println("CLASSIFIER "+m_Classifier);

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
		else if (m_MethodString.equals("BPMLL")) { //BPMLL is run withthe number of hidden units equal to 20% of the input units.
			m_MULAN = new BPMLL();
			((BPMLL)m_MULAN).setLearningRate(0.01);
			((BPMLL)m_MULAN).setHiddenLayers(new int[]{30});
			((BPMLL)m_MULAN).setTrainingEpochs(100);
		}
		else if (m_MethodString.startsWith("HOMER")) {
			//Class m = Class.forName("HierarchyBuilder.Method.Random");
			//Class w = Class.forName("mulan.classifier.LabelPowerset");
			//Constructor c = new h.getConstructor(new Class[]{MultiLabelLearner.class, Integer.TYPE, HierarchyBuilder.Method.class});
			//Object obj = h.newInstance();

			String ops[] = m_MethodString.split("\\.");

			// number of clusters
			int n = 3;
			try {
				n = Integer.parseInt(ops[2]);
			} catch(Exception e) {
				System.err.println("[Warning] Could not parse number of clusters, using default: "+n);
			}

			// learner
			// @TODO use reflection here
			MultiLabelLearner mll = new LabelPowerset(m_Classifier);
			if (ops[3].equalsIgnoreCase("BinaryRelevance")) {
				mll = new BinaryRelevance(m_Classifier);
			}
			else if (ops[3].equalsIgnoreCase("ClassifierChain")) {
				mll = new ClassifierChain(m_Classifier);
			}
			else if (ops[3].equalsIgnoreCase("LabelPowerset")) {
				// already set
			}
			else {
				System.err.println("[Warning] Did not recognise classifier type String, using default: LabelPowerset");
			}

			if (getDebug()) {
				System.out.println("HOMER("+mll+","+n+","+ops[1]+")");
			}

			m_MULAN = new HOMER(mll, n, HierarchyBuilder.Method.valueOf(ops[1]));
		}
		else throw new Exception ("Could not find MULAN Classifier by that name: "+m_MethodString);

		m_MULAN.setDebug(getDebug());
		m_MULAN.build(train);
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		int L = instance.classIndex();
		Instance x = F.meka2mulan((Instance)instance.copy(),L); 
		x.setDataset(m_InstancesTemplate);
		double y[] = m_MULAN.makePrediction(x).getConfidences();
		return y;
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new MULAN(), args);
	}
}
