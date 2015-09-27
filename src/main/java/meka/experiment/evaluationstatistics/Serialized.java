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
 * Serialized.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import meka.core.ExceptionUtils;
import meka.core.OptionUtils;
import weka.core.Option;
import weka.core.SerializationHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Uses Java serialization for readin/writing the statistics.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Serialized
  implements FileBasedEvaluationStatisticsHandler {

	private static final long serialVersionUID = -1090631157162943295L;

	/** the file to read from/write to. */
	protected File m_File = new File(".");

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Uses Java serialization for readin/writing the statistics.";
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
		OptionUtils.addOption(result, fileTipText(), ".", 'F');
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
		setFile(OptionUtils.parse(options, 'F', new File(".")));
	}

	/**
	 * Returns the options.
	 *
	 * @return              the options
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>();
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

	/**
	 * Reads the statistics.
	 *
	 * @return              the statistics that were read
	 */
	@Override
	public List<EvaluationStatistics> read() {
		List<EvaluationStatistics>  result;

		try {
			result = (List<EvaluationStatistics>) SerializationHelper.read(m_File.getAbsolutePath());
		}
		catch (Exception e) {
			result = null;
			ExceptionUtils.handleException(this, "Failed to read serialized statistics from: " + m_File, e);
		}

		return result;
	}

	/**
	 * Stores the given statistics.
	 *
	 * @param stats         the statistics to store
	 * @return              null if successfully stored, otherwise error message
	 */
	@Override
	public String write(List<EvaluationStatistics> stats) {
		try {
			SerializationHelper.write(m_File.getAbsolutePath(), stats);
			return null;
		}
		catch (Exception e) {
			return ExceptionUtils.handleException(this, "Failed to write statistics to: " + m_File, e);
		}
	}

	/**
	 * Gets called after the experiment finished.
	 *
	 * @return          null if successfully finished, otherwise error message
	 */
	public String finish() {
		return null;
	}
}
