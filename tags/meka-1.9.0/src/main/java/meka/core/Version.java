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
 * Version.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.core;

import java.io.InputStream;

/**
 * For handling the MEKA version.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Version {

	/** the file name. */
	public final static String FILENAME = "meka/core/version.txt";

	/**
	 * Returns the version of MEKA.
	 *
	 * @return      the version
	 */
	public static String getVersion() {
		StringBuilder   result;
		InputStream     input;
		int             c;

		result = new StringBuilder();
		try {
			input = ClassLoader.getSystemResourceAsStream(FILENAME);
			while ((c = input.read()) != -1) {
				result.append((char) c);
			}
		}
		catch (Exception e) {
			result.append("?.?.?");
		}
		return result.toString();
	}
}
