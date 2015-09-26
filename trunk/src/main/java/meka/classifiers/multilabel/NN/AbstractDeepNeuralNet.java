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

import meka.core.OptionUtils;
import weka.core.Option;

import java.util.*;

/**
 * AbstractDeepNeuralNet.java - Extends AbstractNeuralNet with depth options. 
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version December 2012
 */
public abstract class AbstractDeepNeuralNet extends AbstractNeuralNet  {

	private static final long serialVersionUID = 5416731163612885485L;

	protected int m_N = 2;

	public int getN() { 
		return m_N;
	}

	public void setN(int n) { 
		m_N = n;
	}

	public String nTipText() {
		return "The number of RBMs.";
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tSets the number of RBMs\n\tdefault: 2", "N", 1, "-N <value>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setN(OptionUtils.parse(options, 'N', 2));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
	  	List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'N', getN());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}
}

