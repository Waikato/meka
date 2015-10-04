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
 * Explorer.java
 * Copyright (C) 2012-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import meka.core.ExceptionUtils;
import meka.core.MLUtils;
import meka.gui.core.*;
import meka.gui.events.RecentItemEvent;
import meka.gui.events.RecentItemListener;
import weka.core.Instances;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ConverterUtils;
import weka.core.converters.SerializedInstancesLoader;
import weka.gui.ConverterFileChooser;
import weka.gui.ViewerDialog;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Explorer GUI for MEKA.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Explorer
		extends MekaPanel
		implements MenuBarProvider, CommandLineArgsHandler {

	/** for serialization. */
	private static final long serialVersionUID = 8958333625051395461L;

	/** the file to store the recent files in. */
	public final static String SESSION_FILE = "ExplorerSession.props";

	/** the tabbed pane for the various panels. */
	protected JTabbedPane m_TabbedPane;

	/** the tabs. */
	protected ArrayList<AbstractExplorerTab> m_Tabs;

	/** the menu bar. */
	protected JMenuBar m_MenuBar;

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

	/** the "undo" menu item. */
	protected JMenuItem m_MenuItemEditUndo;

	/** the "redo" menu item. */
	protected JMenuItem m_MenuItemEditData;

	/** the recent files handler. */
	protected RecentFilesHandlerWithCommandline<JMenu> m_RecentFilesHandler;

	/** data currently loaded. */
	protected Instances m_Data;

	/** the current file. */
	protected File m_CurrentFile;

	/** the file chooser for loading/saving files. */
	protected ConverterFileChooser m_FileChooser;

	/** the undo list. */
	protected ArrayList<File> m_Undo;

	/** the statusbar to use. */
	protected StatusBar m_StatusBar;

	/** the log tab. */
	protected LogTab m_LogTab;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		m_Data        = null;
		m_CurrentFile = null;
		m_MenuBar     = null;
		m_Tabs        = new ArrayList<>();
		m_FileChooser = GUIHelper.newConverterFileChooser();
		m_Undo        = new ArrayList<>();
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
		m_Tabs.add(new PreprocessTab());
		classnames = AbstractExplorerTab.getTabs();
		for (String classname: classnames) {
			try {
				AbstractExplorerTab tab = (AbstractExplorerTab) Class.forName(classname).newInstance();
				if (tab instanceof PreprocessTab)
					continue;
				if (tab instanceof VisualizeTab)
					continue;
				if (tab instanceof LogTab)
					continue;
			m_Tabs.add(tab);
			}
			catch (Exception e) {
				System.err.println("Failed to instantiate Explorer tab: " + classname);
				e.printStackTrace();
			}
		}
		m_Tabs.add(new VisualizeTab());
		for (AbstractExplorerTab tab: m_Tabs) {
			tab.setOwner(this);
			m_TabbedPane.addTab(tab.getTitle(), tab);
		}
		m_LogTab = new LogTab();
		m_Tabs.add(m_LogTab);

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
		for (AbstractExplorerTab tab: m_Tabs)
			tab.update();
		updateMenu();
	}

	/**
	 * Notifies all the tabs that the data has changed.
	 *
	 * @param source not null if a tab triggered this call
	 * @param data the new data to use
	 */
	public void notifyTabsDataChanged(AbstractExplorerTab source, Instances data) {
		m_Data = data;
		for (AbstractExplorerTab tab: m_Tabs) {
			if ((source != null) && (tab == source))
				continue;
			tab.setData(data);
		}
	}

	/**
	 * Returns the menu bar to use.
	 *
	 * @return the menu bar
	 */
	public JMenuBar getMenuBar() {
		JMenuBar	result;
		JMenu		menu;
		JMenu		submenu;
		JMenuItem	menuitem;

		if (m_MenuBar == null) {
			result = new JMenuBar();

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
					open(e.getItem().getFile(), (AbstractFileLoader) e.getItem().getHandler());
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

			// Edit
			menu = new JMenu("Edit");
			menu.setMnemonic('E');
			menu.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					updateMenu();
				}
			});
			result.add(menu);

			// Edit/Undo
			menuitem = new JMenuItem("Undo", GUIHelper.getIcon("undo.gif"));
			menuitem.setMnemonic('U');
			menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed Z"));
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					undo();
				}
			});
			menu.add(menuitem);
			m_MenuItemEditUndo = menuitem;

			// Edit/Data
			menuitem = new JMenuItem("Data", GUIHelper.getIcon("report.gif"));
			menuitem.setMnemonic('D');
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					edit();
				}
			});
			menu.add(menuitem);
			m_MenuItemEditData = menuitem;

			// additional tabs?
			for (AbstractExplorerTab tab: m_Tabs) {
				menu = tab.getMenu();
				if (menu != null)
					result.add(menu);
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
		if (m_MenuBar == null)
			return;

		// File
		m_MenuItemFileOpen.setEnabled(true);
		m_MenuItemFileSave.setEnabled((getCurrentFile() != null) && (getCurrentData() != null));
		m_MenuItemFileSaveAs.setEnabled((getCurrentData() != null));
		m_MenuItemFileClose.setEnabled(true);
		// Edit
		m_MenuItemEditUndo.setEnabled(canUndo());
		m_MenuItemEditData.setEnabled(getCurrentData() != null);
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
	 * Returns the currently loaded data.
	 *
	 * @return      the data, null if none loaded
	 */
	public Instances getCurrentData() {
		return m_Data;
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
	 * Opens the specified file.
	 *
	 * @param file the file to open
	 * @param loader the loader to use
	 */
	public void open(File file, AbstractFileLoader loader) {
		Instances data;

		// load data
		try {
			log(null, "Loading: " + file);
			loader.setFile(file);
			data = loader.getDataSet();
			// fix class attributes definition in relation name if necessary
			MLUtils.fixRelationName(data);
			log(null, "Loaded successfully: " + file);
		}
		catch (Exception e) {
			handleException(null, "Failed to load data from '" + file + "':", e);
			JOptionPane.showMessageDialog(
					this,
					"Failed to load dataset from '" + file + "':\n" + e,
					"Error loading",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// prepare data
		try {
			addUndoPoint();
			MLUtils.prepareData(data);
			m_CurrentFile = file;
			notifyTabsDataChanged(null, data);
			m_RecentFilesHandler.addRecentItem(new RecentFilesHandlerWithCommandline.Setup(file, loader));
		}
		catch (Exception e) {
			handleException(null, "Failed to prepare data from '" + file + "':", e);
			JOptionPane.showMessageDialog(
					this,
					"Failed to load prepare data from '" + file + "':\n" + e,
					"Error loading",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		updateMenu();
	}

	/**
	 * Opens a dataset.
	 */
	public void open() {
		int retVal;

		retVal = m_FileChooser.showOpenDialog(this);
		if (retVal != ConverterFileChooser.APPROVE_OPTION)
			return;

		open(m_FileChooser.getSelectedFile(), m_FileChooser.getLoader());
	}

	/**
	 * Saves the data to the specified file.
	 *
	 * @param file the file to save the data to
	 * @param saver the saver to use, determines it automatically if null
	 */
	public void save(File file, AbstractFileSaver saver) {
		if (saver == null)
			saver = ConverterUtils.getSaverForFile(file);
		try {
			log(null, "Saving: " + file);
			saver.setInstances(m_Data);
			if ((saver.retrieveFile() == null) || !saver.retrieveFile().equals(file))
				saver.setFile(file);
			saver.writeBatch();
			m_CurrentFile = file;
			log(null, "Saved successfully: " + file);
		}
		catch (Exception e) {
			handleException(null, "Failed to save data to '" + file + "':", e);
			JOptionPane.showMessageDialog(
					this,
					"Failed to save dataset to '" + file + "':\n" + e,
					"Error saving",
					JOptionPane.ERROR_MESSAGE);
		}

		updateMenu();
	}

	/**
	 * Saves the current dataset.
	 */
	public void save() {
		if (m_CurrentFile == null) {
			saveAs();
			return;
		}

		save(m_CurrentFile, null);
	}

	/**
	 * Saves the current dataset under a new name.
	 */
	public void saveAs() {
		int retVal;

		m_FileChooser.setSelectedFile(m_CurrentFile);
		retVal = m_FileChooser.showSaveDialog(this);
		if (retVal != ConverterFileChooser.APPROVE_OPTION)
			return;

		save(m_FileChooser.getSelectedFile(), m_FileChooser.getSaver());
	}

	/**
	 * Closes the explorer.
	 */
	public void close() {
		closeParent();
	}

	/**
	 * edits the current instances object in the viewer 
	 */
	public void edit() {
		ViewerDialog        dialog;
		int                 result;
		Instances           copy;
		Instances           newInstances;

		copy   = new Instances(m_Data);
		dialog = new ViewerDialog(null);
		dialog.setSize(800, 600);
		dialog.setLocationRelativeTo(this);
		result = dialog.showDialog(copy);
		if (result == ViewerDialog.APPROVE_OPTION) {
			try {
				addUndoPoint();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// if class was not set before, reset it again after use of filter
			newInstances = dialog.getInstances();
			if (m_Data.classIndex() < 0)
				newInstances.setClassIndex(-1);
			notifyTabsDataChanged(null, newInstances);
		}
	}

	/**
	 * Adds an undo point.
	 *
	 * @return	true if successfully added
	 */
	public boolean addUndoPoint() {
		boolean		result;
		File 		tempFile;
		ObjectOutputStream 	oos;
		ArrayList		data;

		if (m_Data == null)
			return false;

		tempFile = null;
		try {
			// create temporary file
			tempFile = File.createTempFile("meka", SerializedInstancesLoader.FILE_EXTENSION);
			tempFile.deleteOnExit();

			data = new ArrayList();
			data.add(m_CurrentFile);
			data.add(m_Data);

			// save data
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
			oos.writeObject(data);
			oos.flush();
			oos.close();

			m_Undo.add(tempFile);
			result = true;
		}
		catch (Exception e) {
			result = false;
			handleException(null, "Failed to save undo data to '" + tempFile + "':", e);
			JOptionPane.showMessageDialog(
					this,
					"Failed to save undo data to '" + tempFile + "':\n" + e,
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}

		updateMenu();

		return result;
	}

	/**
	 * Returns whether any operations can be undone currently.
	 *
	 * @return		true undo is possible
	 */
	public boolean canUndo() {
		return (m_Undo.size() > 0);
	}

	/**
	 * Undos the last operation.
	 */
	public void undo() {
		File		file;
		ArrayList		data;
		Instances		inst;
		ObjectInputStream	ois;

		if (m_Undo.size() == 0)
			return;

		// load instances from the temporary file
		file = m_Undo.get(m_Undo.size() - 1);
		m_Undo.remove(m_Undo.size() - 1);
		try {
			ois  = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			data = (ArrayList) ois.readObject();

			m_CurrentFile = (File) data.get(0);
			inst          = (Instances) data.get(1);
			notifyTabsDataChanged(null, inst);
		}
		catch (Exception e) {
			handleException(null, "Failed to load undo data from '" + file + "':", e);
			JOptionPane.showMessageDialog(
					this,
					"Failed to load undo data from '" + file + "':\n" + e,
					"Undo",
					JOptionPane.ERROR_MESSAGE);
		}

		updateMenu();
	}

	/**
	 * For logging messages.
	 *
	 * @param tab       the origin of the message
	 * @param msg       the message to output
	 */
	protected synchronized void log(AbstractExplorerTab tab, String msg) {
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
	public String handleException(AbstractExplorerTab tab, String msg, Throwable t) {
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
			open(new File(args[0]), ConverterUtils.getLoaderForFile(args[0]));
	}

	/**
	 * Starts the GUI.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) throws Exception {
		GUILauncher.launchApplication(Explorer.class, "MEKA Explorer", true, args);
	}
}
