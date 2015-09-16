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

import weka.core.TechnicalInformation.*;
import weka.core.*;
import meka.core.*;
import java.util.*;

/**
 * MCC.java - CC with Monte Carlo optimisation. 
 *
 * Note inference is now a bit slower than reported in the paper,
 * <br>
 * Jesse Read, Luca Martino, David Luengo. <i>Efficient Monte Carlo Optimization for Multi-dimensional Classifier Chains</i>. http://arxiv.org/abs/1211.2190. 2012
 * <br>
 * There we used a faster implementation, full of ugly hacks, but it got broken when I updated CC.java.<br>
 * This version extends CC, and thus is a bit cleaner, but for some reason inference is quite slower than expected with high m_Iy.
 *
 * TODO Option for hold-out set, instead of training and testing on training data (internally).
 *
 * @see meka.classifiers.multilabel.CC
 * @author Jesse Read
 * @version	March 2015
 */
public class MCC extends CC implements TechnicalInformationHandler {

	private static final long serialVersionUID = 5085402586815030939L;
	protected int m_Is = 0;
	protected int m_Iy = 10;
	protected String m_Payoff = "Exact match";

	/**
	 * Payoff - Return a default score of h evaluated on D.
	 * @param	h	a classifier
	 * @param	D	a dataset
	 */
	public double payoff(CC h, Instances D) throws Exception {
		Result r = Evaluation.testClassifier(h,D);
		// assume multi-label for now
		r.setInfo("Type","ML");
		r.setInfo("Threshold","0.5"); 
		r.setInfo("Verbosity","7");
		r.output = Result.getStats(r, "7");
		return (Double)r.output.get(m_Payoff);
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);

		// Variables

		int L = D.classIndex(); 
		int N = D.numInstances();
		int d = D.numAttributes()-L;
		m_R = new Random(m_S);

		prepareChain(L);
		int s[] = retrieveChain(); 

		if(getDebug()) System.out.println("s_[0] = "+Arrays.toString(s));

		// If we want to optimize the chain space ...
		if (m_Is > 0) {

			// Make CC
			CC h = CCUtils.buildCC(s,D,m_Classifier);

			if (getDebug()) System.out.println("Optimising s ... ("+m_Is+" iterations):");

			double w = payoff(h,new Instances(D));
			if (getDebug()) System.out.println("h_{t="+0+"} := "+Arrays.toString(s)); //+"; w = "+w);

			for(int t = 0; t < m_Is; t++) {

				// propose a chain s' by swapping two elements in s
				int s_[] = Arrays.copyOf(A.swap(s,m_R),s.length);

				// build h'
				CC h_ = CCUtils.buildCC(s_,D,m_Classifier);

				// rate h'
				double w_ = payoff(h_,new Instances(D));

				// accept h' over h ? 
				if (w_ > w) {
					w = w_;
					s = s_;
					h = h_;
					if (getDebug()) System.out.println("h_{t="+(t+1)+"} := "+Arrays.toString(s)); //+"; w = "+w);
					//if (getDebug()) System.out.print("& "+Utils.doubleToString(likelihood(h_,new Instances(D),1),8,2));
					//if (getDebug()) System.out.print("& "+Utils.doubleToString(likelihood(h_,new Instances(D),2),8,2));
					//if (getDebug()) System.out.println("& "+Utils.doubleToString(likelihood(h_,new Instances(D),5),8,2));
				}
			}
		}
		if (getDebug()) System.out.println("---");

		this.setChain(s);
		super.buildClassifier(D);
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {


		//  T = 0
		double y[] = super.distributionForInstance(x);

		// T > 0
		if (m_Iy > 0) {
			//double yT[] = CCUtils.RandomSearchaa(this,x,m_Iy,m_R,y0);

			double w  = A.product(this.probabilityForInstance(x,y));	// p(y|x)

			Instance t_[] = this.getTransformTemplates(x);

			//System.out.println("----");
			//System.out.println("p0("+Arrays.toString(y)+") = "+Arrays.toString(h.getConfidences())+", w="+w);
			for(int t = 0; t < m_Iy; t++) {
				double y_[] = this.sampleForInstanceFast(t_,m_R); 	    // propose y' by sampling i.i.d.
				//double y_[] = this.sampleForInstance(x,m_R); 	    // propose y' by sampling i.i.d.
				//double p_[] = h.getConfidences();                   //
				double w_  = A.product(this.getConfidences()); 		// rate y' as w'  --- TODO allow for command-line option
				//System.out.println("p("+Arrays.toString(y_)+") = "+Arrays.toString(p_)+", w="+w_);
				if (w_ > w) { 										// accept ? 
					if (getDebug()) System.out.println("y' = "+Arrays.toString(y_)+", :"+w_);
					w = w_;
					//y = y_;
					y = Arrays.copyOf(y_,y_.length);
					//System.out.println("* ACCEPT *");
				}
			}
		}

		return y;
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the number of iterations in the chain space (training)\n\tdefault: "+m_Is, "Is", 1, "-Is <value>"));
		newVector.addElement(new Option("\tSets the number of iterations in the path space (inference)\n\tdefault: "+m_Iy, "Iy", 1, "-Iy <value>"));
		newVector.addElement(new Option("\tSets the payoff function. Should be one listed in normal evaluation output\n\tdefault: "+m_Payoff, "P", 1, "-P <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		m_Is = (Utils.getOptionPos("Is",options) >= 0) ? Integer.parseInt(Utils.getOption("Is", options)) : m_Is;
		m_Iy = (Utils.getOptionPos("Iy",options) >= 0) ? Integer.parseInt(Utils.getOption("Iy", options)) : m_Iy;
		m_Payoff = (Utils.getOptionPos('P',options) >= 0) ? Utils.getOption('P', options) : m_Payoff;
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
		options[current++] = m_Payoff;
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;
	}

	/** Set the inference iterations */
	public void setInferenceInterations(int iy) {
		m_Iy = iy;
	}

	/** Get the inference iterations */
	public int getInferenceInterations() {
		return m_Iy;
	}

	/** Set the iterations of s (chain order) */
	public void setChainIterations(int is) {
		m_Is = is;
	}

	/** Get the iterations of s (chain order) */
	public int getChainIterations() {
		return m_Is;
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
		ProblemTransformationMethod.evaluation(new MCC(), args);
	}

}
