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
 * Copyright (C) 2012 University of Waikato 
 */

package meka.classifiers.multilabel;

import junit.framework.Test;
import junit.framework.TestSuite;
import weka.test.WekaTestSuite;

/**
 * Test class for all classifiers. Run from the command line with: <p/>
 * java meka.classifiers.multilabel.MekaTests
 *
 * @author FracPete (frapcete at waikato dot ac dot nz)
 * @version $Revision: 117 $
 */
public class MekaTests extends WekaTestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite();

	/*
	 * Tests are split into 4 families of related methods:
	 *	- Ensembles, Meta methods
	 *  - BR and CC methods
	 *  - LP (aka LC, PS) methods
	 *  - Misc. (other) methods
	 */
	suite.addTest(EvaluationTests.suite());
	suite.addTest(MetaMethodsTests.suite());
	suite.addTest(CCMethodsTests.suite());
	suite.addTest(LPMethodsTests.suite());
	suite.addTest(MiscMethodsTests.suite());

	/*
	 * Hence, we no longer test methods individually, like this
	 *
    suite.addTest(meka.classifiers.multilabel.BRTest.suite());
    suite.addTest(meka.classifiers.multilabel.BRqTest.suite());
    suite.addTest(meka.classifiers.multilabel.BRUpdateableTest.suite());
    suite.addTest(meka.classifiers.multilabel.CCTest.suite());
    suite.addTest(meka.classifiers.multilabel.CCqTest.suite());
    suite.addTest(meka.classifiers.multilabel.CCUpdateableTest.suite());
    suite.addTest(meka.classifiers.multilabel.CDNTest.suite());
    suite.addTest(meka.classifiers.multilabel.LCTest.suite());
    suite.addTest(meka.classifiers.multilabel.MajorityLabelsetTest.suite());
    suite.addTest(meka.classifiers.multilabel.MajorityLabelsetUpdateableTest.suite());
    suite.addTest(meka.classifiers.multilabel.MULANTest.suite());
    suite.addTest(meka.classifiers.multilabel.PSTest.suite());
    suite.addTest(meka.classifiers.multilabel.PStTest.suite());
    suite.addTest(meka.classifiers.multilabel.PSUpdateableTest.suite());
    suite.addTest(meka.classifiers.multilabel.RTTest.suite());
    suite.addTest(meka.classifiers.multilabel.RTUpdateableTest.suite());
	*/

    return suite;
  }

  public static void main(String []args) {
    junit.textui.TestRunner.run(suite());
  }
}
