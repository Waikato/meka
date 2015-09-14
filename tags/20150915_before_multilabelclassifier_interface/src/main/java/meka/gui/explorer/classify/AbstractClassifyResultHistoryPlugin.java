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
import meka.gui.explorer.AbstractResultHistoryPlugin;
import meka.gui.goe.GenericObjectEditor;

import javax.swing.*;
import java.util.List;

/**
 * Ancestor for plugins that work .
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractClassifyResultHistoryPlugin
		extends AbstractResultHistoryPlugin {

	private static final long serialVersionUID = 7408923951811192736L;

	/**
	 * Returns all the available plugins.
	 *
	 * @return          the classnames of the plugins
	 */
	public static List<String> getPlugins() {
		return GenericObjectEditor.getClassnames(AbstractClassifyResultHistoryPlugin.class.getName());
	}

	/**
	 * Allows to customize the popup menu for the classify result history.
	 *
	 * @param tab the tab the result history belongs to
	 * @param history the list this popup menu is for
	 * @param index the index of the select item from the history
	 * @param menu the menu to customize
	 */
	public static void populateMenu(AbstractExplorerTab tab, ResultHistoryList history, int index, JPopupMenu menu) {
		AbstractResultHistoryPlugin.populateMenu(tab, getPlugins(), history, index, menu);
	}
}
