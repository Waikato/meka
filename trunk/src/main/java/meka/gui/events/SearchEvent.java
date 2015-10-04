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

/*
 * SearchEvent.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.events;

import meka.gui.core.SearchPanel;
import meka.gui.core.SearchParameters;

import java.util.EventObject;

/**
 * Event gets sent when a search is to be initiated.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4584 $
 */
public class SearchEvent
		extends EventObject {

	/** for serialization. */
	private static final long serialVersionUID = 1051763837438899324L;

	/** the search parameters. */
	protected SearchParameters m_Parameters;

	/**
	 * Initializes the event.
	 *
	 * @param source	the search panel that sent the event
	 * @param searchText	the actual text to search for
	 * @param regExp	whether it is a regular expression search
	 */
	public SearchEvent(SearchPanel source, String searchText, boolean regExp) {
		super(source);

		m_Parameters = new SearchParameters(searchText, regExp);
	}

	/**
	 * Returns the search panel that sent the event.
	 *
	 * @return		the panel
	 */
	public SearchPanel getSearchPanel() {
		return (SearchPanel) getSource();
	}

	/**
	 * Returns the search parameters.
	 *
	 * @return		the search parameters
	 */
	public SearchParameters getParameters() {
		return m_Parameters;
	}

	/**
	 * Returns a string representation of the event.
	 *
	 * @return		the string representation
	 */
	public String toString() {
		return "SearchPanel=" + getSource() + ", " + m_Parameters;
	}
}
