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

package meka.classifiers.multilabel.NN;

import meka.classifiers.multilabel.ProblemTransformationMethod;
import weka.core.Option;
import weka.core.Utils;

import java.util.Enumeration;
import java.util.Vector;

/**
 * AbstractNeuralNet.java - Provides common options, constants, and other functions for NNs.
 * @author Jesse Read
 * @version December 2012
 */
public abstract class AbstractNeuralNet extends ProblemTransformationMethod  {

	/** number of hidden units */
	protected int m_H = 10;

	/** number of epochs  */
	protected int m_E = 1000;

	/** learning rate  */
	protected double m_R = 0.1;

	/** momentum  */
	protected double m_M = 0.1;

	public void setH(int h) { 
		m_H = h;
	}

	public int getH() { 
		return m_H;
	}

	public void setE(int n) { 
		m_E = n;
	}

	public int getE() { 
		return m_E;
	}

	@Override
	public String toString() {
		return "h="+getH()+", E="+getE();
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the number of hidden units\n\tdefault: "+getH(), "H", 1, "-H <value>"));
		newVector.addElement(new Option("\tSets the maximum number of epochs\n\tdefault: "+getE()+"\t(auto-cut-out)", "E", 1, "-E <value>"));
		newVector.addElement(new Option("\tSets the learning rate (tyically somewhere between 'very small' and 0.1)\n\tdefault: "+m_R, "r", 1, "-r <value>"));
		newVector.addElement(new Option("\tSets the momentum (typically somewhere between 0.1 and 0.9)\n\tdefault: "+m_M, "m", 1, "-m <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		if(Utils.getOptionPos('H',options) >= 0) {
			setH(Integer.parseInt(Utils.getOption('H', options)));
		}

		if(Utils.getOptionPos('E',options) >= 0) {
			setE(Integer.parseInt(Utils.getOption('E', options)));
		}

		m_R = (Utils.getOptionPos('r',options) >= 0) ? Double.parseDouble(Utils.getOption('r', options)) : m_R;

		m_M = (Utils.getOptionPos('m',options) >= 0) ? Double.parseDouble(Utils.getOption('m', options)) : m_M;

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 8];
		int current = 0;
		options[current++] = "-H";
		options[current++] = String.valueOf(getH());
		options[current++] = "-E";
		options[current++] = String.valueOf(getE());
		options[current++] = "-r";
		options[current++] = String.valueOf(m_R);
		options[current++] = "-m";
		options[current++] = String.valueOf(m_M);
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;

	}

}
