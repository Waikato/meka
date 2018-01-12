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
 * ExperimentFileChooser.java
 * Copyright (C) 2015-2018 University of Waikato, Hamilton, NZ
 */

package meka.gui.choosers;

import meka.core.ObjectUtils;
import meka.core.OptionUtils;
import meka.core.Project;
import meka.experiment.statisticsexporters.FileBasedEvaluationStatisticsExporter;
import meka.experiment.statisticsexporters.TabSeparated;
import meka.gui.goe.GenericObjectEditor;
import weka.core.PluginManager;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * File chooser for evaluation statistics exporters (only writers, no readers!).
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class EvaluationStatisticsExporterFileChooser
		extends AbstractConfigurableExtensionFileFilterFileChooser<FileBasedEvaluationStatisticsExporter,FileBasedEvaluationStatisticsExporter> {

	private static final long serialVersionUID = 1362264114946186967L;

	/** the file filters for the readers. */
	protected static List<ExtensionFileFilterWithClass> m_FileFilters;

	/**
	 * Returns whether the filters have already been initialized.
	 *
	 * @return		true if the filters have been initialized
	 */
	@Override
	protected boolean getFiltersInitialized() {
		return (m_FileFilters != null);
	}

	/**
	 * Performs the actual initialization of the filters.
	 */
	@Override
	protected void doInitializeFilters() {
		List<String> filters = PluginManager.getPluginNamesOfTypeList(FileBasedEvaluationStatisticsExporter.class .getName());
		m_FileFilters = new ArrayList<>();
		for (String filter: filters) {
			try {
				FileBasedEvaluationStatisticsExporter handler = (FileBasedEvaluationStatisticsExporter) Utils.forName(
						FileBasedEvaluationStatisticsExporter.class, filter, new String[0]);
				m_FileFilters.add(new ExtensionFileFilterWithClass(
						handler.getFormatExtensions(),
						handler.getFormatDescription() + " (" + ObjectUtils.flatten(handler.getFormatExtensions(), ", ") + ")",
						filter));
			}
			catch (Exception e) {
				System.err.println("Failed to instantiate file filter: " + filter);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the file filters for opening files.
	 *
	 * @return		the file filters
	 */
	@Override
	protected List<ExtensionFileFilterWithClass> getOpenFileFilters() {
		return m_FileFilters;
	}

	/**
	 * Returns the file filters for writing files.
	 *
	 * @return		the file filters
	 */
	@Override
	protected List<ExtensionFileFilterWithClass> getSaveFileFilters() {
		return m_FileFilters;
	}

	/**
	 * Returns the default reader.
	 *
	 * @return		the default reader
	 */
	@Override
	protected FileBasedEvaluationStatisticsExporter getDefaultReader() {
		return null;
	}

	/**
	 * Returns the reader superclass for the GOE.
	 *
	 * @return		the reader class
	 */
	@Override
	protected Class getReaderClass() {
		return null;
	}

	/**
	 * Returns the default writer.
	 *
	 * @return		the default writer
	 */
	@Override
	protected FileBasedEvaluationStatisticsExporter getDefaultWriter() {
		return new TabSeparated();
	}

	/**
	 * Returns the writer superclass for the GOE.
	 *
	 * @return		the writer class
	 */
	@Override
	protected Class getWriterClass() {
		return FileBasedEvaluationStatisticsExporter.class;
	}

	/**
	 * configures the current converter.
	 *
	 * @param dialogType		the type of dialog to configure for
	 */
	@Override
	protected void configureCurrentHandlerHook(int dialogType) {
		super.configureCurrentHandlerHook(dialogType);

		if (m_CurrentHandler != null) {
			if (getSelectedFile() != null)
				((FileBasedEvaluationStatisticsExporter) m_CurrentHandler).setFile(getSelectedFile());
		}
	}

	/**
	 * Only for testing.
	 *
	 * @param args          ignored
	 * @throws Exception    shouldn't occur
	 */
	public static void main(String[] args) throws Exception {
		Project.initialize();
		GenericObjectEditor.registerAllEditors();
		EvaluationStatisticsExporterFileChooser fchooser = new EvaluationStatisticsExporterFileChooser();
		int retVal = fchooser.showSaveDialog(null);
		System.out.println("OK? " + (retVal == EvaluationStatisticsExporterFileChooser.APPROVE_OPTION));
		System.out.println("file: " + fchooser.getSelectedFile());
		System.out.println("writer: " + OptionUtils.toCommandLine(fchooser.getWriter()));
	}
}
