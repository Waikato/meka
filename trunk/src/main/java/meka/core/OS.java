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
 * OS.java
 * Copyright (C) 2011-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.core;

/**
 * Helper class for operating system related stuff.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class OS {

  /** whether the OS is Windows. */
  protected static Boolean m_IsWindows;

  /** whether the OS is Mac. */
  protected static Boolean m_IsMac;

  /** whether the OS is Linux. */
  protected static Boolean m_IsLinux;

  /** whether the OS is Android. */
  protected static Boolean m_IsAndroid;

  /**
   * Checks whether the operating system is Windows.
   *
   * @return		true if the OS is Windows flavor
   */
  public static synchronized boolean isWindows() {
    if (m_IsWindows == null)
      m_IsWindows = System.getProperty("os.name").toLowerCase().contains("windows");

    return m_IsWindows;
  }

  /**
   * Checks whether the operating system is Mac.
   *
   * @return		true if the OS is Mac flavor
   */
  public synchronized static boolean isMac() {
    if (m_IsMac == null)
      m_IsMac = System.getProperty("os.name").toLowerCase().startsWith("mac os");

    return m_IsMac;
  }

  /**
   * Checks whether the operating system is Linux (but not Android).
   *
   * @return		true if the OS is Linux flavor (but not Android)
   */
  public synchronized static boolean isLinux() {
    String	os;

    if (m_IsLinux == null)
      m_IsLinux = System.getProperty("os.name").toLowerCase().startsWith("linux") && !isAndroid();

    return m_IsLinux;
  }

  /**
   * Checks whether the operating system is Android.
   *
   * @return		true if the OS is Android flavor
   */
  public synchronized static boolean isAndroid() {
    if (m_IsAndroid == null) {
      m_IsAndroid = System.getProperty("java.vm.vendor").toLowerCase().contains("android")
        || System.getProperty("java.vendor").toLowerCase().contains("android")
        || System.getProperty("java.vendor.url").toLowerCase().contains("android");
    }

    return m_IsAndroid;
  }

  /**
   * Returns the "bitness", ie 32 or 64 bit of the underlying OS.
   *
   * @return		the number of bits
   */
  public synchronized static int getBitness() {
    String	arch;

    arch = System.getProperty("os.arch");
    if (arch.endsWith("86"))
      return 32;
    else if (arch.endsWith("64"))
      return 64;
    else
      throw new IllegalStateException("Cannot interpret 'os.arch' for bitness: " + arch);
  }
}
