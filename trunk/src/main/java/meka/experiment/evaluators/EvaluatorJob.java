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
 * EvaluatorJob.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluators;

import meka.experiment.evaluationstatistics.EvaluationStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Job to be used in parallel execution.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class EvaluatorJob
  implements Callable<List<EvaluationStatistics>> {

	/** the results. */
	protected List<EvaluationStatistics> m_Result = new ArrayList<>();

	/**
	 * Performs the actual evaluation.
	 *
	 * @return              the generated results
	 * @throws Exception    if evaluation fails
	 */
	protected abstract List<EvaluationStatistics> doCall() throws Exception;

	/**
	 * Performs the evaluation and stores the results.
	 *
	 * @return              the generated results
	 * @throws Exception    if evaluation fails
	 */
	@Override
	public List<EvaluationStatistics> call() throws Exception {
		m_Result = doCall();
		return m_Result;
	}

	/**
	 * Returns the results.
	 *
	 * @return              the results
	 */
	public List<EvaluationStatistics> getResult() {
		return m_Result;
	}
}
