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
import meka.classifiers.AbstractMekaClassifierTest;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Tests Maniac. Run from the command line with:<p/>
 * java meka.classifiers.multilabel.ManiacTest
 *
 * @author Joerg Wicekr (me@joerg-wicker.org)
 */
public class ManiacTest 
    extends AbstractMekaClassifierTest {

    /**
     * Initializes the test.
     * 
     * @param name the name of the test
     */
    public ManiacTest(String name) { 
	super(name);  
    }

    /** 
     * Creates a default classifier.
     * 
     * @return the classifier
     */
    @Override
    public Classifier getClassifier() {
	Maniac maniac =  new Maniac();
	maniac.setCompression(0.85);
	maniac.setNumberAutoencoders(1);
	return maniac;
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
	return new TestSuite(ManiacTest.class);
    }

    public static void main(String[] args){
	junit.textui.TestRunner.run(suite());
    }
}
