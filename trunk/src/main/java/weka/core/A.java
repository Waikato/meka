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

package weka.core;

import java.util.*;

/**
 * A.java - Handy Array operations
 */
public abstract class A {

	// join 'a' and 'b' together into 'c' [1,2],[3] -> [1,2,3]
	public static final int[] join(int a[], int b[]) {
		int c[] = new int[a.length+b.length];
		int i = 0;
		for(int j = 0; j < a.length; j++, i++) {
			c[i] = a[j];
		}
		for(int j = 0; j < b.length; j++, i++) {
			c[i] = b[j];
		}
		return c;
	}

	// reverse 'a' [1,2,3] -> [3,2,1]
	public static final int[] reverse(int a[]) {
		int c[] = new int[a.length];
		for(int i = a.length-1, j = 0; i >=0 ; i--, j++) {
			c[j] = a[i];
		}
		return c;
	}

	// sort 'a' [1,3,2] -> [1,2,3]
	public static final int[] sort(int a[]) {
		int c[] = Arrays.copyOf(a,a.length);
		Utils.sort(c); // @todo: Arrays.sort ?
		return c;
	}

	// nice string representation for printout
	public static String toString(double v[]) {
		StringBuilder sb = new StringBuilder("[");  
		for(int k = 0; k < v.length; k++) {
			sb.append(Utils.doubleToString(v[k],4,2)+" ");
		}
		sb.append("]");
		return sb.toString();
	}

	// product
	public static final double product(double v[]) {
		double p = 1.0;
		for(double d : v) {
			p *= d;
		}
		return p;
	}

	// sum
	public static double sum(double v[]) {
		double p = 0.0;
		for(double d : v) {
			p += d;
		}
		return p;
	}

	// add value 'v' to 'array[]' @todo: rename to 'append'
	public static int[] add(int array[], final int v) {
		int n = array.length;
		array = Arrays.copyOf(array,n+1);
		array[n] = v;
		return array;
	}
	public static int[] append(int array[], final int v) {
		return add(array,v);
	}

	// delete index 'i' from 'array[]'
	public static int[] delete(int array[], final int i) {
		int n = array.length;
		array[i] = array[n-1];
		array = Arrays.copyOf(array,n-1);
		return array;
	}

	// delete indices i[] from 'array[]'
	public static int[] delete(int array[], final int i[]) {
		// @note: slow!
		for (int i_ : i) {
			array = delete(array,i_);
		}
		return array;
	}

	/*
	// add values 'v[]' to 'array[]'
	public static int[] add(int array[], final int v[]) {
		int n = array.length;
		array = Arrays.copyOf(array,n+1);
		array[n] = v;
		return array;
	}
	*/

	public static final int[] make_seq(int start, int end) {
		int array[] = new int[end-start];
		for(int j = start, i = 0; j < end; j++, i++) {
			array[i] = j;
		}
		return array;
	}

	// select the elements 'indices[]' from 'array[]'
	public static int[] select(int array[], final int indices[]) {
		int selection[] = new int[indices.length];
		for(int i = 0; i < indices.length; i++) {
			selection[i] = array[indices[i]];
		}
		return selection;
	}

	// swap the 'j'th and 'k'th indices in 'array[]'
	public static int[] swap(int array[], int j, int k) {
		int temp = array[j];
		array[j] = array[k];
		array[k] = temp;
		return array;
	}

	// randomly swap two elements in 'array[]'
	public static int[] swap(int array[], Random r) {
		int a = r.nextInt(array.length);
		int b = r.nextInt(array.length-1);
		return swap(array,a,(a==b) ? array.length-1 : b);
	}

	// "12" -> [1,2]
	public static final int[] string2IntArray(String s) {
		int[] array = new int[s.length()];
		 
		for (int i = 0; i < s.length(); i++) {
			array[i] = Character.digit(s.charAt(i), 10);
		}

		return array;
	}

	// select i with probabilitiy w[i] (w must be normalised)
	public static int rndsrc (double w[], Random r) {
		double u = r.nextDouble();
		double sum = w[0];
		int i = 0;
		while (sum < u) {
			i++;
			sum+=w[i];
		}
		return i;
	}

	// same as above
	public static int samplePMF(double w[], Random r) {
		return rndsrc(w,r);
	}

}

