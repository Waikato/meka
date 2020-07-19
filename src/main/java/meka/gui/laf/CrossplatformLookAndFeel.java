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
 * CrossplatformLookAndFeel.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package meka.gui.laf;

import javax.swing.UIManager;

/**
 * Uses the system default.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class CrossplatformLookAndFeel
  extends AbstractLookAndFeel {

	/**
	 * Returns the name of the look and feel.
	 *
	 * @return      the name
	 */
	@Override
	public String getName() {
		return "Cross-platform";
	}

	/**
	 * Installs the look and feel.
	 *
	 * @throws Exception    if installation fails
	 * @return              true if successfully installed
	 */
	@Override
	protected boolean doInstall() throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		return true;
	}
}
