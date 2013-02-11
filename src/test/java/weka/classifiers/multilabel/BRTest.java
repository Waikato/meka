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

package weka.classifiers.multilabel;

import junit.framework.Test;
import junit.framework.TestSuite;
import weka.classifiers.AbstractMekaClassifierTest;
import weka.classifiers.Classifier;
import weka.core.*;
import junit.framework.*;
import junit.framework.Assert.*;

/**
 * Tests BR. Run from the command line with:<p/>
 * java weka.classifiers.multilabel.BRTest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class BRTest 
  extends AbstractMekaClassifierTest {

  /**
   * Initializes the test.
   * 
   * @param name the name of the test
   */
  public BRTest(String name) { 
    super(name);  
  }

  protected void setUp(){
	  // Preparation of the unit tests
	  //super.setUp(); For Generic WEKA Tests
  }

  protected void tearDown(){
	  // Teardown for data used by the unit tests
	  //super.tearDown(); // For Generic WEKA Tests
  }

  /** 
   * Creates a default classifier.
   * 
   * @return the classifier
   */
  @Override
  public Classifier getClassifier() {
    return new BR();
  }

  public void testEvaluation() {
	  System.out.println("Testing Evaluation");
	  Instances D = null;
	  try {
		  D = loadData("data/Music.arff");
	  } catch(Exception e) {
		  System.err.println(" Failed to Load ");
	  }
	  //Assert.assertEquals("Result 1", D.classIndex(), 6);
	  //Assert.assertEquals("Result 1", D.classIndex(), 3);
  }

  public static Test suite() {
    return new TestSuite(BRTest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }
}
