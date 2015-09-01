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
 * MaximizationFixWindowListener.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Provides a work-around for platforms where the maximization of frames
 * (eg JFrame) results in strange behaviors. Linux is such a
 * platform, with popup menus (and the selectio) getting their coordinates
 * completely wrong.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MaximizationFixWindowListener
  extends WindowAdapter {

  /** the owning window. */
  protected Frame m_Owner;

  /** whether enabled or not. */
  protected boolean m_Enabled;

  /** the wait period in msec. */
  protected int m_Wait;

  /** whether to output some logging information. */
  protected boolean m_Logging;

  /** the current size/location. */
  protected Rectangle m_CurrentBounds;

  /** the unmaximized size/location. */
  protected Rectangle m_UnMaximizedBounds;

  /** whether to ignored the state change event. */
  protected boolean m_IgnoreStateChangeEvent;

  /**
   * Initializes the listener.
   *
   * @param owner	the owning frame
   * @param enabled	whether enabled
   * @param wait	the wait period in msec
   */
  public MaximizationFixWindowListener(Frame owner, boolean enabled, int wait) {
    this(owner, enabled, wait, false);
  }

  /**
   * Initializes the listener.
   *
   * @param owner	the owning frame
   * @param enabled	whether enabled
   * @param wait	the wait period in msec
   * @param logging	whether to output logging information
   */
  public MaximizationFixWindowListener(Frame owner, boolean enabled, int wait, boolean logging) {
    super();

    m_Owner                  = owner;
    m_Enabled                = enabled;
    m_Wait                   = wait;
    m_CurrentBounds          = null;
    m_UnMaximizedBounds      = null;
    m_IgnoreStateChangeEvent = false;
    m_Logging                = logging;
  }

  /**
   * Returns the owner.
   *
   * @return		the owning frame
   */
  public Frame getOwner() {
    return m_Owner;
  }

  /**
   * Returns whether the listener is enabled.
   *
   * @return		true if enabled
   */
  public boolean isEnabled() {
    return m_Enabled;
  }

  /**
   * Returns the wait period in msec.
   *
   * @return		the period in msec
   */
  public int getWait() {
    return m_Wait;
  }

  /**
   * Returns whether logging is enabled.
   *
   * @return		true if enabled
   */
  public boolean isLoggingEnabled() {
    return m_Logging;
  }

  /**
   * Returns whether change events are currently ignored.
   *
   * @return		true if ignored
   */
  public boolean getIgnoreStateChangeEvent() {
    return m_IgnoreStateChangeEvent;
  }

  /**
   * Logs the message to stdout.
   *
   * @param msg		the message to log
   */
  protected void log(String msg) {
    if (!isLoggingEnabled())
      return;

    System.out.println(msg);
  }

  /**
   * Updates the current bounds.
   *
   * @param x		the x of the owner
   * @param y		the y of the owner
   * @param width	the width of the owner
   * @param height	the height of the owner
   */
  public void updateBounds(int x, int y, int width, int height) {
    if (!getIgnoreStateChangeEvent())
      m_CurrentBounds = new Rectangle(x, y, width, height);
  }

  /**
   * Updates the current bounds.
   *
   * @param bounds	the current bounds of the owner
   */
  public void updateBounds(Rectangle bounds) {
    if (!getIgnoreStateChangeEvent())
      m_CurrentBounds = (Rectangle) bounds.clone();
  }

  @Override
  public void windowStateChanged(WindowEvent e) {
    int 	state;

    if (!isEnabled())
      return;

    state = e.getNewState();
    log("state: " + state);

    if (getIgnoreStateChangeEvent()) {
      log("ignored");
      return;
    }

    if (((state & Frame.MAXIMIZED_VERT) != 0)
      || ((state & Frame.MAXIMIZED_HORIZ) != 0)
      || ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH)) {
      if (m_UnMaximizedBounds == null) {
	m_IgnoreStateChangeEvent = true;
	log("max");
	final Rectangle fbounds = (m_CurrentBounds == null ? getOwner().getBounds() : m_CurrentBounds);
	final Rectangle sbounds = getOwner().getGraphicsConfiguration().getBounds();
	getOwner().setExtendedState(Frame.NORMAL);
	getOwner().setLocation(sbounds.x, sbounds.y);
	getOwner().setSize(sbounds.width, sbounds.height);
	SwingWorker worker = new SwingWorker() {
	  @Override
	  protected Object doInBackground() throws Exception {
	    try {
	      synchronized (this) {
		wait(getWait());
	      }
	    }
	    catch (Exception e) {
	      // ignored
	    }
	    m_UnMaximizedBounds      = fbounds;
	    m_IgnoreStateChangeEvent = false;
	    log("  bounds=" + m_UnMaximizedBounds);
	    return null;
	  }
	};
	worker.execute();
      }
      else {
	m_IgnoreStateChangeEvent = true;
	log("normal");
	SwingWorker worker = new SwingWorker() {
	  @Override
	  protected Object doInBackground() throws Exception {
	    getOwner().setExtendedState(Frame.MAXIMIZED_BOTH);
	    getOwner().setExtendedState(Frame.NORMAL);
	    try {
	      synchronized (this) {
		wait(getWait());
	      }
	    }
	    catch (Exception e) {
	      // ignored
	    }
	    log("  bounds=" + m_UnMaximizedBounds);
	    getOwner().setBounds(m_UnMaximizedBounds);
	    m_UnMaximizedBounds      = null;
	    m_IgnoreStateChangeEvent = false;
	    return null;
	  }
	};
	worker.execute();
      }
    }
  }
}
