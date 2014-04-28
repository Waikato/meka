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

package meka.classifiers.multilabel;

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
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import meka.core.PSUtils;
import meka.core.MLUtils;
import meka.core.LabelSet;

/**
 * PSe.java - The Pruned Sets Method extended -- faster, uses sparse LabelSets, better OOP.
 * I keep PS.java for now because it reproduces exactly the results of old papers. There are minor differences (probably on account of internal randomness, different set orderings, etc) in PSe -- although they should not be statistically significant.
 * @see PS.java
 * <br>
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes. <i>Multi-label Classification using Ensembles of Pruned Sets</i>. Proc. of IEEE International Conference on Data Mining (ICDM 2008), Pisa, Italy, 2008
 * @version	April 2014
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class PSe extends LC implements Randomizable, TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = 8943667795912487237L;
	
	protected int m_P = 0; 
	protected int m_N = 0;
	protected String m_sP = String.valueOf(m_P);
	protected String m_sN = String.valueOf(m_N);
	protected int m_S = 1;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"The Pruned Sets method (PS).\n"
				+ "Removes examples with P-infrequent labelsets from the training data, then subsamples these labelsets N time to produce N new examples with P-frequent labelsets. Then train a standard LC classifier. The idea is to reduce the number of unique class values that would otherwise need to be learned by LC. Best used in an Ensemble (e.g., EnsembleML).\n"
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Jesse Read, Bernhard Pfahringer, Geoff Holmes");
		result.setValue(Field.TITLE, "Multi-label Classification Using Ensembles of Pruned Sets");
		result.setValue(Field.BOOKTITLE, "ICDM'08: International Conference on Data Mining (ICDM 2008). Pisa, Italy.");
		result.setValue(Field.YEAR, "2008");
		
		return result;
	}

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

	/** 
	 * GetP - Get the pruning value P.
	 */
	public int getP() {
		return m_P;
	}

	/** 
	 * SetP - Sets the pruning value P, defining an infrequent labelset as one which occurs less than P times in the data (P = 0 defaults to LC).
	 */
	public void setP(int p) {
		m_P = p;
	}

	/** 
	 * GetN - Get the subsampling value N.
	 */
	public int getN() {
		return m_N;
	}

	/** 
	 * SetN - Sets the subsampling value N, the (maximum) number of frequent labelsets to subsample from the infrequent labelsets.
	 */
	public void setN(int n) {
		m_N = n;
	}

	/** 
	 * SetSeed - Use random P and N values (in this case P and N arguments determine a <i>range</i> of values to select from randomly, e.g., -P 1-5 selects P randomly in {1,2,3,4,5}.
	 */
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
	
	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the pruning value, defining an infrequent labelset as one which occurs <= P times in the data (P = 0 defaults to LC).\n\tdefault: "+m_P+"\t(LC)", "P", 1, "-P <value>"));
		newVector.addElement(new Option("\tSets the (maximum) number of frequent labelsets to subsample from the infrequent labelsets.\n\tdefault: "+m_N+"\t(none)\n\tn\tN = n\n\t-n\tN = n, or 0 if LCard(D) >= 2\n\tn-m\tN = random(n,m)", "N", 1, "-N <value>"));

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

	public Instances convertInstances(Instances D, int L) throws Exception {

		//Gather combinations
		HashMap<LabelSet,Integer> distinctCombinations = PSUtils.countCombinationsSparse(D,L);

		//Prune combinations
		MLUtils.pruneCountHashMap(distinctCombinations,m_P);

		//Create class attribute
		FastVector ClassValues = new FastVector(L);
		for(LabelSet y : distinctCombinations.keySet()) 
			ClassValues.addElement(y.toString());
		Attribute NewClass = new Attribute("Class", ClassValues);

		//Filter Remove all class attributes
		Remove FilterRemove = new Remove();
		FilterRemove.setAttributeIndices("1-"+L);
		FilterRemove.setInputFormat(D);
		Instances D_ = Filter.useFilter(D, FilterRemove);

		//Insert new special attribute (which has all possible combinations of labels) 
		D_.insertAttributeAt(NewClass,0);
		D_.setClassIndex(0);

		//Add class values
		for (int i = 0; i < D.numInstances(); i++) {
			Instance x = D.instance(i);
			LabelSet y = new LabelSet(MLUtils.toSparseIntArray(x,L));
			String y_string = y.toString();

			// add it
			if(ClassValues.contains(y_string)) 	//if its class value exists
				D_.instance(i).setClassValue(y_string);
			// decomp
			else if(m_N > 0) { 
				//String d_subsets[] = getTopNSubsets(comb,distinctCombinations,m_N);
				LabelSet d_subsets[] = PSUtils.getTopNSubsets(y,distinctCombinations,m_N);
				//LabelSet d_subsets[] = PSUtils.cover(y,distinctCombinations);
				//System.out.println("decomp: "+d_subsets.length);
				for (LabelSet s : d_subsets) {
					//===copy===(from I=0)
					Instance x_ = (Instance)(D_.instance(i)).copy();
					//===assign===(the class)
					x_.setClassValue(s.toString());
					//===add===(to the end)
					D_.add(x_);
					//===remove so we can't choose this subset again!
				}
			}
		}

		// remove with missing class
		D_.deleteWithMissingClass();

		// keep the header of new dataset for classification
		m_InstancesTemplate = new Instances(D_, 0);

		return D_;
	}

	private static final double[] toDoubleArray(String labelSet, int L) {

		int set[] = (labelSet.length() <= 2) ? new int[]{} : MLUtils.toIntArray(labelSet);
		//StringBuffer y = new StringBuffer(L);
		double y[] = new double[L];
		//for(int j = 0; j < L; j++) {
		//	y.append("0");
		//}
		for(int j : set) {
			//y.setCharAt(j,'1');
			y[j] = 1.;
		}
		return y;
		//return y.toString();
	}

	/**
	 * Convert Distribution - to be deprecated.
	 * @NOTE THAT WE WON'T NEED THIS WHEN WE UPGRADE LC.java TO HAVE LabelSets INSTEAD OF Strings.
	 * @TODO use PSUtils.recombination(p,L,map)
	 * @TODO use PSUtils.recombination_t(p,L,map) for PSt
	 */
	public double[] convertDistribution(double p[], int L) {
		
		double y[] = new double[L];

		int i = Utils.maxIndex(p);

		double d[] = toDoubleArray(m_InstancesTemplate.classAttribute().value(i),L);

		for(int j = 0; j < d.length; j++) {
			if(d[j] > 0.0)
				y[j] = 1.0;
		}

		return y;
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
		Instances D_ = convertInstances(D,L);

		// Info
		if(getDebug()) System.out.println("("+m_InstancesTemplate.attribute(0).numValues()+" classes, "+D_.numInstances()+" ins. )");

		// Build
		m_Classifier.buildClassifier(D_);

	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new PSe(),args);
	}

}
