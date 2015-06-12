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

package meka.core;

import java.util.Comparator;
import meka.core.A;
import java.util.HashMap;
import java.util.Arrays;

/**
 * LabelVector - Multi-target compatible vector. 
 * e.g. [0,3,2,1] for L=4.
 * @author 	Jesse Read 
 * @version	March 2014
 */
public class LabelVector extends LabelSet {

	/** for serialization. */
	private static final long serialVersionUID = -6089833712552497991L;

	public int values[];  // values
	// public int confidences[] 

	//HashMap<String,Integer> c = null;

	public LabelVector(int values[]) {
		this.values = values;
	} 

	/*
	public double getValue(int i) {
		return values[i];
	}
	*/

	/*
	public LabelVector(HashMap<String,Integer> c) {
		this.c = c;
	} 
	*/

	@Override
	public boolean equals(Object o) {

		int v2[] = ((LabelVector) o).values;

		if (values.length != v2.length) {
			System.err.println("[Error] different sized vectors!");
			return false;
		}
		else {
			for(int i = 0; i < values.length; i++) {
				if (values[i] != v2[i]) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * multi-label suitable only
	 */
	public int compare(Object obj1, Object obj2) {

		int v1[] = ((LabelVector) obj1).values;
		int v2[] = ((LabelVector) obj2).values;

		if (A.sum(v1) > A.sum(v2))  {
			return -1;
		}
		else if (A.sum(v1) < A.sum(v2))  {
			return 1;
		}
		else {
			/*
			 * DEAL WITH WEIGHTS / COUNTS HERE
			if (c.get(s1) > c.get(s2)) {
				return -1;
			}
			if (c.get(s1) < c.get(s2)) {
				return 1;
			}
			else {
				// @todo: could add further conditions
				return 0;
			}
			*/
			return 0;
		} 
	} 

	public String toString() {
		return Arrays.toString(values);
	}
} 

