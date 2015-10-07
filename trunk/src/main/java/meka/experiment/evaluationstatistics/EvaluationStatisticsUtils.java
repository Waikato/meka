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

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.A;
import meka.core.OptionUtils;
import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.RepeatedRuns;
import weka.core.Instances;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
	 * Returns all the unique classifiers of all the statistics.
	 *
	 * @param stats     the stats to inspect
	 * @param sort      whether to sort the classifiers alphabetically
	 * @return          the classifiers
	 */
	public static List<MultiLabelClassifier> classifiers(List<EvaluationStatistics> stats, boolean sort) {
		List<MultiLabelClassifier>      result;
		List<String>                    cmdlines;

		result   = new ArrayList<>();
		cmdlines = commandLines(stats, sort);
		for (String cmdline: cmdlines) {
			try {
				result.add(OptionUtils.fromCommandLine(MultiLabelClassifier.class, cmdline));
			}
			catch (Exception e) {
				System.err.println("Failed to instantiate command-line: " + cmdline);
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Returns all the unique classifiers of all the statistics.
	 *
	 * @param stats     the stats to inspect
	 * @param sort      whether to sort the classifiers alphabetically
	 * @return          the command-lines
	 */
	public static List<String> commandLines(List<EvaluationStatistics> stats, boolean sort) {
		List<String>        result;

		result = new ArrayList<>();
		for (EvaluationStatistics stat: stats) {
			if (!result.contains(stat.getCommandLine()))
				result.add(stat.getCommandLine());
		}

		if (sort)
			Collections.sort(result);

		return result;
	}

	/**
	 * Returns all the values of a specific measurement for the specified classifier/dataset combination.
	 *
	 * @param stats         the stats to inspect
	 * @param classifier    the classifier to look for
	 * @param dataset       the dataset to look for
	 * @param measurement   the measurement to retrieve
	 * @return              the values
	 */
	public static List<Number> measurements(List<EvaluationStatistics> stats, MultiLabelClassifier classifier, Instances dataset, String measurement) {
		return measurements(stats, OptionUtils.toCommandLine(classifier), dataset.relationName(), measurement);
	}

	/**
	 * Returns all the values of a specific measurement for the specified classifier/dataset combination.
	 *
	 * @param stats         the stats to inspect
	 * @param classifier    the classifier to look for (commandline)
	 * @param dataset       the dataset to look for (relation name)
	 * @param measurement   the measurement to retrieve
	 * @return              the values
	 */
	public static List<Number> measurements(List<EvaluationStatistics> stats, String classifier, String dataset, String measurement) {
		List<Number>    result;

		result = new ArrayList<>();
		for (EvaluationStatistics stat: stats) {
			if (stat.getCommandLine().equals(classifier) && stat.getRelation().equals(dataset)) {
				if (stat.containsKey(measurement))
					result.add(stat.get(measurement));
			}
		}

		return result;
	}

	/*
	 * Nemenyi Test - NOT YET IMPLEMENTED
	 *
	 * @param stats         the stats to inspect
	 * @param measurement   the measurement to run the test on
	 * @return              the Ranks // the Nemenyi test results
	 */

	/**
	 * Value Matrix
	 */
	public static double[][] valueMatrix(List<EvaluationStatistics> stats, String measurement) {
		List<Number>    result;

		List<String> classifiers = EvaluationStatisticsUtils.commandLines(stats, true);
		List<String> relations   = EvaluationStatisticsUtils.relations(stats, true);

		int N = relations.size();
		int k = classifiers.size();
		double V[][] = new double[N][k];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < k; j++) {
				List<Number> measurements = EvaluationStatisticsUtils.measurements(stats, classifiers.get(j), relations.get(i), measurement);
				V[i][j] = (double)measurements.get(0);
			}
		}
		return V;
	}

	/**
	 * Rank Matrix
	 */
	public static int[][] rankMatrix(List<EvaluationStatistics> stats, String measurement) {

		double V[][] = valueMatrix(stats,measurement);
		int N = V.length;
		int k = V[0].length;

		int R[][] = new int[N][k];
		for (int i = 0; i < N; i++) {
			R[i] = Utils.sort(V[i]);
			// add 1 to each
			for (int j = 0; j < k; j++) {
				R[i][j]++;
			}
		}

		return R;
	}

	/**
	 * Returns all available measurements.
	 *
	 * @param stats         the stats to inspect
	 * @return              the values
	 */
	public static List<String> measurements(List<EvaluationStatistics> stats) {
		List<String>    result;
		HashSet<String> set;

		result = new ArrayList<>();
		set    = new HashSet<>();
		for (EvaluationStatistics stat: stats)
			set.addAll(stat.keySet());

		result.addAll(set);
		Collections.sort(result);

		return result;
	}

	/**
	 * Returns all the unique relations of all the statistics.
	 *
	 * @param stats     the stats to inspect
	 * @param sort      whether to sort the relations alphabetically
	 * @return          the relations
	 */
	public static List<String> relations(List<EvaluationStatistics> stats, boolean sort) {
		List<String>        result;

		result = new ArrayList<>();
		for (EvaluationStatistics stat: stats) {
			if (!result.contains(stat.getRelation()))
				result.add(stat.getRelation());
		}

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
	public static List<String> headers(List<EvaluationStatistics> stats, boolean moveRunFold, boolean addClassifierRelation) {
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
