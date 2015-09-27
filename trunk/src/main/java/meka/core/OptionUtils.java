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

import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;

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
	 * Parses a File option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static File parse(String[] options, char option, File defValue) throws Exception {
		return parse(options, "" + option, defValue);
	}

	/**
	 * Parses a File option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static File parse(String[] options, String option, File defValue) throws Exception {
		String value = Utils.getOption(option, options);
		if (value.isEmpty())
			return defValue;
		else
			return new File(value);
	}

	/**
	 * Parses an OptionHandler option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static OptionHandler parse(String[] options, char option, OptionHandler defValue) throws Exception {
		return parse(options, "" + option, defValue);
	}

	/**
	 * Parses an OptionHandler option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static OptionHandler parse(String[] options, String option, OptionHandler defValue) throws Exception {
		String value = Utils.getOption(option, options);
		if (value.isEmpty())
			return defValue;
		else
			return OptionUtils.fromCommandLine(OptionHandler.class, value);
	}

	/**
	 * Parses an array option, returns all the occurrences of the option as a string array.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static String[] parse(String[] options, char option) throws Exception {
		return parse(options, "" + option);
	}

	/**
	 * Parses an array option, returns all the occurrences of the option as a string array.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static String[] parse(String[] options, String option) throws Exception {
		List<String> result = new ArrayList<>();
		while (Utils.getOptionPos(option, options) > -1)
			result.add(Utils.getOption(option, options));
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Parses an array option, returns all the occurrences of the option as a string array.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @return              the parsed value (or default value if option not present)
	 * @param cls           the class type to use (requires a constructor that takes a string)
	 * @throws Exception    if parsing of value fails
	 */
	public static <T> T[] parse(String[] options, char option, Class<T> cls) throws Exception {
		return parse(options, "" + option, cls);
	}

	/**
	 * Parses an array option, returns all the occurrences of the option as a string array.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @return              the parsed value (or default value if option not present)
	 * @param cls           the class type to use (requires a constructor that takes a string)
	 * @throws Exception    if parsing of value fails
	 */
	public static <T> T[] parse(String[] options, String option, Class<T> cls) throws Exception {
		Constructor constr = cls.getConstructor(String.class);
		if (constr == null)
			throw new IllegalArgumentException("Class '" + cls.getName() + "' does not have a constructor that takes a String!");
		// gather all options
		List<String> list = new ArrayList<>();
		while (Utils.getOptionPos(option, options) > -1)
			list.add(Utils.getOption(option, options));
		// convert to type
		Object result = Array.newInstance(cls, list.size());
		for (int i = 0; i < list.size(); i++) {
			try {
				Array.set(result, i, constr.newInstance(Utils.getOption(option, options)));
			}
			catch (Exception e) {
				System.err.println("Failed to instantiate class '" + cls.getName() + "' with string value: " + list.get(i));
			}
		}
		return (T[]) result;
	}

	/**
	 * Adds the int value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, int value) {
		add(options, "" + option, value);
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
		add(options, "" + option, value);
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
		add(options, "" + option, value);
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
		add(options, "" + option, value);
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
		add(options, "" + option, value);
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
	 * Adds the boolean flag (if true) to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, boolean value) {
		add(options, "" + option, value);
	}

	/**
	 * Adds the boolean flag (if true) to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, String option, boolean value) {
		if (value)
			options.add("-" + option);
	}

	/**
	 * Adds the File value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, File value) {
		add(options, "" + option, value);
	}

	/**
	 * Adds the File value to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, String option, File value) {
		options.add("-" + option);
		options.add("" + value);
	}

	/**
	 * Adds the OptionHandler to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, OptionHandler value) {
		add(options, "" + option, value);
	}

	/**
	 * Adds the OptionHandler to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, String option, OptionHandler value) {
		options.add("-" + option);
		options.add("" + Utils.toCommandLine(value));
	}

	/**
	 * Adds the array to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, char option, Object value) {
		add(options, "" + option, value);
	}

	/**
	 * Adds the array to the options.
	 *
	 * @param options   the current list of options to extend
	 * @param option    the option (without the leading dash)
	 * @param value     the current value
	 */
	public static void add(List<String> options, String option, Object value) {
		if (!value.getClass().isArray())
			throw new IllegalArgumentException("Value is not an array!");
		for (int i = 0; i < Array.getLength(value); i++) {
			Object element = Array.get(value, i);
			if (element instanceof OptionHandler) {
				add(options, option, (OptionHandler) element);
			}
			else {
				options.add("-" + option);
				options.add("" + value);
			}
		}
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
	 * Adds an Option for a flag to the list of options.
	 *
	 * @param options   the options to extend
	 * @param text      the description
	 * @param flag      the flag (no dash)
	 */
	public static void addFlag(Vector options, String text, char flag) {
		addFlag(options, text, "" + flag);
	}

	/**
	 * Adds an Option for a flag to the list of options.
	 *
	 * @param options   the options to extend
	 * @param text      the description
	 * @param flag      the flag (no dash)
	 */
	public static void addFlag(Vector options, String text, String flag) {
		options.add(new Option("\t" + text, flag, 0, "-" + flag));
	}

	/**
	 * Adds an Option for a flag to the list of options.
	 *
	 * @param options   the options to extend
	 * @param text      the description
	 * @param option    the option (no dash)
	 */
	public static void addOption(Vector options, String text, String defValue, char option) {
		addOption(options, text, defValue, "" + option);
	}

	/**
	 * Adds an Option for a flag to the list of options.
	 *
	 * @param options   the options to extend
	 * @param text      the description
	 * @param option    the option (no dash)
	 */
	public static void addOption(Vector options, String text, String defValue, String option) {
		options.add(new Option("\t" + text + "\n\t(default: " + defValue + ")", option, 0, "-" + option + " <value>"));
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

	/**
	 * Turns a commandline into an object.
	 *
	 * @param cls           the class that the commandline is expected to be
	 * @param cmdline       the commandline to parse
	 * @return              the object, null if failed to instantiate
	 * @throws Exception    if parsing fails
	 */
	public static <T> T fromCommandLine(Class<T> cls, String cmdline) throws Exception {
		String[]    options;
		String      classname;

		options    = Utils.splitOptions(cmdline);
		classname  = options[0];
		options[0] = "";
		return (T) Utils.forName(cls, classname, options);
	}
}
