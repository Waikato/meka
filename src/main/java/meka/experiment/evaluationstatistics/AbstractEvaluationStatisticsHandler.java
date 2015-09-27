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

import meka.experiment.events.LogEvent;
import meka.experiment.events.LogListener;
import weka.core.Option;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

/**
 * Ancestor for handlers.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractEvaluationStatisticsHandler
  implements EvaluationStatisticsHandler {

	private static final long serialVersionUID = -1090631157162943295L;

	/** the listeners. */
	protected HashSet<LogListener> m_LogListeners = new HashSet<>();

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
	 * Adds the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void addLogListener(LogListener l) {
		m_LogListeners.add(l);
	}

	/**
	 * Remove the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void removeLogListener(LogListener l) {
		m_LogListeners.remove(l);
	}

	/**
	 * For logging messages. Uses stderr if no listeners defined.
	 *
	 * @param msg       the message to output
	 */
	protected synchronized void log(String msg) {
		LogEvent e;

		if (m_LogListeners.size() == 0) {
			System.err.println(msg);
			return;
		}

		e = new LogEvent(this, msg);
		for (LogListener l: m_LogListeners)
			l.logMessage(e);
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
