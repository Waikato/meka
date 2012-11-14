package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * LC.java - aka LP (Laber Powerset) Method.
 * <br>
 * @TODO PS should really extend this class
 * <br>
 * See also <i>LP</i> from the <a href=http://mulan.sourceforge.net>MULAN</a> framework.
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class LC extends MultilabelClassifier implements OptionHandler {

	public void buildClassifier(Instances Train) throws Exception {

		int C = Train.classIndex();

		//Create a nominal class attribute of all (existing) possible combinations of labels as possible values
		FastVector ClassValues = new FastVector(C);
		HashSet<String> UniqueValues = new HashSet<String>();
		for (int i = 0; i < Train.numInstances(); i++) {
			UniqueValues.add(MLUtils.toBitString(Train.instance(i),C));
		}
		Iterator<String> it = UniqueValues.iterator();
		while (it.hasNext()) {
			ClassValues.addElement(it.next());
		}
		Attribute NewClass = new Attribute("Class", ClassValues);

		//Filter Remove all class attributes
		Remove FilterRemove = new Remove();
		FilterRemove.setAttributeIndices("1-"+C);
		FilterRemove.setInputFormat(Train);
		Instances NewTrain = Filter.useFilter(Train, FilterRemove);

		//Insert new special attribute (which has all possible combinations of labels) 
		NewTrain.insertAttributeAt(NewClass, 0);
		NewTrain.setClassIndex(0);

		//Add class values
		for (int i = 0; i < NewTrain.numInstances(); i++) {
			String comb = MLUtils.toBitString(Train.instance(i),C);
			NewTrain.instance(i).setClassValue(comb);
		}

		// keep the header of new dataset for classification
		m_InstancesTemplate = new Instances(NewTrain, 0);

		// build classifier on new dataset
		// Info
		if(getDebug()) System.out.println("("+m_InstancesTemplate.attribute(0).numValues()+" classes, "+NewTrain.numInstances()+" ins. )");
		if(getDebug()) System.out.print("Building Classifier "+m_Classifier.getClass()+" with "+ClassValues.size()+" possible classes .. ");
		m_Classifier.buildClassifier(NewTrain);
		if(getDebug()) System.out.println("Done");

	}

  public Instance convertInstance(Instance TestInstance, int C) {
	  Instance FilteredInstance = (Instance) TestInstance.copy(); 
	  FilteredInstance.setDataset(null);
	  for (int i = 0; i < C; i++)
		  FilteredInstance.deleteAttributeAt(0);
	  FilteredInstance.insertAttributeAt(0);
	  FilteredInstance.setDataset(m_InstancesTemplate);
	  return FilteredInstance;
  }

  //convert e.g. r[0,0,0,0,1,0,1] to r[0,1,1,1,0]
  // @todo clean this up a bit, there should only be one r which needs to be converted (the max)
  // (this will also need to apply to PS if necessary)
  public double[] convertDistribution(double r[], int c) {
	  double newr[] = new double[c];
	  for(int i = 0; i < r.length; i++) {
		  if(r[i] > 0.0) {
			  double d[] = MLUtils.fromBitString(m_InstancesTemplate.classAttribute().value(i));
			  for(int j = 0; j < d.length; j++) {
				  if(d[j] > 0.0)
					  newr[j] = 1.0;
			  }
		  }
	  }
	  return newr;
  }

  public double[] distributionForInstance(Instance mlInstance) throws Exception {

	  int c = mlInstance.classIndex();

	  //if there is only one class (as for e.g. in some hier. mtds) predict it
	  if(c == 1) return new double[]{1.0};

	  Instance slInstance = convertInstance(mlInstance,c);
	  slInstance.setDataset(m_InstancesTemplate);

	  //Get a classification
	  double result[] = new double[slInstance.numClasses()];

	  result[(int)m_Classifier.classifyInstance(slInstance)] = 1.0;

	  return convertDistribution(result,c);
  }

  public static void main(String args[]) {
	  MultilabelClassifier.evaluation(new LC(),args);
  }

}
