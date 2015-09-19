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
 * MenuBarProvider.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import javax.swing.*;

/**
 * Interface for panels that generate a menu bar.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface MenuBarProvider {

	/**
	 * Returns the menu bar to use.
	 *
	 * @return the menu bar
	 */
	public JMenuBar getMenuBar();
}
