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
import meka.core.ExceptionUtils;
import meka.core.OptionUtils;
import meka.experiment.datasetproviders.DatasetProvider;
import meka.experiment.datasetproviders.LocalDatasetProvider;
import meka.experiment.evaluationstatistics.*;
import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.Evaluator;
import meka.experiment.events.IterationNotificationEvent;
import meka.experiment.events.IterationNotificationListener;
import meka.experiment.events.LogEvent;
import meka.experiment.events.LogListener;
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

	/** whether the experiment is still running. */
	protected boolean m_Running;

	/** the listeners for iterations.  */
	protected transient HashSet<IterationNotificationListener> m_IterationNotficationListeners;

	/** the listeners. */
	protected HashSet<LogListener> m_LogListeners = new HashSet<>();

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
	 * Adds the listener.
	 *
	 * @param l         the listener to add
	 */
	public synchronized void addIterationNotificationListener(IterationNotificationListener l) {
		if (m_IterationNotficationListeners == null)
			m_IterationNotficationListeners = new HashSet<>();
		m_IterationNotficationListeners.add(l);
	}

	/**
	 * Removes the listener.
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
	 * Adds the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void addLogListener(LogListener l) {
		m_LogListeners.add(l);
	}

	/**
	 * Remove the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void removeLogListener(LogListener l) {
		m_LogListeners.remove(l);
	}

	/**
	 * For logging messages. Uses stderr if no listeners defined.
	 *
	 * @param msg       the message to output
	 */
	protected synchronized void log(String msg) {
		LogEvent e;

		if (m_LogListeners.size() == 0) {
			System.err.println(msg);
			return;
		}

		e = new LogEvent(this, msg);
		for (LogListener l: m_LogListeners)
			l.logMessage(e);
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
	 * Logs the stacktrace along with the message on stderr and returns a
	 * combination of both of them as string.
	 *
	 * @param msg		the message for the exception
	 * @param t		the exception
	 * @return		the full error message (message + stacktrace)
	 */
	public String handleException(String msg, Throwable t) {
		String    result;

		result = ExceptionUtils.handleException(this, msg, t, false);
		log(result);

		return result;
	}

	/**
	 * Initializes the experiment.
	 *
	 * @return          null if successfully initialized, otherwise error message
	 */
	public String initialize() {
		String      result;

		m_Statistics.clear();
		result = handleError(m_DatasetProvider, m_DatasetProvider.initialize());
		if (result == null)
			result = handleError(m_StatisticsHandler, m_StatisticsHandler.initialize());

		if (result == null) {
			for (LogListener l: m_LogListeners) {
				m_DatasetProvider.addLogListener(l);
				m_StatisticsHandler.addLogListener(l);
				m_Evaluator.addLogListener(l);
			}
		}

		if (result != null)
			log(result);

		return result;
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

		result    = null;
		m_Running = true;

		while (m_DatasetProvider.hasNext()) {
			// next dataset
			dataset = m_DatasetProvider.next();
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
				if (m_StatisticsHandler instanceof IncrementalEvaluationStatisticsHandler) {
					if (!((IncrementalEvaluationStatisticsHandler) m_StatisticsHandler).requires(classifier, dataset)) {
						log("Already present, skipping: " + Utils.toCommandLine(classifier) + " --> " + dataset.relationName());
						m_Statistics.addAll(
								((IncrementalEvaluationStatisticsHandler) m_StatisticsHandler).retrieve(classifier, dataset));
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

				if (m_Running) {
					// notify listeners
					notifyIterationNotificationListeners(classifier, dataset);
					log("Using classifier: " + OptionUtils.toCommandLine(classifier));

					// perform evaluation
					result = m_Evaluator.initialize();
					if (result != null) {
						m_Running = false;
						break;
					}
					try {
						stats = m_Evaluator.evaluate(classifier, dataset);
					}
					catch (Exception e) {
						result = handleException("Failed to evaluate dataset '" + dataset.relationName() + "' with classifier: " + Utils.toCommandLine(classifier), e);
						log(result);
						m_Running = false;
						break;
					}
					if (stats != null) {
						m_Statistics.addAll(stats);
						if (m_StatisticsHandler instanceof IncrementalEvaluationStatisticsHandler)
							((IncrementalEvaluationStatisticsHandler) m_StatisticsHandler).append(stats);
					}
				}

				if (!m_Running)
					break;
			}
			if (!m_Running)
				break;
		}


		if (m_Running) {
			if (!(m_StatisticsHandler instanceof IncrementalEvaluationStatisticsHandler))
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

		m_Running = false;

		return result;
	}

	/**
	 * Finishes the experiment.
	 *
	 * @return          null if successfully finished, otherwise error message
	 */
	public String finish() {
		String      result;

		result = handleError(m_DatasetProvider, m_DatasetProvider.finish());
		if (result != null)
			result = handleError(m_StatisticsHandler, m_StatisticsHandler.finish());

		if (result != null)
			log(result);

		return result;
	}

	/**
	 * Returns whether the experiment is still running.
	 *
	 * @return          true if still running
	 */
	public boolean isRunning() {
		return m_Running;
	}

	/**
	 * Stops the experiment if still running.
	 */
	public void stop() {
		m_Evaluator.stop();
		m_Running = false;
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
