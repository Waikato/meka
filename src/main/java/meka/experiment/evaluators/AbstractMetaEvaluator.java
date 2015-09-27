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
 * AbstractMetaEvaluator.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluators;

import meka.core.OptionUtils;
import weka.core.Option;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Ancestor for evaluators that wrap a base evaluator.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractMetaEvaluator
  extends AbstractEvaluator {

	private static final long serialVersionUID = -6851297570375542238L;

	/** the base evaluator. */
	protected Evaluator m_Evaluator = getDefaultEvaluator();

	/**
	 * Initializes the evaluator.
	 *
	 * @return      null if successfully initialized, otherwise error message
	 */
	@Override
	public String initialize() {
		String      result;

		result = super.initialize();
		if (result == null)
			result = m_Evaluator.initialize();

		return result;
	}

	/**
	 * Returns the default evaluator to use.
	 *
	 * @return          the default
	 */
	protected abstract Evaluator getDefaultEvaluator();

	/**
	 * Sets the evaluator to use.
	 *
	 * @param value     the evaluator
	 */
	public void setEvaluator(Evaluator value) {
		m_Evaluator = value;
	}

	/**
	 * Returns the evaluator in use.
	 *
	 * @return          the evaluator
	 */
	public Evaluator getEvaluator() {
		return m_Evaluator;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String evaluatorTipText() {
		return "The base evaluator to use.";
	}

	/**
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.add(result, super.listOptions());
		OptionUtils.addOption(result, evaluatorTipText(), getDefaultEvaluator().getClass().getName(), "base");
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
		setEvaluator((Evaluator) OptionUtils.parse(options, "base", getDefaultEvaluator()));
		super.setOptions(options);
	}

	/**
	 * Returns the options.
	 *
	 * @return              the current options
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, super.getOptions());
		OptionUtils.add(result, "base", getEvaluator());
		return OptionUtils.toArray(result);
	}

	/**
	 * Stops the evaluation, if possible.
	 */
	@Override
	public void stop() {
		m_Evaluator.stop();
		super.stop();
	}
}
