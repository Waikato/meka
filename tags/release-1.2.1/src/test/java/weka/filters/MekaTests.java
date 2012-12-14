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

package weka.filters;

import junit.framework.Test;
import junit.framework.TestSuite;
import weka.test.WekaTestSuite;

/**
 * Test class for all filters. Run from the command line with:<p/>
 * java weka.filters.MekaTests
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MekaTests 
  extends WekaTestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    
    suite.addTest(weka.filters.unsupervised.attribute.MekaTests.suite());
    //suite.addTest(weka.filters.unsupervised.instance.MekaTests.suite());
    //suite.addTest(weka.filters.supervised.attribute.MekaTests.suite());
    //suite.addTest(weka.filters.supervised.instance.MekaTests.suite());
    
    return suite;
  }

  public static void main(String []args) {
    junit.textui.TestRunner.run(suite());
  }
}
