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
 * Statistics.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.experimenter;

import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsUtils;
import meka.experiment.events.ExecutionStageEvent;
import meka.experiment.events.StatisticsNotificationEvent;
import meka.experiment.events.StatisticsNotificationListener;
import meka.gui.core.SearchPanel;
import meka.gui.core.SortableAndSearchableTable;
import meka.gui.events.SearchEvent;
import meka.gui.events.SearchListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab for evaluating an experiment.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Statistics
  extends AbstractExperimenterTab
  implements StatisticsNotificationListener {

	private static final long serialVersionUID = 3556506064253273853L;

	/** the table model. */
	public static class StatisticsTableModel
	  extends AbstractTableModel {

		private static final long serialVersionUID = 6418545317753165337L;

		/** the underlying statistics. */
		protected List<EvaluationStatistics>  m_Statistics;

		/** the headers. */
		protected List<String> m_Headers;

		/**
		 * Initializes the model with no statistics.
		 */
		public StatisticsTableModel() {
			this(new ArrayList<EvaluationStatistics>());
		}

		/**
		 * Initializes the model with the statistics.
		 *
		 * @param stats     the statistics to use
		 */
		public StatisticsTableModel(List<EvaluationStatistics> stats) {
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
	}

	/** the collected statistics. */
	protected List<EvaluationStatistics> m_Statistics;

	/** the table for the statistics. */
	protected SortableAndSearchableTable m_Table;

	/** the search panel. */
	protected SearchPanel m_SearchPanel;

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		m_Table = new SortableAndSearchableTable(new StatisticsTableModel());
		m_Table.setAutoResizeMode(SortableAndSearchableTable.AUTO_RESIZE_OFF);
		add(new BaseScrollPane(m_Table), BorderLayout.CENTER);

		m_SearchPanel = new SearchPanel(SearchPanel.LayoutType.HORIZONTAL, true);
		m_SearchPanel.addSearchListener(new SearchListener() {
			@Override
			public void searchInitiated(SearchEvent e) {
				m_Table.search(e.getParameters().getSearchString(), e.getParameters().isRegExp());
			}
		});
		add(m_SearchPanel, BorderLayout.SOUTH);
	}

	/**
	 * Returns the title of the tab.
	 *
	 * @return the title
	 */
	@Override
	public String getTitle() {
		return "Statistics";
	}

	/**
	 * Gets called when the experiment enters a new stage.
	 *
	 * @param e         the event
	 */
	public void experimentStage(ExecutionStageEvent e) {
		if (e.getStage() == ExecutionStageEvent.Stage.INITIALIZE) {
			m_Statistics = new ArrayList<>();
			updateView();
		}
	}

	/**
	 * Gets called if new statistics have become available.
	 *
	 * @param e         the event
	 */
	public void statisticsAvailable(StatisticsNotificationEvent e) {
		m_Statistics.addAll(e.getStatistics());
		updateView();
	}

	/**
	 * Gets called when the experiment changed.
	 */
	@Override
	protected void update() {
		super.update();
		m_Statistics = new ArrayList<>();
		if (m_Experiment != null)
			m_Statistics.addAll(m_Experiment.getStatistics());
		updateView();
	}

	/**
	 * Updates the view on the current statistics.
	 */
	protected void updateView() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_Table.setModel(new StatisticsTableModel(m_Statistics));
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_Table.setOptimalColumnWidth();
			}
		});
	}
}
