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
 * CrossValidation.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluators;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.core.Result;
import meka.core.ThreadLimiter;
import meka.core.ThreadUtils;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.Utils;

import java.util.*;
import java.util.concurrent.*;

/**
 * Evaluates the classifier using cross-validation. Order can be preserved.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class CrossValidation
  extends AbstractEvaluator
  implements Randomizable, ThreadLimiter {

	private static final long serialVersionUID = 6318297857792961890L;

	/** the key for the fold. */
	public final static String KEY_FOLD = "Fold";

	/** the number of folds. */
	protected int m_NumFolds = getDefaultNumFolds();

	/** whether to preserve the order. */
	protected boolean m_PreserveOrder = false;

	/** the seed value. */
	protected int m_Seed = getDefaultSeed();

	/** the number of threads to use for parallel execution. */
	protected int m_NumThreads = getDefaultNumThreads();

	/** the actual number of threads to use. */
	protected int m_ActualNumThreads;

	/** the executor service to use for parallel execution. */
	protected transient ExecutorService m_Executor;

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
	 * Returns the default number of threads to use.
	 *
	 * @return 		the number of threads: -1 = # of CPUs/cores; 0/1 = sequential execution
	 */
	protected int getDefaultNumThreads() {
		return ThreadUtils.ALL;
	}

	/**
	 * Sets the number of threads to use.
	 *
	 * @param value 	the number of threads: -1 = # of CPUs/cores; 0/1 = sequential execution
	 */
	public void setNumThreads(int value) {
		if (value >= -1) {
			m_NumThreads = value;
		}
		else {
			log("Number of threads must be >= -1, provided: " + value);
		}
	}

	/**
	 * Returns the number of threads to use.
	 *
	 * @return 		the number of threads: -1 = # of CPUs/cores; 0/1 = sequential execution
	 */
	public int getNumThreads() {
		return m_NumThreads;
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the GUI or for listing the options.
	 */
	public String numThreadsTipText() {
		return "The number of threads to use ; -1 = number of CPUs/cores; 0 or 1 = sequential execution.";
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
		OptionUtils.addOption(result, numFoldsTipText(), "" + getDefaultNumFolds(), 'F');
		OptionUtils.addFlag(result, preserveOrderTipText(), 'O');
		OptionUtils.addOption(result, seedTipText(), "" + getDefaultSeed(), 'S');
		OptionUtils.addOption(result, thresholdTipText(), "" + getDefaultThreshold(), 'T');
		OptionUtils.addOption(result, verbosityTipText(), "" + getDefaultVerbosity(), 'V');
		OptionUtils.addOption(result, numThreadsTipText(), "" + getDefaultNumThreads(), "num-threads");
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
		setThreshold(OptionUtils.parse(options, 'T', getDefaultThreshold()));
		setVerbosity(OptionUtils.parse(options, 'V', getDefaultVerbosity()));
		setNumThreads(OptionUtils.parse(options, "num-threads", getDefaultNumThreads()));
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
		OptionUtils.add(result, 'T', getThreshold());
		OptionUtils.add(result, 'V', getVerbosity());
		OptionUtils.add(result, "num-threads", getNumThreads());
		return OptionUtils.toArray(result);
	}

	/**
	 * Returns the evaluation statistics generated for the dataset (sequential execution).
	 *
	 * @param classifier    the classifier to evaluate
	 * @param dataset       the dataset to evaluate on
	 * @return              the statistics
	 */
	protected List<EvaluationStatistics> evaluateSequential(MultiLabelClassifier classifier, Instances dataset) {
		List<EvaluationStatistics>  result;
		EvaluationStatistics        stats;
		Instances                   train;
		Instances                   test;
		Result                      res;
		int                         i;
		Random                      rand;
		MultiLabelClassifier        current;

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
				current = (MultiLabelClassifier) OptionUtils.shallowCopy(classifier);
				res = Evaluation.evaluateModel(current, train, test, m_Threshold, m_Verbosity);
				stats = new EvaluationStatistics(classifier, dataset, res);
				stats.put(KEY_FOLD, i);
				result.add(stats);
			}
			catch (Exception e) {
				handleException(
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

	/**
	 * Returns the evaluation statistics generated for the dataset (parallel execution).
	 *
	 * @param classifier    the classifier to evaluate
	 * @param dataset       the dataset to evaluate on
	 * @return              the statistics
	 */
	protected List<EvaluationStatistics> evaluateParallel(final MultiLabelClassifier classifier, final Instances dataset) {
		List<EvaluationStatistics>      result;
		ArrayList<EvaluatorJob>	        jobs;
		EvaluatorJob		            job;
		int                             i;
		Random                          rand;

		result = new ArrayList<>();

		debug("pre: create jobs");
		jobs = new ArrayList<>();
		rand = new Random(m_Seed);
		for (i = 1; i <= m_NumFolds; i++) {
			final int index = i;
			final Instances train;
			final Instances test;
			final MultiLabelClassifier current;
			if (m_PreserveOrder)
				train = dataset.trainCV(m_NumFolds, index - 1);
			else
				train = dataset.trainCV(m_NumFolds, index - 1, rand);
			test = dataset.testCV(m_NumFolds, index - 1);
			current = (MultiLabelClassifier) OptionUtils.shallowCopy(classifier);
			job = new EvaluatorJob() {
				protected List<EvaluationStatistics> doCall() throws Exception {
					List<EvaluationStatistics> result = new ArrayList<>();
					log("Executing fold #" + index + "...");
					try {
						Result res = Evaluation.evaluateModel(current, train, test, m_Threshold, m_Verbosity);
						EvaluationStatistics stats = new EvaluationStatistics(classifier, dataset, res);
						stats.put(KEY_FOLD, index);
						result.add(stats);
					}
					catch (Exception e) {
						handleException(
								"Failed to evaluate dataset '" + dataset.relationName() + "' with classifier: " + Utils.toCommandLine(classifier), e);
					}
					log("...finished fold #" + index);
					return result;
				}
			};
			jobs.add(job);
		}
		debug("post: create jobs");

		// execute jobs
		m_Executor = Executors.newFixedThreadPool(m_ActualNumThreads);
		debug("pre: submit");
		try {
			for (i = 0; i < jobs.size(); i++)
				m_Executor.submit(jobs.get(i));
		}
		catch (RejectedExecutionException e) {
			// ignored
		}
		catch (Exception e) {
			handleException("Failed to start up jobs", e);
		}
		debug("post: submit");

		debug("pre: shutdown");
		m_Executor.shutdown();
		debug("post: shutdown");

		// wait for threads to finish
		debug("pre: wait");
		while (!m_Executor.isTerminated()) {
			try {
				m_Executor.awaitTermination(100, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				// ignored
			}
			catch (Exception e) {
				handleException("Failed to await termination", e);
			}
		}
		debug("post: wait");

		// collect results
		debug("pre: collect");
		for (i = 0; i < jobs.size(); i++)
			result.addAll(jobs.get(i).getResult());
		debug("post: collect");

		return result;
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

		m_ActualNumThreads = ThreadUtils.getActualNumThreads(m_NumThreads, m_NumFolds);

		log("Number of threads (" + ThreadUtils.SEQUENTIAL + " = sequential): " + m_ActualNumThreads);
		if (m_ActualNumThreads == ThreadUtils.SEQUENTIAL)
			result = evaluateSequential(classifier, dataset);
		else
			result = evaluateParallel(classifier, dataset);

		if (m_Stopped)
			result.clear();

		return result;
	}

	/**
	 * Stops the evaluation, if possible.
	 */
	@Override
	public void stop() {
		if (m_Executor != null) {
			debug("pre: shutdownNow");
			m_Executor.shutdownNow();
			debug("post: shutdownNow");
		}
		super.stop();
	}
}
