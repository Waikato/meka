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
 * EvaluationStatistics.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.core.Result;
import weka.core.Instances;
import weka.core.Utils;

import java.util.HashMap;

/**
 * Stores evaluation statistics.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class EvaluationStatistics
  extends HashMap<String,Number>  {
	private static final long serialVersionUID = -1873027591755259927L;

	/** the key for the classifier. */
	public final static String KEY_CLASSIFIER = "Classifier";

	/** the key for the relation. */
	public final static String KEY_RELATION = "Relation";

	/** the classifier. */
	protected MultiLabelClassifier m_Classifier;

	/** the classifier commandline. */
	protected String m_CommandLine;

	/** the relation name. */
	protected String m_Relation;

	/**
	 * Default constructor.
	 */
	public EvaluationStatistics() {
		this(null, (String) null, null);
	}

	/**
	 * Extracts the statistics from the Result object.
	 *
	 * @param classifier    the classifier
	 * @param dataset       the dataset
	 * @param result        the evaluation
	 */
	public EvaluationStatistics(MultiLabelClassifier classifier, Instances dataset, Result result) {
		this(classifier, (dataset != null) ? dataset.relationName() : null, result);
	}

	/**
	 * Extracts the statistics from the Result object.
	 *
	 * @param classifier    the classifier
	 * @param relation      the relation
	 * @param result        the evaluation
	 */
	public EvaluationStatistics(MultiLabelClassifier classifier, String relation, Result result) {
		super();

		m_Classifier  = classifier;
		m_CommandLine = (classifier == null) ? null : OptionUtils.toCommandLine(classifier);
		m_Relation    = relation;

		if (result != null) {
			for (String key : result.vals.keySet()) {
				if (result.vals.get(key) instanceof Number)
					put(key, (Number) result.vals.get(key));
			}
			for (String key : result.availableMetrics()) {
				if (result.getMeasurement(key) instanceof Number)
					put(key, (Number) result.getMeasurement(key));
			}
		}
	}

	/**
	 * Returns the classifier for these statistics.
	 *
	 * @return      the classifier, null if not set
	 */
	public MultiLabelClassifier getClassifier() {
		return m_Classifier;
	}

	/**
	 * Returns the commandline of the classifier for these statistics.
	 *
	 * @return      the classifier commandline, null if not set
	 */
	public String getCommandLine() {
		return m_CommandLine;
	}

	/**
	 * Returns the relation for these statistics.
	 *
	 * @return      the relation, null if not set
	 */
	public String getRelation() {
		return m_Relation;
	}

	/**
	 * Returns the statistics as string.
	 *
	 * @return      the statistics
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Classifier=").append(Utils.toCommandLine(m_Classifier)).append(",");
		result.append("Relation=").append(m_Relation).append(",");
		result.append(super.toString());
		return result.toString();
	}
}
