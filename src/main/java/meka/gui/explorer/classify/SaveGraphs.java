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
 * SaveModel.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.explorer.classify;

import meka.core.MultiLabelDrawable;
import meka.gui.choosers.MekaFileChooser;
import meka.gui.core.ResultHistoryList;
import weka.gui.ExtensionFileFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;

/**
 * Allows the user to save the graphs.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SaveGraphs
		extends AbstractClassifyResultHistoryPlugin {
	private static final long serialVersionUID = -1152575716154907544L;

	/** the key in the session for the filechooser. */
	public final static String KEY_FILECHOOSER = "filechooser";

	/**
	 * Returns the group of the plugin. Used for the grouping the menu items.
	 *
	 * @return          the group
	 */
	public String getGroup() {
		return "Visualization";
	}

	/**
	 * Returns the name of the plugin. Used for the menu item text.
	 *
	 * @return          the name
	 */
	@Override
	public String getName() {
		return "Save graph(s)...";
	}

	/**
	 * Checks whether the current item can be handled. Disables/enables the menu item.
	 *
	 * @param history   the current history
	 * @param index     the selected history item
	 * @return          true if can be handled
	 */
	@Override
	public boolean handles(ResultHistoryList history, int index) {
		return (getClassifier(history, index) instanceof MultiLabelDrawable);
	}

	/**
	 * Returns the file chooser to use for the graphs.
	 *
	 * @return          the file chooser
	 */
	protected MekaFileChooser getFileChooser() {
		MekaFileChooser         result;
		ExtensionFileFilter 	filter;

		if (!hasSessionValue(KEY_FILECHOOSER)) {
			result = new MekaFileChooser();
			filter = new ExtensionFileFilter(".txt", "Text file (*.txt)");
			result.addChoosableFileFilter(filter);
			result.setFileFilter(filter);
			setSessionValue(KEY_FILECHOOSER, result);
		}

		return (MekaFileChooser) getSessionValue(KEY_FILECHOOSER);
	}

	/**
	 * Returns the action lister to use in the menu.
	 *
	 * @param history   the current history
	 * @param index     the selected history item
	 * @return          the listener
	 */
	@Override
	public ActionListener getActionListener(final ResultHistoryList history, final int index) {
		final MultiLabelDrawable d = (MultiLabelDrawable) getClassifier(history, index);
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Map<Integer, String> graphs;
				Map<Integer, Integer> types;
				java.util.List<Integer> keys;
				try {
					types = d.graphType();
					graphs = d.graph();
					keys = new ArrayList<Integer>(types.keySet());
				} catch (Exception ex) {
					System.err.println("Failed to obtain graph(s):");
					ex.printStackTrace();
					return;
				}
				int retVal = getFileChooser().showSaveDialog(null);
				if (retVal != MekaFileChooser.APPROVE_OPTION)
					return;
				FileWriter fw = null;
				BufferedWriter bw = null;
				try {
					fw = new FileWriter(getFileChooser().getSelectedFile());
					bw = new BufferedWriter(fw);
					for (Integer key : keys) {
						bw.write("Label: " + key);
						bw.newLine();
						bw.write("Type: " + types.get(key));
						bw.newLine();
						bw.newLine();
						bw.write(graphs.get(key));
						bw.newLine();
						bw.write("---");
						bw.newLine();
					}
					bw.flush();
					fw.flush();
				} catch (Exception ex) {
					System.err.println("Failed to write graph(s) to: " + getFileChooser().getSelectedFile());
					ex.printStackTrace();
				} finally {
					if (fw != null) {
						try {
							fw.close();
						} catch (Exception ex) {
							// ignored
						}
					}
					if (bw != null) {
						try {
							bw.close();
						} catch (Exception ex) {
							// ignored
						}
					}
				}
			}
		};
	}
}
