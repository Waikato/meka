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

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

/**
 * SuperLabelUtils.java - Handy Utils for working with Meta Labels.
 * @todo call this class SuperClassUtils? SuperLabelUtils? Partition? PartitionUtils?
 * @author Jesse Read 
 * @version March 2014
 */
public abstract class SuperLabelUtils {

	private static int[][] generatePartition(int num, double M[][], Random r) {
		int L = M.length;
		int indices[] = A.make_sequence(L);

		// shuffle indices
		MLUtils.randomize(indices,r);

		ArrayList Y_meta[] = new ArrayList[num];

		// we have a minimum of 'num' groups
		for(int i = 0; i < num; i++) {
			Y_meta[i] = new ArrayList<Integer>();
			Y_meta[i].add(indices[i]); 
		}

		// remaining
		for(int i = num; i < L; i++) {
			int idx = 0; //goesBestWith(i,Y_meta[i],M);
			Y_meta[idx].add(indices[i]);
		}

		return convertListArrayTo2DArray(Y_meta);
	}

	/**
	 * generatePartition - return [[0],...,[L-1]].
	 * @param	L	number of labels
	 * @return	[[0],...,[L-1]]
	 */
	public static int[][] generatePartition(int L) {
		int partition[][] = new int[L][];
		for(int j = 0; j < L; j++) {
			partition[j] = new int[]{j};
		}
		return partition;
	}

	/**
	 * generatePartition - .
	 * @param	indices		[1,2,..,L]
	 * @param	r			Random
	 * @return	partition
	 */
	public static int[][] generatePartition(int indices[], Random r) {
		int L = indices.length;
		return generatePartition(indices,r.nextInt(L)+1,r);
	}

	/** 
	 * generatePartition.
	 * @TODO can generate 'indices' inside, given L
	 * Get a random layout of 'num' sets of 'indices'.
	 * @param	indices		[1,2,...,L]
	 * @param	num			number of super-nodes to generate (between 1 and L)
	 * @param	r			Random
	 * @return	partition
	 */
	public static int[][] generatePartition(int indices[], int num, Random r) {

		int L = indices.length;

		// shuffle indices
		MLUtils.randomize(indices,r);

		// we have a minimum of 'num' groups
		ArrayList<Integer> selection[] = new ArrayList[num];
		for(int i = 0; i < num; i++) {
			selection[i] = new ArrayList<Integer>();
			selection[i].add(indices[i]); 
		}

		// remaining
		for(int i = num; i < L; i++) {
			int idx = r.nextInt(num);
			selection[idx].add(indices[i]);
		}

		// convert <int[]>List into an int[][] array
		return convertListArrayTo2DArray(selection);
	}

	public static double scorePartition(int partition[][], double M[][]) {
		return 0.0;
	}

	/**
	 * Rating - Return a score for the super-class 'partition' using the pairwise info in 'M'.
	 * +1 if two co-ocurring labels are in different partitions. 
	 * -1 if two co-ocurring labels are in different partitions. 
	 * @param	partition	super-class partition, e.g., [[0,3],[2],[1,4]]
	 * @param	countMap	each LabelSet and its count
	 */
	public static double scorePartition(int partition[][], HashMap<LabelSet,Integer> countMap) {
		return 0.0;
	}

	public static final int[][] convertListArrayTo2DArray(ArrayList<Integer> listArray[]) {
		int num_partitions = listArray.length;
		int array[][] = new int[num_partitions][];
		for(int i = 0; i < listArray.length; i++) {
			array[i] = A.toPrimitive(listArray[i]); 
		}
		return array;
	}

	/**
	 * ToString - A string representation for the super-class partition 'partition'.
	 */
	public static String toString(int partition[][]) {
		StringBuilder sb = new StringBuilder();  
		sb.append("{");
		for(int i = 0; i < partition.length; i++) {
			sb.append(" "+Arrays.toString(partition[i]));
		}
		sb.append(" }");
		return sb.toString();
	}

}
