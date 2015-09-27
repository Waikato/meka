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
 * TabSeparated.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import meka.core.ExceptionUtils;
import meka.core.FileUtils;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.RepeatedRuns;
import weka.core.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Exports the statistics to a tab-separated file.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class TabSeparated
  extends AbstractFileBasedEvaluationStatisticsExporter {

	private static final long serialVersionUID = -2891664931765964612L;

	/** the header for the classifier. */
	public final static String HEADER_CLASSIFIER = "Classifier";

	/** the header for the relation. */
	public final static String HEADER_RELATION = "Relation";

	@Override
	public String globalInfo() {
		return "Exports the statistics to a tab-separated file.";
	}

	/**
	 * Returns the format description.
	 *
	 * @return      the file format
	 */
	@Override
	public String getFormatDescription() {
		return "Tab-separated";
	}

	/**
	 * Returns the format extension(s).
	 *
	 * @return      the extension(s) (incl dot)
	 */
	@Override
	public String[] getFormatExtensions() {
		return new String[]{".tsv"};
	}

	/**
	 * Exports the statistics.
	 *
	 * @param stats         the statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	@Override
	public String export(List<EvaluationStatistics> stats) {
		String          result;
		HashSet<String> keys;
		List<String>    headers;
		FileWriter      fwriter;
		BufferedWriter  bwriter;
		int             i;

		result = null;

		// construct header
		keys = new HashSet<>();
		for (EvaluationStatistics stat: stats)
			keys.addAll(stat.keySet());
		headers = new ArrayList<>(keys);
		Collections.sort(headers);
		if (headers.contains(CrossValidation.KEY_FOLD)) {
			headers.remove(CrossValidation.KEY_FOLD);
			headers.add(0, CrossValidation.KEY_FOLD);
		}
		if (headers.contains(RepeatedRuns.KEY_RUN)) {
			headers.remove(RepeatedRuns.KEY_RUN);
			headers.add(0, RepeatedRuns.KEY_RUN);
		}
		headers.add(0, HEADER_RELATION);
		headers.add(0, HEADER_CLASSIFIER);

		fwriter = null;
		bwriter = null;
		try {
			fwriter = new FileWriter(m_File);
			bwriter = new BufferedWriter(fwriter);

			// output header
			for (i = 0; i < headers.size(); i++) {
				if (i > 0)
					bwriter.write("\t");
				bwriter.write(headers.get(i));
			}
			bwriter.newLine();
			bwriter.flush();

			// output statistics
			for (EvaluationStatistics stat: stats) {
				for (i = 0; i < headers.size(); i++) {
					if (i > 0)
						bwriter.write("\t");
					if (i == 0) {
						bwriter.write(Utils.toCommandLine(stat.getClassifier()));
					}
					else if (i == 1) {
						bwriter.write(stat.getRelation());
					}
					else {
						if (stat.containsKey(headers.get(i)))
							 bwriter.write("" + stat.get(headers.get(i)));
					}
				}
				bwriter.newLine();
				bwriter.flush();
			}
		}
		catch (Exception e) {
			result = ExceptionUtils.handleException(this, "Failed to export statistics to: " + m_File, e);
		}
		finally {
			FileUtils.closeQuietly(bwriter);
			FileUtils.closeQuietly(fwriter);
		}

		return result;
	}
}
