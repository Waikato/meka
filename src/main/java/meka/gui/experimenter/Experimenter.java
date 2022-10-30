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
 * Experimenter.java
 * Copyright (C) 2015-2018 University of Waikato, Hamilton, NZ
 */

package meka.gui.experimenter;

import meka.core.ExceptionUtils;
import meka.core.OptionUtils;
import meka.events.LogEvent;
import meka.events.LogListener;
import meka.experiment.DefaultExperiment;
import meka.experiment.Experiment;
import meka.experiment.events.ExecutionStageEvent;
import meka.experiment.events.ExecutionStageListener;
import meka.experiment.events.IterationNotificationEvent;
import meka.experiment.events.IterationNotificationListener;
import meka.experiment.events.StatisticsNotificationEvent;
import meka.experiment.events.StatisticsNotificationListener;
import meka.experiment.filehandlers.ExperimentFileHandler;
import meka.gui.choosers.ExperimentFileChooser;
import meka.gui.core.CommandLineArgsHandler;
import meka.gui.core.GUIHelper;
import meka.gui.core.GUILauncher;
import meka.gui.core.MekaPanel;
import meka.gui.core.MenuBarProvider;
import meka.gui.core.RecentFilesHandlerWithCommandline;
import meka.gui.core.StatusBar;
import meka.gui.events.RecentItemEvent;
import meka.gui.events.RecentItemListener;
import meka.gui.experimenter.menu.AbstractExperimenterMenuItem;
import weka.core.PluginManager;
import weka.gui.ConverterFileChooser;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Experimenter interface.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Experimenter
		extends MekaPanel
		implements MenuBarProvider, CommandLineArgsHandler, IterationNotificationListener, LogListener,
		StatisticsNotificationListener, ExecutionStageListener {

	private static final long serialVersionUID = -773818300205896615L;

	/** the file to store the recent files in. */
	public final static String SESSION_FILE = "ExperimenterSession.props";

	/** the tabbed pane for the various panels. */
	protected JTabbedPane m_TabbedPane;

	/** the tabs. */
	protected ArrayList<AbstractExperimenterTab> m_Tabs;

	/** the menu bar. */
	protected JMenuBar m_MenuBar;

	/** the "new" menu. */
	protected JMenu m_MenuItemFileNew;

	/** the "open" menu item. */
	protected JMenuItem m_MenuItemFileOpen;

	/** the "save" menu item. */
	protected JMenuItem m_MenuItemFileSave;

	/** the "load recent" submenu. */
	protected JMenu m_MenuFileOpenRecent;

	/** the "save as" menu item. */
	protected JMenuItem m_MenuItemFileSaveAs;

	/** the "close" menu item. */
	protected JMenuItem m_MenuItemFileClose;

	/** the "start" menu item. */
	protected JMenuItem m_MenuItemExecutionStart;

	/** the "stop" menu item. */
	protected JMenuItem m_MenuItemExecutionStop;

	/** the recent files handler. */
	protected RecentFilesHandlerWithCommandline<JMenu> m_RecentFilesHandler;

	/** the statusbar to use. */
	protected StatusBar m_StatusBar;

	/** the current file. */
	protected File m_CurrentFile;

	/** the log tab. */
	protected LogTab m_LogTab;

	/** the file chooser for the experiments. */
	protected ExperimentFileChooser m_FileChooser;

	/** the current experiment. */
	protected Experiment m_Experiment;

	/** additional menu items. */
	protected HashMap<AbstractExperimenterMenuItem, AbstractAction> m_AdditionalMenuItems;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		m_MenuBar             = null;
		m_Tabs                = new ArrayList<>();
		m_FileChooser         = new ExperimentFileChooser();
		m_Experiment          = null;
		m_AdditionalMenuItems = new HashMap<>();
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		java.util.List<String>  classnames;

		super.initGUI();

		m_TabbedPane = new JTabbedPane();
		add(m_TabbedPane, BorderLayout.CENTER);

		// tabs
		m_Tabs.add(new BasicSetup());
		m_Tabs.add(new ExpertSetup());
		classnames = AbstractExperimenterTab.getTabs();
		for (String classname: classnames) {
			try {
				AbstractExperimenterTab tab = (AbstractExperimenterTab) Class.forName(classname).getDeclaredConstructor().newInstance();
				if (tab.getClass() == BasicSetup.class)
					continue;
				if (tab.getClass() == ExpertSetup.class)
					continue;
				if (tab instanceof LogTab)
					continue;
				m_Tabs.add(tab);
			}
			catch (Exception e) {
				System.err.println("Failed to instantiate Experimenter tab: " + classname);
				e.printStackTrace();
			}
		}
		m_LogTab = new LogTab();
		m_Tabs.add(m_LogTab);
		for (AbstractExperimenterTab tab: m_Tabs) {
			tab.setOwner(this);
			m_TabbedPane.addTab(tab.getTitle(), tab);
		}

		// status bar
		m_StatusBar = new StatusBar();
		add(m_StatusBar, BorderLayout.SOUTH);
	}

	/**
	 * Finishes the initialization.
	 */
	@Override
	protected void finishInit() {
		m_TabbedPane.setSelectedIndex(0);
		for (AbstractExperimenterTab tab: m_Tabs)
			tab.update();
		updateMenu();
	}

	/**
	 * Notifies all the tabs that the experiment has changed.
	 *
	 * @param source not null if a tab triggered this call
	 * @param exp the new experiment to use
	 */
	public void notifyTabsExperimentChanged(AbstractExperimenterTab source, Experiment exp) {
		m_Experiment = exp;
		for (AbstractExperimenterTab tab: m_Tabs) {
			if ((source != null) && (tab == source))
				continue;
			tab.setExperiment(exp);
		}
	}

	/**
	 * Returns the menu bar to use.
	 *
	 * @return the menu bar
	 */
	public JMenuBar getMenuBar() {
		JMenuBar	                    result;
		JMenu		                    menu;
		JMenu		                    submenu;
		JMenuItem	                    menuitem;
		List<String>                    clsnames;
		HashMap<String,JMenu>           menus;
		AbstractExperimenterMenuItem    additional;
		AbstractAction                  action;

		if (m_MenuBar == null) {
			result = new JMenuBar();
			menus  = new HashMap<>();

			// File
			menu = new JMenu("File");
			menu.setMnemonic('F');
			menu.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					updateMenu();
				}
			});
			result.add(menu);
			menus.put(menu.getText(), menu);

			// File/New
			submenu = new JMenu("New");
			submenu.setIcon(GUIHelper.getIcon("new.gif"));
			clsnames = PluginManager.getPluginNamesOfTypeList(Experiment.class.getName());
			for (String clsname: clsnames) {
				try {
					final Class cls = Class.forName(clsname);
					menuitem = new JMenuItem(cls.getSimpleName());
					if (cls == DefaultExperiment.class)
						menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed N"));
					menuitem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							newSetup(cls);
						}
					});
					submenu.add(menuitem);
				}
				catch (Exception e) {
					System.err.println("Failed to load experiment class: " + clsname);
					e.printStackTrace();
				}
			}
			menu.add(submenu);
			m_MenuItemFileNew = submenu;

			// File/Open
			menuitem = new JMenuItem("Open...", GUIHelper.getIcon("open.gif"));
			menuitem.setMnemonic('O');
			menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed O"));
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					open();
				}
			});
			menu.add(menuitem);
			m_MenuItemFileOpen = menuitem;

			// File/Recent files
			submenu = new JMenu("Open recent");
			menu.add(submenu);
			m_RecentFilesHandler = new RecentFilesHandlerWithCommandline<JMenu>(SESSION_FILE, 5, submenu);
			m_RecentFilesHandler.addRecentItemListener(new RecentItemListener<JMenu, RecentFilesHandlerWithCommandline.Setup>() {
				@Override
				public void recentItemAdded(RecentItemEvent<JMenu, RecentFilesHandlerWithCommandline.Setup> e) {
					// ignored
				}

				@Override
				public void recentItemSelected(RecentItemEvent<JMenu, RecentFilesHandlerWithCommandline.Setup> e) {
					open(e.getItem().getFile(), (ExperimentFileHandler) e.getItem().getHandler());
					updateMenu();
				}
			});
			m_MenuFileOpenRecent = submenu;

			// File/Save
			menuitem = new JMenuItem("Save", GUIHelper.getIcon("save.gif"));
			menuitem.setMnemonic('S');
			menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed S"));
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					save();
				}
			});
			menu.add(menuitem);
			m_MenuItemFileSave = menuitem;

			// File/Save as
			menuitem = new JMenuItem("Save as...", GUIHelper.getEmptyIcon());
			menuitem.setMnemonic('a');
			menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift pressed S"));
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveAs();
				}
			});
			menu.add(menuitem);
			m_MenuItemFileSaveAs = menuitem;

			// File/Close
			menuitem = new JMenuItem("Close", GUIHelper.getIcon("exit.png"));
			menuitem.setMnemonic('C');
			menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed Q"));
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					close();
				}
			});
			menu.addSeparator();
			menu.add(menuitem);
			m_MenuItemFileClose = menuitem;

			// Execution
			menu = new JMenu("Execution");
			menu.setMnemonic('E');
			menu.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					updateMenu();
				}
			});
			result.add(menu);
			menus.put(menu.getText(), menu);

			// Execution/Start
			menuitem = new JMenuItem("Start", GUIHelper.getIcon("start.gif"));
			menuitem.setMnemonic('S');
			menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed X"));
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					start();
				}
			});
			menu.add(menuitem);
			m_MenuItemExecutionStart = menuitem;

			// Execution/Stop
			menuitem = new JMenuItem("Stop", GUIHelper.getIcon("stop.gif"));
			menuitem.setMnemonic('o');
			menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift pressed X"));
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
				}
			});
			menu.add(menuitem);
			m_MenuItemExecutionStop = menuitem;

			// additional tabs?
			for (AbstractExperimenterTab tab: m_Tabs) {
				menu = tab.getMenu();
				if (menu != null) {
					result.add(menu);
					menus.put(menu.getText(), menu);
				}
			}

			// additional menu items?
			m_AdditionalMenuItems.clear();
			clsnames = AbstractExperimenterMenuItem.getMenuItems();
			for (String clsname: clsnames) {
				try {
					additional = (AbstractExperimenterMenuItem) Class.forName(clsname).getDeclaredConstructor().newInstance();
					additional.setOwner(this);
					action     = additional.getAction();
					m_AdditionalMenuItems.put(additional, action);
					if (!menus.containsKey(additional.getMenu())) {
						menu = new JMenu(additional.getMenu());
						menu.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent e) {
								updateMenu();
							}
						});
						result.add(menu);
						menus.put(menu.getText(), menu);
					}
					menus.get(additional.getMenu()).add(new JMenuItem(action));
				}
				catch (Exception e) {
					System.err.println("Failed to instantiate additional menu item: " + clsname);
					e.printStackTrace();
				}
			}

			m_MenuBar = result;
		}

		result = m_MenuBar;

		return result;
	}

	/**
	 * Updates the enabled/disabled state of the menu items.
	 */
	protected void updateMenu() {
		boolean     present;
		boolean     initializing;
		boolean     running;
		boolean     stopping;
		boolean     active;

		if (m_MenuBar == null)
			return;

		present      = (m_Experiment != null);
		initializing = present && m_Experiment.isInitializing();
		running      = present && m_Experiment.isRunning();
		stopping     = present && m_Experiment.isStopping();
		active       = initializing || running || stopping;

		// File
		m_MenuItemFileNew.setEnabled(!active);
		m_MenuItemFileOpen.setEnabled(!active);
		m_MenuItemFileSave.setEnabled(present && (getCurrentFile() != null));
		m_MenuItemFileSaveAs.setEnabled(present);
		m_MenuItemFileClose.setEnabled(!active);

		// Execution
		m_MenuItemExecutionStart.setEnabled(present && !active);
		m_MenuItemExecutionStop.setEnabled(present && running);

		// additional menu items
		for (AbstractExperimenterMenuItem item : m_AdditionalMenuItems.keySet())
			item.update(this, m_AdditionalMenuItems.get(item));
	}

	/**
	 * Returns the status bar.
	 *
	 * @return		the status bar
	 */
	public StatusBar getStatusBar() {
		return m_StatusBar;
	}

	/**
	 * Returns the current experiment, null if not available.
	 *
	 * @return      the experiment, null if none available
	 */
	public Experiment getCurrentExperiment() {
		return m_Experiment;
	}

	/**
	 * Returns the filename of the currently loaded data.
	 *
	 * @return      the filename, null if none available
	 */
	public File getCurrentFile() {
		return m_CurrentFile;
	}

	/**
	 * Instantiates an experiment from the provided class.
	 *
	 * @param cls       the experiment class to use
	 */
	public void newSetup(Class cls) {
		try {
			Experiment exp = (Experiment) cls.getDeclaredConstructor().newInstance();
			notifyTabsExperimentChanged(null, exp);
		}
		catch (Exception e) {
			handleException(null, "Failed to instantiate experiment class: " + cls.getName(), e);
		}
	}

	/**
	 * Opens an experiment.
	 */
	public void open() {
		int retVal;

		retVal = m_FileChooser.showOpenDialog(this);
		if (retVal != ConverterFileChooser.APPROVE_OPTION)
			return;

		open(m_FileChooser.getSelectedFile(), m_FileChooser.getReader());
	}

	/**
	 * Opens the specified experiment file.
	 *
	 * @param file the file to open
	 * @param handler the handler to use
	 */
	public void open(File file, ExperimentFileHandler handler) {
		m_StatusBar.startBusy("Loading: " + file);
		log(null, "Loading: " + file);
		try {
			m_Experiment = handler.read(file);
			if (m_Experiment != null)
				log(null, "Loaded successfully experiment: " + file);
			else
				log(null, "Failed to load experiment: " + file);
			notifyTabsExperimentChanged(null, m_Experiment);
			if (m_RecentFilesHandler != null)
				m_RecentFilesHandler.addRecentItem(new RecentFilesHandlerWithCommandline.Setup(file, handler));
			m_CurrentFile = file;
		}
		catch (Exception e) {
			handleException(null, "Failed to load experiment from '" + file + "':", e);
			JOptionPane.showMessageDialog(
					this,
					"Failed to load experiment from '" + file + "':\n" + e,
					"Error loading",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		m_StatusBar.finishBusy("");

		updateMenu();
	}

	/**
	 * Saves the experiment to the specified file.
	 *
	 * @param file the file to save the experiment to
	 * @param handler the saver to use, determines it automatically if null
	 */
	public void save(File file, ExperimentFileHandler handler) {
		if (handler == null)
			handler = m_FileChooser.getWriterForFile(file);
		try {
			log(null, "Saving experiment: " + file);
			String msg = handler.write(m_Experiment, file);
			if (msg == null) {
				m_CurrentFile = file;
				log(null, "Saved experiment successfully: " + file);
				if (m_RecentFilesHandler != null)
					m_RecentFilesHandler.addRecentItem(new RecentFilesHandlerWithCommandline.Setup(file, handler));
			}
			else {
				log(null, "Failed to save experiment to '" + file + "': " + msg);
			}
		}
		catch (Exception e) {
			handleException(null, "Failed to save experiment to '" + file + "':", e);
			JOptionPane.showMessageDialog(
					this,
					"Failed to save experiment to '" + file + "':\n" + e,
					"Error saving",
					JOptionPane.ERROR_MESSAGE);
		}

		updateMenu();
	}

	/**
	 * Saves the current experiment.
	 */
	public void save() {
		if (m_CurrentFile == null) {
			saveAs();
			return;
		}

		save(m_CurrentFile, null);
	}

	/**
	 * Saves the current experiment under a new name.
	 */
	public void saveAs() {
		int retVal;

		m_FileChooser.setSelectedFile(m_CurrentFile);
		retVal = m_FileChooser.showSaveDialog(this);
		if (retVal != ConverterFileChooser.APPROVE_OPTION)
			return;

		save(m_FileChooser.getSelectedFile(), m_FileChooser.getWriter());
	}

	/**
	 * Closes the experimenter.
	 */
	public void close() {
		closeParent();
	}

	/**
	 * Starts the experiment.
	 */
	public void start() {
		SwingWorker     worker;

		if ((m_Experiment == null) || m_Experiment.isRunning())
			return;

		worker = new SwingWorker() {
			String m_Result;
			@Override
			protected Object doInBackground() throws Exception {
				m_Result = null;

				m_Experiment.addIterationNotificationListener(Experimenter.this);
				m_Experiment.addStatisticsNotificationListener(Experimenter.this);
				m_Experiment.addLogListener(Experimenter.this);
				m_Experiment.addExecutionStageListener(Experimenter.this);

				String msg = m_Experiment.initialize();
				if (msg != null) {
					log(null, msg);
					m_Result = "Initialization failed:\n" + msg;
					return m_Result;
				}

				msg = m_Experiment.run();
				if (msg != null) {
					log(null, msg);
					m_Result = "Failed to run experiment:\n" + msg;
				}

				msg = m_Experiment.finish();
				if (msg != null) {
					log(null, msg);
					if (m_Result == null)
						m_Result = "";
					else
						m_Result += "\n";
					m_Result += "Failed to finish up experiment:\n" + msg;
				}
				return m_Result;
			}

			@Override
			protected void done() {
				super.done();

				m_Experiment.removeIterationNotificationListener(Experimenter.this);
				m_Experiment.removeStatisticsNotificationListener(Experimenter.this);
				m_Experiment.removeLogListener(Experimenter.this);
				m_Experiment.removeExecutionStageListener(Experimenter.this);

				if (m_Result != null) {
					JOptionPane.showMessageDialog(
							Experimenter.this,
							"Experiment execution failed:\n" + m_Result,
							"Experiment execution failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.execute();
	}

	/**
	 * Stops the current experiment.
	 */
	public void stop() {
		if ((m_Experiment == null) || !m_Experiment.isRunning())
			return;
		m_Experiment.stop();
	}

	/**
	 * Gets called if there is a new iteration (classifier/dataset combination) occurring in the experiment.
	 *
	 * @param e         the event
	 */
	public void nextIteration(IterationNotificationEvent e) {
		log(null, e.getExperiment().getClass().getName() + ": " + e.getDataset().relationName() + " / " + OptionUtils.toCommandLine(e.getClassifier()));
	}

	/**
	 * Gets called if there is a new log message.
	 *
	 * @param e         the event
	 */
	public void logMessage(LogEvent e) {
		log(null, e.getSource().getClass().getName() + ": " + e.getMessage());
		System.err.println(e.getSource().getClass().getName() + ": " + e.getMessage());
	}

	/**
	 * Gets called if new statistics have become available.
	 *
	 * @param e         the event
	 */
	public void statisticsAvailable(StatisticsNotificationEvent e) {
		for (AbstractExperimenterTab tab: m_Tabs) {
			if (tab instanceof StatisticsNotificationListener)
				((StatisticsNotificationListener) tab).statisticsAvailable(e);
		}
	}

	/**
	 * Gets called when the experiment enters a new stage.
	 *
	 * @param e         the event
	 */
	public void experimentStage(ExecutionStageEvent e) {
		log(null, e.getExperiment().getClass().getName() + ": " + e.getStage());
		for (AbstractExperimenterTab tab: m_Tabs)
			tab.experimentStage(e);

		switch (e.getStage()) {
			case INITIALIZING:
				m_StatusBar.startBusy("Initializing...");
				break;
			case RUNNING:
				m_StatusBar.startBusy("Running...");
				break;
			case STOPPING:
				m_StatusBar.startBusy("Stopping...");
				break;
			case FINISH:
				m_StatusBar.finishBusy("");
				break;
		}
	}

	/**
	 * For logging messages.
	 *
	 * @param tab       the origin of the message
	 * @param msg       the message to output
	 */
	protected synchronized void log(AbstractExperimenterTab tab, String msg) {
		m_LogTab.log(tab, msg);
	}

	/**
	 * Logs the stacktrace along with the message on the log tab and returns a
	 * combination of both of them as string.
	 *
	 * @param tab       the origin of the message
	 * @param msg		the message for the exception
	 * @param t		    the exception
	 * @return		    the full error message (message + stacktrace)
	 */
	public String handleException(AbstractExperimenterTab tab, String msg, Throwable t) {
		String    result;

		result = ExceptionUtils.handleException(tab, msg, t, false);
		log(null, result);

		return result;
	}

	/**
	 * Processes the commandline arguments.
	 *
	 * @param args the arguments
	 */
	public void processCommandLineArgs(String[] args) {
		if (args.length > 0)
			open(new File(args[0]), m_FileChooser.getReaderForFile(new File(args[0])));
	}

	/**
	 * Starts the GUI.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) throws Exception {
		GUILauncher.launchApplication(Experimenter.class, "MEKA Experimenter", true, args);
	}
}
