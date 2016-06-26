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
 * DataViewerMainPanel.java
 * Copyright (C) 2005-2012 University of Waikato, Hamilton, New Zealand
 *
 */

package meka.gui.dataviewer;

import meka.gui.core.GUIHelper;
import meka.gui.core.RecentFilesHandlerWithCommandline;
import meka.gui.core.RecentFilesHandlerWithCommandline.Setup;
import meka.gui.events.RecentItemEvent;
import meka.gui.events.RecentItemListener;
import weka.core.Capabilities;
import weka.core.Instances;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.AbstractSaver;
import weka.core.converters.ConverterUtils;
import weka.gui.ConverterFileChooser;
import weka.gui.JTableHelper;
import weka.gui.ListSelectorDialog;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
 * The main panel of the DataViewer. It has a reference to the menu, that an
 * implementing JFrame only needs to add via the setJMenuBar(JMenuBar) method.
 *
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11342 $
 */

public class DataViewerMainPanel extends JPanel implements ActionListener,
	ChangeListener {

	/** for serialization */
	static final long serialVersionUID = -8763161167586738753L;

	/** the file to store the recent files in. */
	public final static String SESSION_FILE = "DataViewerSession.props";

	/** default width */
	public final static int WIDTH = 800;

	/** default height */
	public final static int HEIGHT = 600;

	protected Container m_Parent;
	protected JTabbedPane m_TabbedPane;
	protected JMenuBar m_MenuBar;
	protected JMenu m_MenuFile;
	protected JMenuItem m_MenuFileOpen;
	protected JMenuItem m_MenuFileOpenRecent;
	protected JMenuItem m_MenuFileSave;
	protected JMenuItem m_MenuFileSaveAs;
	protected JMenuItem m_MenuFileClose;
	protected JMenuItem m_MenuFileCloseAll;
	protected JMenuItem m_MenuFileProperties;
	protected JMenuItem m_MenuFileExit;
	protected JMenu m_MenuEdit;
	protected JMenuItem m_MenuEditUndo;
	protected JMenuItem m_MenuEditCopy;
	protected JMenuItem m_MenuEditSearch;
	protected JMenuItem m_MenuEditClearSearch;
	protected JMenuItem m_MenuEditDeleteAttribute;
	protected JMenuItem m_MenuEditDeleteAttributes;
	protected JMenuItem m_MenuEditRenameAttribute;
	protected JMenuItem m_MenuEditAttributeAsClass;
	protected JMenuItem m_MenuEditDeleteInstance;
	protected JMenuItem m_MenuEditDeleteInstances;
	protected JMenuItem m_MenuEditSortInstances;
	protected JMenu m_MenuView;
	protected JMenuItem m_MenuViewAttributes;
	protected JMenuItem m_MenuViewValues;
	protected JMenuItem m_MenuViewOptimalColWidths;

	protected ConverterFileChooser m_FileChooser;
	protected String m_FrameTitle;
	protected boolean m_ConfirmExit;
	protected boolean m_ExitOnClose;

	/** the recent files handler. */
	protected RecentFilesHandlerWithCommandline<JMenu> m_RecentFilesHandler;

	/**
	 * initializes the object
	 *
	 * @param parentFrame the parent frame (JFrame or JInternalFrame)
	 */
	public DataViewerMainPanel(Container parentFrame) {
		m_Parent = parentFrame;
		m_FrameTitle = "Data-Viewer";
		createPanel();
	}

	/**
	 * creates all the components in the panel
	 */
	protected void createPanel() {
		JMenu	submenu;

		// basic setup
		setSize(WIDTH, HEIGHT);

		setConfirmExit(false);
		setLayout(new BorderLayout());

		// file dialog
		m_FileChooser = GUIHelper.newConverterFileChooser();
		m_FileChooser.setMultiSelectionEnabled(true);

		// menu
		m_MenuBar = new JMenuBar();
		m_MenuFile = new JMenu("File");
		m_MenuFileOpen = new JMenuItem("Open...",
			GUIHelper.getIcon("open.gif"));
		m_MenuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
			KeyEvent.CTRL_MASK));
		m_MenuFileOpen.addActionListener(this);

		// File/Recent files
		submenu = new JMenu("Open recent");
		m_RecentFilesHandler = new RecentFilesHandlerWithCommandline<JMenu>(SESSION_FILE, 5, submenu);
		m_RecentFilesHandler.addRecentItemListener(new RecentItemListener<JMenu, Setup>() {
			@Override
			public void recentItemAdded(RecentItemEvent<JMenu, Setup> e) {
				// ignored
			}

			@Override
			public void recentItemSelected(RecentItemEvent<JMenu, RecentFilesHandlerWithCommandline.Setup> e) {
				loadFile(e.getItem().getFile().getAbsolutePath(), (AbstractFileLoader) e.getItem().getHandler());
				updateMenu();
			}
		});
		m_MenuFileOpenRecent = submenu;

		m_MenuFileSave = new JMenuItem("Save",
			GUIHelper.getIcon("save.gif"));
		m_MenuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
			KeyEvent.CTRL_MASK));
		m_MenuFileSave.addActionListener(this);
		m_MenuFileSaveAs = new JMenuItem("Save as...",
			GUIHelper.getIcon("empty.gif"));
		m_MenuFileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
			KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		m_MenuFileSaveAs.addActionListener(this);
		m_MenuFileClose = new JMenuItem("Close",
			GUIHelper.getIcon("empty.gif"));
		m_MenuFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
			KeyEvent.CTRL_MASK));
		m_MenuFileClose.addActionListener(this);
		m_MenuFileCloseAll = new JMenuItem("Close all",
			GUIHelper.getIcon("empty.gif"));
		m_MenuFileCloseAll.addActionListener(this);
		m_MenuFileProperties = new JMenuItem("Properties",
			GUIHelper.getIcon("empty.gif"));
		m_MenuFileProperties.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
			KeyEvent.CTRL_MASK));
		m_MenuFileProperties.addActionListener(this);
		m_MenuFileExit = new JMenuItem("Exit",
			GUIHelper.getIcon("exit.png"));
		m_MenuFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
			KeyEvent.ALT_MASK));
		m_MenuFileExit.addActionListener(this);

		m_MenuFile.add(m_MenuFileOpen);
		m_MenuFile.add(m_MenuFileOpenRecent);
		m_MenuFile.add(m_MenuFileSave);
		m_MenuFile.add(m_MenuFileSaveAs);
		m_MenuFile.add(m_MenuFileClose);
		m_MenuFile.add(m_MenuFileCloseAll);
		m_MenuFile.addSeparator();
		m_MenuFile.add(m_MenuFileProperties);
		m_MenuFile.addSeparator();
		m_MenuFile.add(m_MenuFileExit);
		m_MenuBar.add(m_MenuFile);

		m_MenuEdit = new JMenu("Edit");
		m_MenuEditUndo = new JMenuItem("Undo",
			GUIHelper.getIcon("undo.gif"));
		m_MenuEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
			KeyEvent.CTRL_MASK));
		m_MenuEditUndo.addActionListener(this);
		m_MenuEditCopy = new JMenuItem("Copy",
			GUIHelper.getIcon("copy.gif"));
		m_MenuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,
			KeyEvent.CTRL_MASK));
		m_MenuEditCopy.addActionListener(this);
		m_MenuEditSearch = new JMenuItem("Search...",
			GUIHelper.getIcon("find.gif"));
		m_MenuEditSearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
			KeyEvent.CTRL_MASK));
		m_MenuEditSearch.addActionListener(this);
		m_MenuEditClearSearch = new JMenuItem("Clear search",
			GUIHelper.getIcon("empty.gif"));
		m_MenuEditClearSearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
			KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		m_MenuEditClearSearch.addActionListener(this);
		m_MenuEditRenameAttribute = new JMenuItem("Rename attribute",
			GUIHelper.getIcon("empty.gif"));
		m_MenuEditRenameAttribute.addActionListener(this);
		m_MenuEditAttributeAsClass = new JMenuItem("Attribute as class",
			GUIHelper.getIcon("empty.gif"));
		m_MenuEditAttributeAsClass.addActionListener(this);
		m_MenuEditDeleteAttribute = new JMenuItem("Delete attribute",
			GUIHelper.getIcon("empty.gif"));
		m_MenuEditDeleteAttribute.addActionListener(this);
		m_MenuEditDeleteAttributes = new JMenuItem("Delete attributes",
			GUIHelper.getIcon("empty.gif"));
		m_MenuEditDeleteAttributes.addActionListener(this);
		m_MenuEditDeleteInstance = new JMenuItem("Delete instance",
			GUIHelper.getIcon("empty.gif"));
		m_MenuEditDeleteInstance.addActionListener(this);
		m_MenuEditDeleteInstances = new JMenuItem("Delete instances",
			GUIHelper.getIcon("empty.gif"));
		m_MenuEditDeleteInstances.addActionListener(this);
		m_MenuEditSortInstances = new JMenuItem("Sort data (ascending)",
			GUIHelper.getIcon("sort.gif"));
		m_MenuEditSortInstances.addActionListener(this);

		m_MenuEdit.add(m_MenuEditUndo);
		m_MenuEdit.addSeparator();
		m_MenuEdit.add(m_MenuEditCopy);
		m_MenuEdit.addSeparator();
		m_MenuEdit.add(m_MenuEditSearch);
		m_MenuEdit.add(m_MenuEditClearSearch);
		m_MenuEdit.addSeparator();
		m_MenuEdit.add(m_MenuEditRenameAttribute);
		m_MenuEdit.add(m_MenuEditAttributeAsClass);
		m_MenuEdit.add(m_MenuEditDeleteAttribute);
		m_MenuEdit.add(m_MenuEditDeleteAttributes);
		m_MenuEdit.addSeparator();
		m_MenuEdit.add(m_MenuEditDeleteInstance);
		m_MenuEdit.add(m_MenuEditDeleteInstances);
		m_MenuEdit.add(m_MenuEditSortInstances);
		m_MenuBar.add(m_MenuEdit);

		m_MenuView = new JMenu("View");
		m_MenuViewAttributes = new JMenuItem("Attributes...",
			GUIHelper.getIcon("objects.gif"));
		m_MenuViewAttributes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
			KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		m_MenuViewAttributes.addActionListener(this);
		m_MenuViewValues = new JMenuItem("Values...",
			GUIHelper.getIcon("properties.gif"));
		m_MenuViewValues.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
			KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		m_MenuViewValues.addActionListener(this);
		m_MenuViewOptimalColWidths = new JMenuItem("Optimal column width (all)",
			GUIHelper.getIcon("resize.gif"));
		m_MenuViewOptimalColWidths.addActionListener(this);

		m_MenuView.add(m_MenuViewAttributes);
		m_MenuView.add(m_MenuViewValues);
		m_MenuView.addSeparator();
		m_MenuView.add(m_MenuViewOptimalColWidths);
		m_MenuBar.add(m_MenuView);

		// tabbed pane
		m_TabbedPane = new JTabbedPane();
		m_TabbedPane.addChangeListener(this);
		add(m_TabbedPane, BorderLayout.CENTER);

		updateMenu();
		updateFrameTitle();
	}

	/**
	 * returns the parent frame, if it's a JFrame, otherwise null
	 *
	 * @return the parent frame
	 */
	public JFrame getParentFrame() {
		if (m_Parent instanceof JFrame) {
			return (JFrame) m_Parent;
		} else {
			return null;
		}
	}

	/**
	 * returns the parent frame, if it's a JInternalFrame, otherwise null
	 *
	 * @return the parent frame
	 */
	public JInternalFrame getParentInternalFrame() {
		if (m_Parent instanceof JInternalFrame) {
			return (JInternalFrame) m_Parent;
		} else {
			return null;
		}
	}

	/**
	 * sets the new parent frame
	 *
	 * @param value the parent frame
	 */
	public void setParent(Container value) {
		m_Parent = value;
	}

	/**
	 * returns the menu bar to be added in a frame
	 *
	 * @return the menu bar
	 */
	public JMenuBar getMenu() {
		return m_MenuBar;
	}

	/**
	 * returns the tabbedpane instance
	 *
	 * @return the tabbed pane
	 */
	public JTabbedPane getTabbedPane() {
		return m_TabbedPane;
	}

	/**
	 * whether to present a MessageBox on Exit or not
	 *
	 * @param confirm whether a MessageBox pops up or not to confirm exit
	 */
	public void setConfirmExit(boolean confirm) {
		m_ConfirmExit = confirm;
	}

	/**
	 * returns the setting of whether to display a confirm messagebox or not on
	 * exit
	 *
	 * @return whether a messagebox is displayed or not
	 */
	public boolean getConfirmExit() {
		return m_ConfirmExit;
	}

	/**
	 * whether to do a System.exit(0) on close
	 *
	 * @param value enables/disables a System.exit(0) on close
	 */
	public void setExitOnClose(boolean value) {
		m_ExitOnClose = value;
	}

	/**
	 * returns TRUE if a System.exit(0) is done on a close
	 *
	 * @return true if a System.exit(0) is done on close
	 */
	public boolean getExitOnClose() {
		return m_ExitOnClose;
	}

	/**
	 * validates and repaints the frame
	 */
	public void refresh() {
		validate();
		repaint();
	}

	/**
	 * returns the title (incl. filename) for the frame
	 *
	 * @return the frame title
	 */
	public String getFrameTitle() {
		if (getCurrentFilename().equals("")) {
			return m_FrameTitle;
		} else {
			return m_FrameTitle + " - " + getCurrentFilename();
		}
	}

	/**
	 * sets the title of the parent frame, if one was provided
	 */
	public void updateFrameTitle() {
		if (getParentFrame() != null) {
			getParentFrame().setTitle(getFrameTitle());
		}
		if (getParentInternalFrame() != null) {
			getParentInternalFrame().setTitle(getFrameTitle());
		}
	}

	/**
	 * sets the enabled/disabled state of the menu
	 */
	protected void updateMenu() {
		boolean fileOpen;
		boolean isChanged;
		boolean canUndo;

		fileOpen = (getCurrentPanel() != null);
		isChanged = fileOpen && (getCurrentPanel().isChanged());
		canUndo = fileOpen && (getCurrentPanel().canUndo());

		// File
		m_MenuFileOpen.setEnabled(true);
		m_MenuFileSave.setEnabled(isChanged);
		m_MenuFileSaveAs.setEnabled(fileOpen);
		m_MenuFileClose.setEnabled(fileOpen);
		m_MenuFileCloseAll.setEnabled(fileOpen);
		m_MenuFileProperties.setEnabled(fileOpen);
		m_MenuFileExit.setEnabled(true);
		// Edit
		m_MenuEditUndo.setEnabled(canUndo);
		m_MenuEditCopy.setEnabled(fileOpen);
		m_MenuEditSearch.setEnabled(fileOpen);
		m_MenuEditClearSearch.setEnabled(fileOpen);
		m_MenuEditAttributeAsClass.setEnabled(fileOpen);
		m_MenuEditRenameAttribute.setEnabled(fileOpen);
		m_MenuEditDeleteAttribute.setEnabled(fileOpen);
		m_MenuEditDeleteAttributes.setEnabled(fileOpen);
		m_MenuEditDeleteInstance.setEnabled(fileOpen);
		m_MenuEditDeleteInstances.setEnabled(fileOpen);
		m_MenuEditSortInstances.setEnabled(fileOpen);
		// View
		m_MenuViewAttributes.setEnabled(fileOpen);
		m_MenuViewValues.setEnabled(fileOpen);
		m_MenuViewOptimalColWidths.setEnabled(fileOpen);
	}

	/**
	 * sets the title of the tab that contains the given component
	 *
	 * @param component the component to set the title for
	 */
	protected void setTabTitle(JComponent component) {
		int index;

		if (!(component instanceof DataPanel)) {
			return;
		}

		index = m_TabbedPane.indexOfComponent(component);
		if (index == -1) {
			return;
		}

		m_TabbedPane.setTitleAt(index, ((DataPanel) component).getTitle());
		updateFrameTitle();
	}

	/**
	 * returns the number of panels currently open
	 *
	 * @return the number of open panels
	 */
	public int getPanelCount() {
		return m_TabbedPane.getTabCount();
	}

	/**
	 * returns the specified panel, <code>null</code> if index is out of bounds
	 *
	 * @param index the index of the panel
	 * @return the panel
	 */
	public DataPanel getPanel(int index) {
		if ((index >= 0) && (index < getPanelCount())) {
			return (DataPanel) m_TabbedPane.getComponentAt(index);
		} else {
			return null;
		}
	}

	/**
	 * returns the currently selected tab index
	 *
	 * @return the index of the currently selected tab
	 */
	public int getCurrentIndex() {
		return m_TabbedPane.getSelectedIndex();
	}

	/**
	 * returns the currently selected panel
	 *
	 * @return the currently selected panel
	 */
	public DataPanel getCurrentPanel() {
		return getPanel(getCurrentIndex());
	}

	/**
	 * checks whether a panel is currently selected
	 *
	 * @return true if a panel is currently selected
	 */
	public boolean isPanelSelected() {
		return (getCurrentPanel() != null);
	}

	/**
	 * returns the filename of the specified panel
	 *
	 * @param index the index of the panel
	 * @return the filename for the panel
	 */
	public String getFilename(int index) {
		String result;
		DataPanel panel;

		result = "";
		panel = getPanel(index);

		if (panel != null) {
			result = panel.getFilename();
		}

		return result;
	}

	/**
	 * returns the filename of the current tab
	 *
	 * @return the current filename
	 */
	public String getCurrentFilename() {
		return getFilename(getCurrentIndex());
	}

	/**
	 * sets the filename of the specified panel
	 *
	 * @param index the index of the panel
	 * @param filename the new filename
	 */
	public void setFilename(int index, String filename) {
		DataPanel panel;

		panel = getPanel(index);

		if (panel != null) {
			panel.setFilename(filename);
			setTabTitle(panel);
		}
	}

	/**
	 * sets the filename of the current tab
	 *
	 * @param filename the new filename
	 */
	public void setCurrentFilename(String filename) {
		setFilename(getCurrentIndex(), filename);
	}

	/**
	 * if the file is changed it pops up a dialog whether to change the settings.
	 * if the project wasn't changed or saved it returns TRUE
	 *
	 * @return true if project wasn't changed or saved
	 */
	protected boolean saveChanges() {
		return saveChanges(true);
	}

	/**
	 * if the file is changed it pops up a dialog whether to change the settings.
	 * if the project wasn't changed or saved it returns TRUE
	 *
	 * @param showCancel whether we have YES/NO/CANCEL or only YES/NO
	 * @return true if project wasn't changed or saved
	 */
	protected boolean saveChanges(boolean showCancel) {
		int button;
		boolean result;

		if (!isPanelSelected()) {
			return true;
		}

		result = !getCurrentPanel().isChanged();

		if (getCurrentPanel().isChanged()) {
			try {
				if (showCancel) {
					button = JOptionPane.showConfirmDialog(
						this,
						"The file is not saved - Do you want to save it?",
						"Changed",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				} else {
					button = JOptionPane.showConfirmDialog(
						this,
						"The file is not saved - Do you want to save it?",
						"Changed",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				}
			} catch (Exception e) {
				button = JOptionPane.CANCEL_OPTION;
			}

			switch (button) {
				case JOptionPane.YES_OPTION:
					saveFile();
					result = !getCurrentPanel().isChanged();
					break;
				case JOptionPane.NO_OPTION:
					result = true;
					break;
				case JOptionPane.CANCEL_OPTION:
					result = false;
					break;
			}
		}

		return result;
	}

	/**
	 * loads the specified file
	 *
	 * @param filename the file to load
	 * @param loaders optional varargs loader to use
	 */
	public void loadFile(String filename, AbstractFileLoader... loaders) {
		DataPanel panel;
		AbstractFileLoader loader;

		panel = new DataPanel(filename, loaders);
		panel.addChangeListener(this);
		m_TabbedPane.addTab(panel.getTitle(), panel);
		m_TabbedPane.setSelectedIndex(m_TabbedPane.getTabCount() - 1);
		if (loaders == null)
			loader = ConverterUtils.getLoaderForFile(filename);
		else
			loader = loaders[0];
		m_RecentFilesHandler.addRecentItem(new RecentFilesHandlerWithCommandline.Setup(new File(filename), loader));
	}

	/**
	 * loads the specified file into the table
	 */
	public void loadFile() {
		int retVal;
		int i;
		String filename;

		retVal = m_FileChooser.showOpenDialog(this);
		if (retVal != ConverterFileChooser.APPROVE_OPTION) {
			return;
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		for (i = 0; i < m_FileChooser.getSelectedFiles().length; i++) {
			filename = m_FileChooser.getSelectedFiles()[i].getAbsolutePath();
			loadFile(filename, m_FileChooser.getLoader());
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * saves the current data into a file
	 */
	public void saveFile() {
		DataPanel panel;
		String filename;
		AbstractSaver saver;

		// no panel? -> exit
		panel = getCurrentPanel();
		if (panel == null) {
			return;
		}

		filename = panel.getFilename();

		if (filename.equals(DataPanel.TAB_INSTANCES)) {
			saveFileAs();
		} else {
			saver = ConverterUtils.getSaverForFile(filename);
			try {
				saver.setFile(new File(filename));
				saver.setInstances(panel.getInstances());
				saver.writeBatch();
				panel.setChanged(false);
				setCurrentFilename(filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * saves the current data into a new file
	 */
	public void saveFileAs() {
		int retVal;
		DataPanel panel;

		// no panel? -> exit
		panel = getCurrentPanel();
		if (panel == null) {
			System.out.println("nothing selected!");
			return;
		}

		if (!getCurrentFilename().equals("")) {
			try {
				m_FileChooser.setSelectedFile(new File(getCurrentFilename()));
			} catch (Exception e) {
				// ignore
			}
		}

		// set filter for savers
		try {
			m_FileChooser.setCapabilitiesFilter(Capabilities.forInstances(panel
				.getInstances()));
		} catch (Exception e) {
			m_FileChooser.setCapabilitiesFilter(null);
		}

		retVal = m_FileChooser.showSaveDialog(this);
		if (retVal != ConverterFileChooser.APPROVE_OPTION) {
			return;
		}

		panel.setChanged(false);
		setCurrentFilename(m_FileChooser.getSelectedFile().getAbsolutePath());
		// saveFile();

		AbstractFileSaver saver = m_FileChooser.getSaver();
		saver.setInstances(panel.getInstances());
		try {
			saver.writeBatch();
			panel.setChanged(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * closes the current tab
	 */
	public void closeFile() {
		closeFile(true);
	}

	/**
	 * closes the current tab
	 *
	 * @param showCancel whether to show an additional CANCEL button in the
	 *          "Want to save changes"-dialog
	 * @see #saveChanges(boolean)
	 */
	public void closeFile(boolean showCancel) {
		if (getCurrentIndex() == -1) {
			return;
		}

		if (!saveChanges(showCancel)) {
			return;
		}

		m_TabbedPane.removeTabAt(getCurrentIndex());
		updateFrameTitle();
		System.gc();
	}

	/**
	 * closes all open files
	 */
	public void closeAllFiles() {
		while (m_TabbedPane.getTabCount() > 0) {
			if (!saveChanges(true)) {
				return;
			}

			m_TabbedPane.removeTabAt(getCurrentIndex());
			updateFrameTitle();
			System.gc();
		}
	}

	/**
	 * displays some properties of the instances
	 */
	public void showProperties() {
		DataPanel panel;
		ListSelectorDialog dialog;
		Vector<String> props;
		Instances inst;

		panel = getCurrentPanel();
		if (panel == null) {
			return;
		}

		inst = panel.getInstances();
		if (inst == null) {
			return;
		}
		if (inst.classIndex() < 0) {
			inst.setClassIndex(inst.numAttributes() - 1);
		}

		// get some data
		props = new Vector<String>();
		props.add("Filename: " + panel.getFilename());
		props.add("Relation name: " + inst.relationName());
		props.add("# of instances: " + inst.numInstances());
		props.add("# of attributes: " + inst.numAttributes());
		props.add("Class attribute: " + inst.classAttribute().name());
		props.add("# of class labels: " + inst.numClasses());

		dialog = new ListSelectorDialog(getParentFrame(), new JList(props));
		dialog.showDialog();
	}

	/**
	 * closes the window, i.e., if the parent is not null and implements the
	 * WindowListener interface it calls the windowClosing method
	 */
	public void close() {
		if (getParentInternalFrame() != null) {
			getParentInternalFrame().doDefaultCloseAction();
		} else if (getParentFrame() != null) {
			((Window) getParentFrame()).dispatchEvent(new WindowEvent(
				getParentFrame(), WindowEvent.WINDOW_CLOSING));
		}
	}

	/**
	 * undoes the last action
	 */
	public void undo() {
		if (!isPanelSelected()) {
			return;
		}

		getCurrentPanel().undo();
	}

	/**
	 * copies the content of the selection to the clipboard
	 */
	public void copyContent() {
		if (!isPanelSelected()) {
			return;
		}

		getCurrentPanel().copyContent();
	}

	/**
	 * searches for a string in the cells
	 */
	public void search() {
		if (!isPanelSelected()) {
			return;
		}

		getCurrentPanel().search();
	}

	/**
	 * clears the search, i.e. resets the found cells
	 */
	public void clearSearch() {
		if (!isPanelSelected()) {
			return;
		}

		getCurrentPanel().clearSearch();
	}

	/**
	 * renames the current selected Attribute
	 */
	public void renameAttribute() {
		if (!isPanelSelected()) {
			return;
		}

		getCurrentPanel().renameAttribute();
	}

	/**
	 * sets the current selected Attribute as class attribute, i.e. it moves it to
	 * the end of the attributes
	 */
	public void attributeAsClass() {
		if (!isPanelSelected()) {
			return;
		}

		getCurrentPanel().attributeAsClass();
	}

	/**
	 * deletes the current selected Attribute or several chosen ones
	 *
	 * @param multiple whether to delete myultiple attributes
	 */
	public void deleteAttribute(boolean multiple) {
		if (!isPanelSelected()) {
			return;
		}

		if (multiple) {
			getCurrentPanel().deleteAttributes();
		} else {
			getCurrentPanel().deleteAttribute();
		}
	}

	/**
	 * deletes the current selected Instance or several chosen ones
	 *
	 * @param multiple whether to delete multiple instances
	 */
	public void deleteInstance(boolean multiple) {
		if (!isPanelSelected()) {
			return;
		}

		if (multiple) {
			getCurrentPanel().deleteInstances();
		} else {
			getCurrentPanel().deleteInstance();
		}
	}

	/**
	 * sorts the current selected attribute
	 */
	public void sortInstances() {
		if (!isPanelSelected()) {
			return;
		}

		getCurrentPanel().sortInstances();
	}

	/**
	 * displays all the attributes, returns the selected item or NULL if canceled
	 *
	 * @return the name of the selected attribute
	 */
	public String showAttributes() {
		DataSortedTableModel model;
		ListSelectorDialog dialog;
		int i;
		JList list;
		String name;
		int result;

		if (!isPanelSelected()) {
			return null;
		}

		list = new JList(getCurrentPanel().getAttributes());
		dialog = new ListSelectorDialog(getParentFrame(), list);
		result = dialog.showDialog();

		if (result == ListSelectorDialog.APPROVE_OPTION) {
			model = (DataSortedTableModel) getCurrentPanel().getTable().getModel();
			name = list.getSelectedValue().toString();
			i = model.getAttributeColumn(name);
			JTableHelper.scrollToVisible(getCurrentPanel().getTable(), 0, i);
			getCurrentPanel().getTable().setSelectedColumn(i);
			return name;
		} else {
			return null;
		}
	}

	/**
	 * displays all the distinct values for an attribute
	 */
	public void showValues() {
		String attribute;
		DataSortedTableModel model;
		DataTable table;
		HashSet<String> values;
		Vector<String> items;
		Iterator<String> iter;
		ListSelectorDialog dialog;
		int i;
		int col;

		// choose attribute to retrieve values for
		attribute = showAttributes();
		if (attribute == null) {
			return;
		}

		table = getCurrentPanel().getTable();
		model = (DataSortedTableModel) table.getModel();

		// get column index
		col = -1;
		for (i = 0; i < table.getColumnCount(); i++) {
			if (table.getPlainColumnName(i).equals(attribute)) {
				col = i;
				break;
			}
		}
		// not found?
		if (col == -1) {
			return;
		}

		// get values
		values = new HashSet<String>();
		items = new Vector<String>();
		for (i = 0; i < model.getRowCount(); i++) {
			values.add(model.getValueAt(i, col).toString());
		}
		if (values.isEmpty()) {
			return;
		}
		iter = values.iterator();
		while (iter.hasNext()) {
			items.add(iter.next());
		}
		Collections.sort(items);

		dialog = new ListSelectorDialog(getParentFrame(), new JList(items));
		dialog.showDialog();
	}

	/**
	 * sets the optimal column width for all columns
	 */
	public void setOptimalColWidths() {
		if (!isPanelSelected()) {
			return;
		}

		getCurrentPanel().setOptimalColWidths();
	}

	/**
	 * invoked when an action occurs
	 *
	 * @param e the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o;

		o = e.getSource();

		if (o == m_MenuFileOpen) {
			loadFile();
		} else if (o == m_MenuFileSave) {
			saveFile();
		} else if (o == m_MenuFileSaveAs) {
			saveFileAs();
		} else if (o == m_MenuFileClose) {
			closeFile();
		} else if (o == m_MenuFileCloseAll) {
			closeAllFiles();
		} else if (o == m_MenuFileProperties) {
			showProperties();
		} else if (o == m_MenuFileExit) {
			close();
		} else if (o == m_MenuEditUndo) {
			undo();
		} else if (o == m_MenuEditCopy) {
			copyContent();
		} else if (o == m_MenuEditSearch) {
			search();
		} else if (o == m_MenuEditClearSearch) {
			clearSearch();
		} else if (o == m_MenuEditDeleteAttribute) {
			deleteAttribute(false);
		} else if (o == m_MenuEditDeleteAttributes) {
			deleteAttribute(true);
		} else if (o == m_MenuEditRenameAttribute) {
			renameAttribute();
		} else if (o == m_MenuEditAttributeAsClass) {
			attributeAsClass();
		} else if (o == m_MenuEditDeleteInstance) {
			deleteInstance(false);
		} else if (o == m_MenuEditDeleteInstances) {
			deleteInstance(true);
		} else if (o == m_MenuEditSortInstances) {
			sortInstances();
		} else if (o == m_MenuViewAttributes) {
			showAttributes();
		} else if (o == m_MenuViewValues) {
			showValues();
		} else if (o == m_MenuViewOptimalColWidths) {
			setOptimalColWidths();
		}

		updateMenu();
	}

	/**
	 * Invoked when the target of the listener has changed its state.
	 *
	 * @param e the change event
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		updateFrameTitle();
		updateMenu();

		// did the content of panel change? -> change title of tab
		if (e.getSource() instanceof JComponent) {
			setTabTitle((JComponent) e.getSource());
		}
	}

	/**
	 * returns only the classname
	 *
	 * @return the classname
	 */
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
