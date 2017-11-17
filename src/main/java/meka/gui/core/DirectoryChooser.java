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
 * DirectoryChooser.java
 * Copyright (C) 2010-2017 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import com.jidesoft.swing.FolderChooser;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;

/**
 * Extended version of the com.jidesoft.swing.FolderChooser to
 * handle PlaceholderFile objects.
 *
 * @author FracPete (fracpete at waikat dot ac dot nz)
 */
public class DirectoryChooser
	extends FolderChooser {

	/** for serialization. */
	private static final long serialVersionUID = -7252242971482953986L;

	/** the bookmarks. */
	protected FileChooserBookmarksPanel m_PanelBookmarks;

	/** the panel for showing/hiding the bookmarks. */
	protected OneTouchPanel m_OneTouchPanel;

	/**
	 * Creates a BaseDirectoryChooser pointing to the user's home directory.
	 */
	public DirectoryChooser() {
		super();
		initialize();
	}

	/**
	 * Creates a BaseDirectoryChooser using the given File as the path.
	 *
	 * @param currentDirectory the directory to start in
	 */
	public DirectoryChooser(File currentDirectory) {
		super(currentDirectory.getAbsoluteFile());
		initialize();
	}

	/**
	 * Creates a BaseDirectoryChooser using the given current directory and
	 * FileSystemView.
	 *
	 * @param currentDirectory the directory to start in
	 * @param fsv              the view to use
	 */
	public DirectoryChooser(File currentDirectory, FileSystemView fsv) {
		super(currentDirectory.getAbsoluteFile(), fsv);
		initialize();
	}

	/**
	 * Creates a BaseDirectoryChooser using the given FileSystemView.
	 *
	 * @param fsv the view to use
	 */
	public DirectoryChooser(FileSystemView fsv) {
		super(fsv);
		initialize();
	}

	/**
	 * Creates a BaseDirectoryChooser using the given path.
	 *
	 * @param currentDirectoryPath the directory to start in
	 */
	public DirectoryChooser(String currentDirectoryPath) {
		super(currentDirectoryPath);
		initialize();
	}

	/**
	 * Creates a BaseDirectoryChooser using the given path and FileSystemView.
	 *
	 * @param currentDirectoryPath the directory to start in
	 * @param fsv                  the view to use
	 */
	public DirectoryChooser(String currentDirectoryPath, FileSystemView fsv) {
		super(currentDirectoryPath, fsv);
		initialize();
	}

	/**
	 * For initializing some stuff.
	 * <br><br>
	 * Default implementation does nothing.
	 */
	protected void initialize() {
		JComponent accessory;

		setRecentListVisible(false);
		setNavigationFieldVisible(true);

		accessory = createAccessoryPanel();
		if (accessory != null)
			setAccessory(accessory);

		showBookmarks(false);

		setPreferredSize(new Dimension(400, 500));
	}

	/**
	 * Creates an accessory panel displayed next to the files.
	 *
	 * @return the panel or null if none available
	 */
	protected JComponent createAccessoryPanel() {
		m_PanelBookmarks = new FileChooserBookmarksPanel();
		m_PanelBookmarks.setOwner(this);

		m_OneTouchPanel = new OneTouchPanel(OneTouchPanel.Location.TOP);
		m_OneTouchPanel.getContentPanel().add(m_PanelBookmarks, BorderLayout.CENTER);
		m_OneTouchPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		m_OneTouchPanel.setToolTipVisible("Click to hide bookmarks");
		m_OneTouchPanel.setToolTipHidden("Click to show bookmarks");

		return m_OneTouchPanel;
	}

	/**
	 * Either displays or hides the bookmarks.
	 *
	 * @param value true if to show bookmarks
	 */
	protected void showBookmarks(boolean value) {
		m_OneTouchPanel.setContentVisible(value);
	}

	/**
	 * Does nothing.
	 *
	 * @param filter ignored
	 */
	@Override
	public void addChoosableFileFilter(FileFilter filter) {
	}

	/**
	 * Sets the selected file. If the file's parent directory is
	 * not the current directory, changes the current directory
	 * to be the file's parent directory.
	 *
	 * @param file the selected file
	 * @beaninfo preferred: true
	 * bound: true
	 * @see #getSelectedFile
	 */
	@Override
	public void setSelectedFile(File file) {
		File selFile;

		selFile = null;

		if (file != null)
			selFile = new File(file.getAbsolutePath());

		super.setSelectedFile(selFile);
	}

	/**
	 * Displays the dialog.
	 *
	 * @param parent            the parent component of the dialog;
	 *                          can be <code>null</code>
	 * @param approveButtonText the text of the <code>ApproveButton</code>
	 * @return the return state of the file chooser on popdown
	 * @throws HeadlessException if GraphicsEnvironment.isHeadless()
	 *                           returns true.
	 * @see java.awt.GraphicsEnvironment#isHeadless
	 */
	@Override
	public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
		m_PanelBookmarks.reload();
		return super.showDialog(parent, approveButtonText);
	}

	/**
	 * For testing only.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setCurrentDirectory(new File(System.getProperty("java.io.tmpdir")));
		if (chooser.showOpenDialog(null) == DirectoryChooser.APPROVE_OPTION)
			System.out.println(chooser.getSelectedFile());
	}
}
