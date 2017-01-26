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

/**
 * FilteredClassifier.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package meka.classifiers.multitarget.meta;

import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.classifiers.multitarget.CC;
import meka.classifiers.multitarget.MultiTargetClassifier;
import weka.classifiers.Classifier;

/**
 * Allows the application of a filter in conjunction with a multi-target classifier.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class FilteredClassifier
  extends meka.classifiers.multilabel.meta.FilteredClassifier
  implements MultiTargetClassifier {

	private static final long serialVersionUID = 6632813009466375365L;

	/**
	 * Default constructor.
	 *
	 * Turns off check for modified class attribute.
	 */
	public FilteredClassifier() {
		super();
		setDoNotCheckForModifiedClassAttribute(true);
		m_Classifier = new CC();
	}

	/**
	 * String describing default classifier.
	 */
	protected String defaultClassifierString() {
		return CC.class.getName();
	}

	/**
	 * Set the base learner.
	 *
	 * @param newClassifier the classifier to use.
	 */
	@Override
	public void setClassifier(Classifier newClassifier) {
		if (!(newClassifier instanceof MultiTargetClassifier))
			throw new IllegalArgumentException("Classifier must be a " + MultiTargetClassifier.class.getName() + "!");
		super.setClassifier(newClassifier);
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new FilteredClassifier(), args);
	}
}
