package meka.classifiers.multilabel.cc;

import meka.core.A;
import meka.core.StatUtils;
import weka.core.Instances;
import java.util.Arrays;
import java.io.Serializable;

/**
 * CTrellis. Classifier Trellis Structure.
 * @author	Jesse Read
 * @version Feb 2014
 */

public class Trellis implements Serializable {

/*
 * Make the Trellis.
 * @TODO use this format:
 *   1 2 3 4 5 6 (with 2)
 * 1   w w
 * 2       w
 * 3       w w
 * 4         w
 * 5         w w
 * 6
 * where w > 0 for connection
 * CURRENTLY:
 * 1:
 * 2:1
 * 3:1 2
 * 4:2 3
 */

	public int trellis[][];
	public int indices[];
	public int WIDTH;
	public int TYPE;
	public int L = -1;

	public Trellis(int indicies[], int WIDTH, int conectivity) {
		this.indices = indicies;
		this.L = indices.length;
		this.WIDTH = WIDTH;
		this.TYPE = conectivity;
		this.make();
	}

	public Trellis(int indicies[], int trellis[][], int WIDTH, int conectivity) {
		this.indices = indicies;
		this.trellis = trellis;
		this.WIDTH = WIDTH;
		this.TYPE = conectivity;
		this.L = indices.length;
	}

	public String toString() {
		/*
		StringBuilder sb = new StringBuilder();  
		for(int jv : this.indices) {
			sb.append("P(y_"+jv+" | y_"+Arrays.toString(this.trellis[jv])+")\n");
		}
		return sb.toString();
		*/
		StringBuilder sb = new StringBuilder();  
		int counter = 0;
		for(int jv : this.indices) {
				counter++;
				String num = String.format("%3d", jv);
				sb.append(num);
				if (counter % WIDTH == 0)
					sb.append("\n");
			}
			sb.append("\n");
		return sb.toString();
	}

	// swap indicies j and k, return the difference of the weight of the neighbourhoods
	public void swap(int j, int k) {
		// swap indices
		this.indices = A.swap(this.indices,j,k);
		// fix parents
		int jv = this.indices[j];
		int kv = this.indices[k];
		this.trellis[jv] = getParents(j, indices, WIDTH, TYPE);
		this.trellis[kv] = getParents(k, indices, WIDTH, TYPE);
	}

	// try and put these two nodes together in the trellis
	public void putTogether(int j, int k) {
		int jv = this.indices[j];
		int kv = this.indices[k];
		if (jv + 1 % WIDTH != 0) {      // if not at the edge
			swap(j + 1,k);
		}
		else if (kv + 1 % WIDTH != 0) { // if not at the edge
			swap(j, k + 1);
		}
		else {
			System.out.println("DO SOMETHING ELSE");
		}
	}

	// return neigborhood of j in trellis
	private int[] ne(int j) {
		return new int[]{};
	}

	public double weight(Instances D) {
		double sum = 0.0;
		for(int jv : this.indices) {
			for(int pa : this.trellis[jv]) {
				sum += StatUtils.I(D,pa,jv);
			}
		}
		return sum;
	}

	public double weightNeighbourhood(int j) {
		double w = 0.0;
		for(int n : ne(j)) {
			//w += StatUtils.I(D,n,j);
		}
		return w;
	}

	/**
	 * What would the 'score' be, putting j_ at position j, in indices, with I matrix.
	 */
	public double weight(int indices[], int j, int j_, double I[][]) {
		int pa_j[] = getParents(j,indices,WIDTH,TYPE);
		double weight = 0.0;
		for (int pj : pa_j) {
			weight += I[pj][j_];
		}
		return weight;
	}

	private final int COL_INDEX(int j) {
		return j % this.WIDTH;
	}

	private final int ROW_INDEX(int j) {
		return j / this.WIDTH;
	}

	/**
	 * Get the neighbouring variables of a given index.
	 * For example,
	 *   4  1
	 *   2  5
	 *   3  0
	 *   getNeighbours(3) = [2,1,0]          // if TYPE = 1
	 *   getNeighbours(3) = [4,2,1,0,3]      // if TYPE = 2
	 *   getNeighbours(3) = []               // if TYPE = 0
	 * @param	j an index
	 * @return	neighbouring variables of index k
	 * NOTE: takes an index, returns variables -- A BIT STRANGE -- should return indices also
	 */
	public int[] getNeighbours(int j) {
		//int jv = this.indices[j];	// variable
		int ne_j[] = new int[]{};
		if (this.TYPE > 0) {
			// immediate neighbours
			if (j >= 1 && j % this.WIDTH > 0) 					// add prev
				ne_j = A.append(ne_j,indices[j-1]);
			if (j >= this.WIDTH) 								// add above
				ne_j = A.append(ne_j,indices[j-this.WIDTH]);
			if (j < (indices.length-1) && (j+1) % this.WIDTH > 0)// add next
				ne_j = A.append(ne_j,indices[j+1]);
			if (j < (indices.length-this.WIDTH)) 				// add below
				ne_j = A.append(ne_j,indices[j+this.WIDTH]);
		}
		if (this.TYPE > 1) {
			// diagonal
			if (j >= 1 && j >= WIDTH & j % this.WIDTH > 0) 				// add up left 
				ne_j = A.append(ne_j,indices[j-1-this.WIDTH]);
			if (COL_INDEX(j) < (WIDTH-1) && j >= WIDTH) 				// add up right 
				ne_j = A.append(ne_j,indices[j-WIDTH+1]);
			if (j < (L-this.WIDTH) && COL_INDEX(j) > 0)					// add below left 
				ne_j = A.append(ne_j,indices[j+1]);
			if (COL_INDEX(j) < (this.WIDTH-1) && L - j > this.WIDTH)	// add below right 
				ne_j = A.append(ne_j,indices[j+this.WIDTH+1]);
		}
		return ne_j;
	}

	/**
	 * Get the parent variables of a given index.
	 * For example,
	 *   4  1
	 *   2  5
	 *   3  0
	 *   getParents(3) = [2,1]          // if CONNECTIVITY = 1
	 * @param	j an index
	 * @return	parent variables of index k
	 * NOTE: takes an index, returns variables -- A BIT STRANGE -- should return indices also
	 */
	private int[] getParents(int j, int indices[], int WIDTH, int CONNECTIVITY) {
		//int jv = indices[j];
		int pa_j[] = new int[]{};
		
		if (CONNECTIVITY > 0) {
			if (j >= 1 && j % WIDTH > 0) 					// add prev
				pa_j = A.append(pa_j,indices[j-1]);
			if (j >= WIDTH) 								// add above
				pa_j = A.append(pa_j,indices[j-WIDTH]);
		}
		if (CONNECTIVITY > 1) {
			if (j >= 1 && j >= WIDTH & j % WIDTH > 0)		// add diag
				pa_j = A.append(pa_j,indices[j-WIDTH-1]);
		}
		if (CONNECTIVITY > 2) {
			if (j % WIDTH >= 2) 					// add 2nd prev
				pa_j = A.append(pa_j,indices[j-2]);
			if (j >= (WIDTH*2)) 							// add 2nd above
				pa_j = A.append(pa_j,indices[j-(WIDTH*2)]);
		}
		return pa_j;
	}

	public void make() {
		trellis = new int[this.L][];

		for(int j = 0; j < L; j++) {
			int jv = indices[j];
			trellis[jv] = getParents(j, indices, WIDTH,TYPE);
		}
	}

}


