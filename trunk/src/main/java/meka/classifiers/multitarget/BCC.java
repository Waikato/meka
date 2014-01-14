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

package meka.classifiers.multitarget;

/**
 * BCC.java - The Bayesian Classifier Chains (BCC) method.
 * Multi-target version of BCC method (directly applicable) -- only the confidence information is different.
 * @see 	meka.classifiers.multilabel.BCC
 * @author 	Jesse Read 
 * @version	June 2012
 */
import java.util.Arrays;

import meka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Instance;
import weka.core.RevisionUtils;

public class BCC extends meka.classifiers.multilabel.BCC implements MultiTargetClassifier {

	/** for serialization. */
	private static final long serialVersionUID = 2395428645144026318L;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"The Bayesian Classifier Chains (BCC) method.\n"
				+ "Multi-target version of the BCC method (directly applicable).";
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		double y_long[] = Arrays.copyOf(super.distributionForInstance(x),L*2);
		Arrays.fill(y_long,L,y_long.length,1.0);
		return y_long;
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new meka.classifiers.multitarget.BCC(),args);
	}
}
