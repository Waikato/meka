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
 * AbstractLookAndFeel.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package meka.gui.laf;

/**
 * Ancestor for look and feel managers.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractLookAndFeel {

	/**
	 * Returns the name of the look and feel.
	 *
	 * @return      the name
	 */
	public abstract String getName();

	/**
	 * Installs the look and feel.
	 *
	 * @return              true if successfully installed
	 * @throws Exception    if installation fails
	 */
	protected abstract boolean doInstall() throws Exception;

	/**
	 * Installs the look and feel.
	 *
	 * @return              true if successfully installed
	 */
	public boolean install() {
		try {
			return doInstall();
		}
		catch (Exception e) {
			System.err.println(getClass().getName() + ": Failed to install look and feel!");
			e.printStackTrace();
			return false;
		}
	}
}
