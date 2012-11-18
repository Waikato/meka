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
 * PreprocessTab.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

/**
 * For preprocessing data.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PreprocessTab
  extends AbstractThreadedExplorerTab {

  /** for serialization. */
  private static final long serialVersionUID = -7524660995639441810L;

  /**
   * Initializes the tab.
   * 
   * @param owner the Explorer this tab belongs to
   */
  public PreprocessTab(Explorer owner) {
    super(owner);
  }

  /* (non-Javadoc)
   * @see meka.gui.explorer.AbstractThreadedExplorerTab#executionStarted()
   */
  @Override
  protected void executionStarted() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see meka.gui.explorer.AbstractThreadedExplorerTab#executionFinished(java.lang.Throwable)
   */
  @Override
  protected void executionFinished(Throwable t) {
    // TODO Auto-generated method stub

  }

  /**
   * Returns the title of the tab.
   * 
   * @return the title
   */
  @Override
  public String getTitle() {
    return "Preprocessing";
  }
}
