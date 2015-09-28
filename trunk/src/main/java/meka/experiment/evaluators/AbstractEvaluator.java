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

/**
 * TrainTestSplit.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluators;

import meka.core.OptionUtils;
import meka.events.LogObject;
import weka.core.Option;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Ancestor for evaluators.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractEvaluator
		extends LogObject
		implements Evaluator {

	private static final long serialVersionUID = 6318297857792961890L;

	/** whether the evaluation got stopped. */
	protected boolean m_Stopped;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public abstract String globalInfo();

	/**
	 * Initializes the evaluator.
	 *
	 * @return      null if successfully initialized, otherwise error message
	 */
	public String initialize() {
		m_Stopped = false;
		return null;
	}

	/**
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		return OptionUtils.toEnumeration(result);
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options to parse
	 * @throws Exception    if parsing fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
	}

	/**
	 * Returns the options.
	 *
	 * @return              the current options
	 */
	@Override
	public String[] getOptions() {
		return new String[0];
	}

	/**
	 * Stops the evaluation, if possible.
	 */
	public void stop() {
		m_Stopped = true;
	}
}
