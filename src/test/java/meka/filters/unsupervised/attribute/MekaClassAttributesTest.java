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

package meka.filters.unsupervised.attribute;

import junit.framework.Test;
import junit.framework.TestSuite;
import meka.filters.AbstractMekaFilterTest;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * Tests MekaClassAttributes. Run from the command line with: <p/>
 * java meka.filters.unsupervised.attribute.MekaClassAttributesTest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 66 $
 */
public class MekaClassAttributesTest
	extends AbstractMekaFilterTest {

	/**
	 * Initializes the test.
	 *
	 * @param name the name of the test to run
	 */
	public MekaClassAttributesTest(String name) {
		super(name);
	}

	/**
	 * Creates a default MekaClassAttributes filter.
	 *
	 * @return the filter instance
	 */
	@Override
	public Filter getFilter() {
		return new MekaClassAttributes();
	}

	/**
	 * Creates a specialized MekaClassAttributes.
	 *
	 * @param range the range of attributes to use
	 */
	public Filter getFilter(String range) {
		MekaClassAttributes af = new MekaClassAttributes();
		try {
			af.setAttributeIndices(range);
		}
		catch (Exception e) {
			fail("Failed to set set range '" + range + "': " + e);
		}
		return af;
	}

	/**
	 * performs the actual test.
	 */
	protected void performTest() {
		Instances icopy = new Instances(m_Instances);
		Instances result = null;
		try {
			m_Filter.setInputFormat(icopy);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception thrown on setInputFormat(): \n" + ex.getMessage());
		}
		try {
			result = Filter.useFilter(icopy, m_Filter);
			assertNotNull(result);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception thrown on useFilter(): \n" + ex.getMessage());
		}

		assertEquals((icopy.numAttributes()), result.numAttributes());
		assertEquals(icopy.numInstances(), m_Instances.numInstances());
	}

	/**
	 * Tests using the first two attributes.
	 */
	public void testFirst() {
		m_Filter = getFilter("1-2");
		testBuffered();
		performTest();
	}


	/**
	 * Tests using the last two attributes.
	 */
	public void testLast() {
		m_Filter = getFilter((m_Instances.numAttributes() - 1) + "-" + m_Instances.numAttributes());
		testBuffered();
		performTest();
	}

	public static Test suite() {
		return new TestSuite(MekaClassAttributesTest.class);
	}

	public static void main(String[] args){
		junit.textui.TestRunner.run(suite());
	}
}
