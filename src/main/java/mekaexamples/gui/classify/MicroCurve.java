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
 * MicroCurve.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package mekaexamples.gui.classify;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.Evaluation;
import meka.core.MLUtils;
import meka.core.Result;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;
import weka.gui.visualize.VisualizePanel;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.BorderLayout;

/**
 * Cross-validates a BR Meka classifier on a dataset supplied by the user
 * and displays the macro curve.
 * <br>
 * Expected parameters: &lt;dataset&gt;
 * <br>
 * Note: The dataset must have been prepared for Meka already.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MicroCurve {

  public static final String CURVE_DATA_MICRO = "Micro Curve Data";
  public static final String SAMPLES = "Samples";
  public static final String ACCURACY = "Accuracy";

  /**
   * Creates a panel displaying the data.
   *
   * @param data          the plot data
   * @return              the panel
   * @throws Exception    if plot generation fails
   */
  protected static VisualizePanel createPanel(Instances data) throws Exception {
    VisualizePanel result = new ThresholdVisualizePanel();
    PlotData2D plot = new PlotData2D(data);
    plot.setPlotName("Micro-averaged Performance");
    plot.m_displayAllPoints = true;
    boolean[] connectPoints = new boolean [data.numInstances()];
    for (int cp = 1; cp < connectPoints.length; cp++)
      connectPoints[cp] = true;
    plot.setConnectPoints(connectPoints);
    result.addPlot(plot);
    if (data.attribute(SAMPLES) != null)
      result.setXIndex(data.attribute(SAMPLES).index());
    if (data.attribute(ACCURACY) != null)
      result.setYIndex(data.attribute(ACCURACY).index());
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

    JFrame frame = new JFrame("Micro curve");
    frame.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout());
    Instances performance = (Instances) result.getMeasurement(CURVE_DATA_MICRO);
    try {
      VisualizePanel panel = createPanel(performance);
      frame.getContentPane().add(panel, BorderLayout.CENTER);
    }
    catch (Exception ex) {
      System.err.println("Failed to create plot!");
      ex.printStackTrace();
    }
    frame.setSize(800, 600);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
