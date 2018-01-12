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
 * AbstractMenuItemDefinition.java
 * Copyright (C) 2015-2018 University of Waikato, Hamilton, NZ
 */

package meka.gui.guichooser;

import meka.gui.core.GUIHelper;
import weka.core.PluginManager;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

/**
 * Defines menu items for the GUIChooser's menu.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractMenuItemDefinition
	implements Serializable, Comparable<AbstractMenuItemDefinition> {

	private static final long serialVersionUID = -5691895005170983500L;

	/** "Program", the first menu. */
	public final static String MENU_PROGRAM = "Program";

	/** "Visualization" menu. */
	public final static String MENU_VISUALIZATION = "Visualization";

	/** "Tools" menu. */
	public final static String MENU_TOOLS = "Tools";

	/** "Help", the last menu. */
	public final static String MENU_HELP = "Help";

	/**
	 * The name of the menu this item belongs to.
	 *
	 * @return      the menu text
	 */
	public abstract String getGroup();

	/**
	 * The name of the menu item.
	 *
	 * @return      the menu item text
	 */
	public abstract String getName();

	/**
	 * The name of the menu icon.
	 *
	 * @return      the file name, null if none
	 */
	public String getIconName() {
		return "MEKA_icon.png";
	}

	/**
	 * Returns the icon to use.
	 *
	 * @return      the icon
	 */
	public ImageIcon getIcon() {
		ImageIcon       result;

		if (getIconName() == null)
			return GUIHelper.getEmptyIcon();

		result = GUIHelper.getIcon(getIconName());
		if (result == null)
			result = GUIHelper.getEmptyIcon();

		return result;
	}

	/**
	 * Called by the menu items action listener.
	 */
	protected abstract void launch();

	/**
	 * Returns the menu item to insert
	 *
	 * @return      the menu item
	 */
	public JMenuItem getMenuItem() {
		JMenuItem       result;

		result = new JMenuItem(getName(), getIcon());
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				launch();
			}
		});

		return result;
	}

	/**
	 * Returns whether this menu item should also be listed as a shortcut button.
	 *
	 * @return          true if to use as button as well
	 */
	public boolean isShortcutButton() {
		return false;
	}

	/**
	 * Returns the shortcut button.
	 *
	 * @return          the button
	 */
	public JButton getButton() {
		final JButton   result;

		result = new JButton(getName());
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				launch();
			}
		});

		return result;
	}

	/**
	 * Comparison on the group and name.
	 *
	 * @param o         the other plugin
	 * @return          less than 0, equal to 0, greater than 0 if the name is less, equal or greater
	 * @see             #getGroup()
	 * @see             #getName()
	 */
    public int compareTo(AbstractMenuItemDefinition o) {
	    int     result;

	    result = getGroup().compareTo(o.getGroup());
	    if (result == 0)
		    result = getName().compareTo(o.getName());

	    return result;
    }

	/**
	 * Checks equality using the name of the plugin.
	 *
	 * @param o         the other object
	 * @return          true if other object is a plugin with the same name
	 * @see             #getGroup()
	 * @see             #getName()
	 */
	public boolean equals(Object o) {
		return (o instanceof AbstractMenuItemDefinition) && (compareTo((AbstractMenuItemDefinition) o) == 0);
	}

	/**
	 * Returns all the available menu items.
	 *
	 * @return          the classnames of the menu items
	 */
	public static List<String> getDefinitions() {
		return PluginManager.getPluginNamesOfTypeList(AbstractMenuItemDefinition.class.getName());
	}
}
