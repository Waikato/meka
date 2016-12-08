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
 * PrecisionRecall.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package mekaexamples.gui.classify;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.Evaluation;
import meka.core.MLUtils;
import meka.core.Result;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

/**
 * Cross-validates a BR Meka classifier on a dataset supplied by the user
 * and displays the precision recall curves per label.
 * <br>
 * Expected parameters: &lt;dataset&gt;
 * <br>
 * Note: The dataset must have been prepared for Meka already.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PrecisionRecall {

  public static final String CURVE_DATA = "Curve Data";

  /**
   * Creates a panel displaying the data.
   *
   * @param data          the plot data
   * @param title	  the title
   * @return              the panel
   * @throws Exception    if plot generation fails
   */
  protected static ThresholdVisualizePanel createPanel(Instances data, String title) throws Exception {
    ThresholdVisualizePanel result = new ThresholdVisualizePanel();
    PlotData2D plot = new PlotData2D(data);
    plot.setPlotName(title);
    plot.m_displayAllPoints = true;
    boolean[] connectPoints = new boolean [data.numInstances()];
    for (int cp = 1; cp < connectPoints.length; cp++)
      connectPoints[cp] = true;
    plot.setConnectPoints(connectPoints);
    result.addPlot(plot);
    result.setROCString("PRC area: " + Utils.doubleToString(ThresholdCurve.getPRCArea(data), 3));
    result.setUpComboBoxes(result.getInstances());
    if (data.attribute(ThresholdCurve.RECALL_NAME) != null)
      result.setXIndex(data.attribute(ThresholdCurve.RECALL_NAME).index());
    if (data.attribute(ThresholdCurve.PRECISION_NAME) != null)
      result.setYIndex(data.attribute(ThresholdCurve.PRECISION_NAME).index());
    return result;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1)
      throw new IllegalArgumentException("Required arguments: <dataset>");

    System.out.println("Loading data: " + args[0]);
    Instances data = DataSource.read(args[0]);
    MLUtils.prepareData(data);

    System.out.println("Cross-validate BR classifier");
    BR classifier = new BR();
    // further configuration of classifier
    String top = "PCut1";
    String vop = "3";
    Result result = Evaluation.cvModel(classifier, data, 10, top, vop);

    JFrame frame = new JFrame("Precision-recall");
    frame.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout());
    JTabbedPane tabbed = new JTabbedPane();
    frame.getContentPane().add(tabbed, BorderLayout.CENTER);
    Instances[] curves = (Instances[]) result.getMeasurement(CURVE_DATA);
    for (int i = 0; i < curves.length; i++) {
      try {
        ThresholdVisualizePanel panel = createPanel(curves[i], "Label " + i);
        tabbed.addTab("" + i, panel);
      }
      catch (Exception ex) {
        System.err.println("Failed to create plot for label " + i);
        ex.printStackTrace();
      }
    }
    frame.setSize(800, 600);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
