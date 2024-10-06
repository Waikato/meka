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
 * TrainTestSplit.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package mekaexamples.classifiers;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.Evaluation;
import meka.core.MLUtils;
import meka.core.Result;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Builds and evaluates a BR Meka classifier on a train/test split dataset supplied by the user.
 * <br>
 * Expected parameters: &lt;dataset&gt; &lt;percentage&gt;
 * <br>
 * Note: The dataset must have been prepared for Meka already.
 * And the percentage must be between 0 and 100.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class TrainTestSplit {

  public static void main(String[] args) throws Exception {
    if (args.length != 2)
      throw new IllegalArgumentException("Required arguments: <dataset> <percentage>");

    System.out.println("Loading data: " + args[0]);
    Instances data = DataSource.read(args[0]);
    MLUtils.prepareData(data);

    double percentage = Double.parseDouble(args[1]);
    int trainSize = (int) (data.numInstances() * percentage / 100.0);
    Instances train = new Instances(data, 0, trainSize);
    Instances test = new Instances(data, trainSize, data.numInstances() - trainSize);

    BR classifier = new BR();
    // further configuration of classifier

    System.out.println("Build BR classifier on " + percentage + "%");
    System.out.println("Evaluate BR classifier on " + (100.0 - percentage) + "%");
    String top = "PCut1";
    String vop = "3";
    Result result = Evaluation.evaluateModel(classifier, train, test, top, vop);

    System.out.println(result);
  }
}
