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
 * AggregatedEvaluationStatisticsExporter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import meka.experiment.evaluationstatistics.EvaluationStatistics;

import java.util.List;

/**
 * Interface for statistics exporter that aggregate their data first.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface AggregatedEvaluationStatisticsExporter
  extends EvaluationStatisticsExporter {

	/**
	 * Sets the blank-separated list of keys to use for aggregating.
	 *
	 * @param keys          the keys (blank-separated)
	 */
	public void setAggregationKeys(String keys);

	/**
	 * Returns the blank-separated list of keys used for aggregating.
	 *
	 * @return              the keys (blank-separated)
	 */
	public String getAggregationKeys();

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String aggregationKeysTipText();

	/**
	 * Aggregates the statistics and returns these.
	 *
	 * @param stats         the statistics to aggregate
	 * @return              the aggregated stats
	 */
	public List<EvaluationStatistics> aggregate(List<EvaluationStatistics> stats);
}
