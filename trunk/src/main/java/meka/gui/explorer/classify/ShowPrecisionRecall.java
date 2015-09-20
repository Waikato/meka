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
 * ShowPrecisionRecall.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.explorer.classify;

import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;
import weka.core.Utils;
import weka.gui.visualize.ThresholdVisualizePanel;

/**
 * Allows the user to display the precision recall curves per label.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ShowPrecisionRecall
		extends AbstractShowThresholdCurve {
	private static final long serialVersionUID = -1152575716154907544L;

	/**
	 * Returns the name of the plugin. Used for the menu item text.
	 *
	 * @return          the name
	 */
	@Override
	public String getName() {
		return "Show Precision-Recall";
	}

	/**
	 * Returns the name of the default X column to display.
	 *
	 * @return              the name of the column
	 */
	protected String getDefaultXColumn() {
		return ThresholdCurve.RECALL_NAME;
	}

	/**
	 * Returns the name of the default Y column to display.
	 *
	 * @return              the name of the column
	 */
	protected String getDefaultYColumn() {
		return ThresholdCurve.PRECISION_NAME;
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
		ThresholdVisualizePanel result = super.createPanel(data, title);
		result.setROCString("PRC area: " + Utils.doubleToString(ThresholdCurve.getPRCArea(data), 3));
		result.setUpComboBoxes(result.getInstances());
		setComboBoxIndices(data, result);
		return result;
	}
}
