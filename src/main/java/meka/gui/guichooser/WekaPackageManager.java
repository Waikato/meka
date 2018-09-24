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
 * WekaPackageManager.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package meka.gui.guichooser;

import meka.gui.core.GUILauncher;
import weka.gui.PackageManager;

/**
 * Launches the Weka package manager.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class WekaPackageManager
  extends AbstractMenuItemDefinition {

	private static final long serialVersionUID = -3184143315707991090L;

	/**
	 * The name of the menu this item belongs to.
	 *
	 * @return      the menu text
	 */
	@Override
	public String getGroup() {
		return MENU_PROGRAM;
	}

	/**
	 * The name of the menu item.
	 *
	 * @return      the menu item text
	 */
	@Override
	public String getName() {
		return "Weka Package manager";
	}

	/**
	 * The name of the menu icon.
	 *
	 * @return      the file name, null if none
	 */
	@Override
	public String getIconName() {
		return "weka_icon_new_small.png";
	}

	/**
	 * Called by the menu items action listener.
	 */
	@Override
	protected void launch() {
		GUILauncher.launchFrame(new PackageManager(), getName(), true);
	}
}
