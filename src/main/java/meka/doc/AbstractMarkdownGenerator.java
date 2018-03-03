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
 * AbstractMarkdownGenerator.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package meka.doc;

import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Ancestor for markdown generators.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractMarkdownGenerator
	implements Serializable, OptionHandler {

	private static final long serialVersionUID = -8846271464951288169L;

	/** whether to output debugging information. */
	protected boolean m_Debug = false;

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
		setDebug(Utils.getFlag("output-debug-info", options));
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
	 * Generates the markdown.
	 *
	 * @return the markdown
	 * @throws Exception if the generation fails
	 */
	public abstract String generate() throws Exception;

	/**
	 * Generates the markdown and outputs it on stdout.
	 *
	 * @param generator the generator to use
	 * @param options the options for the generator
	 * @throws Exception in case the generation fails
	 */
	public static void generateMarkdown(AbstractMarkdownGenerator generator, String[] options) throws Exception {
		generator.setOptions(options);
		System.out.println(generator.generate());
	}
}
