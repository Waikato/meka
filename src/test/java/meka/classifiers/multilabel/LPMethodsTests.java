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
import junit.framework.TestSuite;
import junit.framework.TestCase;

import meka.classifiers.AbstractMekaClassifierTest;
import weka.classifiers.Classifier;
import meka.classifiers.multilabel.*;
import meka.classifiers.multilabel.meta.*;
import weka.classifiers.functions.SMO;
import weka.core.converters.ConverterUtils.DataSource;
import meka.core.Result;
import meka.core.MLEvalUtils;

import meka.gui.explorer.Explorer;

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
		LCe lc = new LCe();
		lc.setClassifier(new SMO());
		r = EvaluationTests.cvEvaluateClassifier(lc);
		String s = r.info.get("Accuracy");
		System.out.println("LCe "+s);
		assertTrue("LCe Accuracy Correct", s.equals("0.568 +/- 0.032"));

		// Test PSe (0,0) -- should be identical
		PSe pse = new PSe();
		pse.setClassifier(new SMO());
		r = EvaluationTests.cvEvaluateClassifier(pse);
		System.out.println("PSe "+r.info.get("Accuracy"));
		assertTrue("PSe(0,0) Accuracy Identical to LCe", s.equals(r.info.get("Accuracy")));

		// Test PS (0,0) -- should be identical to PSe (though it is not, currently ...)
		PS ps = new PS();
		ps.setClassifier(new SMO());
		r = EvaluationTests.cvEvaluateClassifier(ps);
		System.out.println("PS(0,0) "+r.info.get("Accuracy"));
		assertTrue("PS(0,0) Accuracy Correct", r.info.get("Accuracy").startsWith("0.569"));

		// Test PS (3,1) -- should be faster 
		ps.setP(3);
		ps.setN(1);

		r = EvaluationTests.cvEvaluateClassifier(ps);
		System.out.println("PS(3,1) "+r.info.get("Accuracy"));
		assertTrue("PS(3,1) Accuracy Correct", r.info.get("Accuracy").startsWith("0.567"));

		// Test PSe -- should be very similar (differences only from different randomization)
		pse.setP(3);
		pse.setN(1);

		r = EvaluationTests.cvEvaluateClassifier(pse);
		System.out.println("PSe(3,1) "+r.info.get("Accuracy"));
		assertTrue("PSe(3,1) Accuracy Correct", r.info.get("Accuracy").equals("0.567 +/- 0.044") );

		// Test EPSe
		EnsembleML eps = new EnsembleML();
		eps.setClassifier(pse);
		r = EvaluationTests.cvEvaluateClassifier(eps);
		System.out.println("EPSe "+r.info.get("Accuracy"));
		assertTrue("EPSe Accuracy Correct", r.info.get("Accuracy").equals("0.569 +/- 0.044") );
	}

}
