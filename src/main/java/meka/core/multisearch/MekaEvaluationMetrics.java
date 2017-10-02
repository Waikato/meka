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
 * MekaEvaluationMetrics.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package meka.core.multisearch;

import weka.classifiers.meta.multisearch.AbstractEvaluationMetrics;
import weka.core.Tag;

/**
 * Default metrics.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MekaEvaluationMetrics
	extends AbstractEvaluationMetrics {

	private static final long serialVersionUID = 8549253661958964524L;

	/** evaluation via: Accuracy. */
	public static final int EVALUATION_ACC = 0;

	/** evaluation via: Jaccard index. */
	public static final int EVALUATION_JACCARDINDEX = 1;

	/** evaluation via: Hamming score. */
	public static final int EVALUATION_HAMMINGSCORE = 2;

	/** evaluation via: Exact match. */
	public static final int EVALUATION_EXACTMATCH = 3;

	/** evaluation via: Jaccard distance. */
	public static final int EVALUATION_JACCARDDISTANCE = 4;

	/** evaluation via: Hamming loss. */
	public static final int EVALUATION_HAMMINGLOSS = 5;

	/** evaluation via: ZeroOne loss. */
	public static final int EVALUATION_ZEROONELOSS = 6;

	/** evaluation via: Harmonic score. */
	public static final int EVALUATION_HARMONICSCORE = 7;

	/** evaluation via: One error. */
	public static final int EVALUATION_ONEERROR = 8;

	/** evaluation via: Rank loss. */
	public static final int EVALUATION_RANKLOSS = 9;

	/** evaluation via: Avg precision. */
	public static final int EVALUATION_AVGPRECISION = 10;

	/** evaluation via: Log Loss (lim. L). */
	public static final int EVALUATION_LOGLOSSLIML = 11;

	/** evaluation via: Log Loss (lim. D). */
	public static final int EVALUATION_LOGLOSSLIMD = 12;

	/** evaluation via: F1 (micro averaged). */
	public static final int EVALUATION_F1MICRO = 13;

	/** evaluation via: F1 (macro averaged by example). */
	public static final int EVALUATION_F1MACROEXAMPLE = 14;

	/** evaluation via: F1 (macro averaged by label). */
	public static final int EVALUATION_F1MACROLABEL = 15;

	/** evaluation via: AUPRC (macro averaged). */
	public static final int EVALUATION_AUPRCMACRO = 16;

	/** evaluation via: AUROC (macro averaged). */
	public static final int EVALUATION_AUROCMACRO = 17;

	/** evaluation via: Label cardinality (predicted). */
	public static final int EVALUATION_LABELCARDINALITY = 18;

	/** evaluation via: Levenshtein distance. */
	public static final int EVALUATION_LEVENSHTEINDISTANCE = 19;

	/** evaluation. */
	protected static final Tag[] TAGS_EVALUATION = {
		new Tag(EVALUATION_ACC, "ACC", "Accuracy"),
		new Tag(EVALUATION_JACCARDINDEX, "JIDX", "Jaccard index"),
		new Tag(EVALUATION_HAMMINGSCORE, "HSCORE", "Hamming score"),
		new Tag(EVALUATION_EXACTMATCH, "EM", "Exact match"),
		new Tag(EVALUATION_JACCARDDISTANCE, "JDIST", "Jaccard distance"),
		new Tag(EVALUATION_HAMMINGLOSS, "HLOSS", "Hamming loss"),
		new Tag(EVALUATION_ZEROONELOSS, "ZOLOSS", "ZeroOne loss"),
		new Tag(EVALUATION_HARMONICSCORE, "HARSCORE", "Harmonic score"),
		new Tag(EVALUATION_ONEERROR, "OE", "One error"),
		new Tag(EVALUATION_RANKLOSS, "RLOSS", "Rank loss"),
		new Tag(EVALUATION_AVGPRECISION, "AVGPREC", "Avg precision"),
		new Tag(EVALUATION_LOGLOSSLIML, "LOGLOSSL", "Log Loss (lim. L)"),
		new Tag(EVALUATION_LOGLOSSLIMD, "LOGLOSSD", "Log Loss (lim. D)"),
		new Tag(EVALUATION_F1MICRO, "F1MICRO", "F1 (micro averaged)"),
		new Tag(EVALUATION_F1MACROEXAMPLE, "F1MACROEX", "F1 (macro averaged by example)"),
		new Tag(EVALUATION_F1MACROLABEL, "F1MACROLBL", "F1 (macro averaged by label)"),
		new Tag(EVALUATION_AUPRCMACRO, "AUPRC", "AUPRC (macro averaged)"),
		new Tag(EVALUATION_AUROCMACRO, "AUROC", "AUROC (macro averaged)"),
		new Tag(EVALUATION_LABELCARDINALITY, "LCARD", "Label cardinality (predicted)"),
		new Tag(EVALUATION_LEVENSHTEINDISTANCE, "LDIST", "Levenshtein distance"),
	};

	/**
	 * Returns the tags to used in the GUI.
	 *
	 * @return		the tags
	 */
	@Override
	public Tag[] getTags() {
		return TAGS_EVALUATION;
	}

	/**
	 * Returns the ID of default metric to use.
	 *
	 * @return		the default
	 */
	@Override
	public int getDefaultMetric() {
		return EVALUATION_ACC;
	}

	/**
	 * Returns whether to negate the metric for sorting purposes.
	 *
	 * @param id		the metric id
	 * @return		true if to invert
	 */
	public boolean invert(int id) {
		switch (id) {
			case EVALUATION_ACC:
			case EVALUATION_AVGPRECISION:
			case EVALUATION_F1MICRO:
			case EVALUATION_F1MACROEXAMPLE:
			case EVALUATION_F1MACROLABEL:
			case EVALUATION_AUPRCMACRO:
			case EVALUATION_AUROCMACRO:
			case EVALUATION_HAMMINGSCORE:
			case EVALUATION_HARMONICSCORE:
			case EVALUATION_JACCARDINDEX:
				return true;
			default:
				return false;
		}
	}
}
