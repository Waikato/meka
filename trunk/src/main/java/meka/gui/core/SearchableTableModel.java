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
 * SearchableTableModel.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import javax.swing.table.TableModel;

/**
 * Interface for TableModels that can be searched.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4584 $
 */
public interface SearchableTableModel
		extends TableModel {

	/**
	 * Returns the actual row in the model.
	 *
	 * @param rowIndex	the row of the currently displayed data
	 * @return		the row in the underlying data
	 */
	public int getActualRow(int rowIndex);

	/**
	 * Returns the actual row count in the model.
	 *
	 * @return		the row count in the underlying data
	 */
	public int getActualRowCount();

	/**
	 * Performs a search for the given string. Limits the display of rows to
	 * ones containing the search string.
	 *
	 * @param searchString	the string to search for
	 * @param regexp		whether to perform regular expression matching
	 * 				or just plain string comparison
	 */
	public void search(String searchString, boolean regexp);

	/**
	 * Returns the current search string.
	 *
	 * @return		the search string, null if not filtered
	 */
	public String getSeachString();

	/**
	 * Returns whether the last search was a regular expression based one.
	 *
	 * @return		true if last search was a reg exp one
	 */
	public boolean isRegExpSearch();
}
