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
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import meka.gui.core.MekaPanel;
import weka.core.Instances;

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
	
	/**
	 * Initializes the tab.
	 * 
	 * @param owner the Explorer this tab belongs to
	 */
	public AbstractExplorerTab(Explorer owner) {
		super();
		m_Owner = owner;
	}
	
	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();
		
		m_Data = null;
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
	 * <p/>
	 * Default implementation does nothing.
	 */
	protected void update() {
	}
}
