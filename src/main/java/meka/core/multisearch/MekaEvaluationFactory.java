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

/*
 * MekaEvaluationFactory.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package meka.core.multisearch;

import meka.core.Result;
import weka.classifiers.meta.multisearch.AbstractEvaluationFactory;
import weka.classifiers.meta.multisearch.MultiSearchCapable;
import weka.core.Instances;
import weka.core.SetupGenerator;
import weka.core.setupgenerator.Point;

/**
 * Meka factory.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MekaEvaluationFactory
	extends AbstractEvaluationFactory<MekaEvaluationMetrics, MekaEvaluationWrapper, MekaEvaluationTask, Result> {

	private static final long serialVersionUID = -7535032839072532838L;

	/**
	 * Returns a new metrics instance.
	 *
	 * @return the metrics
	 */
	@Override
	public MekaEvaluationMetrics newMetrics() {
		return new MekaEvaluationMetrics();
	}

	/**
	 * Returns a new wrapper.
	 *
	 * @param eval the evaluation to wrap
	 * @return the wrapper
	 */
	@Override
	public MekaEvaluationWrapper newWrapper(Result eval) {
		return new MekaEvaluationWrapper(eval, newMetrics());
	}

	/**
	 * Returns a new task.
	 *
	 * @param owner      the owning search
	 * @param train      the training data
	 * @param test       the test data
	 * @param generator  the generator
	 * @param values     the values
	 * @param folds      the number of folds
	 * @param eval       the evaluation
	 * @param classLabel the class label index (0-based; if applicable)
	 * @return the task
	 */
	@Override
	public MekaEvaluationTask newTask(MultiSearchCapable owner, Instances train, Instances test, SetupGenerator generator, Point<Object> values, int folds, int eval, int classLabel) {
		return new MekaEvaluationTask(owner, train, test, generator, values, folds, eval, classLabel);
	}
}
