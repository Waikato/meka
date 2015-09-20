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
 * ModelViewer.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.modelviewer;

import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.gui.core.*;
import meka.gui.events.RecentItemEvent;
import meka.gui.events.RecentItemListener;
import weka.core.SerializationHelper;
import weka.gui.ExtensionFileFilter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Simple viewer for serialized model files.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ModelViewer
		extends MekaPanel
		implements MenuBarProvider, CommandLineArgsHandler {

	private static final long serialVersionUID = -3845454961273213245L;

	/** the file to store the recent files in. */
	public final static String SESSION_FILE = "ModelViewerSession.props";
	public static final String NO_FILE_LOADED = "No file loaded";

	/** the filechooser for loading the model files. */
	protected MekaFileChooser m_FileChooser;

	/** the tabbed pane for displaying the content of the files. */
	protected JTabbedPane m_TabbedPane;

	/** the panel for the file. */
	protected JPanel m_PanelFile;

	/** the label for the filename. */
	protected JLabel m_LabelFile;

	/** the menu bar. */
	protected JMenuBar m_MenuBar;

	/** the "open" menu item. */
	protected JMenuItem m_MenuItemFileOpen;

	/** the "load recent" submenu. */
	protected JMenu m_MenuFileOpenRecent;

	/** the "close" menu item. */
	protected JMenuItem m_MenuItemFileClose;

	/** the recent files handler. */
	protected RecentFilesHandler<JMenu> m_RecentFilesHandler;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		ExtensionFileFilter     filter;

		super.initialize();

		m_FileChooser = GUIHelper.newFileChooser();
		filter        = new ExtensionFileFilter(".model", "Model files (*.model)");
		m_FileChooser.addChoosableFileFilter(filter);
		m_FileChooser.setFileFilter(filter);
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		setLayout(new BorderLayout());

		m_TabbedPane = new JTabbedPane();
		add(m_TabbedPane, BorderLayout.CENTER);

		m_PanelFile = new JPanel(new FlowLayout(FlowLayout.LEFT));
		m_PanelFile.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		add(m_PanelFile, BorderLayout.NORTH);

		m_LabelFile = new JLabel(NO_FILE_LOADED);
		m_PanelFile.add(m_LabelFile);
	}

	/**
	 * Returns the menu bar to use.
	 *
	 * @return the menu bar
	 */
	@Override
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
			m_RecentFilesHandler = new RecentFilesHandler<JMenu>(SESSION_FILE, 5, submenu);
			m_RecentFilesHandler.addRecentItemListener(new RecentItemListener<JMenu, File>() {
				@Override
				public void recentItemAdded(RecentItemEvent<JMenu, File> e) {
					// ignored
				}

				@Override
				public void recentItemSelected(RecentItemEvent<JMenu, File> e) {
					open(e.getItem());
					updateMenu();
				}
			});
			m_MenuFileOpenRecent = submenu;

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
		m_MenuItemFileClose.setEnabled(true);
	}

	/**
	 * Prompts the user to select a model file.
	 */
	public void open() {
		int     retVal;

		retVal = m_FileChooser.showOpenDialog(this);
		if (retVal != MekaFileChooser.APPROVE_OPTION)
			return;

		open(m_FileChooser.getSelectedFile());
	}

	/**
	 * Opens the specified model file.
	 *
	 * @param file the model file to load
	 */
	public void open(File file) {
		Object[]        data;

		m_TabbedPane.removeAll();
		try {
			data = SerializationHelper.readAll(file.getAbsolutePath());
			for (Object obj: data) {
				if (obj == null)
					continue;
				JTextArea text = new JTextArea(20, 40);
				text.setFont(GUIHelper.getMonospacedFont());
				// TODO : handler class hierarchy
				if (obj instanceof MultiLabelClassifier)
					text.setText(((MultiLabelClassifier) obj).getModel());
				else
					text.setText("" + obj);
				m_TabbedPane.addTab(obj.getClass().getName(), new BaseScrollPane(text));
			}
			m_RecentFilesHandler.addRecentItem(file);
			m_LabelFile.setText(file.getAbsolutePath());
		}
		catch (Exception e) {
			m_TabbedPane.removeAll();
			m_LabelFile.setText(NO_FILE_LOADED);
			System.err.println("Failed to load data from '" + file + "':");
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to load dataset from '" + file + "':\n" + e,
					"Error loading",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Closes the viewer.
	 */
	public void close() {
		closeParent();
	}

	/**
	 * Processes the commandline arguments.
	 *
	 * @param args the arguments
	 */
	public void processCommandLineArgs(String[] args) {
		if (args.length > 0)
			open(new File(args[0]));
	}

	/**
	 * Starts the GUI.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) throws Exception {
		GUILauncher.launchApplication(ModelViewer.class, "Model viewer", true, args);
	}
}
