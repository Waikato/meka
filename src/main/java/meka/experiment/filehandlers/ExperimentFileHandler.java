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
 * ExperimentFileHandler.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.filehandlers;

import meka.experiment.Experiment;
import weka.core.OptionHandler;

import java.io.File;
import java.io.Serializable;

/**
 * Interface for classes load/save experiments.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface ExperimentFileHandler
	extends OptionHandler, Serializable {

	/**
	 * A description of the file format.
	 *
	 * @return          the description
	 */
	public String getFormatDescription();

	/**
	 * Returns the format extensions.
	 *
	 * @return          the extensions (incl dot)
	 */
	public String[] getFormatExtensions();

	/**
	 * Reads an experiment from disk.
	 *
	 * @param file      the file to load
	 * @return          the experiment, null if failed to load
	 */
	public Experiment read(File file);

	/**
	 * Writes and experiment to disk.
	 *
	 * @param exp       the experiment to save
	 * @param file      the file to save to
	 * @return          null if successful, otherwise error message
	 */
	public String write(Experiment exp, File file);
}
