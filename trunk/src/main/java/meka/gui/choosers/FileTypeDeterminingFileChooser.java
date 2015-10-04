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
 * FileTypeDeterminingFileChooser.java
 * Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.choosers;

import java.io.File;

/**
 * Interface for file choosers that support the determination of readers
 * and writers based on filenames (namely extensions).
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4584 $
 * @param <R> the type of reader to use
 * @param <W> the type of writer to use
 */
public interface FileTypeDeterminingFileChooser<R, W> {

  /**
   * Returns the reader for the specified file.
   *
   * @param file	the file to determine a reader for
   * @return		the reader, null if none found
   */
  public R getReaderForFile(File file);

  /**
   * Returns the writer for the specified file.
   *
   * @param file	the file to determine a reader for
   * @return		the writer, null if none found
   */
  public W getWriterForFile(File file);
}
