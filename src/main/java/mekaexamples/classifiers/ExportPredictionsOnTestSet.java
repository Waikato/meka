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
 * ExportPredictionsOnTestSet.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package mekaexamples.classifiers;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.Evaluation;
import meka.core.MLUtils;
import meka.core.Result;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Builds and evaluates a BR Meka classifier on user supplied train/test datasets
 * and outputs the predictions on the test to the specified file
 * (ARFF or CSV, auto-detect based on extension).
 * <br>
 * Expected parameters: &lt;train&gt; &lt;test&gt; &lt;test&gt; &lt;output&gt;
 * <br>
 * Note: The datasets must have been prepared for Meka already and compatible.
 *       The format of the output file is determined by its extension
 *       (eg .arff or .csv).
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExportPredictionsOnTestSet {

  public static void main(String[] args) throws Exception {
    if (args.length != 3)
      throw new IllegalArgumentException("Required arguments: <train> <test> <output>");

    System.out.println("Loading train: " + args[0]);
    Instances train = DataSource.read(args[0]);
    MLUtils.prepareData(train);

    System.out.println("Loading test: " + args[1]);
    Instances test = DataSource.read(args[1]);
    MLUtils.prepareData(test);

    // compatible?
    String msg = train.equalHeadersMsg(test);
    if (msg != null)
      throw new IllegalStateException(msg);

    System.out.println("Build BR classifier on " + args[0]);
    BR classifier = new BR();
    // further configuration of classifier
    classifier.buildClassifier(train);

    System.out.println("Evaluate BR classifier on " + args[1]);
    String top = "PCut1";
    String vop = "3";
    Result result = Evaluation.evaluateModel(classifier, train, test, top, vop);

    System.out.println(result);

    System.out.println("Saving predictions test set to " + args[2]);
    Instances performance = Result.getPredictionsAsInstances(result);
    DataSink.write(args[2], performance);
  }
}
