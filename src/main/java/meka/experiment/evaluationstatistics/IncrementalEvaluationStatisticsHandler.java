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
 * IncrementalEvaluationStatisticsHandler.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.core.Instances;

import java.util.List;

/**
 * For handlers that support incremental writes.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface IncrementalEvaluationStatisticsHandler
  extends EvaluationStatisticsHandler {

	/**
	 * Returns whether the handler supports incremental write.
	 *
	 * @return      true if supported
	 */
	public boolean supportsIncrementalUpdate();

	/**
	 * Checks whether the specified combination of classifier and dataset is required for evaluation
	 * or already present from previous evaluation.
	 *
	 * @param classifier    the classifier to check
	 * @param dataset       the dataset to check
	 * @return              true if it needs evaluating
	 */
	public boolean requires(MultiLabelClassifier classifier, Instances dataset);

	/**
	 * Adds the given statistics.
	 *
	 * @param stats         the statistics to store
	 * @return              null if successfully stored, otherwise error message
	 */
	public String append(List<EvaluationStatistics> stats);
}
