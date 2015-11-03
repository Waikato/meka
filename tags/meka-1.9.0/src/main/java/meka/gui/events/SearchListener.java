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
 * SearchListener.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.events;

/**
 * For classes that wait for search events sent by a SearchPanel.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4584 $
 */
public interface SearchListener {

  /**
   * Gets called whenever a search should be performed.
   * 
   * @param e		the event
   */
  public void searchInitiated(SearchEvent e);
}
