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

import weka.classifiers.Classifier;
import weka.core.OptionHandler;

/**
 *  MultiTargetClassifier.java - A Multi-Target (i.e., Multi-Output / Multi-Dimensional) Classifier.
 *  Implementing this interface signals to the Evaluation that we are dealing with multi-target data, 
 *  and a different evaluation output is given. Training and classification is the same, using the 
 *  methods <i>buildClassifier(Instances)</i> and <i>distributionForInstance(Instance)</i>, except that
 *  the latter only returns the argmax value (i.e., what is to be considered the predicted value). 
 *  <br>
 *  <br>
 *  At the moment it is also possible to extend the <code>double[]</code> from <code>distributionForInstance</code> 
 *  to a vector of <code>L*2</code> doubles instead of <code>L</code> which contain the max.
 *  In other words, <code>k</code> in position <code>j</code> and <code>p(y[j]=k)</code> in position <code>j+L</code>.
 *  <br>
 *  In the future we will make use of <code>double[] distributionForInstance(Instance,int)</code> instead.
 *
 * 	@author 	Jesse Read
 * 	@version	January 2015
 */

public interface MultiTargetClassifier extends Classifier, OptionHandler {

	/*
	 * TODO Returns the distribution of the k-th value, for each label.
	 *
	 * @return      the multi-target distribution
	 */
	//public double[] distributionForInstance(Instance x, int k);

	/**
	 * Set debugging mode.
	 *
	 * @param debug true if debug output should be printed
	 */
	public void setDebug(boolean debug);

	/**
	 * Get whether debugging is turned on.
	 *
	 * @return true if debugging output is on
	 */
	public boolean getDebug();

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String debugTipText();

	/**
	 * Returns a string representation of the model.
	 *
	 * @return      the model
	 */
	public String getModel();

}
