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
 * SortableTableModel.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import javax.swing.table.TableModel;

/**
 * Interface for table models that are sortable.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4584 $
 */
public interface SortableTableModel
		extends TableModel {

	/**
	 * Sets the base model to use.
	 *
	 * @param value       the base model
	 */
	public void setUnsortedModel(TableModel value);

	/**
	 * returns the underlying model, can be null.
	 *
	 * @return            the current model
	 */
	public TableModel getUnsortedModel();

	/**
	 * returns whether the table was sorted.
	 *
	 * @return        true if the table was sorted
	 */
	public boolean isSorted();

	/**
	 * Returns the sort column.
	 *
	 * @return		the sort column
	 */
	public int getSortColumn();

	/**
	 * Returns whether sorting is ascending or not.
	 *
	 * @return		true if ascending
	 * @see		#isSorted()
	 * @see		#getSortColumn()
	 */
	public boolean isAscending();

	/**
	 * Returns the actual underlying row the given visible one represents. Useful
	 * for retrieving "non-visual" data that is also stored in a TableModel.
	 *
	 * @param visibleRow	the displayed row to retrieve the original row for
	 * @return		the original row
	 */
	public int getActualRow(int visibleRow);

	/**
	 * Returns the "visible" row derived from row in the actual table model.
	 *
	 * @param internalRow	the row in the actual model
	 * @return		the row in the sorted model, -1 in case of an error
	 */
	public int getDisplayRow(int internalRow);

	/**
	 * sorts the table over the given column (ascending).
	 *
	 * @param columnIndex     the column to sort over
	 */
	public void sort(int columnIndex);

	/**
	 * sorts the table over the given column, either ascending or descending.
	 *
	 * @param columnIndex     the column to sort over
	 * @param ascending       ascending if true, otherwise descending
	 */
	public void sort(int columnIndex, boolean ascending);
}
