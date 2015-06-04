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
		LC lc = new LC();
		lc.setClassifier(new SMO());
		r = EvaluationTests.cvEvaluateClassifier(lc);
		String s = r.info.get("Accuracy");
		System.out.println("LC "+s);
		assertTrue("LC Accuracy Correct", s.startsWith("0.57"));

		// Test PS (0,0) -- should be identical
		PS ps = new PS();
		ps.setClassifier(new SMO());
		r = EvaluationTests.cvEvaluateClassifier(ps);
		System.out.println("PS "+r.info.get("Accuracy"));
		assertTrue("PS(0,0) Accuracy Identical to LC", s.equals(r.info.get("Accuracy")));

		// Test PS (3,1) -- should be faster 
		ps.setP(3);
		ps.setN(1);

		r = EvaluationTests.cvEvaluateClassifier(ps);
		System.out.println("PS(3,1) "+r.info.get("Accuracy"));
		assertTrue("PS(3,1) Accuracy Correct", r.info.get("Accuracy").startsWith("0.575 +/- 0.04") );

		// Test EPS
		EnsembleML eps = new EnsembleML();
		eps.setClassifier(ps);
		r = EvaluationTests.cvEvaluateClassifier(eps);
		System.out.println("EPS "+r.info.get("Accuracy"));
		assertTrue("EPS Accuracy Correct", r.info.get("Accuracy").equals("0.574 +/- 0.042") );
	}

}
