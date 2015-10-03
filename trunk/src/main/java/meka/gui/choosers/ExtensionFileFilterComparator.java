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
 * ExtensionFileFilterComparator.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.choosers;

import weka.gui.ExtensionFileFilter;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for file filters.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExtensionFileFilterComparator
  implements Serializable, Comparator<ExtensionFileFilter> {

	private static final long serialVersionUID = 6579446866620872706L;

	/**
	 * Compares the two filters.
	 *
	 * @param o1        the first filter
	 * @param o2        the second filter
	 * @return          less than 0, 0, or greater than 0, if the first filter is less than,
	 *                  equal, or greater than the second one
	 */
	@Override
	public int compare(ExtensionFileFilter o1, ExtensionFileFilter o2) {
		int         result;
		int         i;

		result = 0;

		for (i = 0; i < o1.getExtensions().length && i < o2.getExtensions().length; i++) {
			result = o1.getExtensions()[i].compareTo(o2.getExtensions()[i]);
			if (result != 0)
				break;
		}

		if ((result == 0) && (o1.getExtensions().length != o2.getExtensions().length))
			result = new Integer(o1.getExtensions().length).compareTo(o2.getExtensions().length);

		return result;
	}
}
