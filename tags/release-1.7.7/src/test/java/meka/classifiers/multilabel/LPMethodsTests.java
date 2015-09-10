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
import meka.classifiers.multilabel.meta.EnsembleML;
import meka.core.Result;
import meka.test.TestHelper;
import weka.classifiers.functions.SMO;

/**
 * EvaluationTests. Run from the command line with:<p/>
 * java meka.classifiers.multilabel.EvaluationTests
 *
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version $Revision: 66 $
 */
public class LPMethodsTests extends TestCase {

	public LPMethodsTests(String s) {
		super(s);
	}

	public static Test suite() {
		return new TestSuite(LPMethodsTests.class);
	}

	protected void setUp(){
		// Preparation of the unit tests
	}

	protected void tearDown(){
		// Teardown for data used by the unit tests
	}

	public void testLC() {

		Result r = null;

		// Test LC
		LC lc = new LC();
		lc.setClassifier(new SMO());
		r = EvaluationTests.cvEvaluateClassifier(lc);
		String s = (String)r.output.get("Accuracy");
		System.out.println("LC "+s);
		TestHelper.assertAlmostEquals("LC Accuracy Correct", "0.568 +/- 0.032", (String)s, 1);

		// Test PS (0,0) -- should be identical
		PS ps = new PS();
		ps.setClassifier(new SMO());
		r = EvaluationTests.cvEvaluateClassifier(ps);
		System.out.println("PS "+r.output.get("Accuracy"));
		assertTrue("PS(0,0) Accuracy Identical to LC", s.equals(r.output.get("Accuracy")));

		// Test PS (3,1) -- should be faster 
		ps.setP(3);
		ps.setN(1);

		r = EvaluationTests.cvEvaluateClassifier(ps);
		System.out.println("PS(3,1) "+r.output.get("Accuracy"));
		TestHelper.assertAlmostEquals("PS(3,1) Accuracy Correct", "0.565 +/- 0.04", (String)r.output.get("Accuracy"), 1);

		// Test EPS
		EnsembleML eps = new EnsembleML();
		eps.setClassifier(ps);
		r = EvaluationTests.cvEvaluateClassifier(eps);
		System.out.println("EPS "+r.output.get("Accuracy"));
		TestHelper.assertAlmostEquals("EPS Accuracy Correct", "0.574 +/- 0.042", (String)r.output.get("Accuracy"), 1);
	}

}
