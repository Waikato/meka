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

	/**
	 * Get k subset - return a set of k label indices (of L possible labels).
	 */
	public static int[] get_k_subset(int L, int k, Random r) {
		int indices[] = A.make_sequence(L);
		A.shuffle(indices, r);
		int part[] = Arrays.copyOf(indices,k);
		Arrays.sort(part);
		return part;
	}

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
		int partition[][] = convertListArrayTo2DArray(selection);

		for(int part[] : partition) {
			Arrays.sort(part);
		}

		return partition;
	}

	// returns SORTED partition
	public static int[][] generatePartition(int indices[], int num, Random r, boolean balanced) {
		if (!balanced) 
			return generatePartition(indices,num,r);

		int L = indices.length;

		// shuffle indices
		MLUtils.randomize(indices,r);

		int partition[][] = new int[num][];
		int k = L / num;
		int e = L % num;
		int m = 0;
		for(int c = 0; c < num; c++) {
			if (c < e) {
				partition[c] = Arrays.copyOfRange(indices,m,m+k+1);
				m = m + k + 1;
			}
			else {
				partition[c] = Arrays.copyOfRange(indices,m,Math.min(L,m+k));
				m = m + k;
			}
			Arrays.sort(partition[c]);
		}

		return partition;
	}

	/** 
	 * Get Partition From Dataset Hierarchy - assumes attributes are hierarchically arranged with '.'. 
	 * For example europe.spain indicates leafnode spain of branch europe.
	 * @param	D		Dataset
	 * @return	partition
	 */
	public static final int[][] getPartitionFromDatasetHierarchy(Instances D) {
		HashMap<String,LabelSet> map = new HashMap<String,LabelSet>();
		int L = D.classIndex();
		for(int j = 0; j < L; j++) {
			String s = D.attribute(j).name().split("\\.")[0];
			LabelSet Y = map.get(s);
			if (Y==null)
				Y = new LabelSet(new int[]{j});
			else {
				Y.indices = A.append(Y.indices,j);
				Arrays.sort(Y.indices);
			}
			map.put(s, Y);
		}
		int partition[][] = new int[map.size()][];
		int i = 0;
		for(LabelSet part : map.values()) { 
			//System.out.println(""+i+": "+Arrays.toString(part.indices));
			partition[i++] = part.indices;
		}
		return partition;
	}


	/**
	 * Rating - Return a score for the super-class 'partition' using the pairwise info in 'M'.
	 * +1 if two co-ocurring labels are in different partitions. 
	 * -1 if two co-ocurring labels are in different partitions. 
	 * @param	partition	super-class partition, e.g., [[0,3],[2],[1,4]]
	 * @param	countMap	each LabelSet and its count
	public static double scorePartition(int partition[][], HashMap<LabelSet,Integer> countMap) {
		return 0.0;
	}
	public static double scorePartition(int partition[][], double M[][]) {
		return 0.0;
	}
	*/

	// @TODO try and do without this in the future.
	private static final int[][] convertListArrayTo2DArray(ArrayList<Integer> listArray[]) {
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

	/**
	 * Make Partition Dataset - out of dataset D, on indices part[].
	 * @param	D		regular multi-label dataset (of L = classIndex() labels)
	 * @param	part	list of indices we want to make into an LP dataset.
	 * @return Dataset with 1 multi-valued class label, representing the combinations of part[].
	 */
	public static Instances makePartitionDataset(Instances D, int part[]) throws Exception {
		return makePartitionDataset(D,part,0,0);
	}

	/**
	 * Make Partition Dataset - out of dataset D, on indices part[].
	 * @param	D		regular multi-label dataset (of L = classIndex() labels)
	 * @param	part	list of indices we want to make into a PS dataset.
	 * @param	P		@see PSUtils.java
	 * @param	N		@see PSUtils.java
	 * @return Dataset with 1 multi-valued class label, representing the combinations of part[].
	 */
	public static Instances makePartitionDataset(Instances D, int part[], int P, int N) throws Exception {
		int L = D.classIndex();
		Instances D_ = new Instances(D);
		// strip out irrelevant attributes
		D_.setClassIndex(-1);
		D_ = F.keepLabels(D,L,part);
		D_.setClassIndex(part.length);
		// make LC transformation
		D_ = PSUtils.PSTransformation(D_,P,N);
		return D_;
	}
}
