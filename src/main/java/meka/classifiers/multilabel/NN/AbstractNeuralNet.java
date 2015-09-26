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
import weka.core.Randomizable;

import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * AbstractNeuralNet.java - Provides common options, constants, and other functions for NNs.
 * @author Jesse Read
 * @version December 2012
 */
public abstract class AbstractNeuralNet extends ProblemTransformationMethod implements Randomizable {

	private static final long serialVersionUID = 5534606285449062819L;

	/** number of hidden units */
	protected int m_H = 10;

	/** number of epochs  */
	protected int m_E = 1000;

	/** learning rate  */
	protected double m_R = 0.1;

	/** momentum  */
	protected double m_M = 0.1;

	/** random seed */
	protected int m_Seed = 0;

	public void setH(int h) { 
		m_H = h;
	}

	public int getH() { 
		return m_H;
	}

	public String hTipText() {
		return "Number of hidden units.";
	}

	public void setE(int n) { 
		m_E = n;
	}

	public int getE() { 
		return m_E;
	}

	public String eTipText() {
		return "Number of epochs.";
	}

	public void setLearningRate(double r) { 
		m_R = r;
	}

	public double getLearningRate() { 
		return m_H;
	}

	public String learningRateTipText() {
		return "Learning Rate.";
	}

	public void setMomentum(double m) { 
		m_M = m;
	}

	public double getMomentum() { 
		return m_M;
	}

	public String momentumTipText() {
		return "Momentum.";
	}

	@Override
	public int getSeed() {
		return m_Seed;
	}

	@Override
	public void setSeed(int s) {
		m_Seed = s;
	}

	public String seedTipText() {
		return "The seed value for randomizing the data.";
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
		else
			setH(10);

		if(Utils.getOptionPos('E',options) >= 0) {
			setE(Integer.parseInt(Utils.getOption('E', options)));
		}
		else
			setE(1000);

		m_R = (Utils.getOptionPos('r',options) >= 0) ? Double.parseDouble(Utils.getOption('r', options)) : 0.1;

		m_M = (Utils.getOptionPos('m',options) >= 0) ? Double.parseDouble(Utils.getOption('m', options)) : 0.1;

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		ArrayList<String> result;
	  	result = new ArrayList<String>();
	  	result.add("-H");
	  	result.add(String.valueOf(m_H));
		result.add("-E");
	  	result.add(String.valueOf(m_E));
		result.add("-r");
	  	result.add(String.valueOf(m_R));
		result.add("-m");
	  	result.add(String.valueOf(m_M));
		result.addAll(Arrays.asList(super.getOptions()));
		return result.toArray(new String[result.size()]);
	}

}
