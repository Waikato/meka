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

/*
 * SortableAndSearchableWrapperTableModel.java
 * Copyright (C) 2009-2018 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import gnu.trove.list.array.TIntArrayList;
import weka.core.InheritanceUtils;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Wraps around any table model and makes them automatically sortable and
 * searchable.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10824 $
 */
public class SortableAndSearchableWrapperTableModel
		extends AbstractTableModel
		implements TableModelListener, SortableTableModel, SearchableTableModel {

	/** for serialization. */
	private static final long serialVersionUID = 1379439060928152100L;

	/**
	 * Helper class for sorting the columns.
	 */
	public static class SortContainer
			implements Comparable<SortContainer> {

		/** the value to sort. */
		protected Comparable m_Value;

		/** the index of the value. */
		protected int m_Index;

		/**
		 * Initializes the container.
		 *
		 * @param value	the value to sort on
		 * @param index	the original index
		 */
		public SortContainer(Comparable value, int index) {
			super();

			m_Value = value;
			m_Index = index;
		}

		/**
		 * Returns the value to sort on.
		 *
		 * @return		the value
		 */
		public Comparable getValue() {
			return m_Value;
		}

		/**
		 * Returns the original index of the item.
		 *
		 * @return		the index
		 */
		public int getIndex() {
			return m_Index;
		}

		/**
		 * Compares this object with the specified object for order.  Returns a
		 * negative integer, zero, or a positive integer as this object is less
		 * than, equal to, or greater than the specified object. Null is considered
		 * smallest. If both values are null, then 0 is returned.
		 *
		 * @param o 	the object to be compared.
		 * @return  	a negative integer, zero, or a positive integer as this object
		 *			is less than, equal to, or greater than the specified object.
		 * @throws ClassCastException    if the specified object's type prevents it
		 *         				from being compared to this object.
		 */
		public int compareTo(SortContainer o) {
			if ((m_Value == null) || (o.getValue() == null)) {
				if (m_Value == o.getValue())
					return 0;
				if (m_Value == null)
					return -1;
				else
					return +1;
			}
			else {
				if (m_Value.getClass().equals(o.getValue().getClass()))
					return m_Value.compareTo(o.getValue());
				else
					return m_Value.toString().compareTo(o.getValue().toString());
			}
		}

		/**
		 * Indicates whether some other object is "equal to" this one.
		 *
		 * @param obj	the reference object with which to compare.
		 * @return		true if this object is the same as the obj argument;
		 * 			false otherwise.
		 * @throws ClassCastException    if the specified object's type prevents it
		 *         				from being compared to this object.
		 */
		@Override
		public boolean equals(Object obj) {
			return (compareTo((SortContainer) obj) == 0);
		}

		/**
		 * Returns a string representation of the sort container.
		 *
		 * @return		the string representation (value + index)
		 */
		@Override
		public String toString() {
			return "value=" + m_Value + ", index=" + m_Index;
		}
	}

	/** the actual table model. */
	protected TableModel m_Model;

	/** the mapping between displayed and actual index. */
	protected int[] m_SortedIndices;

	/** the sort column. */
	protected int m_SortColumn;

	/** whether sorting is ascending or descending. */
	protected boolean m_SortAscending;

	/** the mouse listener in use. */
	protected MouseListener m_MouseListener;

	/** the string that was searched for. */
	protected String m_SearchString;

	/** whether the search was a regular expression based one. */
	protected boolean m_RegExpSearch;

	/** the indices of the rows to display that match a search string. */
	protected TIntArrayList m_DisplayIndices;

	/** whether a column is numeric. */
	protected boolean[] m_ColumnIsNumeric;

	/**
	 * initializes with no model.
	 */
	public SortableAndSearchableWrapperTableModel() {
		this(null);
	}

	/**
	 * initializes with the given model.
	 *
	 * @param model       the model to initialize the sorted model with
	 */
	public SortableAndSearchableWrapperTableModel(TableModel model) {
		super();

		m_MouseListener   = null;
		m_SortedIndices   = null;
		m_DisplayIndices  = null;
		m_ColumnIsNumeric = null;

		setUnsortedModel(model);
	}

	/**
	 * Sets the model to use. Discards any sorting.
	 *
	 * @param value       the model to use
	 */
	public void setUnsortedModel(TableModel value) {
		setUnsortedModel(value, false);
	}

	/**
	 * Sets the model to use.
	 *
	 * @param value       	the model to use
	 * @param restoreSorting	whether to restore the sorting (if any)
	 */
	public void setUnsortedModel(TableModel value, boolean restoreSorting) {
		boolean	sorted;
		int		sortCol;
		boolean	asc;

		sorted  = isSorted();
		sortCol = getSortColumn();
		asc     = isAscending();

		m_Model = value;

		// initialize indices
		initialize();

		// restore sorting
		if (restoreSorting && sorted)
			sort(sortCol, asc);

		fireTableDataChanged();
	}

	/**
	 * returns the underlying model, can be null.
	 *
	 * @return            the current model
	 */
	public TableModel getUnsortedModel() {
		return m_Model;
	}

	/**
	 * Initializes indices etc.
	 */
	protected void initialize() {
		if (getUnsortedModel() == null) {
			m_SortedIndices   = null;
			m_DisplayIndices  = null;
			m_ColumnIsNumeric = null;
		}
		else {
			initializeSortIndices();
			initializeColumnTypes();
			m_DisplayIndices = null;
			m_SortColumn     = -1;
			m_SortAscending  = true;
			getUnsortedModel().addTableModelListener(this);
			if (m_SearchString != null)
				search(m_SearchString, m_RegExpSearch);
		}
	}

	/**
	 * Determines which column is a numeric one (skipped in search).
	 */
	protected void initializeColumnTypes() {
		int		i;
		Class cls;

		m_ColumnIsNumeric = new boolean[getColumnCount()];
		for (i = 0; i < m_ColumnIsNumeric.length; i++) {
			cls = getColumnClass(i);
			if (    (cls == Byte.class)
					|| (cls == Short.class)
					|| (cls == Integer.class)
					|| (cls == Long.class)
					|| (cls == Float.class)
					|| (cls == Double.class) )
				m_ColumnIsNumeric[i] = true;
		}
	}

	/**
	 * Returns whether the specified column is numeric.
	 *
	 * @param colIndex	the index of the column
	 * @return		true if numeric
	 * @see		#m_ColumnIsNumeric
	 * @see		#initializeColumnTypes()
	 */
	protected boolean isColumnNumeric(int colIndex) {
		if (m_ColumnIsNumeric == null)
			return false;
		else
			return m_ColumnIsNumeric[colIndex];
	}

	/**
	 * (re-)initializes the indices.
	 */
	protected void initializeSortIndices() {
		int       i;

		m_SortedIndices = new int[getUnsortedModel().getRowCount()];
		for (i = 0; i < m_SortedIndices.length; i++)
			m_SortedIndices[i] = i;
	}

	/**
	 * returns whether the table was sorted.
	 *
	 * @return        true if the table was sorted
	 */
	public boolean isSorted() {
		return (m_SortColumn > -1);
	}

	/**
	 * Returns the sort column.
	 *
	 * @return		the sort column
	 */
	public int getSortColumn() {
		return m_SortColumn;
	}

	/**
	 * Returns whether sorting is ascending or not.
	 *
	 * @return		true if ascending
	 * @see		#isSorted()
	 * @see		#getSortColumn()
	 */
	public boolean isAscending() {
		return m_SortAscending;
	}

	/**
	 * whether the model is initialized.
	 *
	 * @return            true if the model is not null and the sort indices
	 *                    match the number of rows
	 */
	protected boolean isInitialized() {
		return (getUnsortedModel() != null);
	}

	/**
	 * Returns the actual underlying row the given visible one represents. Useful
	 * for retrieving "non-visual" data that is also stored in a TableModel.
	 *
	 * @param visibleRow	the displayed row to retrieve the original row for
	 * @return		the original row
	 */
	public int getActualRow(int visibleRow) {
		int		result;

		result = -1;

		if (isInitialized()) {
			if (m_DisplayIndices != null)
				result = m_SortedIndices[m_DisplayIndices.get(visibleRow)];
			else
				result = m_SortedIndices[visibleRow];
		}

		return result;
	}

	/**
	 * Returns the "visible" row derived from row in the actual table model.
	 *
	 * @param internalRow	the row in the actual model
	 * @return		the row in the sorted model, -1 in case of an error
	 */
	public int getDisplayRow(int internalRow) {
		int		result;
		int		sorted;
		int		i;

		result = -1;

		if (isInitialized()) {
			// look up sorted index
			sorted = -1;
			for (i = 0; i < m_SortedIndices.length; i++) {
				if (m_SortedIndices[i] == internalRow) {
					sorted = i;
					break;
				}
			}

			// look up visible index
			if (m_DisplayIndices != null) {
				for (i = 0; i < m_DisplayIndices.size(); i++) {
					if (m_DisplayIndices.get(i) == sorted) {
						result = i;
						break;
					}
				}
			}
			else {
				result = sorted;
			}
		}

		return result;
	}

	/**
	 * Returns the most specific superclass for all the cell values in the
	 * column.
	 *
	 * @param columnIndex     the index of the column
	 * @return                the class of the specified column
	 */
	@Override
	public Class getColumnClass(int columnIndex) {
		if (!isInitialized())
			return null;
		else
			return getUnsortedModel().getColumnClass(columnIndex);
	}

	/**
	 * Returns the number of columns in the model.
	 *
	 * @return          the number of columns in the model
	 */
	public int getColumnCount() {
		if (!isInitialized())
			return 0;
		else
			return getUnsortedModel().getColumnCount();
	}

	/**
	 * Returns the name of the column at columnIndex.
	 *
	 * @param columnIndex   the column to retrieve the name for
	 * @return              the name of the specified column
	 */
	@Override
	public String getColumnName(int columnIndex) {
		if (!isInitialized())
			return null;
		else
			return getUnsortedModel().getColumnName(columnIndex);
	}

	/**
	 * Returns the number of rows in the model.
	 *
	 * @return              the number of rows in the model
	 */
	public int getRowCount() {
		int		result;

		result = 0;

		if (isInitialized()) {
			if (m_DisplayIndices == null)
				result = getUnsortedModel().getRowCount();
			else
				result = m_DisplayIndices.size();
		}

		return result;
	}

	/**
	 * Returns the value for the cell at columnIndex and rowIndex.
	 *
	 * @param rowIndex      the row
	 * @param columnIndex   the column
	 * @return              the value of the sepcified cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (!isInitialized())
			return null;
		else
			return getUnsortedModel().getValueAt(getActualRow(rowIndex), columnIndex);
	}

	/**
	 * Returns true if the cell at rowIndex and columnIndex is editable.
	 *
	 * @param rowIndex      the row
	 * @param columnIndex   the column
	 * @return              true if the cell is editable
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (!isInitialized())
			return false;
		else
			return getUnsortedModel().isCellEditable(getActualRow(rowIndex), columnIndex);
	}

	/**
	 * Sets the value in the cell at columnIndex and rowIndex to aValue.
	 *
	 * @param aValue        the new value of the cell
	 * @param rowIndex      the row
	 * @param columnIndex   the column
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (isInitialized())
			getUnsortedModel().setValueAt(aValue, getActualRow(rowIndex), columnIndex);
	}

	/**
	 * Returns the class of the column.
	 *
	 * @param columnIndex	the index of the column
	 * @return		the class of the column
	 */
	protected Class getColumnClassForComparison(int columnIndex) {
		return getUnsortedModel().getColumnClass(columnIndex);
	}

	/**
	 * sorts the table over the given column (ascending).
	 *
	 * @param columnIndex     the column to sort over
	 */
	public void sort(int columnIndex) {
		sort(columnIndex, true);
	}

	/**
	 * sorts the table over the given column, either ascending or descending.
	 *
	 * @param columnIndex     the column to sort over
	 * @param ascending       ascending if true, otherwise descending
	 */
	public void sort(int columnIndex, boolean ascending) {
		int       			columnType;
		int       			i;
		ArrayList<SortContainer> sorted;
		SortContainer		cont;
		Object value;

		// can we sort?
		if (    (!isInitialized())
				|| (getUnsortedModel().getRowCount() != m_SortedIndices.length) ) {

			System.out.println(
					this.getClass().getName() + ": Table model not initialized!");

			return;
		}

		// init
		m_DisplayIndices = null;
		m_SortColumn     = columnIndex;
		m_SortAscending  = ascending;
		initializeSortIndices();

		// no sorting?
		if (m_SortColumn == -1)
			return;

		// determine the column type: 0=other, 1=comparable
		if (InheritanceUtils.hasInterface(Comparable.class, getColumnClassForComparison(m_SortColumn)))
			columnType = 1;
		else
			columnType = 0;

		// create list for sorting
		sorted = new ArrayList<SortContainer>();
		for (i = 0; i < getRowCount(); i++) {
			value = getValueForComparison(m_SortedIndices[i], m_SortColumn);
			if (columnType == 0) {
				cont = new SortContainer((value == null) ? null : value.toString(), m_SortedIndices[i]);
			}
			else {
				if (m_ColumnIsNumeric[m_SortColumn] && (!(value instanceof Number))) {
					try {
						cont = new SortContainer(Double.parseDouble(value.toString()), m_SortedIndices[i]);
					}
					catch (Exception e) {
						try {
							cont = new SortContainer((Comparable) value, m_SortedIndices[i]);
						}
						catch (Exception ex) {
							cont = new SortContainer(value.toString(), m_SortedIndices[i]);
						}
					}
				}
				else {
					cont = new SortContainer((Comparable) value, m_SortedIndices[i]);
				}
			}
			sorted.add(cont);
		}
		Collections.sort(sorted);

		for (i = 0; i < sorted.size(); i++) {
			if (m_SortAscending)
				m_SortedIndices[i] = sorted.get(i).getIndex();
			else
				m_SortedIndices[i] = sorted.get(sorted.size() - 1 - i).getIndex();
		}

		sorted.clear();
		sorted = null;

		if (m_SearchString != null)
			search(m_SearchString, m_RegExpSearch);
	}

	/**
	 * Returns the value used in the comparison.
	 *
	 * @param row		the row of the cell
	 * @param column	the column of the cell
	 * @return		the cell value
	 */
	protected Object getValueForComparison(int row, int column) {
		return getUnsortedModel().getValueAt(row, column);
	}

	/**
	 * This fine grain notification tells listeners the exact range of cells,
	 * rows, or columns that changed.
	 *
	 * @param e       the event
	 */
	public void tableChanged(TableModelEvent e) {
		initializeSortIndices();
		if (isSorted())
			sort(m_SortColumn, m_SortAscending);

		fireTableChanged(e);
	}

	/**
	 * Adds a mouselistener to the header: left-click on the header sorts in
	 * ascending manner, using shift-left-click in descending manner.
	 *
	 * @param table       the table to add the listener to
	 */
	public void addMouseListenerToHeader(JTable table) {
		final SortableAndSearchableWrapperTableModel 	fModel;
		final JTable fTable;
		JTableHeader header;

		fModel = this;
		fTable = table;
		fTable.setColumnSelectionAllowed(false);
		header = fTable.getTableHeader();

		if (header != null) {
			if (m_MouseListener == null) {
				m_MouseListener = new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						TableColumnModel columnModel = fTable.getColumnModel();
						int viewColumn = columnModel.getColumnIndexAtX(e.getX());
						int column = fTable.convertColumnIndexToModel(viewColumn);
						if (MouseUtils.isLeftClick(e) && !e.isAltDown() && !e.isControlDown() && (column != -1))
							fModel.sort(column, !e.isShiftDown());
					}
				};
			}

			header.addMouseListener(m_MouseListener);
		}
	}

	/**
	 * Returns the mouse listener that got assigned to the header, if any.
	 *
	 * @return		the listener, can be null
	 */
	public MouseListener getHeaderMouseListener() {
		return m_MouseListener;
	}

	/**
	 * Returns the actual row count in the model.
	 *
	 * @return		the row count in the underlying data
	 */
	public int getActualRowCount() {
		return getUnsortedModel().getRowCount();
	}

	/**
	 * Tests whether the search matches the specified row.
	 * <br><br>
	 * Default implementation just checks against the strings that getValueAt(...)
	 * returns (using the toString() method of the returned objects). Derived
	 * classes should override this method in order to implement
	 * a proper/faster search functionality.
	 * Skips numeric columns.
	 *
	 * @param params	the search parameters
	 * @param row		the row of the underlying, unsorted model
	 * @return		true if the search matches this row
	 */
	public boolean isSearchMatch(SearchParameters params, int row) {
		boolean	result;
		Object value;
		int		i;

		result = false;

		for (i = 0; i < getColumnCount(); i++) {
			if (isColumnNumeric(i))
				continue;

			value = getUnsortedModel().getValueAt(row, i);

			if (value != null) {
				result = params.matches(value.toString());
				if (result)
					break;
			}
		}

		return result;
	}

	/**
	 * Performs a search for the given string. Limits the display of rows to
	 * ones containing the search string.
	 *
	 * @param searchString	the string to search for
	 * @param regexp		whether to perform regular expression matching
	 * 				or just plain string comparison
	 */
	public synchronized void search(String searchString, boolean regexp) {
		int			i;
		SearchParameters	params;

		m_RegExpSearch = regexp;
		m_SearchString = searchString;
		params         = new SearchParameters(m_SearchString, m_RegExpSearch);

		// no search -> display everything
		if (m_SearchString == null) {
			m_DisplayIndices = null;
		}
		// perform search
		else {
			m_DisplayIndices = new TIntArrayList();
			for (i = 0; i < getActualRowCount(); i++) {
				if (isSearchMatch(params, m_SortedIndices[i]))
					m_DisplayIndices.add(i);
			}
		}

		fireTableDataChanged();
	}

	/**
	 * Returns the current search string.
	 *
	 * @return		the search string, null if not filtered
	 */
	public String getSeachString() {
		return m_SearchString;
	}

	/**
	 * Returns whether the last search was a regular expression based one.
	 *
	 * @return		true if last search was a reg exp one
	 */
	public boolean isRegExpSearch() {
		return m_RegExpSearch;
	}
}
