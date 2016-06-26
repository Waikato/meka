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
 * DataSortedTableModel.java
 * Copyright (C) 2005-2012 University of Waikato, Hamilton, New Zealand
 *
 */

package meka.gui.dataviewer;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Undoable;
import weka.core.converters.AbstractFileLoader;
import weka.gui.SortedTableModel;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A sorter for the Data-Viewer - necessary because of the custom CellRenderer.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 12697 $ 
 */

public class DataSortedTableModel
	extends SortedTableModel
	implements Undoable {

	/** for serialization */
	static final long serialVersionUID = -5733148376354254030L;

	/**
	 * initializes the sorter w/o a model, but loads the given file and creates
	 * from that a model
	 *
	 * @param filename	the file to load
	 * @param loaders optional varargs loader to use
	 */
	public DataSortedTableModel(String filename, AbstractFileLoader... loaders) {
		this(new DataTableModel(filename, loaders));
	}

	/**
	 * initializes the sorter w/o a model, but uses the given data to create
	 * a model from that
	 *
	 * @param data 	the data to use
	 */
	public DataSortedTableModel(Instances data) {
		this(new DataTableModel(data));
	}

	/**
	 * initializes the sorter with the given model
	 *
	 * @param model	the model to use
	 */
	public DataSortedTableModel(TableModel model) {
		super(model);
	}

	/**
	 * returns whether the notification of changes is enabled
	 *
	 * @return 		true if notification of changes is enabled
	 */
	public boolean isNotificationEnabled() {
		return ((DataTableModel) getModel()).isNotificationEnabled();
	}

	/**
	 * sets whether the notification of changes is enabled
	 *
	 * @param enabled	enables/disables the notification
	 */
	public void setNotificationEnabled(boolean enabled) {
		((DataTableModel) getModel()).setNotificationEnabled(enabled);
	}

	/**
	 * returns whether undo support is enabled
	 *
	 * @return 		true if undo support is enabled
	 */
	public boolean isUndoEnabled() {
		return ((DataTableModel) getModel()).isUndoEnabled();
	}

	/**
	 * sets whether undo support is enabled
	 *
	 * @param enabled	whether to enable/disable undo support
	 */
	public void setUndoEnabled(boolean enabled) {
		((DataTableModel) getModel()).setUndoEnabled(enabled);
	}

	/**
	 * returns whether the model is read-only
	 *
	 * @return 		true if model is read-only
	 */
	public boolean isReadOnly() {
		return ((DataTableModel) getModel()).isReadOnly();
	}

	/**
	 * sets whether the model is read-only
	 *
	 * @param value	if true the model is set to read-only
	 */
	public void setReadOnly(boolean value) {
		((DataTableModel) getModel()).setReadOnly(value);
	}

	/**
	 * returns the double value of the underlying Instances object at the
	 * given position, -1 if out of bounds
	 *
	 * @param rowIndex		the row index
	 * @param columnIndex		the column index
	 * @return			the underlying value in the Instances object
	 */
	public double getInstancesValueAt(int rowIndex, int columnIndex) {
		return ((DataTableModel) getModel()).getInstancesValueAt(mIndices[rowIndex], columnIndex);
	}

	/**
	 * returns the value at the given position
	 *
	 * @param rowIndex		the row index
	 * @param columnIndex		the column index
	 * @return			the value of the model at the given  position
	 */
	public Object getModelValueAt(int rowIndex, int columnIndex) {
		Object            result;

		result = super.getModel().getValueAt(rowIndex, columnIndex);
		// since this is called in the super-class we have to use the original
		// index!
		if (((DataTableModel) getModel()).isMissingAt(rowIndex, columnIndex))
			result = null;

		return result;
	}

	/**
	 * returns the TYPE of the attribute at the given position
	 *
	 * @param columnIndex		the index of the column
	 * @return			the attribute type
	 */
	public int getType(int columnIndex) {
		return ((DataTableModel) getModel()).getType(mIndices.length > 0 ? mIndices[0] : -1, columnIndex);
	}

	/**
	 * returns the TYPE of the attribute at the given position
	 *
	 * @param rowIndex		the index of the row
	 * @param columnIndex		the index of the column
	 * @return			the attribute type
	 */
	public int getType(int rowIndex, int columnIndex) {
		return ((DataTableModel) getModel()).getType(mIndices[rowIndex], columnIndex);
	}

	/**
	 * deletes the attribute at the given col index
	 *
	 * @param columnIndex     the index of the attribute to delete
	 */
	public void deleteAttributeAt(int columnIndex) {
		((DataTableModel) getModel()).deleteAttributeAt(columnIndex);
	}

	/**
	 * deletes the attributes at the given indices
	 *
	 * @param columnIndices	the column indices
	 */
	public void deleteAttributes(int[] columnIndices) {
		((DataTableModel) getModel()).deleteAttributes(columnIndices);
	}

	/**
	 * renames the attribute at the given col index
	 *
	 * @param columnIndex		the index of the column
	 * @param newName		the new name of the attribute
	 */
	public void renameAttributeAt(int columnIndex, String newName) {
		((DataTableModel) getModel()).renameAttributeAt(columnIndex, newName);
	}

	/**
	 * sets the attribute at the given col index as the new class attribute
	 *
	 * @param columnIndex		the index of the column
	 */
	public void attributeAsClassAt(int columnIndex) {
		((DataTableModel) getModel()).attributeAsClassAt(columnIndex);
	}

	/**
	 * deletes the instance at the given index
	 *
	 * @param rowIndex		the index of the row
	 */
	public void deleteInstanceAt(int rowIndex) {
		((DataTableModel) getModel()).deleteInstanceAt(mIndices[rowIndex]);
	}

	/**
	 * Insert a new instance (all values 0) at the given index. If index is < 0,
	 * then inserts at the end of the dataset
	 *
	 * @param index the index to insert at
	 */
	public void insertInstance(int index) {
		((DataTableModel) getModel()).insertInstance(index);
	}

	/**
	 * deletes the instances at the given positions
	 *
	 * @param rowIndices		the indices to delete
	 */
	public void deleteInstances(int[] rowIndices) {
		int[]               realIndices;
		int                 i;

		realIndices = new int[rowIndices.length];
		for (i = 0; i < rowIndices.length; i++)
			realIndices[i] = mIndices[rowIndices[i]];

		((DataTableModel) getModel()).deleteInstances(realIndices);
	}

	/**
	 * sorts the instances via the given attribute
	 *
	 * @param columnIndex		the index of the column
	 */
	public void sortInstances(int columnIndex) {
		((DataTableModel) getModel()).sortInstances(columnIndex);
	}

	/**
	 * sorts the instances via the given attribute
	 *
	 * @param columnIndex         the index of the column
	 * @param ascending ascending if true, otherwise descending
	 */
	public void sortInstances(int columnIndex, boolean ascending) {
		((DataTableModel) getModel()).sortInstances(columnIndex, ascending);
	}

	/**
	 * sorts the table over the given column, either ascending or descending
	 *
	 * @param columnIndex the column to sort over
	 * @param ascending ascending if true, otherwise descending
	 */
	public void sort(int columnIndex, boolean ascending) {
		sortInstances(columnIndex, ascending);
	}

	/**
	 * returns the column of the given attribute name, -1 if not found
	 *
	 * @param name		the name of the attribute
	 * @return			the column index or -1 if not found
	 */
	public int getAttributeColumn(String name) {
		return ((DataTableModel) getModel()).getAttributeColumn(name);
	}

	/**
	 * checks whether the value at the given position is missing
	 *
	 * @param rowIndex		the row index
	 * @param columnIndex		the column index
	 * @return			true if the value at the position is missing
	 */
	public boolean isMissingAt(int rowIndex, int columnIndex) {
		return ((DataTableModel) getModel()).isMissingAt(mIndices[rowIndex], columnIndex);
	}

	/**
	 * sets the data
	 *
	 * @param data	the data to use
	 */
	public void setInstances(Instances data) {
		((DataTableModel) getModel()).setInstances(data);
	}

	/**
	 * returns the data
	 *
	 * @return		the current data
	 */
	public Instances getInstances() {
		return ((DataTableModel) getModel()).getInstances();
	}

	/**
	 * returns the attribute at the given index, can be NULL if not an attribute
	 * column
	 *
	 * @param columnIndex		the index of the column
	 * @return			the attribute at the position
	 */
	public Attribute getAttributeAt(int columnIndex) {
		return ((DataTableModel) getModel()).getAttributeAt(columnIndex);
	}

	/**
	 * adds a listener to the list that is notified each time a change to data
	 * model occurs
	 *
	 * @param l		the listener to add
	 */
	public void addTableModelListener(TableModelListener l) {
		if (getModel() != null)
			((DataTableModel) getModel()).addTableModelListener(l);
	}

	/**
	 * removes a listener from the list that is notified each time a change to
	 * the data model occurs
	 *
	 * @param l		the listener to remove
	 */
	public void removeTableModelListener(TableModelListener l) {
		if (getModel() != null)
			((DataTableModel) getModel()).removeTableModelListener(l);
	}

	/**
	 * notfies all listener of the change of the model
	 *
	 * @param e		the event to send to the listeners
	 */
	public void notifyListener(TableModelEvent e) {
		((DataTableModel) getModel()).notifyListener(e);
	}

	/**
	 * removes the undo history
	 */
	public void clearUndo() {
		((DataTableModel) getModel()).clearUndo();
	}

	/**
	 * returns whether an undo is possible, i.e. whether there are any undo points
	 * saved so far
	 *
	 * @return returns TRUE if there is an undo possible
	 */
	public boolean canUndo() {
		return ((DataTableModel) getModel()).canUndo();
	}

	/**
	 * undoes the last action
	 */
	public void undo() {
		((DataTableModel) getModel()).undo();
	}

	/**
	 * adds an undo point to the undo history
	 */
	public void addUndoPoint() {
		((DataTableModel) getModel()).addUndoPoint();
	}

	/**
	 * Sets whether to display the attribute index in the header.
	 *
	 * @param value	if true then the attribute indices are displayed in the
	 * 			table header
	 */
	public void setShowAttributeIndex(boolean value) {
		((DataTableModel) getModel()).setShowAttributeIndex(value);
	}

	/**
	 * Returns whether to display the attribute index in the header.
	 *
	 * @return		true if the attribute indices are displayed in the
	 * 			table header
	 */
	public boolean getShowAttributeIndex() {
		return ((DataTableModel) getModel()).getShowAttributeIndex();
	}
}
