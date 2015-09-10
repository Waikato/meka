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
 * VisualizeTab.java
 * Copyright (C) 2012-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import weka.gui.visualize.MatrixPanel;

import java.awt.*;

/**
 * For visualizing the data.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class VisualizeTab
  extends AbstractExplorerTab {

  /** for serialization. */
  private static final long serialVersionUID = 7200133227901982729L;

  /** the panel for visualizing the data. */
  protected MatrixPanel m_PanelMatrix;

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    super.initGUI();
    
    m_PanelMatrix = new MatrixPanel();
    add(m_PanelMatrix, BorderLayout.CENTER);
  }
  
  /**
   * Returns the title of the tab.
   * 
   * @return the title
   */
  @Override
  public String getTitle() {
    return "Visualize";
  }

  /**
   * Gets called when the data changed.
   */
  @Override
  protected void update() {
    if (hasData())
      m_PanelMatrix.setInstances(getData());
  }
}
