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
import meka.experiment.events.ExecutionStageEvent;
import meka.experiment.events.StatisticsNotificationEvent;
import meka.experiment.events.StatisticsNotificationListener;
import meka.experiment.statisticsexporters.FileBasedEvaluationStatisticsExporter;
import meka.experiment.statisticsexporters.InMemory;
import meka.experiment.statisticsexporters.SimpleAggregate;
import meka.gui.choosers.EvaluationStatisticsExporterFileChooser;
import meka.gui.core.EvaluationStatisticsTableModel;
import meka.gui.core.GUIHelper;
import meka.gui.core.SearchPanel;
import meka.gui.core.SortableAndSearchableTable;
import meka.gui.events.SearchEvent;
import meka.gui.events.SearchListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

	/** the collected statistics. */
	protected List<EvaluationStatistics> m_Statistics;

	/** the tabbed pane for the statistics. */
	protected JTabbedPane m_TabbedPane;

	/** the table for the raw statistics. */
	protected SortableAndSearchableTable m_TableRaw;

	/** the model for the raw statistics. */
	protected EvaluationStatisticsTableModel m_ModelRaw;

	/** the table for the aggregated statistics. */
	protected SortableAndSearchableTable m_TableAggregated;

	/** the model for the aggregated statistics. */
	protected EvaluationStatisticsTableModel m_ModelAggregated;

	/** the search panel. */
	protected SearchPanel m_SearchPanel;

	/** the button for saving the statistics. */
	protected JButton m_ButtonSave;

	/** the filechooser for saving the statistics. */
	protected EvaluationStatisticsExporterFileChooser m_FileChooser;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();

		m_FileChooser = new EvaluationStatisticsExporterFileChooser();
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		JPanel      panel;
		JPanel      panelSave;

		super.initGUI();

		m_TabbedPane = new JTabbedPane();
		add(m_TabbedPane, BorderLayout.CENTER);

		m_ModelRaw = new EvaluationStatisticsTableModel();
		m_TableRaw = new SortableAndSearchableTable(m_ModelRaw);
		m_TableRaw.setAutoResizeMode(SortableAndSearchableTable.AUTO_RESIZE_OFF);
		m_TabbedPane.addTab("Raw", new BaseScrollPane(m_TableRaw));

		m_ModelAggregated = new EvaluationStatisticsTableModel();
		m_TableAggregated = new SortableAndSearchableTable(m_ModelAggregated);
		m_TableAggregated.setAutoResizeMode(SortableAndSearchableTable.AUTO_RESIZE_OFF);
		m_TabbedPane.addTab("Aggregated", new BaseScrollPane(m_TableAggregated));

		panel = new JPanel(new BorderLayout());
		add(panel, BorderLayout.SOUTH);

		m_SearchPanel = new SearchPanel(SearchPanel.LayoutType.HORIZONTAL, true);
		m_SearchPanel.addSearchListener(new SearchListener() {
			@Override
			public void searchInitiated(SearchEvent e) {
				m_TableRaw.search(e.getParameters().getSearchString(), e.getParameters().isRegExp());
			}
		});
		panel.add(m_SearchPanel, BorderLayout.WEST);

		panelSave = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(panelSave, BorderLayout.EAST);

		m_ButtonSave = new JButton("Save...", GUIHelper.getIcon("save.gif"));
		m_ButtonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		panelSave.add(m_ButtonSave);
	}

	/**
	 * Returns the current statistics.
	 *
	 * @return          the statistics, null if not available
	 */
	protected List<EvaluationStatistics> getCurrentStatistics() {
		if (m_TabbedPane.getSelectedIndex() == 0)
			return m_ModelRaw.getStatistics();
		else if (m_TabbedPane.getSelectedIndex() == 1)
			return m_ModelAggregated.getStatistics();
		else
			return null;
	}

	/**
	 * Prompts the user with a filechooser for saving the statistics to a file.
	 */
	protected void save() {
		List<EvaluationStatistics>              stats;
		int                                     retVal;
		FileBasedEvaluationStatisticsExporter   exporter;
		String                                  msg;

		stats = getCurrentStatistics();
		if (stats == null)
			return;

		retVal = m_FileChooser.showSaveDialog(this);
		if (retVal != EvaluationStatisticsExporterFileChooser.APPROVE_OPTION)
			return;

		exporter = m_FileChooser.getWriter();
		exporter.setFile(m_FileChooser.getSelectedFile());
		exporter.addLogListener(getOwner());
		msg = exporter.export(stats);
		exporter.removeLogListener(getOwner());
		if (msg != null) {
			JOptionPane.showMessageDialog(
					this,
					"Export failed:\n" + msg,
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
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
		InMemory    inmem;

		// raw
		m_ModelRaw = new EvaluationStatisticsTableModel(new ArrayList<>(m_Statistics));

		// aggregated
		inmem = new InMemory();
		SimpleAggregate agg = new SimpleAggregate();
		agg.setSuffixMean("");
		agg.setSuffixCount(" [count]");
		agg.setSuffixStdDev(" [stdev]");
		agg.setExporter(inmem);
		agg.export(m_Statistics);
		m_ModelAggregated = new EvaluationStatisticsTableModel(new ArrayList<>(inmem.getStatistics()));

		// update GUI
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_TableRaw.setModel(m_ModelRaw);
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_TableRaw.setOptimalColumnWidth();
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_TableAggregated.setModel(m_ModelAggregated);
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_TableAggregated.setOptimalColumnWidth();
			}
		});
	}
}
