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

package meka.classifiers.multilabel;

import junit.framework.Test;
import junit.framework.TestSuite;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Tests MLCBMaD. Run from the command line with:<p/>
 * java meka.classifiers.multilabel.MLCBMaDTest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 117 $
 */
public class MLCBMaDTest 
  extends AbstractMultiLabelClassifierTest {

  /**
   * Initializes the test.
   * 
   * @param name the name of the test
   */
  public MLCBMaDTest(String name) { 
    super(name);  
  }

  /** 
   * Creates a default classifier.
   * 
   * @return the classifier
   */
  @Override
  public Classifier getClassifier() {
    MLCBMaD mlcbmad =  new MLCBMaD();
    mlcbmad.setSize(5);
    return mlcbmad;
  }

  public void testEvaluation() {
	  System.out.println("Testing Evaluation");
	  Instances D = null;
	  try {
		  D = loadData("Music.arff");
	  } catch(Exception e) {
		  System.err.println(" Failed to Load ");
	  }
	  //Assert.assertEquals("Result 1", D.classIndex(), 6);
	  //Assert.assertEquals("Result 1", D.classIndex(), 3);
  }

  public static Test suite() {
    return new TestSuite(MLCBMaDTest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }
}
