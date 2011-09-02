package weka.classifiers.multilabel;

/**
 * BR.
 *
 * The Binary Relevance Method
 * see also `BR' from the <a href=http://mulan.sourceforge.net>MULAN</a> framework
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
import weka.classifiers.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import weka.core.*;
import java.util.*;

public class BR extends MultilabelClassifier {

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

	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex(); 

		double result[] = new double[L];

		Instance x_j[] = convertInstance(x,L);

		for (int j = 0; j < L; j++) {
			//result[j] = m_MultiClassifiers[j].classifyInstance(x_j[j]);
			result[j] = m_MultiClassifiers[j].distributionForInstance(x_j[j])[1];
		}

		return result;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new BR(),args);
	}

}
