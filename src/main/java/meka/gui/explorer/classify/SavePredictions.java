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
 * SavePredictions.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.explorer.classify;

import meka.core.MultiLabelDrawable;
import meka.gui.choosers.MekaFileChooser;
import meka.core.Result;
import meka.gui.core.ResultHistoryList;
import weka.gui.ExtensionFileFilter;
import weka.core.Instances;
import weka.core.converters.CSVSaver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Allows the user to save the predictions to CSV.
 *
 * @author Jesse Read
 * @version $Revision$
 */
public class SavePredictions
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
		return "Export Predictions (CSV)";
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
		return true;
	}

	/**
	 * Returns the file chooser to use for the results.
	 *
	 * @return          the file chooser
	 */
	protected MekaFileChooser getFileChooser() {
		MekaFileChooser         result;
		ExtensionFileFilter filter;

		if (!hasSessionValue(KEY_FILECHOOSER)) {
			result = new MekaFileChooser();
			filter = new ExtensionFileFilter(".csv", "CSV file (*.csv)");
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
		final Result result = history.getResultAt(index);
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int retVal = getFileChooser().showSaveDialog(null);
				if (retVal != MekaFileChooser.APPROVE_OPTION)
					return;
				File file = getFileChooser().getSelectedFile();

				try {

					CSVSaver saver = new CSVSaver();
					Instances performance = Result.getPredictionsAsInstances(result);
					saver.setInstances(performance);
					saver.setFile(getFileChooser().getSelectedFile());
					saver.writeBatch();
				} catch (Exception ex) {
					String msg = "Failed to write to '" + file + "'!";
					System.err.println(msg);
					ex.printStackTrace();
					JOptionPane.showMessageDialog( null, msg + "\n" + e);
				}
			}
		};
	}
}
