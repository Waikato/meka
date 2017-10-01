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
 * MekaSearch.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package meka.core.multisearch;

import weka.classifiers.Classifier;
import weka.classifiers.meta.multisearch.AbstractEvaluationTask;
import weka.classifiers.meta.multisearch.AbstractMultiThreadedSearch;
import weka.classifiers.meta.multisearch.Performance;
import weka.classifiers.meta.multisearch.PerformanceComparator;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.setupgenerator.Point;
import weka.core.setupgenerator.Space;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;

import java.io.File;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.Future;

/**
 <!-- globalinfo-start -->
 * Performs a search of an arbitrary number of parameters of a classifier and chooses the best setup found for the actual training.<br>
 * The properties being explored are totally up to the user.<br>
 * <br>
 * E.g., if you have a FilteredClassifier selected as base classifier, sporting a PLSFilter and you want to explore the number of PLS components, then your property will be made up of the following components:<br>
 *  - filter: referring to the FilteredClassifier's property (= PLSFilter)<br>
 *  - numComponents: the actual property of the PLSFilter that we want to modify<br>
 * And assembled, the property looks like this:<br>
 *   filter.numComponents<br>
 * <br>
 * The initial space is worked on with 2-fold CV to determine the values of the parameters for the selected type of evaluation (e.g., accuracy). The best point in the space is then taken as center and a 10-fold CV is performed with the adjacent parameters. If better parameters are found, then this will act as new center and another 10-fold CV will be performed (kind of hill-climbing). This process is repeated until no better pair is found or the best pair is on the border of the parameter space.<br>
 * The number of CV-folds for the initial and subsequent spaces can be adjusted, of course.<br>
 * <br>
 * Instead of using cross-validation, it is possible to specify test sets, for the initial space evaluation and the subsequent ones.<br>
 * <br>
 * The outcome of a mathematical function (= double), MultiSearch will convert to integers (values are just cast to int), booleans (0 is false, otherwise true), float, char and long if necessary.<br>
 * Via a user-supplied 'list' of parameters (blank-separated), one can also set strings and selected tags (drop-down comboboxes in Weka's GenericObjectEditor). Classnames with options (e.g., classifiers with their options) are possible as well.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br>
 *
 * <pre> -sample-size &lt;num&gt;
 *  The size (in percent) of the sample to search the inital space with.
 *  (default: 100)</pre>
 *
 * <pre> -initial-folds &lt;num&gt;
 *  The number of cross-validation folds for the initial space.
 *  Numbers smaller than 2 turn off cross-validation and just
 *  perform evaluation on the training set.
 *  (default: 2)</pre>
 *
 * <pre> -subsequent-folds &lt;num&gt;
 *  The number of cross-validation folds for the subsequent sub-spaces.
 *  Numbers smaller than 2 turn off cross-validation and just
 *  perform evaluation on the training set.
 *  (default: 10)</pre>
 *
 * <pre> -initial-test-set &lt;filename&gt;
 *  The (optional) test set to use for the initial space.
 *  Gets ignored if pointing to a file. Overrides cross-validation.
 *  (default: .)</pre>
 *
 * <pre> -subsequent-test-set &lt;filename&gt;
 *  The (optional) test set to use for the subsequent sub-spaces.
 *  Gets ignored if pointing to a file. Overrides cross-validation.
 *  (default: .)</pre>
 *
 * <pre> -num-slots &lt;num&gt;
 *  Number of execution slots.
 *  (default 1 - i.e. no parallelism)</pre>
 *
 * <pre> -D
 *  Whether to enable debugging output.
 *  (default off)</pre>
 *
 <!-- options-end -->
 *
 * General notes:
 * <ul>
 *   <li>Turn the <i>debug</i> flag on in order to see some progress output in the
 *       console</li>
 * </ul>
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MekaSearch
	extends AbstractMultiThreadedSearch {

	private static final long serialVersionUID = -3579744329581176799L;

	/** the sample size to search the initial space with. */
	protected double m_SampleSize = 100;

	/** number of cross-validation folds in the initial space. */
	protected int m_InitialSpaceNumFolds = 2;

	/** number of cross-validation folds in the subsequent spaces. */
	protected int m_SubsequentSpaceNumFolds = 10;

	/** the optional test set to use for the initial evaluation (overrides cross-validation, ignored if dir). */
	protected File m_InitialSpaceTestSet = new File(".");

	/** the optional test set to use for the subsequent evaluation (overrides cross-validation, ignored if dir). */
	protected File m_SubsequentSpaceTestSet = new File(".");

	/** the optional test set to use for the initial evaluation. */
	protected Instances m_InitialSpaceTestInst;

	/** the optional test set to use for the subsequent evaluation. */
	protected Instances m_SubsequentSpaceTestInst;

	/**
	 * Returns a string describing the object.
	 *
	 * @return 		a description suitable for displaying in the
	 *         		explorer/experimenter gui
	 */
	@Override
	public String globalInfo() {
		return
			"Performs a search of an arbitrary number of parameters of a classifier "
				+ "and chooses the best setup found for the actual training.\n"
				+ "The properties being explored are totally up to the user.\n"
				+ "\n"
				+ "E.g., if you have a FilteredClassifier selected as base classifier, "
				+ "sporting a PLSFilter and you want to explore the number of PLS components, "
				+ "then your property will be made up of the following components:\n"
				+ " - filter: referring to the FilteredClassifier's property (= PLSFilter)\n"
				+ " - numComponents: the actual property of the PLSFilter that we want to modify\n"
				+ "And assembled, the property looks like this:\n"
				+ "  filter.numComponents\n"
				+ "\n"
				+ "The initial space is worked on with 2-fold CV to determine the values "
				+ "of the parameters for the selected type of evaluation (e.g., "
				+ "accuracy). The best point in the space is then taken as center and a "
				+ "10-fold CV is performed with the adjacent parameters. If better parameters "
				+ "are found, then this will act as new center and another 10-fold CV will "
				+ "be performed (kind of hill-climbing). This process is repeated until "
				+ "no better pair is found or the best pair is on the border of the parameter "
				+ "space.\n"
				+ "The number of CV-folds for the initial and subsequent spaces can be "
				+ "adjusted, of course.\n"
				+ "\n"
				+ "Instead of using cross-validation, it is possible to specify test sets, "
				+ "for the initial space evaluation and the subsequent ones.\n"
				+ "\n"
				+ "The outcome of a mathematical function (= double), MultiSearch will convert "
				+ "to integers (values are just cast to int), booleans (0 is false, otherwise "
				+ "true), float, char and long if necessary.\n"
				+ "Via a user-supplied 'list' of parameters (blank-separated), one can also "
				+ "set strings and selected tags (drop-down comboboxes in Weka's "
				+ "GenericObjectEditor). Classnames with options (e.g., classifiers with "
				+ "their options) are possible as well.";
	}

	/**
	 * Gets an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration listOptions() {
		Vector result;
		Enumeration   	en;

		result = new Vector();

		result.addElement(new Option(
			"\tThe size (in percent) of the sample to search the inital space with.\n"
				+ "\t(default: 100)",
			"sample-size", 1, "-sample-size <num>"));

		result.addElement(new Option(
			"\tThe number of cross-validation folds for the initial space.\n"
				+ "\tNumbers smaller than 2 turn off cross-validation and just\n"
				+ "\tperform evaluation on the training set.\n"
				+ "\t(default: 2)",
			"initial-folds", 1, "-initial-folds <num>"));

		result.addElement(new Option(
			"\tThe number of cross-validation folds for the subsequent sub-spaces.\n"
				+ "\tNumbers smaller than 2 turn off cross-validation and just\n"
				+ "\tperform evaluation on the training set.\n"
				+ "\t(default: 10)",
			"subsequent-folds", 1, "-subsequent-folds <num>"));

		result.addElement(new Option(
			"\tThe (optional) test set to use for the initial space.\n"
				+ "\tGets ignored if pointing to a file. Overrides cross-validation.\n"
				+ "\t(default: .)",
			"initial-test-set", 1, "-initial-test-set <filename>"));

		result.addElement(new Option(
			"\tThe (optional) test set to use for the subsequent sub-spaces.\n"
				+ "\tGets ignored if pointing to a file. Overrides cross-validation.\n"
				+ "\t(default: .)",
			"subsequent-test-set", 1, "-subsequent-test-set <filename>"));

		en = super.listOptions();
		while (en.hasMoreElements())
			result.addElement(en.nextElement());

		return result.elements();
	}

	/**
	 * returns the options of the current setup.
	 *
	 * @return		the current options
	 */
	@Override
	public String[] getOptions() {
		int       		i;
		Vector<String>    	result;
		String[]  		options;

		result = new Vector<String>();

		result.add("-sample-size");
		result.add("" + getSampleSizePercent());

		result.add("-initial-folds");
		result.add("" + getInitialSpaceNumFolds());

		result.add("-subsequent-folds");
		result.add("" + getSubsequentSpaceNumFolds());

		result.add("-initial-test-set");
		result.add("" + getInitialSpaceTestSet());

		result.add("-subsequent-test-set");
		result.add("" + getSubsequentSpaceTestSet());

		options = super.getOptions();
		for (i = 0; i < options.length; i++)
			result.add(options[i]);

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Parses the options for this object.
	 *
	 * @param options	the options to use
	 * @throws Exception	if setting of options fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		String		tmpStr;

		tmpStr = Utils.getOption("sample-size", options);
		if (tmpStr.length() != 0)
			setSampleSizePercent(Double.parseDouble(tmpStr));
		else
			setSampleSizePercent(100);

		tmpStr = Utils.getOption("initial-folds", options);
		if (tmpStr.length() != 0)
			setInitialSpaceNumFolds(Integer.parseInt(tmpStr));
		else
			setInitialSpaceNumFolds(2);

		tmpStr = Utils.getOption("subsequent-folds", options);
		if (tmpStr.length() != 0)
			setSubsequentSpaceNumFolds(Integer.parseInt(tmpStr));
		else
			setSubsequentSpaceNumFolds(10);

		tmpStr = Utils.getOption("initial-test-set", options);
		if (tmpStr.length() != 0)
			setInitialSpaceTestSet(new File(tmpStr));
		else
			setInitialSpaceTestSet(new File(System.getProperty("user.dir")));

		tmpStr = Utils.getOption("subsequent-test-set", options);
		if (tmpStr.length() != 0)
			setSubsequentSpaceTestSet(new File(tmpStr));
		else
			setSubsequentSpaceTestSet(new File(System.getProperty("user.dir")));

		super.setOptions(options);
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the explorer/experimenter gui
	 */
	public String sampleSizePercentTipText() {
		return "The sample size (in percent) to use in the initial space search.";
	}

	/**
	 * Gets the sample size for the initial space search.
	 *
	 * @return the sample size.
	 */
	public double getSampleSizePercent() {
		return m_SampleSize;
	}

	/**
	 * Sets the sample size for the initial space search.
	 *
	 * @param value the sample size for the initial space search.
	 */
	public void setSampleSizePercent(double value) {
		m_SampleSize = value;
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the explorer/experimenter gui
	 */
	public String initialSpaceNumFoldsTipText() {
		return
			"The number of cross-validation folds when evaluating the initial "
				+ "space; values smaller than 2 turn cross-validation off and simple "
				+ "evaluation on the training set is performed.";
	}

	/**
	 * Gets the number of CV folds for the initial space.
	 *
	 * @return the number of folds.
	 */
	public int getInitialSpaceNumFolds() {
		return m_InitialSpaceNumFolds;
	}

	/**
	 * Sets the number of CV folds for the initial space.
	 *
	 * @param value the number of folds.
	 */
	public void setInitialSpaceNumFolds(int value) {
		m_InitialSpaceNumFolds = value;
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the explorer/experimenter gui
	 */
	public String subsequentSpaceNumFoldsTipText() {
		return
			"The number of cross-validation folds when evaluating the subsequent "
				+ "sub-spaces; values smaller than 2 turn cross-validation off and simple "
				+ "evaluation on the training set is performed.";
	}

	/**
	 * Gets the number of CV folds for the sub-sequent sub-spaces.
	 *
	 * @return the number of folds.
	 */
	public int getSubsequentSpaceNumFolds() {
		return m_SubsequentSpaceNumFolds;
	}

	/**
	 * Sets the number of CV folds for the sub-sequent sub-spaces.
	 *
	 * @param value the number of folds.
	 */
	public void setSubsequentSpaceNumFolds(int value) {
		m_SubsequentSpaceNumFolds = value;
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the explorer/experimenter gui
	 */
	public String initialSpaceTestSetTipText() {
		return
			"The (optional) test set to use for evaluating the initial search space; "
				+ "overrides cross-validation; gets ignored if pointing to a directory.";
	}

	/**
	 * Gets the test set to use for the initial space.
	 *
	 * @return the number of folds.
	 */
	public File getInitialSpaceTestSet() {
		return m_InitialSpaceTestSet;
	}

	/**
	 * Sets the test set to use folds for the initial space.
	 *
	 * @param value the test set, ignored if dir.
	 */
	public void setInitialSpaceTestSet(File value) {
		m_InitialSpaceTestSet = value;
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the explorer/experimenter gui
	 */
	public String subsequentSpaceTestSetTipText() {
		return
			"The (optional) test set to use for evaluating the subsequent search sub-spaces; "
				+ "overrides cross-validation; gets ignored if pointing to a directory.";
	}

	/**
	 * Gets the test set to use for the sub-sequent sub-spaces.
	 *
	 * @return the test set, ignored if dir.
	 */
	public File getSubsequentSpaceTestSet() {
		return m_SubsequentSpaceTestSet;
	}

	/**
	 * Sets the test set to use for the sub-sequent sub-spaces.
	 *
	 * @param value the test set, ignored if dir.
	 */
	public void setSubsequentSpaceTestSet(File value) {
		m_SubsequentSpaceTestSet = value;
	}

	/**
	 * determines the best point for the given space, using CV with
	 * specified number of folds.
	 *
	 * @param space	the space to work on
	 * @param train	the training data to work with
	 * @param test	the test data to use, null if to use cross-validation
	 * @param folds	the number of folds for cross-validation, if &lt;2 then
	 * 			evaluation based on the training set is used
	 * @return		the best point (not actual parameters!)
	 * @throws Exception	if setup or training fails
	 */
	protected Performance determineBestInSpace(Space space, Instances train, Instances test, int folds) throws Exception {
		Performance			result;
		int				i;
		Enumeration<Point<Object>> enm;
		Performance			performance;
		Point<Object>		values;
		boolean			allCached;
		Performance			p1;
		Performance			p2;
		AbstractEvaluationTask newTask;
		int				classLabel;

		m_Performances.clear();

		if (folds >= 2)
			log("Determining best values with " + folds + "-fold CV in space:\n" + space + "\n");
		else
			log("Determining best values with evaluation on training set in space:\n" + space + "\n");

		enm         = space.values();
		allCached   = true;
		m_NumSetups = space.size();
		if (train.classAttribute().isNominal())
			classLabel = m_Owner.getClassLabelIndex(train.classAttribute().numValues());
		else
			classLabel = -1;

		ArrayList<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		while (enm.hasMoreElements()) {
			values = enm.nextElement();

			// already calculated?
			if (m_Cache.isCached(folds, values)) {
				performance = m_Cache.get(folds, values);
				m_Performances.add(performance);
				m_Trace.add(new AbstractMap.SimpleEntry<Integer, Performance>(folds, performance));
				log(performance + ": cached=true");
			}
			else {
				allCached = false;
				newTask   = m_Owner.getFactory().newTask(m_Owner, train, test, m_Owner.getGenerator(), values, folds, m_Owner.getEvaluation().getSelectedTag().getID(), classLabel);
				results.add(m_ExecutorPool.submit(newTask));
			}
		}

		// wait for execution to finish
		try {
			for (Future<Boolean> future : results) {
				if (!future.get()) {
					throw new IllegalStateException("Execution of evaluaton thread failed.");
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Thread-based execution of evaluation tasks failed: " +
				e.getMessage());
		}

		if (allCached) {
			log("All points were already cached - abnormal state!");
			throw new IllegalStateException("All points were already cached - abnormal state!");
		}

		// sort list
		Collections.sort(m_Performances, new PerformanceComparator(m_Owner.getEvaluation().getSelectedTag().getID(), m_Owner.getMetrics()));

		result = m_Performances.firstElement();

		// check whether all performances are the same
		m_UniformPerformance = true;
		p1 = m_Performances.get(0);
		for (i = 1; i < m_Performances.size(); i++) {
			p2 = m_Performances.get(i);
			if (p2.getPerformance(m_Owner.getEvaluation().getSelectedTag().getID()) != p1.getPerformance(m_Owner.getEvaluation().getSelectedTag().getID())) {
				m_UniformPerformance = false;
				break;
			}
		}
		if (m_UniformPerformance)
			log("All performances are the same!");

		logPerformances(space, m_Performances);
		log("\nBest performance:\n" + m_Performances.firstElement());

		m_Performances.clear();

		return result;
	}

	/**
	 * returns the best point in the space.
	 *
	 * @param inst	the training data
	 * @return 		the best point (not evaluated parameters!)
	 * @throws Exception 	if something goes wrong
	 */
	protected Performance findBest(Instances inst) throws Exception {
		Performance		result;
		Point<Integer>	center;
		Space		neighborSpace;
		boolean		finished;
		Point<Object>	evals;
		Performance		resultOld;
		int			iteration;
		Instances sample;
		Resample resample;
		Classifier cls;

		log("Step 1:\n");

		// generate sample?
		if (getSampleSizePercent() == 100) {
			sample = inst;
		}
		else {
			log("Generating sample (" + getSampleSizePercent() + "%)");
			resample = new Resample();
			resample.setRandomSeed(retrieveOwner().getSeed());
			resample.setSampleSizePercent(getSampleSizePercent());
			resample.setInputFormat(inst);
			sample = Filter.useFilter(inst, resample);
		}

		iteration            = 0;
		m_UniformPerformance = false;

		// find first center
		log("\n=== Initial space - Start ===");
		result = determineBestInSpace(m_Space, sample, m_InitialSpaceTestInst, m_InitialSpaceNumFolds);
		log("\nResult of Step 1: " + result + "\n");
		log("=== Initial space - End ===\n");

		finished = m_UniformPerformance;

		if (!finished) {
			do {
				iteration++;
				resultOld = (Performance) result.clone();
				center    = m_Space.getLocations(result.getValues());
				// on border? -> finished
				if (m_Space.isOnBorder(center)) {
					log("Center is on border of space.");
					finished = true;
				}

				// new space with current best one at center and immediate neighbors
				// around it
				if (!finished) {
					neighborSpace = m_Space.subspace(center);
					result = determineBestInSpace(neighborSpace, sample, m_SubsequentSpaceTestInst, m_SubsequentSpaceNumFolds);
					log("\nResult of Step 2/Iteration " + (iteration) + ":\n" + result);
					finished = m_UniformPerformance;

					// no improvement?
					if (result.getValues().equals(resultOld.getValues())) {
						finished = true;
						log("\nNo better point found.");
					}
				}
			}
			while (!finished);
		}

		log("\nFinal result: " + result);
		evals = m_Owner.getGenerator().evaluate(result.getValues());
		cls = (Classifier) m_Owner.getGenerator().setup((Serializable) m_Owner.getClassifier(), evals);
		log("Classifier: " + getCommandline(cls));

		return result;
	}

	/**
	 * Loads test data, if required.
	 *
	 * @param data	the current training data
	 * @throws Exception	if test sets are not compatible with training data
	 */
	protected void loadTestData(Instances data) throws Exception {
		String		msg;

		m_InitialSpaceTestInst = null;
		if (m_InitialSpaceTestSet.exists() && !m_InitialSpaceTestSet.isDirectory()) {
			m_InitialSpaceTestInst = DataSource.read(m_InitialSpaceTestSet.getAbsolutePath());
			m_InitialSpaceTestInst.setClassIndex(data.classIndex());
			msg = data.equalHeadersMsg(m_InitialSpaceTestInst);
			if (msg != null)
				throw new IllegalArgumentException("Test set for initial space not compatible with training dta:\n" +  msg);
			m_InitialSpaceTestInst.deleteWithMissingClass();
			log("Using test set for initial space: " + m_InitialSpaceTestSet);
		}

		m_SubsequentSpaceTestInst = null;
		if (m_SubsequentSpaceTestSet.exists() && !m_SubsequentSpaceTestSet.isDirectory()) {
			m_SubsequentSpaceTestInst = DataSource.read(m_SubsequentSpaceTestSet.getAbsolutePath());
			m_SubsequentSpaceTestInst.setClassIndex(data.classIndex());
			msg = data.equalHeadersMsg(m_SubsequentSpaceTestInst);
			if (msg != null)
				throw new IllegalArgumentException("Test set for subsequent sub-spaces not compatible with training dta:\n" +  msg);
			m_SubsequentSpaceTestInst.deleteWithMissingClass();
			log("Using test set for subsequent sub-spaces: " + m_InitialSpaceTestSet);
		}
	}

	/**
	 * Performs the actual search and returns the best setup.
	 *
	 * @param data	the dataset to use
	 * @return		the best classifier setup
	 * @throws Exception	if search fails
	 */
	@Override
	public SearchResult doSearch(Instances data) throws Exception {
		SearchResult	result;
		Point<Object>	evals;
		Performance		performance;

		loadTestData(data);

		performance        = findBest(new Instances(data));
		evals              = m_Owner.getGenerator().evaluate(performance.getValues());
		result             = new SearchResult();
		result.classifier  = (Classifier) m_Owner.getGenerator().setup((Serializable) m_Owner.getClassifier(), evals);
		result.performance = performance;
		result.values      = evals;

		return result;
	}
}
