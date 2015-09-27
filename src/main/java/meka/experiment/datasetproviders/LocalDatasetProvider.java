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
 * LocalDatasetProvider.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.datasetproviders;

import meka.core.ExceptionUtils;
import meka.core.MLUtils;
import meka.core.OptionUtils;
import weka.core.Instances;
import weka.core.Option;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.util.*;

/**
 * Loads local files from disk.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class LocalDatasetProvider
		extends AbstractDatasetProvider {

	private static final long serialVersionUID = 2167509900278245507L;

	/** the files to load. */
	protected List<File> m_Datasets = new ArrayList<>();

	/** the iterator index. */
	protected int m_Current;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Loads local files from disk.";
	}

	/**
	 * Sets the datasets to use in the experiment.
	 *
	 * @param value     the datasets
	 */
	public void setDatasets(File[] value) {
		m_Datasets.clear();
		if (value != null)
			m_Datasets.addAll(Arrays.asList(value));
	}

	/**
	 * Returns the datasets to use in the experiment.
	 *
	 * @return          the datasets
	 */
	public File[] getDatasets() {
		return m_Datasets.toArray(new File[m_Datasets.size()]);
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String datasetsTipText() {
		return "The datasets to load.";
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
		OptionUtils.addOption(result, datasetsTipText(), "none", "dataset");
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
		setDatasets(OptionUtils.parse(options, "dataset", File.class));
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
		OptionUtils.add(result, "dataset", getDatasets());
		return OptionUtils.toArray(result);
	}

	/**
	 * Initializes the provider to start providing datasets from scratch.
	 *
	 * @return      null if successfully initialized, otherwise error message
	 */
	@Override
	public String initialize() {
		m_Current = 0;
		for (File dataset: m_Datasets) {
			if (!dataset.exists())
				return "Dataset does not exist: " + dataset;
			if (dataset.isDirectory())
				return "Dataset points to a directory: " + dataset;
		}
		return null;
	}

	/**
	 * Returns whether another dataset is available.
	 *
	 * @return      true if another dataset is available
	 */
	@Override
	public boolean hasNext() {
		return (m_Current < m_Datasets.size());
	}

	/**
	 * Returns the next dataset.
	 *
	 * @return      the next dataset, null in case of an error
	 */
	@Override
	public Instances next() {
		Instances result;
		try {
			result = ConverterUtils.DataSource.read(m_Datasets.get(m_Current).getAbsolutePath());
			MLUtils.prepareData(result);
		}
		catch (Exception e) {
			result = null;
			ExceptionUtils.handleException(this, "Failed to load dataset: " + m_Datasets.get(m_Current), e);
		}
		m_Current++;
		return result;
	}
}
