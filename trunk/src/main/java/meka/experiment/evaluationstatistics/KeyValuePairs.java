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
import meka.core.ExceptionUtils;
import meka.core.FileUtils;
import meka.core.OptionUtils;
import weka.core.Instances;
import weka.core.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Simple plain text format. One statistics object per line, as tab-separated key-value pairs.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class KeyValuePairs
  extends AbstractFileBasedEvaluationStatisticsHandler
  implements IncrementalEvaluationStatisticsHandler {

	private static final long serialVersionUID = -1090631157162943295L;

	/** the key for the classifier. */
	public final static String KEY_CLASSIFIER = "Classifier";

	/** the key for the relation. */
	public final static String KEY_RELATION = "Relation";

	/** the statistics so far. */
	protected List<EvaluationStatistics> m_Statistics = new ArrayList<>();

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
			if (m_File.exists()) {
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
						System.err.println("Failed to parse: " + entry);
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
							System.err.println("Failed to parse double value of '" + key + "': " + raw.get(key));
						}
					}
					result.add(stat);
				}
			}
		}
		catch (Exception e) {
			result = null;
			ExceptionUtils.handleException(this, "Failed to read serialized statistics from: " + m_File, e);
		}
		finally {
			FileUtils.closeQuietly(breader);
			FileUtils.closeQuietly(freader);
		}

		return result;
	}

	/**
	 * Returns whether the handler supports incremental write.
	 *
	 * @return      true if supported
	 */
	@Override
	public boolean supportsIncrementalUpdate() {
		return true;
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
			return ExceptionUtils.handleException(this, "Failed to write statistics to: " + m_File, e);
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
