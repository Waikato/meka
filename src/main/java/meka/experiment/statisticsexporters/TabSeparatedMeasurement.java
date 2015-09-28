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
 * TabSeparated.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import meka.core.FileUtils;
import meka.core.OptionUtils;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsUtils;
import weka.core.Option;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.RemoveByName;
import weka.filters.unsupervised.attribute.RenameAttribute;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Exports a single statistic to a tab-separated file.
 * First column are datasets, first row are classifiers.
 * Automatically aggregates the statistics and displays the "mean".
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class TabSeparatedMeasurement
  extends AbstractFileBasedEvaluationStatisticsExporter {

	private static final long serialVersionUID = -2891664931765964612L;

	/** the measurement to output. */
	protected String m_Measurement = getDefaultMeasurement();

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	@Override
	public String globalInfo() {
		return "Exports a single statistic to a tab-separated file.\n"
				+ "First column are datasets, first row are classifiers.\n"
				+ "Automatically aggregates the statistics and displays the 'mean'.";
	}

	/**
	 * Returns the format description.
	 *
	 * @return      the file format
	 */
	@Override
	public String getFormatDescription() {
		return "Tab-separated statistic";
	}

	/**
	 * Returns the format extension(s).
	 *
	 * @return      the extension(s) (incl dot)
	 */
	@Override
	public String[] getFormatExtensions() {
		return new String[]{".tsv"};
	}

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
		RemoveByName    remove;
		RenameAttribute rename;
		MultiFilter     multi;
		WekaFilter      filter;
		SimpleAggregate aggregate;

		inmem = new InMemory();
		remove = new RemoveByName();
		remove.setExpression(".*(" + SimpleAggregate.SUFFIX_COUNT + "|" + SimpleAggregate.SUFFIX_STDEV + ")$");
		rename = new RenameAttribute();
		rename.setFind(SimpleAggregate.SUFFIX_MEAN + "$");
		rename.setReplace("");
		multi = new MultiFilter();
		multi.setFilters(new Filter[]{remove, rename});
		filter = new WekaFilter();
		filter.setFilter(multi);
		filter.setExporter(inmem);
		aggregate = new SimpleAggregate();
		aggregate.setExporter(filter);
		aggregate.export(stats);

		return inmem.getStatistics();
	}

	/**
	 * Exports the statistics.
	 *
	 * @param stats         the statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	@Override
	protected String doExport(List<EvaluationStatistics> stats) {
		String          result;
		List<String>    classifiers;
		List<String>    relations;
		List<Number>    measurements;
		FileWriter      fwriter;
		BufferedWriter  bwriter;
		int             i;

		result = null;

		// aggregate
		stats = aggregate(stats);

		classifiers = EvaluationStatisticsUtils.commandLines(stats, true);
		relations   = EvaluationStatisticsUtils.relations(stats, true);
		fwriter = null;
		bwriter = null;
		try {
			fwriter = new FileWriter(m_File);
			bwriter = new BufferedWriter(fwriter);

			// output header
			bwriter.write("Datasets");
			for (i = 0; i < classifiers.size(); i++) {
				bwriter.write("\t");
				bwriter.write("[" + (i+1) + "]");
			}
			bwriter.newLine();
			bwriter.flush();

			// output statistics
			for (String relation: relations) {
				bwriter.write(relation);
				for (i = 0; i < classifiers.size(); i++) {
					bwriter.write("\t");
					measurements = EvaluationStatisticsUtils.measurements(stats, classifiers.get(i), relation, m_Measurement);
					if (measurements.size() > 0) {
						if (measurements.size() > 1)
							log("Found " + measurements.size() + " measurements for combination " + classifiers.get(i) + "/" + relation);
						bwriter.write("" + measurements.get(0));
					}
				}
				bwriter.newLine();
				bwriter.flush();
			}

			// output key
			bwriter.newLine();
			bwriter.write("Index");
			bwriter.write("\t");
			bwriter.write("Classifier");
			bwriter.newLine();
			for (i = 0; i < classifiers.size(); i++) {
				bwriter.write("[" + (i+1) + "]");
				bwriter.write("\t");
				bwriter.write(classifiers.get(i));
				bwriter.newLine();
			}
		}
		catch (Exception e) {
			result = handleException("Failed to export statistics to: " + m_File, e);
		}
		finally {
			FileUtils.closeQuietly(bwriter);
			FileUtils.closeQuietly(fwriter);
		}

		return result;
	}
}
