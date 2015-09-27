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
import meka.core.ExceptionUtils;
import meka.core.OptionUtils;
import meka.core.Result;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.Utils;

import java.util.*;

/**
 * Evaluates the classifier using cross-validation. Order can be preserved.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class CrossValidation
  extends AbstractEvaluator
  implements Randomizable {

	private static final long serialVersionUID = 6318297857792961890L;

	/** the key for the fold. */
	public final static String KEY_FOLD = "Fold";

	/** the number of folds. */
	protected int m_NumFolds = getDefaultNumFolds();

	/** whether to preserve the order. */
	protected boolean m_PreserveOrder = false;

	/** the seed value. */
	protected int m_Seed = getDefaultSeed();

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
		return "Evaluates the classifier using cross-validation. Order can be preserved.";
	}

	/**
	 * Gets the number of folds.
	 *
	 * @return the defaut
	 */
	protected int getDefaultNumFolds() {
		return 10;
	}

	/**
	 * Set the number of folds.
	 *
	 * @param value the folds (>= 2)
	 */
	public void setNumFolds(int value) {
		if (value >= 2)
			m_NumFolds = value;
		else
			System.err.println("Number of folds must >= 2, provided: " + value);
	}

	/**
	 * Gets the number of folds
	 *
	 * @return the folds (>= 2)
	 */
	public int getNumFolds() {
		return m_NumFolds;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String numFoldsTipText() {
		return "The number of folds to use.";
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
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.add(result, super.listOptions());
		OptionUtils.addOption(result, numFoldsTipText(), "" + getDefaultNumFolds(), 'F');
		OptionUtils.addFlag(result, preserveOrderTipText(), 'O');
		OptionUtils.addOption(result, seedTipText(), "" + getDefaultSeed(), 'S');
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
		setNumFolds(OptionUtils.parse(options, 'F', getDefaultNumFolds()));
		setPreserveOrder(Utils.getFlag('O', options));
		setSeed(OptionUtils.parse(options, 'S', getDefaultSeed()));
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
		OptionUtils.add(result, 'F', getNumFolds());
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
		EvaluationStatistics        stats;
		Instances                   train;
		Instances                   test;
		Result                      res;
		int                         i;
		Random                      rand;

		result = new ArrayList<>();
		rand   = new Random(m_Seed);
		for (i = 1; i <= m_NumFolds; i++) {
			log("Fold: " + i);
			if (m_PreserveOrder)
				train = dataset.trainCV(m_NumFolds, i - 1);
			else
				train = dataset.trainCV(m_NumFolds, i - 1, rand);
			test = dataset.testCV(m_NumFolds, i - 1);
			try {
				res = Evaluation.evaluateModel(classifier, train, test, m_TOP, m_VOP);
				stats = new EvaluationStatistics(classifier, dataset, res);
				stats.put(KEY_FOLD, i);
				result.add(stats);
			}
			catch (Exception e) {
				ExceptionUtils.handleException(
						this,
						"Failed to evaluate dataset '" + dataset.relationName() + "' with classifier: " + Utils.toCommandLine(classifier), e);
				break;
			}

			if (m_Stopped)
				break;
		}

		if (m_Stopped)
			result.clear();

		return result;
	}
}
