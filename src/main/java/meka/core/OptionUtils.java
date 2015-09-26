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
 * OptionUtils.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.core;

import weka.core.Utils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Helper class for option parsing.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class OptionUtils {

	/**
	 * Parses an int option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static int parse(String[] options, char option, int defValue) throws Exception {
		return parse(options, "" + option, defValue);
	}

	/**
	 * Parses an int option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static int parse(String[] options, String option, int defValue) throws Exception {
		String value = Utils.getOption(option, options);
		if (value.isEmpty())
			return defValue;
		else
			return Integer.parseInt(value);
	}

	/**
	 * Parses an long option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static long parse(String[] options, char option, long defValue) throws Exception {
		return parse(options, "" + option, defValue);
	}

	/**
	 * Parses a long option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static long parse(String[] options, String option, long defValue) throws Exception {
		String value = Utils.getOption(option, options);
		if (value.isEmpty())
			return defValue;
		else
			return Long.parseLong(value);
	}

	/**
	 * Parses a float option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static float parse(String[] options, char option, float defValue) throws Exception {
		return parse(options, "" + option, defValue);
	}

	/**
	 * Parses a float option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static float parse(String[] options, String option, float defValue) throws Exception {
		String value = Utils.getOption(option, options);
		if (value.isEmpty())
			return defValue;
		else
			return Float.parseFloat(value);
	}

	/**
	 * Parses a double option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static double parse(String[] options, char option, double defValue) throws Exception {
		return parse(options, "" + option, defValue);
	}

	/**
	 * Parses a double option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static double parse(String[] options, String option, double defValue) throws Exception {
		String value = Utils.getOption(option, options);
		if (value.isEmpty())
			return defValue;
		else
			return Double.parseDouble(value);
	}

	/**
	 * Parses a String option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static String parse(String[] options, char option, String defValue) throws Exception {
		return parse(options, "" + option, defValue);
	}

	/**
	 * Parses a String option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static String parse(String[] options, String option, String defValue) throws Exception {
		String value = Utils.getOption(option, options);
		if (value.isEmpty())
			return defValue;
		else
			return value;
	}

	/**
	 * Adds the int value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, int value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the int value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, String option, int value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the long value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, long value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the long value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, String option, long value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the float value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, float value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the float value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, String option, float value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the double value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, double value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the double value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, String option, double value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the String value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, String value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the String value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, String option, String value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the "super" options to the current list.
	 *
	 * @param options       the current options
	 * @param superOptions  the "super" options to add
	 */
	public static void add(List<String> options, String[] superOptions) {
		options.addAll(Arrays.asList(superOptions));
	}

	/**
	 * Adds the option description of the super class.
	 *
	 * @param current       the current option descriptions to extend
	 * @param superOptions  the "super" descriptions
	 */
	public static void add(Vector current, Enumeration superOptions) {
		while (superOptions.hasMoreElements())
			current.addElement(superOptions.nextElement());
	}

	/**
	 * Turns the list of options into an array.
	 *
	 * @param options       the list of options to convert
	 * @return              the generated array
	 */
	public static String[] toArray(List<String> options) {
		return options.toArray(new String[options.size()]);
	}

	/**
	 * Returns the option descriptions as an enumeration.
	 *
	 * @param options       the descriptions
	 * @return              the enumeration
	 */
	public static Enumeration toEnumeration(Vector options) {
		return options.elements();
	}
}
