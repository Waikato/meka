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
 * MultiDatasetProvider.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.datasetproviders;

import meka.core.OptionUtils;
import meka.events.LogListener;
import weka.core.Instances;
import weka.core.Option;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Combines multiple dataset providers.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MultiDatasetProvider
		extends AbstractDatasetProvider {

	private static final long serialVersionUID = 5886187555928103838L;

	/** the datasset providers to use. */
	protected DatasetProvider[] m_Providers = getDefaultProviders();

	/** the iterator index. */
	protected int m_Current;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Combines multiple dataset providers.";
	}

	/**
	 * Returns the default dataset providers to use.
	 *
	 * @return          the providers
	 */
	protected DatasetProvider[] getDefaultProviders() {
		return new DatasetProvider[0];
	}

	/**
	 * Sets the dataset providers to use.
	 *
	 * @param value     the providers
	 */
	public void setProviders(DatasetProvider[] value) {
		m_Providers = value;
	}

	/**
	 * Returns the dataset providers to use.
	 *
	 * @return          the providers
	 */
	public DatasetProvider[] getProviders() {
		return m_Providers;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String providersTipText() {
		return "The dataset providers to use.";
	}

	/**
	 * Returns an enumeration of all the available options..
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.add(result, super.listOptions());
		OptionUtils.addOption(result, providersTipText(), "none", "provider");
		return OptionUtils.toEnumeration(result);
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options
	 * @throws Exception    if parsing of options fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		setProviders(OptionUtils.parse(options, "provider", DatasetProvider.class));
		super.setOptions(options);
	}

	/**
	 * Returns the options.
	 *
	 * @return              the options
	 */
	@Override
	public String[] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, super.getOptions());
		OptionUtils.add(result, "provider", getProviders());
		return OptionUtils.toArray(result);
	}

	/**
	 * Adds the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void addLogListener(LogListener l) {
		super.addLogListener(l);
		for (DatasetProvider provider : m_Providers)
			provider.addLogListener(l);
	}

	/**
	 * Remove the log listener to use.
	 *
	 * @param l         the listener
	 */
	public void removeLogListener(LogListener l) {
		super.removeLogListener(l);
		for (DatasetProvider provider : m_Providers)
			provider.removeLogListener(l);
	}

	/**
	 * Initializes the provider to start providing datasets from scratch.
	 *
	 * @return      null if successfully initialized, otherwise error message
	 */
	@Override
	public String initialize() {
		String  result;
		int     i;

		result    = null;
		m_Current = 0;
		for (i = 0; i < m_Providers.length; i++) {
			result = m_Providers[i].initialize();
			if (result != null) {
				result = "Provider #" + (i+1) + ": " + result;
				break;
			}
		}
		return result;
	}

	/**
	 * Returns whether another dataset is available.
	 *
	 * @return      true if another dataset is available
	 */
	@Override
	public boolean hasNext() {
		return (m_Current < m_Providers.length) && (m_Providers[m_Current].hasNext());
	}

	/**
	 * Returns the next dataset.
	 *
	 * @return      the next dataset
	 */
	@Override
	public Instances next() {
		while (m_Current < m_Providers.length) {
			if (m_Providers[m_Current].hasNext()) {
				log("Dataset provider #" + (m_Current + 1));
				return m_Providers[m_Current].next();
			}
			m_Current++;
		}
		return null;
	}
}
