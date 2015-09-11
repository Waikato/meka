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
 * IncrementalStatistics.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.explorer.classify;

import meka.classifiers.multilabel.MultilabelClassifier;
import meka.classifiers.multitarget.MultiTargetClassifier;
import meka.core.Result;
import meka.gui.core.ResultHistoryList;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Allows the user to display the ROC curves per label.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ShowROC
		extends AbstractClassifyResultHistoryPlugin {
	private static final long serialVersionUID = -1152575716154907544L;

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
		return "Show ROC";
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
		return (history.getPayloadAt(index) instanceof MultilabelClassifier)
				|| (history.getPayloadAt(index) instanceof MultiTargetClassifier);
	}

	/**
	 * Creates a panel displaying the ROC data.
	 *
	 * @param data          the threshold curve data
	 * @param title         the title of the plot
	 * @return              the panel
	 * @throws Exception    if plot generation fails
	 */
	protected ThresholdVisualizePanel createPanel(Instances data, String title) throws Exception {
		ThresholdVisualizePanel result = new ThresholdVisualizePanel();
		PlotData2D plot = new PlotData2D(data);
		plot.setPlotName(title);
		plot.m_displayAllPoints = true;
		boolean[] connectPoints = new boolean [data.numInstances()];
		for (int cp = 1; cp < connectPoints.length; cp++)
			connectPoints[cp] = true;
		plot.setConnectPoints(connectPoints);
		result.addPlot(plot);
		if (data.attribute(ThresholdCurve.FP_RATE_NAME) != null)
			result.setXIndex(data.attribute(ThresholdCurve.FP_RATE_NAME).index());
		if (data.attribute(ThresholdCurve.TP_RATE_NAME) != null)
			result.setYIndex(data.attribute(ThresholdCurve.TP_RATE_NAME).index());
		return result;
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
				JDialog dialog = new JDialog((Frame) null, history.getSuffixAt(index), false);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				JTabbedPane tabbed = new JTabbedPane();
				dialog.getContentPane().setLayout(new BorderLayout());
				dialog.getContentPane().add(tabbed, BorderLayout.CENTER);
				Instances[] curves = (Instances[]) result.getMeasurement("Curve Data");
				for (int i = 0; i < curves.length; i++) {
					try {
						ThresholdVisualizePanel panel = createPanel(curves[i], "Label " + i);
						tabbed.addTab("" + i, panel);
					}
					catch (Exception ex) {
						System.err.println("Failed to create ROC plot for label " + i);
						ex.printStackTrace();
					}
				}
				dialog.setSize(800, 600);
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
			}
		};
	}
}
