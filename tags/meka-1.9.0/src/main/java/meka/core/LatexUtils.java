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
 * LatexUtils.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.core;

import gnu.trove.list.array.TCharArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper functions around LaTeX.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class LatexUtils {

	/**
	 * The characters to escape.
	 *
	 * @author FracPete (fracpete at waikato dot ac dot nz)
	 * @version $Revision$
	 */
	public enum Characters {
		BACKSLASH,
		PERCENTAGE,
		UNDERSCORE,
		DOLLAR,
		AMPERSAND,
		CARET
	}

	/**
	 * Ensures that the string is LaTeX compliant.
	 *
	 * @param input     the string to process
	 * @return          the compliant string
	 */
	public static String escape(String input) {
		return escape(input, Characters.values());
	}

	/**
	 * Ensures that the string is LaTeX compliant.
	 *
	 * @param input         the string to process
	 * @param characters    the characters to escape
	 * @return              the compliant string
	 */
	public static String escape(String input, Characters[] characters) {
		String 		    result;
		TCharArrayList	chars;
		List<String>    escaped;

		chars   = new TCharArrayList();
		escaped = new ArrayList<>();
		for (Characters ch: characters) {
			switch (ch) {
				case AMPERSAND:
					chars.add('&');
					escaped.add("\\&");
					break;
				case BACKSLASH:
					chars.add('\\');
					escaped.add("\\textbackslash ");
					break;
				case DOLLAR:
					chars.add('$');
					escaped.add("\\$");
					break;
				case UNDERSCORE:
					chars.add('_');
					escaped.add("\\_");
					break;
				case CARET:
					chars.add('^');
					escaped.add("$^\\wedge$");
					break;
				case PERCENTAGE:
					chars.add('%');
					escaped.add("\\%");
					break;
				default:
					throw new IllegalStateException("Unhandled character: " + ch);
			}
		}
		result = OptionUtils.backQuoteChars(input, chars.toArray(), escaped.toArray(new String[escaped.size()]));

		return result;
	}
}
