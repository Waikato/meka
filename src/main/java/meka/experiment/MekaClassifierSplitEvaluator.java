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
 *    MekaClassifierSplitEvaluator.java
 *    Copyright (C) 1999-2014 University of Waikato, Hamilton, New Zealand
 *
 */

package meka.experiment;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Result;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.experiment.ClassifierSplitEvaluator;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * <!-- globalinfo-start --> A SplitEvaluator that produces results for a
 * classification scheme on a nominal class attribute.
 * <p>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p>
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
 * -no-size
 *  Skips the determination of sizes (train/test/classifier)
 *  (default: sizes are determined)
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
 * All options after -- will be passed to the classifier.
 * 
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10376 $
 */
public class MekaClassifierSplitEvaluator 
	extends ClassifierSplitEvaluator
	implements MekaSplitEvaluator {

	/** for serialization */
	static final long serialVersionUID = -8511241602760467265L;

	/** The length of a key */
	private static final int KEY_SIZE = 3;

	/** The length of a result */
	private static final int RESULT_SIZE = 27;

	/** the total number of classes. */
	protected int m_TotalNumClasses = 0;

	/**
	 * No args constructor.
	 */
	public MekaClassifierSplitEvaluator() {
		super();
		m_Template = new meka.classifiers.multilabel.BR();
		updateOptions();
	}

	/**
	 * Sets the overal number of classes.
	 * 
	 * @param value			the number of classes
	 */
	public void setTotalNumClasses(int value) {
		m_TotalNumClasses = value;
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
	 * Returns a string describing this split evaluator
	 * 
	 * @return a description of the split evaluator suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	@Override
	public String globalInfo() {
		return " A SplitEvaluator that produces results for a MEKA classification "
				+ "scheme on a nominal class attribute.";
	}

	/**
	 * Gets the data types of each of the key columns produced for a single run.
	 * The number of key fields must be constant for a given SplitEvaluator.
	 * 
	 * @return an array containing objects of the type of each key column. The
	 *         objects should be Strings, or Doubles.
	 */
	@Override
	public Object[] getKeyTypes() {

		Object[] keyTypes = new Object[KEY_SIZE];
		keyTypes[0] = "";
		keyTypes[1] = "";
		keyTypes[2] = "";
		return keyTypes;
	}

	/**
	 * Gets the names of each of the key columns produced for a single run. The
	 * number of key fields must be constant for a given SplitEvaluator.
	 * 
	 * @return an array containing the name of each key column
	 */
	@Override
	public String[] getKeyNames() {

		String[] keyNames = new String[KEY_SIZE];
		keyNames[0] = "Scheme";
		keyNames[1] = "Scheme_options";
		keyNames[2] = "Scheme_version_ID";
		return keyNames;
	}

	/**
	 * Gets the key describing the current SplitEvaluator. For example This may
	 * contain the name of the classifier used for classifier predictive
	 * evaluation. The number of key fields must be constant for a given
	 * SplitEvaluator.
	 * 
	 * @return an array of objects containing the key.
	 */
	@Override
	public Object[] getKey() {

		Object[] key = new Object[KEY_SIZE];
		key[0] = m_Template.getClass().getName();
		key[1] = m_ClassifierOptions;
		key[2] = m_ClassifierVersion;
		return key;
	}

	/**
	 * Gets the data types of each of the result columns produced for a single
	 * run. The number of result fields must be constant for a given
	 * SplitEvaluator.
	 * 
	 * @return an array containing objects of the type of each result column. The
	 *         objects should be Strings, or Doubles.
	 */
	@Override
	public Object[] getResultTypes() {
		int addm = (m_AdditionalMeasures != null) ? m_AdditionalMeasures.length : 0;
		int overall_length = RESULT_SIZE + addm + m_TotalNumClasses*2;

		if (getAttributeID() >= 0)
			overall_length += 1;

		Object[] resultTypes = new Object[overall_length];
		Double doub = new Double(0);
		int current = 0;

		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;

		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;

		for (int i = 0; i < m_TotalNumClasses; i++) {
			resultTypes[current++] = doub;
			resultTypes[current++] = doub;
		}
		
		// sizes
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;
		resultTypes[current++] = doub;

		// ID/Targets/Predictions
		if (getAttributeID() >= 0)
			resultTypes[current++] = "";

		// Classifier defined extras
		resultTypes[current++] = "";

		// add any additional measures
		for (int i = 0; i < addm; i++)
			resultTypes[current++] = doub;

		if (current != overall_length)
			throw new Error("ResultTypes didn't fit RESULT_SIZE: " + current + " != " + RESULT_SIZE);
		
		return resultTypes;
	}

	/**
	 * Gets the names of each of the result columns produced for a single run. The
	 * number of result fields must be constant for a given SplitEvaluator.
	 * 
	 * @return an array containing the name of each result column
	 */
	@Override
	public String[] getResultNames() {
		int addm = (m_AdditionalMeasures != null) ? m_AdditionalMeasures.length : 0;
		int overall_length = RESULT_SIZE + addm + m_TotalNumClasses*2;
		if (getAttributeID() >= 0)
			overall_length += 1;

		String[] resultNames = new String[overall_length];
		int current = 0;

		resultNames[current++] = "N_train";
		resultNames[current++] = "N_test";
		resultNames[current++] = "LCard_train";
		resultNames[current++] = "LCard_test";
		resultNames[current++] = "Build_time";
		resultNames[current++] = "Test_time";
		resultNames[current++] = "Total_time";
		
		resultNames[current++] = "Accuracy";
		resultNames[current++] = "Hamming_score";
		resultNames[current++] = "Exact_match";
		resultNames[current++] = "Jaccard_dist";
		resultNames[current++] = "Hamming_loss";
		resultNames[current++] = "ZeroOne_loss";
		resultNames[current++] = "Harmonic_score";
		resultNames[current++] = "One_error";
		resultNames[current++] = "Rank_loss";
		resultNames[current++] = "Avg_precision";
		resultNames[current++] = "Log_Loss_D";
		resultNames[current++] = "Log_Loss_L";
		resultNames[current++] = "F-micro";
		resultNames[current++] = "F-macro_D";
		resultNames[current++] = "F-macro_L";
		resultNames[current++] = "N_empty";

		for (int i = 0; i < m_TotalNumClasses; i++) {
			resultNames[current++] = "Accuracy[" + i + "]";
			resultNames[current++] = "Harmonic[" + i + "]";
		}
		
		// sizes
	    resultNames[current++] = "Serialized_Model_Size";
	    resultNames[current++] = "Serialized_Train_Set_Size";
	    resultNames[current++] = "Serialized_Test_Set_Size";

		// ID
		if (getAttributeID() >= 0)
			resultNames[current++] = "Instance_ID";

		// Classifier defined extras
		resultNames[current++] = "Summary";
		// add any additional measures
		for (int i = 0; i < addm; i++)
			resultNames[current++] = m_AdditionalMeasures[i];

		if (current != overall_length)
			throw new Error("ResultNames didn't fit RESULT_SIZE");

		return resultNames;
	}

	/**
	 * Returns a value from the evaluation metric. Missing value if not
	 * available.
	 * 
	 * @param values	the evaluation metrics to use
	 * @param name		the name of the metric to retrieve
	 * @return			the value or missing value if not available
	 */
	protected Double getEvaluationMetric(HashMap<String,Object> values, String name) {
		if (values.containsKey(name))
			return (Double)values.get(name);
		else
			return Utils.missingValue();
	}
	
	/**
	 * Gets the results for the supplied train and test datasets. Now performs a
	 * deep copy of the classifier before it is built and evaluated (just in case
	 * the classifier is not initialized properly in buildClassifier()).
	 * 
	 * @param train the training Instances.
	 * @param test the testing Instances.
	 * @return the results stored in an array. The objects stored in the array may
	 *         be Strings, Doubles, or null (for the missing value).
	 * @throws Exception if a problem occurs while getting the results
	 */
	@Override
	public Object[] getResult(Instances train, Instances test) throws Exception {
		if (m_Template == null)
			throw new Exception("No classifier has been specified");
		int addm = (m_AdditionalMeasures != null) ? m_AdditionalMeasures.length : 0;
		int overall_length = RESULT_SIZE + addm + m_TotalNumClasses*2;
		if (getAttributeID() >= 0)
			overall_length += 1;

		Object[] result = new Object[overall_length];
		m_Classifier = AbstractClassifier.makeCopy(m_Template);

		// evaluate classifier
	    Result res = Evaluation.evaluateModel((MultiLabelClassifier) m_Classifier, train, test, "PCut1", "3");
	    HashMap<String,Object> map = Result.getStats(res, "3");

		m_result = res.toString();
		int current = 0;
		
		/*
		result[current++] = res.getValue("N_train");
		result[current++] = res.getValue("N_test");
		result[current++] = res.getValue("LCard_train");
		result[current++] = res.getValue("LCard_test");
		result[current++] = res.getValue("Build_time");
		result[current++] = res.getValue("Test_time");
		result[current++] = res.getValue("Total_time");
		*/

		result[current++] = getEvaluationMetric(map, "Accuracy");
		result[current++] = getEvaluationMetric(map, "Hamming score");
		result[current++] = getEvaluationMetric(map, "Exact match");
		/*
		result[current++] = getEvaluationMetric(map, "Jaccard dist");
		result[current++] = getEvaluationMetric(map, "Hamming loss");
		result[current++] = getEvaluationMetric(map, "ZeroOne loss");
		result[current++] = getEvaluationMetric(map, "Harmonic score");
		result[current++] = getEvaluationMetric(map, "One error");
		result[current++] = getEvaluationMetric(map, "Rank loss");
		result[current++] = getEvaluationMetric(map, "Avg precision");
		result[current++] = getEvaluationMetric(map, "Log Loss D");
		result[current++] = getEvaluationMetric(map, "Log Loss L");
		result[current++] = getEvaluationMetric(map, "F-micro");
		result[current++] = getEvaluationMetric(map, "F-macro_D");
		result[current++] = getEvaluationMetric(map, "F-macro_L");
		result[current++] = getEvaluationMetric(map, "N_empty");

		for (int i = 0; i < m_TotalNumClasses; i++) {
			result[current++] = getEvaluationMetric(map, "Accuracy[" + i + "]");
			result[current++] = getEvaluationMetric(map, "Harmonic[" + i + "]");
		}
		*/

		// sizes
		if (getNoSizeDetermination()) {
			result[current++] = -1.0;
			result[current++] = -1.0;
			result[current++] = -1.0;
		} else {
			ByteArrayOutputStream bastream = new ByteArrayOutputStream();
			ObjectOutputStream oostream = new ObjectOutputStream(bastream);
			oostream.writeObject(m_Classifier);
			result[current++] = new Double(bastream.size());
			bastream = new ByteArrayOutputStream();
			oostream = new ObjectOutputStream(bastream);
			oostream.writeObject(train);
			result[current++] = new Double(bastream.size());
			bastream = new ByteArrayOutputStream();
			oostream = new ObjectOutputStream(bastream);
			oostream.writeObject(test);
			result[current++] = new Double(bastream.size());
		}

		// IDs
		if (getAttributeID() >= 0) {
			String idsString = "";
			if (test.attribute(getAttributeID()).isNumeric()) {
				if (test.numInstances() > 0)
					idsString += test.instance(0).value(getAttributeID());
				for (int i = 1; i < test.numInstances(); i++)
					idsString += "|" + test.instance(i).value(getAttributeID());
			} 
			else {
				if (test.numInstances() > 0)
					idsString += test.instance(0).stringValue(getAttributeID());
				for (int i = 1; i < test.numInstances(); i++)
					idsString += "|" + test.instance(i).stringValue(getAttributeID());
			}
			result[current++] = idsString;
		}

		if (m_Classifier instanceof Summarizable)
			result[current++] = ((Summarizable) m_Classifier).toSummaryString();
		else
			result[current++] = null;

		for (int i = 0; i < addm; i++) {
			if (m_doesProduce[i]) {
				try {
					double dv = ((AdditionalMeasureProducer) m_Classifier).getMeasure(m_AdditionalMeasures[i]);
					if (!Utils.isMissingValue(dv)) {
						Double value = new Double(dv);
						result[current++] = value;
					} 
					else {
						result[current++] = null;
					}
				} 
				catch (Exception ex) {
					System.err.println(ex);
				}
			} 
			else {
				result[current++] = null;
			}
		}

		if (current != overall_length)
			throw new Error("Results didn't fit RESULT_SIZE");
		
		return result;
	}

	/**
	 * Sets the classifier.
	 * 
	 * @param newClassifier the new classifier to use.
	 */
	@Override
	public void setClassifier(Classifier newClassifier) {
		if (newClassifier instanceof MultiLabelClassifier)
			super.setClassifier(newClassifier);
		else
			throw new IllegalArgumentException(
					"Classifier must be a " + MultiLabelClassifier.class.getName()
					+ ", provided: " + newClassifier.getClass().getName());
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	@Override
	public String classifierTipText() {
		return super.classifierTipText() + ", must be a " + MultiLabelClassifier.class.getName();
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 10376 $");
	}
}
