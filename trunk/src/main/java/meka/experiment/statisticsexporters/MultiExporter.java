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
 * AbstractMetaEvaluationStatisticsExporter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import meka.core.OptionUtils;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.events.LogListener;
import weka.core.Option;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Exports the statistics using multiple exporters.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MultiExporter
  extends AbstractEvaluationStatisticsExporter {

	private static final long serialVersionUID = 7974229256817823349L;

	/** the base exporter. */
	protected EvaluationStatisticsExporter[] m_Exporters = getDefaultExporters();

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	@Override
	public String globalInfo() {
		return "Exports the statistics using multiple exporters.";
	}

	/**
	 * Returns the default exporters to use.
	 *
	 * @return          the default
	 */
	protected EvaluationStatisticsExporter[] getDefaultExporters() {
		return new EvaluationStatisticsExporter[0];
	}

	/**
	 * Sets the exporters to use.
	 *
	 * @param value     the exporters
	 */
	public void setExporters(EvaluationStatisticsExporter[] value) {
		m_Exporters = value;
	}

	/**
	 * Returns the exporter in use.
	 *
	 * @return          the exporter
	 */
	public EvaluationStatisticsExporter[] getExporters() {
		return m_Exporters;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String exportersTipText() {
		return "The base exporters to use.";
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
		OptionUtils.addOption(result, exportersTipText(), "none", "base");
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
		setExporters(OptionUtils.parse(options, "base", EvaluationStatisticsExporter.class));
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
		OptionUtils.add(result, "base", getExporters());
		return OptionUtils.toArray(result);
	}

	/**
	 * Adds the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void addLogListener(LogListener l) {
		super.addLogListener(l);
		for (EvaluationStatisticsExporter exporter: m_Exporters)
			exporter.addLogListener(l);
	}

	/**
	 * Remove the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void removeLogListener(LogListener l) {
		super.removeLogListener(l);
		for (EvaluationStatisticsExporter exporter: m_Exporters)
			exporter.removeLogListener(l);
	}

	/**
	 * Exports the statistics.
	 *
	 * @param stats         the statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	@Override
	public String export(List<EvaluationStatistics> stats) {
		String      result;
		int         i;

		result = null;

		for (i = 0; i < m_Exporters.length; i++) {
			result = m_Exporters[i].export(stats);
			if (result != null) {
				result = "Exporter #" + (i+1) + ": " + result;
				log(result);
				break;
			}
		}

		return result;
	}
}
