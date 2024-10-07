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

/*
 * EvaluatePrequential.java
 * Copyright (C) 2024 University of Waikato, Hamilton, NZ
 */

package mekaexamples.classifiers;

import meka.classifiers.incremental.IncrementalEvaluation;
import meka.classifiers.multilabel.incremental.BRUpdateable;
import meka.core.MLUtils;
import meka.core.Result;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Evaluates a BRUpdateable Meka classifier prequentially on a dataset supplied by the user.
 * <br>
 * Expected parameters: &lt;dataset&gt;
 * <br>
 * Note: The dataset must have been prepared for Meka already.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class EvaluatePrequential {

  public static void main(String[] args) throws Exception {
    if (args.length != 1)
      throw new IllegalArgumentException("Required arguments: <dataset>");

    System.out.println("Loading data: " + args[0]);
    Instances data = DataSource.read(args[0]);
    MLUtils.prepareData(data);

    System.out.println("Build BRUpdateable classifier");
    BRUpdateable classifier = new BRUpdateable();
    // further configuration of classifier
    Result result = IncrementalEvaluation.evaluateModelPrequentialBasic(classifier, data, 20, 1.0,"PCut1","3");
    System.out.println(result);
  }
}
