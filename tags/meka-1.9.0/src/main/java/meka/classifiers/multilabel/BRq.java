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

import meka.core.OptionUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.*;

/**
 * BRq.java - Random Subspace ('quick') Version.
 * Like BR, but randomly samples the attribute space for each binary model. Intended for use in an ensemble (but will work in a standalone fashion also).
 * <br>
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer. Vol. 85(3), pp 333-359. (May 2011).
 * <br>
 * @see BR
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 * @version January 2009
 */
public class BRq extends ProblemTransformationMethod implements Randomizable, TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = 398261703726763108L;

	/** The downsample ratio*/
	protected double m_DownSampleRatio = 0.75;

	/** The random generator */
	protected int m_S = 0;

	protected Random m_Random = new Random(m_S);

	protected Classifier m_MultiClassifiers[] = null;

	/**
	 * Description to display in the GUI.
	 *
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return
				"The Binary Relevance Method - Random Subspace ('quick') Version.\n"
						+ "This version is able to downsample the number of instances across the binary models.\n"
						+ "For more information see:\n"
						+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank");
		result.setValue(Field.TITLE, "Classifier Chains for Multi-label Classification");
		result.setValue(Field.JOURNAL, "Machine Learning Journal");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.VOLUME, "85");
		result.setValue(Field.NUMBER, "3");
		result.setValue(Field.PAGES, "333-359");

		return result;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		testCapabilities(data);

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
			int numToRemove = sub_data.numInstances() - (int)Math.round(sub_data.numInstances() * m_DownSampleRatio);
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

	@Override
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

	public int getSeed() {
		return m_S;
	}

	public String seedTipText() {
		return "The seed value for randomizing the data.";
	}

	public void setDownSampleRatio(double value) {
		m_DownSampleRatio = value;
	}

	public double getDownSampleRatio() {
		return m_DownSampleRatio;
	}

	public String downSampleRatioTipText() {
		return "The down sample ratio.";
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tSets the downsampling ratio\n\tdefault: 0.75\n\t(% of original)", "P", 1, "-P <value>"));
		result.addElement(new Option("\tThe seed value for randomization\n\tdefault: 0", "S", 1, "-S <value>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setDownSampleRatio(OptionUtils.parse(options, 'P', 0.75));
		setSeed(OptionUtils.parse(options, 'S', 0));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'P', getDownSampleRatio());
		OptionUtils.add(result, 'S', getSeed());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new BRq(), args);
	}
}
