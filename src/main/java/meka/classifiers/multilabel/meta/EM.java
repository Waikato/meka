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
import meka.core.MLUtils;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import meka.classifiers.multilabel.*;

/**
 * EM.java - Expectation Maximization using any multi-label classifier.
 *
 * A specified multi-label classifier is built on the training data. This model is then used to classify the test data. 
 * The confidence with which instances are classified is used to reweight them. This data is then used to retrain the classifier. 
 * This cycle continues ('EM'-style) for I iterations. The final model is used to officially classifier the test data.
 * <br>
 * Because of the weighting, it is advised to use a classifier which gives good confidence (probabalistic) outputs.
 * <br>
 *
 * @version 2010
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class EM extends MultilabelClassifier implements SemisupervisedClassifier, TechnicalInformationHandler {

	protected int m_I = 10;
	protected Instances D_ = null;

	@Override
	public void setUnlabelledData(Instances D) {
		this.D_ = D;
	}

	@Override
	public String globalInfo() {
		return "Train a classifier using labelled and unlabelled data (semi-supervised) using an EM-type algorithm. Works best if the classifier can give good probabalistic outputs. " + "A similar procedure was used with LC and Naive Bayes in:\n" + getTechnicalInformation().toString();
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);

		if (getDebug()) 
			System.out.println("Initial build ...");
	  	
		m_Classifier.buildClassifier(D); 

		Instances DA = MLUtils.combineInstances(D,D_);

		if (getDebug()) 
			System.out.print("Performing "+m_I+" 'EM' Iterations: [");
		for(int i = 0; i < m_I; i++) {
			if (getDebug())
				System.out.print(".");
			// expectation (classify + update weights)
			updateWeights((MultilabelClassifier)m_Classifier, DA);
			// maximization of parameters (training)
			m_Classifier.buildClassifier(DA);
		}
		System.out.println("]");
	}

	protected void updateWeights(MultilabelClassifier h, Instances D) throws Exception {
		for(Instance x : D) {
			double w = 1.0; // weight (product of probability)
			double y[] = h.distributionForInstance(x);
			// convert ML distribution into probability vector, and multiply to w as we go ..
			for(int j = 0; j < y.length; j++) {
				w *= (y[j] < 0.5) ? 1. - y[j] : y[j];
			}
			x.setWeight(w);
		}
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		return m_Classifier.distributionForInstance(x);
	}

	public void setIterations(int i) {
		m_I = i;
	}

	public int getIterations() {
		return m_I;
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tThe number of iterations of EM to carry out (default: "+m_I+")", "I", 1, "-I <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		m_I = (Utils.getOptionPos('I',options) >= 0) ? Integer.parseInt(Utils.getOption('I',options)) : m_I; 
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
	  	ArrayList<String> result;
	  	result = new ArrayList<String>(Arrays.asList(super.getOptions()));
	  	result.add("-I");
	  	result.add(String.valueOf(m_I));
		return result.toArray(new String[result.size()]);
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Nigam, Kamal and Mccallum, Andrew K. and Thrun, Sebastian and Mitchell, Tom M.");
		result.setValue(Field.TITLE, "Text classification from Labeled and Unlabeled Documents using EM");
		result.setValue(Field.JOURNAL, "Machine Learning");
		result.setValue(Field.VOLUME, "39");
		result.setValue(Field.NUMBER, "2/3");
		result.setValue(Field.PAGES, "103--134");
		result.setValue(Field.YEAR, "2010");

		return result;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new EM(),args);
	}

}
