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
 * KeyValuePairs.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.evaluationstatistics;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.FileUtils;
import meka.core.OptionUtils;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Simple plain text format. One statistics object per line, as tab-separated key-value pairs.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class KeyValuePairs
  extends AbstractFileBasedEvaluationStatisticsHandler
  implements OptionalIncrementalEvaluationStatisticsHandler {

	private static final long serialVersionUID = -1090631157162943295L;

	/** the key for the classifier. */
	public final static String KEY_CLASSIFIER = "Classifier";

	/** the key for the relation. */
	public final static String KEY_RELATION = "Relation";

	/** the statistics so far. */
	protected List<EvaluationStatistics> m_Statistics = new ArrayList<>();

	/** whether the incremental mode is off. */
	protected boolean m_IncrementalDisabled;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	public String globalInfo() {
		return "Simple plain text format that places one statistcs result per line, as tab-separated "
				+ "key-value pairs (separated by '=').";
	}

	/**
	 * Returns the format description.
	 *
	 * @return      the file format
	 */
	public String getFormatDescription() {
		return "Key-value pairs";
	}

	/**
	 * Returns the format extension(s).
	 *
	 * @return      the extension(s) (incl dot)
	 */
	public String[] getFormatExtensions() {
		return new String[]{".txt"};
	}

	/**
	 * Sets whether incremental model is turned off.
	 *
	 * @param value     true to turn off incremental mode
	 */
	public void setIncrementalDisabled(boolean value) {
		m_IncrementalDisabled = value;
	}

	/**
	 * Returns whether incremental mode is turned off.
	 *
	 * @return          true if incremental mode is pff
	 */
	public boolean isIncrementalDisabled() {
		return m_IncrementalDisabled;
	}

	/**
	 * Describes this property.
	 *
	 * @return          the description
	 */
	public String incrementalDisabledTipText() {
		return "If enabled, incremental mode is turned off.";
	}

	/**
	 * Returns whether the handler is threadsafe.
	 *
	 * @return      true if threadsafe
	 */
	@Override
	public boolean isThreadSafe() {
		return m_IncrementalDisabled;
	}

	/**
	 * Returns whether the handler supports incremental write.
	 *
	 * @return      true if supported
	 */
	@Override
	public boolean supportsIncrementalUpdate() {
		return !m_IncrementalDisabled;
	}

	/**
	 * Returns an enumeration of all the available options.
	 *
	 * @return an enumeration of all available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector result = new Vector();
		OptionUtils.add(result, super.listOptions());
		OptionUtils.addOption(result, incrementalDisabledTipText(), "no", "incremental-disabled");
		return OptionUtils.toEnumeration(result);
	}

	/**
	 * Sets the options.
	 *
	 * @param options       the options
	 * @throws Exception    never
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		setIncrementalDisabled(Utils.getFlag("incremental-disabled", options));
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
		OptionUtils.add(result, "incremental-disabled", isIncrementalDisabled());
		return OptionUtils.toArray(result);
	}

	/**
	 * Initializes the handler.
	 *
	 * @return      null if successfully initialized, otherwise error message
	 */
	@Override
	public String initialize() {
		String      result;

		result = super.initialize();

		if (result == null) {
			m_Statistics.clear();
			if (m_File.exists() && !m_IncrementalDisabled) {
				log("File '" + m_File + "' exists, loading...");
				m_Statistics.addAll(read());
			}
		}

		return result;
	}

	/**
	 * Reads the statistics.
	 *
	 * @return              the statistics that were read
	 */
	@Override
	public List<EvaluationStatistics> read() {
		List<EvaluationStatistics>  result;
		String                      line;
		String[]                    parts;
		String[]                    entries;
		HashMap<String,String>      raw;
		EvaluationStatistics        stat;
		BufferedReader              breader;
		FileReader                  freader;

		result  = new ArrayList<>();
		freader = null;
		breader = null;
		try {
			freader = new FileReader(m_File);
			breader = new BufferedReader(freader);
			while ((line = breader.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				entries = line.split("\t");
				raw   = new HashMap<>();
				for (String entry: entries) {
					if (entry.trim().isEmpty())
						continue;
					parts = entry.split("=");
					if (parts.length == 2)
						raw.put(parts[0], parts[1]);
					else
						log("Failed to parse: " + entry);
				}
				if (raw.containsKey(KEY_CLASSIFIER) && raw.containsKey(KEY_RELATION)) {
					stat = new EvaluationStatistics(
							(MultiLabelClassifier) OptionUtils.fromCommandLine(MultiLabelClassifier.class, raw.get(KEY_CLASSIFIER)),
							raw.get(KEY_RELATION),
							null);
					for (String key: raw.keySet()) {
						if (key.equals(KEY_CLASSIFIER) || key.equals(KEY_RELATION))
							continue;
						try {
							stat.put(key, Double.parseDouble(raw.get(key)));
						}
						catch (Exception e) {
							log("Failed to parse double value of '" + key + "': " + raw.get(key));
						}
					}
					result.add(stat);
				}
			}
		}
		catch (Exception e) {
			result = null;
			handleException("Failed to read serialized statistics from: " + m_File, e);
		}
		finally {
			FileUtils.closeQuietly(breader);
			FileUtils.closeQuietly(freader);
		}

		return result;
	}

	/**
	 * Checks whether the specified combination of classifier and dataset is required for evaluation
	 * or already present from previous evaluation.
	 *
	 * @param classifier    the classifier to check
	 * @param dataset       the dataset to check
	 * @return              true if it needs evaluating
	 */
	public boolean requires(MultiLabelClassifier classifier, Instances dataset) {
		boolean     result;
		String      cls;
		String      rel;

		result = true;

		cls = Utils.toCommandLine(classifier);
		rel = dataset.relationName();

		for (EvaluationStatistics stat: m_Statistics) {
			if (stat.getCommandLine().equals(cls) && stat.getRelation().equals(rel)) {
				result = false;
				break;
			}
		}

		return result;
	}
	/**
	 * Retrieves the statis for the specified combination of classifier and dataset.
	 *
	 * @param classifier    the classifier to check
	 * @param dataset       the dataset to check
	 * @return              the stats, null if not available
	 */
	public List<EvaluationStatistics> retrieve(MultiLabelClassifier classifier, Instances dataset) {
		List<EvaluationStatistics>  result;
		String                      cls;
		String                      rel;

		result = new ArrayList<>();

		cls = Utils.toCommandLine(classifier);
		rel = dataset.relationName();

		for (EvaluationStatistics stat: m_Statistics) {
			if (stat.getCommandLine().equals(cls) && stat.getRelation().equals(rel))
				result.add(stat);
		}

		return result;
	}

	/**
	 * Adds the given statistics.
	 *
	 * @param stats         the statistics to store
	 * @return              null if successfully stored, otherwise error message
	 */
	@Override
	public String append(List<EvaluationStatistics> stats) {
		BufferedWriter  bwriter;
		FileWriter      fwriter;

		log("Writing " + stats.size() + " statistics to: " + m_File);

		bwriter = null;
		fwriter = null;
		try {
			fwriter = new FileWriter(m_File, true);
			bwriter = new BufferedWriter(fwriter);
			for (EvaluationStatistics stat: stats) {
				bwriter.write(KEY_CLASSIFIER + "=" + stat.getCommandLine());
				bwriter.write("\t");
				bwriter.write(KEY_RELATION + "=" + stat.getRelation());
				bwriter.write("\t");
				for (String key: stat.keySet()) {
					bwriter.write("\t");
					bwriter.write(key + "=" + stat.get(key));
				}
				bwriter.newLine();
			}
			return null;
		}
		catch (Exception e) {
			return handleException("Failed to write statistics to: " + m_File, e);
		}
		finally {
			FileUtils.closeQuietly(bwriter);
			FileUtils.closeQuietly(fwriter);
		}
	}

	/**
	 * Stores the given statistics.
	 *
	 * @param stats         the statistics to store
	 * @return              null if successfully stored, otherwise error message
	 */
	@Override
	public String write(List<EvaluationStatistics> stats) {
		return append(stats);
	}

	/**
	 * Gets called after the experiment finished.
	 *
	 * @return          null if successfully finished, otherwise error message
	 */
	public String finish() {
		return null;
	}
}
