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

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import java.util.*;

/**
 * PSUtils.java - Handy Utils for working with Pruned Sets.
 * Essentially, we have a <code>P</code> parameter for pruning and an <code>N</code> parameter for reintroduction.
 * @author Jesse Read 
 * @version June 2014
 */
public abstract class PSUtils {

	/**
	 * Sum Counts - sum all the values in 'map'.
	 */
	public static int sumCounts(HashMap<LabelSet,Integer> map) {
		int c = 0;
		for(Integer c_ : map.values()) {
			c = c + c_;
		}
		return c;
	}

	/**
	 * Count Subsets - returns the number of times labelset 'ysub' exists as a subset in 'Y'.
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
	 * GetAllSubsets - Get all frequent subsets of 'y' according to 'map'.
	 * @param	y	a labelset, e.g., [0,2,7]
	 * @param	map	a map of labelsets to counts e.g., {[0,2]:39, [2,7]:5, [2,9]:24...}
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
	 * @param	y	a labelset, e.g., [0,2,7]
	 * @param	map	a map of labelsets to counts e.g., {[0,2]:39, [2,7]:5, [2,9]:24...}
	 * @param	n	the number of sets to take
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

	/** used by convertDistribution(p,L) */
	@Deprecated
	private static final double[] toDoubleArray(String labelSet, int L) {

		int set[] = (labelSet.length() <= 2) ? new int[]{} : MLUtils.toIntArray(labelSet);
		//StringBuffer y = new StringBuffer(L);
		double y[] = new double[L];
		//for(int j = 0; j < L; j++) {
		//	y.append("0");
		//}
		for(int j : set) {
			//y.setCharAt(j,'1');
			y[j] = 1.;
		}
		return y;
		//return y.toString();
	}


	/**
	 * Convert Distribution - Given the posterior across combinations, return the distribution across labels.
	 * <br>
	 * TODO	Use recombination!!!
	 * @see		PSUtils#recombination(double[],int,LabelSet[])
	 * @param	p	the posterior of the super classes (combinations), e.g., P([1,3],[2]) = [1,0]
	 * @param	L 	the number of labels
	 * @return	the distribution across labels, e.g., P(1,2,3) = [1,0,1]
	 */
	@Deprecated
	public static double[] convertDistribution(double p[], int L, Instances iTemplate) {
		
		double y[] = new double[L];

		int i = Utils.maxIndex(p);

		double d[] = toDoubleArray(iTemplate.classAttribute().value(i),L);
		for(int j = 0; j < d.length; j++) {
			if(d[j] > 0.0)
				y[j] = 1.0;
		}

		return y;
	}

	/**
	 * Convert Distribution - Given the posterior across combinations, return the distribution across labels.
	 * @param	p			the posterior of the super classes (combinations), e.g., P([1,3],[2]) = [0.3,0.7]
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
	/**
	 * Convert Distribution - Given the posterior across combinations, return the distribution across labels.
	 * @param	p	the posterior of the super classes (combinations), e.g., P([1,3],[2]) = [1,0]
	 * @param	L 	the number of labels
	 * @return	the distribution across labels, e.g., P(1,2,3) = [1,0,1]
	 */
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
	/**
	 * Convert Distribution - Given the posterior across combinations, return the distribution across labels.
	 * @param	p	the posterior of the super classes (combinations), e.g., P([1,3],[2]) = [0.3,0.7]
	 * @param	L 	the number of labels
	 * @return	the distribution across labels, e.g., P(1,2,3) = [0.3,0.7,0.3]
	 */
	public static final double[] recombination_t(double p[], int L, Instances iTemplate) {

		double y[] = new double[L];

		for(int k = 0; k < p.length; k++) {                                                              
			String d_string = iTemplate.classAttribute().value(k);   // e.g. d_string = "[1,3,5]"
			int d[] = MLUtils.toIntArray(d_string); 		         // e.g.        d = [1,3,5]    p[k] = 0.5
			for(int j : d) {
				y[j] += p[k];                                                         // e.g., y[0] += d[0] * p[k] = 1 * 0.5 = 0.5
			}
		}
		return y;
	}
	public static final double[] recombination_t(double p[], int L, LabelSet map[]) {

		double y[] = new double[L];

		for(int k = 0; k < p.length; k++) {                                                              

			LabelSet y_meta = map[k];

			for(int j : y_meta.indices) {
				y[j] += p[k];
			}
		}
		return y;
	}

	/**
	 * Convert a multi-label instance into a multi-class instance, according to a template.
	 */
	public static Instance convertInstance(Instance x, int L, Instances template) {
		Instance x_ = (Instance) x.copy(); 
		x_.setDataset(null);
		for (int i = 0; i < L; i++)
			x_.deleteAttributeAt(0);
		x_.insertAttributeAt(0);
		x_.setDataset(template);
		return x_;
	}

	public static Instances LCTransformation(Instances D) {
		return LCTransformation(D,D.classIndex());

	}

	public static Instances LCTransformation(Instances D, int L) {
		return PSTransformation(D,L,"Class",0,0);
	}

	public static Instances PSTransformation(Instances D, int P, int N) {
		return PSTransformation(D,D.classIndex(),"Class",P,N);
	}

	public static Instances PSTransformation(Instances D, int L, int P, int N) {
		return PSTransformation(D,L,"Class",P,N);
	}

	/**
	 * Transform instances into a multi-class representation.
	 * @param D			original dataset
	 * @param L			number of labels in the original dataset
	 * @param cname		class name for the new dataset (may want to encode the list of indices here for RAkEL-like methods)
	 * @param p			pruning value
	 * @param n			restoration value
	 * @return transformed dataset
	 */
	public static Instances PSTransformation(Instances D, int L, String cname, int p, int n) {
		D = new Instances(D);

		// Gather combinations
		HashMap<LabelSet,Integer> distinctCombinations = PSUtils.countCombinationsSparse(D,L);

		// Prune combinations
		if (p > 0)
			MLUtils.pruneCountHashMap(distinctCombinations,p);

		// Check there are > 2
		if (distinctCombinations.size() <= 1 && p > 0) {
			// ... or try again if not ...
			System.err.println("[Warning] You did too much pruning, setting P = P-1");
			return PSTransformation(D,L,cname,p-1,n);
		}

		// Create class attribute
		ArrayList<String> ClassValues = new ArrayList<String>();
		for(LabelSet y : distinctCombinations.keySet()) 
			ClassValues.add(y.toString());
		Attribute C = new Attribute(cname, ClassValues);

		// Insert new special attribute (which has all possible combinations of labels) 
		D.insertAttributeAt(C,L);
		D.setClassIndex(L);

		//Add class values
		int N = D.numInstances();
		for (int i = 0; i < N; i++) {
			Instance x = D.instance(i);
			LabelSet y = new LabelSet(MLUtils.toSparseIntArray(x,L));
			String y_string = y.toString();

			// add it
			if(ClassValues.contains(y_string)) 	//if its class value exists
				x.setClassValue(y_string);
			// decomp
			else if(n > 0) { 
				//String d_subsets[] = getTopNSubsets(comb,distinctCombinations,n);
				LabelSet d_subsets[] = PSUtils.getTopNSubsets(y,distinctCombinations,n);
				//LabelSet d_subsets[] = PSUtils.cover(y,distinctCombinations);
				if (d_subsets.length > 0) {
					// fast
					x.setClassValue(d_subsets[0].toString());
					// additional
					if (d_subsets.length > 1) {
						for(int s_i = 1; s_i < d_subsets.length; s_i++) {
							Instance x_ = (Instance)(x).copy();
							x_.setClassValue(d_subsets[s_i].toString());
							D.add(x_);
						}
					}
				}
				else {
					x.setClassMissing();
				}
			}
		}

		// remove with missing class
		D.deleteWithMissingClass();

		try {
			D = F.removeLabels(D,L);
		} catch(Exception e) {
			// should never happen
		}
		D.setClassIndex(0);

		return D;
	}

	/*
	 * This method was used before tighter MOA integration (in Feb 2016). 
	 * This method could probably be elimitated if doing so does not cause any problems.
	public static Instance[] PSTransformation(Instance x, int L, HashMap<LabelSet,Integer> map, int n) {

		int y_[] = MLUtils.toSparseIntArray(x,L);
		if (y_.length <= 0)
			// there can be no transformation if there are no labels!
			return new Instance[0];

		LabelSet y = new LabelSet(y_);

		if (map.get(y) != null) {
			Instance x_subsets[] = new Instance[1];
			x_subsets[0] = convertInstance(x,L,x.dataset());
			x_subsets[0].setClassValue(y.toString());
			return x_subsets;
		}
		else {
			LabelSet d_subsets[] = PSUtils.getTopNSubsets(y,map,n);
			Instance x_subsets[] = new Instance[d_subsets.length];
			Instance x_template = convertInstance(x,L,x.dataset());
			for(int i = 1; i < d_subsets.length; i++) {
				x_subsets[i] = (Instance)(x_template).copy();
				x_subsets[i].setClassValue(d_subsets[i].toString());
			}
			return x_subsets;
		}
	}
	*/

	/**
	 * Transform one instance into multi-class representations (an array of possibly multiple single-label instances).
	 * @param x			instance
	 * @param L			number of labels in the instance
	 * @param map		a map of labelsets to their frequencies 
	 * @param n			restoration value
	 * @return transformed instances
	 */
	public static Instance[] PSTransformation(Instance x, int L, HashMap<LabelSet,Integer> map, int n, Instances iTemplate) {

		int y_[] = MLUtils.toSparseIntArray(x,L);

		if (y_.length <= 0)
			// There can be no transformation if there are no labels!
			return new Instance[0];

		LabelSet y = new LabelSet(y_);

		if (map.get(y) != null) {
			// The labelset already exists in the map (was observed in the training set)
			Instance x_subsets[] = new Instance[1];
			x_subsets[0] = convertInstance(x,L,iTemplate);
			x_subsets[0].setClassValue(y.toString());            // problem here!
			return x_subsets;
		}
		else {
			// The labelset has not been seen before, use thap to construct some instances that fit
			LabelSet d_subsets[] = PSUtils.getTopNSubsets(y,map,n);
			Instance x_subsets[] = new Instance[d_subsets.length];
			Instance x_template = convertInstance(x,L,iTemplate);
			for(int i = 1; i < d_subsets.length; i++) {
				x_subsets[i] = (Instance)(x_template).copy();
				x_subsets[i].setClassValue(d_subsets[i].toString());
			}
			return x_subsets;
		}
	}

	/**
	 * Transform instances into a multi-class representation.
	 * @param D			original dataset
	 * @param L			number of labels in that dataset
	 * @param cname		class name for the new dataset (may want to encode the list of indices here for RAkEL-like methods)
	 * @param p			pruning value
	 * @param n			restoration value
	 * @return transformed dataset
	 */
	public static Instances SLTransformation(Instances D, int L, String cname, int p, int n) {
		D = new Instances(D);

		// Gather combinations
		HashMap<LabelSet,Integer> distinctCombinations = PSUtils.countCombinationsSparse(D,L);

		// Prune combinations
		if (p > 0)
			MLUtils.pruneCountHashMap(distinctCombinations,p);

		// Check there are > 2
		if (distinctCombinations.size() <= 1 && p > 0) {
			// ... or try again if not ...
			System.err.println("[Warning] You did too much pruning, setting P = P-1");
			return PSTransformation(D,L,cname,p-1,n);
		}

		// Create class attribute
		ArrayList<String> ClassValues = new ArrayList<String>();
		for(LabelSet y : distinctCombinations.keySet())
			ClassValues.add(y.toString());
		Attribute C = new Attribute(cname, ClassValues);

		// Insert new special attribute (which has all possible combinations of labels)
		D.insertAttributeAt(C,L);
		D.setClassIndex(L);

		//Add class values
		int N = D.numInstances();
		for (int i = 0; i < N; i++) {
			Instance x = D.instance(i);
			LabelSet y = new LabelSet(MLUtils.toSparseIntArray(x,L));
			String y_string = y.toString();

			// add it
			if(ClassValues.contains(y_string)) 	//if its class value exists
				x.setClassValue(y_string);
				// decomp
			else if(n > 0) {
				//String d_subsets[] = getTopNSubsets(comb,distinctCombinations,n);
				LabelSet d_subsets[] = PSUtils.getTopNSubsets(y,distinctCombinations,n);
				//LabelSet d_subsets[] = PSUtils.cover(y,distinctCombinations);
				if (d_subsets.length > 0) {
					// fast
					x.setClassValue(d_subsets[0].toString());
					// additional
					if (d_subsets.length > 1) {
						for(int s_i = 1; s_i < d_subsets.length; s_i++) {
							Instance x_ = (Instance)(x).copy();
							x_.setClassValue(d_subsets[s_i].toString());
							D.add(x_);
						}
					}
				}
				else {
					x.setClassMissing();
				}
			}
		}

		// remove with missing class
		D.deleteWithMissingClass();

		try {
			D = F.removeLabels(D,L);
		} catch(Exception e) {
			// should never happen
		}
		D.setClassIndex(0);

		return D;
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

