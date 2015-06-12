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
 * TestHelper.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.test;

import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Some helper methods for tests.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class TestHelper {

	/** the stddev indicator. */
	public final static String PLUS_MINUS = "+/-";

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
	 * @param t		    the throwable to get the stacktrace for
	 * @param maxLines	the maximum number of lines to print, <= 0 for all
	 * @return		    the stacktrace
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
	 * Attempts to parse the string as double. In case this fails, TestCase.fail() is triggered.
	 *
	 * @param s         the string to parse
	 * @return          the number
	 */
	protected static double toDouble(String s) {
		try {
			return Double.parseDouble(s);
		}
		catch (Exception e) {
			TestCase.fail("Failed to parse double: " + s + "\n" + throwableToString(e));
			return Double.NaN;   // will never be reached
		}
	}

	/**
	 * Tests whether numbers in the string are the same up to the specified decimals.
	 * Can be separated by "+/-".
	 *
	 * @param expected      the expected string
	 * @param actual        the generated string
	 * @param decimals      the number of decimals to check
	 * @see                 #PLUS_MINUS
	 */
	public static void assertAlmostEquals(String expected, String actual, int decimals) {
		assertAlmostEquals(null, expected, actual, decimals);
	}

	/**
	 * Tests whether numbers in the string are the same up to the specified decimals.
	 * Can be separated by "+/-".
	 *
	 * @param message       the optional message to display in case of failure, can be null
	 * @param expected      the expected string
	 * @param actual        the generated string
	 * @param decimals      the number of decimals to check
	 * @see                 #PLUS_MINUS
	 */
	public static void assertAlmostEquals(String message, String expected, String actual, int decimals) {
		String  exp1;
		String  exp2;
		String  act1;
		String  act2;
		double  e1;
		double  e2;
		double  a1;
		double  a2;

		if (expected.contains(PLUS_MINUS) && (actual.contains(PLUS_MINUS))) {
			exp1 = expected.substring(0, expected.indexOf(PLUS_MINUS)).trim();
			exp2 = expected.substring(expected.indexOf(PLUS_MINUS) + PLUS_MINUS.length()).trim();
			act1 = actual.substring(0, actual.indexOf(PLUS_MINUS)).trim();
			act2 = actual.substring(actual.indexOf(PLUS_MINUS) + PLUS_MINUS.length()).trim();
			e1   = toDouble(exp1);
			e2   = toDouble(exp2);
			a1   = toDouble(act1);
			a2   = toDouble(act2);
			assertAlmostEquals(message + " (1st part)", e1, a1, decimals);
			assertAlmostEquals(message + " (2nd part)", e2, a2, decimals);
		}
		else {
			e1   = toDouble(expected);
			a1   = toDouble(actual);
			assertAlmostEquals(message, e1, a1, decimals);
		}
	}

	/**
	 * Tests whether numbers are the same up to the specified decimals.
	 *
	 * @param expected      the expected number
	 * @param actual        the generated number
	 * @param decimals      the number of decimals to check
	 */
	public static void assertAlmostEquals(double expected, double actual, int decimals) {
		assertAlmostEquals(null, new Double(expected), new Double(actual), decimals);
	}

	/**
	 * Tests whether numbers are the same up to the specified decimals.
	 *
	 * @param expected      the expected number
	 * @param actual        the generated number
	 * @param decimals      the number of decimals to check
	 */
	public static void assertAlmostEquals(Number expected, Number actual, int decimals) {
		assertAlmostEquals(null, expected, actual, decimals);
	}

	/**
	 * Tests whether numbers are the same up to the specified decimals.
	 *
	 * @param message       the optional message to display in case of failure, can be null
	 * @param expected      the expected number
	 * @param actual        the generated number
	 * @param decimals      the number of decimals to check
	 */
	public static void assertAlmostEquals(String message, double expected, double actual, int decimals) {
		assertAlmostEquals(message, new Double(expected), new Double(actual), decimals);
	}

	/**
	 * Tests whether numbers are the same up to the specified decimals.
	 *
	 * @param message       the optional message to display in case of failure, can be null
	 * @param expected      the expected number
	 * @param actual        the generated number
	 * @param decimals      the number of decimals to check
	 */
	public static void assertAlmostEquals(String message, Number expected, Number actual, int decimals) {
		double  exp;
		double  act;
		double  factor;

		factor = Math.pow(10, decimals);
		exp    = Math.round(expected.doubleValue() * factor) / factor;
		act    = Math.round(actual.doubleValue() * factor) / factor;
		if (message == null)
			TestCase.assertEquals(exp, act);
		else
			TestCase.assertEquals(message, exp, act);
	}
}
