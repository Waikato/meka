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
 * MekaApplicationLauncher.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import meka.core.Project;
import meka.gui.goe.GenericObjectEditor;

import javax.swing.*;
import java.awt.*;

/**
 * Helper class for launching applications and frames.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class GUILauncher {

	/**
	 * Embeds the panel in a frame and displays.
	 * Uses GUIHelper.props for determining window packing state/dimensions.
	 *
	 * @param panelCls the panel class to instantiated and place in the frame
	 * @param title the title for the fraame
	 * @param center whether to center the frame
	 * @return the generated frame
	 * @throws Exception if failed to instantiate panel
	 */
	public static MekaFrame launchFrame(Class panelCls, String title, boolean center) throws Exception {
		JPanel panel = (JPanel) panelCls.newInstance();
		MekaFrame result = new MekaFrame();
		result.setTitle(title);
		result.setDefaultCloseOperation(MekaFrame.DISPOSE_ON_CLOSE);
		result.setIconImage(GUIHelper.getLogoIcon().getImage());
		result.setLayout(new BorderLayout());
		result.add(panel, BorderLayout.CENTER);
		if (panel instanceof MenuBarProvider)
			result.setJMenuBar(((MenuBarProvider) panel).getMenuBar());
		if (GUIHelper.getPackFrame(panel.getClass()))
			result.pack();
		else
			result.setSize(GUIHelper.getDefaultFrameDimensions(panel.getClass()));
		if (center)
			result.setLocationRelativeTo(null);
		result.setVisible(true);
		return result;
	}

	/**
	 * Embeds the panel in a frame and displays. Also initializes the project and GOE editors.
	 * Uses GUIHelper.props for determining window packing state/dimensions.
	 *
	 * @param panelCls the panel class to instantiated and place in the frame
	 * @param title the title for the fraame
	 * @param center whether to center the frame
	 * @param args the commandline arguments
	 * @return the generated frame
	 * @throws Exception if failed to instantiate panel
	 */
	public static MekaFrame launchApplication(Class panelCls, String title, boolean center, String[] args) throws Exception {
		Project.initialize();
		GenericObjectEditor.registerAllEditors();
		JPanel panel = (JPanel) panelCls.newInstance();
		MekaFrame result = new MekaFrame();
		result.setTitle(title);
		result.setDefaultCloseOperation(MekaFrame.EXIT_ON_CLOSE);
		result.setIconImage(GUIHelper.getLogoIcon().getImage());
		result.setLayout(new BorderLayout());
		result.add(panel, BorderLayout.CENTER);
		if (panel instanceof MenuBarProvider)
			result.setJMenuBar(((MenuBarProvider) panel).getMenuBar());
		if (GUIHelper.getPackFrame(panel.getClass()))
			result.pack();
		else
			result.setSize(GUIHelper.getDefaultFrameDimensions(panel.getClass()));
		if (center)
			result.setLocationRelativeTo(null);
		result.setVisible(true);
		if ((panel instanceof CommandLineArgsHandler) && (args.length > 0))
			((CommandLineArgsHandler) panel).processCommandLineArgs(args);
		return result;
	}
}
