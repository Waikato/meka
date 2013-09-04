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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class Test extends MultilabelClassifier implements SemisupervisedClassifier, MultiTargetCapable {

	Instances Dx = null;

	@Override
	public void setUnlabelledData(Instances D) {
		System.out.println("setting unlabelled instances");
		System.exit(1);
		Dx = D;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		if (this instanceof SemisupervisedClassifier) {
			System.out.println("OK 1");
		}
		if (this instanceof MultiTargetCapable) {
			System.out.println("OK 2");
		}
		if (this instanceof MultilabelClassifier) {
			System.out.println("OK 3");
		}
	}

	@Override
	public double[] distributionForInstance(Instance xy) throws Exception {
		return new double[xy.classIndex()];
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new Test(),args);
	}

}
