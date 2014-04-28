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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.util.Enumeration;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import meka.core.A;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

/**
 * CDN.java - Conditional Dependency Networks.
 * A fully connected network, each node (label) is connected to each other node (label). Each node is a BR classifier that predicts p(y_j|x,y_1,...,y_{j-1},y_{j-1},...,y_L). Inference is done using Gibbs sampling over I iterations. The final I_c iterations are used to collected the marginal probabilities, which becomes the prediction y[].
 * <br>
 * See: Yuhong Guoand and Suicheng Gu. <i>Multi-Label Classification Using Conditional Dependency Networks</i>. IJCAI '11. 2011.
 * <br>
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 * @version	November 2012
 */
public class CDN extends MultilabelClassifier implements Randomizable, TechnicalInformationHandler {

	/** for serialization. */
  	private static final long serialVersionUID = -4571133392057899417L;
  	
	protected Classifier h[] = null;
	protected Random u = null;
	protected Instances D_templates[];

	protected int I = 1000;	// total iterations
	protected int I_c = 100;	// collection iterations

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		int N = D.numInstances();
		int L = D.classIndex();
		h = new Classifier[L];
		u = new Random(m_S);
		D_templates = new Instances[L];

		// Build L probabilistic models, each to predict Y_i | X, Y_{-y}; save the templates.
		for(int j = 0; j < L; j++) {
			// X = [Y[0],...,Y[j-1],Y[j+1],...,Y[L],X]
			D_templates[j] = new Instances(D);
			D_templates[j].setClassIndex(j);
			// train H[j] : X -> Y
			h[j] = AbstractClassifier.forName(getClassifier().getClass().getName(),((AbstractClassifier)getClassifier()).getOptions());
			h[j].buildClassifier(D_templates[j]);
		}
	}

	/*
	 *  Discrete Classification.
	 *  Use Gibbs sampling.
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		int r[] = MLUtils.gen_indices(L);
		Collections.shuffle(Arrays.asList(r));
		for(int i = 0; i < I; i++) {
			for(int j : r) {
				x.setDataset(D_templates[j]);           // set target att. to j
				x.setValue(j,h[j].classifyInstance(x)); // y_j = h_j(x)
			}
			System.out.println(""+MLUtils.toBitString(x,L));
		}
		double y[] = new double[L];
		for(int j : r) {
			y[j] = x.value(j);
		}
		System.out.println(""+Arrays.toString(y));
		return y;
	}
	*/

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();
		//ArrayList<double[]> collection = new ArrayList<double[]>(100);

		double y[] = new double[L];		// for collectiing marginal
		int sequence[] = A.make_sequence(L);

		double likelihood[] = new double[L];

		for(int i = 0; i < I; i++) {
			Collections.shuffle(Arrays.asList(sequence));
			for(int j : sequence) {
				// x = [x,y[1],...,y[j-1],y[j+1],...,y[L]]
				x.setDataset(D_templates[j]);
				// q = h_j(x)    i.e. p(y_j | x)

				double dist[] = h[j].distributionForInstance(x);
				int k = A.samplePMF(dist,u);
				x.setValue(j,k);
				likelihood[j] = dist[k];
				// likelihood
				double s = Utils.sum(likelihood);
				// collect  // and where is is good 
				if (i > (I - I_c)) {
					y[j] += x.value(j);
				}
				// else still burning in
			}
		}
		// finish, calculate marginals
		for(int j = 0; j < L; j++) {
			y[j] /= I_c;
		}

		return y;
	}

	protected int m_S = 0;

	@Override
	public void setSeed(int s) {
		m_S = s;
	}

	@Override
	public int getSeed() {
		return m_S;
	}
	
	public String seedTipText() {
	  return "The seed value for randomization.";
	}

	/** 
	 * GetI - Get the number of iterations.
	 */
	public int getI() {
		return I;
	}

	/** 
	 * SetI - Sets the number of iterations.
	 */
	public void setI(int i) {
		I = i;
	}

	/** 
	 * GetIc - Get the number of collection iterations.
	 */
	public int getIc() {
		return I_c;
	}

	/** 
	 * SetIc - Sets the number of collection iterations.
	 */
	public void setIc(int ic) {
		I_c = ic;
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tTotal Iterations.\n\tdefault: "+I, "I", 1, "-I <value>"));
		newVector.addElement(new Option("\tCollection Iterations.\n\tdefault: "+I_c, "Ic", 1, "-Ic <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		I = (Utils.getOptionPos('I',options) >= 0) ? Integer.parseInt(Utils.getOption('I', options)) : I;
		I_c = (Utils.getOptionPos("Ic",options) >= 0) ? Integer.parseInt(Utils.getOption("Ic", options)) : I_c;

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		ArrayList<String> result;
	  	result = new ArrayList<String>(Arrays.asList(super.getOptions()));
	  	result.add("-I");
	  	result.add("" + I);
		result.add("-Ic");
	  	result.add("" + I_c);
		return result.toArray(new String[result.size()]);
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CDN(),args);
	}

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"A Conditional Dependency Network. "
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Yuhong Guoand and Suicheng Gu");
		result.setValue(Field.TITLE, "Multi-Label Classification Using Conditional Dependency Networks");
		result.setValue(Field.BOOKTITLE, "IJCAI '11");
		result.setValue(Field.YEAR, "2011");

		return result;
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}
}

