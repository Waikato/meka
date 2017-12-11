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

import meka.classifiers.multilabel.IncrementalMultiLabelClassifier;
import meka.classifiers.multilabel.incremental.BRUpdateable;
import meka.classifiers.incremental.IncrementalEvaluation;
import meka.classifiers.multilabel.meta.EnsembleML;
import weka.classifiers.UpdateableClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

import java.util.Random;

/**
 * BaggingMLUpdatable.java - Using the OzaBag scheme (see OzaBag.java from MOA)).
 * See also: N. Oza and S. Russell. Online bagging and boosting. In Artificial Intelligence and Statistics 2001, pages 105-112. Morgan Kaufmann, 2001.
 * @version 	Jan 2013
 * @author 		Jesse Read
 */

public class BaggingMLUpdateable extends EnsembleML implements IncrementalMultiLabelClassifier, TechnicalInformationHandler {

	private static final long serialVersionUID = 4978269895923479962L;
	protected Random random = null;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "Incremental Bagging";
	}

	public BaggingMLUpdateable() {
		// default classifier for GUI
		this.m_Classifier = new BRUpdateable();
	}

	@Override
	protected String defaultClassifierString() {
		// default classifier for CLI
		return "meka.classifiers.multilabel.incremental.BRUpdateable";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		random = new Random(m_Seed);
		super.buildClassifier(D);
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {

		for(int i = 0; i < m_NumIterations; i++) {
			// Oza-Bag style
			int k = poisson(1.0, random);
			if (m_BagSizePercent == 100) {
				// Train on all instances
				k = 1;
			}
			if (k > 0) {
				// Train on this instance only if k > 0
				Instance x_weighted = (Instance) x.copy();
				x_weighted.setWeight(x.weight() * (double)k);
				((UpdateableClassifier)m_Classifiers[i]).updateClassifier(x_weighted);
			}
		}
	}


	protected static int poisson(double lambda, Random r) {
		if (lambda < 100.0) {
			double product = 1.0;
			double sum = 1.0;
			double threshold = r.nextDouble() * Math.exp(lambda);
			int i = 1;
			int max = Math.max(100, 10 * (int) Math.ceil(lambda));
			while ((i < max) && (sum <= threshold)) {
				product *= (lambda / i);
				sum += product;
				i++;
			}
			return i - 1;
		}
		double x = lambda + Math.sqrt(lambda) * r.nextGaussian();
		if (x < 0.0) {
			return 0;
		}
		return (int) Math.floor(x);
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "N.Oza, S. Russell");
		result.setValue(Field.TITLE, "Online bagging and boosting");
		result.setValue(Field.JOURNAL, "Artificial Intelligence and Statistics");
		result.setValue(Field.PUBLISHER, "Morgan Kaufmann");
		result.setValue(Field.YEAR, "2001");
		result.setValue(Field.PAGES, "105-112");
		return result;
	}

	public static void main(String args[]) {
		IncrementalEvaluation.runExperiment(new BaggingMLUpdateable(),args);
	}

}
