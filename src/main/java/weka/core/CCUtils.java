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

package weka.core;

import weka.core.*;
import java.util.*;

public abstract class CCUtils {

	/**
	 * LinkTransform - prepare 'D' for training at node 'j' of the chain, using index 'idx'.
	 * @return	the transformed dataset (which can be used as a template)
	 */
	public static Instances linkTransform(Instances D, int j, int idx, int exl[]) {
		Instances D_j = new Instances(D);
		D_j.setClassIndex(-1); 
		// delete all the attributes (and track where our index ends up)
		int ndx = idx;
		for(int i = exl.length-1; i >= 0; i--) {
			D_j.deleteAttributeAt(exl[i]);
			if (exl[i] < ndx)
				ndx--; 
		}
		D_j.setClassIndex(ndx); 
		return D_j;
	}

	public static Instance linkTransformation(Instance x, int excl[], Instances _template) {
		// copy
		Instance copy = (Instance)x.copy();
		copy.setDataset(null);

		// delete attributes we don't need
		for(int i = excl.length-1; i >= 0; i--) {
			copy.deleteAttributeAt(excl[i]);
		}

		//set template
		copy.setDataset(_template);

		return copy;
	}

}
