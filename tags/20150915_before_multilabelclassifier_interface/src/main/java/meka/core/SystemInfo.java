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
 * SystemInfo.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.core;

import java.util.Hashtable;

/**
 * Gathers information about the system environment.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SystemInfo
  extends weka.core.SystemInfo {

  /**
   * returns a copy of the system info. the key is the name of the property and
   * the associated object is the value of the property (a string).
   */
  public Hashtable<String, String> getSystemInfo() {
	  Hashtable<String, String>     result;

	  result = super.getSystemInfo();
	  result.put("Meka home dir", Project.getHome().getAbsolutePath());

	  return result;
  }
}
