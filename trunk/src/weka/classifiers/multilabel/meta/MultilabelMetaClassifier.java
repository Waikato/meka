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

import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Instance;
import weka.core.Option;
import weka.core.Utils;

/**
 * MultilabelMetaClassifier.java - For ensembles of multi-label methods.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public abstract class MultilabelMetaClassifier extends MultilabelClassifier {

	/** for serialization. */
	private static final long serialVersionUID = -6604797895790690612L;
	
	public Classifier m_Classifiers[] = null;
	public int m_Seed = 1;
	public int m_NumIterations = 10;
	public int m_BagSizePercent = 67;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"For ensembles of multi-label methods.";
	}


	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {

		double r[] = new double[instance.classIndex()];

		for(int i = 0; i < m_NumIterations; i++) {
			double d[] = m_Classifiers[i].distributionForInstance(instance);
			for(int j = 0; j < d.length; j++) {
				r[j] += d[j];
			}
		}

		// turn votes into a [0,1] confidence for each label
		for(int j = 0; j < r.length; j++) {
			r[j] = r[j]/m_NumIterations;
		}

		return r;
	}

	@Override
	public Enumeration listOptions() {
		Vector newVector = new Vector();
		newVector.addElement(new Option("\t@Sets the number of models (default "+m_NumIterations+")", "I", 1, "-I <num>"));
		newVector.addElement(new Option("\t@Size of each bag, as a percentage of total training size (default "+m_BagSizePercent+")", "P", 1, "-P <size percentage>"));
		newVector.addElement(new Option("\t@Random number seed for sampling (default "+m_Seed+")", "S", 1, "-S <seed>"));

		Enumeration enu = super.listOptions();
		while (enu.hasMoreElements()) {
			newVector.addElement(enu.nextElement());
		}

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		try { m_Seed = Integer.parseInt(Utils.getOption('S',options)); } catch(Exception e) { }
		try { m_NumIterations = Integer.parseInt(Utils.getOption('I',options)); } catch(Exception e) { }
		try { m_BagSizePercent = Integer.parseInt(Utils.getOption('P',options)); } catch(Exception e) { }
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 6];
		int current = 0;
		options[current++] = "-S";
		options[current++] = String.valueOf(m_Seed);
		options[current++] = "-I";
		options[current++] = String.valueOf(m_NumIterations);
		options[current++] = "-P";
		options[current++] = String.valueOf(m_BagSizePercent);
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;
	}

}
