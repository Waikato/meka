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
 * SortableAndSearchableTable.java
 * Copyright (C) 2010-2014 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A specialized JTable that allows double-clicking on header for resizing to
 * optimal width, as well as being searchable and sortable.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 8807 $
 */
public class SortableAndSearchableTable
		extends MekaTable
		implements SortableTable, SearchableTable {

	/** for serialization. */
	private static final long serialVersionUID = -3176811618121454828L;

	/** the key for the sort column setting. */
	public static final String KEY_SORTCOL = "sort col";

	/** the key for the sort oder. */
	public static final String KEY_SORTORDER = "sort order";

	/** the key for the search string. */
	public static final String KEY_SEARCHSTRING = "search string";

	/** the key for the regular expression search flag. */
	public static final String KEY_SEARCHREGEXP = "search reg exp";

	/** the sortable/searchable model. */
	protected SortableAndSearchableWrapperTableModel m_Model;

	/** whether to automatically set optimal column widths. */
	protected boolean m_UseOptimalColumnWidths;

	/** whether to automatically sort table models that get set via setModel. */
	protected boolean m_SortNewTableModel;

	/**
	 * Constructs a default <code>SortedBaseTable</code> that is initialized with a default
	 * data model, a default column model, and a default selection
	 * model.
	 */
	public SortableAndSearchableTable() {
		super();
	}

	/**
	 * Constructs a <code>SortedBaseTable</code> with <code>numRows</code>
	 * and <code>numColumns</code> of empty cells using
	 * <code>DefaultTableModel</code>.  The columns will have
	 * names of the form "A", "B", "C", etc.
	 *
	 * @param numRows           the number of rows the table holds
	 * @param numColumns        the number of columns the table holds
	 */
	public SortableAndSearchableTable(int numRows, int numColumns) {
		super(numRows, numColumns);
	}

	/**
	 * Constructs a <code>SortedBaseTable</code> to display the values in the two dimensional array,
	 * <code>rowData</code>, with column names, <code>columnNames</code>.
	 * <code>rowData</code> is an array of rows, so the value of the cell at row 1,
	 * column 5 can be obtained with the following code:
	 * <p>
	 * <pre> rowData[1][5]; </pre>
	 * <p>
	 * All rows must be of the same length as <code>columnNames</code>.
	 * <p>
	 * @param rowData           the data for the new table
	 * @param columnNames       names of each column
	 */
	public SortableAndSearchableTable(final Object[][] rowData, final Object[] columnNames) {
		super(rowData, columnNames);
	}

	/**
	 * Constructs a <code>SortedBaseTable</code> to display the values in the
	 * <code>Vector</code> of <code>Vectors</code>, <code>rowData</code>,
	 * with column names, <code>columnNames</code>.  The
	 * <code>Vectors</code> contained in <code>rowData</code>
	 * should contain the values for that row. In other words,
	 * the value of the cell at row 1, column 5 can be obtained
	 * with the following code:
	 * <p>
	 * <pre>((Vector)rowData.elementAt(1)).elementAt(5);</pre>
	 * <p>
	 * @param rowData           the data for the new table
	 * @param columnNames       names of each column
	 */
	public SortableAndSearchableTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
	}

	/**
	 * Constructs a <code>SortedBaseTable</code> that is initialized with
	 * <code>dm</code> as the data model, a default column model,
	 * and a default selection model.
	 *
	 * @param dm        the data model for the table
	 */
	public SortableAndSearchableTable(TableModel dm) {
		super(dm);
	}

	/**
	 * Constructs a <code>SortedBaseTable</code> that is initialized with
	 * <code>dm</code> as the data model, <code>cm</code>
	 * as the column model, and a default selection model.
	 *
	 * @param dm        the data model for the table
	 * @param cm        the column model for the table
	 */
	public SortableAndSearchableTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
	}

	/**
	 * Constructs a <code>SortedBaseTable</code> that is initialized with
	 * <code>dm</code> as the data model, <code>cm</code> as the
	 * column model, and <code>sm</code> as the selection model.
	 * If any of the parameters are <code>null</code> this method
	 * will initialize the table with the corresponding default model.
	 * The <code>autoCreateColumnsFromModel</code> flag is set to false
	 * if <code>cm</code> is non-null, otherwise it is set to true
	 * and the column model is populated with suitable
	 * <code>TableColumns</code> for the columns in <code>dm</code>.
	 *
	 * @param dm        the data model for the table
	 * @param cm        the column model for the table
	 * @param sm        the row selection model for the table
	 */
	public SortableAndSearchableTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
	}

	/**
	 * Returns the initial setting of whether to set optimal column widths.
	 * Default implementation returns "false", since large tables might take too
	 * long to be displayed otherwise.
	 *
	 * @return		true if optimal column widths are used by default
	 */
	protected boolean initialUseOptimalColumnWidths() {
		return false;
	}

	/**
	 * Sets whether to automatically set optimal column widths.
	 *
	 * @param value	if true then optimal column widths are used
	 */
	public void setUseOptimalColumnWidhts(boolean value) {
		m_UseOptimalColumnWidths = value;
		if (m_UseOptimalColumnWidths) {
			setAutoResizeMode(AUTO_RESIZE_OFF);
			setOptimalColumnWidth();
		}
	}

	/**
	 * Returns whether to automatically set optimal column widths.
	 * Default implementation is initialized with "false".
	 *
	 * @return		true if optimal column widths are to be used
	 */
	public boolean getUseOptimalColumnWidths() {
		return m_UseOptimalColumnWidths;
	}

	/**
	 * Returns the initial setting of whether to sort new models.
	 * Default implementation returns "false".
	 *
	 * @return		true if new models need to be sorted
	 */
	protected boolean initialSortNewTableModel() {
		return false;
	}

	/**
	 * Sets whether to sort new models.
	 *
	 * @param value	if true then new models get sorted
	 */
	public void setSortNewTableModel(boolean value) {
		m_SortNewTableModel = value;
		if (m_SortNewTableModel)
			sort(0);
	}

	/**
	 * Returns whether to sort new models.
	 * Default implementation is initialized with "false".
	 *
	 * @return		true if new models get sorted
	 */
	public boolean getSortNewTableModel() {
		return m_SortNewTableModel;
	}

	/**
	 * Initializes some GUI-related things.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		m_SortNewTableModel = initialSortNewTableModel();
		m_Model.addMouseListenerToHeader(this);
		if (getSortNewTableModel())
			sort(0);

		m_UseOptimalColumnWidths = initialUseOptimalColumnWidths();
		if (getUseOptimalColumnWidths()) {
			setAutoResizeMode(AUTO_RESIZE_OFF);
			setOptimalColumnWidth();
		}
	}

	/**
	 * Returns the class of the table model that the models need to be derived
	 * from. The default implementation just returns TableModel.class
	 *
	 * @return		the class the models must be derived from
	 */
	protected Class getTableModelClass() {
		return TableModel.class;
	}

	/**
	 * Backs up the settings from the old model.
	 *
	 * @param model	the old model (the model stored within the SortedModel)
	 * @return		the backed up settings
	 */
	protected Hashtable<String,Object> backupModelSettings(TableModel model) {
		Hashtable<String,Object> result;

		result = new Hashtable<String,Object>();

		result.put(KEY_SORTCOL, m_Model.getSortColumn());
		result.put(KEY_SORTORDER, m_Model.isAscending());

		if (model instanceof SearchableTableModel) {
			if (((SearchableTableModel) model).getSeachString() != null)
				result.put(KEY_SEARCHSTRING, ((SearchableTableModel) model).getSeachString());
			result.put(KEY_SEARCHREGEXP, ((SearchableTableModel) model).isRegExpSearch());
		}

		return result;
	}

	/**
	 * Restores the settings previously backed up.
	 *
	 * @param model	the new model (the model stored within the SortedModel)
	 * @param settings	the old settings, null if no settings were available
	 */
	protected void restoreModelSettings(TableModel model, Hashtable<String,Object> settings) {
		int		sortCol;
		boolean	asc;
		String search;
		boolean	regexp;

		// default values
		sortCol = 0;
		asc     = true;
		search  = null;
		regexp  = false;

		// get stored values
		if (settings != null) {
			sortCol = (Integer) settings.get(KEY_SORTCOL);
			asc     = (Boolean) settings.get(KEY_SORTORDER);

			if (model instanceof SearchableTableModel) {
				search = (String) settings.get(KEY_SEARCHSTRING);
				regexp = (Boolean) settings.get(KEY_SEARCHREGEXP);
			}
		}

		// restore sorting
		if (getSortNewTableModel())
			m_Model.sort(sortCol, asc);

		// restore search
		if (model instanceof SearchableTableModel)
			((SearchableTableModel) model).search(search, regexp);

		// set optimal column widths
		if (getUseOptimalColumnWidths())
			setOptimalColumnWidth();
	}

	/**
	 * Sets the model to display - only {@link #getTableModelClass()}.
	 *
	 * @param model	the model to display
	 */
	@Override
	public synchronized void setModel(TableModel model) {
		Hashtable<String,Object> settings;

		if (!(getTableModelClass().isInstance(model)))
			model = createDefaultDataModel();

		// backup current setup
		if (m_Model != null) {
			settings = backupModelSettings(m_Model);
			getTableHeader().removeMouseListener(m_Model.getHeaderMouseListener());
		}
		else {
			settings = null;
		}

		m_Model = new SortableAndSearchableWrapperTableModel(model);
		super.setModel(m_Model);
		m_Model.addMouseListenerToHeader(this);

		// restore setup
		restoreModelSettings(m_Model, settings);
	}

	/**
	 * Sets the base model to use. Discards any sorting.
	 *
	 * @param value       the base model
	 */
	public synchronized void setUnsortedModel(TableModel value) {
		m_Model.setUnsortedModel(value);
	}

	/**
	 * Sets the base model to use.
	 *
	 * @param value       	the base model
	 * @param restoreSorting	whether to restore the sorting
	 */
	public synchronized void setUnsortedModel(TableModel value, boolean restoreSorting) {
		m_Model.setUnsortedModel(value, restoreSorting);
	}

	/**
	 * returns the underlying model, can be null.
	 *
	 * @return            the current model
	 */
	public synchronized TableModel getUnsortedModel() {
		if (m_Model != null)
			return m_Model.getUnsortedModel();
		else
			return null;
	}

	/**
	 * Returns the actual underlying row the given visible one represents. Useful
	 * for retrieving "non-visual" data that is also stored in a TableModel.
	 *
	 * @param visibleRow	the displayed row to retrieve the original row for
	 * @return		the original row
	 */
	public synchronized int getActualRow(int visibleRow) {
		return m_Model.getActualRow(visibleRow);
	}

	/**
	 * Returns the "visible" row derived from row in the actual table model.
	 *
	 * @param internalRow	the row in the actual model
	 * @return		the row in the sorted model, -1 in case of an error
	 */
	public synchronized int getDisplayRow(int internalRow) {
		return m_Model.getDisplayRow(internalRow);
	}

	/**
	 * returns whether the table was sorted.
	 *
	 * @return        true if the table was sorted
	 */
	public synchronized boolean isSorted() {
		return m_Model.isSorted();
	}

	/**
	 * Returns the sort column.
	 *
	 * @return		the sort column
	 */
	public synchronized int getSortColumn() {
		return m_Model.getSortColumn();
	}

	/**
	 * Returns whether sorting is ascending or not.
	 *
	 * @return		true if ascending
	 * @see		#isSorted()
	 * @see		#getSortColumn()
	 */
	public synchronized boolean isAscending() {
		return m_Model.isAscending();
	}

	/**
	 * sorts the table over the given column (ascending).
	 *
	 * @param columnIndex     the column to sort over
	 */
	public synchronized void sort(int columnIndex) {
		if (m_Model != null)
			m_Model.sort(columnIndex);
	}

	/**
	 * sorts the table over the given column, either ascending or descending.
	 *
	 * @param columnIndex     the column to sort over
	 * @param ascending       ascending if true, otherwise descending
	 */
	public synchronized void sort(int columnIndex, boolean ascending) {
		if (m_Model != null)
			m_Model.sort(columnIndex, ascending);
	}

	/**
	 * Returns the actual row count in the model.
	 *
	 * @return		the row count in the underlying data
	 */
	public synchronized int getActualRowCount() {
		return m_Model.getActualRowCount();
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
		int[]	selected;
		int		i;
		int		index;

		// determine actual selected rows
		selected = getSelectedRows();
		for (i = 0; i < selected.length; i++)
			selected[i] = getActualRow(selected[i]);

		m_Model.search(searchString, regexp);

		// re-select rows that are still in current search
		clearSelection();
		for (i = 0; i < selected.length; i++) {
			index = getDisplayRow(selected[i]);
			if (index != -1)
				getSelectionModel().addSelectionInterval(index, index);
		}
	}

	/**
	 * Returns the current search string.
	 *
	 * @return		the search string, null if not filtered
	 */
	public synchronized String getSeachString() {
		return m_Model.getSeachString();
	}

	/**
	 * Returns whether the last search was a regular expression based one.
	 *
	 * @return		true if last search was a reg exp one
	 */
	public synchronized boolean isRegExpSearch() {
		return m_Model.isRegExpSearch();
	}
}
