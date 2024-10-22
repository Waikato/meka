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
 * AbstractExperimentFileHandler.java
 * Copyright (C) 2015-2024 University of Waikato, Hamilton, NZ
 */

package meka.experiment.filehandlers;

import meka.events.LogObject;
import meka.experiment.Experiment;
import weka.core.Option;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Ancestor for experiment file handler classes.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractExperimentFileHandler
		extends LogObject
		implements ExperimentFileHandler {

	private static final long serialVersionUID = -5374752688504488703L;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public abstract String globalInfo();

	/**
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		return new Vector().elements();
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options
	 * @throws Exception    never
	 */
	@Override
	public void setOptions(String[] options) throws Exception {

	}

	/**
	 * Returns the options.
	 *
	 * @return              the options
	 */
	@Override
	public String[] getOptions() {
		return new String[0];
	}

	/**
	 * Reads an experiment from disk.
	 *
	 * @param file      the file to load
	 * @return          the experiment, null if failed to load
	 */
	@Override
	public abstract Experiment read(File file);

	/**
	 * Writes an experiment to disk.
	 *
	 * @param exp       the experiment to save
	 * @param file      the file to save to
	 * @return          null if successful, otherwise error message
	 */
	@Override
	public abstract String write(Experiment exp, File file);
}
