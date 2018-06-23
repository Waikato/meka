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
 * MultiSearch.java
 * Copyright (C) 2008-2017 University of Waikato, Hamilton, New Zealand
 */

package meka.classifiers.multitarget.meta;

import meka.classifiers.AbstractMultiSearch;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.classifiers.multitarget.MultiTargetClassifier;
import meka.classifiers.multitarget.RAkELd;
import weka.classifiers.Classifier;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.setupgenerator.AbstractParameter;
import weka.core.setupgenerator.MathParameter;

/**
 <!-- globalinfo-start -->
 * Performs a search of an arbitrary number of parameters of a classifier and chooses the best combination found.<br>
 * The properties being explored are totally up to the user.<br>
 * <br>
 * E.g., if you have a FilteredClassifier selected as base classifier, sporting a PLSFilter and you want to explore the number of PLS components, then your property will be made up of the following components:<br>
 *  - filter: referring to the FilteredClassifier's property (= PLSFilter)<br>
 *  - numComponents: the actual property of the PLSFilter that we want to modify<br>
 * And assembled, the property looks like this:<br>
 *   filter.numComponents<br>
 * <br>
 * <br>
 * The best classifier setup can be accessed after the buildClassifier call via the getBestClassifier method.<br>
 * <br>
 * The trace of setups evaluated can be accessed after the buildClassifier call as well, using the following methods:<br>
 * - getTrace()<br>
 * - getTraceSize()<br>
 * - getTraceValue(int)<br>
 * - getTraceFolds(int)<br>
 * - getTraceClassifierAsCli(int)<br>
 * - getTraceParameterSettings(int)<br>
 * <br>
 * Using the weka.core.setupgenerator.ParameterGroup parameter, it is possible to group dependent parameters. In this case, all top-level parameters must be of type weka.core.setupgenerator.ParameterGroup.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 *
 * <pre> -E &lt;ACC|JIDX|HSCORE|EM|JDIST|HLOSS|ZOLOSS|HARSCORE|OE|RLOSS|AVGPREC|LOGLOSSL|LOGLOSSD|F1MICRO|F1MACROEX|F1MACROLBL|AUPRC|AUROC|LCARD|LDIST&gt;
 *  Determines the parameter used for evaluation:
 *  ACC = Accuracy
 *  JIDX = Jaccard index
 *  HSCORE = Hamming score
 *  EM = Exact match
 *  JDIST = Jaccard distance
 *  HLOSS = Hamming loss
 *  ZOLOSS = ZeroOne loss
 *  HARSCORE = Harmonic score
 *  OE = One error
 *  RLOSS = Rank loss
 *  AVGPREC = Avg precision
 *  LOGLOSSL = Log Loss (lim. L)
 *  LOGLOSSD = Log Loss (lim. D)
 *  F1MICRO = F1 (micro averaged)
 *  F1MACROEX = F1 (macro averaged by example)
 *  F1MACROLBL = F1 (macro averaged by label)
 *  AUPRC = AUPRC (macro averaged)
 *  AUROC = AUROC (macro averaged)
 *  LCARD = Label cardinality (predicted)
 *  LDIST = Levenshtein distance
 *  (default: ACC)</pre>
 *
 * <pre> -search "&lt;classname options&gt;"
 *  A property search setup.
 * </pre>
 *
 * <pre> -algorithm "&lt;classname options&gt;"
 *  A search algorithm.
 * </pre>
 *
 * <pre> -log-file &lt;filename&gt;
 *  The log file to log the messages to.
 *  (default: none)</pre>
 *
 * <pre> -S &lt;num&gt;
 *  Random number seed.
 *  (default 1)</pre>
 *
 * <pre> -W
 *  Full name of base classifier.
 *  (default: meka.classifiers.multitarget.RAkELd)</pre>
 *
 * <pre> -output-debug-info
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 *
 * <pre> -do-not-check-capabilities
 *  If set, classifier capabilities are not checked before classifier is built
 *  (use with caution).</pre>
 *
 * <pre> -num-decimal-places
 *  The number of decimal places for the output of numbers in the model (default 2).</pre>
 *
 * <pre> -batch-size
 *  The desired batch size for batch prediction  (default 100).</pre>
 *
 * <pre>
 * Options specific to classifier meka.classifiers.multitarget.RAkELd:
 * </pre>
 *
 * <pre> -k &lt;num&gt;
 *  The number of labels in each partition -- should be 1 &lt;= k &lt; (L/2) where L is the total number of labels.</pre>
 *
 * <pre> -P &lt;value&gt;
 *  Sets the pruning value, defining an infrequent labelset as one which occurs &lt;= P times in the data (P = 0 defaults to LC).
 *  default: 0 (LC)</pre>
 *
 * <pre> -N &lt;value&gt;
 *  Sets the (maximum) number of frequent labelsets to subsample from the infrequent labelsets.
 *  default: 0 (none)
 *  n N = n
 *  -n N = n, or 0 if LCard(D) &gt;= 2
 *  n-m N = random(n,m)</pre>
 *
 * <pre> -S &lt;value&gt;
 *  The seed value for randomization
 *  default: 0</pre>
 *
 * <pre> -W
 *  Full name of base classifier.
 *  (default: weka.classifiers.trees.J48)</pre>
 *
 * <pre> -output-debug-info
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 *
 * <pre> -do-not-check-capabilities
 *  If set, classifier capabilities are not checked before classifier is built
 *  (use with caution).</pre>
 *
 * <pre> -num-decimal-places
 *  The number of decimal places for the output of numbers in the model (default 2).</pre>
 *
 * <pre> -batch-size
 *  The desired batch size for batch prediction  (default 100).</pre>
 *
 * <pre>
 * Options specific to classifier weka.classifiers.trees.J48:
 * </pre>
 *
 * <pre> -U
 *  Use unpruned tree.</pre>
 *
 * <pre> -O
 *  Do not collapse tree.</pre>
 *
 * <pre> -C &lt;pruning confidence&gt;
 *  Set confidence threshold for pruning.
 *  (default 0.25)</pre>
 *
 * <pre> -M &lt;minimum number of instances&gt;
 *  Set minimum number of instances per leaf.
 *  (default 2)</pre>
 *
 * <pre> -R
 *  Use reduced error pruning.</pre>
 *
 * <pre> -N &lt;number of folds&gt;
 *  Set number of folds for reduced error
 *  pruning. One fold is used as pruning set.
 *  (default 3)</pre>
 *
 * <pre> -B
 *  Use binary splits only.</pre>
 *
 * <pre> -S
 *  Do not perform subtree raising.</pre>
 *
 * <pre> -L
 *  Do not clean up after the tree has been built.</pre>
 *
 * <pre> -A
 *  Laplace smoothing for predicted probabilities.</pre>
 *
 * <pre> -J
 *  Do not use MDL correction for info gain on numeric attributes.</pre>
 *
 * <pre> -Q &lt;seed&gt;
 *  Seed for random data shuffling (default 1).</pre>
 *
 * <pre> -doNotMakeSplitPointActualValue
 *  Do not make split point actual value.</pre>
 *
 * <pre> -output-debug-info
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 *
 * <pre> -do-not-check-capabilities
 *  If set, classifier capabilities are not checked before classifier is built
 *  (use with caution).</pre>
 *
 * <pre> -num-decimal-places
 *  The number of decimal places for the output of numbers in the model (default 2).</pre>
 *
 * <pre> -batch-size
 *  The desired batch size for batch prediction  (default 100).</pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4521 $
 */
public class MultiSearch
	extends AbstractMultiSearch
	implements MultiLabelClassifier, MultiTargetClassifier {

	/** for serialization. */
	private static final long serialVersionUID = -5129316523575906233L;

	/**
	 * Returns the default classifier to use.
	 *
	 * @return		the default classifier
	 */
	protected Classifier defaultClassifier() {
		return new RAkELd();
	}

	/**
	 * Returns the default search parameters.
	 *
	 * @return		the parameters
	 */
	protected AbstractParameter[] defaultSearchParameters() {
		AbstractParameter[] 	result;
		MathParameter param;

		result = new AbstractParameter[1];

		param = new MathParameter();
		param.setProperty("K");
		param.setMin(1);
		param.setMax(3);
		param.setStep(1);
		param.setBase(10);
		param.setExpression("I");
		result[0] = param;

		try {
			result = (AbstractParameter[]) new SerializedObject(result).getObject();
		}
		catch (Exception e) {
			result = new AbstractParameter[0];
			System.err.println("Failed to create copy of default parameters!");
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Set the base learner.
	 *
	 * @param newClassifier 	the classifier to use.
	 */
	@Override
	public void setClassifier(Classifier newClassifier) {
		if (!(newClassifier instanceof MultiTargetClassifier))
			throw new IllegalStateException(
				"Base classifier must implement " + MultiTargetClassifier.class.getName()
					+ ", provided: " + newClassifier.getClass().getName());
		super.setClassifier(newClassifier);
	}

	/**
	 * Returns the revision string.
	 *
	 * @return		the revision
	 */
	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 4521 $");
	}

	/**
	 * Main method for running this classifier from commandline.
	 *
	 * @param args 	the options
	 */
	public static void main(String[] args) {
		ProblemTransformationMethod.evaluation(new MultiSearch(), args);
	}
}
