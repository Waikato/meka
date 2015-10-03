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
 * SerializedExperiment.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.filehandlers;

import meka.experiment.Experiment;
import weka.core.SerializationHelper;

import java.io.File;

/**
 * Stores the experiment as a Java serialized object.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SerializedExperiment
		extends AbstractExperimentFileHandler {

	private static final long serialVersionUID = -5374752688504488703L;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Stores the experiment as a Java serialized object.";
	}

	/**
	 * A description of the file format.
	 *
	 * @return          the description
	 */
	@Override
	public String getFormatDescription() {
		return "Serialized experiment";
	}

	/**
	 * Returns the format extensions.
	 *
	 * @return          the extensions (incl dot)
	 */
	@Override
	public String[] getFormatExtensions() {
		return new String[]{".ser"};
	}

	/**
	 * Reads an experiment from disk.
	 *
	 * @param file      the file to load
	 * @return          the experiment, null if failed to load
	 */
	@Override
	public Experiment read(File file) {
		try {
			return (Experiment) SerializationHelper.read(file.getAbsolutePath());
		}
		catch (Exception e) {
			handleException("Failed to read experiment from: " + file, e);
			return null;
		}
	}

	/**
	 * Writes and experiment to disk.
	 *
	 * @param exp       the experiment to save
	 * @param file      the file to save to
	 * @return          null if successful, otherwise error message
	 */
	@Override
	public String write(Experiment exp, File file) {
		String          result;

		result  = null;
		try {
			SerializationHelper.write(file.getAbsolutePath(), exp);
		}
		catch (Exception e) {
			result = handleException("Failed to write experiment to: " + file, e);
		}

		return result;
	}
}
