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
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.PluginManager;

import javax.swing.JPopupMenu;
import java.lang.reflect.Array;
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
	 * Returns the classifier from the history list.
	 *
	 * @param history       the history
	 * @param index         the index in the history
	 * @return              the classifier
	 */
	public Classifier getClassifier(ResultHistoryList history, int index) {
		Object  payload;

		payload = history.getPayloadAt(index);
		if (payload.getClass().isArray()) {
			return (Classifier) Array.get(payload, 0);
		}
		else {
			return (Classifier) payload;
		}
	}

	/**
	 * Returns the dataset header from the history list.
	 *
	 * @param history       the history
	 * @param index         the index in the history
	 * @return              the instances, null if not available
	 */
	public Instances getHeader(ResultHistoryList history, int index) {
		Object  payload;

		payload = history.getPayloadAt(index);
		if (payload.getClass().isArray()) {
			return (Instances) Array.get(payload, 1);
		}
		else {
			return null;
		}
	}

	/**
	 * Returns all the available plugins.
	 *
	 * @return          the classnames of the plugins
	 */
	public static List<String> getPlugins() {
		return PluginManager.getPluginNamesOfTypeList(AbstractClassifyResultHistoryPlugin.class.getName());
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
