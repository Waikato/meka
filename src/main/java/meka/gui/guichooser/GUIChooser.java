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
 * GUIChooser.java
 * Copyright (C) 2015-2018 University of Waikato, Hamilton, NZ
 */

package meka.gui.guichooser;

import meka.core.Version;
import meka.gui.core.GUIHelper;
import meka.gui.core.GUILauncher;
import meka.gui.core.MekaPanel;
import meka.gui.core.MenuBarProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * The main application launcher.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class GUIChooser
    extends MekaPanel
    implements MenuBarProvider {

	private static final long serialVersionUID = 7629211225812516714L;
	/** the menu bar. */
	protected JMenuBar m_MenuBar;

	/** the "close" menu item. */
	protected JMenuItem m_MenuItemProgramClose;

	/** the menus. */
	protected HashMap<String,JMenu> m_Menus;

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		java.util.List<AbstractMenuItemDefinition>  definitions;
		JPanel                                      imagePanel;
		JPanel                                      versionPanel;
		JPanel                                      rightPanel;
		JPanel                                      buttonsPanel;

		super.initGUI();

		setLayout(new BorderLayout(5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		imagePanel = new JPanel(new BorderLayout());
		imagePanel.add(new JLabel(GUIHelper.getIcon("GUIChooser.png")), BorderLayout.CENTER);
		add(imagePanel, BorderLayout.CENTER);
		versionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		versionPanel.add(new JLabel(Version.getVersion()), BorderLayout.SOUTH);
		imagePanel.add(versionPanel, BorderLayout.SOUTH);

		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(BorderFactory.createTitledBorder("Shortcuts"));
		add(rightPanel, BorderLayout.EAST);

		buttonsPanel = new JPanel(new GridLayout(0, 1));
		rightPanel.add(buttonsPanel, BorderLayout.NORTH);

		definitions = loadDefinitions();
		for (AbstractMenuItemDefinition definition: definitions) {
			if (definition.isShortcutButton())
				buttonsPanel.add(definition.getButton());
		}
	}

	/**
	 * Loads the menu item definitions.
	 *
	 * @return      the definitions
	 */
	protected java.util.List<AbstractMenuItemDefinition> loadDefinitions() {
		java.util.List<AbstractMenuItemDefinition>  result;
		java.util.List<String>                      classnames;

		result = new ArrayList<>();
		classnames  = AbstractMenuItemDefinition.getDefinitions();
		for (String classname: classnames) {
			try {
				AbstractMenuItemDefinition definition = (AbstractMenuItemDefinition) Class.forName(classname).newInstance();
				result.add(definition);
			}
			catch (Exception e) {
				System.err.println("Failed to instantiate menu definition: " + classname);
				e.printStackTrace();
			}
		}
		Collections.sort(result);

		return result;
	}

	/**
	 * Returns the menu bar to use.
	 *
	 * @return the menu bar
	 */
	public JMenuBar getMenuBar() {
		JMenuBar	                                result;
		JMenu		                                menu;
		JMenuItem	                                menuitem;
		java.util.List<AbstractMenuItemDefinition>  definitions;

		if (m_MenuBar == null) {
			result  = new JMenuBar();
			m_Menus = new HashMap<>();

			// Program
			menu = new JMenu(AbstractMenuItemDefinition.MENU_PROGRAM);
			result.add(menu);
			m_Menus.put(menu.getText(), menu);

			// add the menu item plugins
			definitions = loadDefinitions();

			// create menus
			for (AbstractMenuItemDefinition definition: definitions) {
				if (m_Menus.containsKey(definition.getGroup()))
					continue;
				// help menu gets added at end
				if (definition.getGroup().equals(AbstractMenuItemDefinition.MENU_HELP))
					continue;
				menu = new JMenu(definition.getGroup());
				result.add(menu);
				m_Menus.put(menu.getText(), menu);
			}
			// add help menu at end
			menu = new JMenu(AbstractMenuItemDefinition.MENU_HELP);
			result.add(menu);
			m_Menus.put(menu.getText(), menu);

			// add menu items
			for (AbstractMenuItemDefinition definition: definitions) {
				menu = m_Menus.get(definition.getGroup());
				menu.add(definition.getMenuItem());
			}

			// Program/Close
			menuitem = new JMenuItem("Close", GUIHelper.getIcon("exit.png"));
			menuitem.setMnemonic('C');
			menuitem.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed Q"));
			menuitem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					close();
				}
			});
			m_Menus.get(AbstractMenuItemDefinition.MENU_PROGRAM).add(menuitem);
			m_MenuItemProgramClose = menuitem;

			m_MenuBar = result;
		}

		result = m_MenuBar;

		return result;
	}

	/**
	 * Closes the application.
	 */
	public void close() {
		closeParent();
		System.exit(0);
	}

	/**
	 * Starts the GUI.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) throws Exception {
		GUILauncher.launchApplication(GUIChooser.class, "MEKA GUIChooser", false, new String[0]);
	}
}
