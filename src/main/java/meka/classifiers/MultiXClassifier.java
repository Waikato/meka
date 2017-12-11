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
 * MultiXClassifier.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.classifiers;

import weka.classifiers.Classifier;
import weka.core.OptionHandler;

/**
 * Interface for multi-label classifiers.
 *
 * @author Joerg Wicker
 * @version $Revision$
 */
public interface MultiXClassifier
		extends Classifier, OptionHandler {

	/**
	 * Set debugging mode.
	 *
	 * @param debug true if debug output should be printed
	 */
	public void setDebug(boolean debug);

	/**
	 * Get whether debugging is turned on.
	 *
	 * @return true if debugging output is on
	 */
	public boolean getDebug();

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String debugTipText();

	/**
	 * Returns a string representation of the model.
	 *
	 * @return      the model
	 */
	public String getModel();
}
