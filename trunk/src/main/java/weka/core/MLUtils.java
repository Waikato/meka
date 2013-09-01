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

import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import weka.core.*;
import java.util.*;

public abstract class MLUtils {

	/**
	 * GetDataSetOptions - Look for options in the 'relationName' in format 'dataset-name: options'
	 * @return	The dataset options found
	 */
	public static final String[] getDatasetOptions(Instances instances) {
		String name = instances.relationName();
		if(name.indexOf(':') > 0) {
			return name.substring(name.indexOf(':')+1).split(" ");
		}
		else return new String[]{};
	}

	/**
	 * GetDataSetName - Look for name in the 'relationName' in format 'dataset-name: options'
	 * @return	The dataset name
	 */
	public static final String getDatasetName(Instances instances) {
		String name = instances.relationName();
		if(name.indexOf(':') > 0) {
			return name.substring(0,name.indexOf(':'));
		}
		else return name;
	}

	public static final String takeClassifierName(String alg_spec[]) {
		if (alg_spec.length < 1) return null;
		String _name = alg_spec[0];
		alg_spec[0] = "";
		return _name;
	}

	// Given L, generate int[0,1,2,3,...,L]
	public static final int[] gen_indices(int L) {
		int ind[] = new int[L];
		for(int i = 0; i < L; i++) {
			ind[i] = i;
		}
		return ind;
	}


	// Shuffle an array given 'r', @TODO use Collections.shuffle(array.asList());
	public static final void randomize(int array[], Random r) {
		for (int i = array.length - 1; i > 0; i--) {
			int index = r.nextInt(i + 1);
			int temp = array[index];
			array[index] = array[i];
			array[i] = temp;
		}
	}

	// raw instance to int array (i.e. from binary representation)
	public static final double[] toDoubleArray(Instance ins, int c) {
		double a[] = new double[c];
		for(int i = 0; i < c; i++) {
			a[i] = Math.round(ins.value(i));
		}
		return a;
	}

	// same as above, no rounding
	public static final double[] toDoubleArray(Instance x) {
		double y[] = new double[x.classIndex()];
		for(int j = 0; j < y.length; j++) {
			y[j] = x.value(j);
		}
		return y;
	}

	// bit string of c bits where the jth bit is turned on
	public static final String toBitString(int j, int c) {
		StringBuilder sb = new StringBuilder(c);  
		for(int i = 0; i < c; i++) {
			if (j == i) 
				sb.append('1');
			else
				sb.append('0');
		}
		return sb.toString();
	}

	// raw instance to bit string (i.e. from binary representation)
	public static final String toBitString(Instance ins, int c) {
		StringBuilder sb = new StringBuilder(c);  
		for(int i = 0; i < c; i++) {
			sb.append((int)Math.round(ins.value(i)));
		}
		return sb.toString();
	}

	// may not need this // yes we do
	public static final String toBitString(int i[]) {
		StringBuilder sb = new StringBuilder(i.length);  
		for (int b : i) {
			sb.append(b);
		}
		return sb.toString();
	}

	public static final String toBitString(double d[]) {
		StringBuilder sb = new StringBuilder(d.length);  
		for (double b : d) {
			sb.append((int)Math.round(b));
		}
		return sb.toString();
	}

	public static final double[] fromBitString(String s) {
		char a[] = s.toCharArray();
		double d[] = new double[a.length];
		for(int i = 0; i < a.length; i++) {
			d[i] = (double)char2int(a[i]);
		}
		return d;
	}

	// "[1,2]" -> [1,2]
	public static final int[] toIntArray(String s) {
		s = new String(s.trim());
		return toIntArray((s.substring(1,s.length()-1)).split(","));
	}

	// raw instance to bit string (i.e. from binary representation)
	public static final int[] toIntArray(Instance x, int L) {
		int y[] = new int[L];
		for(int j = 0; j < L; j++) {
			y[j] = (int)Math.round(x.value(j));
		}
		return y;
	}
	// raw instance to bit string (i.e. from binary representation)
	public static final int[] toIntArray(double w[], double t) {
		int y[] = new int[w.length];
		for(int j = 0; j < w.length; j++) {
			if (w[j] >= t) 
				y[j] = 1;
		}
		return y;
	}

	public static final int[] toIntArray(String s[]) {
		int y[] = new int[s.length];
		for(int j = 0; j < s.length; j++) {
			y[j] = Integer.parseInt(s[j].trim());
		}
		return y;
	}

	// "[1.0,2.0]" -> [1.0,2.0]
	public static final double[] toDoubleArray(String s) {
		s = new String(s.trim());
		/*
		if (s.startsWith("[")) {
			s = s.substring(1);
		}
		if (s.endsWith("]")) {
			s = s.substring(0,s.length()-1);
		}
		*/
		return toDoubleArray((s.substring(1,s.length()-1)).split(","));
	}

	public static final double[] toDoubleArray(String s[]) {
		double y[] = new double[s.length];
		for(int j = 0; j < s.length; j++) {
			y[j] = Double.parseDouble(s[j].trim());
		}
		return y;
	}

	public static final double[] toDoubleArray(int z[]) {
		double y[] = new double[z.length];
		for(int j = 0; j < z.length; j++) {
			y[j] = (double)z[j];
		}
		return y;
	}

	/** 
	 * LabelCardinality - return the label cardinality of dataset D.
	 */
	public static final double labelCardinality(Instances D) {
		return labelCardinality(D,D.classIndex());
	}

	/** 
	 * LabelCardinality - return the label cardinality of dataset D of L labels.
	 */
	public static final double labelCardinality(Instances D, int L) {
		double sum = 0.0;
		for(int i = 0; i < D.numInstances(); i++) {
			for(int j = 0; j < L; j++) {
				sum += D.instance(i).value(j);
			}
		}
		return (double)sum/(double)D.numInstances();
	}

	/** 
	 * LabelCardinality - return the label cardinality of label data Y
	 * @TODO move to Metrics.java ?
	 */
	public static final double labelCardinality(int Y[][]) {
		int N = Y.length;
		int L = Y[0].length;
		double sum = 0.0;
		for(int i = 0; i < N; i++) {
			for(int j = 0; j < L; j++) {
				sum += Y[i][j];
			}
		}
		return (double)sum/(double)N;
	}

	/** 
	 * LabelCardinalities - return the frequency of each label of dataset D.
	 */
	public static final double[] labelCardinalities(Instances D) {
		int L = D.classIndex();
		double lc[] = new double[L];
		for(int j = 0; j < L; j++) {
			for(int i = 0; i < D.numInstances(); i++) {
				lc[j] += D.instance(i).value(j);
			}
			lc[j] /= D.numInstances();
		}
		return lc;
	}

	/** 
	 * LabelCardinalities - return the frequency of each label of dataset D.
	 */
	public static final double[] labelCardinalities(ArrayList<int[]> Y) {
		int L = ((int[]) Y.get(0)).length;
		double lc[] = new double[L];
		for(int y[] : Y) {
			for(int j = 0; j < L; j++) {
				lc[j] += y[j];
			}
		}
		for(int j = 0; j < L; j++) {
			lc[j] /= Y.size();
		}
		return lc;
	}

	/** 
	 * EmptyVectors - percentage of empty vectors sum(y[i])==0 in Y.
	 */
	public static final double emptyVectors(int Y[][]) {
		int N = Y.length;
		int L = Y[0].length;
		double sum = 0.0;
		for(int i = 0; i < N; i++) {
			if (Utils.sum(Y[i]) <= 0.0)
				sum ++;
		}
		return (double)sum/(double)N;
	}

	// Most Common Combination. Assuming binary format.
	public static final String mostCommonCombination(Instances D) {
		return mostCommonCombination(D,D.classIndex());
	}

	public static final String mostCommonCombination(Instances D, int L) {
		HashMap<String,Integer> hm = new HashMap<String,Integer>(D.numInstances());
		double max_v  = 0.0;
		int max_i = 0;

		for(int i = 0; i < D.numInstances(); i++) {
			String y = MLUtils.toBitString(D.instance(i),L);

			Integer v = hm.get(y);
			if (v == null) {
				hm.put(y,0); 
			} else {
				if (v > max_v) {
					max_v = v;
					max_i = i;
				}
				hm.put(y,v+1);
			}
		}

		return MLUtils.toBitString(D.instance(max_i),L);
	}

	// the number of chars different in the two strings (suitable for binary strings)
	public static final int bitDifference(String s1, String s2) {
		int sum = 0;
		for(int i = 0; i < s1.length(); i++) {
			if (s1.charAt(i) != s2.charAt(i))
				sum++;
		}
		return sum;
	}

	public static final int bitDifference(String y1[], String y2[]) {
		int sum = 0;
		for(int i = 0; i < y1.length; i++) {
			if (!y1[i].equals(y2[i]))
				sum++;
		}
		return sum;
	}

	public static final int bitDifference(int y1[], int y2[]) {
		int sum = 0;
		for(int i = 0; i < y1.length; i++) {
			if (y1[i] != y2[i])
				sum++;
		}
		return sum;
	}

	// BitCount. Count relevant labels
	public static final int bitCount(String s) {
		int total = 0;
		for(int i = 0; i < s.length(); i++) {
			total += char2int(s.charAt(i));
		}
		return total;
	}

	// eg '0' ==> 0
	public static final int char2int(char c) {
		return (int)(c - '0');
	}

	public static final HashMap<String,Integer> countCombinations(Instances D, int L) {
		HashMap<String,Integer> map = new HashMap<String,Integer>();  
		for (int i = 0; i < D.numInstances(); i++) {
			String y = MLUtils.toBitString(D.instance(i),L);
			Integer c = map.get(y);
			map.put(y,c == null ? 1 : c+1);
		}
		return map;
	}

	/*
	// ["A","B","NEG"] -> "A+B+NEG"
	public static String encodeValue(String s[]) {
		StringBuilder sb = new StringBuilder(String.valueOf(s[0]));  
		for(int i = 1; i < s.length; i++) {
			sb.append('+').append(s[i]);
		}
		return sb.toString();
	}
	*/

	// [0,3,2] -> "0+3+2"
	public static String encodeValue(int s[]) {
		StringBuilder sb = new StringBuilder(String.valueOf(s[0]));  
		for(int i = 1; i < s.length; i++) {
			sb.append('+').append(s[i]);
		}
		return sb.toString();
	}

	// "0+3+2" -> [0,3,2]
	public static int[] decodeValue(String a) {
		return toIntArray(a.split("\\+"));
	}

	// MULTI-TARGET VERSION of 'countCombinations'
	// returns entries like e.g. [0,2,2,3,2],5
	public static final HashMap<String,Integer> classCombinationCounts(Instances D) {
		int L = D.classIndex();
		HashMap<String,Integer> map = new HashMap<String,Integer>();  
		for (int i = 0; i < D.numInstances(); i++) {
			String y = encodeValue(toIntArray(D.instance(i),L));
			Integer c = map.get(y);
			map.put(y,c == null ? 1 : c+1);
		}
		return map;
	}

	// @TODO remove either this or the following function
	public static final Object maxItem(HashMap<?,Double> map) {
		Object max_k = null;
		double max_v = 0.0;
		for (Object k : map.keySet()) {
			if (map.get(k) >= max_v) {
				max_k = k;
				max_v = map.get(k);
			}
		}
		return max_k;
	}

	public static final String maxCombination(HashMap<String,Integer> count) {

		int max_v  = 0;
		String max_s = null;

		for(String s : count.keySet()) {
			Integer v = count.get(s);

			if (v > max_v) {
					max_v = v;
					max_s = s;
				}
			}
		return max_s;
	}

	public static final int numberOfUniqueCombinations(Instances D) {
		HashMap<String,Integer> hm = classCombinationCounts(D);
		return hm.size();
	}

	/**
	 * SwitchAttributes - Move label attributes from End to Beginning of attribute space (MULAN format to MEKA format). 
	 * Note: can use e.g.: java weka.filters.unsupervised.attribute.Reorder -i thyroid.arff -R 30-last,1-29"
	 */
	public static final Instances switchAttributes(Instances D, int L) {
		int d = D.numAttributes();
		for(int j = 0; j < L; j++) {
			D.insertAttributeAt(D.attribute(d-1).copy(D.attribute(d-1).name()+"-"),0);
			for(int i = 0; i < D.numInstances(); i++) {
				D.instance(i).setValue(0,D.instance(i).value(d));
			}
			D.deleteAttributeAt(d);
		}
		return D;
	}

	// NEW

	public static final Instance deleteAttributesAt(Instance x, int indicesToRemove[]) {//, boolean keep) {
		Utils.sort(indicesToRemove);
		for(int j = indicesToRemove.length-1; j >= 0; j--) {
			x.deleteAttributeAt(indicesToRemove[j]);
		}
		return x;
	}

	public static final Instance keepAttributesAt(Instance x, int indicesToRemove[], int lim){
		return deleteAttributesAt(x, invert(indicesToRemove, lim));
	}

	public static final Instances deleteAttributesAt(Instances D, int indicesToRemove[]) {//, boolean keep) {
		Utils.sort(indicesToRemove);
		for(int j = indicesToRemove.length-1; j >= 0; j--) {
			D.deleteAttributeAt(indicesToRemove[j]);
		}
		return D;
	}


	public static final Instances keepAttributesAt(Instances D, int indicesToRemove[], int lim){
		return deleteAttributesAt(D, invert(indicesToRemove, lim));
	}

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

	public static final Instance setTemplate(Instance x, Instances instancesTemplate) {
		int L = x.classIndex();
		int L_t = instancesTemplate.classIndex();
		x = (Instance)x.copy();
		x.setDataset(null);
		for (int i = L_t; i < L; i++)
			x.deleteAttributeAt(0);
		x.setDataset(instancesTemplate);
		return x;
	}

	/**
	 * SetTemplate - returns a copy of x_template, set with x's attributes, and set to dataset D_template (of which x_template) is a template of this.
	 * This function is very useful when Weka throws a strange IndexOutOfBounds exception for setTemplate(x,Template)
	 */
	public static final Instance setTemplate(Instance x, Instance x_template, Instances D_template) {
		Instance x_ = (Instance)x_template.copy();
		int L_y = x.classIndex();
		int L_z = D_template.classIndex();
		// copy over x space
		MLUtils.copyValues(x_,x,L_y,L_z);
		// set class values to missing
		MLUtils.setLabelsMissing(x_,L_z);
		// set dataset
		x_.setDataset(D_template);
		return x_;
	}

	/**
	 * CopyValues - Set x_dest[j+offset] = x_src[i+from].
	 */
	public static final Instance copyValues(Instance x_dest, Instance x_src, int from, int offset) {
		int d = x_src.numAttributes();
		for(int i = from, j = 0; i < d; i++, j++) {
			x_dest.setValue(j+offset,x_src.value(i));
		}
		return x_dest;
	}
	
	/**
	 * SetValues - set the attribute values in Instsance x (having L labels) to z[].
	 * @todo call above method
	 */
	public static final Instance setValues(Instance x, double z[], int L) {
		for(int a = 0; a < z.length; a++) {
			x.setValue(L+a,z[a]);
		}
		return x;
	}

	/* not in use
	public static int max(int array[]) {
		int max = Integer.MIN_VALUE;
		for(int i : array) {
			if (max > i)
				max = i;
		}
		return max;
	}
	*/

	public static int mode(int a[]) {
		int max = 0;
		int count = 0;
		HashMap<Integer,Integer> d = new HashMap<Integer,Integer>();
		for(int v : a) {
			int n = d.containsKey(v) ? d.get(v) + 1 : 1;
			d.put(v,n);
			if (n > count)
				max = v;
		}
		return max;
	}

	public static String printAsTextMatrix(double M[][]) {
		StringBuilder sb = new StringBuilder("M = [\n");  
		for(int j = 0; j < M.length; j++) {
			for(int k = 0; k < M[j].length; k++) {
				sb.append(Utils.doubleToString(M[j][k],8,2));
			}
			if (j < M.length - 1)
				sb.append("\n");
		}
		sb.append("\n]");
		return sb.toString();
	}

	public static void pruneCountHashMap(HashMap<?,Integer> hm, int p) {
		ArrayList al = new ArrayList();  
		for (Object o : hm.keySet()) {
			if(hm.get(o) <= p) {
				al.add(o);
			}
		}
		for (Object o : al) {
			hm.remove(o);
		}
		al.clear();
		al = null;
	}

	// assume that no hm.get(.) > N
	public static void pruneCountHashMapBasedAsAFractionOf(HashMap<?,Integer> hm, double p, int N) {
		ArrayList al = new ArrayList();  
		for (Object o : hm.keySet()) {
			if((double)hm.get(o)/(double)N <= p) {
				al.add(o);
			}
		}
		for (Object o : al) {
			hm.remove(o);
		}
		al.clear();
		al = null;
	}

	/**
	 * SetLabelsMissing - Set all labels in D to missing.
	 */
	public static Instances setLabelsMissing(Instances D) {
		int L = D.classIndex();
		for(int i = 0; i < D.numInstances(); i++) {
			for(int j = 0; j < L ; j++) {
				D.instance(i).setMissing(j);
			}
		}
		return D;
	}

	/**
	 * SetLabelsMissing - Set all labels in x to missing.
	 */
	public static Instance setLabelsMissing(Instance x) {
		return setLabelsMissing(x,x.classIndex());
	}

	/**
	 * SetLabelsMissing - Set all (L) labels in x to missing.
	 */
	public static Instance setLabelsMissing(Instance x, int L) {
		for(int j = 0; j < L ; j++) {
			x.setMissing(j);
		}
		return x;
	}

	public static final Instances combineInstances(Instances D1, Instances D2) {
		Instances D = new Instances(D1);
		for(int i = 0; i < D2.numInstances(); i++) {
			D.add(D2.instance(i));
		}
		return D;
	}

	public static final String toDebugString(Instances D) {
		int L = D.classIndex();
		StringBuilder sb = new StringBuilder();  
		sb.append("D="+D.numInstances());
		sb.append(" L="+L+" {");
		for(int j = 0; j < L; j++) {
			sb.append(D.attribute(j).name()+" ");
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static final String toDebugString(Instance x) {
		int L = x.classIndex();
		StringBuilder sb = new StringBuilder();  
		sb.append("y = [");
		for(int j = 0; j < L; j++) {
			sb.append(x.value(j)+" ");
		}
		sb.append("], x = [");
		for(int j = L; j < L+10; j++) {
			sb.append(x.value(j)+" ");
		}
		sb.append(" ... ]");
		return sb.toString();
	}

	/*
    public static int factorial(int n) {
        int fact = 1; 
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }
	*/

	public static int[] toPrimitive(Integer a[]) {
		int b[] = new int[a.length];
		for(int i = 0; i < a.length; i++) {
			b[i] = a[i];
		}
		return b;
	}

	/**
	 * ToBinaryString - use to go through all 'L' binary combinations.
	 * @param	l	the number to permute
	 * @param	L	number of labels
	 */
	public static final String toBinaryString(int l, int L) {
		String sb = new String(Integer.toBinaryString(l));
		while (sb.length() < L) {
			sb = "0"+sb;
		}
		return sb;
	}

	private static void permute(String beginningString, String endingString, ArrayList<String> perm) {
		if (endingString.length() <= 1) {
			perm.add(beginningString + endingString);
		}
		else
			for (int i = 0; i < endingString.length(); i++) {
				String newString = endingString.substring(0, i) + endingString.substring(i + 1);
				permute(beginningString + endingString.charAt(i), newString, perm);
			}
	}

	// AB -> AB,BA
	public static String[] permute(String s) {
		ArrayList<String> a = new ArrayList<String>();  
		permute("", s, a);
		return a.toArray(new String[0]);
	}

	public static String hashmapToString(HashMap<?,?> hm) {
		StringBuilder sb = new StringBuilder();
		for (Object k : hm.keySet()) {
			sb.append(Utils.padLeft(k.toString(),20)).append(" : ").append(hm.get(k)).append('\n');
		}
		return sb.toString();
	}

	public static int getIntegerOption(String op, int def) {
		try {
			return Integer.parseInt(op);
		} catch(Exception e) {
			System.err.println("Failed to parse "+op+" to double: using default of "+def);
			return def;
		}
	}

	public static void clearLabels(Instance x) {
		int L = x.classIndex();
		for(int j = 0; j < L; j++) 
			x.setValue(j,0.0);
	}

	/**
	 * GetXfromD - Extract attributes as a double X[][] from Instances D.
	 * @TODO getXfromInstances would be a better name.
	 */
	public static double[][] getXfromD(Instances D) {
		int N = D.numInstances();
		int L = D.classIndex();
		int d = D.numAttributes()-L;
		//System.out.println("d="+d);
		double X[][] = new double[N][d];
		for(int i = 0; i < N; i++) {
			for(int k = 0; k < d; k++) {
				X[i][k] = D.instance(i).value(k+L);
			}
		}
		return X;
	}

	/**
	 * GetXfromD - Extract labels as a double Y[][] from Instances D.
	 * @TODO getYfromInstances would be a better name.
	 */
	public static double[][] getYfromD(Instances D) {
		int L = D.classIndex();
		int N = D.numInstances();
		double Y[][] = new double[N][L];
		for(int i = 0; i < N; i++) {
			for(int k = 0; k < L; k++) {
				Y[i][k] = D.instance(i).value(k);
			}
		}
		return Y;
	}

	/**
	 * GetxfromInstances - Extract attributes as a double x[] from an Instance.
		// @NOTE changed this to xy.toDoubleArray();
	 */
	public static double[] getxfromInstance(Instance xy) {
		int L = xy.classIndex();
		double xy_[] = xy.toDoubleArray();
		return Arrays.copyOfRange(xy_,L,xy_.length);
		//double x[] = new double[xy.numAttributes() - L];
		//for(int i = 0; i < x.length; i++) {
			//x[i] = xy.value(i+L);
		//}
		//return x;
	}

	/**
	 * ReplaceZasAttributes - data Z[][] will be the new attributes in D.
	 */
	public static Instances replaceZasAttributes(Instances D, double Z[][], int L) {
		D.setClassIndex(0);
		int m = D.numAttributes()-L;
		for(int j = 0; j < m; j++) {
			D.deleteAttributeAt(L);
		}
		return addZtoD(D,Z,L);
	}

	/**
	 * AddZtoD - Add data Z[][] to Instances D.
	 * @TODO L could be extracted from D.numAttributes() here.
	 */
	public static Instances addZtoD(Instances D, double Z[][], int L) {

		// add attributes
		for(int a = 0; a < Z[0].length; a++) {
			D.insertAttributeAt(new Attribute("A"+a),L+a);
		}

		// add values Z[0]...Z[N] to D
		// (note that if D.numInstances() < Z.length, only some are added)
		for(int a = 0; a < Z[0].length; a++) {
			for(int i = 0; i < D.numInstances(); i++) {
				D.instance(i).setValue(L+a,Z[i][a]);
			}
		}
		D.setClassIndex(L);
		return D;
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

	public static void main(String args[]) {
		// NEED THIS FOR SOME SCRIPTS
		/*
		String p[] = permute(args[0]);
		int i = 0;
		for(String s: p) {
			System.out.println(""+(i++)+" "+s);
		}
		*/
		//System.out.println(""+Arrays.toString(invert(new int[]{1,2},6)));
		//System.out.println(""+Arrays.toString(invert(new int[]{0,2},6)));
		//System.out.println(""+Arrays.toString(invert(new int[]{5,2},6)));
	}

}
