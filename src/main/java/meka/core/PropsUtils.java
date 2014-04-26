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
 * PropsUtils.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package meka.core;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import weka.core.Utils;

/**
 * Utility class for props files.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PropsUtils {
  
  /** whether to output some debug information. */
  public static boolean DEBUG = false;

  /**
   * Reads properties that inherit from three locations. Properties are first
   * defined in the system resource location (i.e. in the CLASSPATH). These
   * default properties must exist. Properties optionally defined in the user
   * properties location (WekaPackageManager.PROPERTIES_DIR) override default
   * settings. Properties defined in the current directory (optional) override
   * all these settings.
   * 
   * @param props the location of the props file that should be loaded.
   *          e.g.: "weka/core/Utils.props".
   * @return the Properties
   * @throws Exception if an error occurs reading the properties files.
   * @see Utils#readProperties(String)
   */
  public static Properties read(String props) throws Exception {
    Properties	result;
    
    result = Utils.readProperties(props);

    if (DEBUG)
      System.out.println("start<PropsUtils.read: " + props + ">\n" + toString(result, null) + "end<PropsUtils.read: " + props + ">\n");
    
    return result;
  }
  
  /**
   * Locates the properties file in the current classpath.
   * 
   * @param props	the props file to locate
   * @return		the URLs where the props file was found
   */
  public static URL[] find(String props) {
    List<URL>		result;
    Enumeration<URL>	urls;
    String		propsName;
    File		propsFile;
    URL			url;

    if (DEBUG)
      System.out.println("start<PropsUtils.find: " + props + ">");

    result = new ArrayList<URL>();

    propsName = new File(props).getName();
    if (DEBUG)
      System.out.println("- propsName: " + propsName);
    
    try {
      if (DEBUG)
	System.out.println("1. system resources: ");
      urls = ClassLoader.getSystemResources(props);
      while (urls.hasMoreElements()) {
	url = urls.nextElement();
	if (DEBUG)
	  System.out.println("- " + url);
	result.add(url);
      }
    }
    catch (Exception e) {
      System.err.println("Failed to obtain systems resources (URLs) for: " + props);
    }
    
    // home directory
    if (DEBUG)
      System.out.println("2. home dir: " + System.getProperty("user.home"));
    propsFile = new File(System.getProperty("user.home") + File.separator + propsName);
    if (DEBUG) {
      System.out.println("- propsFile: " + propsFile);
      System.out.println("- propsFile exists: " + propsFile.exists());
    }
    if (propsFile.exists()) {
      try {
	result.add(propsFile.toURL());
      }
      catch (Exception e) {
	System.err.println("Failed to turn '" + propsFile + "' into URL:");
	e.printStackTrace();
      }
    }
    
    // current directory
    if (DEBUG)
      System.out.println("3. current dir: " + System.getProperty("user.dir"));
    propsFile = new File(System.getProperty("user.dir") + File.separator + propsName);
    if (DEBUG) {
      System.out.println("- propsFile: " + propsFile);
      System.out.println("- propsFile exists: " + propsFile.exists());
    }
    if (propsFile.exists()) {
      try {
	result.add(propsFile.toURL());
      }
      catch (Exception e) {
	System.err.println("Failed to turn '" + propsFile + "' into URL:");
	e.printStackTrace();
      }
    }

    if (DEBUG)
      System.out.println("end<PropsUtils.find: " + props + ">");

    return result.toArray(new URL[result.size()]);
  }

  /**
   * Collapses all the inherited and current properties into a single Properties
   * object and returns it.
   *
   * @param props	the properties to collapse
   * @return		the collapsed version of this Properties object
   */
  public static Properties collapse(Properties props) {
    Properties		result;
    Enumeration<String>	keys;
    String		key;

    result = new Properties();
    keys   = (Enumeration<String>) props.propertyNames();
    while (keys.hasMoreElements()) {
      key = keys.nextElement();
      result.setProperty(key, props.getProperty(key));
    }

    return result;
  }

  /**
   * Outputs the properties as they would be written to a file.
   *
   * @param props	the properties to turn into a string
   * @param comment	the comment to output
   * @return		the generated output or null in case of an error
   */
  public static String toString(Properties props, String comment) {
    String		result;
    StringWriter	writer;

    result = null;

    try {
      writer = new StringWriter();
      collapse(props).store(writer, comment);
      writer.flush();
      writer.close();
      result = writer.toString();
    }
    catch (Exception e) {
      result = null;
      System.err.println("Failed to turn props into string: " + props);
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Prints the usage of this class from the commandline to stdout.
   */
  protected static void printUsage() {
    System.out.println("Usage: " + PropsUtils.class.getName() + " <read|find> <props>");
    System.out.println("Use uppercase of read/find to enable debug output");
    System.out.println();
    System.out.println("Examples:");
    System.out.println("- read");
    System.out.println("  " + PropsUtils.class.getName() + " read meka/gui/goe/MekaEditors.props");
    System.out.println("- find");
    System.out.println("  " + PropsUtils.class.getName() + " find meka/gui/goe/MekaEditors.props");
    System.out.println();
  }
  
  /**
   * Allows some basic operations on properties files:
   * <ul>
   *   <li>read &lt;props&gt;- reads the specified props file and outputs it, 
   *   e.g., "read meka/gui/goe/MekaEditors.props"
   *   <li>find &lt;props&gt;- finds all occurrences of the specified props 
   *   file and outputs them, e.g., "find meka/gui/goe/MekaEditors.props"
   * </ul>
   */
  public static void main(String[] args) throws Exception {
    if (args.length == 2) {
      if (args[0].toLowerCase().equals("read")) {
	if (args[0].equals("READ"))
	  DEBUG = true;
	Properties props = read(args[1]);
	System.out.println(toString(props, null));
      }
      else if (args[0].toLowerCase().equals("find")) {
	if (args[0].equals("FIND"))
	  DEBUG = true;
	URL[] urls = find(args[1]);
	for (URL url: urls)
	  System.out.println(url);
      }
      else {
	printUsage();
      }
    }
    else {
      printUsage();
    }
  }
}
