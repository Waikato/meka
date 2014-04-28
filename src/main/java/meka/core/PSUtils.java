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
import weka.core.Utils;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;
import java.util.Comparator;
import java.io.*; // for saving/reading

public abstract class PSUtils {

	/**
	 * Count Subsets - returns the number of times subset 'ysub' exists in 'map'.
	 */
	public static int sumCounts(HashMap<LabelSet,Integer> map) {
		int c = 0;
		for(Integer c_ : map.values()) {
			c = c + c_;
		}
		return c;
	}

	/**
	 * Count Subsets - returns the number of times subset 'ysub' exists in 'map'.
	 */
	public static int countSubsets(LabelSet ysub, Set<LabelSet> Y) {
		int c = 0;
		for(LabelSet s : Y) {
			if (ysub.subsetof(s) > 0)
				c++;
		}
		return c;
	}

	/**
	 * Get Subsets - get all subsets of 'y' in the set 'set'.
	 */
	public static Set<LabelSet> getSubsets(LabelSet y, Set<LabelSet> set) {
		Set<LabelSet> subsets = new HashSet<LabelSet>();
		for(LabelSet s : set) {
				// is it a subset?
				int m = LabelSet.subset(s.indices,y.indices);
				if (m > 0) {
					// it is!
					subsets.add(s);
				}
			}
		return subsets;
	}

	/**
	 * Get Sorted Subsets - get all subsets of 'y' in the set 'set'; sorted according to 'cmp'.
	 */
	public static SortedSet<LabelSet> getSortedSubsets(LabelSet y, Set<LabelSet> set, Comparator cmp) {
		SortedSet<LabelSet> subsets = new TreeSet<LabelSet>(cmp);
		for(LabelSet s : set) {
				// is it a subset?
				int m = LabelSet.subset(s.indices,y.indices);
				if (m > 0) {
					// it is!
					subsets.add(s);
				}
			}
		return subsets;
	}

	/**
	 * Get Sorted Subsets - get all subsets of 'y' in the set 'set'; sorted according to length, and counts in 'map'.
	 */
	public static SortedSet<LabelSet> getSortedSubsets(LabelSet y, HashMap<LabelSet,Integer> map) {
		return getSortedSubsets(y, map.keySet(), new LabelSetComparator(map));
	}
	

	/**
	 * Cover - cover 'y' completely (or as best as possible) with sets from 'map'.
	 * @param	y		a LabelSet, e.g., [0,2,7]
	 * @param	map		a map of LabelSets to counts e.g., {[0,2,7]:39,...}
	 * @return	the sets to cover y (or just y, if it already covers itself).
	 */
	public static LabelSet[] cover(LabelSet y, HashMap<LabelSet,Integer> map) {

		Integer count = map.get(y);

		if (count != null && count >= 1) {

			return new LabelSet[]{y};
		}
		else {

			// Find some matches (i.e., subsets)
			Comparator cmp = new LabelSetComparator(map);

			SortedSet<LabelSet> allS = getSortedSubsets(y, map.keySet(), cmp);

			Set<LabelSet> covS = cover(y, allS, cmp);

			return covS.toArray(new LabelSet[0]);
		}

	}

	public static Set<LabelSet> cover(LabelSet y, SortedSet<LabelSet> S, Comparator cmp) {

		LabelSet y_copy = y.deep_copy();
		Set<LabelSet> K = new HashSet<LabelSet>();
		// While we have more, and not covered, ...
		while (S.size() > 0 && y_copy.indices.length > 0) {
			//System.out.println("y = "+y_copy);
			//System.out.println("S = "+S);
			LabelSet s_ = S.last();
			//System.out.println("s_ = "+s_);
			K.add(s_);
			// add s_ to new 'keep' list
			y_copy.minus(s_);
			S = getSortedSubsets(y_copy, S, cmp);
			//System.out.println(""+y_copy);
		}

		return K;

	}

	/**
	 * GetAllSubsets - Get all frequent subsets of 'y' (according to 'map').
	 * @see cover
	 * @param	LabelSet	y		a labelset, e.g., [0,2,7]
	 * @param	HashMap		jmap	a map of labelsets to counts e.g., {[0,2]:39, [2,7]:5, [2,9]:24...}
	 * @return	the LabelSets to use to decompose y into, e.g., [[0,2],[2,7]]
	 */
	public static LabelSet[] getAllSubsets(LabelSet y, HashMap<LabelSet,Integer> map) {
		Integer count = map.get(y);

		if (count != null && count >= 1) {
			// don't prune
			return new LabelSet[]{y};
		}

		SortedSet<LabelSet> subsets = getSortedSubsets(y, map.keySet(), new LabelSetComparator(map));

		LabelSet s[] = subsets.toArray(new LabelSet[subsets.size()]);

		return s;
	}

	/**
	 * GetTopNSubsets - Don't cover all (like cover(y,map), rather only the top 'n')
	 * @see cover
	 * @param	LabelSet	y		a labelset, e.g., [0,2,7]
	 * @param	HashMap		jmap	a map of labelsets to counts e.g., {[0,2]:39, [2,7]:5, [2,9]:24...}
	 * @param	int			n		the number of sets to take
	 * @return	the LabelSets to use to decompose y into, e.g., [[0,2],[2,7]]
	 */
	public static LabelSet[] getTopNSubsets(LabelSet y, HashMap<LabelSet,Integer> map, int n) {

		LabelSet s[] = getAllSubsets(y,map);

		return Arrays.copyOfRange(s,Math.max(0,s.length-n),s.length);
	}

	public static SortedSet<LabelSet> getTopNSubsetsAsSet(LabelSet y, HashMap<LabelSet,Integer> map, int n) {

		SortedSet<LabelSet> allSets = getSortedSubsets(y, map);
		SortedSet<LabelSet> topSets = new TreeSet<LabelSet>();

		int n_ = 0;
		for(LabelSet Y : allSets) {
			topSets.add(Y);
			if (++n_ > n)
				break;
		}

		return topSets;
	}

	public static LabelSet getTopSubset(LabelSet y, HashMap<LabelSet,Integer> map) {
		return getTopNSubsets(y,map,1)[0];
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
	 * Convert Distribution - Given the posterior across combinations, return the distribution across labels.
	 * @param	p[]			the posterior of the super classes (combinations), e.g., P([1,3],[2]) = [0.3,0.7]
	 * @param	L 			the number of labels, e.g., L = 3
	 * @param	meta_labels	typical mapping, e.g., [13] to [1,3]
	 * @return	the distribution across labels, e.g., P(1,2,3) = [0.3,0.7,0.3]
	 */
	public static double[] convertDistribution(double p[], int L, LabelSet meta_labels[]) {
		double y[] = new double[L];
		for(int i = 0; i < p.length; i++) {
			LabelSet Y_i = meta_labels[i];			// e.g., [1,4]
			for(int j : Y_i.indices) {              //  j = 1, 4
				y[j] += p[i];                       // y[1] += p[i] = 0.5
			}
		}
		return y;
	}


	public static final LabelSet[] makeLabelSetMap(Instances T) {
		int L_ = 4;
		return new LabelSet[L_];
	}

	// @todo name convertDistribution ?
	public static final double[] recombination(double p[], int L, LabelSet map[]) {

		double y[] = new double[L];

		int i = Utils.maxIndex(p);

		LabelSet y_meta = map[i]; 

		for(int j : y_meta.indices) {
			y[j] = 1.0;
		}

		return y;
	}

	// @todo name convertDistribution ?
	public static final double[] recombination_t(double p[], int L, LabelSet map[]) {

		double y[] = new double[L];

		for(int i = 0; i < p.length; i++) {                                                              

			LabelSet y_meta = map[i];

			for(int j : y_meta.indices) {
				y[j] += p[i];
			}
		}
		return y;
	}

	/**
	 * Given N labelsets 'sparseY', use a count 'map' to 
	 */
	public static final LabelSet[] convert(LabelSet[] sparseY, HashMap<LabelSet,Integer> map) {
		return null;
	}

	/**
	 * SaveMap - Save the HashMap 'map' to the file 'filename'.
	 */
	public static final void saveMap(String filename, HashMap<LabelSet,Integer> map) throws Exception {
		MLUtils.saveObject(map,filename);
	}

	/**
	 * LoadMap - Load the HashMap stored in 'filename'.
	 */
	public static HashMap<LabelSet,Integer> loadMap(String filename) throws Exception {
		return (HashMap<LabelSet,Integer>) MLUtils.loadObject(filename);
	}

}

