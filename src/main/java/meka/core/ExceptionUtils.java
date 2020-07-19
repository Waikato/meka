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
 * ExceptionUtils.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.core;

import com.googlecode.jfilechooserbookmarks.core.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Helper class for throwables and exceptions.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExceptionUtils {

  /**
   * Returns the stacktrace of the throwable as string.
   *
   * @param t		the throwable to get the stacktrace for
   * @return		the stacktrace
   */
  public static String throwableToString(Throwable t) {
    return throwableToString(t, -1);
  }

  /**
   * Returns the stacktrace of the throwable as string.
   *
   * @param t		the throwable to get the stacktrace for
   * @param maxLines	the maximum number of lines to print, &lt;= 0 for all
   * @return		the stacktrace
   */
  public static String throwableToString(Throwable t, int maxLines) {
    StringWriter writer;
    StringBuilder	result;
    String[]		lines;
    int			i;

    writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));

    if (maxLines > 0) {
      result = new StringBuilder();
      lines  = writer.toString().split("\n");
      for (i = 0; i < maxLines; i++) {
	if (i > 0)
	  result.append("\n");
	result.append(lines[i]);
      }
    }
    else {
      result = new StringBuilder(writer.toString());
    }

    return result.toString();
  }

  /**
   * Outputs the stacktrace along with the message on stderr and returns a
   * combination of both of them as string.
   *
   * @param source	the object that generated the exception, can be null
   * @param msg		the message for the exception
   * @param t		the exception
   * @return		the full error message (message + stacktrace)
   */
  public static String handleException(Object source, String msg, Throwable t) {
    return handleException(source, msg, t, false);
  }

  /**
   * Generates a string from the stacktrace along with the message and returns
   * that. Depending on the silent flag, this string is also output on stderr.
   *
   * @param source	the object that generated the exception, can be null
   * @param msg		the message for the exception
   * @param t		the exception
   * @param silent	if true then the generated message is not forwarded
   * 			to the source's logger
   * @return		the full error message (message + stacktrace)
   */
  public static String handleException(Object source, String msg, Throwable t, boolean silent) {
    String	result;

    result = msg.trim() + "\n" + Utils.throwableToString(t);
    if (!silent) {
      if (source != null)
	      System.err.println(source.getClass().getName());
	    System.err.println(result);
    }

    return result;
  }
}
