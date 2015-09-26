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

import meka.core.A;
import meka.core.OptionUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.*;

/**
 * CDN.java - Conditional Dependency Networks.
 * A fully connected undirected network, each node (label) is connected to each other node (label). Each node is a binary classifier that predicts p(y_j|x,y_1,...,y_{j-1},y_{j-1},...,y_L). Inference is done using Gibbs sampling over I iterations. The final I_c iterations are used to collected the marginal probabilities, which becomes the prediction y[].
 * <br>
 * See: Yuhong Guoand and Suicheng Gu. <i>Multi-Label Classification Using Conditional Dependency Networks</i>. IJCAI '11. 2011.
 * <br>
 * @author 	Jesse Read
 * @version	November 2012
 */
public class CDN extends ProblemTransformationMethod implements Randomizable, TechnicalInformationHandler {

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

	public String iTipText() {
		return "The number of iterations.";
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

	public String icTipText() {
		return "The number of collection iterations.";
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tTotal Iterations.\n\tdefault: 1000", "I", 1, "-I <value>"));
		result.addElement(new Option("\tCollection Iterations.\n\tdefault: 100", "Ic", 1, "-Ic <value>"));
		result.addElement(new Option("\tThe seed value for randomization\n\tdefault: 0", "S", 1, "-S <value>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setI(OptionUtils.parse(options, 'I', 1000));
		setIc(OptionUtils.parse(options, "Ic", 100));
		setSeed(OptionUtils.parse(options, 'S', 0));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'I', getI());
		OptionUtils.add(result, "Ic", getIc());
		OptionUtils.add(result, 'S', getSeed());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new CDN(), args);
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

