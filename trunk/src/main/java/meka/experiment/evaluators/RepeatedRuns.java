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
 * RepeatedRuns.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluators;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Repeatedly executes the base evaluator.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class RepeatedRuns
  extends AbstractMetaEvaluator {

	private static final long serialVersionUID = -1230107553603089463L;

	/** the key for the run number. */
	public final static String KEY_RUN = "Run";

	/** the lower number of runs (included). */
	protected int m_LowerRuns = getDefaultLowerRuns();

	/** the upper number of runs (included). */
	protected int m_UpperRuns = getDefaultUpperRuns();

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
		return new PercentageSplit();  // TODO
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
		List<EvaluationStatistics>  stats;
		int                         i;

		result = new ArrayList<>();

		for (i = m_LowerRuns; i <= m_UpperRuns; i++) {
			log("Run: " + i);
			if (m_Evaluator instanceof Randomizable)
				((Randomizable) m_Evaluator).setSeed(i);
			m_Evaluator.initialize();
			stats = m_Evaluator.evaluate(classifier, dataset);
			if (stats != null) {
				for (EvaluationStatistics stat: stats) {
					stat.put(KEY_RUN, i);
					result.add(stat);
				}
			}
		}

		return result;
	}
}
