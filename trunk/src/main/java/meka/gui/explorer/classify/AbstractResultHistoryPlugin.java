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

package meka.gui.explorer.classify;

import meka.gui.core.ResultHistoryList;
import meka.gui.explorer.AbstractExplorerTab;
import meka.gui.goe.GenericObjectEditor;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.*;

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
	 * Comparison on the name.
	 *
	 * @param o         the other plugin
	 * @return          less than 0, equal to 0, greater than 0 if the name is less, equal or greater
	 * @see             #getName()
	 */
    public int compareTo(AbstractResultHistoryPlugin o) {
	    return getName().compareTo(o.getName());
    }

	/**
	 * Checks equality using the name of the plugin.
	 *
	 * @param o         the other object
	 * @return          true if other object is a plugin with the same name
	 * @see             #getName()
	 */
	public boolean equals(Object o) {
		return (o instanceof AbstractResultHistoryPlugin) && (compareTo((AbstractResultHistoryPlugin) o) == 0);
	}

	/**
	 * Returns the name of the plugin. Used for the menu item text.
	 *
	 * @return          the name
	 */
	public abstract String getName();

	/**
	 * Checks whether the current item can be handled. Disables/enables the menu item.
	 *
	 * @param suffix    the suffix of the item
	 * @param item      the item itself
	 * @return          true if can be handled
	 */
	public abstract boolean handles(String suffix, Object item);

	/**
	 * Returns the action lister to use in the menu.
	 *
	 * @param history   the current history
	 * @param index     the selected history item
	 * @param suffix    the suffix of the selected item
	 * @param item      the selected item itself
	 * @return          the listener
	 */
	public abstract ActionListener getActionListener(final ResultHistoryList history, final int index, final String suffix, final Object item);

	/**
	 * Allows to customize the popup menu for the result history.
	 *
	 * @param history the list this popup menu is for
	 * @param index the index of the select item from the history
	 * @param menu the menu to customize
	 */
	@Override
	public void customizePopupMenu(final ResultHistoryList history, final int index, final JPopupMenu menu) {
		JMenuItem           menuitem;
		final String 		suffix;
		final Object		item;

		suffix = history.getSuffixAt(index);
		item = history.getPayloadAt(index);

		menuitem = new JMenuItem(getName());
		menuitem.setEnabled(handles(suffix, item));
		menuitem.addActionListener(getActionListener(history, index, suffix, item));
		menu.add(menuitem);
	}

	/**
	 * Returns all the available plugins.
	 *
	 * @return          the classnames of the plugins
	 */
	public static List<String> getPlugins() {
		return GenericObjectEditor.getClassnames(AbstractResultHistoryPlugin.class.getName());
	}

	/**
	 * Allows to customize the popup menu for the result history.
	 *
	 * @param history the list this popup menu is for
	 * @param index the index of the select item from the history
	 * @param menu the menu to customize
	 */
	public static void populateMenu(AbstractExplorerTab tab, ResultHistoryList history, int index, JPopupMenu menu) {
		List<String>                        classnames;
		List<AbstractResultHistoryPlugin>   plugins;
		AbstractResultHistoryPlugin         plugin;

		classnames = getPlugins();
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

		for (AbstractResultHistoryPlugin p: plugins)
			p.customizePopupMenu(history, index, menu);
	}
}
