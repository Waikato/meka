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
 * FlatLookAndFeel.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package meka.gui.laf;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;

/**
 * Uses FlatLaf.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class FlatLookAndFeel
  extends AbstractLookAndFeel {

	/**
	 * Returns the name of the look and feel.
	 *
	 * @return      the name
	 */
	@Override
	public String getName() {
		return "Flat";
	}

	/**
	 * Installs the look and feel.
	 *
	 * @return              true if successfully installed
	 * @throws Exception    if installation fails
	 */
	@Override
	protected boolean doInstall() throws Exception {
		FlatLightLaf.setup();
		UIManager.put("Table.showHorizontalLines", true);
		UIManager.put("Table.showVerticalLines", true);
		return true;
	}
}
