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

package meka.classifiers.multilabel.meta;

import Jama.Matrix;
import rbms.RBM;
import rbms.DBM;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import meka.core.*;
import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.classifiers.multilabel.NN.*;
import meka.classifiers.multilabel.BR;

/**
 * DeepML.java - Deep Multi-label Classification.
 * Trains an RBM/DBM on the feature space of the training data; then train on it (with the labels) with whichever multi-label classifier is specified.
 * <br>
 * See: Jesse Read and Jaakko Hollmen. <i>A Deep Interpretation of Classifier Chains</i>. IDA 2014.
 * <br>
 * The first RBM will have h = d / 2 hidden units, the second h = h / 2, and so on, where d is the number of original (visible) input feature attributes.
 *
 * @author Jesse Read 
 * @version December 2012
 */
public class DeepML extends AbstractDeepNeuralNet implements TechnicalInformationHandler {

	private static final long serialVersionUID = 3388606529764305098L;
	protected RBM dbm = null;
	protected long rbm_time = 0;

	/**
	 * CreateDBM - make a RBM if N=1 or a DBM otherwise.
	 */
	protected RBM createDBM(int d) throws Exception {
		return ((m_N == 1) ? 
			new RBM(this.getOptions()) :		// RBM
			new DBM(this.getOptions())) ;		// DBM
	}

	public DeepML() {
		// default classifier for GUI
		this.m_Classifier = new BR();
	}

	@Override
	protected String defaultClassifierString() {
		// default classifier for CLI
		return "meka.classifiers.multilabel.BR";
	}


	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);

		// Extract variables

		int L = D.classIndex();
		int d = D.numAttributes()-L;
		double X_[][] = MLUtils.getXfromD(D);

		// Pre Tune ===========================
		/*
		if (m_M < 0 || m_R < 0 || m_H < 0) {
			System.out.println("We will do some pre-tuning here ...");
			//BR h = new BR();
			//h.setClassifier(new SMO());
			String ops[] = RBMTools.tuneRBM((MultilabelClassifier)m_Classifier,D,m_R,m_M,m_H,m_E);
			System.out.println("got: "+Arrays.toString(ops));
			this.setOptions(ops);
		}
		*/
		// ====================================

		// Build DBM
		dbm = createDBM(d);
		dbm.setSeed(m_Seed);

		dbm.setE(m_E);

		// Train RBM, get Z
		long before = System.currentTimeMillis();
		dbm.train(X_,L);
		rbm_time = System.currentTimeMillis() - before;
		double Z[][] = dbm.prob_Z(X_);
		if (getDebug()) {
			Matrix tW[] = dbm.getWs();
			System.out.println("X = \n"+ MatrixUtils.toString(X_));
			System.out.println("W = \n"+ MatrixUtils.toString(tW[0].getArray()));
			System.out.println("Y = \n"+ MatrixUtils.toString(MLUtils.getYfromD(D), 0));
			System.out.println("Z = \n"+ MatrixUtils.toString(MatrixUtils.threshold(Z, 0.5), 0));
			/*
			Instances newD = RBMTools.makeDataset(D,M.threshold(Z,0.5));
			System.out.println(""+newD);
			ArffSaver saver = new ArffSaver();
			saver.setInstances(newD);
			saver.setFile(new File("newD.arff"));
			saver.writeBatch();
			System.exit(1);
			*/
		}

		// Train Classifier
		m_InstancesTemplate = new Instances(MLUtils.replaceZasAttributes(D,Z,L)); // did not clear
		m_Classifier.buildClassifier(m_InstancesTemplate);
	}

	@Override
	public double[] distributionForInstance(Instance xy) throws Exception {

		int L = xy.classIndex();

		double z[] = dbm.prob_z(MLUtils.getxfromInstance(xy));

		Instance zy = (Instance)m_InstancesTemplate.firstInstance().copy();

		MLUtils.setValues(zy,z,L);
		zy.setDataset(m_InstancesTemplate);

		return m_Classifier.distributionForInstance(zy);
	}

	@Override
	public String toString() {
		return super.toString() + ", RBM-Build_Time="+rbm_time;
	}

	/* 
	 * TODO: Make a generic abstract -dependency_user- class that has this option, and extend it here
	 */

	public String globalInfo() {
		return 
				"Create a new feature space using a stack of RBMs, then employ a multi-label classifier on top. "
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}
	
	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Jesse Read and Jaako Hollmen");
		result.setValue(Field.TITLE, "A Deep Interpretation of Classifier Chains");
		result.setValue(Field.BOOKTITLE, "Advances in Intelligent Data Analysis {XIII} - 13th International Symposium, {IDA} 2014");
		result.setValue(Field.PAGES, "251--262");
		result.setValue(Field.YEAR, "2014");

		return result;
	}

	public static void main(String args[]) throws Exception {
		ProblemTransformationMethod.evaluation(new DeepML(), args);
	}

}

