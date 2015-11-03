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
 * MeasurementEvaluationStatisticsTableModel.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsUtils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for displaying a Dataset X Classifier table for a specific measurement.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MeasurementEvaluationStatisticsTableModel
		extends AbstractTableModel {

	private static final long serialVersionUID = 6418545317753165337L;

	/** the underlying statistics. */
	protected List<EvaluationStatistics> m_Statistics;

	/** the measurement. */
	protected String m_Measurement;

	/** the datasets. */
	protected List<String> m_Datasets;

	/** the classifiers. */
	protected List<String> m_Classifiers;

	/** whether to show the classifier or an index in the column header. */
	protected boolean m_ShowIndex;

	/**
	 * Initializes the model with no statistics.
	 */
	public MeasurementEvaluationStatisticsTableModel() {
		this(new ArrayList<EvaluationStatistics>(), null, true);
	}

	/**
	 * Initializes the model with the statistics.
	 *
	 * @param stats         the statistics to use
	 * @param measurement   the measurement to display
	 * @param showIndex     whether to show an index or the classifier in the header
	 */
	public MeasurementEvaluationStatisticsTableModel(List<EvaluationStatistics> stats, String measurement, boolean showIndex) {
		m_Statistics  = stats;
		m_Measurement = measurement;
		m_ShowIndex   = showIndex;
		m_Classifiers = EvaluationStatisticsUtils.commandLines(stats, true);
		m_Datasets    = EvaluationStatisticsUtils.relations(stats, true);
	}

	/**
	 * Returns the name of the column.
	 *
	 * @param column    the column index
	 * @return          the name
	 */
	@Override
	public String getColumnName(int column) {
		if (column == 0)
			return "Dataset";
		else if (m_ShowIndex)
			return "[" + column + "]";
		else
			return m_Classifiers.get(column - 1);
	}

	/**
	 * Returns the number of datasets in this model.
	 *
	 * @return          the number of datasets
	 */
	@Override
	public int getRowCount() {
		return m_Datasets.size();
	}

	/**
	 * The number of columns in this model.
	 *
	 * @return          the number of columns
	 */
	@Override
	public int getColumnCount() {
		return m_Classifiers.size() + 1;
	}

	/**
	 * Returns the type of the column.
	 *
	 * @param columnIndex   the column index
	 * @return              the type
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0)
			return String.class;
		else
			return Double.class;
	}

	/**
	 * Returns the cell value at the specified location.
	 *
	 * @param rowIndex          the row
	 * @param columnIndex       the column
	 * @return                  the value, null if not available
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		List<Number>    stats;

		if (columnIndex == 0) {
			return m_Datasets.get(rowIndex);
		}
		else {
			if (m_Measurement == null)
				return null;
			stats = EvaluationStatisticsUtils.measurements(
					m_Statistics,
					m_Classifiers.get(columnIndex - 1),
					m_Datasets.get(rowIndex),
					m_Measurement);
			if (stats.size() > 1)
				System.err.println("Found multiple values for measurement '" + m_Measurement + "': " + stats.size());
			if (stats.size() > 0)
				return stats.get(0);
			else
				return null;
		}
	}

	/**
	 * Sets the measurement the model is for.
	 *
	 * @param value             the measurement
	 */
	public void setMeasurement(String value) {
		m_Measurement = value;
		fireTableDataChanged();
	}

	/**
	 * Returns the measurement the model is for.
	 *
	 * @return                  the measurement
	 */
	public String getMeasurement() {
		return m_Measurement;
	}

	/**
	 * Sets whether to show an index or the classifier in the header.
	 *
	 * @param value             true if to show index
	 */
	public void setShowIndex(boolean value) {
		m_ShowIndex = value;
		fireTableDataChanged();
	}

	/**
	 * Returns whethert o show an index or the classifier in the header.
	 *
	 * @return                  true if to show index
	 */
	public boolean getShowIndex() {
		return m_ShowIndex;
	}

	/**
	 * Returns the underlying statistics.
	 *
	 * @return                  the statistics
	 */
	public List<EvaluationStatistics> getStatistics() {
		return m_Statistics;
	}
}
