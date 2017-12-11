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

package meka.classifiers.multilabel.incremental;

import meka.classifiers.incremental.IncrementalEvaluation;
import meka.classifiers.multilabel.IncrementalMultiLabelClassifier;
import meka.classifiers.multilabel.MajorityLabelset;
import weka.core.Instance;

/**
 * MajorityLabelsetUpdateable.java - Updateable version of MajorityLabelset.
 * @see MajorityLabelset
 * @author 		Jesse Read
 * @version 	September 2011
 */
public class MajorityLabelsetUpdateable extends MajorityLabelset implements IncrementalMultiLabelClassifier {

	/** for serialization. */
  	private static final long serialVersionUID = -6454034593889787500L;

	@Override
	public String globalInfo() {
		return "Updateable Majority Labelset Classifier";
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {
		int L = x.classIndex();
		super.updateCount(x,L);
	}

	public static void main(String args[]) {
		IncrementalEvaluation.runExperiment(new MajorityLabelsetUpdateable(),args);
	}

}
