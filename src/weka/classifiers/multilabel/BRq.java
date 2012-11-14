package weka.classifiers.multilabel;

/**
 * The Binary Relevance Method - Random Subspace ('quick') Version.
 * This version is able to downsample the number of instances across the binary models.
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer. Vol. 85(3), pp 333-359. (May 2011).
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 * @version January 2009
 */
import weka.classifiers.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import weka.core.*;
import java.util.*;

public class BRq extends MultilabelClassifier {

	/** The downsample ratio*/
	public double m_DownSampleRatio = 0.75;

	/** The random generator */
	protected int m_S = 0;

	public Random m_Random = new Random(m_S);

	protected Classifier m_MultiClassifiers[] = null;

	public void buildClassifier(Instances data) throws Exception {

		int c = data.classIndex();

		if(getDebug()) System.out.print("-: Creating "+c+" models ("+m_Classifier.getClass().getName()+"): ");
		m_MultiClassifiers = AbstractClassifier.makeCopies(m_Classifier,c);

		Instances sub_data = null;

		for(int i = 0; i < c; i++) {

			int indices[][] = new int[c][c - 1];
			for(int j = 0, k = 0; j < c; j++) {
				if(j != i) {
					indices[i][k++] = j;
				}
			}

			//Select only class attribute 'i'
			Remove FilterRemove = new Remove();
			FilterRemove.setAttributeIndicesArray(indices[i]);
			FilterRemove.setInputFormat(data);
			FilterRemove.setInvertSelection(true);
			sub_data = Filter.useFilter(data, FilterRemove);
			sub_data.setClassIndex(0);
			/* BEGIN downsample for this link */
			sub_data.randomize(m_Random);
			int numToRemove = sub_data.numInstances() - (int)Math.round((double)sub_data.numInstances() * m_DownSampleRatio);
			for(int m = 0, removed = 0; m < sub_data.numInstances(); m++) {
				if (sub_data.instance(m).classValue() <= 0.0) {
					sub_data.instance(m).setClassMissing();
					if (++removed >= numToRemove)
						break;
				}
			}
			sub_data.deleteWithMissingClass();
			/* END downsample for this link */


			//Build the classifier for that class
			m_MultiClassifiers[i].buildClassifier(sub_data);
			if(getDebug()) System.out.print(" " + (i+1));

		}

		if(getDebug()) System.out.println(" :-");

		m_InstancesTemplate = new Instances(sub_data, 0);

	}

	protected Instance[] convertInstance(Instance instance, int c) {

		Instance FilteredInstances[] = new Instance[c];

		//for each 'i' classifiers
		for (int i = 0; i < c; i++) {

			//remove all except 'i'
			FilteredInstances[i] = (Instance) instance.copy(); 
			FilteredInstances[i].setDataset(null);
			for (int j = 0, offset = 0; j < c; j++) {
				if (j == i) offset = 1;
				else FilteredInstances[i].deleteAttributeAt(offset);
			}
			FilteredInstances[i].setDataset(m_InstancesTemplate);
		}

		return FilteredInstances;

	}

	public double[] distributionForInstance(Instance instance) throws Exception {

		int c = instance.classIndex(); 

		double result[] = new double[c];

		Instance finstances[] = convertInstance(instance,c);

		for (int i = 0; i < c; i++) {
			result[i] = m_MultiClassifiers[i].classifyInstance(finstances[i]);
			//result[i] = m_MultiClassifiers[i].distributionForInstance(finstances[i])[1];
		}

		return result;
	}


	public void setSeed(int s) {
		m_S = s;
		m_Random = new Random(m_S);
	}

	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the downsampling ratio        \n\tdefault: "+m_DownSampleRatio+"\t(% of original)", "P", 1, "-P <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	public void setOptions(String[] options) throws Exception {

		try {
			m_DownSampleRatio = Double.parseDouble(Utils.getOption('P', options));
		} catch(Exception e) {
			if(getDebug()) System.err.println("Using default P = "+m_DownSampleRatio);
		}

		super.setOptions(options);
	}

	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 2];
		int current = 0;
		options[current++] = "-P";
		options[current++] = "" + m_DownSampleRatio;
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;

	}

	public int getSeed() {
		return m_S;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new BRq(),args);
	}

}
