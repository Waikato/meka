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
 *  MultiTargetClassifier.java - A Multi-target Classifier.
 *  To implement this interface, it is also necessary to extend MultilabelClassifier.
 *  Implementing this interface only signals to the Evaluator that we are dealing with multi-target data, 
 *  and a different evaluation output is made. Training and classification is the same, using the 
 *  methods <i>buildClassifier(Instances)</i> and <i>distributionForInstance(Instance)</i>, except that
 *  the latter may return a vector of L*2 doubles instead of L. The extra values are probabalistic 
 *  information (i.e., the probability of class y[j] for the jth target is y[j*2]) that may be used by 
 *  ensemble classifiers.
 *
 * 	@author 	Jesse Read (jesse@tsc.uc3m.es)
 * 	@version	January 2012
 */

public interface MultiTargetClassifier {

	/*
	 * Everything is the same as MultilabelClassifier except for the Evaluation
	 * ... but in the future I would like to add getProbabilities() or something here.
	 */
}
