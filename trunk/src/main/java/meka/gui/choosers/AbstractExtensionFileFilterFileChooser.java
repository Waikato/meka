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
 * AbstractExtensionFileFilterFileChooser.java
 * Copyright (C) 2010-2015 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.choosers;

import meka.core.FileUtils;
import weka.gui.ExtensionFileFilter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

/**
 * A specialized JFileChooser that lists all available file Readers and Writers
 * for reports.
 * <br><br>
 * Based on <code>weka.gui.ConverterFileChooser</code>
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10824 $
 * @see	    weka.gui.ConverterFileChooser
 * @param <F> the type of extension file filters to use
 */
public abstract class AbstractExtensionFileFilterFileChooser<F extends ExtensionFileFilter>
		extends MekaFileChooser {

	/** for serialization. */
	private static final long serialVersionUID = -1607568357690603421L;

	/** unhandled type of dialog. */
	public final static int UNHANDLED_DIALOG = -1;

	/** the type of dialog to display. */
	protected int m_DialogType;

	/** the converter that was chosen by the user. */
	protected Object m_CurrentHandler;

	/** the propertychangelistener. */
	protected PropertyChangeListener m_Listener;

	/** the last filter that was used for opening/saving. */
	protected ExtensionFileFilter m_LastFilter;

	/** whether the file to be opened must exist (only open dialog). */
	protected boolean m_FileMustExist;

	/** for comparing the filters. */
	protected ExtensionFileFilterComparator m_Comparator;

	/**
	 * onstructs a FileChooser pointing to the user's default directory.
	 */
	protected AbstractExtensionFileFilterFileChooser() {
		super();
	}

	/**
	 * Constructs a FileChooser using the given File as the path.
	 *
	 * @param currentDirectory	the path to start in
	 */
	protected AbstractExtensionFileFilterFileChooser(File currentDirectory) {
		super(currentDirectory);
	}

	/**
	 * Constructs a FileChooser using the given path.
	 *
	 * @param currentDirectory	the path to start in
	 */
	protected AbstractExtensionFileFilterFileChooser(String currentDirectory) {
		super(currentDirectory);
	}

	/**
	 * Further initializations.
	 */
	@Override
	protected void initialize() {
		super.initialize();

		m_DialogType = UNHANDLED_DIALOG;
		m_Comparator = new ExtensionFileFilterComparator();

		super.setAcceptAllFileFilterUsed(false);
		setFileMustExist(true);

		initializeFilters(this);
	}

	/**
	 * Returns whether the filters have already been initialized.
	 *
	 * @return		true if the filters have been initialized
	 */
	protected abstract boolean getFiltersInitialized();

	/**
	 * Performs the actual initialization of the filters.
	 */
	protected abstract void doInitializeFilters();

	/**
	 * Performs the initialization of the file filters, if necessary.
	 *
	 * @param chooser	the chooser that is being initialized currently
	 * @see		#getFiltersInitialized()
	 */
	protected static synchronized void initializeFilters(AbstractExtensionFileFilterFileChooser chooser) {
		if (!chooser.getFiltersInitialized())
			chooser.doInitializeFilters();
	}

	/**
	 * Whether the selected file must exst (only open dialog).
	 *
	 * @param value	if true the file must exist
	 */
	public void setFileMustExist(boolean value) {
		m_FileMustExist = value;
	}

	/**
	 * Returns whether the selected file must exist (only open dialog).
	 *
	 * @return		true if the file must exist
	 */
	public boolean getFileMustExist() {
		return m_FileMustExist;
	}

	/**
	 * Returns the file filters for opening files.
	 *
	 * @return		the file filters
	 */
	protected abstract List<F> getOpenFileFilters();

	/**
	 * Returns the file filters for writing files.
	 *
	 * @return		the file filters
	 */
	protected abstract List<F> getSaveFileFilters();

	/**
	 * Returns the default file filter to use.
	 *
	 * @param dialogType	the dialog type: open/save
	 * @return		the default file filter, null if unable find default one
	 */
	protected F getDefaultFileFilter(int dialogType) {
		List<F> list;

		if (dialogType == OPEN_DIALOG)
			list = getOpenFileFilters();
		else
			list = getSaveFileFilters();

		if (list.size() > 0)
			return list.get(0);
		else
			return null;
	}

	/**
	 * Attempts to restore the last filter in use.
	 *
	 * @param dialogType	the dialog type: open/save
	 * @return		the filter that was selected, null if none
	 */
	protected F restoreLastFilter(int dialogType) {
		F		result;
		List<F> list;
		int		i;

		result = null;

		if (dialogType == OPEN_DIALOG)
			list = getOpenFileFilters();
		else
			list = getSaveFileFilters();
		if (list.size() > 0) {
			result = getDefaultFileFilter(dialogType);
			setFileFilter(result);
			if (m_LastFilter != null) {
				for (i = 0; i < list.size(); i++) {
					if (m_Comparator.compare(list.get(i), m_LastFilter) == 0) {
						result = (F) m_LastFilter;
						setFileFilter(m_LastFilter);
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * initializes the GUI.
	 *
	 * @param dialogType		the type of dialog to setup the GUI for
	 */
	protected void initGUI(int dialogType) {
		List<F> list;
		int		i;

		m_CurrentHandler = null;

		// setup filters
		resetChoosableFileFilters();
		if (dialogType == OPEN_DIALOG)
			list = getOpenFileFilters();
		else
			list = getSaveFileFilters();
		for (i = 0; i < list.size(); i++) {
			addChoosableFileFilter(list.get(i));
		}
		restoreLastFilter(dialogType);

		// listener
		if (m_Listener != null)
			removePropertyChangeListener(m_Listener);
		m_Listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				// filter changed
				if (evt.getPropertyName().equals(FILE_FILTER_CHANGED_PROPERTY)) {
					if (getFileFilter() != null)
						updateCurrentHandlerHook();
				}
			}
		};
		addPropertyChangeListener(m_Listener);

		if (getFileFilter() != null)
			updateCurrentHandlerHook();
	}

	/**
	 * Pops a custom file chooser dialog with a custom approve button. Throws
	 * an exception, if the dialog type is UNHANDLED_DIALOG.
	 *
	 * @param parent		the parent of this dialog
	 * @param approveButtonText	the text for the OK button
	 * @return			the user's action
	 */
	@Override
	public int showDialog(Component parent, String approveButtonText) {
		if (m_DialogType == UNHANDLED_DIALOG)
			throw new IllegalStateException("Either use showOpenDialog or showSaveDialog!");
		else
			return super.showDialog(parent, approveButtonText);
	}

	/**
	 * Returns the current dialog type.
	 *
	 * @return		the type
	 * @see		#OPEN_DIALOG
	 * @see		#SAVE_DIALOG
	 * @see		#UNHANDLED_DIALOG
	 */
	@Override
	public int getDialogType() {
		return m_DialogType;
	}

	/**
	 * Pops up an "Open File" file chooser dialog.
	 *
	 * @param parent		the parent of this file chooser
	 * @return			the result of the user's action
	 */
	@Override
	public int showOpenDialog(Component parent) {
		File	    selFile;
		int 		result;

		m_DialogType = OPEN_DIALOG;

		initGUI(OPEN_DIALOG);
		result = super.showOpenDialog(parent);

		m_DialogType = UNHANDLED_DIALOG;
		removePropertyChangeListener(m_Listener);

		// does file exist?
		selFile = getSelectedFile();
		if (    (result == APPROVE_OPTION)
				&& (getFileMustExist())
				&& (selFile == null)) {
			result = showOpenDialog(parent);
		}
		else if (    (result == APPROVE_OPTION)
				&& (getFileMustExist())
				&& (selFile != null)
				&& (selFile.isFile())
				&& (!selFile.exists()) ) {
			int retVal = JOptionPane.showConfirmDialog(
					parent,
					"The file '"
							+ selFile
							+ "' does not exist - please select again!");
			if (retVal == JOptionPane.OK_OPTION)
				result = showOpenDialog(parent);
			else
				result = CANCEL_OPTION;
		}

		if (result == APPROVE_OPTION) {
			if (getFileFilter() instanceof ExtensionFileFilter)
				m_LastFilter = (ExtensionFileFilter) getFileFilter();
			else
				m_LastFilter = null;
			if (getSelectedFile() != null)
				configureCurrentHandlerHook(OPEN_DIALOG);
		}

		return result;
	}

	/**
	 * Pops up an "Save File" file chooser dialog.
	 *
	 * @param parent		the parent of this file chooser
	 * @return			the result of the user's action
	 */
	@Override
	public int showSaveDialog(Component parent) {
		int 	    result;
		FileFilter  currentFilter;
		File        currentFile;

		m_DialogType = SAVE_DIALOG;

		initGUI(SAVE_DIALOG);

		// using "setAcceptAllFileFilterUsed" messes up the currently selected
		// file filter/file, hence backup/restore of currently selected
		// file filter/file
		currentFilter = getFileFilter();
		currentFile   = getSelectedFile();
		setAcceptAllFileFilterUsed(false);
		setFileFilter(currentFilter);
		setSelectedFile(currentFile);

		result = super.showSaveDialog(parent);

		// using "setAcceptAllFileFilterUsed" messes up the currently selected
		// file filter/file, hence backup/restore of currently selected
		// file filter/file
		currentFilter = getFileFilter();
		currentFile   = getSelectedFile();
		setFileFilter(currentFilter);
		setSelectedFile(currentFile);

		m_DialogType = UNHANDLED_DIALOG;
		removePropertyChangeListener(m_Listener);

		if (result == APPROVE_OPTION) {
			if (getFileFilter() instanceof ExtensionFileFilter)
				m_LastFilter = (ExtensionFileFilter) getFileFilter();
			else
				m_LastFilter = null;
			if (getSelectedFile() != null)
				configureCurrentHandlerHook(SAVE_DIALOG);
		}

		return result;
	}

	/**
	 * Sets the current handler according to the current filefilter.
	 * <br><br>
	 * Default implementation does nothing.
	 *
	 * @see 		#m_CurrentHandler
	 */
	protected void updateCurrentHandlerHook() {
	}

	/**
	 * Configures the current handler.
	 * <br><br>
	 * Default implementation does nothing.
	 *
	 * @param dialogType	the type of dialog to configure for
	 * @see 		#m_CurrentHandler
	 */
	protected void configureCurrentHandlerHook(int dialogType) {
	}

	/**
	 * Is always using false, since we can't determine the reader/writer based
	 * on the extension.
	 *
	 * @param b		ignored
	 */
	@Override
	public void setAcceptAllFileFilterUsed(boolean b) {
		super.setAcceptAllFileFilterUsed(false);
	}

	/**
	 * Attempts to set the correct file filter for the specified file, using its
	 * extension to determine the file filter.
	 *
	 * @param file	the file to set the filter for
	 * @return		true if successfully set filter
	 */
	public boolean setCorrectOpenFileFilter(File file) {
		boolean	result;
		String fileExt;

		result = false;

		fileExt = FileUtils.getExtension(file);
		for (F filter: getOpenFileFilters()) {
			for (String ext: filter.getExtensions()) {
				if (ext.equals(fileExt)) {
					setFileFilter(filter);
					m_LastFilter = filter;
					result       = true;
					updateCurrentHandlerHook();
					break;
				}
			}
			if (result)
				break;
		}

		return result;
	}

	/**
	 * Attempts to set the correct file filter for the specified file, using its
	 * extension to determine the file filter.
	 *
	 * @param file	the file to set the filter for
	 * @return		true if successfully set filter
	 */
	public boolean setCorrectSaveFileFilter(File file) {
		boolean	result;
		String fileExt;

		result = false;

		fileExt = FileUtils.getExtension(file);
		for (F filter: getSaveFileFilters()) {
			for (String ext: filter.getExtensions()) {
				if (ext.equals(fileExt)) {
					setFileFilter(filter);
					m_LastFilter = filter;
					result       = true;
					updateCurrentHandlerHook();
					break;
				}
			}
			if (result)
				break;
		}

		return result;
	}

	/**
	 * Checks whether the filter uses "*.*".
	 *
	 * @param filter	the filter to check
	 * @return		true if filter has extension .*
	 */
	public static boolean isAllFilter(ExtensionFileFilter filter) {
		for (String ext: filter.getExtensions()) {
			if (ext.endsWith("*") || ext.endsWith(".*"))
				return true;
		}
		return false;
	}
}
