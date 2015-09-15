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
 * TransferableImage.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

/**
 * A container for images.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4584 $
 */
public class TransferableImage
  implements Serializable, Transferable {

  /** for serialization. */
  private static final long serialVersionUID = 7613537409206432362L;

  /** the image to transfer. */
  protected BufferedImage m_Data;

  /**
   * Initializes the container.
   *
   * @param data	the string to transfer
   */
  public TransferableImage(BufferedImage data) {
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
    return new DataFlavor[]{DataFlavor.imageFlavor};
  }

  /**
   * Returns whether or not the specified data flavor is supported for
   * this object.
   *
   * @param flavor 	the requested flavor for the data
   * @return 		boolean indicating whether or not the data flavor is supported
   */
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return (flavor.equals(DataFlavor.imageFlavor));
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
    if (flavor.equals(DataFlavor.imageFlavor))
      return m_Data;
    else
      throw new UnsupportedFlavorException(flavor);
  }

  /**
   * Returns the underlying image.
   *
   * @return		the image
   */
  public BufferedImage getData() {
    return m_Data;
  }

  /**
   * Returns a string representation of the underlying image.
   *
   * @return		the string representation
   */
  public String toString() {
    return m_Data.toString();
  }
}
