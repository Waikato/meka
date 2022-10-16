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

import nz.ac.waikato.cms.gui.core.BaseFileChooser;

import java.io.File;

/**
 * Simple directory chooser based on JFileChooser.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class DirectoryChooser
    extends BaseFileChooser {

  /**
   * Constructs a <code>BaseFileChooser</code> pointing to the user's
   * default directory. This default depends on the operating system.
   * It is typically the "My Documents" folder on Windows, and the
   * user's home directory on Unix.
   */
  public DirectoryChooser() {
    super();
  }

  /**
   * Constructs a <code>BaseFileChooser</code> using the given path.
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
    super(new File(currentDirectoryPath).getAbsolutePath());
  }

  /**
   * Constructs a <code>BaseFileChooser</code> using the given <code>File</code>
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
   * For initializing some stuff.
   */
  @Override
  protected void initialize() {
    super.initialize();

    super.setFileSelectionMode(BaseFileChooser.DIRECTORIES_ONLY);
  }

  /**
   * Ignored.
   */
  public void setFileSelectionMode(int model) {
    // ignored
  }
}
