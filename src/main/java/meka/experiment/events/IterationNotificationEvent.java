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
 * IterationNotificationEvent.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.events;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.experiment.Experiment;
import weka.core.Instances;

import java.util.EventObject;

/**
 * Event that gets sent by an experiment when a new classifier/dataset combination is being evaluated.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class IterationNotificationEvent
  extends EventObject {

	private static final long serialVersionUID = 7732581989591408787L;

	/** the classifier. */
	protected MultiLabelClassifier m_Classifier;

	/** the dataset. */
	protected Instances m_Dataset;

	/**
	 * Gets called when the experiment starts on a new evaluation.
	 *
	 * @param source        the experiment that triggered the event
	 * @param classifier    the classifier
	 * @param dataset       the dataset
	 */
	public IterationNotificationEvent(Experiment source, MultiLabelClassifier classifier, Instances dataset) {
		super(source);

		m_Classifier = classifier;
		m_Dataset    = dataset;
	}

	/**
	 * Returns the associated experiment.
	 *
	 * @return      the experiment
	 */
	public Experiment getExperiment() {
	  return (Experiment) getSource();
	}

	/**
	 * Returns the classifier.
	 *
	 * @return      the classifier
	 */
	public MultiLabelClassifier getClassifier() {
		return m_Classifier;
	}

	/**
	 * Returns the dataset.
	 *
	 * @return      the datasetD
	 */
	public Instances getDataset() {
		return m_Dataset;
	}
}
