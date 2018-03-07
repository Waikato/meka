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
 * ClassMarkdown.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package meka.doc;

import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Generates markdown for a class.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ClassMarkdown
  extends AbstractMarkdownGenerator {

	private static final long serialVersionUID = 814382607780262012L;

	/** the classname to generate the markdown for. */
	protected String m_Classname;

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
		String value;

		value = Utils.getOption("W", options);
		if (value.isEmpty())
			throw new Exception("No classname provided!");
		setClassname(value);

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
		result.add("-W");
		result.add(getClassname());
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Set the classname to generate the markdown for.
	 *
	 * @param value the classname
	 */
	public void setClassname(String value) {
		m_Classname = value;
	}

	/**
	 * Get the classname to generate the markdown for.
	 *
	 * @return the classname
	 */
	public String getClassname() {
		return m_Classname;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String classnameTipText() {
		return "The classname to generate the markdown for.";
	}

	/**
	 * Generates the markdown.
	 *
	 * @return the markdown
	 * @throws Exception if the generation fails
	 */
	@Override
	public String generate() throws Exception {
		StringBuilder result;
		Method method;
		Class cls;
		Object instance;
		String value;
		TechnicalInformationHandler handler;
		Enumeration<Option> options;
		Option option;
		String[] parts;

		result = new StringBuilder();
		cls = Class.forName(m_Classname);
		instance = cls.newInstance();

		// title
		result.append("# ").append(cls.getName()).append("\n\n");

		// global info
		try {
			method = cls.getMethod("globalInfo");
			value = (String) method.invoke(instance);
			result.append("## ").append("Synopsis").append("\n");
			result.append(value).append("\n\n");
		}
		catch (Exception e) {
			// ignored
		}

		// technical information
		if (instance instanceof TechnicalInformationHandler) {
			handler = (TechnicalInformationHandler) instance;
			result.append("## ").append("BibTeX").append("\n");
			result.append("```\n");
			result.append(handler.getTechnicalInformation().toBibTex()).append("\n");
			result.append("```\n");
		}

		// options
		if (instance instanceof OptionHandler) {
			result.append("## ").append("Options").append("\n");
			options = ((OptionHandler) instance).listOptions();
			while (options.hasMoreElements()) {
				option = options.nextElement();
				if (option.synopsis().startsWith("-")) {
					result.append("* `").append(option.synopsis().trim()).append("`\n\n");
					parts = option.description().split("\n");
					for (String part : parts) {
						if (part.trim().isEmpty())
							continue;
						result.append("    ").append(part.trim()).append("\n");
					}
					result.append("\n");
				}
				else {
					result.append("**").append(option.synopsis().trim()).append("**\n");
					result.append("\n");
				}
			}
			result.append("\n");
		}

		return result.toString();
	}

	public static void main(String[] args) throws Exception {
		generateMarkdown(new ClassMarkdown(), args);
	}
}
