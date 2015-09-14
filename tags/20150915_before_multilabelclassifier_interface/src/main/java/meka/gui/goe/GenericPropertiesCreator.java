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
 * GenericPropertiesCreator.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.goe;

import java.util.Enumeration;
import java.util.Properties;

import meka.core.PropsUtils;
import weka.core.Utils;

/**
 * Custom GOE props creator, to include the MEKA classes.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class GenericPropertiesCreator
  extends weka.gui.GenericPropertiesCreator {
  
  /** The name of the properties file to use as a template. */
  protected static String MEKA_CREATOR_FILE = "meka/gui/goe/MekaPropertiesCreator.props";

  /** whether to output some debugging information. */
  public static boolean DEBUG = "true".equals(System.getenv("MEKA_DEBUG"));
  
  static {
    if (DEBUG)
      PropsUtils.DEBUG = true;
  }

  /**
   * initializes the creator, locates the props file with the Utils class.
   * 
   * @throws Exception if loading of CREATOR_FILE fails
   * @see #MEKA_CREATOR_FILE
   */
  public GenericPropertiesCreator() throws Exception {
    super(MEKA_CREATOR_FILE);
    m_ExplicitPropsFile = false;
    if (DEBUG)
      System.out.println("start<GenericPropertiesCreator/find: " + MEKA_CREATOR_FILE + ">\n" + Utils.arrayToString(PropsUtils.find(MEKA_CREATOR_FILE)) + "\nend<GenericPropertiesCreator/find: + " + MEKA_CREATOR_FILE + ">\n");
  }
  
  /**
   * For testing only.
   * 
   * @param args	ignored
   */
  public static void main(String[] args) throws Exception {
    GenericPropertiesCreator creator = new GenericPropertiesCreator();
    creator.execute(false);
    Properties props = creator.getOutputProperties();
    Enumeration<String> keys = (Enumeration<String>) props.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      System.out.println("\n--> " + key);
      System.out.println(props.getProperty(key));
    }
  }
}
