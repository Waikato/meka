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
 * EvaluationStatisticsComparator.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import weka.core.Utils;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for statistics.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class EvaluationStatisticsComparator
  implements Comparator<EvaluationStatistics>, Serializable {

	private static final long serialVersionUID = 6043447420299753468L;

	/** the default keys. */
	public final static String[] DEFAULT_KEYS = new String[]{
			EvaluationStatistics.KEY_CLASSIFIER,
			EvaluationStatistics.KEY_RELATION};

	/** the keys to use for comparison. */
	protected String[] m_Keys;

	/**
	 * Initializes the comparator with the default keys.
	 *
	 * @see     #DEFAULT_KEYS
	 */
	public EvaluationStatisticsComparator() {
		this(DEFAULT_KEYS);
	}

	/**
	 * Initializes the comparator with the specified keys.
	 *
	 * @param keys          the keys to use
	 */
	public EvaluationStatisticsComparator(String[] keys) {
		super();
		m_Keys = keys;
	}

	/**
	 * Compares the two statistics.
	 *
	 * @param o1        the first stats
	 * @param o2        the second stats
	 * @return          less than zero if first smaller than second, zero if the same, greater than zero if first
	 *                  greater than second
	 */
	@Override
	public int compare(EvaluationStatistics o1, EvaluationStatistics o2) {
		int         result;
		Number      n1;
		Number      n2;

		result = 0;

		for (String key: m_Keys) {
			if (key.equals(EvaluationStatistics.KEY_CLASSIFIER)) {
				result = Utils.toCommandLine(o1.getClassifier()).compareTo(Utils.toCommandLine(o2.getClassifier()));
			}
			else if (key.equals(EvaluationStatistics.KEY_RELATION)) {
				result = o1.getRelation().compareTo(o2.getRelation());
			}
			else {
				n1 = o1.get(key);
				n2 = o2.get(key);
				if ((n1 == null) && (n2 == null))
					result = 0;
				else if (n1 == null)
					result = -1;
				else if (n2 == null)
					result = +1;
				else
					result = new Double(n1.doubleValue()).compareTo(n2.doubleValue());
			}

			if (result != 0)
				break;
		}

		return result;
	}

	/**
	 * Returns the keys as string.
	 *
	 * @return          the keys
	 */
	public String toString() {
		return Utils.arrayToString(m_Keys);
	}
}
