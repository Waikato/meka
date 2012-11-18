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

package weka.classifiers.multilabel;

import java.util.HashSet;
import java.util.Iterator;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MLUtils;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * LC.java - aka LP (Laber Powerset) Method.
 * <br>
 * TODO PS should really extend this class
 * <br>
 * See also <i>LP</i> from the <a href=http://mulan.sourceforge.net>MULAN</a> framework.
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class LC extends MultilabelClassifier implements OptionHandler {

	/** for serialization. */
	private static final long serialVersionUID = -2726090581435923988L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"LC aka LP (Laber Powerset) Method.\n"
				+ "See also LP from MULAN:\n"
				+ "http://mulan.sourceforge.net";
	}

	@Override
	public void buildClassifier(Instances Train) throws Exception {
	  	getCapabilities().testWithFail(Train);
	  	
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

  @Override
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

  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 9117 $");
  }

  public static void main(String args[]) {
	  MultilabelClassifier.evaluation(new LC(),args);
  }

}
