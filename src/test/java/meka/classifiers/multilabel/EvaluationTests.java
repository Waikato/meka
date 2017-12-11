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

package meka.classifiers.multilabel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import meka.classifiers.multilabel.incremental.CCUpdateable;
import meka.classifiers.incremental.IncrementalEvaluation;
import meka.classifiers.multilabel.incremental.meta.BaggingMLUpdateable;
import meka.classifiers.multilabel.meta.BaggingML;
import meka.core.MLUtils;
import meka.core.Metrics;
import meka.core.Result;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * EvaluationTests. Run from the command line with:<p/>
 * java meka.classifiers.multilabel.EvaluationTests
 *
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version $Revision: 66 $
 */
public class EvaluationTests extends TestCase {

	public EvaluationTests(String s) {
		super(s);
	}

	public static Test suite() {
		return new TestSuite(EvaluationTests.class);
	}

	protected void setUp(){
		// Preparation of the unit tests
	}

	protected void tearDown(){
		// Teardown for data used by the unit tests
	}

	public static Instances loadInstances(String fn) {
		try {
			Instances D = DataSource.read("src/test/resources/" + fn);
			MLUtils.prepareData(D);
			return D;
		} catch(Exception e) {
			System.err.println("");
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public void testRepeatable() {
		// Load Music
		Instances D = loadInstances("Music.arff");
		Instances D_train = new Instances(D,0,400);
		Instances D_test = new Instances(D,400,D.numInstances()-400);
		// Train ECC
		MultiLabelClassifier h = makeECC();
		// Eval
		try {
			Result r1 = Evaluation.evaluateModel(h, D_train, D_test, "PCut1");
			Result r2 = Evaluation.evaluateModel(h, D_train, D_test, "PCut1");
			assertTrue("Experiments are Repeatable (with same result)", r1.getMeasurement("Accuracy").equals(r2.getMeasurement("Accuracy")));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public MultiLabelClassifier makeECC() {
		BaggingML h = new BaggingML();
		CC cc = new CC();
		cc.setClassifier(new SMO());
		h.setClassifier(cc);
		return h;
	}

	public void testMulanFormat() {
		Result r1 = null, r2 = null;
		// Load Music-train
		Instances D_train = loadInstances("Music-train.arff");
		// Load Music-test
		Instances D_test = loadInstances("Music-test.arff");
		// Train CC
		CC h = new CC();
		h.setClassifier(new SMO());
		// Eval
		try {
			r1 = Evaluation.evaluateModel(h, D_train, D_test, "PCut1");
		} catch(Exception e) {
			e.printStackTrace();
		}

		// Load Music
		Instances D = loadInstances("Music.arff");
		D_train = new Instances(D,0,491);
		D_test = new Instances(D,491,D.numInstances()-491);
		// Eval
		try {
			r2 = Evaluation.evaluateModel(h, D_train, D_test, "PCut1");
		} catch(Exception e) {
			e.printStackTrace();
		}

		assertTrue("Mulan Format OK? (same result?)", r1.getMeasurement("Accuracy").equals(r2.getMeasurement("Accuracy")));
	}

	public void testIncrementalEvaluation() {
		// Batch
		Result r1 = null, r2 = null;
		// Load Data
		Instances D = loadInstances("Music.arff");
		// Train ECCUpdateable
		BaggingMLUpdateable h = new BaggingMLUpdateable();
		CCUpdateable cc = new CCUpdateable();
		cc.setClassifier(new IBk());
		h.setClassifier(cc);
		try {
			r1 = IncrementalEvaluation.evaluateModel(h,D);
			r2 = IncrementalEvaluation.evaluateModel(h,D);
		} catch(Exception e) {
			System.err.println("FAILED TO GET r1, r2");
			e.printStackTrace();
		}
		// Good @TODO
		//assertTrue("Inc. Eval OK? ?", r1.info.get("Accuracy").equals("0.486 +/- 0.045"));
		// The same?
		if (r1==null)
			System.out.println("r1 is null");
		if (r2==null)
			System.out.println("r2 is null");

		assertTrue("Inc. Eval the same?", ((String)r1.getMeasurement("Accuracy")).equals(((String)r2.getMeasurement("Accuracy"))));
		// test/train

		// compare with non-ss
	}

	// TESTS
	public void testMetrics() {
		double log_loss_L = 0.0;
		double w[] = new double[]{0.5,0.0,0.4,0.2};
		double y[] = new double[]{1.0,0.0,0.0,1.0};
		int L = w.length;
		double lim = Math.log(100);
		for(int j = 0; j < L; j++) {
			log_loss_L += Metrics.L_LogLoss(y[j],w[j],lim);
		}
		assertTrue("Log Loss OK?", log_loss_L == 2.8134107167600364);
	}

	public void testThreshold() {
		BaggingML h = new BaggingML();
		CC cc = new CC();
		cc.setClassifier(new Logistic());
		h.setClassifier(cc);
		Result r = EvaluationTests.cvEvaluateClassifier(h,"0.5");
		assertTrue("PCutL Thresholds OK?", r.info.get("Threshold").equals("[0.4, 0.4, 0.4, 0.4, 0.6, 0.6]") );
	}

	public static Result cvEvaluateClassifier(MultiLabelClassifier h) {
		return cvEvaluateClassifier(h,"0.5");
	}

	public static Result cvEvaluateClassifier(MultiLabelClassifier h, String top) {
		Instances D = null;
		try {
			D = EvaluationTests.loadInstances("Music.arff");
			Result result = Evaluation.cvModel(h,D,5,top,"7");
			return result;
		} catch(Exception e) {
			System.err.println("");
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
} 

