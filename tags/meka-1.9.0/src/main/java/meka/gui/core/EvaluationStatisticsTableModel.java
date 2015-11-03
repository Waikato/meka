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
 * EvaluationStatisticsTableModel.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsUtils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * The table model for evaluation statistics.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class EvaluationStatisticsTableModel
		extends AbstractTableModel {

	private static final long serialVersionUID = 6418545317753165337L;

	/** the underlying statistics. */
	protected List<EvaluationStatistics> m_Statistics;

	/** the headers. */
	protected List<String> m_Headers;

	/**
	 * Initializes the model with no statistics.
	 */
	public EvaluationStatisticsTableModel() {
		this(new ArrayList<EvaluationStatistics>());
	}

	/**
	 * Initializes the model with the statistics.
	 *
	 * @param stats     the statistics to use
	 */
	public EvaluationStatisticsTableModel(List<EvaluationStatistics> stats) {
		m_Statistics = stats;
		m_Headers    = EvaluationStatisticsUtils.headers(stats, true, false);
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
			return "Classifier";
		else if (column == 1)
			return "Relation";
		else
			return m_Headers.get(column - 2);
	}

	/**
	 * Returns the number of statistics in this model.
	 *
	 * @return          the number of statistics
	 */
	@Override
	public int getRowCount() {
		return m_Statistics.size();
	}

	/**
	 * The number of columns in this model.
	 *
	 * @return          the number of columns
	 */
	@Override
	public int getColumnCount() {
		return m_Headers.size() + 2;
	}

	/**
	 * Returns the type of the column.
	 *
	 * @param columnIndex   the column index
	 * @return              the type
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex < 2)
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
		if (columnIndex == 0)
			return m_Statistics.get(rowIndex).getCommandLine();
		else if (columnIndex == 1)
			return m_Statistics.get(rowIndex).getRelation();
		else
			return m_Statistics.get(rowIndex).get(m_Headers.get(columnIndex - 2));
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
