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
 * TransferableString.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;

/**
 * A container for strings. Used in drag'n'drop.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4584 $
 */
public class TransferableString
  implements Serializable, Transferable {

  /** for serialization. */
  private static final long serialVersionUID = -4291529156857201031L;

  /** the string to transfer. */
  protected String m_Data;

  /**
   * Initializes the container.
   *
   * @param data	the string to transfer
   */
  public TransferableString(String data) {
    super();

    m_Data = data;
  }

  /**
   * Returns an array of DataFlavor objects indicating the flavors the data
   * can be provided in.  The array should be ordered according to preference
   * for providing the data (from most richly descriptive to least descriptive).
   *
   * @return 		an array of data flavors in which this data can be transferred
   */
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[]{DataFlavor.stringFlavor};
  }

  /**
   * Returns whether or not the specified data flavor is supported for
   * this object.
   *
   * @param flavor 	the requested flavor for the data
   * @return 		boolean indicating whether or not the data flavor is supported
   */
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return (flavor.equals(DataFlavor.stringFlavor));
  }

  /**
   * Returns an object which represents the data to be transferred.  The class
   * of the object returned is defined by the representation class of the flavor.
   *
   * @param flavor 		the requested flavor for the data
   * @return			the transferred string
   * @throws IOException    if the data is no longer available
   *              		in the requested flavor.
   * @throws UnsupportedFlavorException    if the requested data flavor is
   *              				not supported.
   * @see DataFlavor#getRepresentationClass
   */
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor.equals(DataFlavor.stringFlavor))
      return m_Data;
    else
      throw new UnsupportedFlavorException(flavor);
  }

  /**
   * Returns the underlying data string.
   *
   * @return		the data string
   */
  public String getData() {
    return m_Data;
  }

  /**
   * Returns the underlying data string.
   *
   * @return		the data string
   * @see		#m_Data
   */
  public String toString() {
    return m_Data;
  }
}
