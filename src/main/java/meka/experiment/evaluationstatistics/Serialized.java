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

import weka.core.SerializationHelper;

import java.util.List;

/**
 * Uses Java serialization for readin/writing the statistics.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Serialized
  extends AbstractFileBasedEvaluationStatisticsHandler {

	private static final long serialVersionUID = -1090631157162943295L;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Uses Java serialization for readin/writing the statistics.";
	}

	/**
	 * Returns the format description.
	 *
	 * @return      the file format
	 */
	public String getFormatDescription() {
		return "Java serialized statistics";
	}

	/**
	 * Returns the format extension(s).
	 *
	 * @return      the extension(s) (incl dot)
	 */
	public String[] getFormatExtensions() {
		return new String[]{".ser"};
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
			handleException("Failed to read serialized statistics from: " + m_File, e);
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
			return handleException("Failed to write statistics to: " + m_File, e);
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
