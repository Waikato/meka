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

import weka.classifiers.*;
import weka.classifiers.functions.*;
import meka.classifiers.multitarget.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.supervised.attribute.*;
import weka.core.TechnicalInformation.*;
import weka.attributeSelection.*;
import weka.filters.*;
import weka.core.*;
import meka.core.*;
import java.util.*;

/**
 * MCCe.java - CC with Monte Carlo optimisation. 
 * This is a cleaned up version of MCC ( @see MCC ) by extending CCe.
 * Note inference is now a bit slower than reported in the paper,
 * <br>
 * See: Jesse Read, Luca Martino, David Luengo. <i>Efficient Monte Carlo Optimization for Multi-dimensional Classifier Chains</i>. http://arxiv.org/abs/1211.2190. 2012
 * <br>
 * There we used a faster implementation, MCC.java, full of ugly hacks, but it got broken when I updated CCe.java.<br>
 * This version extends CCe, and thus is a bit more elegant, but for some reason inference is quite slower than expected with high m_Iy.
 *
 * @see meka.classifiers.multilabel.CCe
 * @author Jesse Read
 * @version	April 2014
 */
public class MCCe extends CCe implements TechnicalInformationHandler { 

	protected int m_Is = 0;
	protected int m_Iy = 10;
	protected int m_Payoff = 0;

	/**
	 * Payoff - Return a default score of h evaluated on D.
	 * sum ( p(y_i | x_i, h_s) )
	 * @TODO use meka.core.Metrics
	 */
	public double payoff(CCe h, Instances D) throws Exception {
		return payoff(h,D,m_Payoff);
	}

	/**
	 * Payoff - Return a score of choice (payoff_fn) of h evaluated on D.
	 * @TODO use meka.core.Metrics
	 */
	public double payoff(CCe h, Instances D, int payoff_fn) throws Exception {
		int L = D.classIndex();
		int N = D.numInstances();
		double s = 1.; // <-- @note!
		for(int i = 0; i < N; i++) {
			Instance x = D.instance(i);
			double y[] = MLUtils.toDoubleArray(x);			// y = [0 0 1 1]			<--- y_1,...,y_L
			double p[] = h.probabilityForInstance(x,y);     // p = [0.9 0.9 0.2 0.1]    <--- p(y_1),...,p(y_L)

			if (payoff_fn == 5) 		// SUM OF SUM
				s += A.sum(p);
			else if (payoff_fn == 2) 	// SUM OF LOG OF PRODUCT 
				s += Math.log(A.product(p));
			else // (payoff_fn == 0) 	// SUM OF PRODUCT
				s += A.product(p);

		}
		return s;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);

		// Variables

		int L = D.classIndex(); 
		int N = D.numInstances();
		int d = D.numAttributes()-L;
		m_R = new Random(m_S);

		int s[] = MLUtils.gen_indices(L); 
		MLUtils.randomize(s,m_R);
		if(getDebug()) System.out.println("s_[0] = "+Arrays.toString(s));

		// Make CC
		CCe h = CCUtils.buildCC(s,D,m_Classifier);

		// If we want to optimize the chain space ...
		if (m_Is > 0) {

			if (getDebug()) System.out.println("Optimising s ... ("+m_Is+" iterations):");

			double w = payoff(h,new Instances(D));
			if (getDebug()) System.out.println("h_{t="+0+"} := "+Arrays.toString(s)); //+"; w = "+w);

			for(int t = 0; t < m_Is; t++) {

				// propose a chain s' by swapping two elements in s
				int s_[] = Arrays.copyOf(A.swap(s,m_R),s.length);

				// build h'
				CCe h_ = CCUtils.buildCC(s_,D,m_Classifier);

				// rate h'
				double w_ = payoff(h_,new Instances(D));

				// accept h' over h ? 
				if (w_ > w) {
					w = w_;
					s = s_;
					h = h_;
					if (getDebug()) System.out.println("h_{t="+(t+1)+"} := "+Arrays.toString(s)); //+"; w = "+w);
					//if (getDebug()) System.out.print("& "+Utils.doubleToString(payoff(h_,new Instances(D),1),8,2));
					//if (getDebug()) System.out.print("& "+Utils.doubleToString(payoff(h_,new Instances(D),2),8,2));
					//if (getDebug()) System.out.println("& "+Utils.doubleToString(payoff(h_,new Instances(D),5),8,2));
				}
			}
		}
		if (getDebug()) System.out.println("---");

		this.setChain(s);
		super.buildClassifier(D);
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {


		double y0[] = super.distributionForInstance(x);

		double yT[] = CCUtils.RandomSearch(this,x,m_Iy,m_R,y0);

		return yT;
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the number of iterations in the chain space (training)\n\tdefault: "+m_Is, "Is", 1, "-Is <value>"));
		newVector.addElement(new Option("\tSets the number of iterations in the path space (inference)\n\tdefault: "+m_Iy, "Iy", 1, "-Iy <value>"));
		newVector.addElement(new Option("\tSets the type of payoff fn. to use (for I > 0) \n\tdefault: "+m_Payoff, "P", 1, "-P <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		m_Is = (Utils.getOptionPos("Is",options) >= 0) ? Integer.parseInt(Utils.getOption("Is", options)) : m_Is;
		m_Iy = (Utils.getOptionPos("Iy",options) >= 0) ? Integer.parseInt(Utils.getOption("Iy", options)) : m_Iy;
		m_Payoff = (Utils.getOptionPos('P',options) >= 0) ? Integer.parseInt(Utils.getOption('P', options)) : m_Payoff;
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 6];
		int current = 0;
		options[current++] = "-Is";
		options[current++] = String.valueOf(m_Is);
		options[current++] = "-Iy";
		options[current++] = String.valueOf(m_Iy);
		options[current++] = "-P";
		options[current++] = String.valueOf(m_Payoff);
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;
	}

	@Override
	public String globalInfo() {
		return "Classifier Chains with Monte Carlo optimization. " + "For more information see:\n" + getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		TechnicalInformation	additional;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Jesse Read and Luca Martino and David Luengo");
		result.setValue(Field.TITLE, "Efficient Monte Carlo Optimization for Multi-label Classifier Chains");
		result.setValue(Field.BOOKTITLE, "ICASSP'13: International Conference on Acoustics, Speech, and Signal Processing");
		result.setValue(Field.YEAR, "2013");

		additional = new TechnicalInformation(Type.ARTICLE);
		additional.setValue(Field.AUTHOR, "Jesse Read and Luca Martino and David Luengo");
		additional.setValue(Field.TITLE, "Efficient Monte Carlo Optimization for Multi-dimensional Classifier Chains");
		additional.setValue(Field.JOURNAL, "Elsevier Pattern Recognition");
		additional.setValue(Field.YEAR, "2013");

		result.add(additional);
		return result;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new MCCe(),args);
	}

}
