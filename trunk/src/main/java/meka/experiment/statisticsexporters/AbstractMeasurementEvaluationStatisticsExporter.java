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
 * AbstractMeasurementEvaluationStatisticsExporter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import meka.core.OptionUtils;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import weka.core.Option;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Ancestor for classes that export a single statistic.
 * First column are datasets, first row are classifiers.
 * Automatically aggregates the statistics and displays the "mean".
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractMeasurementEvaluationStatisticsExporter
  extends AbstractFileBasedEvaluationStatisticsExporter {

	private static final long serialVersionUID = -2891664931765964612L;

	/** the measurement to output. */
	protected String m_Measurement = getDefaultMeasurement();

	/**
	 * Returns the default exporters to use.
	 *
	 * @return          the default
	 */
	protected String getDefaultMeasurement() {
		return "Hamming loss";
	}

	/**
	 * Sets the measurement to use.
	 *
	 * @param value     the measurement
	 */
	public void setMeasurement(String value) {
		m_Measurement = value;
	}

	/**
	 * Returns the measurement in use.
	 *
	 * @return          the measurement
	 */
	public String getMeasurement() {
		return m_Measurement;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String measurementTipText() {
		return "The measurement to output.";
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
		OptionUtils.addOption(result, measurementTipText(), getDefaultMeasurement(), 'M');
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
		setMeasurement(OptionUtils.parse(options, 'M', getDefaultMeasurement()));
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
		OptionUtils.add(result, 'M', getMeasurement());
		return OptionUtils.toArray(result);
	}

	/**
	 * Aggregates the stats and returns the "mean".
	 *
	 * @param stats         the stats to aggregate
	 * @return              the aggregated stats
	 */
	protected List<EvaluationStatistics> aggregate(List<EvaluationStatistics> stats) {
		InMemory        inmem;
		SimpleAggregate aggregate;

		inmem = new InMemory();
		aggregate = new SimpleAggregate();
		aggregate.setSuffixMean("");
		aggregate.setExporter(inmem);
		aggregate.export(stats);

		return inmem.getStatistics();
	}

	/**
	 * Exports the aggregated statistics.
	 *
	 * @param stats         the aggregated statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	protected abstract String doExportAggregated(List<EvaluationStatistics> stats);

	/**
	 * Exports the statistics.
	 *
	 * @param stats         the statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	@Override
	protected String doExport(List<EvaluationStatistics> stats) {
		return doExportAggregated(aggregate(stats));
	}
}
