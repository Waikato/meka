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
 * MatrixUtils.java
 * Copyright (C) 2015 University of Mainz, Germany
 */

package meka.core;

import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.matrix.Matrix;

/**
 * Utility functions relating to matrices.
 *
 * @author Joerg Wicker (wicker@uni-mainz.de)
 * @version $Revision$
 */
public class MatrixUtils {

	/**
	 * Helper method that transforma an Instances object to a Matrix object.
	 *
	 * @param inst The Instances to transform.
	 * @return  The resulting Matrix object.
	 */
	public static Matrix instancesToMatrix(Instances inst){
		double[][] darr = new double[inst.numInstances()][inst.numAttributes()];
		for (int i =0 ; i < inst.numAttributes(); i++) {
			for (int j = 0; j < inst.attributeToDoubleArray(i).length; j++) {
				darr[j][i] = inst.attributeToDoubleArray(i)[j];
			}
		}
		return new Matrix(darr);
	}

	/**
	 * Helper method that transforms a Matrix object to an Instances object.
	 *
	 * @param mat The Matrix to transform.
	 * @param patternInst the Instances template to use
	 * @return  The resulting Instances object.
	 */
	public static Instances matrixToInstances(Matrix mat, Instances patternInst){
		Instances result = new Instances(patternInst);
		for (int i = 0; i < mat.getRowDimension(); i++) {
			double[] row =  mat.getArray()[i];
			DenseInstance denseInst = new DenseInstance(1.0, row);
			result.add(denseInst);
		}

		return result;
	}

}
