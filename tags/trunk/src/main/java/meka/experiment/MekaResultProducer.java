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
 * MekaResultProducer.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package meka.experiment;

import weka.experiment.ResultProducer;

/**
 * Interface for MEKA {@link ResultProducer} classes that need to know
 * about the overall number of classes.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface MekaResultProducer
	extends ResultProducer {

	/**
	 * Sets the overal number of classes.
	 * 
	 * @param value			the number of classes
	 */
	public void setTotalNumClasses(int value);

	/**
	 * Returns the overal number of classes.
	 * 
	 * @return			the number of classes
	 */
	public int getTotalNumClasses();
}
