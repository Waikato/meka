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

package meka.classifiers.multilabel.meta;

import junit.framework.Test;
import junit.framework.TestSuite;
import weka.test.WekaTestSuite;

/**
 * Test class for all classifiers. Run from the command line with: <p/>
 * java meka.classifiers.multilabel.meta.MekaTests
 *
 * @author FracPete (frapcete at waikato dot ac dot nz)
 * @version $Revision: 66 $
 */
public class MekaTests 
  extends WekaTestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite();

    // Template:
    //suite.addTest(meka.classifiers.multilabel.meta.BlahTest.suite());
    suite.addTest(meka.classifiers.multilabel.meta.BaggingMLTest.suite());
    suite.addTest(meka.classifiers.multilabel.meta.BaggingMLdupTest.suite());
    suite.addTest(meka.classifiers.multilabel.meta.EnsembleMLTest.suite());
    suite.addTest(meka.classifiers.multilabel.meta.MBRTest.suite());
    suite.addTest(meka.classifiers.multilabel.meta.RandomSubspaceMLTest.suite());
    suite.addTest(meka.classifiers.multilabel.meta.SubsetMapperTest.suite());

    return suite;
  }

  public static void main(String []args) {
    junit.textui.TestRunner.run(suite());
  }
}
