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
 * RecentItemListener.java
 * Copyright (C) 2013-2015 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.events;

import meka.gui.core.AbstractRecentItemsHandler;

/**
 * Interface for classes that listen to events of {@link AbstractRecentItemsHandler}.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 * @see AbstractRecentItemsHandler
 * @param <M> the type of menu
 * @param <T> the type of item
 */
public interface RecentItemListener<M,T> {

  /**
   * Gets called in case a recent item got added.
   * 
   * @param e		the event
   */
  public void recentItemAdded(RecentItemEvent<M, T> e);

  /**
   * Gets called in case a recent item got selected.
   * 
   * @param e		the event
   */
  public void recentItemSelected(RecentItemEvent<M, T> e);
}
