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
 * ExperimentUtils.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment;

import meka.core.ThreadLimiter;
import meka.core.ThreadUtils;
import meka.experiment.evaluationstatistics.OptionalIncrementalEvaluationStatisticsHandler;
import meka.experiment.evaluators.AbstractMetaEvaluator;
import meka.experiment.evaluators.Evaluator;

/**
 * Experiment related methods.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExperimentUtils {

	/**
	 * Checks whether an Evaluator implements {@link ThreadLimiter} and uses multi-threading.
	 *
	 * @param exp       the experiment to check/update
	 * @param evaluator the current evaluator to check
	 * @return          true if ThreadLimiter evaluator present
	 */
	protected static boolean isMultiThreaded(Experiment exp, Evaluator evaluator) {
		int         numThreads;

		if (evaluator instanceof ThreadLimiter) {
			numThreads = ((ThreadLimiter) evaluator).getNumThreads();
			if (ThreadUtils.isMultiThreaded(numThreads))
				return true;
		}

		if (evaluator instanceof AbstractMetaEvaluator)
			return isMultiThreaded(exp, ((AbstractMetaEvaluator) evaluator).getEvaluator());
		else
			return false;
	}

	/**
	 * Makes sure that the experiment uses a threadsafe setup.
	 *
	 * @param exp       the experiment to check/update
	 * @param evaluator the current evaluator to check
	 */
	protected static void ensureThreadSafety(Experiment exp, Evaluator evaluator) {
		int         old;

		if (evaluator instanceof ThreadLimiter) {
			old = ((ThreadLimiter) evaluator).getNumThreads();
			if (ThreadUtils.isMultiThreaded(old)) {
				((ThreadLimiter) evaluator).setNumThreads(ThreadUtils.SEQUENTIAL);
				exp.log(evaluator.getClass().getName() + ": changed #threads from " + old + " to " + ThreadUtils.SEQUENTIAL + " "
						+ "(" + exp.getStatisticsHandler().getClass().getName() + " is not threadsafe)!");
			}
		}

		if (evaluator instanceof AbstractMetaEvaluator)
			ensureThreadSafety(exp, ((AbstractMetaEvaluator) evaluator).getEvaluator());
	}

	/**
	 * Makes sure that the experiment uses a threadsafe setup.
	 *
	 * @param exp       the experiment to check/update
	 */
	public static void ensureThreadSafety(Experiment exp) {
		boolean     old;

		// threadsafe statistics handler? don't worry then
		if (exp.getStatisticsHandler().isThreadSafe())
			return;

		if (isMultiThreaded(exp, exp.getEvaluator())) {
			// can we turn off incremental mode to take advantage of multi-threading?
			if (exp.getStatisticsHandler() instanceof OptionalIncrementalEvaluationStatisticsHandler) {
				OptionalIncrementalEvaluationStatisticsHandler optional = (OptionalIncrementalEvaluationStatisticsHandler) exp.getStatisticsHandler();
				old = optional.isIncrementalDisabled();
				optional.setIncrementalDisabled(true);
				if (optional.isThreadSafe()) {
					exp.log("Turned off incremental mode for " + optional.getClass().getName() + " to make use of multi-threading!");
					return;
				}
				else {
					optional.setIncrementalDisabled(old);
				}
			}

			ensureThreadSafety(exp, exp.getEvaluator());
		}
	}
}
