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

package weka.classifiers.multitarget;

/**
 * CC.java - The Classifier Chains (CC) method.
 * Multi-target version of CC method (directly applicable) -- only the confidence information is different.
 * @see 	weka.classifiers.multilabel.CC
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 * @version	Jan 2012
 */
import java.util.Arrays;

import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Instance;
import weka.core.RevisionUtils;

public class CC extends weka.classifiers.multilabel.CC implements MultiTargetClassifier {

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
				"The Classifier Chains (CC) method.\n"
				+ "Multi-target version of the BR-based CC method (directly applicable).";
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
		MultilabelClassifier.evaluation(new weka.classifiers.multitarget.CC(),args);
	}
}
