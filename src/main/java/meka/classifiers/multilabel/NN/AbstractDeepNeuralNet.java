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

import weka.core.Option;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

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

	/*
	public void setN(int n) { 
		m_N = n;
	}
	*/

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the number of RBMs\n\tdefault: "+m_N, "N", 1, "-N <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		m_N = (Utils.getOptionPos('N',options) >= 0) ? Integer.parseInt(Utils.getOption('N', options)) : m_N;

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

	  	ArrayList<String> result;
	  	result = new ArrayList<String>(Arrays.asList(super.getOptions()));
	  	result.add("-N");
		result.add(String.valueOf(m_N));
		return result.toArray(new String[result.size()]);
	}
}

