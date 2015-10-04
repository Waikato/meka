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
 * ExperimenterDefinition.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.guichooser;

import meka.gui.core.GUILauncher;
import meka.gui.experimenter.Experimenter;

/**
 * Launches the Experimenter.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExperimenterDefinition
  extends AbstractMenuItemDefinition {

	private static final long serialVersionUID = -3184143315707991090L;

	/**
	 * The name of the menu this item belongs to.
	 *
	 * @return      the menu text
	 */
	@Override
	public String getGroup() {
		return MENU_TOOLS;
	}

	/**
	 * The name of the menu item.
	 *
	 * @return      the menu item text
	 */
	@Override
	public String getName() {
		return "Experimenter";
	}

	/**
	 * The name of the menu icon.
	 *
	 * @return      the file name, null if none
	 */
	@Override
	public String getIconName() {
		return "experimenter.gif";
	}

	/**
	 * Returns whether this menu item should also be listed as a shortcut button.
	 *
	 * @return          true if to use as button as well
	 */
	public boolean isShortcutButton() {
		return true;
	}

	/**
	 * Called by the menu items action listener.
	 */
	@Override
	protected void launch() {
		GUILauncher.launchFrame(new Experimenter(), "MEKA Experimenter", true);
	}
}
