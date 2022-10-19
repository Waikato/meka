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
 * Copyright (C) 2022 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import com.googlecode.jfilechooserbookmarks.Communication;

import javax.swing.BorderFactory;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * A more intuitive dialog for selecting a directory than JFileChooser.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class DirectoryChooser
    extends nz.ac.waikato.cms.adams.simpledirectorychooser.SimpleDirectoryChooser {

  public static class SimpleDirectoryChooserCommunication
      implements Communication {

    /** the chooser. */
    protected DirectoryChooser m_Chooser;

    /**
     * Initializes the communication.
     *
     * @param chooser	the underlying chooser
     */
    public SimpleDirectoryChooserCommunication(DirectoryChooser chooser) {
      m_Chooser = chooser;
    }

    /**
     * Sets the current directory.
     *
     * @param dir the directory to use
     */
    @Override
    public void setCurrentDirectory(File dir) {
      m_Chooser.setCurrentDirectory(dir);
    }

    /**
     * Returns the current directory.
     *
     * @return the directory in use
     */
    @Override
    public File getCurrentDirectory() {
      return m_Chooser.getCurrentDirectory();
    }

    /**
     * Returns all the selected files.
     *
     * @return the currently selected files
     */
    @Override
    public File[] getSelectedFiles() {
      return m_Chooser.getSelectedFiles();
    }

    /**
     * Scrolls the specified file into view
     *
     * @param f the file to scroll into view
     */
    @Override
    public void ensureFileIsVisible(File f) {
      m_Chooser.ensureFileIsVisible(f);
    }

    /**
     * Adds the property change listener.
     *
     * @param l the listener to add
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
      m_Chooser.addPropertyChangeListener(l);
    }

    /**
     * Removes the property change listener.
     *
     * @param l the listener to remove
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
      m_Chooser.removePropertyChangeListener(l);
    }
  }

  /** the panel with the bookmarks. */
  protected FileChooserBookmarksPanel m_PanelBookmarks;

  /**
   * Constructs a <code>SimpleDirectoryChooser</code> pointing to the user's
   * default directory. This default depends on the operating system.
   * It is typically the "My Documents" folder on Windows, and the
   * user's home directory on Unix.
   */
  public DirectoryChooser() {
    super();
  }

  /**
   * Constructs a <code>SimpleDirectoryChooser</code> using the given path.
   * Passing in a <code>null</code>
   * string causes the file chooser to point to the user's default directory.
   * This default depends on the operating system. It is
   * typically the "My Documents" folder on Windows, and the user's
   * home directory on Unix.
   *
   * @param currentDirectoryPath  a <code>String</code> giving the path
   *				to a file or directory
   */
  public DirectoryChooser(String currentDirectoryPath) {
    super(currentDirectoryPath);
  }

  /**
   * Constructs a <code>SimpleDirectoryChooser</code> using the given <code>File</code>
   * as the path. Passing in a <code>null</code> file
   * causes the file chooser to point to the user's default directory.
   * This default depends on the operating system. It is
   * typically the "My Documents" folder on Windows, and the user's
   * home directory on Unix.
   *
   * @param currentDirectory  a <code>File</code> object specifying
   *				the path to a file or directory
   */
  public DirectoryChooser(File currentDirectory) {
    super(currentDirectory);
  }

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initWidgets() {
    super.initWidgets();

    m_PanelBookmarks = new FileChooserBookmarksPanel();
    m_PanelBookmarks.setOwner(new SimpleDirectoryChooserCommunication(this));
    m_PanelBookmarks.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
    setAccessory(m_PanelBookmarks);
    setPreferredSize(new Dimension(750, 500));
  }
}
