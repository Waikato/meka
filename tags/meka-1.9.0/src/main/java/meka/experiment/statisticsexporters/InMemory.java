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
 * InMemory.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import meka.experiment.evaluationstatistics.EvaluationStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Just stores the statistics in mmemory.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class InMemory
  extends AbstractEvaluationStatisticsExporter {

	private static final long serialVersionUID = 7694488908134978844L;

	/** the statistics. */
	protected List<EvaluationStatistics> m_Statistics = new ArrayList<>();

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	@Override
	public String globalInfo() {
		return "Just stores the statistics in mmemory.";
	}

	/**
	 * Exports the statistics.
	 *
	 * @param stats         the statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	@Override
	protected String doExport(List<EvaluationStatistics> stats) {
		m_Statistics.clear();
		m_Statistics.addAll(stats);
		return null;
	}

	/**
	 * Returns the statistics.
	 *
	 * @return          the statistics
	 */
	public List<EvaluationStatistics> getStatistics() {
		return m_Statistics;
	}
}
