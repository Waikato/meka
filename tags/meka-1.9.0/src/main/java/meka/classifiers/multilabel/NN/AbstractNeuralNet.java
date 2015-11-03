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
import meka.core.OptionUtils;
import weka.core.Option;
import weka.core.Randomizable;

import java.util.*;

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
		return m_R;
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
		Vector result = new Vector();
		result.addElement(new Option("\tSets the number of hidden units\n\tdefault: 10", "H", 1, "-H <value>"));
		result.addElement(new Option("\tSets the maximum number of epochs\n\tdefault: 1000\t(auto-cut-out)", "E", 1, "-E <value>"));
		result.addElement(new Option("\tSets the learning rate (tyically somewhere between 'very small' and 0.1)\n\tdefault: 0.1", "r", 1, "-r <value>"));
		result.addElement(new Option("\tSets the momentum (typically somewhere between 0.1 and 0.9)\n\tdefault: 0.1", "m", 1, "-m <value>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setH(OptionUtils.parse(options, 'H', 10));
		setE(OptionUtils.parse(options, 'E', 1000));
		setLearningRate(OptionUtils.parse(options, 'r', 0.1));
		setMomentum(OptionUtils.parse(options, 'm', 0.1));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'H', getH());
		OptionUtils.add(result, 'E', getE());
		OptionUtils.add(result, 'r', getLearningRate());
		OptionUtils.add(result, 'm', getMomentum());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

}
