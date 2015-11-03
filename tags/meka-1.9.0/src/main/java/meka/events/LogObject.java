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
 * LogObject.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.events;

import meka.core.ExceptionUtils;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Ancestor for objects that support logging.
 * Debug mode can be enabled using boolean system property 'meka.exec.debug'.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class LogObject
  implements Serializable, LogSupporter {

	private static final long serialVersionUID = -3814825277914734502L;

	/** whether to run code in DEBUG mode */
	protected boolean m_Debug = System.getProperty("meka.exec.debug", "false").equals("true");

	/** the listeners. */
	protected transient HashSet<LogListener> m_LogListeners;

	/**
	 * Returns the log listeners. Instantiates them if neccessary.
	 *
	 * @return              the listeners
	 */
	protected HashSet<LogListener> getLogListeners() {
		if (m_LogListeners == null)
			m_LogListeners = new HashSet<>();
		return m_LogListeners;
	}

	/**
	 * Adds the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void addLogListener(LogListener l) {
		getLogListeners().add(l);
	}

	/**
	 * Remove the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void removeLogListener(LogListener l) {
		getLogListeners().remove(l);
	}

	/**
	 * For logging messages. Uses stderr if no listeners defined.
	 *
	 * @param msg       the message to output
	 */
	public synchronized void log(String msg) {
		LogEvent e;

		debug(msg);

		if (getLogListeners().size() == 0) {
			System.err.println(msg);
			return;
		}

		e = new LogEvent(this, msg);
		for (LogListener l: getLogListeners())
			l.logMessage(e);
	}

	/**
	 * For debugging messages. Uses stderr.
	 *
	 * @param msg       the message to output
	 */
	public synchronized void debug(String msg) {
		if (m_Debug)
			System.err.println("[DEBUG] " + getClass().getName() + " - " + msg);
	}

	/**
	 * Logs the stacktrace along with the message on stderr and returns a
	 * combination of both of them as string.
	 *
	 * @param msg		the message for the exception
	 * @param t		the exception
	 * @return		the full error message (message + stacktrace)
	 */
	public String handleException(String msg, Throwable t) {
		String    result;

		result = ExceptionUtils.handleException(this, msg, t, false);
		log(result);

		return result;
	}
}
