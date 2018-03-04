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
 * OutputClassHierarchyMarkdown.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package meka.doc;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.gui.goe.GenericObjectEditor;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.PluginManager;
import weka.core.Utils;
import weka.core.WekaPackageClassLoaderManager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Ancestor for outputting documentation for class hierarchies in files.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractOutputClassHierarchy
	implements Serializable, OptionHandler {

	private static final long serialVersionUID = -1269488196227028742L;

	/** whether to output debugging information. */
	protected boolean m_Debug = false;

	/** the superclass. */
	protected String m_Superclass = getDefaultSuperclass();

	/** the output directory. */
	protected File m_OutputDir = getDefaultOutputDir();

	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> result = new Vector<>();

		result.addElement(new Option(
			"\tIf set, generator is run in debug mode and\n"
				+ "\tmay output additional info to the console",
			"output-debug-info", 0, "-output-debug-info"));

		result.addElement(new Option(
			"\tThe superclass of the class hierarchy to output.",
			"superclass", 1, "-superclass <classname>"));

		result.addElement(new Option(
			"\tThe directory to output the generated documentation in.",
			"output-dir", 1, "-output-dir <directory>"));

		return result.elements();
	}

	/**
	 * Parses a given list of options.
	 *
	 * @param options the list of options as an array of strings
	 * @throws  Exception if an option is not supported
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		String value;

		setDebug(Utils.getFlag("output-debug-info", options));

		value = Utils.getOption("superclass", options);
		if (value.isEmpty())
			setSuperclass(getDefaultSuperclass());
		else
			setSuperclass(value);

		value = Utils.getOption("output-dir", options);
		if (value.isEmpty())
			setOutputDir(getDefaultOutputDir());
		else
			setOutputDir(new File(value));
	}

	/**
	 * Gets the current option settings for the OptionHandler.
	 *
	 * @return the list of current option settings as an array of strings
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>();

		if (m_Debug)
			result.add("-output-debug-info");

		result.add("-superclass");
		result.add(getSuperclass());

		result.add("-output-dir");
		result.add(getOutputDir().toString());

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Set debugging mode.
	 *
	 * @param value true if debug output should be printed
	 */
	public void setDebug(boolean value) {
		m_Debug = value;
	}

	/**
	 * Get whether debugging is turned on.
	 *
	 * @return true if debugging output is on
	 */
	public boolean getDebug() {
		return m_Debug;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String debugTipText() {
		return "If set to true, generator may output additional info to the console.";
	}

	/**
	 * Returns the default superclass.
	 *
	 * @return the default
	 */
	protected String getDefaultSuperclass() {
		return MultiLabelClassifier.class.getName();
	}

	/**
	 * Set the superclass of the class hierarchy to output.
	 *
	 * @param value the superclass
	 */
	public void setSuperclass(String value) {
		m_Superclass = value;
	}

	/**
	 * Get the superclass of the class hierarchy to output.
	 *
	 * @return the superclass
	 */
	public String getSuperclass() {
		return m_Superclass;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String superclassTipText() {
		return "The superclass of the class hierarchy to output.";
	}

	/**
	 * Returns the default output directory.
	 *
	 * @return the default
	 */
	protected File getDefaultOutputDir() {
		return new File(".");
	}

	/**
	 * Set the output directory to store the generated documentation in.
	 *
	 * @param value the output dir
	 */
	public void setOutputDir(File value) {
		m_OutputDir = value;
	}

	/**
	 * Get the output directory to store the generated documentation in.
	 *
	 * @return the output dir
	 */
	public File getOutputDir() {
		return m_OutputDir;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String outputDirTipText() {
		return "The output directory to store the generated documentation in.";
	}

	/**
	 * Hook method before generating the documentation.
	 *
	 * @return null if successful, otherwise error message
	 */
	protected String check() {
		GenericObjectEditor.registerAllEditors();

		try {
			WekaPackageClassLoaderManager.forName(m_Superclass);
		}
		catch (Exception e) {
			return "Failed to instantiate superclass '" + m_Superclass + "': " + e;
		}

		if (!m_OutputDir.exists())
			return "Output directory does not exist: " + m_OutputDir;
		if (!m_OutputDir.isDirectory())
			return "Output directory does not point to a directory: " + m_OutputDir;

		return null;
	}

	/**
	 * Generates a filename (without path) from the classname.
	 *
	 * @param classname the classname to generate the filename for
	 * @return the filename
	 */
	protected abstract String generateFilename(String classname);

	/**
	 * Generates the documentation for the specified class.
	 *
	 * @param classname the class to generate the documentation for
	 * @param outFile the file to store the documentation in
	 * @throws Exception if generation fails
	 */
	protected abstract void doGenerate(String classname, File outFile) throws Exception;

	/**
	 * Generates the documentation.
	 *
	 * @throws Exception if generation fails
	 */
	protected void doGenerate() throws Exception {
		List<String> classnames;
		File outFile;

		GenericObjectEditor.registerAllEditors();
		classnames = PluginManager.getPluginNamesOfTypeList(m_Superclass);
		if (m_Debug)
			System.out.println("# classes: " + classnames.size());

		for (String classname: classnames) {
			if (m_Debug)
				System.out.println("--> " + classname);
			outFile = new File(m_OutputDir.getAbsolutePath() + File.separator + generateFilename(classname));
			if (m_Debug)
				System.out.println("output file: " + outFile);
			doGenerate(classname, outFile);
		}
	}

	/**
	 * Generates the documentation.
	 *
	 * @throws Exception if generation fails
	 */
	public void generate() throws Exception {
		String msg;
		msg = check();
		if (msg != null)
			throw new IllegalStateException(msg);
		doGenerate();
	}

	/**
	 * Generates the documentation using the supplied arguments.
	 *
	 * @param generator the generator to use
	 * @param options the options for the generator
	 * @throws Exception in case the generation fails
	 */
	public static void generateOutput(AbstractOutputClassHierarchy generator, String[] options) throws Exception {
		generator.setOptions(options);
		generator.generate();
	}

}
