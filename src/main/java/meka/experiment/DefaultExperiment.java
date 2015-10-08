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
 * DefaultExperiment.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.events.LogListener;
import meka.events.LogObject;
import meka.experiment.datasetproviders.DatasetProvider;
import meka.experiment.datasetproviders.LocalDatasetProvider;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsHandler;
import meka.experiment.evaluationstatistics.IncrementalEvaluationStatisticsHandler;
import meka.experiment.evaluationstatistics.KeyValuePairs;
import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.Evaluator;
import meka.experiment.events.*;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

import java.util.*;

/**
 * Default experiment which executes experiments on the local machine.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class DefaultExperiment
		extends LogObject
		implements Experiment {

	private static final long serialVersionUID = 8654760249461885158L;

	/** the classifiers to evaluate. */
	protected MultiLabelClassifier[] m_Classifiers = new MultiLabelClassifier[0];

	/** the dataset provider. */
	protected DatasetProvider m_DatasetProvider = getDefaultDatasetProvider();

	/** the evaluator. */
	protected Evaluator m_Evaluator = getDefaultEvaluator();

	/** the statistics handler. */
	protected EvaluationStatisticsHandler m_StatisticsHandler = getDefaultStatisticsHandler();

	/** whether the experiment is initializing. */
	protected boolean m_Initializing;

	/** whether the experiment is running. */
	protected boolean m_Running;

	/** whether the experiment is stopping. */
	protected boolean m_Stopping;

	/** the listeners for execution stages.  */
	protected transient HashSet<ExecutionStageListener> m_ExecutionStageListeners;

	/** the listeners for iterations.  */
	protected transient HashSet<IterationNotificationListener> m_IterationNotficationListeners;

	/** the listeners for statistics.  */
	protected transient HashSet<StatisticsNotificationListener> m_StatisticsNotificationListeners;

	/** the collected statistics. */
	protected List<EvaluationStatistics> m_Statistics = new ArrayList<>();

	/**
	 * Sets the classifiers to be evaluated.
	 *
	 * @param value         the classifiers
	 */
	public void setClassifiers(MultiLabelClassifier[] value) {
		m_Classifiers = value;
	}

	/**
	 * Returns the classifiers to be evaluated.
	 *
	 * @return              the classifiers
	 */
	public MultiLabelClassifier[] getClassifiers() {
		return m_Classifiers;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String classifiersTipText() {
		return "The classifiers to evaluate.";
	}

	/**
	 * Returns the default dataset provider.
	 *
	 * @return          the default
	 */
	protected DatasetProvider getDefaultDatasetProvider() {
		return new LocalDatasetProvider();
	}

	/**
	 * Sets the dataset provider to use.
	 *
	 * @param value         the provider
	 */
	@Override
	public void setDatasetProvider(DatasetProvider value) {
		m_DatasetProvider = value;
	}

	/**
	 * Returns the dataset provider in use.
	 *
	 * @return              the provider
	 */
	@Override
	public DatasetProvider getDatasetProvider() {
		return m_DatasetProvider;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	@Override
	public String datasetProviderTipText() {
		return "The dataset provider to use.";
	}

	/**
	 * Returns the default evaluator to use.
	 *
	 * @return              the default
	 */
	protected Evaluator getDefaultEvaluator() {
		return new CrossValidation();
	}

	/**
	 * Sets the evaluator to use.
	 *
	 * @param value         the evaluator
	 */
	@Override
	public void setEvaluator(Evaluator value) {
		m_Evaluator = value;
	}

	/**
	 * Returns the evaluator in use.
	 *
	 * @return              the evaluator
	 */
	@Override
	public Evaluator getEvaluator() {
		return m_Evaluator;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	@Override
	public String evaluatorTipText() {
		return "The evaluator to use.";
	}

	/**
	 * Sets the statistics handler.
	 *
	 * @param value     the handler
	 */
	@Override
	public void setStatisticsHandler(EvaluationStatisticsHandler value) {
		m_StatisticsHandler = value;
	}

	/**
	 * Returns the default statistics handler.
	 *
	 * @return          the default
	 */
	protected EvaluationStatisticsHandler getDefaultStatisticsHandler() {
		return new KeyValuePairs();
	}

	/**
	 * Returns the statistics handler.
	 *
	 * @return          the handler
	 */
	@Override
	public EvaluationStatisticsHandler getStatisticsHandler() {
		return m_StatisticsHandler;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	@Override
	public String statisticsHandlerTipText() {
		return "The handler for the statistics (load/save).";
	}

	/**
	 * Adds the execution stage listener.
	 *
	 * @param l         the listener to add
	 */
	public synchronized void addExecutionStageListener(ExecutionStageListener l) {
		if (m_ExecutionStageListeners == null)
			m_ExecutionStageListeners = new HashSet<>();
		m_ExecutionStageListeners.add(l);
	}

	/**
	 * Removes the execution stage listener.
	 *
	 * @param l         the listener to remove
	 */
	public synchronized void removeExecutionStageListener(ExecutionStageListener l) {
		if (m_ExecutionStageListeners == null)
			m_ExecutionStageListeners = new HashSet<>();
		m_ExecutionStageListeners.remove(l);
	}

	/**
	 * Notifies all listeners of a new execution stage.
	 *
	 * @param stage         the new stage
	 */
	protected synchronized void notifyExecutionStageListeners(ExecutionStageEvent.Stage stage) {
		ExecutionStageEvent  e;

		if (m_ExecutionStageListeners == null)
			return;

		e = new ExecutionStageEvent(this, stage);
		for (ExecutionStageListener l: m_ExecutionStageListeners)
			l.experimentStage(e);
	}

	/**
	 * Adds the iteration listener.
	 *
	 * @param l         the listener to add
	 */
	public synchronized void addIterationNotificationListener(IterationNotificationListener l) {
		if (m_IterationNotficationListeners == null)
			m_IterationNotficationListeners = new HashSet<>();
		m_IterationNotficationListeners.add(l);
	}

	/**
	 * Removes the iteration listener.
	 *
	 * @param l         the listener to remove
	 */
	public synchronized void removeIterationNotificationListener(IterationNotificationListener l) {
		if (m_IterationNotficationListeners == null)
			m_IterationNotficationListeners = new HashSet<>();
		m_IterationNotficationListeners.remove(l);
	}

	/**
	 * Notifies all listeners of a new classifier/dataset combination.
	 *
	 * @param classifier    the classifier
	 * @param dataset       the dataset
	 */
	protected synchronized void notifyIterationNotificationListeners(MultiLabelClassifier classifier, Instances dataset) {
		IterationNotificationEvent  e;

		if (m_IterationNotficationListeners == null)
			return;

		e = new IterationNotificationEvent(this, classifier, dataset);
		for (IterationNotificationListener l: m_IterationNotficationListeners)
			l.nextIteration(e);
	}

	/**
	 * Adds the statistics listener.
	 *
	 * @param l         the listener to add
	 */
	public synchronized void addStatisticsNotificationListener(StatisticsNotificationListener l) {
		if (m_StatisticsNotificationListeners == null)
			m_StatisticsNotificationListeners = new HashSet<>();
		m_StatisticsNotificationListeners.add(l);
	}

	/**
	 * Removes the statistics listener.
	 *
	 * @param l         the listener to remove
	 */
	public synchronized void removeStatisticsNotificationListener(StatisticsNotificationListener l) {
		if (m_StatisticsNotificationListeners == null)
			m_StatisticsNotificationListeners = new HashSet<>();
		m_StatisticsNotificationListeners.remove(l);
	}

	/**
	 * Notifies all listeners of a new classifier/dataset combination.
	 *
	 * @param stats     the statistics
	 */
	protected synchronized void notifyStatisticsNotificationListeners(List<EvaluationStatistics> stats) {
		StatisticsNotificationEvent e;

		if (m_StatisticsNotificationListeners == null)
			return;

		e = new StatisticsNotificationEvent(this, stats);
		for (StatisticsNotificationListener l: m_StatisticsNotificationListeners)
			l.statisticsAvailable(e);
	}

	/**
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.addOption(result, classifiersTipText(), "none", 'C');
		OptionUtils.addOption(result, datasetProviderTipText(), getDefaultDatasetProvider().getClass().getName(), 'D');
		OptionUtils.addOption(result, evaluatorTipText(), getDefaultEvaluator().getClass().getName(), 'E');
		OptionUtils.addOption(result, statisticsHandlerTipText(), getDefaultStatisticsHandler().getClass().getName(), 'S');
		return OptionUtils.toEnumeration(result);
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options
	 * @throws Exception    if parsing fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		setClassifiers(OptionUtils.parse(options, 'C', MultiLabelClassifier.class));
		setDatasetProvider((DatasetProvider) OptionUtils.parse(options, 'D', getDefaultDatasetProvider()));
		setEvaluator((Evaluator) OptionUtils.parse(options, 'E', getDefaultEvaluator()));
		setStatisticsHandler((EvaluationStatisticsHandler) OptionUtils.parse(options, 'S', getDefaultStatisticsHandler()));
	}

	/**
	 * Returns the options.
	 *
	 * @return              the options
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'C', getClassifiers());
		OptionUtils.add(result, 'D', getDatasetProvider());
		OptionUtils.add(result, 'E', getEvaluator());
		OptionUtils.add(result, 'S', getStatisticsHandler());
		return OptionUtils.toArray(result);
	}

	/**
	 * Adds the source's class name to the message if not null.
	 *
	 * @param source    the source
	 * @param msg       the error message, can be null
	 * @return          null if no error message, otherwise enriched message
	 */
	protected String handleError(Object source, String msg) {
		if (msg == null)
			return null;
		msg = source.getClass().getName() + ": " + msg;
		log(msg);
		return msg;
	}

	/**
	 * Initializes the experiment.
	 *
	 * @return          null if successfully initialized, otherwise error message
	 */
	public String initialize() {
		String      result;

		debug("pre: init");
		m_Initializing = true;
		m_Running      = false;
		m_Stopping     = false;

		notifyExecutionStageListeners(ExecutionStageEvent.Stage.INITIALIZING);

		ExperimentUtils.ensureThreadSafety(this);

		for (LogListener l: m_LogListeners) {
			m_DatasetProvider.addLogListener(l);
			m_StatisticsHandler.addLogListener(l);
			m_Evaluator.addLogListener(l);
		}

		m_Statistics.clear();
		result = handleError(m_DatasetProvider, m_DatasetProvider.initialize());
		if (result == null)
			result = handleError(m_StatisticsHandler, m_StatisticsHandler.initialize());

		if (result != null)
			log(result);

		m_Initializing = false;
		debug("post: init");

		return result;
	}

	/**
	 * Returns whether the experiment is initializing.
	 *
	 * @return          true if initializing
	 */
	public boolean isInitializing() {
		return m_Initializing;
	}

	/**
	 * Runs the experiment.
	 *
	 * @return          null if successfully run, otherwise error message
	 */
	public String run() {
		String                      result;
		Instances                   dataset;
		List<EvaluationStatistics>  stats;
		boolean                     incremental;

		debug("pre: run");

		result      = null;
		m_Running   = true;
		incremental = (m_StatisticsHandler instanceof IncrementalEvaluationStatisticsHandler) &&
				(((IncrementalEvaluationStatisticsHandler) m_StatisticsHandler).supportsIncrementalUpdate());
		debug("Incremental statistics? " + incremental);

		notifyExecutionStageListeners(ExecutionStageEvent.Stage.RUNNING);

		while (m_DatasetProvider.hasNext()) {
			// next dataset
			debug("pre: next-dataset");
			dataset = m_DatasetProvider.next();
			debug("post: next-dataset");
			if (dataset == null) {
				result = "Failed to obtain next dataset!";
				log(result);
				m_Running = false;
				break;
			}
			log("Using dataset: " + dataset.relationName());

			// iterate classifiers
			for (MultiLabelClassifier classifier: m_Classifiers) {
				// evaluation required?
				if (incremental) {
					if (!((IncrementalEvaluationStatisticsHandler) m_StatisticsHandler).requires(classifier, dataset)) {
						log("Already present, skipping: " + Utils.toCommandLine(classifier) + " --> " + dataset.relationName());
						List<EvaluationStatistics> priorStats = ((IncrementalEvaluationStatisticsHandler) m_StatisticsHandler).retrieve(classifier, dataset);
						m_Statistics.addAll(priorStats);
						notifyStatisticsNotificationListeners(priorStats);
						continue;
					}
				}

				try {
					classifier = (MultiLabelClassifier) AbstractClassifier.makeCopy(classifier);
				}
				catch (Exception e) {
					result = handleException("Failed to create copy of classifier: " + classifier.getClass().getName(), e);
					log(result);
					m_Running = false;
					break;
				}

				if (m_Running && !m_Stopping) {
					// notify listeners
					notifyIterationNotificationListeners(classifier, dataset);
					log("Using classifier: " + OptionUtils.toCommandLine(classifier));

					// perform evaluation
					debug("pre: evaluator init");
					result = m_Evaluator.initialize();
					debug("post: evaluator init");
					if (result != null) {
						m_Running = false;
						break;
					}
					try {
						debug("pre: evaluator evaluate");
						stats = m_Evaluator.evaluate(classifier, dataset);
						debug("post: evaluator evaluate");
					}
					catch (Exception e) {
						result = handleException("Failed to evaluate dataset '" + dataset.relationName() + "' with classifier: " + Utils.toCommandLine(classifier), e);
						log(result);
						m_Running = false;
						break;
					}
					if (stats != null) {
						m_Statistics.addAll(stats);
						if (incremental)
							((IncrementalEvaluationStatisticsHandler) m_StatisticsHandler).append(stats);
						notifyStatisticsNotificationListeners(stats);
					}
				}

				if (!m_Running || m_Stopping)
					break;
			}
			if (!m_Running || m_Stopping)
				break;
		}


		if (m_Running && !m_Stopping) {
			if (!incremental)
				m_StatisticsHandler.write(m_Statistics);
		}
		if (!m_Running) {
			if (result == null)
				result = "Experiment interrupted!";
			else
				result = "Experiment interrupted: " + result;
		}

		if (result != null)
			log(result);

		m_Running  = false;
		m_Stopping = false;

		debug("post: run");

		return result;
	}

	/**
	 * Returns whether the experiment is running.
	 *
	 * @return          true if running
	 */
	public boolean isRunning() {
		return m_Running;
	}

	/**
	 * Stops the experiment if running.
	 */
	public void stop() {
		debug("pre: stop");

		m_Stopping     = true;
		m_Initializing = false;
		m_Running      = false;

		notifyExecutionStageListeners(ExecutionStageEvent.Stage.STOPPING);

		m_Evaluator.stop();

		debug("post: stop");
	}

	/**
	 * Returns whether the experiment is stopping.
	 *
	 * @return          true if stopping
	 */
	public boolean isStopping() {
		return m_Stopping;
	}

	/**
	 * Finishes the experiment.
	 *
	 * @return          null if successfully finished, otherwise error message
	 */
	public String finish() {
		String      result;

		debug("pre: finish");

		result = handleError(m_DatasetProvider, m_DatasetProvider.finish());
		if (result != null)
			result = handleError(m_StatisticsHandler, m_StatisticsHandler.finish());

		if (result != null)
			log(result);

		for (LogListener l: m_LogListeners) {
			m_DatasetProvider.removeLogListener(l);
			m_StatisticsHandler.removeLogListener(l);
			m_Evaluator.removeLogListener(l);
		}

		m_Stopping     = false;
		m_Initializing = false;
		m_Running      = false;

		notifyExecutionStageListeners(ExecutionStageEvent.Stage.FINISH);

		debug("post: finish");

		return result;
	}

	/**
	 * Returns the current statistics.
	 *
	 * @return          the statistics, if any
	 */
	public List<EvaluationStatistics> getStatistics() {
		return m_Statistics;
	}
}
