package weka.core;

import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import weka.core.*;
import java.util.*;

public abstract class MLUtils {

	//Look for options in the @relation name: in format 'dataset-name: options'
	public static final String[] getDatasetOptions(Instances instances) {
		String name = instances.relationName();
		if(name.indexOf(':') > 0) {
			return name.substring(name.indexOf(':')+1).split(" ");
		}
		else return null;
	}

	//Look for dataset name in the @relation name: in format 'dataset-name: options'
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


	// Shuffle an array given 'r'
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

	// may not need this
	public static final String toBitString(int i[]) {
		StringBuilder sb = new StringBuilder(i.length);  
		for (int b : i) {
			sb.append(b);
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

	// Label Cardinality.
	public static final double labelCardinality(Instances instances) {
		int sum = 0;
		for(int i = 0; i < instances.numInstances(); i++) {
			sum += bitCount(instances.instance(i).stringValue(0));
		}
		return (double)sum/(double)instances.numInstances();
	}

	// LabelCardinality. Assuming binary format.
	public static final double labelCardinality(Instances instances, int N) {
		double sum = 0.0;
		for(int i = 0; i < instances.numInstances(); i++) {
			for(int j = 0; j < N; j++) {
				sum += instances.instance(i).value(j);
			}
		}
		return (double)sum/(double)instances.numInstances();
	}

	// Most Common Combination. Assuming binary format.
	public static final String mostCommonCombination(Instances instances, int N) {
		HashMap<String,Integer> hm = new HashMap<String,Integer>(instances.numInstances());
		double max_v  = 0.0;
		int max_i = 0;

		for(int i = 0; i < instances.numInstances(); i++) {
			String comb = MLUtils.toBitString(instances.instance(i),N);

			Integer v = hm.get(comb);
			if (v == null) {
				hm.put(comb,0); 
			} else {
				if (v > max_v) {
					max_v = v;
					max_i = i;
				}
				hm.put(comb,v+1);
			}

		}

		return MLUtils.toBitString(instances.instance(max_i),N);
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

	public static final Count countCombinations(Instances instances, int numClasses) {
		Count distinctCombinations = new Count();  
		for (int i = 0; i < instances.numInstances(); i++) {
			String combo = MLUtils.toBitString(instances.instance(i),numClasses);
			distinctCombinations.add(combo); 
		}
		return distinctCombinations;
	}

	/**
	 * Move label attributes from END to BEGINNING of attribute space. 
	 */
	public static final Instances switchAttributes(Instances D, int L) {
		System.out.println("Switching attributes: ");
		int d = D.numAttributes();
		for(int j = 0; j < L; j++) {
			System.out.print(".");
			D.insertAttributeAt(D.attribute(d-1).copy(D.attribute(d-1).name()+"-"),0);
			for(int i = 0; i < D.numInstances(); i++) {
				D.instance(i).setValue(0,D.instance(i).value(d));
			}
			D.deleteAttributeAt(d);
		}
		System.out.println("");
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

	private static final int[] invert(int indices[], int L) {
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

	public static void main(String args[]) {
		System.out.println(""+Arrays.toString(invert(new int[]{1,2},6)));
		System.out.println(""+Arrays.toString(invert(new int[]{0,2},6)));
		System.out.println(""+Arrays.toString(invert(new int[]{5,2},6)));
	}




}
