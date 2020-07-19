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
 * Copyright (C) 2014-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.core;

import weka.core.EnvironmentProperties;
import weka.core.Utils;
import weka.core.WekaPackageManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

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
	 * settings. props files in MEKA_HOME ({@link Project#getHome()}) override these.
	 * Properties defined in the current directory (optional) override all these settings.
	 *
	 * @param resourceName the location of the resource that should be loaded.
	 *          e.g.: "weka/core/Utils.props". (The use of hardcoded forward
	 *          slashes here is OK - see jdk1.1/docs/guide/misc/resources.html)
	 *          This routine will also look for the file (in this case)
	 *          "Utils.props" in the users home directory and the current
	 *          directory.
	 * @param loader the class loader to use when loading properties
	 * @return the Properties
	 * @exception Exception if no default properties are defined, or if an error
	 *              occurs reading the properties files.
	 */
	protected static Properties read(String resourceName, ClassLoader loader) throws Exception {
		Properties defaultProps = new Properties();
		try {
			// Apparently hardcoded slashes are OK here
			// jdk1.1/docs/guide/misc/resources.html
			Enumeration<URL> urls = loader.getResources(resourceName);
			boolean first = true;
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (first) {
					defaultProps.load(url.openStream());
					first = false;
				}
				else {
					Properties props = new Properties(defaultProps);
					props.load(url.openStream());
					defaultProps = props;
				}
			}
		}
		catch (Exception ex) {
			System.err.println("Warning, unable to load properties file(s) from "
				+ "system resource (Utils.java): " + resourceName);
		}

		// Hardcoded slash is OK here
		// eg: see jdk1.1/docs/guide/misc/resources.html
		int slInd = resourceName.lastIndexOf('/');
		if (slInd != -1) {
			resourceName = resourceName.substring(slInd + 1);
		}

		// Allow a properties file in the WekaPackageManager.PROPERTIES_DIR to
		// override
		Properties userProps = new Properties(defaultProps);
		if (!WekaPackageManager.PROPERTIES_DIR.exists()) {
			WekaPackageManager.PROPERTIES_DIR.mkdir();
		}
		File propFile =
			new File(WekaPackageManager.PROPERTIES_DIR.toString() + File.separator
				+ resourceName);

		if (propFile.exists()) {
			try {
				userProps.load(new FileInputStream(propFile));
			}
			catch (Exception ex) {
				throw new Exception("Problem reading user properties: " + propFile);
			}
		}

		// Allow a properties file in the Project home directory to override
		Properties mekaHomeProps = new Properties(userProps);
		propFile = Project.expandFile(resourceName);
		if (propFile.exists()) {
			try {
				mekaHomeProps.load(new FileInputStream(propFile));
			}
			catch (Exception ex) {
				throw new Exception("Problem reading MEKA HOME properties: " + propFile);
			}
		}

		// Allow a properties file in the current directory to override
		Properties localProps = new Properties(mekaHomeProps);
		propFile = new File(resourceName);
		if (propFile.exists()) {
			try {
				localProps.load(new FileInputStream(propFile));
			}
			catch (Exception ex) {
				throw new Exception("Problem reading local properties: " + propFile);
			}
		}

		return new EnvironmentProperties(localProps);
	}

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

		result = read(props, new PropsUtils().getClass().getClassLoader());

		if (DEBUG)
			System.out.println("start<PropsUtils.read: " + props + ">\n" + toString(result, null) + "end<PropsUtils.read: " + props + ">\n");

		return result;
	}

	/**
	 * Writes the properties to the specified file.
	 *
	 * @param props     the properties to write
	 * @param filename  the file to write to
	 */
	public static void write(Properties props, String filename) {
		write(props, null, filename);
	}

	/**
	 * Writes the properties to the specified file.
	 *
	 * @param props     the properties to write
	 * @param comment   the comments to write out as well at start of file (can be null)
	 * @param filename  the file to write to
	 */
	public static void write(Properties props, String comment, String filename) {
		BufferedWriter	bwriter;
		FileWriter		fwriter;

		fwriter = null;
		bwriter = null;
		try {
			fwriter = new FileWriter(filename);
			bwriter = new BufferedWriter(fwriter);
			props.store(bwriter, comment);
			bwriter.flush();
		}
		catch (Exception e) {
			System.err.println("Failed to write properties: " + filename);
			e.printStackTrace();
		}
		finally {
			FileUtils.closeQuietly(bwriter);
			FileUtils.closeQuietly(fwriter);
		}
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

		// home directory
		if (DEBUG)
			System.out.println("3. meka home dir: " + Project.getHome());
		propsFile = new File(Project.getHome() + File.separator + propsName);
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
			System.out.println("4. current dir: " + System.getProperty("user.dir"));
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
