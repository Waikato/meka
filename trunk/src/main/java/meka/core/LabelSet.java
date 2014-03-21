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
import java.util.HashMap;
import java.util.Arrays;
import meka.core.A;
import java.util.List;
import java.util.Set;
import java.io.Serializable;

/**
 * Comparator - A very fast / sparse labelset representation
 */
public class LabelSet implements Comparator, Serializable {

	/** for serialization. */
	private static final long serialVersionUID = -6089833712444497991L;

	public int indices[];  // indices of relevant labels, e.g., [1,5,7]
	//double w;		// a weight, e.g., a acount

	public LabelSet() {
	}

	public LabelSet(int indices[]) {
		// ASSUME THEY ARE SORTED
		this.indices = indices;
	} 

	public LabelSet(int indices[], boolean sort) {
		this.indices = indices;
		// SORT THEM
		if (sort == true)
			Arrays.sort(this.indices);
	}

	public LabelSet(List<Integer> list) {
		// ASSUME THEY ARE NOT SORTED
		// Set<Integer> = new HashSet<Integer>();
		Integer[] array = list.toArray(new Integer[0]);
		
		//Arrays.sort(list);
		//this.indices = new int[list.size()];
		//list.toArray(this.indices);
		this.indices = A.toPrimitive(array);
	}

	public LabelSet(Set<Integer> set) {
		Integer[] array = set.toArray(new Integer[0]);
		this.indices = A.toPrimitive(array);
	}

	/*
	public double getValue(int i) {
		int i_ = Arrays.binarySearch(indices,j);
		return (i_ < 0) 0.0 : values[j];
	}
	*/

	public final boolean contains(int j) {
		return Arrays.binarySearch(indices,j) < 0 ? false : true;
	}

	public final boolean contains(int js[]) {
		for (int j : js) {
			if (!contains(j))
				return false;
		}
		return true;
	}

	@Override
	public final int hashCode() {
		return Arrays.hashCode(indices);
	}

	@Override
	public boolean equals(Object o) {
		LabelSet l2 = (LabelSet)o;
		if (indices.length != l2.indices.length)
			return false;
		else {
			for(int i = 0; i < indices.length; i++) {
				if (indices[i] != l2.indices[i]) {
					return false;
				}
			}
		}
		return true;
	}

	// @todo, return based on map, if we have access to one, else just length
	@Override
	public int compare(Object o1, Object o2) {

			LabelSet l1 = (LabelSet) o1;
			LabelSet l2 = (LabelSet) o2;

			if (l2.indices.length > l1.indices.length) {
				return -1;
			}
			else if (l2.indices.length < l1.indices.length) {
				return 1;
			}
			else {
				/*
				if (l1.w > l2.w) {
					return -1;
				}
				if (l1.w < l2.w) {
					return 1;
				}
				*/
				//else {
					return 0;
				//}
			} 
	} 

	public final int subsetof(LabelSet y) {
		return subset(this.indices,y.indices);
	}

	/**
	 * Subset - returns > 0 if y1 \subsetof y2
	 */
	public final static int subset(int y1[], int y2[]) {

		//System.out.println(""+Arrays.toString(y1) + " subsetof " + Arrays.toString(y2));

		int j = 0;
		int k = 0;
		while (j < y1.length) {

			if (k >= y2.length)
				return -1;

			if (y1[j] == y2[k]) {
				j++;
				k++;
			}
			else if (y1[j] > y2[k]) {
				k++;
			}
			else {
				return -1;
			}
		}

		return j;

	}

              //j       k
	//  j                                  k          
	//[275226, 338304] \ [99203, 115256]]]

	public void minus(LabelSet l2) {
		this.indices = minus(this.indices,l2.indices);
	}

	/**
	 * Minus - [3 4 7 9] \ [3 7] = [4 9].
	 */
	public static int[] minus(int y1[], int y2[]) {

		int keep[] = new int[Math.max(y1.length,y2.length)];
		int i = 0, j = 0, k = 0;

		while (j < y1.length && k < y2.length) {
			if (y1[j] == y2[k]) {
				j++;
				k++;
			}
			else if (y1[j] < y2[k]) {
				keep[i++] = y1[j];
				j++;
			}
			else {
				//keep[i++] = y1[j];
				k++;
			}
		}
		while (j < y1.length) {
			//System.out.println("");
			//System.out.println("keep["+i+"] = y1["+j+"]");
			keep[i++] = y1[j++];
		}

		return Arrays.copyOf(keep,i);
	}

	public LabelSet deep_copy() {
		return new LabelSet(Arrays.copyOf(this.indices,this.indices.length));
	}

	public String toString() {
		return Arrays.toString(indices);
	}
}

