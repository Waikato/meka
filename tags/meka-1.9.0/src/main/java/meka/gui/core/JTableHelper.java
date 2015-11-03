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
 * JTableHelper.java
 * Copyright (C) 2005 University of Waikato, Hamilton, New Zealand
 * Copyright http://fopps.sourceforge.net/
 */

package meka.gui.core;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * A helper class for JTable, e.g. calculating the optimal colwidth.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4584 $
 * @see weka.gui.JTableHelper
 */
public class JTableHelper {

	/** the maximum number of rows to use for calculation. */
	public final static int MAX_ROWS = 100;

	/** the table to work with. */
	protected JTable m_Table;

	/**
	 * initializes the object.
	 *
	 * @param table	the table to work on
	 */
	public JTableHelper(JTable table) {
		this.m_Table = table;
	}

	/**
	 * returns the JTable.
	 *
	 * @return		the table to work on
	 */
	public JTable getJTable() {
		return m_Table;
	}

	/**
	 * calcs the optimal column width of the given column.
	 *
	 * @param col		the column index
	 * @return		the optimal width
	 */
	public int calcColumnWidth(int col) {
		return calcColumnWidth(getJTable(), col);
	}

	/**
	 * Calculates the optimal width for the column of the given table. The
	 * calculation is based on the preferred width of the header and cell
	 * renderer.
	 * <br>
	 * Taken from the newsgroup de.comp.lang.java with some modifications.<br>
	 * Taken from FOPPS/EnhancedTable - http://fopps.sourceforge.net/<br>
	 *
	 * @param table    the table to calculate the column width
	 * @param col      the column to calculate the widths
	 * @return         the width, -1 if error
	 */
	public static int calcColumnWidth(JTable table, int col) {
		int 	result;
		TableModel data;
		int 	rowCount;
		int		row;
		int		dec;
		Component c;

		result = calcHeaderWidth(table, col);
		if (result == -1)
			return result;

		data     = table.getModel();
		rowCount = data.getRowCount();
		dec      = (int) Math.ceil((double) rowCount / (double) MAX_ROWS);
		try {
			for (row = rowCount - 1; row >= 0; row -= dec) {
				c = table.prepareRenderer(
						table.getCellRenderer(row, col),
						row, col);
				result = Math.max(result, c.getPreferredSize().width + 10);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * calcs the optimal header width of the given column.
	 *
	 * @param col		the column index
	 * @return		the optimal width
	 */
	public int calcHeaderWidth(int col) {
		return calcHeaderWidth(getJTable(), col);
	}

	/**
	 * Calculates the optimal width for the header of the given table. The
	 * calculation is based on the preferred width of the header renderer.
	 *
	 * @param table    the table to calculate the column width
	 * @param col      the column to calculate the widths
	 * @return         the width, -1 if error
	 */
	public static int calcHeaderWidth(JTable table, int col) {
		if (table == null)
			return -1;

		if (col < 0 || col > table.getColumnCount()) {
			System.out.println("invalid col " + col);
			return -1;
		}

		JTableHeader header = table.getTableHeader();
		TableCellRenderer defaultHeaderRenderer = null;
		if (header != null) defaultHeaderRenderer = header.getDefaultRenderer();
		TableColumnModel columns = table.getColumnModel();
		TableColumn column = columns.getColumn(col);
		int width = -1;
		TableCellRenderer h = column.getHeaderRenderer();
		if (h == null) h = defaultHeaderRenderer;
		if (h != null) {
			// Not explicitly impossible
			Component c = h.getTableCellRendererComponent(
					table,
					column.getHeaderValue(),
					false, false, -1, col);
			width = c.getPreferredSize().width + 5;
		}

		return width;
	}

	/**
	 * sets the optimal column width for the given column.
	 *
	 * @param col		the column index
	 */
	public void setOptimalColumnWidth(int col) {
		setOptimalColumnWidth(getJTable(), col);
	}

	/**
	 * sets the optimal column width for the given column.
	 *
	 * @param table	the table to work on
	 * @param col		the column index
	 */
	public static void setOptimalColumnWidth(final JTable table, final int col) {
		final int	  width;

		if ( (col >= 0) && (col < table.getColumnModel().getColumnCount()) ) {
			width = calcColumnWidth(table, col);

			if (width >= 0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JTableHeader header = table.getTableHeader();
						TableColumn column = table.getColumnModel().getColumn(col);
						column.setPreferredWidth(width);
						table.doLayout();
						header.repaint();
					}
				});
			}
		}
	}

	/**
	 * sets the optimal column width for all columns.
	 */
	public void setOptimalColumnWidth() {
		setOptimalColumnWidth(getJTable());
	}

	/**
	 * sets the optimal column width for alls column if the given table.
	 *
	 * @param table	the table to work on
	 */
	public static void setOptimalColumnWidth(JTable table) {
		int		i;

		for (i = 0; i < table.getColumnModel().getColumnCount(); i++)
			setOptimalColumnWidth(table, i);
	}

	/**
	 * sets the optimal header width for the given column.
	 *
	 * @param col		the column index
	 */
	public void setOptimalHeaderWidth(int col) {
		setOptimalHeaderWidth(getJTable(), col);
	}

	/**
	 * sets the optimal header width for the given column.
	 *
	 * @param table	the table to work on
	 * @param col		the column index
	 */
	public static void setOptimalHeaderWidth(final JTable table, final int col) {
		final int   width;

		if ( (col >= 0) && (col < table.getColumnModel().getColumnCount()) ) {
			width = calcHeaderWidth(table, col);

			if (width >= 0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JTableHeader header = table.getTableHeader();
						TableColumn column = table.getColumnModel().getColumn(col);
						column.setPreferredWidth(width);
						table.doLayout();
						header.repaint();
					}
				});
			}
		}
	}

	/**
	 * sets the optimal header width for all columns.
	 */
	public void setOptimalHeaderWidth() {
		setOptimalHeaderWidth(getJTable());
	}

	/**
	 * sets the optimal header width for alls column if the given table.
	 *
	 * @param table	the table to work with
	 */
	public static void setOptimalHeaderWidth(JTable table) {
		int		i;

		for (i = 0; i < table.getColumnModel().getColumnCount(); i++)
			setOptimalHeaderWidth(table, i);
	}

	/**
	 * Assumes table is contained in a JScrollPane.
	 * Scrolls the cell (rowIndex, vColIndex) so that it is visible
	 * within the viewport.
	 *
	 * @param row		the row index
	 * @param col		the column index
	 */
	public void scrollToVisible(int row, int col) {
		scrollToVisible(getJTable(), row, col);
	}

	/**
	 * Assumes table is contained in a JScrollPane.
	 * Scrolls the cell (rowIndex, vColIndex) so that it is visible
	 * within the viewport.
	 *
	 * @param table	the table to work with
	 * @param row		the row index
	 * @param col		the column index
	 */
	public static void scrollToVisible(JTable table, int row, int col) {
		if (!(table.getParent() instanceof JViewport))
			return;

		JViewport viewport = (JViewport) table.getParent();

		// This rectangle is relative to the table where the
		// northwest corner of cell (0,0) is always (0,0).
		Rectangle rect = table.getCellRect(row, col, true);

		// The location of the viewport relative to the table
		Point pt = viewport.getViewPosition();

		// Translate the cell location so that it is relative
		// to the view, assuming the northwest corner of the
		// view is (0,0)
		rect.setLocation(rect.x - pt.x, rect.y - pt.y);

		// Scroll the area into view
		viewport.scrollRectToVisible(rect);
	}
}
