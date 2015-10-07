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
 * EvaluationStatisticsHandler.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import meka.events.LogSupporter;
import weka.core.OptionHandler;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for classes that load and save collected {@link EvaluationStatistics} in some form.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface EvaluationStatisticsHandler
  extends OptionHandler, Serializable, LogSupporter {

	/**
	 * Returns whether the handler is threadsafe.
	 *
	 * @return      true if threadsafe
	 */
	public boolean isThreadSafe();

	/**
	 * Initializes the handler.
	 *
	 * @return      null if successfully initialized, otherwise error message
	 */
	public String initialize();

	/**
	 * Reads the statistics.
	 *
	 * @return              the statistics that were read
	 */
	public List<EvaluationStatistics> read();

	/**
	 * Stores the given statistics.
	 *
	 * @param stats         the statistics to store
	 * @return              null if successfully stored, otherwise error message
	 */
	public String write(List<EvaluationStatistics> stats);

	/**
	 * Gets called after the experiment finished.
	 *
	 * @return          null if successfully finished, otherwise error message
	 */
	public String finish();
}
