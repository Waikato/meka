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
 * AbstractFileBasedEvaluationStatisticsHandler.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import meka.core.OptionUtils;
import weka.core.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Ancestor for file-base handlers.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractFileBasedEvaluationStatisticsHandler
	extends AbstractEvaluationStatisticsHandler
    implements FileBasedEvaluationStatisticsHandler {

	private static final long serialVersionUID = -1090631157162943295L;

	/** the file to read from/write to. */
	protected File m_File = getDefaultFile();

	/**
	 * Returns the default file.
	 *
	 * @return          the default
	 */
	protected File getDefaultFile() {
		return new File(".");
	}

	/**
	 * Sets the file to read from/write to.
	 *
	 * @param value     the file
	 */
	public void setFile(File value) {
		m_File = value;
	}

	/**
	 * Returns the file to read from/write to.
	 *
	 * @return          the file
	 */
	public File getFile() {
		return m_File;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String fileTipText() {
		return "The file to read from/write to.";
	}

	/**
	 * Returns an enumeration of all the available options.
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.add(result, super.listOptions());
		OptionUtils.addOption(result, fileTipText(), "" + getDefaultFile(), 'F');
		return OptionUtils.toEnumeration(result);
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options
	 * @throws Exception    never
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		setFile(OptionUtils.parse(options, 'F', getDefaultFile()));
		super.setOptions(options);
	}

	/**
	 * Returns the options.
	 *
	 * @return              the options
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, super.getOptions());
		OptionUtils.add(result, 'F', getFile());
		return OptionUtils.toArray(result);
	}

	/**
	 * Initializes the handler.
	 *
	 * @return      null if successfully initialized, otherwise error message
	 */
	@Override
	public String initialize() {
		if (m_File.isDirectory())
			return "File points to a directory: " + m_File;
		return null;
	}
}
