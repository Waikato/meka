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
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers.multitarget.meta;

import junit.framework.Test;
import junit.framework.TestSuite;
import weka.classifiers.AbstractMekaClassifierTest;
import weka.classifiers.Classifier;

/**
 * Tests BaggingMT. Run from the command line with:<p/>
 * java weka.classifiers.multitarget.meta.BaggingMTTest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class BaggingMTTest 
  extends AbstractMekaClassifierTest {

  /**
   * Initializes the test.
   * 
   * @param name the name of the test
   */
  public BaggingMTTest(String name) { 
    super(name);  
  }

  /** 
   * Creates a default BaggingMT.
   * 
   * @return the classifier
   */
  @Override
  public Classifier getClassifier() {
    return new BaggingMT();
  }

  /**
   * FIXME no useful predictions returned. custom dataset?
   */
  @Override
  public void testRegression() throws Exception {
  }

  public static Test suite() {
    return new TestSuite(BaggingMTTest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }
}
