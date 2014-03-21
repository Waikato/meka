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

import weka.core.Instances;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

public abstract class PSUtils {

	/**
	 * Cover - cover 'y' completely with sets from 'jmap'.
	 * @param	LabelSet	y		a labelset, e.g., [0,2,7]
	 * @param	HashMap		jmap	a map of labelsets to counts e.g., {[0,2,7]:39,...}
	 * @return	the sets to cover y (or just y, if it already covers itself).
	 */
	public static LabelSet[] cover(LabelSet y, HashMap<LabelSet,Integer> jmap) {

		Integer count = jmap.get(y);

		if (count != null && count >= 1) {

			return new LabelSet[]{y};
		}
		else {

			// Find some matches (i.e., subsets)

			ArrayList<LabelSet> S = new ArrayList<LabelSet>();
			for(LabelSet s : jmap.keySet()) {
				// is it a subset?
				int m = LabelSet.subset(s.indices,y.indices);
				if (m > 0) {
					// it is!
					S.add(s);
				}
			}

			// Use those matches to 'cover' y

			Collections.sort(S,y);
			Collections.reverse(S);

			LabelSet y_copy = y.deep_copy();
			List<LabelSet> K = new ArrayList<LabelSet>();
			// While we have more, and not covered, ...
			while (S.size() > 0 && y_copy.indices.length > 0) {
				LabelSet s_ = S.remove(0);
				K.add(s_);
				// add s_ to new 'keep' list
				y_copy.minus(s_);
				//System.out.println(""+y_copy);
			}

			return K.toArray(new LabelSet[0]);
		}

	}

	/**
	 * GetTopNSubsets - Don't cover all (like cover(y,map), rather only the top 'n')
	 * @see cover
	 * @param	LabelSet	y		a labelset, e.g., [0,2,7]
	 * @param	HashMap		jmap	a map of labelsets to counts e.g., {[0,2,7]:39,...}
	 * @param	int			n		the number of sets to take
	 * @return	the best LabelSets to use to decompose y into
	 */
	public static LabelSet[] getTopNSubsets(LabelSet y, HashMap<LabelSet,Integer> map, int n) {
		return getTopNSubsets(y,map,n,new LabelSetComparator(map));
	}

	/**
	 * GetTopNSubsets - Don't cover all (like cover(y,map), rather only the top 'n')
	 * @see 	cover
	 * @todo	use SortedList or something faster
	 * @param	LabelSet	y		a labelset, e.g., [0,2,7]
	 * @param	HashMap		jmap	a map of labelsets to counts e.g., {[0,2,7]:39,...}
	 * @param	int			n		the number of sets to take
	 * @param	Comparator	comp	the Comparator with which to judge LabelSets
	 * @return	the best LabelSets to use to decompose y into
	 */
	public static LabelSet[] getTopNSubsets(LabelSet y, HashMap<LabelSet,Integer> map, int n, Comparator comp) {

		Integer count = map.get(y);

		if (count != null && count >= 1) {
			// don't prune
			return new LabelSet[]{y};
		}

		ArrayList<LabelSet> subsets = new ArrayList<LabelSet>();  
		// add
		for(LabelSet s : map.keySet()) {
			if (s.subsetof(y) >= 0) {
				subsets.add(s);
			}
		}
		// rank
		Collections.sort(subsets,comp);
		LabelSet s[] = subsets.toArray(new LabelSet[subsets.size()]);

		return Arrays.copyOf(s,Math.min(n,s.length));
	}


	private static class LabelSetComparator extends LabelSet {

		HashMap<LabelSet,Integer> c = null;

		public LabelSetComparator(HashMap<LabelSet,Integer> c) {
			this.c = c;
		} 

		@Override
		public int compare(Object obj1, Object obj2) {

			LabelSet l1 = (LabelSet) obj1;
			LabelSet l2 = (LabelSet) obj2;

			if (l2.indices.length > l1.indices.length) {
				return -1;
			}
			else if (l2.indices.length < l1.indices.length) {
				return 1;
			}
			else {

				int c1 = this.c.get(l1);
				int c2 = this.c.get(l2);

				if (c1 > c2) {
					return -1;
				}
				if (c1 < c2) {
					return 1;
				}
				else {
					return 0;
				}
			} 
		} 

	}

	/**
	 * CountCombinationsSparseSubset - like CountCombinationsSparse, but only interested in 'indices[]' wrt 'D'.
	 * @param	D		dataset 
	 * @param	indices	indices we are interested in
	 * @return	a HashMap where a LabelSet representation of each label combination is associated with an Integer count, e.g., [3,7,14],3
	 */
	public static HashMap<LabelSet,Integer> countCombinationsSparseSubset(Instances D, int indices[]) {
		HashMap<LabelSet,Integer> map = new HashMap<LabelSet,Integer>();

		for(int i = 0; i < D.numInstances(); i++) {
			LabelSet m = new LabelSet(MLUtils.toSubIndicesSet(D.instance(i), indices)); 
			map.put(m, map.containsKey(m) ? map.get(m) + 1 : 1);
		}
		return map;
	}

	/**
	 * CountCombinationsSparse - return a mapping of each distinct label combination and its count.
	 * @param	D	dataset 
	 * @param	L	number of labels
	 * @return	a HashMap where a LabelSet representation of each label combination is associated with an Integer count, e.g., [3,7,14],3
	 */
	public static final HashMap<LabelSet,Integer> countCombinationsSparse(Instances D, int L) {
		HashMap<LabelSet,Integer> map = new HashMap<LabelSet,Integer>();  
		for (int i = 0; i < D.numInstances(); i++) {
			LabelSet y = new LabelSet(MLUtils.toSparseIntArray(D.instance(i),L));
			Integer c = map.get(y);
			map.put(y, c == null ? 1 : c+1);
		}
		return map;
	}

	/**
	 * Given N labelsets 'sparseY', use a count 'map' to 
	 */
	public static final LabelSet[] convert(LabelSet[] sparseY, HashMap<LabelSet,Integer> map) {
		return null;
	}


}

