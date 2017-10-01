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
 * MekaEvaluationWrapper.java
 * Copyright (C) 2015-2016 University of Waikato, Hamilton, NZ
 */

package meka.core.multisearch;

import meka.core.Result;
import weka.classifiers.meta.multisearch.AbstractEvaluationWrapper;

/**
 * Wrapper for the Result class.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MekaEvaluationWrapper
	extends AbstractEvaluationWrapper<Result, MekaEvaluationMetrics> {

	private static final long serialVersionUID = 931329614934902835L;

	/** the evaluation object. */
	protected Result m_Evaluation;

	/**
	 * Initializes the wrapper.
	 *
	 * @param eval    the evaluation to wrap
	 * @param metrics the metrics to use
	 */
	public MekaEvaluationWrapper(Result eval, MekaEvaluationMetrics metrics) {
		super(eval, metrics);
	}

	/**
	 * Sets the evaluation object to use.
	 *
	 * @param eval the evaluation
	 */
	@Override
	protected void setEvaluation(Result eval) {
		m_Evaluation = eval;
	}

	/**
	 * Returns the metric for the given ID.
	 *
	 * @param id         the id to get the metric for
	 * @param classLabel the class label index for which to return metric (if applicable)
	 * @return the metric
	 */
	public double getMetric(int id, int classLabel) {
		try {
			switch (id) {
				case MekaEvaluationMetrics.EVALUATION_ACC:
					return (Double) m_Evaluation.output.get("Accuracy");
				case MekaEvaluationMetrics.EVALUATION_JACCARDINDEX:
					return (Double) m_Evaluation.output.get("Jaccard index");
				case MekaEvaluationMetrics.EVALUATION_HAMMINGSCORE:
					return (Double) m_Evaluation.output.get("Hamming score");
				case MekaEvaluationMetrics.EVALUATION_EXACTMATCH:
					return (Double) m_Evaluation.output.get("Exact match");
				case MekaEvaluationMetrics.EVALUATION_JACCARDDISTANCE:
					return (Double) m_Evaluation.output.get("Jaccard distance");
				case MekaEvaluationMetrics.EVALUATION_HAMMINGLOSS:
					return (Double) m_Evaluation.output.get("Hamming loss");
				case MekaEvaluationMetrics.EVALUATION_ZEROONELOSS:
					return (Double) m_Evaluation.output.get("ZeroOne loss");
				case MekaEvaluationMetrics.EVALUATION_HARMONICSCORE:
					return (Double) m_Evaluation.output.get("Harmonic score");
				case MekaEvaluationMetrics.EVALUATION_ONEERROR:
					return (Double) m_Evaluation.output.get("One error");
				case MekaEvaluationMetrics.EVALUATION_RANKLOSS:
					return (Double) m_Evaluation.output.get("Rank loss");
				case MekaEvaluationMetrics.EVALUATION_AVGPRECISION:
					return (Double) m_Evaluation.output.get("Avg precision");
				case MekaEvaluationMetrics.EVALUATION_LOGLOSSLIML:
					return (Double) m_Evaluation.output.get("Log Loss (lim. L)");
				case MekaEvaluationMetrics.EVALUATION_LOGLOSSLIMD:
					return (Double) m_Evaluation.output.get("Log Loss (lim. D)");
				case MekaEvaluationMetrics.EVALUATION_F1MICRO:
					return (Double) m_Evaluation.output.get("F1 (micro averaged)");
				case MekaEvaluationMetrics.EVALUATION_F1MACROEXAMPLE:
					return (Double) m_Evaluation.output.get("F1 (macro averaged by example)");
				case MekaEvaluationMetrics.EVALUATION_F1MACROLABEL:
					return (Double) m_Evaluation.output.get("F1 (macro averaged by label)");
				case MekaEvaluationMetrics.EVALUATION_AUPRCMACRO:
					return (Double) m_Evaluation.output.get("AUPRC (macro averaged)");
				case MekaEvaluationMetrics.EVALUATION_AUROCMACRO:
					return (Double) m_Evaluation.output.get("AUROC (macro averaged)");
				case MekaEvaluationMetrics.EVALUATION_LABELCARDINALITY:
					return (Double) m_Evaluation.output.get("Label cardinality (predicted)");
				case MekaEvaluationMetrics.EVALUATION_LEVENSHTEINDISTANCE:
					return (Double) m_Evaluation.output.get("Levenshtein distance");
				default:
					return Double.NaN;
			}
		}
		catch (Exception e) {
			return Double.NaN;
		}
	}
}
