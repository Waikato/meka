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
public class MetaMethodsTests extends TestCase {

	public MetaMethodsTests(String s) {
		super(s);
		System.out.println("Evaluation Test");
	}

	public static Test suite() {
		return new TestSuite(MetaMethodsTests.class);
	}

	protected void setUp(){
		// Preparation of the unit tests
	}

	protected void tearDown(){
		// Teardown for data used by the unit tests
	}

	public void testEBR() {

		// Test BR
		BR br = new BR();
		br.setClassifier(new SMO());
		Result r = EvaluationTests.cvEvaluateClassifier(br);
		assertTrue("BR Accuracy Correct", r.output.get("Accuracy").equals("0.493 +/- 0.036") );

		// Test EBR
		EnsembleML ebr = new EnsembleML();
		ebr.setClassifier(br);
		Result Er = EvaluationTests.cvEvaluateClassifier(ebr);
		assertTrue("EBR Accuracy Correct", Er.output.get("Accuracy").equals("0.557 +/- 0.04 ") );
	}

}
