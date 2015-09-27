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
 * DatasetProvider.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.datasetproviders;

import weka.core.Instances;
import weka.core.OptionHandler;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Interface for classes that provide datasets for the experiment.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface DatasetProvider
  extends OptionHandler, Iterator<Instances>, Serializable {

	/**
	 * Initializes the provider to start providing datasets from scratch.
	 *
	 * @return      null if successfully initialized, otherwise error message
	 */
	public String initialize();

	/**
	 * Returns whether another dataset is available.
	 *
	 * @return      true if another dataset is available
	 */
	@Override
	public boolean hasNext();

	/**
	 * Returns the next dataset.
	 *
	 * @return      the next dataset
	 */
	@Override
	public Instances next();

	/**
	 * Gets called after the experiment finishes.
	 *
	 * @return          null if successfully finished, otherwise error message
	 */
	public String finish();
}
