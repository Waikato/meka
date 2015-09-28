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
 * AbstractEvaluationStatisticsExporter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import meka.events.LogObject;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import weka.core.Option;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Ancestor for statistics exporters.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractEvaluationStatisticsExporter
		extends LogObject
		implements EvaluationStatisticsExporter {

	private static final long serialVersionUID = 8950819250563958834L;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public abstract String globalInfo();

	/**
	 * Returns an enumeration of all the available options.
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		return new Vector().elements();
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options
	 * @throws Exception    never
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
	}

	/**
	 * Returns the options.
	 *
	 * @return              the options
	 */
	@Override
	public String[] getOptions() {
		return new String[0];
	}

	/**
	 * Exports the statistics.
	 *
	 * @param stats         the statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	protected abstract String doExport(List<EvaluationStatistics> stats);

	/**
	 * Exports the statistics.
	 *
	 * @param stats         the statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	@Override
	public String export(List<EvaluationStatistics> stats) {
		return doExport(stats);
	}
}
