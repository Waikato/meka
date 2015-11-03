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
 * ThreadLimiter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.core;

/**
 * Interface for classes that allow limiting the number of threads in use.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface ThreadLimiter {

  /**
   * Sets the number of threads to use.
   *
   * @param value 	the number of threads: -1 = # of CPUs/cores
   */
  public void setNumThreads(int value);

  /**
   * Returns the number of threads to use.
   *
   * @return 		the number of threads: -1 = # of CPUs/cores
   */
  public int getNumThreads();
}
