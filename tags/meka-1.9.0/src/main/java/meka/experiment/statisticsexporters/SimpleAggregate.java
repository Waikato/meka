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
import meka.core.OptionUtils;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsComparator;
import meka.experiment.evaluationstatistics.EvaluationStatisticsUtils;
import weka.core.Option;
import weka.core.Utils;

import java.util.*;

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

	/** the count suffix. */
	protected String m_SuffixCount = getDefaultSuffixCount();

	/** the mean suffix. */
	protected String m_SuffixMean = getDefaultSuffixMean();

	/** the stdev suffix. */
	protected String m_SuffixStdDev = getDefaultSuffixStdDev();

	/** whether to skip the count. */
	protected boolean m_SkipCount = false;

	/** whether to skip the mean. */
	protected boolean m_SkipMean = false;

	/** whether to skip the stdev. */
	protected boolean m_SkipStdDev = false;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	@Override
	public String globalInfo() {
		return "Simple aggregator of statistics.\n"
				+ "For each numeric attribute the following attributes get generated:\n"
				+ "- " + getDefaultSuffixCount() + ": the number of rows used to calculate this aggregate\n"
				+ "- " + getDefaultSuffixMean() + ": the average/mean\n"
				+ "- " + getDefaultSuffixStdDev() + ": the standard deviation";
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
	 * Returns the default suffix for the count.
	 *
	 * @return          the default
	 */
	protected String getDefaultSuffixCount() {
		return SUFFIX_COUNT;
	}

	/**
	 * Sets the suffix for the count.
	 *
	 * @param value         the suffix
	 */
	public void setSuffixCount(String value) {
		m_SuffixCount = value;
	}

	/**
	 * Returns the suffix for the count.
	 *
	 * @return              the suffix
	 */
	public String getSuffixCount() {
		return m_SuffixCount;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String suffixCountTipText() {
		return "The suffix for the 'count' statistic.";
	}

	/**
	 * Returns the default suffix for the mean.
	 *
	 * @return          the default
	 */
	protected String getDefaultSuffixMean() {
		return SUFFIX_MEAN;
	}

	/**
	 * Sets the suffix for the mean.
	 *
	 * @param value         the suffix
	 */
	public void setSuffixMean(String value) {
		m_SuffixMean = value;
	}

	/**
	 * Returns the suffix for the mean.
	 *
	 * @return              the suffix
	 */
	public String getSuffixMean() {
		return m_SuffixMean;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String suffixMeanTipText() {
		return "The suffix for the 'mean' statistic.";
	}

	/**
	 * Returns the default suffix for the stddev.
	 *
	 * @return          the default
	 */
	protected String getDefaultSuffixStdDev() {
		return SUFFIX_STDEV;
	}

	/**
	 * Sets the suffix for the stddev.
	 *
	 * @param value         the suffix
	 */
	public void setSuffixStdDev(String value) {
		m_SuffixStdDev = value;
	}

	/**
	 * Returns the suffix for the stddev.
	 *
	 * @return              the suffix
	 */
	public String getSuffixStdDev() {
		return m_SuffixStdDev;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String suffixStdDevTipText() {
		return "The suffix for the 'stddev' statistic.";
	}

	/**
	 * Sets whether to skip the count.
	 *
	 * @param value         true if to skip
	 */
	public void setSkipCount(boolean value) {
		m_SkipCount = value;
	}

	/**
	 * Returns whether to skip the count.
	 *
	 * @return              true if to skip
	 */
	public boolean getSkipCount() {
		return m_SkipCount;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String skipCountTipText() {
		return "If enabled, the count is skipped, ie not output.";
	}

	/**
	 * Sets whether to skip the mean.
	 *
	 * @param value         true if to skip
	 */
	public void setSkipMean(boolean value) {
		m_SkipMean = value;
	}

	/**
	 * Returns whether to skip the mean.
	 *
	 * @return              true if to skip
	 */
	public boolean getSkipMean() {
		return m_SkipMean;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String skipMeanTipText() {
		return "If enabled, the mean is skipped, ie not output.";
	}

	/**
	 * Sets whether to skip the stdDev.
	 *
	 * @param value         true if to skip
	 */
	public void setSkipStdDev(boolean value) {
		m_SkipStdDev = value;
	}

	/**
	 * Returns whether to skip the stdDev.
	 *
	 * @return              true if to skip
	 */
	public boolean getSkipStdDev() {
		return m_SkipStdDev;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String skipStdDevTipText() {
		return "If enabled, the standard deviation is skipped, ie not output.";
	}

	/**
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.add(result, super.listOptions());
		OptionUtils.addOption(result, aggregationKeysTipText(), getDefaultAggregationKeys(), "key");
		OptionUtils.addOption(result, suffixCountTipText(), getDefaultSuffixCount(), "suffix-count");
		OptionUtils.addOption(result, suffixMeanTipText(), getDefaultSuffixMean(), "suffix-mean");
		OptionUtils.addOption(result, suffixStdDevTipText(), getDefaultSuffixStdDev(), "suffix-stddev");
		OptionUtils.addOption(result, skipCountTipText(), "no", "skip-count");
		OptionUtils.addOption(result, skipMeanTipText(), "no", "skip-mean");
		OptionUtils.addOption(result, skipStdDevTipText(), "no", "skip-stddev");
		return OptionUtils.toEnumeration(result);
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options to parse
	 * @throws Exception    if parsing fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		setAggregationKeys(OptionUtils.parse(options, "key", getDefaultAggregationKeys()));
		setSuffixCount(OptionUtils.parse(options, "suffix-count", getDefaultSuffixCount()));
		setSuffixMean(OptionUtils.parse(options, "suffix-mean", getDefaultSuffixMean()));
		setSuffixStdDev(OptionUtils.parse(options, "suffix-stddev", getDefaultSuffixStdDev()));
		setSkipCount(Utils.getFlag("skip-count", options));
		setSkipMean(Utils.getFlag("skip-mean", options));
		setSkipStdDev(Utils.getFlag("skip-stddev", options));
		super.setOptions(options);
	}

	/**
	 * Returns the options.
	 *
	 * @return              the current options
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, super.getOptions());
		OptionUtils.add(result, "key", getAggregationKeys());
		OptionUtils.add(result, "suffix-count", getSuffixCount());
		OptionUtils.add(result, "suffix-mean", getSuffixMean());
		OptionUtils.add(result, "suffix-stddev", getSuffixStdDev());
		OptionUtils.add(result, "skip-count", getSkipCount());
		OptionUtils.add(result, "skip-mean", getSkipMean());
		OptionUtils.add(result, "skip-stddev", getSkipStdDev());
		return OptionUtils.toArray(result);
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
				if (!m_SkipCount)
					result.put(key + m_SuffixCount, values.size());
				if (!m_SkipMean)
					result.put(key + m_SuffixMean, Utils.mean(values.toArray()));
				if (!m_SkipStdDev)
					result.put(key + m_SuffixStdDev, Math.sqrt(Utils.variance(values.toArray())));
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
