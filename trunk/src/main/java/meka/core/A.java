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

import weka.core.Utils;

import java.util.*;

/**
 * A.java - Handy array operations
 * @version 	April 2014
 * @author 		Jesse Read 
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

	/** ToString - Return a double[] as a nice String. */
	public static String toString(double v[]) {
		return toString(v,2);
	}

	/** ToString - Return a double[] as a nice String (formated to a 'adp' digits after the decimal point). */
	public static String toString(double v[], int adp) {
		if (adp <= 0)
			return toString(toIntArray(v));
		int w = adp + 2;
		StringBuilder sb = new StringBuilder("[ ");  
		for(int k = 0; k < v.length; k++) {
			sb.append(String.format("%"+w+"."+adp+"f ", v[k]));
		}
		sb.append("]");
		return sb.toString();
	}

	/** ToString - Return an int[] as a nice String. */
	public static String toString(int v[]) {
		StringBuilder sb = new StringBuilder("[ ");  
		for(int k = 0; k < v.length; k++) {
			sb.append((int)v[k]);
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}

	/** ToString - Return an int[] as a nice String. */
	public static String toString(int v[], int w) {
		StringBuilder sb = new StringBuilder("[ ");  
		for(int k = 0; k < v.length; k++) {
			sb.append(String.format("%"+w+"d ", v[k]));
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

	// sum
	public static int sum(int v[]) {
		int p = 0;
		for(int d : v) {
			p += d;
		}
		return p;
	}

	/** Mode */
	public static int mode(int a[]) {
		int max = 0;
		int count = 0;
		HashMap<Integer,Integer> d = new HashMap<Integer,Integer>();
		for (int v: a) {
			Integer nn = d.get(v);
			int n = (nn==null) ? 1 : nn + 1;
			d.put(v,n);
			if (count < n) {
				count = n;
				max = v;
			}
		}
		return max;
	}

	// append value 'v' to 'array[]' 
	public static int[] append(int array[], final int v) {
		int n = array.length;
		array = Arrays.copyOf(array,n+1);
		array[n] = v;
		return array;
	}

	// delete index 'i' from 'array[]'
	public static int[] delete(int array[], final int i) {
		int n = array.length;
		array[i] = array[n-1];
		array = Arrays.copyOf(array,n-1);
		return array;
	}

	// delete indices 'i[]' from 'array[]' @note: slow!
	public static int[] delete(int array[], final int i[]) {
		for (int i_ : i) {
			array = delete(array,i_);
		}
		return array;
	}

	/*
	// append values 'v[]' to 'array[]'
	public static int[] append(int array[], final int v[]) {
		int n = array.length;
		array = Arrays.copyOf(array,n+1);
		array[n] = v;
		return array;
	}
	*/

	// select/get the elements 'indices[]' from 'array[]'
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

	/** Sample a PMF - select i with probabilitiy w[i] (w must be normalised) */
	public static int samplePMF(double w[], Random r) {
		double u = r.nextDouble();
		double sum = w[0];
		int i = 0;
		while (sum < u) {
			i++;
			sum+=w[i];
		}
		return i;
	}

	/** Make Sequence - Given L, generate and return new int[]{0,1,2,3,...,L-1}. */
	public static final int[] make_sequence(int L) {
		int ind[] = new int[L];
		for(int i = 0; i < L; i++) {
			ind[i] = i;
		}
		return ind;
	}

	/** Make Sequence - Generate and return new int[]{start,start+1,...,end-2,end-1}. */
	public static final int[] make_sequence(int start, int end) {
		int array[] = new int[end-start];
		for(int j = start, i = 0; j < end; j++, i++) {
			array[i] = j;
		}
		return array;
	}

	/**
	 * Invert - take the compliment of indices up to length L, e.g., if indices = [3,5,6], then invert(indices,7) = [1,2,4,7].
	 * @param	indices indices
	 * @param	L		supposed length of the compliment
	 * @return	the compliment (inverted list)
	 */
	public static final int[] invert(int indices[], int L) {
		int sindices[] = Arrays.copyOf(indices,indices.length);
		Arrays.sort(sindices);
		int inverted[] = new int[L-sindices.length];
		for(int j = 0,i = 0; j < L; j++) {
			if (Arrays.binarySearch(sindices,j) < 0) {
				inverted[i++] = j;
			}
		}
		return inverted;
	}

	/** Shuffle 'array' given Random 'r' */
	public static final void shuffle(int array[], Random r) {

		//TODO: use this (but beware some results may change):
		//Collections.shuffle(Arrays.asList(array), r);
		//return array;

		//MLUtils.randomize(array,r);
		for (int i = array.length - 1; i > 0; i--) {
			int index = r.nextInt(i + 1);
			int temp = array[index];
			array[index] = array[i];
			array[i] = temp;
		}
	}

	/**
	 * ToPrimitive - cast Integer[] to int[].
	 */
	public static int[] toPrimitive(Integer a[]) {
		int b[] = new int[a.length];
		for(int i = 0; i < a.length; i++) {
			b[i] = a[i];
		}
		return b;
	}

	/**
	 * ToPrimitive - cast List&lt;Integer&gt; to int[].
	 */
	public static int[] toPrimitive(List<Integer> list) {
		int[] a = new int[list.size()];       
		Iterator<Integer> iter = list.iterator();
		for (int i=0; iter.hasNext(); i++) {       
			a[i] = iter.next();                
		}
		return a;
	}

	/**
	 * ToDoubleArray - cast int[] to double[].
	 */
	public static final double[] toDoubleArray(int z[]) {
		double y[] = new double[z.length];
		for(int j = 0; j < z.length; j++) {
			y[j] = (double)z[j];
		}
		return y;
	}

	// "1,2" -> [1,2]
	//@see MLUtils.toIntArray(String s) 

	/** Convert eg "12" to [1,2] */
	public static final int[] toIntArray(String s) {
		int[] array = new int[s.length()];
		 
		for (int i = 0; i < s.length(); i++) {
			array[i] = Character.digit(s.charAt(i), 10);
		}

		return array;
	}

	/** Convert eg ["1","2"] to [1,2] */
	public static final int[] toIntArray(String s[]) {
		int u[] = new int[s.length];
		for(int j = 0; j < s.length; j++) {
			u[j] = Integer.parseInt(s[j]);
		}
		return u;
	}

	// @see also toIntArray(z,t)
	public static final int[] toIntArray(double z[]) {
		int u[] = new int[z.length];
		for(int j = 0; j < z.length; j++) {
			u[j] = (int)z[j];
		}
		return u;
	}

	/**
	 * Convert an array of doubles to ints through a threshold.
	 *	@see M.threshold
	 */ 
	public static final int[] toIntArray(double z[], double t) {
		int u[] = new int[z.length];
		for(int j = 0; j < z.length; j++) {
			u[j] = (z[j] >= t) ? 1 : 0;
		}
		return u;
	}

	/**
	 * Convert integer to binary string (double representation) of L digits.
	 * e.g., toDoubleArray(3,4) = [0., 0., 1., 1.]
	 */
	public static final double[] toDoubleArray(int i, int L) {
		double u[] = new double[L];
		if (i == 0)
			return u;
		String s = Integer.toBinaryString(i);
		for(int j = 0; j < s.length(); j++) {
			u[j] = (double)Integer.parseInt(String.valueOf(s.charAt(j)));
		}
		return u;
	}

	/**
	 * Do some tests.
	 */
	public static void main (String args[]) {
		int a[] = new int[] {1, 3, 4, 8, -4, -3, 4, 4, 4, 10, -3, -3, -3, -3, -3};
		System.out.println("mode: "+A.mode(a));
	}

}

