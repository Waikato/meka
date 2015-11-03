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
 * FileChooserBookmarksPanel.java
 * Copyright (C) 2014-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import com.googlecode.jfilechooserbookmarks.AbstractBookmarksPanel;
import com.googlecode.jfilechooserbookmarks.AbstractFactory;
import com.googlecode.jfilechooserbookmarks.AbstractPropertiesHandler;
import com.googlecode.jfilechooserbookmarks.DefaultFactory;
import meka.core.Project;

import javax.swing.JFileChooser;

/**
 * Panel for bookmarking directories in a {@link JFileChooser}.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 9380 $
 */
public class FileChooserBookmarksPanel
  extends AbstractBookmarksPanel {

  /** for serialization. */
  private static final long serialVersionUID = -1969362821325599909L;
  
  /** the properties to store the bookmarks in. */
  public final static String FILENAME = "FileChooserBookmarks.props";

  /**
   * The MEKA-specific properties handler.
   * 
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 9380 $
   */
  public static class FileChooserBookmarksPropertiesHandler
    extends AbstractPropertiesHandler {

    /** for serialization. */
    private static final long serialVersionUID = 396276849965032378L;

    /**
     * The properties file in the $HOME/.meka directory.
     * 
     * @return		the filename
     */
    @Override
    protected String getFilename() {
      return Project.expandFile(FILENAME).getAbsolutePath();
    }
  }
  
  /**
   * MEKA-specific factory.
   * 
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 9380 $
   */
  public static class FileChooserBookmarksFactory
    extends DefaultFactory {
    
    /** for serialization. */
    private static final long serialVersionUID = -8327179027505887784L;

    /**
     * Returns a new instance of the properties handler to be used.
     * 
     * @return		the handler instance
     */
    @Override
    public AbstractPropertiesHandler newPropertiesHandler() {
      return new FileChooserBookmarksPropertiesHandler();
    }
  }

  /**
   * Creates a new instance of the factory.
   * 
   * @return		the factory
   */
  @Override
  protected AbstractFactory newFactory() {
    return new FileChooserBookmarksFactory();
  }
}
