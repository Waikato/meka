package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.multilabel.*;
import weka.core.*;
import java.util.*;

/**
 * CDN.java - Conditional Dependency Networks.
 * <br>
 * See: Yuhong Guoand Suicheng Gu. <i>Multi-Label Classification Using Conditional Dependency Networks</i>. IJCAI '11. 2011.
 * <br>
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 * @version	November 2012
 */
public class CDN extends MultilabelClassifier implements Randomizable {

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

}

