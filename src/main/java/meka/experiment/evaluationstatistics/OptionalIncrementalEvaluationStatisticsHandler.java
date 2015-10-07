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
 * OptionalIncrementalEvaluationStatisticsHandler.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

/**
 * For handlers that support incremental writes but use it as optional feature that can be turned off.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface OptionalIncrementalEvaluationStatisticsHandler
  extends IncrementalEvaluationStatisticsHandler {

	/**
	 * Sets whether incremental model is turned on.
	 *
	 * @param value     true to turn off incremental mode
	 */
	public void setIncrementalDisabled(boolean value);

	/**
	 * Returns whether incremental mode is turned on.
	 *
	 * @return          true if incremental mode is off
	 */
	public boolean isIncrementalDisabled();

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String incrementalDisabledTipText();
}
