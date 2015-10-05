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
import meka.gui.choosers.EvaluationStatisticsExporterFileChooser;
import meka.gui.core.GUIHelper;
import meka.gui.core.SearchPanel;
import meka.gui.core.SortableAndSearchableTable;
import meka.gui.core.EvaluationStatisticsTableModel;
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

	/** the table for the statistics. */
	protected SortableAndSearchableTable m_Table;

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

		m_Table = new SortableAndSearchableTable(new EvaluationStatisticsTableModel());
		m_Table.setAutoResizeMode(SortableAndSearchableTable.AUTO_RESIZE_OFF);
		add(new BaseScrollPane(m_Table), BorderLayout.CENTER);

		panel = new JPanel(new BorderLayout());
		add(panel, BorderLayout.SOUTH);

		m_SearchPanel = new SearchPanel(SearchPanel.LayoutType.HORIZONTAL, true);
		m_SearchPanel.addSearchListener(new SearchListener() {
			@Override
			public void searchInitiated(SearchEvent e) {
				m_Table.search(e.getParameters().getSearchString(), e.getParameters().isRegExp());
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
	 * Prompts the user with a filechooser for saving the statistics to a file.
	 */
	protected void save() {
		int                                     retVal;
		FileBasedEvaluationStatisticsExporter   exporter;
		String                                  msg;

		retVal = m_FileChooser.showSaveDialog(this);
		if (retVal != EvaluationStatisticsExporterFileChooser.APPROVE_OPTION)
			return;

		exporter = m_FileChooser.getWriter();
		exporter.setFile(m_FileChooser.getSelectedFile());
		exporter.addLogListener(getOwner());
		msg = exporter.export(m_Statistics);
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_Table.setModel(new EvaluationStatisticsTableModel(new ArrayList<>(m_Statistics)));
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
