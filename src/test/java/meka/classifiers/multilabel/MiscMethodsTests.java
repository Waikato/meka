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
import meka.classifiers.multilabel.meta.*;
import weka.classifiers.functions.*;
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
public class MiscMethodsTests extends TestCase {

	public MiscMethodsTests(String s) {
		super(s);
	}

	public static Test suite() {
		return new TestSuite(MiscMethodsTests.class);
	}

	protected void setUp(){
		// Preparation of the unit tests
	}

	protected void tearDown(){
		// Teardown for data used by the unit tests
	}

	public void testRT() {

		// Test RT
		System.out.println("Test RT");
		RT rt = new RT();
		rt.setClassifier(new Logistic());
		Result r = EvaluationTests.cvEvaluateClassifier(rt);
		assertTrue("RT Accuracy Correct", r.output.get("Accuracy").equals("0.5   +/- 0.04 ") );
	}

	public void testMULAN() {
		// Test MULAN
		System.out.println("Test MULAN");
		MULAN mulan = new MULAN();
		// ... RAkEL
		mulan.setMethod("RAkEL2");
		mulan.setClassifier(new SMO());
		Result r;
		r = EvaluationTests.cvEvaluateClassifier(mulan);
		System.out.println("MULAN (RAkEL): "+r.output.get("Accuracy"));
		assertTrue("MULAN (RAkEL) Accuracy Correct", ((String)r.output.get("Accuracy")).startsWith("0.58") );
		// ... MLkNN
		mulan.setMethod("MLkNN");
		r = EvaluationTests.cvEvaluateClassifier(mulan);
		System.out.println("MULAN (MLkNN): "+r.output.get("Accuracy"));
		assertTrue("MULAN (MLkNN) Accuracy Correct", r.output.get("Accuracy").equals("0.561 +/- 0.035") );
		// ... BR (and , vs MEKA's BR)
		mulan.setMethod("BR");
		r = EvaluationTests.cvEvaluateClassifier(mulan);
		System.out.println("MULAN (BR): "+r.output.get("Accuracy"));
		assertTrue("MULAN (BR) Accuracy Correct", r.output.get("Accuracy").equals("0.493 +/- 0.036") );
		BR br = new BR();
		br.setClassifier(new SMO());
		Result r_other = EvaluationTests.cvEvaluateClassifier(br);
		assertTrue("MULAN BR Equal to MEKA BR", r.output.get("Accuracy").equals(r_other.output.get("Accuracy")) );
	}

}
