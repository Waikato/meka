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

package meka.core;

import java.util.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import weka.core.Instances;
import weka.core.Instance;

/**
 * F.java - TRANSFORM/FILTER OPERATIONS.
 * Transform 'D' and 'x' in many different ways.
 */
public abstract class F {

	/**
	 * SwitchAttributes - Move L label attributes from the beginning to end of attribute space of an Instances. 
	 * Necessary because MULAN assumes label attributes are at the end, not the beginning.
	 * (the extra time for this process is not counted in the running-time analysis of published work).
	 */
	public static final Instances switchAttributes(Instances D, int L) {
		for(int j = 0; j < L; j++) {
			//D.insertAttributeAt(new Attribute(D.attribute(0).name()+"-"),D.numAttributes());
			D.insertAttributeAt(D.attribute(0).copy(D.attribute(0).name()+"-"),D.numAttributes());
			for(int i = 0; i < D.numInstances(); i++) {
				D.instance(i).setValue(D.numAttributes()-1,D.instance(i).value(0));
			}
			D.deleteAttributeAt(0);
		}
		return D;
	}

	/**
	 * SwitchAttributes - Move L label attributes from the beginning to end of attribute space of an Instance. 
	 * Necessary because MULAN assumes label attributes are at the end, not the beginning.
	 * (the extra time for this process is not counted in the running-time analysis of published work).
	 */
	public static final Instance switchAttributes(Instance x, int L) {
		x.setDataset(null);
		for(int j = 0; j < L; j++) {
			x.insertAttributeAt(x.numAttributes());
			x.deleteAttributeAt(0);
		}
		return x;
	}

	/**
	 * SwapIndices - swap values of y[1] to y[L] according to s[].
	 * @TODO this function will be called from CT
	 * @TODO call this function also from SwitchAttributes()
	 * @param	s	new indices order
	public static void swapIndices(Instance x, int s[]) {
		int L = s.length;
		int y[] = x.asArray();
		MLUtils.replacezasattributes(x,s,L);
		for(int j = 0; j < L; j++) {
			x.setValue(0,s[j]);
			temp = 
		}
	}
	*/

	/**
	 * SwapIndices - swap values of y[1] to y[L] according to s[].
	 * @TODO this function will be called from CT
	 * @TODO call this function also from SwitchAttributes()
	 * @param	s	new indices order
	 */
	public static void reorderLabels(Instances D, int s[]) throws Exception {
		int L = s.length;

		Reorder f = new Reorder();

		String range = "";
		for(int j = 0; j < L; j++) {
			range += String.valueOf(s[0]) + ",";
		}
		range = range + (L+1) + "-last";
		f.setAttributeIndices(range);
		f.setInputFormat(D);
		D = Filter.useFilter(D, f);

		//return D;
	}

	public static Instances remove(Instances D, int indices[], boolean inv) throws Exception {
		Remove remove = new Remove();
		remove.setAttributeIndicesArray(indices);
		remove.setInvertSelection(inv);
		remove.setInputFormat(D);
		return Filter.useFilter(D, remove);
	}

}
