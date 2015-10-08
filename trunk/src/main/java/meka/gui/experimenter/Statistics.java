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
import meka.experiment.evaluationstatistics.FileBasedEvaluationStatisticsHandler;
import meka.experiment.events.ExecutionStageEvent;
import meka.experiment.events.StatisticsNotificationEvent;
import meka.experiment.events.StatisticsNotificationListener;
import meka.experiment.statisticsexporters.FileBasedEvaluationStatisticsExporter;
import meka.experiment.statisticsexporters.FileBasedMeasurementEvaluationStatisticsExporter;
import meka.experiment.statisticsexporters.InMemory;
import meka.experiment.statisticsexporters.SimpleAggregate;
import meka.gui.choosers.EvaluationStatisticsExporterFileChooser;
import meka.gui.choosers.EvaluationStatisticsFileChooser;
import meka.gui.choosers.MeasurementEvaluationStatisticsExporterFileChooser;
import meka.gui.core.*;
import meka.gui.events.SearchEvent;
import meka.gui.events.SearchListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
	public static final int TAB_RAW = 0;
	public static final int TAB_AGGREGATED = 1;
	public static final int TAB_MEASUREMENT = 2;

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

	/** the combobox with the measurements. */
	protected JComboBox<String> m_ComboBoxMeasurements;

	/** the model for the measurement statistics. */
	protected MeasurementEvaluationStatisticsTableModel m_ModelMeasurement;

	/** the table for the measurement statistics. */
	protected SortableAndSearchableTable m_TableMeasurement;

	/** the model for the keys of the measurement statistics. */
	protected DefaultTableModel m_ModelMeasurementKey;

	/** the table for the keys of the measurement statistics. */
	protected SortableAndSearchableTable m_TableMeasurementKey;

	/** the search panel. */
	protected SearchPanel m_SearchPanel;

	/** the filechooser for saving the statistics. */
	protected EvaluationStatisticsExporterFileChooser m_FileChooser;

	/** the filechooser for saving the measurement statistics. */
	protected MeasurementEvaluationStatisticsExporterFileChooser m_FileChooserMeasurement;

	/** the filechooser for loading the statistics. */
	protected EvaluationStatisticsFileChooser m_FileChooserStatistics;

	/** whether to ignore changes in the UI. */
	protected boolean m_IgnoreChanges;

	/** the menu item for loading stats. */
	protected JMenuItem m_MenuItemOpen;

	/** the menu item for saving raw stats. */
	protected JMenuItem m_MenuItemSaveAsRaw;

	/** the menu item for saving aggregated stats. */
	protected JMenuItem m_MenuItemSaveAsAggregated;

	/** the menu item for saving measurement stats. */
	protected JMenuItem m_MenuItemSaveAsMeasurement;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();

		m_FileChooser            = new EvaluationStatisticsExporterFileChooser();
		m_FileChooserMeasurement = new MeasurementEvaluationStatisticsExporterFileChooser();
		m_FileChooserStatistics  = new EvaluationStatisticsFileChooser();
		m_IgnoreChanges          = false;
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		JPanel          panel;
		JPanel          panelMeasurement;
		JLabel          label;
		BaseScrollPane  scrollPane;

		super.initGUI();

		m_TabbedPane = new JTabbedPane();
		add(m_TabbedPane, BorderLayout.CENTER);

		// raw
		m_ModelRaw = new EvaluationStatisticsTableModel();
		m_TableRaw = new SortableAndSearchableTable(m_ModelRaw);
		m_TableRaw.setAutoResizeMode(SortableAndSearchableTable.AUTO_RESIZE_OFF);
		m_TabbedPane.addTab("Raw", new BaseScrollPane(m_TableRaw));

		// aggregated
		m_ModelAggregated = new EvaluationStatisticsTableModel();
		m_TableAggregated = new SortableAndSearchableTable(m_ModelAggregated);
		m_TableAggregated.setAutoResizeMode(SortableAndSearchableTable.AUTO_RESIZE_OFF);
		m_TabbedPane.addTab("Aggregated", new BaseScrollPane(m_TableAggregated));

		// measurement
		m_ComboBoxMeasurements = new JComboBox<>();
		m_ComboBoxMeasurements.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (m_IgnoreChanges)
					return;
				if (m_ComboBoxMeasurements.getSelectedIndex() == -1)
					return;
				m_ModelMeasurement.setMeasurement((String) m_ComboBoxMeasurements.getSelectedItem());
			}
		});
		label = new JLabel("Measurement");
		label.setDisplayedMnemonic('M');
		label.setLabelFor(m_ComboBoxMeasurements);
		panelMeasurement = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelMeasurement.add(label);
		panelMeasurement.add(m_ComboBoxMeasurements);
		m_ModelMeasurement = new MeasurementEvaluationStatisticsTableModel();
		m_TableMeasurement = new SortableAndSearchableTable(m_ModelMeasurement);
		m_TableMeasurement.setAutoResizeMode(SortableAndSearchableTable.AUTO_RESIZE_OFF);
		m_ModelMeasurementKey = new DefaultTableModel();
		m_TableMeasurementKey = new SortableAndSearchableTable(m_ModelMeasurementKey);
		m_TableMeasurementKey.setAutoResizeMode(SortableAndSearchableTable.AUTO_RESIZE_OFF);
		panel = new JPanel(new BorderLayout());
		panel.add(panelMeasurement, BorderLayout.NORTH);
		panel.add(new BaseScrollPane(m_TableMeasurement), BorderLayout.CENTER);
		scrollPane = new BaseScrollPane(m_TableMeasurementKey);
		scrollPane.setPreferredSize(new Dimension(200, 150));
		panel.add(scrollPane, BorderLayout.SOUTH);
		m_TabbedPane.addTab("Measurement", panel);

		// bottom
		panel = new JPanel(new BorderLayout());
		add(panel, BorderLayout.SOUTH);

		// search
		m_SearchPanel = new SearchPanel(SearchPanel.LayoutType.HORIZONTAL, true);
		m_SearchPanel.addSearchListener(new SearchListener() {
			@Override
			public void searchInitiated(SearchEvent e) {
				m_TableRaw.search(e.getParameters().getSearchString(), e.getParameters().isRegExp());
			}
		});
		panel.add(m_SearchPanel, BorderLayout.WEST);
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
	 * Returns an optional menu to be added to the Experimenter menu.
	 *
	 * @return the menu
	 */
	@Override
	public JMenu getMenu() {
		JMenu       result;
		JMenuItem   menuitem;

		result = new JMenu(getTitle());
		result.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateMenu();
			}
		});

		menuitem = new JMenuItem("Open...", GUIHelper.getIcon("open.gif"));
		menuitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				load();
			}
		});
		result.add(menuitem);
		m_MenuItemOpen = menuitem;

		menuitem = new JMenuItem("Save as (raw)...", GUIHelper.getIcon("save.gif"));
		menuitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_TabbedPane.setSelectedIndex(TAB_RAW);
				save();
			}
		});
		result.add(menuitem);
		m_MenuItemSaveAsRaw = menuitem;

		menuitem = new JMenuItem("Save as (aggregated)...", GUIHelper.getIcon("save.gif"));
		menuitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_TabbedPane.setSelectedIndex(TAB_AGGREGATED);
				save();
			}
		});
		result.add(menuitem);
		m_MenuItemSaveAsAggregated = menuitem;

		menuitem = new JMenuItem("Save as (measurement)...", GUIHelper.getIcon("save.gif"));
		menuitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_TabbedPane.setSelectedIndex(TAB_MEASUREMENT);
				save();
			}
		});
		result.add(menuitem);
		m_MenuItemSaveAsMeasurement = menuitem;

		return result;
	}

	/**
	 * Prompts the user with a filechooser for saving the statistics to a file.
	 */
	protected void save() {
		int                         tab;
		List<EvaluationStatistics>  stats;
		int                         retVal;
		String                      msg;

		tab = m_TabbedPane.getSelectedIndex();

		// stats
		if (tab == TAB_RAW)
			stats = m_ModelRaw.getStatistics();
		else if (tab == TAB_AGGREGATED)
			stats = m_ModelAggregated.getStatistics();
		else if (tab == TAB_MEASUREMENT)
			stats = m_ModelMeasurement.getStatistics();
		else
			return;

		// select output file
		if (tab == TAB_MEASUREMENT) {
			m_FileChooserMeasurement.setDialogTitle("Save measurement");
			retVal = m_FileChooserMeasurement.showSaveDialog(this);
			if (retVal != MeasurementEvaluationStatisticsExporterFileChooser.APPROVE_OPTION)
				return;
		}
		else {
			if (tab == TAB_RAW)
				m_FileChooser.setDialogTitle("Save raw");
			else
				m_FileChooser.setDialogTitle("Save aggregated");
			retVal = m_FileChooser.showSaveDialog(this);
			if (retVal != EvaluationStatisticsExporterFileChooser.APPROVE_OPTION)
				return;
		}

		// export
		if (tab == TAB_MEASUREMENT) {
			FileBasedMeasurementEvaluationStatisticsExporter mexporter = m_FileChooserMeasurement.getWriter();
			mexporter.setFile(m_FileChooserMeasurement.getSelectedFile());
			mexporter.setMeasurement((String) m_ComboBoxMeasurements.getSelectedItem());
			mexporter.addLogListener(getOwner());
			msg = mexporter.export(stats);
			mexporter.removeLogListener(getOwner());
		}
		else {
			FileBasedEvaluationStatisticsExporter exporter = m_FileChooser.getWriter();
			exporter.setFile(m_FileChooser.getSelectedFile());
			exporter.addLogListener(getOwner());
			msg = exporter.export(stats);
			exporter.removeLogListener(getOwner());
		}
		if (msg != null) {
			JOptionPane.showMessageDialog(
					this,
					"Export failed:\n" + msg,
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Loads statistics from disk.
	 */
	protected void load() {
		int                                         retVal;
		final FileBasedEvaluationStatisticsHandler  handler;
		SwingWorker                                 worker;

		retVal = m_FileChooserStatistics.showOpenDialog(this);
		if (retVal != EvaluationStatisticsFileChooser.APPROVE_OPTION)
			return;

		handler = m_FileChooserStatistics.getReader();
		handler.addLogListener(getOwner());
		handler.setFile(m_FileChooserStatistics.getSelectedFile());
		worker = new SwingWorker() {
			@Override
			protected Object doInBackground() throws Exception {
				startBusy("Loading statistics: " + handler.getFile());
				List<EvaluationStatistics> stats = handler.read();
				handler.removeLogListener(getOwner());
				if (stats == null) {
					log("Failed to read statistics from: " + m_FileChooserStatistics.getSelectedFile());
					return null;
				}
				m_Statistics = stats;
				updateView();
				return null;
			}

			@Override
			protected void done() {
				super.done();
				finishBusy("");
			}
		};
		worker.execute();
	}

	/**
	 * Gets called when the experiment enters a new stage.
	 *
	 * @param e         the event
	 */
	public void experimentStage(ExecutionStageEvent e) {
		if (e.getStage() == ExecutionStageEvent.Stage.INITIALIZING) {
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
		InMemory            inmem;
		final List<String>  measurements;
		String              selMeasurement;
		final String        oldMeasurement;
		int                 i;
		List<String>        classifiers;
		Vector<String>      colNames;

		m_IgnoreChanges = true;

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

		// measurement
		measurements = EvaluationStatisticsUtils.measurements(inmem.getStatistics());
		if (m_ComboBoxMeasurements.getSelectedIndex() == -1)
			selMeasurement = (measurements.size() > 0) ? measurements.get(0) : null;
		else
			selMeasurement = (String) m_ComboBoxMeasurements.getSelectedItem();
		if (measurements.contains(selMeasurement))
			oldMeasurement = selMeasurement;
		else if (measurements.size() > 0)
			oldMeasurement = measurements.get(0);
		else
			oldMeasurement = null;
		m_ModelMeasurement = new MeasurementEvaluationStatisticsTableModel(inmem.getStatistics(), oldMeasurement, true);

		// measurement key
		classifiers = EvaluationStatisticsUtils.commandLines(inmem.getStatistics(), false);
		colNames = new Vector<>();
		colNames.add("Index");
		colNames.add("Classifier");
		m_ModelMeasurementKey = new DefaultTableModel(colNames, classifiers.size());
		for (i = 0; i < classifiers.size(); i++) {
			m_ModelMeasurementKey.setValueAt("[" + (i + 1) + "]", i, 0);
			m_ModelMeasurementKey.setValueAt(classifiers.get(i), i, 1);
		}

		// update GUI
		// 1. raw
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
		// 2. aggregated
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
		// 3. measurement
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_ComboBoxMeasurements.setModel(
						new DefaultComboBoxModel<>(measurements.toArray(new String[measurements.size()])));
				if (oldMeasurement != null)
					m_ComboBoxMeasurements.setSelectedItem(oldMeasurement);
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_TableMeasurement.setModel(m_ModelMeasurement);
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_TableMeasurement.setOptimalColumnWidth(0);
			}
		});
		for (i = 1; i < m_ModelMeasurement.getColumnCount(); i++) {
			final int col = i;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					TableColumn column = m_TableMeasurement.getColumnModel().getColumn(col);
					if (column != null)
						column.setPreferredWidth(50);  // TODO parameter?
				}
			});
		}
		// 4. measurement key
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_TableMeasurementKey.setModel(m_ModelMeasurementKey);
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_TableMeasurementKey.setOptimalColumnWidth();
			}
		});
		// finish
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_IgnoreChanges = false;
			}
		});
	}

	/**
	 * Updates the menu items in the custom menu.
	 */
	protected void updateMenu() {
		boolean     present;

		present = (m_Statistics != null) && (m_Statistics.size() > 0);

		m_MenuItemOpen.setEnabled(true);
		m_MenuItemSaveAsRaw.setEnabled(present);
		m_MenuItemSaveAsAggregated.setEnabled(present);
		m_MenuItemSaveAsMeasurement.setEnabled(present);
	}
}
