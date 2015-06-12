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

package meka;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for all tests in this directory. Run from the command line 
 * with:<p>
 * java weka.MekaTests
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 117 $
 */
public class MekaTests extends TestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite();

    // classifiers
    suite.addTest(meka.classifiers.MekaTests.suite());

    // filters
    suite.addTest(meka.filters.MekaTests.suite());

    return suite;
  }

  public static void main(String []args) {
    junit.textui.TestRunner.run(suite());
  }
}
