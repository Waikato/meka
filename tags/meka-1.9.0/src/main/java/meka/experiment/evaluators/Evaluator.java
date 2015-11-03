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
 * Evaluator.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluators;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.events.LogSupporter;
import weka.core.Instances;
import weka.core.OptionHandler;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for classes that evaluate on a dataset.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface Evaluator
  extends OptionHandler, Serializable, LogSupporter {

	/**
	 * Initializes the evaluator.
	 *
	 * @return      null if successfully initialized, otherwise error message
	 */
	public String initialize();

	/**
	 * Returns the evaluation statistics generated for the dataset.
	 *
	 * @param classifier    the classifier to evaluate
	 * @param dataset       the dataset to evaluate on
	 * @return              the statistics
	 */
	public List<EvaluationStatistics> evaluate(MultiLabelClassifier classifier, Instances dataset);

	/**
	 * Stops the evaluation, if possible.
	 */
	public void stop();
}
