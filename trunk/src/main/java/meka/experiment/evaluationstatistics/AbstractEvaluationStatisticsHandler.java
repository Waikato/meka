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
 * AbstractEvaluationStatisticsHandler.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import meka.events.LogObject;
import weka.core.Option;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Ancestor for handlers.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractEvaluationStatisticsHandler
		extends LogObject
		implements EvaluationStatisticsHandler {

	private static final long serialVersionUID = -1090631157162943295L;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public abstract String globalInfo();

	/**
	 * Returns an enumeration of all the available options.
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		return new Vector().elements();
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options
	 * @throws Exception    never
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
	}

	/**
	 * Returns the options.
	 *
	 * @return              the options
	 */
	@Override
	public String[] getOptions() {
		return new String[0];
	}

	/**
	 * Gets called after the experiment finished.
	 *
	 * @return          null if successfully finished, otherwise error message
	 */
	public String finish() {
		return null;
	}
}
