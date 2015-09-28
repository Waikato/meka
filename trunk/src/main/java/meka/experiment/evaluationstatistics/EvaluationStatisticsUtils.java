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
 * EvaluationStatisticsUtils.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.RepeatedRuns;

import java.util.*;

/**
 * Helper class for stats related operations.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class EvaluationStatisticsUtils {

	/**
	 * Returns all the keys of all the statistics.
	 *
	 * @param stats     the stats to inspect
	 * @param sort      whether to sort the keys alphabetically
	 * @return          the keys
	 */
	public static List<String> keys(List<EvaluationStatistics> stats, boolean sort) {
		List<String>        result;
		HashSet<String>     keys;

		keys = new HashSet<>();
		for (EvaluationStatistics stat: stats)
			keys.addAll(stat.keySet());

		result = new ArrayList<>(keys);
		if (sort)
			Collections.sort(result);

		return result;
	}

	/**
	 * Creates a list of headers (= stats keys) from the provided statistics.
	 *
	 * @param stats                 the stats to use
	 * @param moveRunFold           whether to moved "Fold" and "Run" to the start
	 * @param addClassifierRelation whether to add "Classifier" and "Relation"
	 * @return                      the generated header list
	 */
	public static List<String> header(List<EvaluationStatistics> stats, boolean moveRunFold, boolean addClassifierRelation) {
		List<String>    result;

		result = keys(stats, true);

		if (moveRunFold) {
			if (result.contains(CrossValidation.KEY_FOLD)) {
				result.remove(CrossValidation.KEY_FOLD);
				result.add(0, CrossValidation.KEY_FOLD);
			}
			if (result.contains(RepeatedRuns.KEY_RUN)) {
				result.remove(RepeatedRuns.KEY_RUN);
				result.add(0, RepeatedRuns.KEY_RUN);
			}
		}
		if (addClassifierRelation) {
			result.add(0, EvaluationStatistics.KEY_RELATION);
			result.add(0, EvaluationStatistics.KEY_CLASSIFIER);
		}

		return result;
	}
}
