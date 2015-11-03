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
 * Experiment.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.events.LogSupporter;
import meka.experiment.datasetproviders.DatasetProvider;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsHandler;
import meka.experiment.evaluators.Evaluator;
import meka.experiment.events.ExecutionStageListener;
import meka.experiment.events.IterationNotificationListener;
import meka.experiment.events.StatisticsNotificationListener;
import weka.core.OptionHandler;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for experiments.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface Experiment
  extends OptionHandler, Serializable, LogSupporter {

	/**
	 * Sets the classifiers to be evaluated.
	 *
	 * @param value         the classifiers
	 */
	public void setClassifiers(MultiLabelClassifier[] value);

	/**
	 * Returns the classifiers to be evaluated.
	 *
	 * @return              the classifiers
	 */
	public MultiLabelClassifier[] getClassifiers();

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String classifiersTipText();

	/**
	 * Sets the dataset provider to use.
	 *
	 * @param value         the provider
	 */
	public void setDatasetProvider(DatasetProvider value);

	/**
	 * Returns the dataset provider in use.
	 *
	 * @return              the provider
	 */
	public DatasetProvider getDatasetProvider();

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String datasetProviderTipText();

	/**
	 * Sets the evaluator to use.
	 *
	 * @param value         the evaluator
	 */
	public void setEvaluator(Evaluator value);

	/**
	 * Returns the evaluator in use.
	 *
	 * @return              the evaluator
	 */
	public Evaluator getEvaluator();

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String evaluatorTipText();

	/**
	 * Sets the statistics handler.
	 *
	 * @param value     the handler
	 */
	public void setStatisticsHandler(EvaluationStatisticsHandler value);

	/**
	 * Returns the statistics handler.
	 *
	 * @return          the handler
	 */
	public EvaluationStatisticsHandler getStatisticsHandler();

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String statisticsHandlerTipText();

	/**
	 * Adds the execution stage listener.
	 *
	 * @param l         the listener to add
	 */
	public void addExecutionStageListener(ExecutionStageListener l);

	/**
	 * Removes the execution stage listener.
	 *
	 * @param l         the listener to remove
	 */
	public void removeExecutionStageListener(ExecutionStageListener l);

	/**
	 * Adds the iteration listener.
	 *
	 * @param l         the listener to add
	 */
	public void addIterationNotificationListener(IterationNotificationListener l);

	/**
	 * Removes the iteration listener.
	 *
	 * @param l         the listener to remove
	 */
	public void removeIterationNotificationListener(IterationNotificationListener l);

	/**
	 * Adds the statistics listener.
	 *
	 * @param l         the listener to add
	 */
	public void addStatisticsNotificationListener(StatisticsNotificationListener l);

	/**
	 * Removes the statistics listener.
	 *
	 * @param l         the listener to remove
	 */
	public void removeStatisticsNotificationListener(StatisticsNotificationListener l);

	/**
	 * Initializes the experiment.
	 *
	 * @return          null if successfully initialized, otherwise error message
	 */
	public String initialize();

	/**
	 * Returns whether the experiment is initializing.
	 *
	 * @return          true if initializing
	 */
	public boolean isInitializing();

	/**
	 * Runs the experiment.
	 *
	 * @return          null if successfully run, otherwise error message
	 */
	public String run();

	/**
	 * Returns whether the experiment is running.
	 *
	 * @return          true if running
	 */
	public boolean isRunning();

	/**
	 * Stops the experiment if still running.
	 */
	public void stop();

	/**
	 * Returns whether the experiment is stopping.
	 *
	 * @return          true if stopping
	 */
	public boolean isStopping();

	/**
	 * Finishes the experiment.
	 *
	 * @return          null if successfully finished, otherwise error message
	 */
	public String finish();

	/**
	 * Returns the current statistics.
	 *
	 * @return          the statistics, if any
	 */
	public List<EvaluationStatistics> getStatistics();
}
