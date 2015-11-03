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

package meka.classifiers.multilabel.meta;

import meka.classifiers.multilabel.CC;
import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.core.OptionUtils;
import weka.core.Instance;
import weka.core.Option;
import weka.core.Randomizable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * MultilabelMetaClassifier.java - For ensembles of multi-label methods.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public abstract class MetaProblemTransformationMethod extends ProblemTransformationMethod implements Randomizable {

	/** for serialization. */
	private static final long serialVersionUID = -6604797895790690612L;
	
	protected ProblemTransformationMethod m_Classifiers[] = null;
	protected int m_Seed = 1;
	protected int m_NumIterations = 10;
	protected int m_BagSizePercent = 67;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "For ensembles of multi-label methods.";
	}

	public MetaProblemTransformationMethod() {
		// default classifier for GUI
		this.m_Classifier = new CC();
	}

	@Override
	protected String defaultClassifierString() {
		// default classifier for CLI
		return "meka.classifiers.multilabel.CC";
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		double p[] = new double[x.classIndex()];

		for(int i = 0; i < m_NumIterations; i++) {
			double d[] = m_Classifiers[i].distributionForInstance(x);
			for(int j = 0; j < d.length; j++) {
				p[j] += d[j];
			}
		}

		// turn votes into a [0,1] confidence for each label
		for(int j = 0; j < p.length; j++) {
			p[j] = p[j]/m_NumIterations;
		}

		return p;
	}

	public int getNumIterations() {
		return m_NumIterations;
	}

	public void setNumIterations(int n) {
		m_NumIterations = n;
	}

	public String numIterationsTipText() {
		return "The number of iterations to perform.";
	}

	public int getBagSizePercent() {
		return m_BagSizePercent;
	}

	public void setBagSizePercent(int p) {
		m_BagSizePercent = p;
	}

	public String bagSizePercentTipText() {
		return "The size of the bag in percent (0-100).";
	}

	@Override
	public void setSeed(int s) {
		m_Seed = s;
	}

	@Override
	public int getSeed() {
		return m_Seed;
	}

	public String seedTipText() {
		return "The seed value for randomizing the data.";
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tSets the number of models (default 10)", "I", 1, "-I <num>"));
		result.addElement(new Option("\tSize of each bag, as a percentage of total training size (default 67)", "P", 1, "-P <size percentage>"));
		result.addElement(new Option("\tRandom number seed for sampling (default 1)", "S", 1, "-S <seed>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setSeed(OptionUtils.parse(options, 'S', 1));
		setNumIterations(OptionUtils.parse(options, 'I', 10));
		setBagSizePercent(OptionUtils.parse(options, 'P', 67));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'S', getSeed());
		OptionUtils.add(result, 'I', getNumIterations());
		OptionUtils.add(result, 'P', getBagSizePercent());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	/**
	 * Returns a string representation of the model.
	 *
	 * @return      the model
	 */
	public String getModel() {
		StringBuilder   result;
		int             i;
		String          model;

		if (m_Classifiers == null)
			return getClass().getName() + ": No model built yet";

		result = new StringBuilder();
		for (i = 0; i < m_Classifiers.length; i++) {
			if (i > 0)
				result.append("\n\n");
			result.append(getClass().getName() + ": Model #" + (i+1) + "\n\n");
			model = m_Classifiers[i].getModel();
			if (model.length() > 0)
				result.append(model);
			else
				result.append("No model representation available");
		}

		return result.toString();
	}
}
