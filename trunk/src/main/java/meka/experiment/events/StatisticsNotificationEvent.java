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
 * StatisticsNotificationEvent.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.events;

import meka.experiment.Experiment;
import meka.experiment.evaluationstatistics.EvaluationStatistics;

import java.util.EventObject;
import java.util.List;

/**
 * Event that gets sent by an experiment when new statistics become available.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class StatisticsNotificationEvent
  extends EventObject {

	private static final long serialVersionUID = 7732581989591408787L;

	/** the statistics. */
	protected List<EvaluationStatistics> m_Statistics;

	/**
	 * Gets called when the experiment makes new statistics available.
	 *
	 * @param source        the experiment that triggered the event
	 * @param stats         the statistics
	 */
	public StatisticsNotificationEvent(Experiment source, List<EvaluationStatistics> stats) {
		super(source);

		m_Statistics = stats;
	}

	/**
	 * Returns the associated experiment.
	 *
	 * @return      the experiment
	 */
	public Experiment getExperiment() {
	  return (Experiment) getSource();
	}

	/**
	 * Returns the statistics.
	 *
	 * @return      the statistics
	 */
	public List<EvaluationStatistics> getStatistics() {
		return m_Statistics;
	}
}
