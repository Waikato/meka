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
 * CombineStatisticsFiles.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.experimenter.menu;

import meka.events.LogEvent;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.FileBasedEvaluationStatisticsHandler;
import meka.gui.choosers.EvaluationStatisticsFileChooser;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows the user to combine several statistics files into one.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class CombineStatisticsFiles
  extends AbstractExperimenterMenuItem {

	private static final long serialVersionUID = 2938840464322473481L;

	/**
	 * Returns the name of the menu this menu item will get added to.
	 *
	 * @return      the menu name
	 */
	@Override
	public String getMenu() {
		return "Statistics";
	}

	/**
	 * Returns the name of the menu item.
	 *
	 * @return      the menu item name
	 */
	@Override
	public String getItem() {
		return "Combine files...";
	}

	/**
	 * Returns the name of the icon to use.
	 *
	 * @return      the file name (no path)
	 */
	@Override
	protected String getIconName() {
		return "combinestats.gif";
	}

	/**
	 * Launches the menu item action.
	 */
	@Override
	protected void launch() {
		EvaluationStatisticsFileChooser fileChooser = new EvaluationStatisticsFileChooser();
		fileChooser.setMultiSelectionEnabled(true);

		// input
		fileChooser.setDialogTitle("Select statistics to combine");
		int retVal = fileChooser.showOpenDialog(getOwner());
		if (retVal != EvaluationStatisticsFileChooser.APPROVE_OPTION)
			return;
		final File[] input = fileChooser.getSelectedFiles();
		final FileBasedEvaluationStatisticsHandler inputReader = fileChooser.getReader();
		if (input.length < 1) {
			getOwner().logMessage(new LogEvent(this, "No input statistics files selected!"));
			return;
		}

		// output
		fileChooser.setDialogTitle("Save combined statistics to");
		retVal = fileChooser.showSaveDialog(getOwner());
		if (retVal != EvaluationStatisticsFileChooser.APPROVE_OPTION)
			return;
		final File output = fileChooser.getSelectedFile();
		final FileBasedEvaluationStatisticsHandler outputWriter = fileChooser.getWriter();

		// combine
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				inputReader.addLogListener(getOwner());
				List<EvaluationStatistics> combined = new ArrayList<>();
				// load
				for (int i = 0; i < input.length; i++) {
					inputReader.setFile(input[i]);
					List<EvaluationStatistics> stats = inputReader.read();
					if (stats != null)
						combined.addAll(stats);
				}
				inputReader.removeLogListener(getOwner());
				// save
				outputWriter.addLogListener(getOwner());
				outputWriter.setFile(output);
				outputWriter.write(combined);
				outputWriter.removeLogListener(getOwner());
				// inform user
				JOptionPane.showMessageDialog(
						getOwner(),
						"Finished combining the statistics, saved to:\n" + output,
						"Combine statistics",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
}
