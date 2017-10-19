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
 * RepeatedRuns.java
 * Copyright (C) 2015-2017 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluators;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.core.ThreadLimiter;
import meka.core.ThreadUtils;
import meka.events.LogListener;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Repeatedly executes the base evaluator.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class RepeatedRuns
		extends AbstractMetaEvaluator
		implements ThreadLimiter {

	private static final long serialVersionUID = -1230107553603089463L;

	/** the key for the run number. */
	public final static String KEY_RUN = "Run";

	/** the lower number of runs (included). */
	protected int m_LowerRuns = getDefaultLowerRuns();

	/** the upper number of runs (included). */
	protected int m_UpperRuns = getDefaultUpperRuns();

	/** the number of threads to use for parallel execution. */
	protected int m_NumThreads = getDefaultNumThreads();

	/** the actual number of threads to use. */
	protected int m_ActualNumThreads;

	/** the executor service to use for parallel execution. */
	protected transient ExecutorService m_Executor;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Performs repeated runs of the base evaluator. If the base evaluator is randomizable, "
				+ "the run number is used as seed. The base evaluator gets initialized before each "
				+ "run.";
	}

	/**
	 * Returns the default evaluator to use.
	 *
	 * @return          the default
	 */
	@Override
	protected Evaluator getDefaultEvaluator() {
		return new CrossValidation();
	}

	/**
	 * Returns the default lower number of runs to perform.
	 *
	 * @return the default
	 */
	protected int getDefaultLowerRuns() {
		return 1;
	}

	/**
	 * Sets the lower number of runs to perform (included).
	 *
	 * @param value the number of runs
	 */
	public void setLowerRuns(int value) {
		m_LowerRuns = value;
	}

	/**
	 * Returns the lower number of runs to perform (included).
	 *
	 * @return the number of runs
	 */
	public int getLowerRuns() {
		return m_LowerRuns;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String lowerRunsTipText() {
		return "The lower number of runs to perform (included).";
	}

	/**
	 * Returns the default upper number of runs to perform.
	 *
	 * @return the default
	 */
	protected int getDefaultUpperRuns() {
		return 10;
	}

	/**
	 * Sets the upper number of runs to perform (included).
	 *
	 * @param value the number of runs
	 */
	public void setUpperRuns(int value) {
		m_UpperRuns = value;
	}

	/**
	 * Returns the upper number of runs to perform (included).
	 *
	 * @return the number of runs
	 */
	public int getUpperRuns() {
		return m_UpperRuns;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String upperRunsTipText() {
		return "The upper number of runs to perform (included).";
	}

	/**
	 * Returns the default number of threads to use.
	 *
	 * @return 		the number of threads: -1 = # of CPUs/cores; 0/1 = sequential execution
	 */
	protected int getDefaultNumThreads() {
		return ThreadUtils.SEQUENTIAL;
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
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.add(result, super.listOptions());
		OptionUtils.addOption(result, lowerRunsTipText(), "" + getDefaultLowerRuns(), "lower");
		OptionUtils.addOption(result, upperRunsTipText(), "" + getDefaultUpperRuns(), "upper");
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
		setLowerRuns(OptionUtils.parse(options, "lower", getDefaultLowerRuns()));
		setUpperRuns(OptionUtils.parse(options, "upper", getDefaultUpperRuns()));
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
		OptionUtils.add(result, "lower", getLowerRuns());
		OptionUtils.add(result, "upper", getUpperRuns());
		OptionUtils.add(result, "num-threads", getNumThreads());
		return OptionUtils.toArray(result);
	}

	/**
	 * Executes the runs in sequential order.
	 *
	 * @param classifier    the classifier to evaluate
	 * @param dataset       the dataset to evaluate on
	 * @return              the statistics
	 */
	protected List<EvaluationStatistics> evaluateSequential(MultiLabelClassifier classifier, Instances dataset) {
		List<EvaluationStatistics>  result;
		List<EvaluationStatistics>  stats;
		int                         i;

		result = new ArrayList<>();

		for (i = m_LowerRuns; i <= m_UpperRuns; i++) {
			log("Run: " + i);
			Instances data = new Instances(dataset);
			data.randomize(new Random(i));
			if (m_Evaluator instanceof Randomizable)
				((Randomizable) m_Evaluator).setSeed(i);
			m_Evaluator.initialize();
			stats = m_Evaluator.evaluate(classifier, data);
			if (stats != null) {
				for (EvaluationStatistics stat: stats) {
					stat.put(KEY_RUN, i);
					result.add(stat);
				}
			}
			if (m_Stopped)
				break;
		}

		return result;
	}

	/**
	 * Executes the runs in sequential order.
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

		result = new ArrayList<>();

		debug("pre: create jobs");
		jobs = new ArrayList<>();
		for (i = m_LowerRuns; i <= m_UpperRuns; i++) {
			final int index = i;
			job = new EvaluatorJob() {
				protected List<EvaluationStatistics> doCall() throws Exception {
					log("Executing run #" + index + "...");
					Evaluator evaluator = (Evaluator) OptionUtils.shallowCopy(m_Evaluator);
					for (LogListener l: m_LogListeners)
						evaluator.addLogListener(l);
					if (evaluator instanceof Randomizable)
						((Randomizable) evaluator).setSeed(index);
					evaluator.initialize();
					Instances data = new Instances(dataset);
					data.randomize(new Random(index));
					List<EvaluationStatistics> stats = m_Evaluator.evaluate(classifier, data);
					for (LogListener l: m_LogListeners)
						evaluator.removeLogListener(l);
					log("...finished run #" + index + ((stats == null) ? "" : " with error"));
					return stats;
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

		m_ActualNumThreads = ThreadUtils.getActualNumThreads(m_NumThreads, m_UpperRuns - m_LowerRuns + 1);

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
