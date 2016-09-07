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
 * SaveCSV.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.explorer.classify;

import meka.classifiers.multilabel.IncrementalMultiLabelClassifier;
import meka.core.Result;
import meka.gui.choosers.MekaFileChooser;
import meka.gui.core.ResultHistoryList;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.gui.ExtensionFileFilter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Allows the user to displays graphs of the performance of an incremental classifier if available.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SaveCSV
		extends AbstractClassifyResultHistoryPlugin {
	private static final long serialVersionUID = -1152375316154907544L;

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
		return "Save CSV";
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
		return (getClassifier(history, index) instanceof IncrementalMultiLabelClassifier);
	}

	/**
	 * Returns the file chooser to use for the data.
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
				Instances performance = (Instances) result.getMeasurement(IncrementalPerformance.RESULTS_SAMPLED_OVER_TIME);

				int retVal = getFileChooser().showSaveDialog(null);
				if (retVal != MekaFileChooser.APPROVE_OPTION)
					return;
				File file = getFileChooser().getSelectedFile();


				try {

					CSVSaver saver = new CSVSaver();
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
