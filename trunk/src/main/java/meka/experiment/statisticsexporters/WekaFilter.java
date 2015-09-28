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
 * WekaFilter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsUtils;
import weka.core.*;
import weka.filters.AllFilter;
import weka.filters.Filter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Applies a Weka filter to the data.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class WekaFilter
  extends AbstractMetaEvaluationStatisticsExporter {

	private static final long serialVersionUID = 3442329448692418564L;

	/** the filter to apply to the data before passing it on to the base exporter. */
	protected Filter m_Filter = getDefaultFilter();
	
	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	@Override
	public String globalInfo() {
		return 
				"Applies the specified Weka filter to the statistics before passing them on to the base exporter.\n"
				+ "This allows you to remove attributes, filter instances, etc.";
	}

	/**
	 * Returns the default keys used for aggregation.
	 *
	 * @return          the default
	 */
	protected Filter getDefaultFilter() {
		return new AllFilter();
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
	 * Sets the filter to use.
	 *
	 * @param value          the filter
	 */
	public void setFilter(Filter value) {
		m_Filter = value;
	}

	/**
	 * Returns the filter to use.
	 *
	 * @return              the filter
	 */
	public Filter getFilter() {
		return m_Filter;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String filterTipText() {
		return "The Weka filter to apply to the data.";
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
		OptionUtils.addOption(result, filterTipText(), getDefaultFilter().getClass().getName(), "filter");
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
		setFilter((Filter) OptionUtils.parse(options, "filter", getDefaultFilter()));
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
		OptionUtils.add(result, "filter", getFilter());
		return OptionUtils.toArray(result);
	}

	/**
	 * Turns the statistics into Instances.
	 *
	 * @param stats         the statistics to convert
	 * @return              the generated data
	 */
	protected Instances toInstances(List<EvaluationStatistics> stats) {
		Instances               result;
		ArrayList<Attribute>    atts;
		List<String>            headers;
		Instance                inst;
		double[]                values;
		int                     i;

		// header
		headers = EvaluationStatisticsUtils.header(stats, true, true);
		atts    = new ArrayList<>();
		for (String header: headers) {
			if (header.equals(EvaluationStatistics.KEY_CLASSIFIER) || header.equals(EvaluationStatistics.KEY_RELATION))
				atts.add(new Attribute(header, (List) null));
			else
				atts.add(new Attribute(header));
		}
		result = new Instances("stats", atts, stats.size());

		// data
		for (EvaluationStatistics stat: stats) {
			values = new double[result.numAttributes()];
			for (i = 0; i < values.length; i++) {
				if (headers.get(i).equals(EvaluationStatistics.KEY_CLASSIFIER))
					values[i] = result.attribute(i).addStringValue(Utils.toCommandLine(stat.getClassifier()));
				else if (headers.get(i).equals(EvaluationStatistics.KEY_RELATION))
					values[i] = result.attribute(i).addStringValue(stat.getRelation());
				else if (stat.containsKey(headers.get(i)))
					values[i] = stat.get(headers.get(i)).doubleValue();
				else
					values[i] = Utils.missingValue();
			}
			inst = new DenseInstance(1.0, values);
			result.add(inst);
		}

		return result;
	}

	/**
	 * Converts the Instances back into statistics.
	 *
	 * @param data          the data to convert
	 * @return              the generated statistics
	 */
	protected List<EvaluationStatistics> fromInstances(Instances data) {
		List<EvaluationStatistics>      result;
		EvaluationStatistics            stat;
		MultiLabelClassifier            cls;
		String                          rel;
		int                             i;
		int                             n;
		Instance                        inst;

		result = new ArrayList<>();

		if (data.attribute(EvaluationStatistics.KEY_CLASSIFIER) == null) {
			log("Failed to locate attribute: " + EvaluationStatistics.KEY_CLASSIFIER);
			return result;
		}
		if (data.attribute(EvaluationStatistics.KEY_RELATION) == null) {
			log("Failed to locate attribute: " + EvaluationStatistics.KEY_RELATION);
			return result;
		}

		for (i = 0; i < data.numInstances(); i++) {
			inst = data.instance(i);
			try {
				cls = OptionUtils.fromCommandLine(MultiLabelClassifier.class, inst.stringValue(data.attribute(EvaluationStatistics.KEY_CLASSIFIER)));
				rel = inst.stringValue(data.attribute(EvaluationStatistics.KEY_RELATION));
				stat = new EvaluationStatistics(cls, rel, null);
				for (n = 0; n < inst.numAttributes(); n++) {
					if (inst.attribute(n).isNumeric() && !inst.isMissing(n)) {
						stat.put(inst.attribute(n).name(), inst.value(n));
					}
				}
				result.add(stat);
			}
			catch (Exception e) {
				handleException("Failed to process instance: " + inst, e);
			}
		}

		return result;
	}

	/**
	 * Filters the statistics using the specified filter.
	 *
	 * @param stats         the stats to filter
	 * @return              the filtered stats
	 */
	protected List<EvaluationStatistics> filter(List<EvaluationStatistics> stats) {
		Instances       data;
		Instances       filtered;
		Filter          filter;

		try {
			data   = toInstances(stats);
			filter = Filter.makeCopy(m_Filter);
			filter.setInputFormat(data);
			filtered = Filter.useFilter(data, filter);
			stats = fromInstances(filtered);
		}
		catch (Exception e) {
			handleException("Failed to filter statistics!", e);
		}

		return stats;
	}

	/**
	 * Exports the statistics.
	 *
	 * @param stats         the statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	@Override
	public String export(List<EvaluationStatistics> stats) {
		return m_Exporter.export(filter(stats));
	}
}
