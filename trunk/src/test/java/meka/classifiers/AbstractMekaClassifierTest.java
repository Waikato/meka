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
 * AbstractMekaClassifierTest.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */
package meka.classifiers;

import junit.framework.TestCase;
import weka.classifiers.CheckClassifier;
import weka.classifiers.Classifier;
import weka.core.CheckGOE;
import weka.core.CheckOptionHandler;
import weka.core.Instances;
import weka.core.OptionHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Abstract test for classifiers within the MEKA framework.
 * <br>
 * The following system properties can be set:
 * <ul>
 *   <li>meka.test.debug [true|false] to set tester object debug flag</li>
 *   <li>meka.test.silent [true|false] to set tester object silent flag</li>
 * </ul>
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 117 $
 */
public abstract class AbstractMekaClassifierTest
		extends TestCase {

	/** whether to run testers in DEBUG mode */
	public boolean DEBUG = System.getProperty("meka.test.debug", "false").equals("true");

	/** whether to run testers in SILENT mode */
	public boolean SILENT = System.getProperty("meka.test.silent", "true").equals("true");

	/**
	 * Dummy class to expose protected methods.
	 */
	public static class MekaCheckClassifier
			extends CheckClassifier {

		/**
		 * Checks whether the scheme's toString() method works even though the
		 * classifies hasn't been built yet.
		 *
		 * @return index 0 is true if the toString() method works fine
		 */
		public boolean[] testToString() {
			return super.testToString();
		}

		/**
		 * tests for a serialVersionUID. Fails in case the scheme doesn't declare a
		 * UID.
		 *
		 * @return index 0 is true if the scheme declares a UID
		 */
		public boolean[] declaresSerialVersionUID() {
			return super.declaresSerialVersionUID();
		}
	}

	/** The classifier to be tested */
	protected Classifier m_Classifier;

	/** For testing the classifier */
	protected MekaCheckClassifier m_Tester;

	/** the OptionHandler tester */
	protected CheckOptionHandler m_OptionTester;

	/** for testing GOE stuff */
	protected CheckGOE m_GOETester;

	/**
	 * Initializes the test.
	 *
	 * @param name	the name of the test
	 */
	public AbstractMekaClassifierTest(String name) {
		super(name);
	}

	/**
	 * configures the CheckClassifier instance used throughout the tests
	 *
	 * @return the fully configured CheckClassifier instance used for testing
	 */
	protected MekaCheckClassifier getTester() {
		MekaCheckClassifier result;

		result = new MekaCheckClassifier();
		result.setSilent(SILENT);
		result.setClassifier(m_Classifier);
		result.setNumInstances(20);
		result.setDebug(DEBUG);

		return result;
	}

	/**
	 * Configures the CheckOptionHandler uses for testing the optionhandling. Sets
	 * the classifier return from the getClassifier() method.
	 *
	 * @return the fully configured CheckOptionHandler
	 * @see #getClassifier()
	 */
	protected CheckOptionHandler getOptionTester() {
		CheckOptionHandler result;

		result = new CheckOptionHandler();
		result.setOptionHandler((OptionHandler) getClassifier());
		result.setUserOptions(new String[0]);
		result.setSilent(SILENT);
		result.setDebug(DEBUG);

		return result;
	}

	/**
	 * Configures the CheckGOE used for testing GOE stuff. Sets the Classifier
	 * returned from the getClassifier() method.
	 *
	 * @return the fully configured CheckGOE
	 * @see #getClassifier()
	 */
	protected CheckGOE getGOETester() {
		CheckGOE result;

		result = new CheckGOE();
		result.setObject(getClassifier());
		result.setSilent(SILENT);
		result.setDebug(DEBUG);

		return result;
	}

	/**
	 * Called by JUnit before each test method. This implementation creates the
	 * default classifier to test and loads a test set of Instances.
	 *
	 * @exception Exception if an error occurs reading the example instances.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		m_Classifier = getClassifier();
		m_Tester = getTester();
		m_OptionTester = getOptionTester();
		m_GOETester = getGOETester();
	}

	/** Called by JUnit after each test method */
	@SuppressWarnings("unchecked")
	@Override
	protected void tearDown() {
		m_Classifier = null;
		m_Tester = null;
		m_OptionTester = null;
		m_GOETester = null;
	}

	/**
	 * Used to create an instance of a specific classifier.
	 *
	 * @return a suitably configured <code>Classifier</code> value
	 */
	public abstract Classifier getClassifier();

	/**
	 * tests whether the toString method of the classifier works even though the
	 * classifier hasn't been built yet.
	 */
	public void testToString() {
		boolean[] result;

		result = m_Tester.testToString();

		if (!result[0]) {
			fail("Error in toString() method!");
		}
	}

	/**
	 * tests whether the scheme declares a serialVersionUID.
	 */
	public void testSerialVersionUID() {
		boolean[] result;

		result = m_Tester.declaresSerialVersionUID();

		if (!result[0]) {
			fail("Doesn't declare serialVersionUID!");
		}
	}

	/**
	 * tests the listing of the options
	 */
	public void testListOptions() {
		if (!m_OptionTester.checkListOptions()) {
			fail("Options cannot be listed via listOptions.");
		}
	}

	/**
	 * tests the setting of the options
	 */
	public void testSetOptions() {
		if (!m_OptionTester.checkSetOptions()) {
			fail("setOptions method failed.");
		}
	}

	/**
	 * tests whether the default settings are processed correctly
	 */
	public void testDefaultOptions() {
		if (!m_OptionTester.checkDefaultOptions()) {
			fail("Default options were not processed correctly.");
		}
	}

	/**
	 * tests whether there are any remaining options
	 */
	public void testRemainingOptions() {
		if (!m_OptionTester.checkRemainingOptions()) {
			fail("There were 'left-over' options.");
		}
	}

	/**
	 * tests the whether the user-supplied options stay the same after setting.
	 * getting, and re-setting again.
	 *
	 * @see #getOptionTester()
	 */
	public void testCanonicalUserOptions() {
		if (!m_OptionTester.checkCanonicalUserOptions()) {
			fail("setOptions method failed");
		}
	}

	/**
	 * tests the resetting of the options to the default ones
	 */
	public void testResettingOptions() {
		if (!m_OptionTester.checkSetOptions()) {
			fail("Resetting of options failed");
		}
	}

	/**
	 * tests for a globalInfo method
	 */
	public void testGlobalInfo() {
		if (!m_GOETester.checkGlobalInfo()) {
			fail("No globalInfo method");
		}
	}

	/**
	 * tests the tool tips
	 */
	public void testToolTips() {
		if (!m_GOETester.checkToolTips()) {
			fail("Tool tips inconsistent");
		}
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
}
