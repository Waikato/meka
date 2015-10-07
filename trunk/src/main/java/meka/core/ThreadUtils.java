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
 * ThreadUtils.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.core;

/**
 * Thread and multi-process related methods.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ThreadUtils {

	/** whether to use all available cores. */
	public final static int ALL = -1;

	/** the number of threads used to be considered sequential. */
	public final static int SEQUENTIAL = 1;

	/**
	 * Returns the available number of processors on the machine.
	 *
	 * @return                  the number of processors
	 */
	public static int getAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * Calculates the number of threads to use.
	 *
	 * @param numThreads        the requested number of threads (-1 for # of cores/cpus)
	 * @param maxThreads        the maximum to ask for
	 * @return                  the actual number of threads to use, (1 = single thread)
	 */
	public static int getActualNumThreads(int numThreads, int maxThreads) {
		int result;

		if (numThreads == ALL)
			result = getAvailableProcessors();
		else if (numThreads > SEQUENTIAL)
			result = Math.min(numThreads, maxThreads);
		else
			result = SEQUENTIAL;
		if (result > getAvailableProcessors())
			result = getAvailableProcessors();

		return result;
	}

	/**
	 * Returns whether the number of threads represent a multi-threaded setup.
	 *
	 * @param numThreads        the number of threads
	 * @return                  true if multi-threaded
	 */
	public static boolean isMultiThreaded(int numThreads) {
		return (ThreadUtils.getActualNumThreads(numThreads, ThreadUtils.getAvailableProcessors()) != ThreadUtils.SEQUENTIAL);
	}
}
