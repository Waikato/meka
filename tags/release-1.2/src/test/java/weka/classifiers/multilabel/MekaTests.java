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

package weka.classifiers.multilabel;

import junit.framework.Test;
import junit.framework.TestSuite;
import weka.test.WekaTestSuite;

/**
 * Test class for all classifiers. Run from the command line with: <p/>
 * java weka.classifiers.multilabel.MekaTests
 *
 * @author FracPete (frapcete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MekaTests 
  extends WekaTestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite();

    // Template
    //suite.addTest(weka.classifiers.multilabel.BlahTest.suite());
    suite.addTest(weka.classifiers.multilabel.BRTest.suite());
    suite.addTest(weka.classifiers.multilabel.BRqTest.suite());
    suite.addTest(weka.classifiers.multilabel.BRUpdateableTest.suite());
    suite.addTest(weka.classifiers.multilabel.CCTest.suite());
    suite.addTest(weka.classifiers.multilabel.CCqTest.suite());
    suite.addTest(weka.classifiers.multilabel.CCUpdateableTest.suite());
    suite.addTest(weka.classifiers.multilabel.CDNTest.suite());
    suite.addTest(weka.classifiers.multilabel.LCTest.suite());
    suite.addTest(weka.classifiers.multilabel.MajorityLabelsetTest.suite());
    suite.addTest(weka.classifiers.multilabel.MajorityLabelsetUpdateableTest.suite());
    suite.addTest(weka.classifiers.multilabel.MULANTest.suite());
    suite.addTest(weka.classifiers.multilabel.PSTest.suite());
    suite.addTest(weka.classifiers.multilabel.PStTest.suite());
    suite.addTest(weka.classifiers.multilabel.PSUpdateableTest.suite());
    suite.addTest(weka.classifiers.multilabel.RTTest.suite());
    suite.addTest(weka.classifiers.multilabel.RTUpdateableTest.suite());

    return suite;
  }

  public static void main(String []args) {
    junit.textui.TestRunner.run(suite());
  }
}
