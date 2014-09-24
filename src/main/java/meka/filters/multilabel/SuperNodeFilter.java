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

package meka.filters.multilabel;

import weka.core.*;
import meka.core.MLUtils;
import meka.classifiers.multitarget.NSR;
import weka.filters.*;
import java.util.*;
import java.io.*; // for test routin main()

/**
 * SuperNodeFilter.java - Super Class Filter.
 *
 * Input:<br>
 * 		Data with label attributes,       e.g., [0,1,2,3,4]<br>
 * 		A desired partition of indices,   e.g., [[1,3],[4],[0,2]], filter <br>
 * Output:<br>
 * 		New data with label attributes:         [1+3,4,0+2]<br>
 * 		(the values each attribute can take are pruned if necessary)<br>
 *
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 * @version	June 2012
 */
public class SuperNodeFilter extends SimpleBatchFilter {

	protected Instance x_template = null;
	protected int m_P = 0, m_N = 0;
	protected int indices[][] = null;

	public void setIndices(int n[][]) {
		for(int i = 0; i < n.length; i++) {
			Arrays.sort(n[i]); // always sorted!
		}
		this.indices = n;
	}

	public void setP(int p) {
		this.m_P = p;
	}

	public int getP() {
		return this.m_P;
	}

	public void setN(int n) {
		this.m_N = n;
	}

	@Override
	public Instances determineOutputFormat(Instances D) throws Exception {
		//System.out.println("DETERMINE OUTPUT FORMAT = "+D.numInstances());
		Instances D_out = new Instances(D,0);
		int L = D.classIndex();
		for(int i = 0; i < L-indices.length; i++) {
			D_out.deleteAttributeAt(0);
		}
		return D_out;
	}

	public Instance getTemplate() {
		return x_template;
	}

	@Override
	public Instances process(Instances D) throws Exception {

		//System.out.println("PROCESS! = "+D.numInstances());

		int L = D.classIndex();
		D = new Instances(D); // D_

		// rename classes 
		for(int j = 0; j < L; j++) {
			D.renameAttribute(j,encodeClass(j));
		}

		// merge labels
		D = mergeLabels(D,indices,m_P,m_N);

		// templates
		x_template = D.firstInstance();
		setOutputFormat(D);

		//System.out.println("PROCESS! => "+D);
		return D;
	}

	private static String join(int objs[], final String delimiter) {
		if (objs == null || objs.length < 1)
			return "";
		StringBuffer buffer = new StringBuffer(String.valueOf(objs[0]));
		for(int j = 1; j < objs.length; j++) {
			buffer.append(delimiter).append(String.valueOf(objs[j]));
		}
		return buffer.toString();
	}

	// (3,'_') -> "c_3"
	public static String encodeClass(int j) {
		return "c_"+j;
	}

	// ("c_3",'_') -> 3
	public static int decodeClass(String a) {
		//System.out.println(""+a);
		return Integer.parseInt(a.substring(a.indexOf('_')+1));
	}

	// (["c_3","c_1"]) -> "c_3+1"
	public static String encodeClass(String c_j, String c_k) {
		return "c_"+join(decodeClasses(c_j),"+")+"+"+join(decodeClasses(c_k),"+");
	}

	// ([3,1]) -> "c_3+1"
	public static String encodeClass(int c_[]) {
		String c = "c_";
		for(int j = 0; j < c_.length; j++) {
			c = c + c_[j] + "+";
		}
		c = c.substring(0,c.length()-1);
		return c;
	}

	// ("c_3+1") -> [3,1]
	public static int[] decodeClasses(String a) {
		String s[] = new String(a.substring(a.indexOf('_')+1)).split("\\+");
		int vals[] = new int[s.length]; 
		for(int j = 0; j < vals.length; j++) {
			vals[j] = Integer.parseInt(s[j]);
		}
		return vals;
	}

	// (3,1) -> "3+1"
	public static String encodeValue(String v_j, String v_k) {
		return String.valueOf(v_j)+"+"+String.valueOf(v_k);
	}

	// (3,1,2) -> "3+1+2"
	public static String encodeValue(Instance x, int indices[]) {
		String v = "";
		for(int j = 0; j < indices.length; j++) {
			v+=x.stringValue(indices[j])+"+"; 
		}
		v = v.substring(0,v.length()-1);
		return v;
	}

	// "C+A+B" -> ["C","A","B"]
	public static String[] decodeValue(String a) {
		return a.split("\\+");
	}

	/*
	   Return a set of all the combinations of 'indices' in 'D', pruned by 'p'
	   e.g. {00,01,11}
   */
	public static Set<String> getValues(Instances D, int indices[], int p) {
		HashMap<String,Integer> count = getCounts(D,indices,p);
		return count.keySet();
	}

	public static HashMap<String,Integer> getCounts(Instances D, int indices[], int p) {
		HashMap<String,Integer> count = new HashMap<String,Integer>();
		for(int i = 0; i < D.numInstances(); i++) {
			String v = encodeValue(D.instance(i), indices);
			count.put(v, count.containsKey(v) ? count.get(v) + 1 : 1);
		}
		MLUtils.pruneCountHashMap(count,p);
		return count;
	}

	/**
	 * Merge Labels - Make a new 'D', with labels made into superlabels, according to partition 'indices', and pruning values 'p' and 'n'.
	 * @assume	j < k
	 * @assume attributes in D labeled by original index
	 * @returns	 Instaces with attributes at j and k moved to position L as (j,k), with classIndex = L-1
	 */
	public static Instances mergeLabels(Instances D, int indices[][], int p, int n) {

		int L = D.classIndex();
		int K = indices.length;
		ArrayList<String> values[] = new ArrayList[K];
		HashMap<String,Integer> counts[] = new HashMap[K];

		// create D_
		Instances D_ = new Instances(D);

		// clear D_
		for(int j = 0; j < L; j++) {
			D_.deleteAttributeAt(0);
		}

		// create atts
		for(int j = 0; j < K; j++) {
			int att[] = indices[j];
			//int values[] = new int[2]; //getValues(indices,D,p);
			counts[j] = getCounts(D,att,p);
			Set<String> vals = counts[j].keySet(); //getValues(D,att,p);
			values[j] = new ArrayList(vals);
			D_.insertAttributeAt(new Attribute(encodeClass(att),new ArrayList(vals)),j);
		}

		// copy over values
		ArrayList<Integer> deleteList = new ArrayList<Integer>();
		for(int i = 0; i < D.numInstances(); i++) {
			Instance x = D.instance(i);
			for(int j = 0; j < K; j++) {
				String y = encodeValue(x,indices[j]);
				try {
					D_.instance(i).setValue(j,y); // y = 
				} catch(Exception e) {
					// value not allowed
					deleteList.add(i); 									   // mark it for deletion
					String y_close[] = NSR.getTopNSubsets(y,counts[j],n); // get N subsets
					for(int m = 0; m < y_close.length; m++) {
						//System.out.println("add "+y_close[m]+" "+counts[j]);
						Instance x_copy = (Instance)D_.instance(i).copy();
						x_copy.setValue(j,y_close[m]);
						x_copy.setWeight(1.0/y_close.length);
						D_.add(x_copy);
					}
				}
			}
		}
		// clean up
		Collections.sort(deleteList,Collections.reverseOrder());
		//System.out.println("Deleting "+deleteList.size()+" defunct instances.");
		for (int i : deleteList) {
			D_.delete(i);
		}
		// set class
		D_.setClassIndex(K);
		// done!
		D = null;
		return D_;
	}

	/**
	 * Merge Labels.
	 * @assume	j < k
	 * @assume: attributes in D labeled by original index
	 * @returns	 Instaces with attributes at j and k moved to position L as (j,k), with classIndex = L-1
	 */
	public static Instances mergeLabels(Instances D, int j, int k, int p) {
		int L = D.classIndex();

		HashMap<String,Integer> count = new HashMap<String,Integer>();

		Set<String> values = new HashSet<String>();
		for(int i = 0; i < D.numInstances(); i++) {
			String v = encodeValue(D.instance(i).stringValue(j),D.instance(i).stringValue(k));
			String w = ""+(int)D.instance(i).value(j)+(int)D.instance(i).value(k);
			//System.out.println("w = "+w);
			count.put(v,count.containsKey(v) ? count.get(v) + 1 : 1);
			values.add(encodeValue(D.instance(i).stringValue(j),D.instance(i).stringValue(k)));
		}
		//System.out.println("("+j+","+k+")"+values);
		System.out.print("pruned from "+count.size()+" to ");
		MLUtils.pruneCountHashMap(count,p);
		String y_max = (String)MLUtils.argmax(count); // @todo won't need this in the future
		System.out.println(""+count.size()+" with p = "+p);
		System.out.println(""+count);
		values = count.keySet();

		// Create and insert the new attribute
		D.insertAttributeAt(new Attribute(encodeClass(D.attribute(j).name(),D.attribute(k).name()),new ArrayList(values)),L);

		// Set values for the new attribute
		for(int i = 0; i < D.numInstances(); i++) {
			Instance x = D.instance(i);
			String y_jk = encodeValue(x.stringValue(j),x.stringValue(k));
			try {
				x.setValue(L,y_jk); // y_jk = 
			} catch(Exception e) {
				//x.setMissing(L);
				//D.delete(i);
				//i--;
				String y_close[] = getNeighbours(y_jk,count,1); // A+B+NEG, A+C+NEG
				//System.out.println("OK, that value ("+y_jk+") didn't exist ... set the closests ones ...: "+Arrays.toString(y_close));
				int max_c = 0;
				for (String y_ : y_close) {
					int c = count.get(y_);
					if (c > max_c) {
						max_c = c;
						y_max = y_;
					}
				}
				//System.out.println("we actually found "+Arrays.toString(y_close)+" but will only set one for now (the one with the highest count) : "+y_max+" ...");
				x.setValue(L,y_max);
				// ok, that value didn't exist, set the maximum one (@TODO: set the nearest one)
			}
		}

		// Delete separate attributes
		D.deleteAttributeAt(k > j ? k : j);
		D.deleteAttributeAt(k > j ? j : k);

		// Set class index
		D.setClassIndex(L-1);
		return D;
	}

	/**
	 * GetNeighbours - return from set S, label-vectors closest to y, having no more different than 'n' bits different.
	 */
	public static String[] getNeighbours(String y, ArrayList<String> S, int n) {
		String ya[] = decodeValue(y);
		ArrayList<String> Y = new ArrayList<String>();  
		for(String y_ : S) {
			if(MLUtils.bitDifference(ya,decodeValue(y_)) <= n) {
				Y.add(y_);
			}
		}
		return (String[])Y.toArray(new String[Y.size()]);
	}

	public static String[] getNeighbours(String y, HashMap <String,Integer>S, int n) {
		return getNeighbours(y,new ArrayList<String>(S.keySet()),n);
	}

	protected int m_Seed = 0;

	@Override
	public String globalInfo() {
		return "A SuperNode Filter";
	}

	public static void main(String[]  argv) {
		try {
			String fname = Utils.getOption('i',argv);
			Instances D = new Instances(new BufferedReader(new FileReader(fname)));
			SuperNodeFilter f = new SuperNodeFilter();
			int c = Integer.parseInt(Utils.getOption('c',argv));
			D.setClassIndex(c);
			System.out.println(""+f.process(D));
			//runFilter(new SuperNodeFilter(), argv);
		} catch(Exception e) {
			System.err.println("");
			e.printStackTrace();
			//System.exit(1);
		}
	}
}
