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
 * PrepareClassAttributes.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package mekaexamples.filter;

import meka.filters.unsupervised.attribute.MekaClassAttributes;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;

import java.io.File;

/**
 * Prepares a dataset for use in Meka, if it isn't already prepared properly
 * (the relation name in an ARFF file used by Meka stores information on how
 * many attributes from the left are used as class attributes).
 * <br>
 * Expects the following parameters: &lt;input&gt; &lt;attribute_indices&gt; &lt;output&gt;
 * <br>
 * The "input" parameter points to a dataset that Meka can read (eg CSV or ARFF).
 * The "attribute_indices" parameter is a comma-separated list of 1-based indices
 * of the attributes to use as class attributes in Meka.
 * The "output" parameters is the filename where to store the generated output data (as ARFF).
 *
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PrepareClassAttributes {

  public static void main(String[] args) throws Exception {
    if (args.length != 3)
      throw new IllegalArgumentException("Required parameters: <input> <attribute_indices> <output>");

    System.out.println("Loading input data: " + args[0]);
    Instances input = DataSource.read(args[0]);

    System.out.println("Applying filter using indices: " + args[1]);
    MekaClassAttributes filter = new MekaClassAttributes();
    filter.setAttributeIndices(args[1]);
    filter.setInputFormat(input);
    Instances output = Filter.useFilter(input, filter);

    System.out.println("Saving filtered data to: " + args[2]);
    ArffSaver saver = new ArffSaver();
    saver.setFile(new File(args[2]));
    DataSink.write(saver, output);
  }
}
