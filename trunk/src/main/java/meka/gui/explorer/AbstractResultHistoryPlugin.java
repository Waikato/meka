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
 * AbstractResultHistoryPlugin.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.explorer;

import meka.gui.core.ResultHistoryList;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Ancestor for plugins that work .
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractResultHistoryPlugin
		implements ResultHistoryList.ResultHistoryPopupMenuCustomizer, Serializable, Comparable<AbstractResultHistoryPlugin> {

	private static final long serialVersionUID = 7408923951811192736L;

	/** the tab this plugin belongs to (for accessing the session). */
	protected AbstractExplorerTab m_Owner;

	public void setOwner(AbstractExplorerTab value) {
		m_Owner = value;
	}

	/**
	 * Returns the tab the plugin belongs to.
	 *
	 * @return          the owner
	 */
	public AbstractExplorerTab getOwner() {
		return m_Owner;
	}

	/**
	 * Checks whether a session value is present.
	 *
	 * @param key       the key to look for
	 * @return          true if present
	 */
	public boolean hasSessionValue(String key) {
		if (getOwner().getSession().containsKey(getClass())) {
			return getOwner().getSession().get(getClass()).containsKey(key);
		}
		return false;
	}

	/**
	 * Returns a session value, if present.
	 *
	 * @param key       the key to look for
	 * @return          the value, null if not present
	 */
	public Object getSessionValue(String key) {
		if (getOwner().getSession().containsKey(getClass())) {
			return getOwner().getSession().get(getClass()).get(key);
		}
		return null;
	}

	/**
	 * Sets a session value.
	 *
	 * @param key       the key to look for
	 * @param value     the value to set
	 */
	public void setSessionValue(String key, Object value) {
		if (!getOwner().getSession().containsKey(getClass()))
			getOwner().getSession().put(getClass(), new HashMap<String, Object>());
		getOwner().getSession().get(getClass()).put(key, value);
	}

	/**
	 * Comparison on the group and name.
	 *
	 * @param o         the other plugin
	 * @return          less than 0, equal to 0, greater than 0 if the name is less, equal or greater
	 * @see             #getGroup()
	 * @see             #getName()
	 */
    public int compareTo(AbstractResultHistoryPlugin o) {
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
		return (o instanceof AbstractResultHistoryPlugin) && (compareTo((AbstractResultHistoryPlugin) o) == 0);
	}

	/**
	 * Returns the group of the plugin. Used for the grouping the menu items.
	 *
	 * @return          the group
	 */
	public abstract String getGroup();

	/**
	 * Returns the name of the plugin. Used for the menu item text.
	 *
	 * @return          the name
	 */
	public abstract String getName();

	/**
	 * Checks whether the current item can be handled. Disables/enables the menu item.
	 *
	 * @param history   the current history
	 * @param index     the selected history item
	 * @return          true if can be handled
	 */
	public abstract boolean handles(ResultHistoryList history, int index);

	/**
	 * Returns the action lister to use in the menu.
	 *
	 * @param history   the current history
	 * @param index     the selected history item
	 * @return          the listener
	 */
	public abstract ActionListener getActionListener(ResultHistoryList history, int index);

	/**
	 * Allows to customize the popup menu for the result history.
	 *
	 * @param history the list this popup menu is for
	 * @param index the index of the select item from the history
	 * @param menu the menu to customize
	 */
	@Override
	public void customizePopupMenu(ResultHistoryList history, int index, JPopupMenu menu) {
		JMenuItem           menuitem;

		menuitem = new JMenuItem(getName());
		menuitem.setEnabled(handles(history, index));
		menuitem.addActionListener(getActionListener(history, index));
		menu.add(menuitem);
	}

	/**
	 * Allows to customize the popup menu for the result history.
	 *
	 * @param tab the tab the result history belongs to
	 * @param classnames the plugins (classnames) to add
	 * @param history the list this popup menu is for
	 * @param index the index of the select item from the history
	 * @param menu the menu to customize
	 */
	public static void populateMenu(AbstractExplorerTab tab, List<String> classnames, ResultHistoryList history, int index, JPopupMenu menu) {
		List<AbstractResultHistoryPlugin>   plugins;
		AbstractResultHistoryPlugin         plugin;
		String                              group;

		plugins    = new ArrayList<>();
		for (String classname: classnames) {
			try {
				plugin = (AbstractResultHistoryPlugin) Class.forName(classname).newInstance();
				plugin.setOwner(tab);
				plugins.add(plugin);
			}
			catch (Exception e) {
				System.err.println("Failed to instantiate plugin: " + classname);
				e.printStackTrace();
			}
		}
		Collections.sort(plugins);

		group = null;
		for (AbstractResultHistoryPlugin p: plugins) {
			if (group != null) {
				if (!group.equals(p.getGroup()))
					menu.addSeparator();
			}
			p.customizePopupMenu(history, index, menu);
			group = p.getGroup();
		}
	}
}
