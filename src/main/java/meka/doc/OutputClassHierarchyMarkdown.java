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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

/**
 * Generates markdown documentation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class OutputClassHierarchyMarkdown
	extends AbstractOutputClassHierarchy {

	private static final long serialVersionUID = -6256728478824354526L;

	/** the generator. */
	protected ClassMarkdown m_Generator;

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
		}

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
