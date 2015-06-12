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
 * ResultHistory.java
 * Copyright (C) 2012-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import meka.core.Result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

/**
 * For maintaining a history of results.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ResultHistory
	implements Serializable {

	/** for serialization. */
	private static final long serialVersionUID = -2806420567531406970L;

	/** for storing the results (timestamp / result). */
	protected Hashtable<Date,Result> m_Results;

	/** for storing the suffixes for the index (timestamp / suffix). */
	protected Hashtable<Date,String> m_Suffixes;

	/** the ordered list of results. */
	protected ArrayList<Date> m_Ordered;
	
	/**
	 * Initializes the history.
	 */
	public ResultHistory() {
		m_Results  = new Hashtable<Date,Result>();
		m_Suffixes = new Hashtable<Date,String>();
		m_Ordered  = new ArrayList<Date>();
	}
	
	/**
	 * Empties the history.
	 */
	public synchronized void clear() {
		m_Results.clear();
		m_Suffixes.clear();
		m_Ordered.clear();
	}
	
	/**
	 * Returns the number of history items stored.
	 * 
	 * @return the size of the history
	 */
	public synchronized int size() {
		return m_Ordered.size();
	}
	
	/**
	 * Returns the specified history item.
	 * 
	 * @param index the index of the item to retrieve
	 * @return the item
	 */
	public synchronized Result get(int index) {
		return m_Results.get(m_Ordered.get(index));
	}
	
	/**
	 * Returns the specified timestamp.
	 * 
	 * @param index the index of the item to retrieve
	 * @return the timestamp
	 */
	public synchronized Date getTimestamp(int index) {
		return m_Ordered.get(index);
	}

	/**
	 * Returns the specified suffix.
	 *
	 * @param index the index of the item to retrieve
	 * @return the suffix
	 */
	public synchronized String getSuffix(int index) {
		return m_Suffixes.get(m_Ordered.get(index));
	}

	/**
	 * Adds the item to the history.
	 * 
	 * @param result the result to add
	 * @param suffix the suffix to add
	 */
	public synchronized void add(Result result, String suffix) {
		Date date;
		
		date = new Date();
		m_Results.put(date, result);
		m_Suffixes.put(date, suffix);
		m_Ordered.add(date);
	}
	
	/**
	 * Removes the specified entry.
	 * 
	 * @param index the history entry to remove
	 * @return the removed item
	 */
	public synchronized Result remove(int index) {
		Result result;
		Date date;
		
		date   = m_Ordered.remove(index);
		m_Suffixes.remove(date);
		result = m_Results.remove(date);
		
		return result;
	}
	
	/**
	 * Returns the history.
	 * 
	 * @return the history as string
	 */
	@Override
	public synchronized String toString() {
		StringBuilder result;
		
		result = new StringBuilder();
		result.append("[");
		for (Date date: m_Ordered) {
			if (result.length() > 1)
				result.append(", ");
			result.append(date);
			result.append(":");
			result.append(m_Suffixes.get(date));
			result.append("=");
			result.append(m_Results.get(date));
		}
		result.append("]");
		
		return result.toString();
	}
}
