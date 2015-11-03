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
 * FileBasedMeasurementEvaluationStatisticsExporter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

/**
 * Interface for file-based statistics exporters for a single measurement.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface FileBasedMeasurementEvaluationStatisticsExporter
  extends FileBasedEvaluationStatisticsExporter {

	/**
	 * Sets the measurement to use.
	 *
	 * @param value     the measurement
	 */
	public void setMeasurement(String value);

	/**
	 * Returns the measurement in use.
	 *
	 * @return          the measurement
	 */
	public String getMeasurement();

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String measurementTipText();
}
