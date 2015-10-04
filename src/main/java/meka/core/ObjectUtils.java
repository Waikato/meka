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
 * Utils.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.core;

import weka.core.SerializedObject;

import java.io.Serializable;
import java.util.List;

/**
 * Helper class for object-related operations.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ObjectUtils {

  /**
   * Flattens the list into a single, long string. The separator string gets
   * added between the objects, but not after the last one.
   *
   * @param lines	the lines to flatten
   * @param sep		the separator
   * @return		the generated string
   */
  public static String flatten(List lines, String sep) {
    return flatten(lines.toArray(new Object[lines.size()]), sep);
  }

  /**
   * Flattens the array into a single, long string. The separator string gets
   * added between the objects, but not after the last one. Uses the "toString()"
   * method of the objects to turn them into a string.
   *
   * @param lines	the lines to flatten
   * @param sep		the separator
   * @return		the generated string
   */
  public static String flatten(Object[] lines, String sep) {
    StringBuilder	result;
    int			i;

    result = new StringBuilder();

    for (i = 0; i < lines.length; i++) {
      if (i > 0)
	result.append(sep);
      result.append(lines[i].toString());
    }

    return result.toString();
  }

	/**
	 * Creates a deep copy of the given object (must be serializable!). Returns
	 * null in case of an error.
	 *
	 * @param o		the object to copy
	 * @return		the deep copy
	 */
	public static Object deepCopy(Object o) {
		Object		result;
		SerializedObject so;

		try {
			so     = new SerializedObject((Serializable) o);
			result = so.getObject();
		}
		catch (Exception e) {
			System.err.println("Failed to serialize " + o.getClass().getName() + ":");
			e.printStackTrace();
			result = null;
		}

		return result;
	}
}
