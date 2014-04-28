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

import meka.filters.multilabel.SuperNodeFilter;
import meka.core.A;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * SuperLabel - A meta label is a label composed of multiple labels, e.g., [3,7], which can take multiple values, e.g., [[0,0],[0,1],[1,1]].
 * @author 	Jesse Read 
 * @version	March 2014
 */
public class SuperLabel {

	/** for serialization. */
	private static final long serialVersionUID = -6783833712552497991L;

	public int indices[];  // e.g., [1,0]
	public int values[][]; // e.g., [[0,0],[0,1],[1,1]]

	/**
	 * SuperLabel
	 */
	public SuperLabel(int indices[], int values[][]) {
		this.indices = indices;
		this.values = values;
	} 

	private static ArrayList<String> getList(Enumeration<String> ve) {
		ArrayList<String> list  = new ArrayList<String>();

		while (ve.hasMoreElements()) {
			list.add(ve.nextElement());
		}

		return list;
	}

	public SuperLabel(int indices[], Enumeration<String> ve) {
		this(indices,getList(ve));
	}

	public SuperLabel(int indices[], ArrayList<String> vlist) {
		this.indices = indices;
		this.values = new int[vlist.size()][indices.length];
		for(int i = 0; i < this.values.length; i++) {
			this.values[i] = A.toIntArray(SuperNodeFilter.decodeValue(vlist.get(i)));
		}
	}

	@Override
	public String toString() {
		String s = "";
		s += ("INDICES "+Arrays.toString(indices)+", taking values in {");
		for(int i = 0; i < this.values.length; i++) {
			s += (" ["+i+"]:"+Arrays.toString(values[i]));
		}
		s += (" }");
		return s;
	}

	// THE FOLLOWING CAME FROME SCC.java -- @TODO see if it is of any use.

	/**
	 * Super Class.
	class SuperClass {

		SuperClass index[] = null;

		int idx = -1;
		double prob = 1.0;

		public SuperClass() { 			// root
		}

		public SuperClass(int n) {		// branch
			idx = n;
		}

		**
		 * AddNode - add a node with index 'j' that can take 'k' values.
		 * @param	n	node index
		 * @param	k	number of values
		 *
		public void addNode(int n, int k) {
			double p = 0.0;
			if (index != null) {
				for(int v = 0; v < index.length; v++) {
					index[v].addNode(n,k);
				}
			}
			else {
				index = new SuperClass[k];
				for(int v = 0; v < index.length; v++) {
					index[v] = new SuperClass(n); 		// p(x==v)
				}
			}
		}

		public void fillNodes(Instances D) {
			// @todo could be much faster by prebuffering arry of size of the depth of the tree
			fillNodes(new int[]{},new int[]{},D);
		}

		public void fillNodes(int indices[], int values[], Instances D) {
			//System.out.println("fillNodes("+Arrays.toString(indices)+","+Arrays.toString(values)+")");
			if (index == null) {
				// END, calculate the joint
				prob = StatUtils.P(D,indices,values);
				//System.out.println("we arrived with P("+Arrays.toString(indices)+" == "+Arrays.toString(values)+") = "+prob);
			}
			else {
				// GO DOWN
				// @todo could be faster by moving the add(indices,idx) outside of the v loop (but not by much!)
				for(int v = 0; v < index.length; v++) {
					index[v].fillNodes(A.add(indices,index[0].idx), A.add(values,v), D);
				}
			}
		}

		// the probability of 'path' in this factor
		// (only a part of the path may be relevant)
		public double p_path(int path[]) {
			//System.out.println(""+Arrays.toString(path));
			//System.out.println(""+idx);
			if (index==null) {
				return prob;
			}
			else {
				int i = index[0].idx;				// 3	\in {1,...,L}
				int v = path[i];					// 0 	\in {0,1,..,K}
				//System.out.println("take "+v+"th path");
				return index[v].p_path(path);
			}
		}

		public String toString() {
			return (index!=null) ?  idx + "/" + index.length + "\n" + index[0].toString() : " = "+prob;
		}

	}
	*/
} 

