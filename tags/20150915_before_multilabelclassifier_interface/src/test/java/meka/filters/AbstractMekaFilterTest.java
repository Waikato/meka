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

/**
 * AbstractMekaFilterTest.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package meka.filters;

import weka.core.converters.ConverterUtils;
import weka.filters.AbstractFilterTest;

/**
 * Abstract test for filters within the MEKA framework.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 67 $
 */
public abstract class AbstractMekaFilterTest
  extends AbstractFilterTest {
  
  static {
    System.setProperty("weka.test.Regression.root", "src/test/resources");
  }

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public AbstractMekaFilterTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method. This implementation creates
   * the default filter to test and loads a test set of Instances.
   *
   * @throws Exception if an error occurs reading the example instances.
   */
  protected void setUp() throws Exception {
    m_Filter             = getFilter();
    m_Instances          = ConverterUtils.DataSource.read("FilterTest.arff");
    m_OptionTester       = getOptionTester();
    m_GOETester          = getGOETester();
    m_FilteredClassifier = getFilteredClassifier();
  }
}
