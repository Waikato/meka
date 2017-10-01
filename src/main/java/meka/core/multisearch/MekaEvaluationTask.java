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
 * MekaEvaluationTask.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package meka.core.multisearch;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Result;
import weka.classifiers.Classifier;
import weka.classifiers.meta.multisearch.AbstractEvaluationTask;
import weka.classifiers.meta.multisearch.MultiSearchCapable;
import weka.classifiers.meta.multisearch.Performance;
import weka.core.Instances;
import weka.core.SetupGenerator;
import weka.core.setupgenerator.Point;

import java.io.Serializable;

/**
 * Meka Evaluation task.
 */
public class MekaEvaluationTask
	extends AbstractEvaluationTask {

	/** the threshold option. */
	protected String m_TOP;

	/** the verbosity option. */
	protected String m_VOP;

	/**
	 * Initializes the task.
	 *
	 * @param owner		the owning MultiSearch classifier
	 * @param train		the training data
	 * @param test		the test data, can be null
	 * @param generator		the generator to use
	 * @param values		the setup values
	 * @param folds		the number of cross-validation folds
	 * @param eval		the type of evaluation
	 * @param classLabel		the class label index (0-based; if applicable)
	 */
	public MekaEvaluationTask(
		MultiSearchCapable owner, Instances train, Instances test,
		SetupGenerator generator, Point<Object> values, int folds, int eval, int classLabel) {
		super(owner, train, test, generator, values, folds, eval, classLabel);
		m_TOP = "PCut1";
		m_VOP = "3";
	}

	/**
	 * Returns whether predictions can be discarded (depends on selected measure).
	 */
	protected boolean canDiscardPredictions() {
		switch (m_Owner.getEvaluation().getSelectedTag().getID()) {
			default:
				return true;
		}
	}

	/**
	 * Performs the evaluation.
	 *
	 * @return false	if evaluation fails
	 */
	protected Boolean doRun() throws Exception{
		Point<Object>	evals;
		Result eval;
		MultiLabelClassifier classifier;
		Performance performance;
		boolean		completed;

		// setup
		evals      = m_Generator.evaluate(m_Values);
		classifier = (MultiLabelClassifier) m_Generator.setup((Serializable) m_Owner.getClassifier(), evals);

		// evaluate
		try {
			if (m_Test == null) {
				if (m_Folds >= 2) {
					eval = Evaluation.cvModel(classifier, m_Train, m_Folds, m_TOP, m_VOP);
				}
				else {
					classifier.buildClassifier(m_Train);
					eval = Evaluation.evaluateModel(classifier, m_Train, m_TOP, m_VOP);
				}
			}
			else {
				classifier.buildClassifier(m_Train);
				eval = Evaluation.evaluateModel(classifier, m_Test, m_TOP, m_VOP);
			}
			completed = true;
		}
		catch (Exception e) {
			eval = null;
			System.err.println("Encountered exception while evaluating classifier, skipping!");
			System.err.println("- Classifier: " + m_Owner.getCommandline(classifier));
			e.printStackTrace();
			completed = false;
		}

		// store performance
		performance = new Performance(
			m_Values,
			m_Owner.getFactory().newWrapper(eval),
			m_Evaluation,
			m_ClassLabel,
			(Classifier) m_Generator.setup((Serializable) m_Owner.getClassifier(), evals));
		m_Owner.getAlgorithm().addPerformance(performance, m_Folds);

		// log
		m_Owner.log(performance + ": cached=false");

		return completed;
	}
}
