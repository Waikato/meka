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
import meka.classifiers.multilabel.meta.DeepML;
import meka.core.Result;
import weka.classifiers.functions.SMO;
import weka.core.Utils;

/**
 * EvaluationTests. Run from the command line with:<p/>
 * java meka.classifiers.multilabel.EvaluationTests
 *
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version $Revision: 66 $
 */
public class DeepMethodsTests extends TestCase {

	public DeepMethodsTests(String s) {
		super(s);
		System.out.println("Evaluation Test");
	}

	public static Test suite() {
		return new TestSuite(DeepMethodsTests.class);
	}

	protected void setUp(){
		// Preparation of the unit tests
	}

	protected void tearDown(){
		// Teardown for data used by the unit tests
	}

	public void testDeepML() {
		System.out.println("Test Stacked Boltzmann Machines with an off-the-shelf multi-label classifier");
		DeepML dbn = new DeepML();

		MCC h = new MCC();
		SMO smo = new SMO();
		smo.setBuildCalibrationModels(true);
		h.setClassifier(smo);

		dbn.setClassifier(h);
		dbn.setE(100);
		dbn.setH(30);

		Result r = EvaluationTests.cvEvaluateClassifier(dbn);
		System.out.println("DeepML + MCC" + r.getMeasurement("Accuracy"));
		String s = (String)r.getMeasurement("Accuracy");
		assertTrue("DeepML+MCC Accuracy Correct", s.startsWith("0.53")); // Good enough 
	}

	public void testDBPNN() {
		
		System.out.println("Test Back Prop Neural Network with pre-trained weights (via RBM)");
		DBPNN dbn = new DBPNN();
		dbn.setClassifier(new BPNN());
		dbn.setDebug(true);

		try {
			dbn.setOptions(Utils.splitOptions("-H 30 -E 500 -r 0.1 -m 0.2"));
		} catch(Exception e) {
			System.err.println("Fatal Error");
			e.printStackTrace();
			System.exit(1);
		}
		Result r = EvaluationTests.cvEvaluateClassifier(dbn);
		String s = (String)r.getMeasurement("Accuracy");
		System.out.println("DBPNN + _" + r.getMeasurement("Accuracy"));
		assertTrue("DBPNN Accuracy Correct", s.equals("0.556 +/- 0.038"));
	}

}
