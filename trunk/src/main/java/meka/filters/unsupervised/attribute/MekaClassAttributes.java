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
 * MekaClassAttributes.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;

import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WekaException;
import weka.filters.SimpleStreamFilter;

/**
 * Reorders attributes for MEKA to use as class attributes.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 66 $
 */
public class MekaClassAttributes
  extends SimpleStreamFilter {

  /** for serialization. */
  private static final long serialVersionUID = 6733841542030233313L;

  /** the range of attributes to use as class attributes. */
  protected Range m_AttributeIndices = new Range("last");

  /** for reordering the attributes. */
  protected Reorder m_Reorder = new Reorder();
  
  /**
   * Returns a string describing this filter.
   *
   * @return 		a description of the filter suitable for
   * 			displaying in the explorer/experimenter gui
   */
  @Override
  public String globalInfo() {
    return 
	"Reorders attributes for MEKA. Attribute range defines the "
	+ "attributes to use as class attributes.";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration listOptions() {
    Vector newVector = new Vector();

    newVector.addElement(new Option(
              "\tSpecify list of columns to use as MEKA class attributes. 'first' and 'last' are valid\n"
	      +"\tindexes. (default: last)",
              "R", 1, "-R <index1,index2-index4,...>"));

    return newVector.elements();
  }

  /**
   * Parses a given list of options.
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  @Override
  public void setOptions(String[] options) throws Exception {
    String orderList = Utils.getOption('R', options);
    if (orderList.length() != 0)
      setAttributeIndices(orderList);
    else
      setAttributeIndices("last");
    
    if (getInputFormat() != null)
      setInputFormat(getInputFormat());
  }

  /**
   * Gets the current settings of the filter.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  @Override
  public String[] getOptions() {
    String[] options = new String [2];
    int current = 0;

    if (!getAttributeIndices().equals("")) {
      options[current++] = "-R"; 
      options[current++] = getAttributeIndices();
    }

    while (current < options.length) {
      options[current++] = "";
    }
    return options;
  }

  /**
   * Set which attributes are to be used as MEKA class attributes.
   *
   * @param value a string representing the list of attributes.  Since
   * the string will typically come from a user, attributes are indexed from
   * 1. <br>
   * eg: first-3,5,6-last<br>
   * Note: use this method before you call 
   * <code>setInputFormat(Instances)</code>, since the output format is
   * determined in that method.
   * @throws Exception if an invalid range list is supplied
   */
  public void setAttributeIndices(String value) throws Exception {
    // simple test
    if (value.replaceAll("[afilrst0-9\\-,]*", "").length() != 0)
      throw new IllegalArgumentException("Not a valid range string!");
    
    m_AttributeIndices.setRanges(value);
  }

  /**
   * Get the current range selection
   *
   * @return a string containing a comma separated list of ranges
   */
  public String getAttributeIndices() {
    return m_AttributeIndices.getRanges();
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String attributeIndicesTipText() {
    return "Specify range of attributes to use as MEKA class attributes."
      + " This is a comma separated list of attribute indices, with"
      + " \"first\" and \"last\" valid values. Specify an inclusive"
      + " range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }

  /** 
   * Returns the Capabilities of this filter.
   *
   * @return            the capabilities of this object
   * @see               Capabilities
   */
  @Override
  public Capabilities getCapabilities() {
    Capabilities	result;
    
    result = m_Reorder.getCapabilities();
    result.setOwner(this);
    
    return result;
  }

  /**
   * Determines the output format based on the input format and returns 
   * this. In case the output format cannot be returned immediately, i.e.,
   * hasImmediateOutputFormat() returns false, then this method will called
   * from batchFinished() after the call of preprocess(Instances), in which,
   * e.g., statistics for the actual processing step can be gathered.
   *
   * @param inputFormat     the input format to base the output format on
   * @return                the output format
   * @throws Exception      in case the determination goes wrong
   */
  @Override
  protected Instances determineOutputFormat(Instances inputFormat) throws Exception {
    int			i;
    int[]		indices;
    StringBuilder	order;
    Instances		output;
    
    m_AttributeIndices.setUpper(inputFormat.numAttributes() - 1);
    order   = new StringBuilder();
    indices = m_AttributeIndices.getSelection();
    if (indices.length == 0)
      throw new WekaException("No attributes defined as class attributes!");
    for (i = 0; i < indices.length; i++) {
      if (i > 0)
	order.append(",");
      order.append("" + (indices[i]+1));
    }
    for (i = 0; i < inputFormat.numAttributes(); i++) {
      if (m_AttributeIndices.isInRange(i))
	continue;
      order.append(",");
      order.append("" + (i+1));
    }
    m_Reorder.setAttributeIndices(order.toString());
    m_Reorder.setInputFormat(inputFormat);
    
    output = m_Reorder.getOutputFormat();
    output.setClassIndex(indices.length);
    output.setRelationName("-C " + indices.length);
    
    return output;
  }

  /**
   * processes the given instance (may change the provided instance) and
   * returns the modified version.
   *
   * @param instance    the instance to process
   * @return            the modified data
   * @throws Exception  in case the processing goes wrong
   */
  @Override
  protected Instance process(Instance instance) throws Exception {
    m_Reorder.input(instance);
    m_Reorder.batchFinished();
    return m_Reorder.output();
  }

  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 66 $");
  }

  /**
   * runs the filter with the given arguments.
   *
   * @param args      the commandline arguments
   */
  public static void main(String[] args) {
    runFilter(new MekaClassAttributes(), args);
  }
}
