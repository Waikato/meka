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
 * Ancestor for objects that support logging..
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class LogObject
  implements Serializable, LogSupporter {

	private static final long serialVersionUID = -3814825277914734502L;

	/** the listeners. */
	protected transient HashSet<LogListener> m_LogListeners = new HashSet<>();

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
