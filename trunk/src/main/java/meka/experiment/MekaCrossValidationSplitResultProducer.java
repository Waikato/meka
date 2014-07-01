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
 *    MekaCrossValidationSplitResultProducer.java
 *    Copyright (C) 1999-2014 University of Waikato, Hamilton, New Zealand
 *
 */

package meka.experiment;

import java.util.Random;

import meka.core.MLUtils;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.experiment.CrossValidationSplitResultProducer;
import weka.experiment.OutputZipper;
import weka.experiment.SplitEvaluator;

/**
 * <!-- globalinfo-start --> Carries out one split of a repeated k-fold
 * cross-validation, using the set SplitEvaluator to generate some results. Note
 * that the run number is actually the nth split of a repeated k-fold
 * cross-validation, i.e. if k=10, run number 100 is the 10th fold of the 10th
 * cross-validation run. This producer's sole purpose is to allow more
 * fine-grained distribution of cross-validation experiments. If the class
 * attribute is nominal, the dataset is stratified.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -X &lt;number of folds&gt;
 *  The number of folds to use for the cross-validation.
 *  (default 10)
 * </pre>
 * 
 * <pre>
 * -D
 * Save raw split evaluator output.
 * </pre>
 * 
 * <pre>
 * -O &lt;file/directory name/path&gt;
 *  The filename where raw output will be stored.
 *  If a directory name is specified then then individual
 *  outputs will be gzipped, otherwise all output will be
 *  zipped to the named file. Use in conjuction with -D. (default splitEvalutorOut.zip)
 * </pre>
 * 
 * <pre>
 * -W &lt;class name&gt;
 *  The full class name of a SplitEvaluator.
 *  eg: weka.experiment.ClassifierSplitEvaluator
 * </pre>
 * 
 * <pre>
 * Options specific to split evaluator weka.experiment.ClassifierSplitEvaluator:
 * </pre>
 * 
 * <pre>
 * -W &lt;class name&gt;
 *  The full class name of the classifier.
 *  eg: weka.classifiers.bayes.NaiveBayes
 * </pre>
 * 
 * <pre>
 * -C &lt;index&gt;
 *  The index of the class for which IR statistics
 *  are to be output. (default 1)
 * </pre>
 * 
 * <pre>
 * -I &lt;index&gt;
 *  The index of an attribute to output in the
 *  results. This attribute should identify an
 *  instance in order to know which instances are
 *  in the test set of a cross validation. if 0
 *  no output (default 0).
 * </pre>
 * 
 * <pre>
 * -P
 *  Add target and prediction columns to the result
 *  for each fold.
 * </pre>
 * 
 * <pre>
 * Options specific to classifier weka.classifiers.rules.ZeroR:
 * </pre>
 * 
 * <pre>
 * -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * All options after -- will be passed to the split evaluator.
 * 
 * @author Len Trigg
 * @author Eibe Frank
 * @version $Revision: 10203 $
 */
public class MekaCrossValidationSplitResultProducer
	extends CrossValidationSplitResultProducer
	implements MekaResultProducer {

	/** for serialization */
	static final long serialVersionUID = 1403798164046795073L;

	/** the total number of classes. */
	protected int m_TotalNumClasses = 0;

	/**
	 * Initializes the producer.
	 */
	public MekaCrossValidationSplitResultProducer() {
		m_SplitEvaluator = new MekaClassifierSplitEvaluator();
	}

	/**
	 * Sets the overal number of classes.
	 * 
	 * @param value			the number of classes
	 */
	public void setTotalNumClasses(int value) {
		m_TotalNumClasses = value;
		if (m_SplitEvaluator instanceof MekaSplitEvaluator)
			((MekaSplitEvaluator) m_SplitEvaluator).setTotalNumClasses(value);
	}

	/**
	 * Returns the overal number of classes.
	 * 
	 * @return			the number of classes
	 */
	public int getTotalNumClasses() {
		return m_TotalNumClasses;
	}
	
	/**
	 * Set the SplitEvaluator.
	 * 
	 * @param newSplitEvaluator new SplitEvaluator to use.
	 */
	@Override
	public void setSplitEvaluator(SplitEvaluator newSplitEvaluator) {
		if (newSplitEvaluator instanceof MekaSplitEvaluator)
			super.setSplitEvaluator(newSplitEvaluator);
		else
			throw new IllegalArgumentException(
					"Split evaluator must implement " + MekaSplitEvaluator.class.getName() 
					+ ", provided: " + newSplitEvaluator.getClass().getName());
	}

	/**
	 * Gets the results for a specified run number. Different run numbers
	 * correspond to different randomizations of the data. Results produced should
	 * be sent to the current ResultListener
	 * 
	 * @param run the run number to get results for.
	 * @throws Exception if a problem occurs while getting the results
	 */
	@Override
	public void doRun(int run) throws Exception {

		if (getRawOutput()) {
			if (m_ZipDest == null) {
				m_ZipDest = new OutputZipper(m_OutputFile);
			}
		}

		if (m_Instances == null) {
			throw new Exception("No Instances set");
		}

		// Compute run and fold number from given run
		int fold = (run - 1) % m_NumFolds;
		run = ((run - 1) / m_NumFolds) + 1;

		// Randomize on a copy of the original dataset
		Instances runInstances = new Instances(m_Instances);
		Random random = new Random(run);
		runInstances.randomize(random);

		// Add in some fields to the key like run and fold number, dataset name
		Object[] seKey = m_SplitEvaluator.getKey();
		Object[] key = new Object[seKey.length + 3];
		key[0] = Utils.backQuoteChars(m_Instances.relationName());
		key[1] = "" + run;
		key[2] = "" + (fold + 1);
		System.arraycopy(seKey, 0, key, 3, seKey.length);
		if (m_ResultListener.isResultRequired(this, key)) {

			// Just to make behaviour absolutely consistent with
			// CrossValidationResultProducer
			for (int tempFold = 0; tempFold < fold; tempFold++) {
				runInstances.trainCV(m_NumFolds, tempFold, random);
			}

			Instances train = runInstances.trainCV(m_NumFolds, fold, random);
			Instances test = runInstances.testCV(m_NumFolds, fold);
			MLUtils.prepareData(train);
			MLUtils.prepareData(test);
			try {
				Object[] seResults = m_SplitEvaluator.getResult(train, test);
				Object[] results = new Object[seResults.length + 1];
				results[0] = getTimestamp();
				System.arraycopy(seResults, 0, results, 1, seResults.length);
				if (m_debugOutput) {
					String resultName = ("" + run + "." + (fold + 1) + "."
							+ Utils.backQuoteChars(runInstances.relationName()) + "." + m_SplitEvaluator
							.toString()).replace(' ', '_');
					resultName = Utils.removeSubstring(resultName, "weka.classifiers.");
					resultName = Utils.removeSubstring(resultName, "meka.classifiers.");
					resultName = Utils.removeSubstring(resultName, "weka.filters.");
					resultName = Utils.removeSubstring(resultName, "meka.filters.");
					resultName = Utils.removeSubstring(resultName, "weka.attributeSelection.");
					m_ZipDest.zipit(m_SplitEvaluator.getRawResultOutput(), resultName);
				}
				m_ResultListener.acceptResult(this, key, results);
			} catch (Exception ex) {
				// Save the train and test datasets for debugging purposes?
				throw ex;
			}
		}
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 10203 $");
	}
} // CrossValidationSplitResultProducer

