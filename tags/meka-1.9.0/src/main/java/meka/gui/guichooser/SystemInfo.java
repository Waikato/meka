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
 * SystemInfo.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.guichooser;

import meka.gui.core.GUIHelper;
import meka.gui.core.MekaFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Displays the system info data.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SystemInfo
  extends AbstractMenuItemDefinition {

	private static final long serialVersionUID = -3184143315707991090L;

	/**
	 * The name of the menu this item belongs to.
	 *
	 * @return      the menu text
	 */
	@Override
	public String getGroup() {
		return MENU_HELP;
	}

	/**
	 * The name of the menu item.
	 *
	 * @return      the menu item text
	 */
	@Override
	public String getName() {
		return "System info";
	}

	/**
	 * The name of the menu icon.
	 *
	 * @return      the file name, null if none
	 */
	@Override
	public String getIconName() {
		return "systeminfo.png";
	}

	/**
	 * Called by the menu items action listener.
	 */
	@Override
	protected void launch() {
		MekaFrame frame = new MekaFrame();
		frame.setTitle(getName());
		frame.setDefaultCloseOperation(MekaFrame.DISPOSE_ON_CLOSE);
		frame.setIconImage(GUIHelper.getLogoIcon().getImage());
		frame.setLayout(new BorderLayout());

		// get info
		Hashtable<String, String> info = new meka.core.SystemInfo().getSystemInfo();

		// sort names
		Vector<String> names = new Vector<String>();
		Enumeration<String> enm = info.keys();
		while (enm.hasMoreElements()) {
			names.add(enm.nextElement());
		}
		Collections.sort(names);

		// generate table
		String[][] data = new String[info.size()][2];
		for (int i = 0; i < names.size(); i++) {
			data[i][0] = names.get(i).toString();
			data[i][1] = info.get(data[i][0]).toString();
		}
		String[] titles = new String[]{ "Key", "Value" };
		JTable table = new JTable(data, titles);


		frame.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
