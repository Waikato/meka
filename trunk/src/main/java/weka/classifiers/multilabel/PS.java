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

package weka.classifiers.multilabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MLUtils;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * PS.java - The Pruned Sets Method.
 * <br>
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes. <i>Multi-label Classification using Ensembles of Pruned Sets</i>. Proc. of IEEE International Conference on Data Mining (ICDM 2008), Pisa, Italy, 2008
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class PS extends LC implements Randomizable {

	/** for serialization. */
	private static final long serialVersionUID = 8943667795912487237L;
	
	protected int m_P = 0; 
	protected int m_N = 0;
	protected String m_sP = String.valueOf(m_P);
	protected String m_sN = String.valueOf(m_N);
	protected int m_S = 1;

	private int parseValue(String s) {
		int i = s.indexOf('-');
		Random m_R = new Random(m_S);
		if(i > 0 && i < s.length()) {
			int lo = Integer.parseInt(s.substring(0,i));
			int hi  = Integer.parseInt(s.substring(i+1,s.length()));
			return lo + m_R.nextInt(hi-lo+1);
		}
		else
			return Integer.parseInt(s);
	}

	public int getP() {
		return m_P;
	}

	public void setP(int p) {
		m_P = p;
	}

	public int getN() {
		return m_N;
	}

	public void setN(int n) {
		m_N = n;
	}

	// For Random P/N values
	@Override
	public void setSeed(int s) {
		// set random P / N values here (used by, e.g., EnsembleML)
		m_S = s;
		if (m_sP != null)
			m_P = parseValue(m_sP);
		if (m_sN != null)
			m_N = parseValue(m_sN);
		if (getDebug()) {
			System.out.println("P = "+m_P);
			System.out.println("N = "+m_N);
		}
	}

	@Override
	public int getSeed() {
		return m_S;
	}

	public String seedTipText() {
	  return "The seed value for randomization.";
	}
	
	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the pruning value        \n\tdefault: "+m_P+"\t(off)", "P", 1, "-P <value>"));
		newVector.addElement(new Option("\tSets the subsampling strategy \n\tdefault: "+m_N+"\t(off)\n\tn\tN = n\n\t-n\tN = n, or 0 if LCard(D) >= 2\n\tn-m\tN = random(n,m)", "N", 1, "-N <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		try {
			m_sP = Utils.getOption('P', options);
			setP(parseValue(m_sP));
		} catch(Exception e) {
			m_sP = String.valueOf(getP());
			if(getDebug()) System.err.println("Using default P = "+getP());
		}

		try {
			m_sN = Utils.getOption('N', options);
			setN(parseValue(m_sN));
		} catch(Exception e) {
			m_sN = String.valueOf(m_N);
			if(getDebug()) System.err.println("Using default N = "+getN());
		}

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 4];
		int current = 0;
		options[current++] = "-P";
		options[current++] = "" + m_P;
		options[current++] = "-N";
		options[current++] = "" + m_N;
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;

	}

	@Override
	public Instance convertInstance(Instance x, int L) {
		Instance x_sl = (Instance) x.copy(); 
		x_sl.setDataset(null);
		for (int i = 0; i < L; i++)
			x_sl.deleteAttributeAt(0);
		x_sl.insertAttributeAt(0);
		x_sl.setDataset(m_InstancesTemplate);
		return x_sl;
	}

	public Instances convertInstances(Instances D, int L) throws Exception {

		//Gather combinations
		HashMap<String,Integer> distinctCombinations = MLUtils.countCombinations(D,L);

		//Prune combinations
		MLUtils.pruneCountHashMap(distinctCombinations,m_P);

		//Create class attribute
		FastVector ClassValues = new FastVector(L);
		for(String y : distinctCombinations.keySet()) 
			ClassValues.addElement(y);
		Attribute NewClass = new Attribute("Class", ClassValues);

		//Filter Remove all class attributes
		Remove FilterRemove = new Remove();
		FilterRemove.setAttributeIndices("1-"+L);
		FilterRemove.setInputFormat(D);
		Instances NewTrain = Filter.useFilter(D, FilterRemove);

		//Insert new special attribute (which has all possible combinations of labels) 
		NewTrain.insertAttributeAt(NewClass,0);
		NewTrain.setClassIndex(0);

		//Add class values
		for (int i = 0; i < D.numInstances(); i++) {

			String comb = MLUtils.toBitString(D.instance(i),L);
			// add it
			if(ClassValues.contains(comb)) 	//if its class value exists
				NewTrain.instance(i).setClassValue(comb);
			// decomp
			else if(m_N > 0) { 
				String d_subsets[] = getTopNSubsets(comb,distinctCombinations,m_N);
				//System.out.println("decomp: "+d_subsets.length);
				for (String s : d_subsets) {
					//===copy===(from I=0)
					Instance copy = (Instance)(NewTrain.instance(i)).copy();
					//===assign===(the class)
					copy.setClassValue(s);
					//===add===(to the end)
					NewTrain.add(copy);
					//===remove so we can't choose this subset again!
				}
			}
		}

		// remove with missing class
		NewTrain.deleteWithMissingClass();

		// keep the header of new dataset for classification
		m_InstancesTemplate = new Instances(NewTrain, 0);

		return NewTrain;
	}


	public static String[] getTopNSubsets(String comb, HashMap <String,Integer>all, int n) {
		ArrayList<String> subsets = new ArrayList<String>();  
		// add
		for(String s : all.keySet()) {
			if(isSubsetOf(s,comb)) {
				subsets.add(s);
			}
		}
		// rank
		Collections.sort(subsets,new LabelSet(all));
		String s[] = subsets.toArray(new String[subsets.size()]);

		return Arrays.copyOf(s,Math.min(n,s.length));
	}

	private static class LabelSet implements Comparator {

		HashMap<String,Integer> c = null;

		public LabelSet(HashMap<String,Integer> c) {
			this.c = c;
		} 

		public int compare(Object obj1, Object obj2) {

			String s1 = (String) obj1;
			String s2 = (String) obj2;


			if (MLUtils.bitCount(s1) > MLUtils.bitCount(s2))  {
				return -1;
			}
			if (MLUtils.bitCount(s1) < MLUtils.bitCount(s2)) {
				return 1;
			}
			else {
				if (c.get(s1) > c.get(s2)) {
					return -1;
				}
				if (c.get(s1) < c.get(s2)) {
					return 1;
				}
				else {
					// @todo: could add further conditions
					return 0;
				}
			} 
		} 
	} 

	// if a is a subset of b
	private static boolean isSubsetOf(String a, String b) {
		int m = Math.min(a.length(),b.length());
		for(int i = 0; i < m; i++) {
			if(a.charAt(i) == '1')
				if(b.charAt(i) != '1')
					return false;
		}
		return true;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		int L = D.classIndex();

		// Check N
		if (m_N < 0) {
			double lc = MLUtils.labelCardinality(D,L);
			if (lc > 2.0) 
				m_N = 0;
			else 
				m_N = Math.abs(m_N);
			m_sN = String.valueOf(m_N);
			System.err.println("N set to "+m_N);
		}

		// Convert
		Instances NewTrain = convertInstances(D,L);

		// Info
		if(getDebug()) System.out.println("("+m_InstancesTemplate.attribute(0).numValues()+" classes, "+NewTrain.numInstances()+" ins. )");

		// Build
		m_Classifier.buildClassifier(NewTrain);

	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new PS(),args);
	}

}
