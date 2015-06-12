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
 * PS.java - The Pruned Sets Method.
 * Removes examples with P-infrequent labelsets from the training data, then subsamples these labelsets N time to produce N new examples with P-frequent labelsets. Then train a standard LC classifier. The idea is to reduce the number of unique class values that would otherwise need to be learned by LC. Best used in an Ensemble (e.g., EnsembleML).<br>
 * This class was rewritten and extended to be faster, use sparse LabelSets, and better OOP. There may be minor variation in results (probably on account of internal randomness, different set orderings, etc) but this should not be statistically significant.
 * <br>
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes. <i>Multi-label Classification using Ensembles of Pruned Sets</i>. Proc. of IEEE International Conference on Data Mining (ICDM 2008), Pisa, Italy, 2008
 * @see LC
 * @version	April 2014
 * @author 	Jesse Read
 */
public class PS extends LC implements Randomizable, TechnicalInformationHandler {

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

		m_sP = Utils.getOption('P', options);
		if (m_sP.length() != 0)
			setP(parseValue(m_sP));
		else
			m_sP = String.valueOf(getP());

		m_sN = Utils.getOption('N', options);
		if (m_sN.length() != 0)
			setN(parseValue(m_sN));
		else
			m_sN = String.valueOf(m_N);

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

		// Transform
		Instances D_ = PSUtils.PSTransformation(D,L,m_P,m_N); 
		m_InstancesTemplate = new Instances(D_,0);

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
		MultilabelClassifier.evaluation(new PS(),args);
	}

}
