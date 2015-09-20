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
 * PrecisionRecallCurve.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.guichooser;

import meka.gui.core.GUIHelper;
import meka.gui.core.MekaFrame;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;
import weka.core.Utils;
import weka.gui.ConverterFileChooser;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Displays an ROC curve from a dataset.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PrecisionRecallCurve
		extends AbstractMenuItemDefinition {

	private static final long serialVersionUID = -3184143315707991090L;

	/** filechooser for ROCs. */
	protected ConverterFileChooser m_FileChooser;

	/**
	 * The name of the menu this item belongs to.
	 *
	 * @return      the menu text
	 */
	@Override
	public String getGroup() {
		return MENU_VISUALIZATION;
	}

	/**
	 * The name of the menu item.
	 *
	 * @return      the menu item text
	 */
	@Override
	public String getName() {
		return "Precision-recall curve";
	}

	/**
	 * The name of the menu icon.
	 *
	 * @return      the file name, null if none
	 */
	@Override
	public String getIconName() {
		return "prc.gif";
	}

	/**
	 * Called by the menu items action listener.
	 */
	@Override
	protected void launch() {
		m_FileChooser = GUIHelper.newConverterFileChooser();
		// choose file
		int retVal = m_FileChooser.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return;
		File file = m_FileChooser.getSelectedFile();

		// create plot
		Instances data;
		try {
			data = m_FileChooser.getLoader().getDataSet();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(
					null,
					"Error loading file '" + file + "':\n" + e,
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		data.setClassIndex(data.numAttributes() - 1);
		ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
		vmc.setROCString("(Area under PRC = " +
				Utils.doubleToString(ThresholdCurve.getPRCArea(data), 4) + ")");
		vmc.setName(data.relationName());
		PlotData2D tempd = new PlotData2D(data);
		tempd.setPlotName(data.relationName());
		tempd.m_displayAllPoints = true;
		// specify which points are connected
		boolean[] cp = new boolean[data.numInstances()];
		for (int n = 1; n < cp.length; n++)
			cp[n] = true;
		try {
			tempd.setConnectPoints(cp);
			vmc.addPlot(tempd);
			if (data.attribute(ThresholdCurve.RECALL_NAME) != null)
				vmc.setXIndex(data.attribute(ThresholdCurve.RECALL_NAME).index());
			if (data.attribute(ThresholdCurve.PRECISION_NAME) != null)
				vmc.setYIndex(data.attribute(ThresholdCurve.PRECISION_NAME).index());
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(
					null,
					"Error adding plot:\n" + e,
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}

		MekaFrame frame = new MekaFrame();
		frame.setTitle(getName());
		frame.setDefaultCloseOperation(MekaFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(vmc);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
