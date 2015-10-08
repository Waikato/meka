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
 * PercentageSplit.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluators;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.core.Result;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.Utils;

import java.util.*;

/**
 * Evaluates the classifier on a percentage split. Order can be preserved.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PercentageSplit
  extends AbstractEvaluator
  implements Randomizable {

	private static final long serialVersionUID = 6318297857792961890L;

	/** percentage to use for training. */
	protected double m_TrainPercentage = getDefaultTrainPercentage();

	/** whether to preserve the order. */
	protected boolean m_PreserveOrder = false;

	/** the seed value. */
	protected int m_Seed = getDefaultSeed();

	/** the threshold option. */
	protected String m_Threshold = getDefaultThreshold();

	/** the verbosity option. */
	protected String m_Verbosity = getDefaultVerbosity();

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Evaluates the classifier on a percentage split. Order can be preserved.";
	}

	/**
	 * Gets the default percentage to use for training.
	 *
	 * @return the defaut
	 */
	protected double getDefaultTrainPercentage() {
		return 67.0;
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
	 * Gets the default seed for the random number generations
	 *
	 * @return the default
	 */
	protected int getDefaultSeed() {
		return 0;
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
	 * Gets the default threshold option.
	 *
	 * @return the defaut
	 */
	protected String getDefaultThreshold() {
		return "PCut1";
	}

	/**
	 * Set the threshold option.
	 *
	 * @param value the option
	 */
	public void setThreshold(String value) {
		m_Threshold = value;
	}

	/**
	 * Gets the threshold option.
	 *
	 * @return the option
	 */
	public String getThreshold() {
		return m_Threshold;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String thresholdTipText() {
		return "The threshold option.";
	}

	/**
	 * Gets the default threshold option.
	 *
	 * @return the defaut
	 */
	protected String getDefaultVerbosity() {
		return "3";
	}

	/**
	 * Set the verbosity option.
	 *
	 * @param value the option
	 */
	public void setVerbosity(String value) {
		m_Verbosity = value;
	}

	/**
	 * Gets the verbosity option.
	 *
	 * @return the option
	 */
	public String getVerbosity() {
		return m_Verbosity;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String verbosityTipText() {
		return "The verbosity option.";
	}

	/**
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.add(result, super.listOptions());
		OptionUtils.addOption(result, trainPercentageTipText(), "" + getDefaultTrainPercentage(), 'P');
		OptionUtils.addFlag(result, preserveOrderTipText(), 'O');
		OptionUtils.addOption(result, seedTipText(), "" + getDefaultSeed(), 'S');
		OptionUtils.addOption(result, thresholdTipText(), "" + getDefaultThreshold(), 'T');
		OptionUtils.addOption(result, verbosityTipText(), "" + getDefaultVerbosity(), 'V');
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
		setTrainPercentage(OptionUtils.parse(options, 'P', getDefaultTrainPercentage()));
		setPreserveOrder(Utils.getFlag('O', options));
		setSeed(OptionUtils.parse(options, 'S', getDefaultSeed()));
		setThreshold(OptionUtils.parse(options, 'T', getDefaultThreshold()));
		setVerbosity(OptionUtils.parse(options, 'V', getDefaultVerbosity()));
		super.setOptions(options);
	}

	/**
	 * Returns the options.
	 *
	 * @return              the current options
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, super.getOptions());
		OptionUtils.add(result, 'P', getTrainPercentage());
		OptionUtils.add(result, 'O', getPreserveOrder());
		OptionUtils.add(result, 'S', getSeed());
		OptionUtils.add(result, 'T', getThreshold());
		OptionUtils.add(result, 'V', getVerbosity());
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

		result = new ArrayList<>();
		if (!m_PreserveOrder) {
			dataset = new Instances(dataset);
			dataset.randomize(new Random(m_Seed));
		}
		trainSize = (int) (dataset.numInstances() * m_TrainPercentage / 100.0);
		train     = new Instances(dataset, 0, trainSize);
		test      = new Instances(dataset, trainSize, dataset.numInstances() - trainSize);
		try {
			res = Evaluation.evaluateModel(classifier, train, test, m_Threshold, m_Verbosity);
			result.add(new EvaluationStatistics(classifier, dataset, res));
		}
		catch (Exception e) {
			handleException(
					"Failed to evaluate dataset '" + dataset.relationName() + "' with classifier: " + Utils.toCommandLine(classifier), e);
		}

		if (m_Stopped)
			result.clear();

		return result;
	}
}
