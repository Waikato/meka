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
import weka.core.Instance;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.Utils;

import java.util.Enumeration;
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
		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the number of models (default "+m_NumIterations+")", "I", 1, "-I <num>"));
		newVector.addElement(new Option("\tSize of each bag, as a percentage of total training size (default "+m_BagSizePercent+")", "P", 1, "-P <size percentage>"));
		newVector.addElement(new Option("\tRandom number seed for sampling (default "+m_Seed+")", "S", 1, "-S <seed>"));

		Enumeration enu = super.listOptions();
		while (enu.hasMoreElements()) {
			newVector.addElement(enu.nextElement());
		}

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		String tmpStr; 

		tmpStr = Utils.getOption('S', options);
		if (tmpStr.length() != 0) 
			setSeed(Integer.parseInt(tmpStr));
		else
		  setSeed(1);

		tmpStr = Utils.getOption('I', options);
		if (tmpStr.length() != 0) 
			setNumIterations(Integer.parseInt(tmpStr));
		else
		  setNumIterations(10);

		tmpStr = Utils.getOption('P', options);
		if (tmpStr.length() != 0) 
			setBagSizePercent(Integer.parseInt(tmpStr));
		else
		  setBagSizePercent(67);

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 6];
		int current = 0;
		options[current++] = "-S";
		options[current++] = "" + getSeed();
		options[current++] = "-I";
		options[current++] = "" + getNumIterations();
		options[current++] = "-P";
		options[current++] = "" + getBagSizePercent();
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;
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
