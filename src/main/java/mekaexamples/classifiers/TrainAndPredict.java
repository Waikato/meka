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
 * TrainAndPredict.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package mekaexamples.classifiers;

import meka.classifiers.multilabel.BR;
import meka.core.MLUtils;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Builds a BR Meka classifier on user supplied train dataset and outputs
 * predictions on a supplied dataset with missing class values.
 * <br>
 * Expected parameters: &lt;train&gt; &lt;predict&gt;
 * <br>
 * Note: The datasets must have been prepared for Meka already and compatible.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class TrainAndPredict {

  public static void main(String[] args) throws Exception {
    if (args.length != 2)
      throw new IllegalArgumentException("Required arguments: <train> <predict>");

    System.out.println("Loading train: " + args[0]);
    Instances train = DataSource.read(args[0]);
    MLUtils.prepareData(train);

    System.out.println("Loading predict: " + args[1]);
    Instances predict = DataSource.read(args[1]);
    MLUtils.prepareData(predict);

    // compatible?
    String msg = train.equalHeadersMsg(predict);
    if (msg != null)
      throw new IllegalStateException(msg);

    System.out.println("Build BR classifier on " + args[0]);
    BR classifier = new BR();
    // further configuration of classifier
    classifier.buildClassifier(train);

    System.out.println("Use BR classifier on " + args[1]);
    for (int i = 0; i < predict.numInstances(); i++) {
      double[] dist = classifier.distributionForInstance(predict.instance(i));
      System.out.println((i+1) + ": " + Utils.arrayToString(dist));
    }
  }
}
