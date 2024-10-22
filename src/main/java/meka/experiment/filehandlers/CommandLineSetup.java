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
 * CommandLineSetup.java
 * Copyright (C) 2015-2024 University of Waikato, Hamilton, NZ
 */

package meka.experiment.filehandlers;

import meka.core.FileUtils;
import meka.core.OptionUtils;
import meka.experiment.Experiment;
import weka.core.Utils;

import java.io.*;

/**
 * Stores the setup of the experiment as a commandline.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class CommandLineSetup
		extends AbstractExperimentFileHandler {

	private static final long serialVersionUID = -5374752688504488703L;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Stores the setup of the experiment as a commandline.";
	}

	/**
	 * A description of the file format.
	 *
	 * @return          the description
	 */
	@Override
	public String getFormatDescription() {
		return "Experiment setup";
	}

	/**
	 * Returns the format extensions.
	 *
	 * @return          the extensions (incl dot)
	 */
	@Override
	public String[] getFormatExtensions() {
		return new String[]{".setup"};
	}

	/**
	 * Reads an experiment from disk.
	 *
	 * @param file      the file to load
	 * @return          the experiment, null if failed to load
	 */
	@Override
	public Experiment read(File file) {
		Experiment      result;
		FileReader      freader;
		BufferedReader  breader;
		String          line;

		result  = null;
		freader = null;
		breader = null;
		try {
			freader = new FileReader(file);
			breader = new BufferedReader(freader);
			line    = breader.readLine();
			result  = OptionUtils.fromCommandLine(Experiment.class, line);
		}
		catch (Exception e) {
			result = null;
			handleException("Failed to read experiment from: " + file, e);
		}
		finally {
			FileUtils.closeQuietly(breader);
			FileUtils.closeQuietly(freader);
		}

		return result;
	}

	/**
	 * Writes an experiment to disk.
	 *
	 * @param exp       the experiment to save
	 * @param file      the file to save to
	 * @return          null if successful, otherwise error message
	 */
	@Override
	public String write(Experiment exp, File file) {
		String          result;
		FileWriter      fwriter;
		BufferedWriter  bwriter;

		result  = null;
		fwriter = null;
		bwriter = null;
		try {
			fwriter = new FileWriter(file);
			bwriter = new BufferedWriter(fwriter);
			bwriter.write(Utils.toCommandLine(exp));
			bwriter.newLine();
			bwriter.flush();
		}
		catch (Exception e) {
			result = handleException("Failed to write experiment to: " + file, e);
		}
		finally {
			FileUtils.closeQuietly(bwriter);
			FileUtils.closeQuietly(fwriter);
		}

		return result;
	}
}
