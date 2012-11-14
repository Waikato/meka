package weka.classifiers.multilabel.meta;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.multilabel.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * Maps the output of a multi-label classifier to a known label combination using the hamming distance.
 * described in	``Improved Boosting Algorithms Using Confidence-rated Predictions'' by Schapire, Robert E. and Singer, Yoram  
 *
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class SubsetMapper extends MultilabelClassifier {

	protected HashMap<String,Integer> m_Count = new HashMap<String,Integer>();

    public double[] nearestSubset(double d[]) throws Exception {   

		String comb = MLUtils.toBitString(doubles2ints(d));

		// If combination exists
		if (m_Count.get(comb) != null) {
			return MLUtils.fromBitString(comb);
		}

        int closest_count = 0;
        int min_distance = Integer.MAX_VALUE;
		String nearest = comb;

        for(String current : shuffle(m_Count.keySet())) {   
            int distance = hammingDistance(current,comb);
            if (distance == min_distance) {   
                int count = m_Count.get(current);
                if (count > closest_count) {   
                    nearest = current;
                    closest_count = count;
                }
            }
            if (distance < min_distance) {   
                min_distance = distance;
                nearest = current;
                closest_count = m_Count.get(nearest);
            }
        }
		return MLUtils.fromBitString(nearest);
    }

	private Collection<String> shuffle(Set<String> labelSubsets)
    {
    	int seed = 1;
    	Vector<String> result = new Vector<String>(labelSubsets.size());
    	result.addAll(labelSubsets);
    	Collections.shuffle(result, new Random(seed));
    	return result;
    }

	public void buildClassifier(Instances D) throws Exception {

		for (int i = 0; i < D.numInstances(); i++) {
			m_Count.put(MLUtils.toBitString(D.instance(i),D.classIndex()),0);
		}

		m_Classifier.buildClassifier(D);

	}

	public double[] distributionForInstance(Instance TestInstance) throws Exception {

		double r[] = ((MultilabelClassifier)m_Classifier).distributionForInstance(TestInstance);

		return nearestSubset(r);
	}

	private static final int[] doubles2ints(double d[]) {
		int b[] = new int[d.length];
		for(int i = 0; i < d.length; i++) {
			b[i] = (int)Math.round(d[i]);
		}
		return b;
	}

	private static final int hammingDistance(String s1, String s2) {
		int dist = 0;
		for(int i = 0; i < Math.min(s1.length(),s2.length()); i++) {
			dist += Math.abs(MLUtils.char2int(s1.charAt(i)) - MLUtils.char2int(s2.charAt(i)));
		}
		return dist;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new SubsetMapper(),args);
	}

}
