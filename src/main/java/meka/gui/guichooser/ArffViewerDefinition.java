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
 * ArffViewerDefinition.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.guichooser;

import meka.gui.core.GUIHelper;
import meka.gui.core.MekaFrame;
import weka.gui.arffviewer.ArffViewerMainPanel;

import java.awt.*;

/**
 * Launches the Arff Viewer.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ArffViewerDefinition
  extends AbstractMenuItemDefinition {

	private static final long serialVersionUID = -3184143315707991090L;

	/**
	 * The name of the menu this item belongs to.
	 *
	 * @return      the menu text
	 */
	@Override
	public String getGroup() {
		return MENU_TOOLS;
	}

	/**
	 * The name of the menu item.
	 *
	 * @return      the menu item text
	 */
	@Override
	public String getName() {
		return "Arff viewer";
	}

	/**
	 * The name of the menu icon.
	 *
	 * @return      the file name, null if none
	 */
	@Override
	public String getIconName() {
		return "report.gif";
	}

	/**
	 * Called by the menu items action listener.
	 */
	@Override
	protected void launch() {
		MekaFrame frame = new MekaFrame();
		ArffViewerMainPanel main = new ArffViewerMainPanel(frame);
		frame.setTitle("Arff viewer");
		frame.setDefaultCloseOperation(MekaFrame.DISPOSE_ON_CLOSE);
		frame.setIconImage(GUIHelper.getLogoIcon().getImage());
		frame.setLayout(new BorderLayout());
		frame.add(main, BorderLayout.CENTER);
		frame.setJMenuBar(main.getMenu());
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
