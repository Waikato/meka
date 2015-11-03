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
import meka.events.LogListener;
import weka.core.Option;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Wraps another exporter.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractMetaEvaluationStatisticsExporter
  extends AbstractEvaluationStatisticsExporter {

	private static final long serialVersionUID = 7974229256817823349L;

	/** the base exporter. */
	protected EvaluationStatisticsExporter m_Exporter = getDefaultExporter();

	/**
	 * Returns the default exporter to use.
	 *
	 * @return          the default
	 */
	protected abstract EvaluationStatisticsExporter getDefaultExporter();

	/**
	 * Sets the exporter to use.
	 *
	 * @param value     the exporter
	 */
	public void setExporter(EvaluationStatisticsExporter value) {
		m_Exporter = value;
	}

	/**
	 * Returns the exporter in use.
	 *
	 * @return          the exporter
	 */
	public EvaluationStatisticsExporter getExporter() {
		return m_Exporter;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String exporterTipText() {
		return "The base exporter to use.";
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
		OptionUtils.addOption(result, exporterTipText(), getDefaultExporter().getClass().getName(), "base");
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
		setExporter((EvaluationStatisticsExporter) OptionUtils.parse(options, "base", getDefaultExporter()));
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
		OptionUtils.add(result, "base", getExporter());
		return OptionUtils.toArray(result);
	}

	/**
	 * Adds the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void addLogListener(LogListener l) {
		super.addLogListener(l);
		m_Exporter.addLogListener(l);
	}

	/**
	 * Remove the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void removeLogListener(LogListener l) {
		super.removeLogListener(l);
		m_Exporter.removeLogListener(l);
	}
}
