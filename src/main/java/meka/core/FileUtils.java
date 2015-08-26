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
 * FileUtils.java
 * Copyright (C) 2009-2015 University of Waikato, Hamilton, New Zealand
 */

package meka.core;

import java.io.File;

/**
 * Utility class for I/O related actions.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class FileUtils {


  /**
   * Returns the number of directories that this file object contains.
   * E.g.: /home/blah/some/where.txt will return 3. /blah.txt returns 0.
   * 
   * @param file		the file
   */
  public static int getDirectoryDepth(File file) {
    int		result;

    result = 0;
    
    if (!file.isDirectory())
      file = file.getParentFile();
    
    while (file.getParentFile() != null) {
      result++;
      file = file.getParentFile();
    }
    
    return result;
  }
  
  /**
   * Creates a partial filename for the given file, based on how many parent
   * directories should be included. Examples:
   * <pre>
   * createPartialFilename(new File("/home/some/where/file.txt"), -1)
   *   = /home/some/where/file.txt
   * createPartialFilename(new File("/home/some/where/file.txt"), 0)
   *   = file.txt
   * createPartialFilename(new File("/home/some/where/file.txt"), 1)
   *   = where/file.txt
   * createPartialFilename(new File("/home/some/where/file.txt"), 2)
   *   = some/where/file.txt
   * </pre>
   *
   * @param file		the file to create the partial filename for
   * @param numParentDirs	the number of parent directories to include in
   * 				the partial name, -1 returns the absolute
   * 				filename
   * @return			the generated filename
   */
  public static String createPartialFilename(File file, int numParentDirs) {
    String	result;
    File	parent;
    int		i;

    if (numParentDirs == -1) {
      result = file.getAbsolutePath();
    }
    else {
      result = file.getName();
      parent = file;
      for (i = 0; (i < numParentDirs) && (parent.getParentFile() != null); i++) {
        parent = parent.getParentFile();
        result = parent.getName() + File.separator + result;
      }
    }

    return result;
  }
}
