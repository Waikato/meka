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
 * AbstractRecentItemsHandler.java
 * Copyright (C) 2013-2020 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import meka.core.Project;
import meka.core.PropsUtils;
import meka.gui.events.RecentItemEvent;
import meka.gui.events.RecentItemListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Ancestor for classes that handle a list of recent items. Reads/writes them from/to
 * a props file in the application's home directory.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 * @param <M> the type of menu to use
 * @param <T> the type of item to use
 */
public abstract class AbstractRecentItemsHandler<M, T>
		implements Serializable {

	/** for serialization. */
	private static final long serialVersionUID = 7532226757387619342L;

	/** the props file to use. */
	protected String m_PropertiesFile;

	/** the prefix for the properties. */
	protected String m_PropertyPrefix;

	/** the maximum number of items to keep. */
	protected int m_MaxCount;

	/** the menu to add the items as sub-items to. */
	protected M m_Menu;

	/** the items. */
	protected List<T> m_RecentItems;

	/** whether to ignore changes temporarily. */
	protected boolean m_IgnoreChanges;

	/** the event listeners. */
	protected HashSet<RecentItemListener<M,T>> m_Listeners;

	/**
	 * Initializes the handler with a maximum of 5 items.
	 *
	 * @param propsFile	the props file to store the items in
	 * @param menu	the menu to add the recent items as subitems to
	 */
	public AbstractRecentItemsHandler(String propsFile, M menu) {
		this(propsFile, 5, menu);
	}

	/**
	 * Initializes the handler.
	 *
	 * @param propsFile	the props file to store the items in
	 * @param maxCount	the maximum number of items to keep in menu
	 * @param menu	the menu to add the recent items as subitems to
	 */
	public AbstractRecentItemsHandler(String propsFile, int maxCount, M menu) {
		this(propsFile, null, maxCount, menu);
	}

	/**
	 * Initializes the handler.
	 *
	 * @param propsFile	the props file to store the items in
	 * @param propPrefix	the properties prefix, use null to ignore
	 * @param maxCount	the maximum number of items to keep in menu
	 * @param menu	the menu to add the recent items as subitems to
	 */
	public AbstractRecentItemsHandler(String propsFile, String propPrefix, int maxCount, M menu) {
		super();

		if (!((menu instanceof JMenu) || (menu instanceof JPopupMenu)))
			throw new IllegalArgumentException(
					"Menu must be derived from " + JMenu.class.getName()
							+ " or " + JPopupMenu.class.getName()
							+ ", provided: " + menu.getClass().getName());

		m_PropertiesFile = Project.expandFile(new File(propsFile).getName()).getAbsolutePath();
		m_PropertyPrefix = propPrefix;
		m_MaxCount       = maxCount;
		m_Menu           = menu;
		m_RecentItems    = new ArrayList<T>();
		m_IgnoreChanges  = false;
		m_Listeners      = new HashSet<RecentItemListener<M,T>>();

		readProps();
		updateMenu();
	}

	/**
	 * Returns the props file used to store the recent items in.
	 *
	 * @return		the filename
	 */
	public String getPropertiesFile() {
		return m_PropertiesFile;
	}

	/**
	 * Returns the prefix for the property names.
	 *
	 * @return		the prefix
	 */
	public String getPropertyPrefix() {
		return m_PropertyPrefix;
	}

	/**
	 * Returns the maximum number of items to keep.
	 *
	 * @return		the maximum number
	 */
	public int getMaxCount() {
		return m_MaxCount;
	}

	/**
	 * Returns the menu to add the recent items as subitems to.
	 *
	 * @return		the menu
	 */
	public M getMenu() {
		return m_Menu;
	}

	/**
	 * Returns the key to use for the counts in the props file.
	 *
	 * @return		the key
	 */
	protected abstract String getCountKey();

	/**
	 * Returns the key prefix to use for the items in the props file.
	 *
	 * @return		the prefix
	 */
	protected abstract String getItemPrefix();

	/**
	 * Turns an object into a string for storing in the props.
	 *
	 * @param obj		the object to convert
	 * @return		the string representation
	 */
	protected abstract String toString(T obj);

	/**
	 * Turns the string obtained from the props into an object again.
	 *
	 * @param s		the string representation
	 * @return		the parsed object
	 */
	protected abstract T fromString(String s);

	/**
	 * Adds the prefix to the property name if provided.
	 *
	 * @param property	the property to expand
	 * @return		the expanded property name
	 */
	protected String expand(String property) {
		if (m_PropertyPrefix == null)
			return property;
		else
			return m_PropertyPrefix + property;
	}

	/**
	 * Loads the properties file from disk, if possible.
	 *
	 * @return		the properties file
	 */
	protected Properties loadProps() {
		Properties	result;
		File		file;
		FileReader reader;

		reader = null;
		try {
			result = new Properties();
			file = new File(m_PropertiesFile);
			if (file.exists()) {
				reader = new FileReader(m_PropertiesFile);
				result.load(reader);
			}
		}
		catch (Exception e) {
			System.err.println("Failed to load properties: " + m_PropertiesFile);
			e.printStackTrace();
			result = new Properties();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception e) {
					// ignored
				}
			}
		}

		return result;
	}

	/**
	 * Checks the item after obtaining from the props file.
	 * <br><br>
	 * Default implementation performs no checks and always returns true.
	 *
	 * @param item	the item to check
	 * @return		true if checks passed
	 */
	protected boolean check(T item) {
		return true;
	}

	/**
	 * Reads the recent items from the props file.
	 */
	protected void readProps() {
		int			count;
		Properties	props;
		int			i;
		String		itemStr;
		T			item;

		m_IgnoreChanges = true;

		props = loadProps();
		count = Integer.parseInt(props.getProperty(expand(getCountKey()), "0"));
		m_RecentItems.clear();
		for (i = count - 1; i >= 0; i--) {
			itemStr = props.getProperty(getItemPrefix() + i, "");
			if (itemStr.length() > 0) {
				item = fromString(itemStr);
				if (check(item))
					addRecentItem(item);
			}
		}

		m_IgnoreChanges = false;
	}

	/**
	 * Writes the current recent items back to the props file.
	 */
	protected synchronized void writeProps() {
		Properties		props;
		int				i;

		props = loadProps();
		props.setProperty(getCountKey(), "" + m_RecentItems.size());
		for (i = 0; i < m_RecentItems.size(); i++)
			props.setProperty(getItemPrefix() + i, toString(m_RecentItems.get(i)));

		PropsUtils.write(props, m_PropertiesFile);
	}

	/**
	 * Hook method which gets executed just before the menu gets updated.
	 * <br><br>
	 * Default implementation does nothing.
	 */
	protected void preUpdateMenu() {
	}

	/**
	 * Generates the text for the menuitem.
	 *
	 * @param index	the index of the item
	 * @param item	the item itself
	 * @return		the generated text
	 */
	protected abstract String createMenuItemText(int index, T item);

	/**
	 * Updates the menu.
	 */
	protected void doUpdateMenu() {
		int		i;
		JMenuItem	menuitem;

		// clear menu
		if (m_Menu instanceof JMenu) {
			((JMenu) m_Menu).removeAll();
			((JMenu) m_Menu).setEnabled(m_RecentItems.size() > 0);
		}
		else if (m_Menu instanceof JPopupMenu) {
			((JPopupMenu) m_Menu).removeAll();
			((JPopupMenu) m_Menu).setEnabled(m_RecentItems.size() > 0);
		}

		// add menu items
		for (i = 0; i < m_RecentItems.size(); i++) {
			final T item = m_RecentItems.get(i);
			menuitem = new JMenuItem((i+1) + " - " + createMenuItemText(i, item));
			if (i < 9)
				menuitem.setMnemonic(Integer.toString(i+1).charAt(0));
			if (i == 9)
				menuitem.setMnemonic('0');
			menuitem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					notifyRecentItemListenersOfSelect(item);
				}
			});

			if (m_Menu instanceof JMenu) {
				if (i < 9)
					menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed " + (i+1)));
				if (i == 9)
					menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed 0"));
				((JMenu) m_Menu).add(menuitem);
			}
			else if (m_Menu instanceof JPopupMenu)
				((JPopupMenu) m_Menu).add(menuitem);
		}

		// add "clear"
		if (m_RecentItems.size() > 0) {
			if (m_Menu instanceof JMenu)
				((JMenu) m_Menu).addSeparator();
			else if (m_Menu instanceof JPopupMenu)
				((JPopupMenu) m_Menu).addSeparator();
			menuitem = new JMenuItem("Clear");
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeAll();
				}
			});
			if (m_Menu instanceof JMenu)
				((JMenu) m_Menu).add(menuitem);
			else if (m_Menu instanceof JPopupMenu)
				((JPopupMenu) m_Menu).add(menuitem);
		}
	}

	/**
	 * Hook method which gets executed just after the menu was updated.
	 * <br><br>
	 * Default implementation does nothing.
	 */
	protected void postUpdateMenu() {
	}

	/**
	 * Updates the menu with the currently stored recent files.
	 */
	protected void updateMenu() {
		preUpdateMenu();
		doUpdateMenu();
		postUpdateMenu();
	}

	/**
	 * Adds the item to the internal list.
	 *
	 * @param item	the item to add to the list
	 */
	public synchronized void addRecentItem(T item) {
		item = fromString(toString(item));

		// is it the first item again? -> ignore it
		if (m_RecentItems.size() > 0) {
			if (item.equals(m_RecentItems.get(0)))
				return;
		}

		m_RecentItems.remove(item);
		m_RecentItems.add(0, item);
		while (m_RecentItems.size() > m_MaxCount)
			m_RecentItems.remove(m_RecentItems.size() - 1);

		if (m_IgnoreChanges)
			return;

		writeProps();
		updateMenu();

		notifyRecentItemListenersOfAdd(item);
	}

	/**
	 * Removes the item from the internal list, e.g., if it no longer exists on
	 * disk.
	 *
	 * @param item	the item to remove from the list
	 */
	public synchronized void removeRecentItem(T item) {
		item = fromString(toString(item));
		m_RecentItems.remove(item);

		if (m_IgnoreChanges)
			return;

		writeProps();
		updateMenu();
	}

	/**
	 * Removes all items from the internal list.
	 */
	public synchronized void removeAll() {
		m_RecentItems.clear();

		if (m_IgnoreChanges)
			return;

		writeProps();
		updateMenu();
	}

	/**
	 * Returns the currently stored recent items.
	 *
	 * @return		the items
	 */
	public List<T> getRecentItems() {
		return new ArrayList(m_RecentItems);
	}

	/**
	 * Returns the number of recent items currently stored.
	 *
	 * @return		the number of items
	 */
	public int size() {
		return m_RecentItems.size();
	}

	/**
	 * Adds the listener to the internal list.
	 *
	 * @param l		the listener to add
	 */
	public void addRecentItemListener(RecentItemListener l) {
		m_Listeners.add(l);
	}

	/**
	 * Removes the listener from the internal list.
	 *
	 * @param l		the listener to remove
	 */
	public void removeRecentItemListener(RecentItemListener l) {
		m_Listeners.remove(l);
	}

	/**
	 * Notifies the listeners of a item that got added.
	 *
	 * @param item	the affected item
	 */
	protected void notifyRecentItemListenersOfAdd(T item) {
		Iterator<RecentItemListener<M,T>>	iter;
		RecentItemEvent<M,T>		e;

		e    = new RecentItemEvent<M,T>(this, item);
		iter = m_Listeners.iterator();
		while (iter.hasNext())
			iter.next().recentItemAdded(e);
	}

	/**
	 * Notifies the listeners of a item that got selected.
	 *
	 * @param item	the affected item
	 */
	protected void notifyRecentItemListenersOfSelect(T item) {
		Iterator<RecentItemListener<M,T>>	iter;
		RecentItemEvent			e;

		e    = new RecentItemEvent<M,T>(this, item);
		iter = m_Listeners.iterator();
		while (iter.hasNext())
			iter.next().recentItemSelected(e);
	}
}
