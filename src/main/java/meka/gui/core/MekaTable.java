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
 * MekaTable.java
 * Copyright (C) 2009-2015 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * A specialized JTable that allows double-clicking on header for resizing to
 * optimal width.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11517 $
 */
public class MekaTable
		extends JTable {

	/** for serialization. */
	private static final long serialVersionUID = -2360462659067336490L;

	/** for setting the optimal column width. */
	protected JTableHelper m_TableHelper;

	/**
	 * Constructs a default <code>BaseTable</code> that is initialized with a default
	 * data model, a default column model, and a default selection
	 * model.
	 */
	public MekaTable() {
		super();
		initGUI();
	}

	/**
	 * Constructs a <code>BaseTable</code> with <code>numRows</code>
	 * and <code>numColumns</code> of empty cells using
	 * <code>DefaultTableModel</code>.  The columns will have
	 * names of the form "A", "B", "C", etc.
	 *
	 * @param numRows           the number of rows the table holds
	 * @param numColumns        the number of columns the table holds
	 */
	public MekaTable(int numRows, int numColumns) {
		super(numRows, numColumns);
		initGUI();
	}

	/**
	 * Constructs a <code>BaseTable</code> to display the values in the two dimensional array,
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
	public MekaTable(final Object[][] rowData, final Object[] columnNames) {
		super(rowData, columnNames);
		initGUI();
	}

	/**
	 * Constructs a <code>BaseTable</code> to display the values in the
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
	public MekaTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
		initGUI();
	}

	/**
	 * Constructs a <code>BaseTable</code> that is initialized with
	 * <code>dm</code> as the data model, a default column model,
	 * and a default selection model.
	 *
	 * @param dm        the data model for the table
	 */
	public MekaTable(TableModel dm) {
		super(dm);
		initGUI();
	}

	/**
	 * Constructs a <code>BaseTable</code> that is initialized with
	 * <code>dm</code> as the data model, <code>cm</code>
	 * as the column model, and a default selection model.
	 *
	 * @param dm        the data model for the table
	 * @param cm        the column model for the table
	 */
	public MekaTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
		initGUI();
	}

	/**
	 * Constructs a <code>BaseTable</code> that is initialized with
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
	public MekaTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
		initGUI();
	}

	/**
	 * Returns the table helper instance. Instantiates it if necessary.
	 *
	 * @return		the table helper
	 */
	protected JTableHelper getTableHelper() {
		if (m_TableHelper == null)
			m_TableHelper = new JTableHelper(this);

		return m_TableHelper;
	}

	/**
	 * Initializes some GUI-related things.
	 */
	protected void initGUI() {
		getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// just the selecte column
				if (MouseUtils.isDoubleClick(e) && e.isControlDown() && !e.isAltDown() && !e.isShiftDown()) {
					final int col = columnAtPoint(e.getPoint());
					if ((col != -1) && isVisible()) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								getTableHelper().setOptimalColumnWidth(col);
							}
						});
					}
				}
				// all columns
				else if (MouseUtils.isDoubleClick(e) && e.isControlDown() && !e.isAltDown() && e.isShiftDown()) {
					if (isVisible()) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								getTableHelper().setOptimalColumnWidth();
							}
						});
					}
				}
				else {
					super.mouseClicked(e);
				}
			}
		});
	}

	/**
	 * Sets the optimal column width for all columns. AutoResize must be set
	 * to BaseTable.AUTO_RESIZE_OFF.
	 */
	public void setOptimalColumnWidth() {
		if (isVisible()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getTableHelper().setOptimalColumnWidth();
				}
			});
		}
	}

	/**
	 * Sets the optimal column width for the specified column. AutoResize must be set
	 * to BaseTable.AUTO_RESIZE_OFF.
	 *
	 * @param column	the column to resize
	 */
	public void setOptimalColumnWidth(final int column) {
		if (isVisible()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getTableHelper().setOptimalColumnWidth(column);
				}
			});
		}
	}

	/**
	 * Scrolls the row into view.
	 *
	 * @param row		the row to scroll into view
	 */
	public void scrollRowToVisible(int row) {
		scrollRectToVisible(getCellRect(row, 0, true));
	}

	/**
	 * Scrolls the column into view.
	 *
	 * @param col		the column to scroll into view
	 */
	public void scrollColumnToVisible(int col) {
		scrollRectToVisible(getCellRect(0, col, true));
	}

	/**
	 * Copies either the selected rows to the clipboard.
	 */
	public void copyToClipboard() {
		Action copy;
		ActionEvent event;

		copy  = getActionMap().get("copy");
		event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
		copy.actionPerformed(event);
	}

	/**
	 * Displays the specified cell.
	 *
	 * @param row		the row of the cell
	 * @param column	the column of the cell
	 */
	public void showCell(int row, int column) {
		scrollRectToVisible(getCellRect(row, column, true));
	}
}
