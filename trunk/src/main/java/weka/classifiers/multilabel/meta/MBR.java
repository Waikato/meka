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

package weka.classifiers.multilabel.meta;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.multilabel.BR;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

/**
 * MBR.java - Meta BR: BR stacked with feature outputs into another BR.
 * Described in: Godbole and Sarawagi, <i>Discriminative Methods for Multi-labeled Classification</i>. 
 * 
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class MBR extends MultilabelClassifier 
  implements TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = 865889198021748917L;
	
	protected BR m_BASE = null;
	protected BR m_META = null;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"BR stacked with feature outputs.\n"
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		
		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Shantanu Godbole, Sunita Sarawagi");
		result.setValue(Field.TITLE, "Discriminative Methods for Multi-labeled Classification");
		result.setValue(Field.BOOKTITLE, "Advances in Knowledge Discovery and Data Mining");
		result.setValue(Field.YEAR, "2004");
		result.setValue(Field.PAGES, "22-30");
		result.setValue(Field.SERIES, "LNCS");
		
		return result;
	}


	@Override
	public void buildClassifier(Instances data) throws Exception {
	  	getCapabilities().testWithFail(data);
	  	
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

	@Override
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

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new MBR(),args);
	}
}
