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
 * FileBasedEvaluationStatisticsHandler.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import meka.core.FileFormatSupporter;

import java.io.File;

/**
 * Interface for file-based statistics handlers.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface FileBasedEvaluationStatisticsHandler
  extends EvaluationStatisticsHandler, FileFormatSupporter {

	/**
	 * Returns the format description.
	 *
	 * @return      the file format
	 */
	public String getFormatDescription();

	/**
	 * Returns the format extension(s).
	 *
	 * @return      the extension(s) (incl dot)
	 */
	public String[] getFormatExtensions();

	/**
	 * Sets the file to read from/write to.
	 *
	 * @param value     the file
	 */
	public void setFile(File value);

	/**
	 * Returns the file to read from/write to.
	 *
	 * @return          the file
	 */
	public File getFile();

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String fileTipText();
}
