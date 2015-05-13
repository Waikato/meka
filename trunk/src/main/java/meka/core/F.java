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

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.Reorder;

/**
 * F.java - TRANSFORM/FILTER OPERATIONS.
 * Transform 'D' and 'x' in many different ways.
 * @author Jesse Read (jesse@tsc.uc3m.es)
 */
public abstract class F {

	/**
	 * meka2mulan - Move L label attributes from the beginning to end of attribute space of an Instances. 
	 * Necessary because MULAN assumes label attributes are at the end, not the beginning.
	 * (the extra time for this process is not counted in the running-time analysis of published work).
	 */
	public static final Instances meka2mulan(Instances D, int L) {
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
	 * meka2mulan - Move L label attributes from the beginning to end of attribute space of an Instance. 
	 * Necessary because MULAN assumes label attributes are at the end, not the beginning.
	 * (the extra time for this process is not counted in the running-time analysis of published work).
	 */
	public static final Instance meka2mulan(Instance x, int L) {
		x.setDataset(null);
		for(int j = 0; j < L; j++) {
			x.insertAttributeAt(x.numAttributes());
			x.deleteAttributeAt(0);
		}
		return x;
	}

	/**
	 * mulan2meka - Move label attributes from the End to the Beginning of attribute space (MULAN format to MEKA format). 
	 * Note: can use e.g.: java weka.filters.unsupervised.attribute.Reorder -i thyroid.arff -R 30-last,1-29"
	 * See also: F.reorderLabels(D,s)
	 */
	public static final Instances mulan2meka(Instances D, int L) {
		int d = D.numAttributes();
		for(int j = 0; j < L; j++) {
			D.insertAttributeAt(D.attribute(d-1).copy(D.attribute(d-1).name()+"-"),0);
			for(int i = 0; i < D.numInstances(); i++) {
				D.instance(i).setValue(0,D.instance(i).value(d));
			}
			D.deleteAttributeAt(d);
		}
		return D;
	}

	/**
	 * ReorderLabels - swap values of y[1] to y[L] according to s[].
	 * @param	s	new indices order (supposing that it contains the first s.length indices)
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

	/**
	 * Remove Indices - Remove attribute indices 'indices' from 'D'.
	 * @param	D		Dataset
	 * @param	indices	attribute indices to remove/keep
	 * @param	inv		if true, then keep 'indices'
	 * @return	New dataset with 'indices' removed.
	 */
	public static Instances remove(Instances D, int indices[], boolean inv) throws Exception {
		Remove remove = new Remove();
		remove.setAttributeIndicesArray(indices);
		remove.setInvertSelection(inv);
		remove.setInputFormat(D);
		return Filter.useFilter(D, remove);
	}

	/**
	 * Remove Indices - Remove ALL labels (assume they are the first L attributes) from D.
	 * @param	D		Dataset
	 * @param	L 		number of labels
	 * @return	New dataset with labels removed.
	 */
	public static Instances removeLabels(Instances D, int L) throws Exception {
		Remove remove = new Remove();
		remove.setAttributeIndices("1-"+L);
		remove.setInputFormat(D);
		return Filter.useFilter(D, remove);
	}

	/**
	 * Remove Indices - Remove some labels (assume they are the first L attributes) from D.
	 * @param	D		Dataset
	 * @param	L 		number of labels
	 * @param	j		indices of labels to keep
	 * @return	New dataset with labels removed.
	 */
	public static Instances keepLabels(Instances D, int L, int j[]) throws Exception {
		int to_remove[] = A.invert(j,L);
		return remove(D,to_remove,false);
	}
}
