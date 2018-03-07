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

import weka.core.Option;
import weka.core.Utils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Generates markdown documentation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class OutputClassHierarchyMarkdown
	extends AbstractOutputClassHierarchy {

	private static final long serialVersionUID = -6256728478824354526L;

	/** whether to skip the title. */
	protected boolean m_SkipTitle = false;

	/** the generator. */
	protected ClassMarkdown m_Generator;

	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> result = new Vector<>();
		Enumeration<Option> enm = super.listOptions();
		while (enm.hasMoreElements())
			result.add(enm.nextElement());

		result.addElement(new Option(
			"\tIf set, title is skipped in the output",
			"skip-title", 0, "-skip-title"));

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
		setSkipTitle(Utils.getFlag("skip-title", options));

		super.setOptions(options);
	}

	/**
	 * Gets the current option settings for the OptionHandler.
	 *
	 * @return the list of current option settings as an array of strings
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>(Arrays.asList(super.getOptions()));

		if (m_SkipTitle)
			result.add("-skip-title");

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Set whether skipping title.
	 *
	 * @param value true if skipping title
	 */
	public void setSkipTitle(boolean value) {
		m_SkipTitle = value;
	}

	/**
	 * Get whether skipping title.
	 *
	 * @return true if to skip title
	 */
	public boolean getSkipTitle() {
		return m_SkipTitle;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String skipTitleTipText() {
		return "If set to true, the title is skipped in the output.";
	}

	/**
	 * Generates a filename (without path) from the classname.
	 *
	 * @param classname the classname to generate the filename for
	 * @return the filename
	 */
	@Override
	protected String generateFilename(String classname) {
		return classname + ".md";
	}

	/**
	 * Generates the documentation for the specified class.
	 *
	 * @param classname the class to generate the documentation for
	 * @param outFile the file to store the documentation in
	 * @throws Exception if generation fails
	 */
	@Override
	protected void doGenerate(String classname, File outFile) throws Exception {
		String content;
		List<String> lines;

		if (m_Generator == null) {
			m_Generator = new ClassMarkdown();
			m_Generator.setDebug(m_Debug);
			m_Generator.setSkipTitle(m_SkipTitle);
		}

		System.out.println("      - " + classname + ": " + outFile.getName());
		m_Generator.setClassname(classname);
		content = m_Generator.generate();
		lines = Arrays.asList(content.split("\n"));
		Files.write(
			outFile.toPath(),
			lines,
			Charset.forName("UTF-8"),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * Generates the documentation using the supplied arguments.
	 *
	 * @param args the parameters
	 * @throws Exception if generation fails
	 */
	public static void main(String[] args) throws Exception {
		generateOutput(new OutputClassHierarchyMarkdown(), args);
	}
}
