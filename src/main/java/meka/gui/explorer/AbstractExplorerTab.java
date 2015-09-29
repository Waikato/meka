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
 * AbstractExplorerTab.java
 * Copyright (C) 2012-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import meka.gui.core.MekaPanel;
import meka.gui.goe.GenericObjectEditor;
import weka.core.Instances;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;

/**
 * Ancestor for tabs in the Explorer.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractExplorerTab
	extends MekaPanel {

	/** for serialization. */
	private static final long serialVersionUID = -3326953872854691533L;

	/** the Explorer instance this tab belongs to. */
	protected Explorer m_Owner;
	
	/** the current dataset. */
	protected Instances m_Data;

	/** the session object. */
	protected transient HashMap<Class,HashMap<String,Object>> m_Session;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();
		
		m_Data    = null;
		m_Session = null;
	}

	/**
	 * Sets the Explorer instance this tab belongs to.
	 *
	 * @param value the Explorer instance
	 */
	public void setOwner(Explorer value) {
		m_Owner = value;
	}

	/**
	 * Returns the Explorer instance this tab belongs to.
	 * 
	 * @return the Explorer instance
	 */
	public Explorer getOwner() {
		return m_Owner;
	}
	
	/**
	 * Returns the title of the tab.
	 * 
	 * @return the title
	 */
	public abstract String getTitle();

	/**
	 * Returns an optional menu to be added to the Explorer menu.
	 * <br>
	 * Default implementation returns null.
	 *
	 * @return the menu
	 */
	public JMenu getMenu() {
		return null;
	}

	/**
	 * Returns the session object.
	 *
	 * @return the session, for storing temporary objects
	 */
	public synchronized HashMap<Class,HashMap<String,Object>> getSession() {
		if (m_Session == null)
			m_Session = new HashMap<>();
		return m_Session;
	}

	/**
	 * Returns whether data is currently present.
	 * 
	 * @return true if data is loaded
	 */
	public boolean hasData() {
		return (m_Data != null);
	}
	
	/**
	 * Returns the current data.
	 * 
	 * @return the data, can be null if none set
	 */
	public Instances getData() {
		return m_Data;
	}
	
	/**
	 * Sets the data to use.
	 * 
	 * @param value the data to use
	 */
	public void setData(Instances value) {
		m_Data = value;
		update();
	}
	
	/**
	 * Gets called when the data changed.
	 * <p>
	 * Default implementation does nothing.
	 */
	protected void update() {
	}

	/**
	 * Displays the specified status message.
	 * 
	 * @param msg		the message to display
	 */
	public void showStatus(String msg) {
		m_Owner.getStatusBar().showStatus(msg);
	}

	/**
	 * Clears status message.
	 */
	public void clearStatus() {
		m_Owner.getStatusBar().clearStatus();
	}

	/**
	 * Starts the animated icon, without setting status message.
	 */
	public void startBusy() {
		m_Owner.getStatusBar().startBusy();
	}

	/**
	 * Starts the animated icon, setting the specified status message.
	 * 
	 * @param msg		the message to display
	 */
	public void startBusy(String msg) {
		log(msg);
		m_Owner.getStatusBar().startBusy(msg);
	}

	/**
	 * Stops the animated icon, without setting status message.
	 */
	public void finishBusy() {
		log("Finished");
		m_Owner.getStatusBar().finishBusy("");
	}

	/**
	 * Stops the animated icon, setting the specified status message.
	 * 
	 * @param msg		the message to display
	 */
	public void finishBusy(String msg) {
		log(msg);
		m_Owner.getStatusBar().finishBusy(msg);
	}

	/**
	 * Returns all the available tabs.
	 *
	 * @return          the classnames of the tabs
	 */
	public static List<String> getTabs() {
		return GenericObjectEditor.getClassnames(AbstractExplorerTab.class.getName());
	}

	/**
	 * For logging messages. Uses stderr if no listeners defined.
	 *
	 * @param msg       the message to output
	 */
	protected synchronized void log(String msg) {
		m_Owner.log(this, msg);
	}

	/**
	 * Logs the stacktrace along with the message on the log tab and returns a
	 * combination of both of them as string.
	 *
	 * @param msg		the message for the exception
	 * @param t		the exception
	 * @return		the full error message (message + stacktrace)
	 */
	public String handleException(String msg, Throwable t) {
		return m_Owner.handleException(this, msg, t);
	}
}
