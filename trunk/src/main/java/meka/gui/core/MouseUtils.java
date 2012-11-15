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
 * MouseUtils.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import java.awt.event.MouseEvent;

/**
 * Helper class for mouse events.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MouseUtils {

  /**
   * Checks whether the mouse event is a left-click event.
   * Ctrl/Alt/Shift are allowed.
   *
   * @param e		the event
   * @return		true if a left-click event
   */
  public static boolean isLeftClick(MouseEvent e) {
    return ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 1));
  }

  /**
   * Checks whether the mouse event is a double-click event (with the left
   * mouse button).
   * Ctrl/Alt/Shift are allowed.
   *
   * @param e		the event
   * @return		true if a double-click event
   */
  public static boolean isDoubleClick(MouseEvent e) {
    return ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2));
  }

  /**
   * Checks whether the mouse event is a middle/wheel-click event.
   * Ctrl/Alt/Shift are allowed.
   *
   * @param e		the event
   * @return		true if a middle/wheel-click event
   */
  public static boolean isMiddleClick(MouseEvent e) {
    return ((e.getButton() == MouseEvent.BUTTON2) && (e.getClickCount() == 1));
  }

  /**
   * Checks whether the mouse event is a right-click event.
   * Alt+Left-Click is also interpreted as right-click.
   *
   * @param e		the event
   * @return		true if a right-click event
   */
  public static boolean isRightClick(MouseEvent e) {
    boolean	result;

    result = false;

    if ((e.getButton() == MouseEvent.BUTTON3) && (e.getClickCount() == 1))
      result = true;
    else if ((e.getButton() == MouseEvent.BUTTON1) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown())
      result = true;

    return result;
  }

  /**
   * Checks whether no modified key is pressed.
   *
   * @param e		the event
   * @return		true if no modifier key pressed
   */
  public static boolean hasNoModifierKey(MouseEvent e) {
    return (!e.isAltDown() && !e.isShiftDown() && !e.isControlDown());
  }
}
