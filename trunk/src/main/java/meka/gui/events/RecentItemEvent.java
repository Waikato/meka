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
 * RecentItemEvent.java
 * Copyright (C) 2013-2015 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.events;

import java.util.EventObject;

import meka.gui.core.AbstractRecentItemsHandler;

/**
 * Event that gets sent when recent items change or get selected.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 * @param <M> the type of menu
 * @param <T> the type of item
 */
public class RecentItemEvent<M,T>
  extends EventObject {

  /** for serialization. */
  private static final long serialVersionUID = 4812680587917660625L;

  /** the affected item. */
  protected T m_Item;
  
  /**
   * Initializes the event.
   * 
   * @param source	the handler that triggered the event
   * @param file	the affected item
   */
  public RecentItemEvent(AbstractRecentItemsHandler<M,T> source, T file) {
    super(source);
    
    m_Item = file;
  }
  
  /**
   * Returns the handler that triggered the event.
   * 
   * @return		the handler
   */
  public AbstractRecentItemsHandler<M,T> getHandler() {
    return (AbstractRecentItemsHandler<M,T>) getSource();
  }
  
  /**
   * Returns the affected file.
   * 
   * @return		the file
   */
  public T getItem() {
    return m_Item;
  }
}
