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

package meka.classifiers.multilabel;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import meka.classifiers.AbstractMekaClassifierTest;
import weka.classifiers.Classifier;
import meka.classifiers.multilabel.meta.*;
import weka.classifiers.functions.SMO;
import weka.core.converters.ConverterUtils.DataSource;
import meka.core.Result;
import meka.core.MLEvalUtils;

import meka.gui.explorer.Explorer;

/**
 * EvaluationTests. Run from the command line with:<p/>
 * java meka.classifiers.multilabel.EvaluationTests
 *
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version $Revision: 66 $
 */
public class LPMethodsTests extends TestCase {

	public LPMethodsTests(String s) {
		super(s);
	}

	public static Test suite() {
		return new TestSuite(LPMethodsTests.class);
	}

	protected void setUp(){
		// Preparation of the unit tests
	}

	protected void tearDown(){
		// Teardown for data used by the unit tests
	}

	public void testLC() {
		PS ps = new PS();
		assertEquals("Result 1", 50, 50);
		assertEquals("Result 2", 50, 50);
	}

}
