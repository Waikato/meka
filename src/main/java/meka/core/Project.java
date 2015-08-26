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
 * Project.java
 * Copyright (C) 2015 FracPete (fracpete at gmail dot com)
 *
 */

package meka.core;

import java.io.File;

/**
 * .
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Project {

	/**
	 * Returns the "home" directory of Meka, where to store the config files.
	 *
	 * @return			the directory
	 */
	public static File getHome() {
		return new File(System.getProperty("user.home") + File.separator + ".meka");
	}

	/**
	 * Adds the home directory to the file.
	 *
	 * @param file		the file without path
	 * @return			the expanded path
	 */
	public static File addHome(String file) {
		return new File(getHome().getAbsolutePath() + File.separator + file);
	}
}
