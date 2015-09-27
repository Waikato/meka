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
 * TrainTestSplit.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluators;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.ExceptionUtils;
import meka.core.OptionUtils;
import meka.core.Result;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Evaluates the classifier on a train/test split. Order can be preserved.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class TrainTestSplit
  implements Evaluator, Randomizable {

	private static final long serialVersionUID = 6318297857792961890L;

	/** percentage to use for training. */
	protected double m_TrainPercentage = 67.0;

	/** whether to preserve the order. */
	protected boolean m_PreserveOrder = false;

	/** the seed value. */
	protected int m_Seed = 0;

	/** the threshold option. */
	protected String m_TOP = "PCut1";

	/** the verbosity option. */
	protected String m_VOP = "3";

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Evaluates the classifier on a train/test split. Order can be preserved.";
	}

	/**
	 * Set the percentage to use for training.
	 *
	 * @param value the percentage (0-100)
	 */
	public void setTrainPercentage(double value) {
		if ((value > 0) && (value < 100))
			m_TrainPercentage = value;
		else
			System.err.println("Train percentage must satisfy 0 < x < 100, provided: " + value);
	}

	/**
	 * Gets the percentage to use for training.
	 *
	 * @return the percentage (0-100)
	 */
	public double getTrainPercentage() {
		return m_TrainPercentage;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String trainPercentageTipText() {
		return "The percentage of the dataset to use for training.";
	}

	/**
	 * Sets whether to preserve the order instead of randomizing the data.
	 *
	 * @param value true if to preserve the order
	 */
	public void setPreserveOrder(boolean value) {
		m_PreserveOrder = value;
	}

	/**
	 * Returns whether to preserve the order instead of randomizing the data.
	 *
	 * @return true if to preserve the order
	 */
	public boolean getPreserveOrder() {
		return m_PreserveOrder;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String preserveOrderTipText() {
		return "If enabled, no randomization is occurring and the order in the data is preserved.";
	}

	/**
	 * Set the seed for random number generation.
	 *
	 * @param value the seed
	 */
	@Override
	public void setSeed(int value) {
		m_Seed = value;
	}

	/**
	 * Gets the seed for the random number generations
	 *
	 * @return the seed for the random number generation
	 */
	@Override
	public int getSeed() {
		return m_Seed;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String seedTipText() {
		return "The seed to use for randomization.";
	}

	/**
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.addOption(result, trainPercentageTipText(), "67", 'P');
		OptionUtils.addFlag(result, preserveOrderTipText(), 'O');
		OptionUtils.addOption(result, seedTipText(), "0", 'S');
		return OptionUtils.toEnumeration(result);
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options to parse
	 * @throws Exception    if parsing fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		setTrainPercentage(OptionUtils.parse(options, 'P', 67.0));
		setPreserveOrder(Utils.getFlag('O', options));
		setSeed(OptionUtils.parse(options, 'S', 0));
	}

	/**
	 * Returns the options.
	 *
	 * @return              the current options
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'P', getTrainPercentage());
		OptionUtils.add(result, 'O', getPreserveOrder());
		OptionUtils.add(result, 'S', getSeed());
		return OptionUtils.toArray(result);
	}

	/**
	 * Returns the evaluation statistics generated for the dataset.
	 *
	 * @param classifier    the classifier to evaluate
	 * @param dataset       the dataset to evaluate on
	 * @return              the statistics
	 */
	@Override
	public List<EvaluationStatistics> evaluate(MultiLabelClassifier classifier, Instances dataset) {
		List<EvaluationStatistics>  result;
		int                         trainSize;
		Instances                   train;
		Instances                   test;
		Result                      res;

		result    = new ArrayList<>();
		trainSize = (int) (dataset.numInstances() * m_TrainPercentage / 100.0);
		train     = new Instances(dataset, 0, trainSize);
		test      = new Instances(dataset, trainSize, dataset.numInstances() - trainSize);
		try {
			res = Evaluation.evaluateModel(classifier, train, test, m_TOP, m_VOP);
			result.add(new EvaluationStatistics(classifier, dataset, res));
		}
		catch (Exception e) {
			ExceptionUtils.handleException(
					this,
					"Failed to evaluate dataset '" + dataset.relationName() + "' with classifier: " + Utils.toCommandLine(classifier), e);
		}

		return result;
	}
}
