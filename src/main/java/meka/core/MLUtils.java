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
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;

import java.io.*;
import java.util.*;

/**
 * MLUtils - Helpful functions for dealing with multi-labelled data.
 * Note that there are some similar/related methods in F.java.
 * @see MLUtils
 * @author Jesse Read 
 * @version	March 2013
 */
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

	/**
	 * DEPRECATED - use A.make_sequence(L) instead.
	 */
	@Deprecated
	public static final int[] gen_indices(int L) {
		return A.make_sequence(L);
	}


	/**
	 * DEPRECATED - use A.shuffle(array,r) instead.
	 */
	@Deprecated
	public static final void randomize(int array[], Random r) {
		A.shuffle(array,r);
	}

	/**
	 * Instance with L labels to double[] of length L.
	 * Rounds to the nearest whole number.
	 */
	public static final double[] toDoubleArray(Instance x, int L) {
		double a[] = new double[L];
		for(int i = 0; i < L; i++) {
			a[i] = Math.round(x.value(i));
		}
		return a;
	}

	/**
	 * Instance with L labels to double[] of length L, where L = x.classIndex().
	 * Rounds to the nearest whole number.
	 */
	public static final double[] toDoubleArray(Instance x) {
		int L = x.classIndex();
		return toDoubleArray(x,L);
	}

	/**
	 * ToBitString - returns a String representation of x = [0,0,1,0,1,0,0,0], e.g., "000101000".
	 * NOTE: It may be better to use a sparse representation for some applications.
	 */
	public static final String toBitString(Instance x, int L) {
		StringBuilder sb = new StringBuilder(L);  
		for(int i = 0; i < L; i++) {
			sb.append((int)Math.round(x.value(i)));
		}
		return sb.toString();
	}

	/**
	 * ToBitString - returns a String representation of i[].
	 */
	public static final String toBitString(int i[]) {
		StringBuilder sb = new StringBuilder(i.length);  
		for (int b : i) {
			sb.append(b);
		}
		return sb.toString();
	}

	/**
	 * ToBitString - returns a String representation of d[].
	 */
	public static final String toBitString(double d[]) {
		StringBuilder sb = new StringBuilder(d.length);  
		for (double b : d) {
			sb.append((int)Math.round(b));
		}
		return sb.toString();
	}

	/**
	 * FromBitString - returns a double[] representation of s.
	 */
	public static final double[] fromBitString(String s) {
		char a[] = s.toCharArray();
		double d[] = new double[a.length];
		for(int i = 0; i < a.length; i++) {
			d[i] = (double)char2int(a[i]);
		}
		return d;
	}

	/** ToIntArray - Return an int[] from a String, e.g., "[0,1,2,0]" to [0,1,2,3]. */
	public static final int[] toIntArray(String s) {
		s = new String(s.trim());
		return toIntArray((s.substring(1,s.length()-1)).split(","));
	}

	/** ToIntArray - Return an int[] from a String[], e.g., ["0","1","2","3"] to [0,1,2,3]. */
	public static final int[] toIntArray(String s[]) {
		int y[] = new int[s.length];
		for(int j = 0; j < s.length; j++) {
			y[j] = Integer.parseInt(s[j].trim());
		}
		return y;
	}

	/** 
	 * Convert to Weka (multi-target) Predictions. 
	 * Note: currently only multi-label.
	 * */
	public static ArrayList<Prediction> toWekaPredictions(int y[], double p[]) {
		ArrayList<Prediction> predictions = new ArrayList<Prediction>();
		for(int i = 0; i < y.length; i++) {
			predictions.add(new NominalPrediction((double)y[i], new double[]{1.-p[i],p[i]}));
		}
		return predictions;
	}

	/**
	 * To Sub Indices Set - return the indices out of 'sub_indices', in x, whose values are greater than 1.
	 */
	public static final List toSubIndicesSet(Instance x, int sub_indices[]) {
		List<Integer> y_list = new ArrayList<Integer>();
		for(int j : sub_indices) {
			if (x.value(j) > 0.) {
				y_list.add(j);
			}
		}
		return y_list;
	}

	/**
	 * To Indices Set - return the indices in x[], whose values are greater than t, e.g., [0.3,0.0,0.5,0.8],0.4 to {2,3}.
	 */
	public static final List toIndicesSet(double x[], double t) {
		List<Integer> y_list = new ArrayList<Integer>();
		for(int j = 0; j < x.length; j++) {
			if (x[j] > t) {
				y_list.add(j);
			}
		}
		return y_list;
	}

	/** 
	 * To Indices Set - return the indices in x[], whose values are greater than 0, e.g., [0,0,1,1] to {2,3}.
	 */
	public static final List toIndicesSet(int x[]) {
		List<Integer> y_list = new ArrayList<Integer>();
		for(int j = 0; j < x.length; j++) {
			if (x[j] > 0) {
				y_list.add(j);
			}
		}
		return y_list;
	}

	/**
	 * To Indices Set - return the indices in x, whose values are greater than 1.
	 */
	public static final List<Integer> toIndicesSet(Instance x, int L) {
		List<Integer> y_list = new ArrayList<Integer>();
		for(int j = 0; j < L; j++) {
			if (x.value(j) > 0.) {
				y_list.add(j);
			}
		}
		return y_list;
	}

	/**
	 * To Sparse Int Array - A sparse String representation, e.g., [1,34,73]. 
	 * Only returns indices greater than 0 (not necessarily multi-target generic!)
	 */
	public static final int[] toSparseIntArray(Instance x, int L) {
		return A.toPrimitive(toIndicesSet(x,L));
	}

	/**
	 * From Sparse String - From a sparse String representation, e.g., [1,34,73], to a binary int[] where those indices are set to 1. 
	 */
	public static final int[] fromSparseString(String s) {
		return toIntArray(s.split(","));
	}


	/** 
	 * ToIntArray - raw instance to int[] representation
	 */
	public static final int[] toIntArray(Instance x, int L) {
		int y[] = new int[L];
		for(int j = 0; j < L; j++) {
			y[j] = (int)Math.round(x.value(j));
		}
		return y;
	}

	// @see also M.threshold(z,t)
	@Deprecated
	/** Use A.toIntArray(z,t) instead */
	public static final int[] toIntArray(double z[], double t) {
		return A.toIntArray(z,t);
	}

	/** To Double Arary - Convert something like "[1.0,2.0]" to [1.0,2.0] */
	public static final double[] toDoubleArray(String s) {
		s = new String(s.trim());
		return toDoubleArray((s.substring(1,s.length()-1)).split(","));
	}

	/** To Double Arary - Convert something like ["1.0","2.0"] to [1.0,2.0] */
	public static final double[] toDoubleArray(String s[]) {
		double y[] = new double[s.length];
		for(int j = 0; j < s.length; j++) {
			y[j] = Double.parseDouble(s[j].trim());
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
	 * LabelCardinality - return the average number of times the j-th label is relevant in label data Y.
	 */
	public static final double labelCardinality(int Y[][], int j) {
		int N = Y.length;
		int L = Y[0].length;
		double sum = 0.0;
		for(int i = 0; i < N; i++) {
			sum += Y[i][j];
		}
		return (double)sum/(double)N;
	}

	/** 
	 * LabelCardinality - return the label cardinality of label data Y.
	 * TODO: move to Metrics.java ? / Use M.sum(Y)/N
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

	/**
	 * MostCommonCombination -  Most common label combination in D.
	 */
	public static final String mostCommonCombination(Instances D) {
		return mostCommonCombination(D,D.classIndex());
	}

	/**
	 * MostCommonCombination -  Most common label combination in D (of L labels).
	 */
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

	/**
	 * CountCombinations - return a mapping of each distinct label combination and its count.
	 * NOTE: A sparse representation would be much better for many applications, i.e., instead of using toBitString(...), use toSparseRepresentation(...) instead.
	 * @param	D	dataset 
	 * @param	L	number of labels
	 * @return	a HashMap where a String representation of each label combination is associated with an Integer count, e.g., "00010010",3
	 */
	public static final HashMap<String,Integer> countCombinations(Instances D, int L) {
		HashMap<String,Integer> map = new HashMap<String,Integer>();  
		for (int i = 0; i < D.numInstances(); i++) {
			//String y = MLUtils.toSparseRepresentation(D.instance(i),L);
			String y = MLUtils.toBitString(D.instance(i),L);
			Integer c = map.get(y);
			map.put(y,c == null ? 1 : c+1);
		}
		return map;
	}

	/**
	 * CountCombinations in a sparse way.
	 * @see		MLUtils#countCombinations(Instances,int)
	 * @param	D	dataset 
	 * @param	L	number of labels
	 * @return	a HashMap where a String representation of each label combination is associated with an Integer count, e.g., "00010010",3
	 */
	public static final HashMap<LabelSet,Integer> countCombinationsSparse(Instances D, int L) {
		return PSUtils.countCombinationsSparse(D,L);
	}

	/**
	 * ClassCombinationCounts - multi-target version of countCombinations(...).
	 * NOTE: uses the encodeValue(...) function which does NOT consider sparse data.
	 * TODO: use LabelVector instead of Strings
	 * @param	D	dataset 
	 * @return	a HashMap where a String representation of each class combination is associated with an Integer count, e.g. [0,2,2,3,2],5
	 */
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

	/**
	 * Encode Value.
	 * [0,3,2] -&gt; "0+3+2"
	 * Deprecated - Use LabelSet or LabelVector
	 */
	@Deprecated
	public static String encodeValue(int s[]) {
		StringBuilder sb = new StringBuilder(String.valueOf(s[0]));  
		for(int i = 1; i < s.length; i++) {
			sb.append('+').append(s[i]);
		}
		return sb.toString();
	}

	/**
	 * Encode Value.
	 * "0+3+2"-&gt; [0,3,2]
	 * Deprecated - Use LabelSet or LabelVector
	 */
	@Deprecated
	public static int[] decodeValue(String a) {
		return toIntArray(a.split("\\+"));
	}

	/**
	 * maxItem - argmax function for a HashMap
	 * @return 	argmax_k map.get(k)
	 */
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

	/**
	 * maxItem - argmax function for a HashMap
	 * NOTE: same as above, but for integer
	 * (TODO: do something more clever than this)
	 */
	public static final Object argmax(HashMap<?,Integer> map) {
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

	/** Get the number of unique label combinations in a dataset */
	public static final int numberOfUniqueCombinations(Instances D) {
		HashMap<String,Integer> hm = classCombinationCounts(D);
		return hm.size();
	}

	// 
	// ***NOTE*** The following functions are present in F.java in the form of removeLabels/keepLabels, but using the Remove() filter.
	// I will keep these ones here for now in case they are faster or have some other advantage. Otherwise they can be deleted, and
	// use F.java instead.
	// 

	/**
	 * Delete attributes from an instance 'x' indexed by 'indicesToRemove[]'.
	 * @param	x					instance
	 * @param	indicesToRemove		array of attribute indices
	 * @return	the modified dataset
	 */
	public static final Instance deleteAttributesAt(Instance x, int indicesToRemove[]) {//, boolean keep) {
		Arrays.sort(indicesToRemove);
		for(int j = indicesToRemove.length-1; j >= 0; j--) {
			x.deleteAttributeAt(indicesToRemove[j]);
		}
		return x;
	}

	/**
	 * Delete all attributes from an instance 'x' <i>except</i> those indexed by 'indicesToRemove[]', up to the 'lim'-th attribute.
	 * For example, lim = 10, indicesToRemove[] = {4,5}, keeps indices 4,5,10,11,12,...
	 * @param	x					instance
	 * @param	indicesToRemove		array of attribute indices
	 * @param	lim					excluding 
	 * @return	the modified dataset
	 */
	public static final Instance keepAttributesAt(Instance x, int indicesToRemove[], int lim){
		return deleteAttributesAt(x, A.invert(indicesToRemove, lim));
	}

	/**
	 * Delete attributes from a dataset 'D' indexed by 'indicesToRemove[]'.
	 * @param	D					dataset
	 * @param	indicesToRemove  	array of attribute indices
	 * @return	the modified dataset
	 */
	public static final Instances deleteAttributesAt(Instances D, int indicesToRemove[]) {//, boolean keep) {
		Arrays.sort(indicesToRemove);
		for(int j = indicesToRemove.length-1; j >= 0; j--) {
			D.deleteAttributeAt(indicesToRemove[j]);
		}
		return D;
	}

	/**
	 * Delete all attributes from a dataset 'D' <i>except</i> those indexed by 'indicesToRemove[]', up to the 'lim'-th attribute.
	 * For example, lim = 10, indicesToRemove[] = {4,5}, keeps indices 4,5,10,11,12,...
	 * @param	D					dataset
	 * @param	indicesToRemove  	array of attribute indices
	 * @param	lim					excluding 
	 * @return	the modified dataset
	 */
	public static final Instances keepAttributesAt(Instances D, int indicesToRemove[], int lim){
		return deleteAttributesAt(D, A.invert(indicesToRemove, lim));
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
	 * CopyValues - Set x_dest[i++] = x_src[j] for all j in indices[].
	 */
	public static final Instance copyValues(Instance x_dest, Instance x_src, int indices[]) {
		int i = 0;
		for(int j : indices) {
			x_dest.setValue(i++,x_src.value(j));
		}
		return x_dest;
	}

	/**
	 * SetValues - set the attribute values in Instsance x (having L labels) to z[].
	 * TODO: call above method
	 */
	public static final Instance setValues(Instance x, double z[], int L) {
		for(int a = 0; a < z.length; a++) {
			x.setValue(L+a,z[a]);
		}
		return x;
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

	/**
	 * PruneCountHashMap - remove entries in hm = {(label,count)} where 'count' is no more than 'p'.
	 */
	public static void pruneCountHashMap(HashMap<?,Integer> hm, int p) {
		ArrayList removeList = new ArrayList();  
		for (Object obj : hm.keySet()) {
			if(hm.get(obj) <= p) {
				removeList.add(obj);
			}
		}
		for (Object obj : removeList) {
			hm.remove(obj);
		}
		removeList.clear();
		removeList = null;
	}

	// assume that no hm.get(.) > N
	public static HashMap<?,Integer> pruneCountHashMapBasedAsAFractionOf(HashMap<?,Integer> hm, double p, int N) {
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
		return hm;
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

	/**
	 * Stack two Instances together row-wise.
	 */
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

	public static int[] toPrimitive(Integer a[]) {
		int b[] = new int[a.length];
		for(int i = 0; i < a.length; i++) {
			b[i] = a[i];
		}
		return b;
	}

	/**
	 * ToBinaryString - use to go through all 'L' binary combinations.
	 * @see 	A#toDoubleArray(int, int)
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

	/**
	 * Permute -- e.g., permute("AB") returns ["AB","BA"]
	 */
	public static String[] permute(String s) {
		ArrayList<String> a = new ArrayList<String>();  
		permute("", s, a);
		return a.toArray(new String[0]);
	}

	/**
	 * HashMapToString - print out a HashMap nicely.
	 * @param	map	HashMap
	 * @param	dp	decimal point precision (-1 for no limitation)
	 * @return	String representation of map
	 */
	public static String hashMapToString(HashMap<?,?> map, int dp) {
		StringBuilder sb = new StringBuilder();
		for (Object k : map.keySet()) {
			sb.append(Utils.padRight(k.toString(),31));
			Object obj = map.get(k);
			//sb.append(" : ");
			if (obj instanceof Double) {
				sb.append(Utils.doubleToString((Double)obj,5,dp));
			}
			else if (obj instanceof double[]) {
				sb.append(A.toString((double[])obj,dp));
			}
			else if (obj instanceof int[]) {
				sb.append(A.toString((int[])obj,dp+2));
			}
			else if (obj instanceof String) {
				sb.append(obj);
			}
			else {
				// don't append if we don't know what it is!
				//sb.append(obj);
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	public static String hashMapToString(HashMap<?,?> map) {
		return hashMapToString(map,-1);
	}

	/**
	 * GetIntegerOption - parse 'op' to an integer if we can, else used default 'def'.
	 */
	public static int getIntegerOption(String op, int def) {
		try {
			return Integer.parseInt(op);
		} catch(Exception e) {
			System.err.println("[Warning] Failed to parse "+op+" to integer number; using default of "+def);
			return def;
		}
	}

	/** Clear Labels -- set the value of all label attributes to 0.0 */
	public static void clearLabels(Instance x) {
		int L = x.classIndex();
		for(int j = 0; j < L; j++) 
			x.setValue(j,0.0);
	}

	/**
	 * GetXfromD - Extract attributes as a double X[][] from Instances D.
	 * TODO: getXfromInstances would be a better name.
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
	 * TODO: getYfromInstances would be a better name.
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
	 */
	public static double[] getxfromInstance(Instance xy) {
		int L = xy.classIndex();
		double xy_[] = xy.toDoubleArray();
		return Arrays.copyOfRange(xy_,L,xy_.length);
	}

	/**
	 * ReplaceZasAttributes - data Z[][] will be the new attributes in D.
	 * @param	D 	dataset (of N instances)
	 * @param	Z	attribute space (of N rows, H columns)
	 * @param	L	number of classes / labels.
	 */
	public static Instances replaceZasAttributes(Instances D, double Z[][], int L) {
		D.setClassIndex(0);
		int m = D.numAttributes()-L;
		for(int j = 0; j < m; j++) {
			D.deleteAttributeAt(L);
		}
		return addZtoD(D, Z, L);
	}

	/**
	 * ReplaceZasClasses - data Z[][] will be the new class labels in D.
	 * @param	D 	dataset (of N instances)
	 * @param	Z	attribute space (of N rows, H columns)
	 * @param	L	column to add Z from in D
	 */
	public static Instances replaceZasClasses(Instances D, double Z[][], int L) {

		D.setClassIndex(-1);

		for(int j = 0; j < L; j++) {
			D.deleteAttributeAt(0);
		}
		return insertZintoD(D, Z);
	}

	/**
	 * InsertZintoD - Insert data Z[][] to Instances D (e.g., as labels).
	 * NOTE: Assumes binary labels!
	 * @see #addZtoD(Instances, double[][], int)
	 */
	private static Instances insertZintoD(Instances D, double Z[][]) {

		int L = Z[0].length;

		// add attributes
		for(int j = 0; j < L; j++) {
			D.insertAttributeAt(new Attribute("c"+j,Arrays.asList(new String[]{"0","1"})),j);
		}

		// add values Z[0]...Z[N] to D
		// (note that if D.numInstances() < Z.length, only some are added)
		for(int j = 0; j < L; j++) {
			for(int i = 0; i < D.numInstances(); i++) {
				D.instance(i).setValue(j,Z[i][j] > 0.5 ? 1.0 : 0.0);
			}
		}

		D.setClassIndex(L);
		return D;
	}

	/**
	 * AddZtoD - Add attribute space Z[N][H] (N rows of H columns) to Instances D, which should have N rows also.
	 * @param	D 	dataset (of N instances)
	 * @param	Z	attribute space (of N rows, H columns)
	 * @param	L	column to add Z from in D
	 */
	private static Instances addZtoD(Instances D, double Z[][], int L) {

		int H = Z[0].length;
		int N = D.numInstances();

		// add attributes
		for(int a = 0; a < H; a++) {
			D.insertAttributeAt(new Attribute("A"+a),L+a);
		}

		// add values Z[0]...Z[N] to D
		for(int a = 0; a < H; a++) {
			for(int i = 0; i < N; i++) {
				D.instance(i).setValue(L+a,Z[i][a]);
			}
		}

		D.setClassIndex(L);
		return D;
	}

	/**
	 * Get K - get the number of values associated with each label L.
	 * @param	D 	a dataset
	 * @return	a vector of size L: K_1,...,K_L
	 */
	public int[] getK(Instances D) {
		int L = D.classIndex();
		HashSet counts[] = new HashSet[L];
		int K[] = new int[L];
		for(int j = 0; j < L; j++) {
			counts[j] = new HashSet<Integer>();
			for(Instance x : D) {
				int k = (int)x.value(j);
				counts[j].add(k);
			}
			K[j] = counts[j].size();
			/*
			   System.out.println(""+j+" = "+counts[j]);
			   if (counts[j].size() < 2) {
			   System.out.println("OK, this is a problem ...");
			//System.exit(1);
			   }
			   */
		}
		return K;
	}

	/**
	 * Load Object - load the Object stored in 'filename'.
	 */
	public static final Object loadObject(String filename) throws Exception {
		FileInputStream streamIn = new FileInputStream(filename);
		ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
		Object object = objectinputstream.readObject();
		objectinputstream.close();
		return object;
	}

	/**
	 * Save Object - save 'object' into file 'filename'.
	 */
	public static final void saveObject(Object object, String filename) throws Exception {
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(object);
		oos.flush();
		oos.close();
	}

	/**
	 * Fixes the relation name by adding the "-C" attribute to it if necessary.
	 *
	 * @param data the dataset to fix
	 */
	public static void fixRelationName(Instances data) {
		fixRelationName(data, 0);
	}

	/**
	 * Fixes the relation name by adding the "-C" attribute to it if necessary.
	 *
	 * @param data the dataset to fix
	 * @param numClassAtts the number of class attributes (0 for none, &gt;0 for attributes at start, &lt;0 for attributes at end)
	 */
	public static void fixRelationName(Instances data, int numClassAtts) {
		if (data.relationName().indexOf(":") == -1)
			data.setRelationName(data.relationName() + ": -C " + numClassAtts);
	}

	/**
	 * Prepares the class index of the data.
	 * 
	 * @param data the data to prepare
	 * @throws Exception if preparation fails
	 */
	public static void prepareData(Instances data) throws Exception {
		String doptions[] = null;
		try {
			doptions = MLUtils.getDatasetOptions(data);
		} 
		catch(Exception e) {
			throw new Exception("[Error] Failed to Get Options from @Relation Name", e);
		}

		try {
			int c = (Utils.getOptionPos('C', doptions) >= 0) ? Integer.parseInt(Utils.getOption('C',doptions)) : Integer.parseInt(Utils.getOption('c',doptions));
			// if negative, then invert
			if ( c < 0) {
				c = -c;
				data = F.mulan2meka(data,c);
			}
			// set c
			data.setClassIndex(c);
		}
		catch (Exception e) {
			throw new Exception(
					"Failed to parse options stored in relation name; expected format for relation name:\n"
							+ "  'name: options'\n"
							+ "But found:\n"
							+ "  '" + data.relationName() + "'\n"
							+ "Format example:\n"
							+ "  'Example_Dataset: -C 3 -split-percentage 50'\n"
							+ "'-C 3' specifies the number of target attributes to be 3. See tutorial for more information.", 
							e);
		}
	}
	
	/**
	 * Attempts to determine the number of classes/class index from the 
	 * specified file. In case of ARFF files, only the header will get loaded.
	 * 
	 * @param file		the file to inspect
	 * @return			the class index of the file, Integer.MAX_VALUE in case of error
	 */
	public static int peekClassIndex(File file) {
		int			result;
		DataSource	source;
		Instances	structure;
		
		result = Integer.MAX_VALUE;
		
		try {
			source    = new DataSource(file.getAbsolutePath());
			structure = source.getStructure();
			prepareData(structure);
			result    = structure.classIndex();
		}
		catch (Exception e) {
			// ignored
		}
		
		return result;
	}

	/**
	 * For retrieving some dataset statistics on the command line.
	 * Note: -L, -d does not work for Mulan format (labels at the end)
	 */
	public static final void main (String args[]) throws Exception {

		/*
		 * If we are given an argument, load a file and extract some info and exit.
		 */
		if (args.length > 0) {

			//System.out.println("loading ...");
			Instances D = new Instances(new BufferedReader(new FileReader(args[0])));
			int N = D.numInstances();

			int L = Integer.parseInt(Utils.getOption('C',MLUtils.getDatasetOptions(D)));
			D.setClassIndex(L);

			switch(args[1].charAt(0)) {

				case 'L' :  System.out.println(L);							// return the number of labels of D
							break;
				case 'N' :  System.out.println(D.numInstances());			// return the number of Instances of D
							break;
				case 'd' :  System.out.println(D.numAttributes()-L);		// reurns the number of (non-label) attributes of D
							break;
				case 'A' :  System.out.println(D.numAttributes());			// returns the number of ALL attributes of D
							break;
				case 'l' :  System.out.println(MLUtils.labelCardinality(D));		// reurns the label cardinalities
							break;
				case 'P' :  System.out.println(Arrays.toString(MLUtils.labelCardinalities(D)));		// reurns the label cardinalities
							break;
				case 'C' :  System.out.println(hashMapToString(MLUtils.countCombinations(D,L)));		// counts
							break;
				case 'p' :  System.out.println("collecting ...");
							HashMap<LabelSet,Integer> hm = PSUtils.countCombinationsSparse(D,L);
							System.out.println("pruning ...");
							//MLUtils.pruneCountHashMap(hm,1);
							//System.out.println(""+hm);
							System.out.println("writing ...");
							saveObject(hm, "hm-NEW.serialized");
							break;
				default  : 	System.out.println(MLUtils.getDatasetName(D));	// returns the name of D
							break;
			}

			return;
		}
		/*
		 * Else, just do some tests ...
		 */
		else {

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
			return;
		}
	}

}
