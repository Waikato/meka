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
 * AbstractAdamsFilterTest.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package weka.classifiers;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import weka.core.Instances;

/**
 * Abstract test for classifiers within the MEKA framework.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractMekaClassifierTest
  extends AbstractClassifierTest {
  
  static {
    System.setProperty("weka.test.Regression.root", "src/test/resources");
  }

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public AbstractMekaClassifierTest(String name) {
    super(name);
  }
  
  /**
   * Loads the dataset from disk.
   * 
   * @param file the dataset to load (e.g., "weka/classifiers/data/something.arff")
   * @throws Exception if loading fails, e.g., file does not exit
   */
  public static Instances loadData(String file) throws Exception {
    return new Instances(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(file))));
  }
  
  /**
   * Ignored.
   */
  @Override
  public void testAttributes() {
  }

  /**
   * Ignored.
   */
  @Override
  public void testClassAsNthAttribute() {
  }

  /**
   * Ignored.
   */
  @Override
  public void testOnlyClass() {
  }

  /**
   * Ignored.
   */
  @Override
  public void testNClasses() {
  }
}
