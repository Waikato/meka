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
 * AbstractExperimenterMenuItem.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.experimenter.menu;

import meka.gui.core.GUIHelper;
import meka.gui.experimenter.Experimenter;
import weka.core.PluginManager;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.List;

/**
 * Ancestor for menu items to be displayed in the Experimenter menu.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractExperimenterMenuItem
	implements Serializable {

	private static final long serialVersionUID = -355350206100168793L;

	/** the experimenter the menu item belongs to. */
	protected Experimenter m_Owner;

	/**
	 * Sets the owner.
	 *
	 * @param value     the owner
	 */
	public void setOwner(Experimenter value) {
		m_Owner = value;
	}

	/**
	 * Returns the owner.
	 *
	 * @return          the owner
	 */
	public Experimenter getOwner() {
		return m_Owner;
	}

	/**
	 * Returns the name of the menu this menu item will get added to.
	 *
	 * @return      the menu name
	 */
	public abstract String getMenu();

	/**
	 * Returns the name of the menu item.
	 *
	 * @return      the menu item name
	 */
	public abstract String getItem();

	/**
	 * Returns the name of the icon to use.
	 * <br>
	 * Default implementation returns null.
	 *
	 * @return      the file name (no path)
	 */
	protected String getIconName() {
		return null;
	}

	/**
	 * Returns the icon for the menu item.
	 *
	 * @return      the icon
	 */
	public ImageIcon getIcon() {
		if (getIconName() == null)
			return GUIHelper.getEmptyIcon();
		else
			return GUIHelper.getIcon(getIconName());
	}

	/**
	 * Launches the menu item action.
	 */
	protected abstract void launch();

	/**
	 * Returns the action.
	 *
	 * @return      the action
	 */
	public AbstractAction getAction() {
		AbstractAction  result;

		result = new AbstractAction() {
			private static final long serialVersionUID = 5574495652910000349L;
			@Override
			public void actionPerformed(ActionEvent e) {
				launch();
			}
		};
		result.putValue(AbstractAction.SMALL_ICON, getIcon());
		result.putValue(AbstractAction.LARGE_ICON_KEY, getIcon());
		result.putValue(AbstractAction.NAME, getItem());

		return result;
	}

	/**
	 * Updates the action using the experimenter.
	 * <br>
	 * Default implementation does nothing.
	 *
	 * @param exp       the experimenter
	 * @param action    the action to update
	 */
	public void update(Experimenter exp, AbstractAction action) {
	}

	/**
	 * Returns the classnames of all additional experiment menu items.
	 *
	 * @return          the classnames
	 */
	public static List<String> getMenuItems() {
		return PluginManager.getPluginNamesOfTypeList(AbstractExperimenterMenuItem.class.getName());
	}
}
