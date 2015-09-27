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
 * LogEvent.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.events;

import java.util.EventObject;

/**
 * Event that contains a log message.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class LogEvent
  extends EventObject {

	private static final long serialVersionUID = 7732581989591408787L;

	/** the log message. */
	protected String m_Message;

	/**
	 * Gets called when the experiment starts on a new evaluation.
	 *
	 * @param source        the object that triggered the event
	 * @param message       the log message
	 */
	public LogEvent(Object source, String message) {
		super(source);

		m_Message = message;
	}

	/**
	 * Returns the message.
	 *
	 * @return      the message
	 */
	public String getMessage() {
		return m_Message;
	}
}
