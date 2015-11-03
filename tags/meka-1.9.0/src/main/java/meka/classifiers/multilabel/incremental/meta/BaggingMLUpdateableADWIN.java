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

package meka.classifiers.multilabel.incremental.meta;

import meka.classifiers.multilabel.incremental.IncrementalEvaluation;
import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.core.MLUtils;
import moa.classifiers.core.driftdetection.ADWIN;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * BaggingMLUpdatableUpdateableADWIN.java - Using the OzaBag scheme (see OzaBag.java from MOA)).
 * @see			BaggingMLUpdateable
 * @version 	Jan 2013
 * @author 		Jesse Read
 */

public class BaggingMLUpdateableADWIN extends BaggingMLUpdateable {

	private static final long serialVersionUID = 8515198118749028799L;
	protected ADWIN adwin = null;
	protected double accuracies[];

	@Override
	public void buildClassifier(Instances D) throws Exception {
		// init adwin
		this.adwin = new ADWIN();
		// make accuracies array
		accuracies = new double[m_NumIterations];
		// set template (for reset later)
		m_InstancesTemplate = new Instances(D,0);
		// continue ...
		super.buildClassifier(D);
	}

	/**
	 * DistributionForInstance - And Check for drift by measuring a type of error.
	 */
	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		// classification

		double y[] = new double[x.classIndex()];

		for(int i = 0; i < m_NumIterations; i++) {
			double y_i[] = m_Classifiers[i].distributionForInstance(x);
			for(int j = 0; j < y_i.length; j++) {
				y[j] += y_i[j];
			}

			accuracies[i] += error(y_i,MLUtils.toDoubleArray(x,y.length));
		}

		for(int j = 0; j < y.length; j++) {
			y[j] = y[j]/m_NumIterations;
		}

		double d = error(y,MLUtils.toDoubleArray(x,y.length));

		// ADWIN stuff

		double ErrEstim=this.adwin.getEstimation();
		if (this.adwin.setInput(1.0-d))
				if (this.adwin.getEstimation() > ErrEstim) {
						// find worst classifier
						int index = Utils.minIndex(accuracies);
						if (getDebug())
							System.out.println("------- CHANGE DETECTED / Reset Model #"+index+" ------- ");
						// reset this classifier
						m_Classifiers[index] = (ProblemTransformationMethod)AbstractClassifier.makeCopy(m_Classifier);
						m_Classifiers[index].buildClassifier(new Instances(m_InstancesTemplate));
						// ... and reset ADWIN
						this.adwin = new ADWIN();
						accuracies = new double[m_NumIterations];
				}

		return y;
	}

	// measure the error
	private double error(double y_pred[], double y_real[]) {
		double distance = 0.0;
		for(int j = 0; j < y_pred.length; j++) {
			distance += Math.pow((y_pred[j] - y_real[j]),2);
		}
		return distance;
	}

	public static void main(String args[]) {
		IncrementalEvaluation.runExperiment(new BaggingMLUpdateableADWIN(),args);
	}

}
