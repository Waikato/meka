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
 * ListWithButtons.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * A JList with buttons.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ListWithButtons
		extends MekaPanelWithButtons {

	private static final long serialVersionUID = 8924105224758572216L;

	/** the list. */
	protected JList m_List;

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		m_List = new JList();
		add(new BaseScrollPane(m_List), BorderLayout.CENTER);
	}

	/**
	 * Returns the list.
	 *
	 * @return      the list
	 */
	public JList getList() {
		return m_List;
	}
}
