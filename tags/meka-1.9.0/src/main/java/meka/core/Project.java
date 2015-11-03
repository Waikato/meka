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
 * Helper class related to the project and it's "home" directory.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Project {

	/** the windows directory. */
	public final static String DIR_WINDOWS = "mekafiles";

	/** the unix directory. */
	public final static String DIR_UNIX = ".meka";

	/**
	 * Returns the "home" directory of Meka, where to store the config files.
	 *
	 * @return			the directory
	 */
	public static File getHome() {
		String	dir;

		dir = System.getProperty("user.home") + File.separator;
		if (OS.isWindows())
			dir += DIR_WINDOWS;
		else
			dir += DIR_UNIX;

		return new File(dir);
	}

	/**
	 * Adds the home directory to the file.
	 *
	 * @param file		the file without path
	 * @return			the expanded path
	 */
	public static File expandFile(String file) {
		return new File(getHome().getAbsolutePath() + File.separator + file);
	}

	/**
	 * Makes sure that the project's home directory is present.
	 *
	 * @return          true if home directory present (or successfully created)
	 */
	public static boolean initialize() {
		if (!getHome().exists())
			return getHome().mkdirs();
		return getHome().isDirectory();
	}
}
