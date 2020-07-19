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
 * LookAndFeel.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package meka.gui.guichooser;

import meka.gui.core.GUIHelper;
import meka.gui.laf.AbstractLookAndFeel;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.event.ActionEvent;

/**
 * Sub-menu for installing the look and feel.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class LookAndFeel
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
		return "Look and feel";
	}

	/**
	 * The name of the menu icon.
	 *
	 * @return      the file name, null if none
	 */
	@Override
	public String getIconName() {
		return "lookandfeel.gif";
	}

	/**
	 * Not used.
	 */
	@Override
	protected void launch() {
	}

	/**
	 * Installs the look and feel and prompts the user to restart MEKA.
	 *
	 * @param menuitem  the context
	 * @param laf       the look and feel to install
	 */
	protected void install(JMenuItem menuitem, AbstractLookAndFeel laf) {
		meka.gui.laf.LookAndFeel.install(laf);
		JOptionPane.showConfirmDialog(
			null,
			"Look and feel installed, please exit MEKA now.",
			UIManager.getString("OptionPane.titleText"),
			JOptionPane.OK_CANCEL_OPTION);
	}

	/**
	 * Returns the menu item to insert
	 *
	 * @return      the menu item
	 */
	public JMenuItem getMenuItem() {
		JMenu       result;

		result = new JMenu(getName());
		result.setIcon(GUIHelper.getIcon(getIconName()));
		for (AbstractLookAndFeel l : meka.gui.laf.LookAndFeel.getAvailable()) {
			final AbstractLookAndFeel laf = l;
			final JMenuItem menuitem = new JMenuItem(l.getName());
			menuitem.addActionListener((ActionEvent e) -> install(menuitem, laf));
			result.add(menuitem);
		}

		return result;
	}
}
