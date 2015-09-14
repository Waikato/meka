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

import java.util.HashMap;

public class LabelSetComparator extends LabelSet {

	HashMap<LabelSet,Integer> c = null;

	public LabelSetComparator(HashMap<LabelSet,Integer> c) {
		this.c = c;
	} 

	@Override
	//                  a negative integer, zero,     or a positive integer as the 
	//first argument is less than,          equal to, or greater than the second.
	public int compare(Object obj1, Object obj2) {

		LabelSet l1 = (LabelSet) obj1;
		LabelSet l2 = (LabelSet) obj2;

		if (l1.indices.length < l2.indices.length) {
			return -1;
		}
		else if (l1.indices.length > l2.indices.length) {
			return 1;
		}
		else {

			int c1 = this.c.get(l1);
			int c2 = this.c.get(l2);

			if (c2 > c1) {
				return -1;
			}
			else if (c1 > c2) {
				return 1;
			}
			else {
				return 0;
			}
		} 
	} 

}


