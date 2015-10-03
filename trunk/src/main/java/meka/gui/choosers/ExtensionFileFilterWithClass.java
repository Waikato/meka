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
 * ExtensionFileFilterWithClass.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.choosers;

import weka.gui.ExtensionFileFilter;

/**
 * A custom filter class that stores the associated class along the
 * description and extensions.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 7569 $
 */
public class ExtensionFileFilterWithClass
  extends ExtensionFileFilter {

  /** for serialization. */
  private static final long serialVersionUID = 5863117558505811134L;

  /** the classname. */
  protected String m_Classname;

  /**
   * Constructs a filter that matches files with the given extension, not
   * case-sensitive.
   *
   * @param extension		the extensions of the files (no dot!)
   * @param description	    the display string
   * @param classname		the classname this filter is for
   */
  public ExtensionFileFilterWithClass(String extension, String description, String classname) {
    super(extension, description);

    m_Classname = classname;
  }

  /**
   * Constructs a filter that matches files with the given extension, not
   * case-sensitive.
   *
   * @param classname		the classname this filter is for
   * @param extensions	    the extensions of the files (no dot!)
   * @param description	    the display string
   */
  public ExtensionFileFilterWithClass(String[] extensions, String description, String classname) {
    super(extensions, description);

    m_Classname = classname;
  }

  /**
   * Returns the associated classname.
   *
   * @return		the classname
   */
  public String getClassname() {
    return m_Classname;
  }
}