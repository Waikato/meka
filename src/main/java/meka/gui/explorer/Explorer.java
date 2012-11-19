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
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import meka.gui.core.GUIHelper;
import meka.gui.core.MekaPanel;
import meka.gui.goe.GenericObjectEditor;
import weka.core.Instances;
import weka.core.MLUtils;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ConverterUtils;
import weka.gui.BrowserHelper;
import weka.gui.ConverterFileChooser;

/**
 * Explorer GUI for MEKA.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Explorer
	extends MekaPanel {

	/** for serialization. */
	private static final long serialVersionUID = 8958333625051395461L;
	
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

	/** the "save as" menu item. */
	protected JMenuItem m_MenuItemFileSaveAs;

	/** the "close" menu item. */
	protected JMenuItem m_MenuItemFileClose;

	/** the "homepage" menu item. */
	protected JMenuItem m_MenuItemFileHomepage;
	
	/** data currently loaded. */
	protected Instances m_Data;
	
	/** the current file. */
	protected File m_CurrentFile;
	
	/** the file chooser for loading/saving files. */
	protected ConverterFileChooser m_FileChooser;
	
	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		m_Data        = null;
		m_CurrentFile = null;
		m_MenuBar     = null;
		m_Tabs        = new ArrayList<AbstractExplorerTab>();
		m_FileChooser = new ConverterFileChooser(System.getProperty("user.home"));
	}
	
	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();
		
		m_TabbedPane = new JTabbedPane();
		add(m_TabbedPane, BorderLayout.CENTER);
		
		// tabs
		m_Tabs.add(new PreprocessTab(this));
		m_Tabs.add(new ClassifyTab(this));
		for (AbstractExplorerTab tab: m_Tabs)
			m_TabbedPane.addTab(tab.getTitle(), tab);
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
		JMenu			menu;
		JMenuItem	menuitem;
		
		if (m_MenuBar == null) {
			result = new JMenuBar();
			
			// File
			menu = new JMenu("File");
			menu.setMnemonic('F');
			menu.addChangeListener(new ChangeListener() {
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
			
			// Help
			menu = new JMenu("Help");
			menu.setMnemonic('H');
			menu.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					updateMenu();
				}
			});
			result.add(menu);
			
			// Help/Homepage
			menuitem = new JMenuItem("Homepage", GUIHelper.getIcon("homepage.png"));
			menuitem.setMnemonic('H');
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openHomepage();
				}
			});
			menu.add(menuitem);
			m_MenuItemFileHomepage = menuitem;
			
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
		m_MenuItemFileSave.setEnabled((m_CurrentFile != null) && (m_Data != null));
		m_MenuItemFileSaveAs.setEnabled((m_Data != null));
		m_MenuItemFileClose.setEnabled(true);
		// Help
		m_MenuItemFileHomepage.setEnabled(true);
	}
	
	/**
	 * Prepares the class index of the data.
	 * 
	 * @param data the data to prepare
	 * @throws Exception if preparation fails
	 */
	protected void prepareData(Instances data) throws Exception {
		String doptions[] = null;
		try {
			doptions = MLUtils.getDatasetOptions(data);
		} 
		catch(Exception e) {
			throw new Exception("[Error] Failed to Get Options from @Relation Name", e);
		}

		int c = (Utils.getOptionPos('C', doptions) >= 0) ? Integer.parseInt(Utils.getOption('C',doptions)) : Integer.parseInt(Utils.getOption('c',doptions));
		// if negative, then invert ...
		if ( c < 0) {
			c = -c;
			data = MLUtils.switchAttributes(data,c);
		}
		// end
		data.setClassIndex(c);
	}
	
	/**
	 * Opens the specified file.
	 * 
	 * @param file the file to open
	 * @param loader the loader to use
	 */
	protected void open(File file, AbstractFileLoader loader) {
		Instances data;

		try {
			data          = loader.getDataSet();
			m_CurrentFile = file;
			prepareData(data);
			notifyTabsDataChanged(null, data);
		}
		catch (Exception e) {
			System.err.println("Failed to load data from '" + file + "':");
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					this, 
					"Failed to load dataset from '" + file + "':\n" + e, 
					"Error loading",
					JOptionPane.ERROR_MESSAGE);
		}

		updateMenu();
	}
	
	/**
	 * Opens a dataset.
	 */
	protected void open() {
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
	protected void save(File file, AbstractFileSaver saver) {
		if (saver == null)
			saver = ConverterUtils.getSaverForFile(file);
		try {
			saver.setFile(file);
			saver.writeBatch();
			m_CurrentFile = file;
		}
		catch (Exception e) {
			System.err.println("Failed to save data to '" + file + "':");
			e.printStackTrace();
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
	protected void save() {
		if (m_CurrentFile == null) {
			saveAs();
			return;
		}
		
		save(m_CurrentFile, null);
	}
	
	/**
	 * Saves the current dataset under a new name.
	 */
	protected void saveAs() {
		int retVal;
		
		retVal = m_FileChooser.showSaveDialog(this);
		if (retVal != ConverterFileChooser.APPROVE_OPTION)
			return;
		
		save(m_FileChooser.getSelectedFile(), m_FileChooser.getSaver());
	}
	
	/**
	 * Closes the explorer.
	 */
	protected void close() {
		closeParent();
	}

	/**
	 * Opens the homepage in a browser.
	 */
	protected void openHomepage() {
		BrowserHelper.openURL("http://meka.sourceforge.net/");
	}

	/**
	 * Starts the GUI.
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) throws Exception {
		GenericObjectEditor.registerAllEditors();
		Explorer main = new Explorer();
		JFrame frame = new JFrame();
		frame.setTitle("MEKA Explorer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(GUIHelper.getLogoIcon().getImage());
		frame.setLayout(new BorderLayout());
		frame.add(main, BorderLayout.CENTER);
		frame.setJMenuBar(main.getMenuBar());
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
