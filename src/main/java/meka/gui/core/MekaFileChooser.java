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
 * MekaFileChooser.java
 * Copyright (C) 2014-2015 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import javax.swing.*;
import java.io.File;

/**
 * A file chooser dialog with directory bookmarks.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 8361 $
 */
public class MekaFileChooser
		extends JFileChooser {

	/** for serialization. */
	private static final long serialVersionUID = -5712455182900852653L;

	/** the bookmarks. */
	protected FileChooserBookmarksPanel m_PanelBookmarks;

	/**
	 * Constructs a <code>MekaFileChooser</code> pointing to the user's
	 * default directory. This default depends on the operating system.
	 * It is typically the "My Documents" folder on Windows, and the
	 * user's home directory on Unix.
	 */
	public MekaFileChooser() {
		super();

		initialize();
	}

	/**
	 * Constructs a <code>MekaFileChooser</code> using the given path.
	 * Passing in a <code>null</code>
	 * string causes the file chooser to point to the user's default directory.
	 * This default depends on the operating system. It is
	 * typically the "My Documents" folder on Windows, and the user's
	 * home directory on Unix.
	 *
	 * @param currentDirectoryPath  a <code>String</code> giving the path
	 *				to a file or directory
	 */
	public MekaFileChooser(String currentDirectoryPath) {
		super(currentDirectoryPath);

		initialize();
	}

	/**
	 * Constructs a <code>MekaFileChooser</code> using the given <code>File</code>
	 * as the path. Passing in a <code>null</code> file
	 * causes the file chooser to point to the user's default directory.
	 * This default depends on the operating system. It is
	 * typically the "My Documents" folder on Windows, and the user's
	 * home directory on Unix.
	 *
	 * @param currentDirectory  a <code>File</code> object specifying
	 *				the path to a file or directory
	 */
	public MekaFileChooser(File currentDirectory) {
		super(currentDirectory);

		initialize();
	}

	/**
	 * For initializing some stuff.
	 */
	protected void initialize() {
		JComponent		accessory;

		accessory = createAccessoryPanel();
		if (accessory != null)
			setAccessory(accessory);
		setPreferredSize(GUIHelper.getDefaultDimensions("FileChooser", 750, 500));
	}

	/**
	 * Creates an accessory panel displayed next to the files.
	 *
	 * @return		the panel or null if none available
	 */
	protected JComponent createAccessoryPanel() {
		m_PanelBookmarks = new FileChooserBookmarksPanel();
		m_PanelBookmarks.setOwner(this);
		m_PanelBookmarks.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
		return m_PanelBookmarks;
	}
}
