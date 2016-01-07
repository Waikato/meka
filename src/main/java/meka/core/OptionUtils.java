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

import weka.core.ClassDiscovery;
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
	 * Parses a double option, uses default if option is missing.
	 *
	 * @param options       the option array to use
	 * @param option        the option to look for in the options array (no leading dash)
	 * @param defValue      the default value
	 * @return              the parsed value (or default value if option not present)
	 * @throws Exception    if parsing of value fails
	 */
	public static boolean parse(String[] options, char option, boolean defValue) throws Exception {
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
	public static boolean parse(String[] options, String option, boolean defValue) throws Exception {
		String value = Utils.getOption(option, options);
		if (value.isEmpty())
			return defValue;
		else
			return Boolean.parseBoolean(value);
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
		// gather all options
		List<String> list = new ArrayList<>();
		while (Utils.getOptionPos(option, options) > -1)
			list.add(Utils.getOption(option, options));
		// Optionhandler?
		if (ClassDiscovery.hasInterface(OptionHandler.class, cls)) {
			Object result = Array.newInstance(cls, list.size());
			for (int i = 0; i < list.size(); i++) {
				try {
					Array.set(result, i, OptionUtils.fromCommandLine(cls, list.get(i)));
				} catch (Exception e) {
					System.err.println("Failed to instantiate class '" + cls.getName() + "' with command-line: " + list.get(i));
				}
			}
			return (T[]) result;
		}
		else {
			Constructor constr = cls.getConstructor(String.class);
			if (constr == null)
				throw new IllegalArgumentException("Class '" + cls.getName() + "' does not have a constructor that takes a String!");
			// convert to type
			Object result = Array.newInstance(cls, list.size());
			for (int i = 0; i < list.size(); i++) {
				try {
					Array.set(result, i, constr.newInstance(list.get(i)));
				} catch (Exception e) {
					System.err.println("Failed to instantiate class '" + cls.getName() + "' with string value: " + list.get(i));
				}
			}
			return (T[]) result;
		}
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
				options.add("" + element);
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

	/**
	 * Returns the commandline string for the object.
	 *
	 * @param obj           the object to generate the commandline for
	 * @return              the commandline
	 * @see                 Utils#toCommandLine(Object)
	 */
	public static String toCommandLine(Object obj) {
		return Utils.toCommandLine(obj);
	}

	/**
	 * Converts specified characters into the string equivalents.
	 *
	 * @param string 	the string
	 * @param find	the characters to replace
	 * @param replace	the replacement strings for the characters
	 * @return 		the converted string
	 * @see		#unbackQuoteChars(String, String[], char[])
	 */
	public static String backQuoteChars(String string, char[] find, String[] replace) {
		int 		index;
		StringBuilder 	newStr;
		int			i;

		if (string == null)
			return string;

		for (i = 0; i < find.length; i++) {
			if (string.indexOf(find[i]) != -1 ) {
				newStr = new StringBuilder();
				while ((index = string.indexOf(find[i])) != -1) {
					if (index > 0)
						newStr.append(string.substring(0, index));
					newStr.append(replace[i]);
					if ((index + 1) < string.length())
						string = string.substring(index + 1);
					else
						string = "";
				}
				newStr.append(string);
				string = newStr.toString();
			}
		}

		return string;
	}

	/**
	 * Converts carriage returns and new lines in a string into \r and \n.
	 * Backquotes the following characters: ` " \ \t and %
	 *
	 * @param string 	the string
	 * @return 		the converted string
	 * @see		#unbackQuoteChars(String)
	 */
	public static String backQuoteChars(String string) {
		return backQuoteChars(
				string,
				new char[]  {'\\',   '\'',  '\t',  '\n',  '\r',  '"'},
				new String[]{"\\\\", "\\'", "\\t", "\\n", "\\r", "\\\""});
	}

	/**
	 * The inverse operation of backQuoteChars().
	 * Converts the specified strings into their character representations.
	 *
	 * @param string 	the string
	 * @param find	the string to find
	 * @param replace	the character equivalents of the strings
	 * @return 		the converted string
	 * @see		#backQuoteChars(String, char[], String[])
	 */
	public static String unbackQuoteChars(String string, String[] find, char[] replace) {
		int 		index;
		StringBuilder 	newStr;
		int[] 		pos;
		int			curPos;
		String 		str;
		int			i;

		if (string == null)
			return null;

		pos = new int[find.length];

		str = new String(string);
		newStr = new StringBuilder();
		while (str.length() > 0) {
			// get positions and closest character to replace
			curPos = str.length();
			index  = -1;
			for (i = 0; i < pos.length; i++) {
				pos[i] = str.indexOf(find[i]);
				if ( (pos[i] > -1) && (pos[i] < curPos) ) {
					index  = i;
					curPos = pos[i];
				}
			}

			// replace character if found, otherwise finished
			if (index == -1) {
				newStr.append(str);
				str = "";
			}
			else {
				newStr.append(str.substring(0, pos[index]));
				newStr.append(replace[index]);
				str = str.substring(pos[index] + find[index].length());
			}
		}

		return newStr.toString();
	}

	/**
	 * The inverse operation of backQuoteChars().
	 * Converts back-quoted carriage returns and new lines in a string
	 * to the corresponding character ('\r' and '\n').
	 * Also "un"-back-quotes the following characters: ` " \ \t and %
	 *
	 * @param string 	the string
	 * @return 		the converted string
	 * @see		#backQuoteChars(String)
	 */
	public static String unbackQuoteChars(String string) {
		return unbackQuoteChars(
				string,
				new String[]{"\\\\", "\\'", "\\t", "\\n", "\\r", "\\\""},
				new char[]{'\\', '\'', '\t', '\n', '\r', '"'});
	}

	/**
	 * Creates a shallow copy of the option handler, just using its options.
	 *
	 * @param obj           the object to copy
	 * @return              the copy, null if failed to copy
	 */
	public static OptionHandler shallowCopy(OptionHandler obj) {
		if (obj == null) {
			System.err.println("Cannot create shallow copy of null object!");
			return null;
		}

		try {
			return fromCommandLine(OptionHandler.class, toCommandLine(obj));
		}
		catch (Exception e) {
			System.err.println("Failed to create shallow copy of " + obj.getClass().getName() + ":");
			e.printStackTrace();
			return null;
		}
	}
}
