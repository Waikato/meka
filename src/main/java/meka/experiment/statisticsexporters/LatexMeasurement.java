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
 * LatexMeasurement.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.statisticsexporters;

import meka.core.*;
import meka.experiment.evaluationstatistics.EvaluationStatistics;
import meka.experiment.evaluationstatistics.EvaluationStatisticsUtils;
import weka.core.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

/**
 * Exports a single statistic to a tab-separated file.
 * First column are datasets, first row are classifiers.
 * Automatically aggregates the statistics and displays the "mean".
 *
 * @author Jesse Read
 * @version $Revision$
 */
public class LatexMeasurement
  extends AbstractMeasurementEvaluationStatisticsExporter {

	private static final long serialVersionUID = -2891664931765964612L;

	/**
	 * Description to be displayed in the GUI.
	 *
	 * @return      the description
	 */
	@Override
	public String globalInfo() {
		return "Exports a single statistic to a LaTex file.\n"
				+ "First column are datasets, first row are classifiers.\n"
				+ "Automatically aggregates the statistics and displays the 'mean'.";
	}

	/**
	 * Returns the format description.
	 *
	 * @return      the file format
	 */
	@Override
	public String getFormatDescription() {
		return "LaTex statistic";
	}

	/**
	 * Returns the format extension(s).
	 *
	 * @return      the extension(s) (incl dot)
	 */
	@Override
	public String[] getFormatExtensions() {
		return new String[]{".tex"};
	}

	/**
	 * Exports the aggregated statistics.
	 *
	 * @param stats         the aggregated statistics to export
	 * @return              null if successfully exported, otherwise error message
	 */
	@Override
	protected String doExportAggregated(List<EvaluationStatistics> stats) {
		String          result;
		List<String>    classifiers;
		List<String>    relations;
		List<Number>    measurements;
		FileWriter      fwriter;
		BufferedWriter  bwriter;
		int             i;

		result = null;

		classifiers = EvaluationStatisticsUtils.commandLines(stats, true);
		relations   = EvaluationStatisticsUtils.relations(stats, true);
		fwriter = null;
		bwriter = null;
		try {
			fwriter = new FileWriter(m_File);
			bwriter = new BufferedWriter(fwriter);

			// output header
			String tabularConfig = "lllllllllllllllllllllllllllllllllllllllllllllllllll".substring(0,classifiers.size()+1);
			bwriter.write("% " + m_Measurement);
			bwriter.newLine();
			bwriter.write("\\begin{tabular}{"+tabularConfig+"}");
			bwriter.newLine();
			bwriter.write("\\hline");
			bwriter.newLine();
			bwriter.write(String.format("%12s ", "Datasets"));
			for (i = 0; i < classifiers.size(); i++) {
				String name = MLUtils.getShortMethodName(classifiers.get(i));
				name = LatexUtils.escape(name);
				bwriter.write(String.format("& [%d] %-6s", (i + 1), name));
			}
			bwriter.write("\\\\");
			bwriter.newLine();
			bwriter.write("\\hline");
			bwriter.newLine();
			bwriter.flush();

			int ranks[][] = EvaluationStatisticsUtils.rankMatrix(stats,m_Measurement);

			// output statistics
			for (int d = 0; d < relations.size(); d++) {
				String relation = relations.get(d);
				String name = MLUtils.getRelationName(relation);
				name = LatexUtils.escape(name);
				bwriter.write(String.format("%12s ", name));
				for (i = 0; i < classifiers.size(); i++) {
					measurements = EvaluationStatisticsUtils.measurements(stats, classifiers.get(i), relation, m_Measurement);
					if (measurements.size() > 0) {
						if (measurements.size() > 1)
							log("Found " + measurements.size() + " measurements for combination " + classifiers.get(i) + "/" + relation);
						String value = String.format("& %5.3f (%d) ", measurements.get(0), ranks[d][i]);
						bwriter.write(value);
					}
				}
				bwriter.write(" \\\\");
				bwriter.newLine();
				bwriter.flush();
			}
			bwriter.write("\\hline");
			bwriter.newLine();
			bwriter.write(String.format("%12s ", "Avg.\\ Rank"));
			for (i = 0; i < classifiers.size(); i++) {
				String value = String.format("& %5.3f      ", Utils.mean(A.toDoubleArray(MatrixUtils.getCol(ranks, i))));
				bwriter.write(value);
			}
			bwriter.write(" \\\\");
			bwriter.newLine();
			bwriter.write("\\hline");
			bwriter.newLine();
			bwriter.write("\\end{tabular}");
			bwriter.newLine();
			bwriter.flush();

			// output key
			bwriter.newLine();
			bwriter.write("%Index");
			bwriter.write("\t");
			bwriter.write("Classifier");
			bwriter.newLine();
			for (i = 0; i < classifiers.size(); i++) {
				bwriter.write("%[" + (i+1) + "]");
				bwriter.write("\t");
				bwriter.write(classifiers.get(i));
				bwriter.newLine();
			}
		}
		catch (Exception e) {
			result = handleException("Failed to export statistics to: " + m_File, e);
		}
		finally {
			FileUtils.closeQuietly(bwriter);
			FileUtils.closeQuietly(fwriter);
		}

		return result;
	}
}
