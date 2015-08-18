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

import java.util.Map;

/**
 * Interface for classes that generate graphs per label.
 *
 * @author fracpete
 */
public interface MultiLabelDrawable {
	int NOT_DRAWABLE = 0, TREE = 1, BayesNet = 2, Newick = 3;

	/**
	 * Returns the type of graph representing
	 * the object.
	 *
	 * @return the type of graph representing the object (label index as key)
	 */
	public Map<Integer,Integer> graphType();

	/**
	 * Returns a string that describes a graph representing
	 * the object. The string should be in XMLBIF ver.
	 * 0.3 format if the graph is a BayesNet, otherwise
	 * it should be in dotty format.
	 *
	 * @return the graph described by a string (label index as key)
	 * @throws Exception if the graph can't be computed
	 */
	public Map<Integer,String> graph() throws Exception;
}
