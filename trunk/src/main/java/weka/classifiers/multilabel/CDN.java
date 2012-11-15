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

import java.util.*;

import weka.classifiers.*;
import weka.classifiers.multilabel.*;
import weka.core.*;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

/**
 * CDN.java - Conditional Dependency Networks.
 * <br>
 * See: Yuhong Guoand and Suicheng Gu. <i>Multi-Label Classification Using Conditional Dependency Networks</i>. IJCAI '11. 2011.
 * <br>
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 * @version	November 2012
 */
public class CDN extends MultilabelClassifier implements Randomizable, TechnicalInformationHandler {

	Classifier h[] = null;
	Random u = null;
	Instances D_templates[];

	int I = 1000;	// total iterations
	int I_c = 100;	// collection iterations

	/**
	 *  Build Classifier.
	 *  Build L probabilistic models, each to predict Y_i | X, Y_{-y}; save the templates.
	 */
	public void buildClassifier(Instances D) throws Exception {
		int N = D.numInstances();
		int L = D.classIndex();
		h = new Classifier[L];
		u = new Random(m_S);
		D_templates = new Instances[L];

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

	/**
	 *  Probabilistic Classification.
	 *  Use Gibbs sampling.
	 */
	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();
		//ArrayList<double[]> collection = new ArrayList<double[]>(100);

		double y[] = new double[L];		// for collectiing marginal
		int r[] = MLUtils.gen_indices(L);

		double likelihood[] = new double[L];

		for(int i = 0; i < I; i++) {
			Collections.shuffle(Arrays.asList(r));
			for(int j : r) {
				// x = [x,y[1],...,y[j-1],y[j+1],...,y[L]]
				x.setDataset(D_templates[j]);
				// q = h_j(x)    i.e. p(y_j | x)
				double q = h[j].distributionForInstance(x)[1];
				if (u.nextDouble() < q) { 
					// accept
					x.setValue(j,1);
				}
				else {
					// reject
					x.setValue(j,0);
				}
				// likelihood
				likelihood[j] = Math.max(q,1.0-q);
				double s = Utils.sum(likelihood);
				//System.out.println("likelihood: "+s);
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
}

