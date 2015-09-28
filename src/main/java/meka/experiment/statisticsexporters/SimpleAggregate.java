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
 * SimpleAggregate.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import gnu.trove.list.array.TDoubleArrayList;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsComparator;
import meka.experiment.evaluationstatistics.EvaluationStatisticsUtils;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple aggregator of statistics.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SimpleAggregate
  extends AbstractMetaEvaluationStatisticsExporter
  implements AggregatedEvaluationStatisticsExporter {

	private static final long serialVersionUID = 45553920349638331L;

	/** the suffix for the count. */
	public final static String SUFFIX_COUNT = "-Count";

	/** the suffix for the mean. */
	public final static String SUFFIX_MEAN = "-Mean";

	/** the suffix for the stdev. */
	public final static String SUFFIX_STDEV = "-StdDev";

	/** the aggregation keys. */
	protected String m_AggregationKeys = getDefaultAggregationKeys();

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	@Override
	public String globalInfo() {
		return "Simple aggregator of statistics.\n"
				+ "For each numeric attribute the following attributes get generated:\n"
				+ "- " + SUFFIX_COUNT + ": the number of rows used to calculate this aggregate\n"
				+ "- " + SUFFIX_MEAN + ": the average/mean\n"
				+ "- " + SUFFIX_STDEV + ": the standard deviation";
	}

	/**
	 * Returns the default keys used for aggregation.
	 *
	 * @return          the default
	 */
	protected String getDefaultAggregationKeys() {
		StringBuilder   result;

		result = new StringBuilder();
		for (String key: EvaluationStatisticsComparator.DEFAULT_KEYS) {
			if (result.length() > 0)
				result.append(" ");
			result.append(key);
		}

		return result.toString();
	}

	/**
	 * Returns the default exporter to use.
	 *
	 * @return          the default
	 */
	@Override
	protected EvaluationStatisticsExporter getDefaultExporter() {
		return new TabSeparated();
	}

	/**
	 * Sets the blank-separated list of keys to use for aggregating.
	 *
	 * @param keys          the keys (blank-separated)
	 */
	@Override
	public void setAggregationKeys(String keys) {
		m_AggregationKeys = keys;
	}

	/**
	 * Returns the blank-separated list of keys used for aggregating.
	 *
	 * @return              the keys (blank-separated)
	 */
	@Override
	public String getAggregationKeys() {
		return m_AggregationKeys;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	@Override
	public String aggregationKeysTipText() {
		return "The keys to use for aggregating the statistics (blank-separated).";
	}

	/**
	 * Calculates the actual aggregates. For each numeric statistic, a mean, stdev and count column get generated.
	 *
	 * @param stats     the statistics to aggregate
	 * @return          the aggregated values
	 */
	protected EvaluationStatistics doAggregate(List<EvaluationStatistics> stats) {
		EvaluationStatistics    result;
		List<String>            keys;
		TDoubleArrayList        values;

		result = new EvaluationStatistics(stats.get(0).getClassifier(), stats.get(0).getRelation(), null);

		// collect all stats
		keys = EvaluationStatisticsUtils.keys(stats, false);

		// collect values
		for (String key: keys) {
			values = new TDoubleArrayList();
			for (EvaluationStatistics stat: stats) {
				if (stat.containsKey(key))
					values.add(stat.get(key).doubleValue());
			}
			if (values.size() > 0) {
				result.put(key + SUFFIX_COUNT, values.size());
				result.put(key + SUFFIX_MEAN, Utils.mean(values.toArray()));
				result.put(key + SUFFIX_STDEV, Math.sqrt(Utils.variance(values.toArray())));
			}
		}

		return result;
	}

	/**
	 * Aggregates the statistics and returns these.
	 *
	 * @param stats         the statistics to aggregate
	 * @return              the aggregated stats
	 */
	@Override
	public List<EvaluationStatistics> aggregate(List<EvaluationStatistics> stats) {
		List<EvaluationStatistics>      result;
		List<EvaluationStatistics>      temp;
		EvaluationStatisticsComparator  comp;
		int                             i;

		try {
			stats  = new ArrayList<>(stats);
			result = new ArrayList<>();
			comp   = new EvaluationStatisticsComparator(Utils.splitOptions(m_AggregationKeys));
			// sort
			Collections.sort(stats, comp);
			// create groups and aggregate them
			i    = 0;
			temp = new ArrayList<>();
			while (i < stats.size()) {
				if ((temp.size() == 0) || (comp.compare(temp.get(temp.size() - 1), stats.get(i)) == 0)) {
					temp.add(stats.get(i));
					i++;
				}
				else {
					result.add(doAggregate(temp));
					temp.clear();
				}
			}
			if (temp.size() > 0)
				result.add(doAggregate(temp));
		}
		catch (Exception e) {
			result = stats;
			handleException("Failed to aggregate!", e);
		}

		return result;
	}

	/**
	 * Exports the statistics.
	 *
	 * @param stats         the statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	@Override
	protected String doExport(List<EvaluationStatistics> stats) {
		return m_Exporter.export(aggregate(stats));
	}
}
