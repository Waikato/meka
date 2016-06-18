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
 * ReevaluateModelOnTestset.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package meka.gui.explorer.classify;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import meka.core.OptionUtils;
import meka.core.Result;
import meka.gui.core.ResultHistoryList;
import meka.gui.explorer.ClassifyTab;
import weka.core.Instances;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Reevaluates the model on the current test set (if any loaded).
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ReevaluateModelOnTestset
		extends AbstractClassifyResultHistoryPlugin {
	private static final long serialVersionUID = -1152575716154907544L;

	/**
	 * Returns the group of the plugin. Used for the grouping the menu items.
	 *
	 * @return          the group
	 */
	public String getGroup() {
		return "Evaluation";
	}

	/**
	 * Returns the name of the plugin. Used for the menu item text.
	 *
	 * @return          the name
	 */
	@Override
	public String getName() {
		return "Reevaluate model on test set";
	}

	/**
	 * Checks whether the current item can be handled. Disables/enables the menu item.
	 *
	 * @param history   the current history
	 * @param index     the selected history item
	 * @return          true if can be handled
	 */
	@Override
	public boolean handles(ResultHistoryList history, int index) {
		return (getClassifier(history, index) != null)
			&& (((ClassifyTab) getOwner()).getTestData() != null);
	}

	/**
	 * Returns the action lister to use in the menu.
	 *
	 * @param history   the current history
	 * @param index     the selected history item
	 * @return          the listener
	 */
	@Override
	public ActionListener getActionListener(final ResultHistoryList history, final int index) {
		final MultiLabelClassifier classifier = (MultiLabelClassifier) getClassifier(history, index);
		final Instances header = getHeader(history, index);

		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Runnable run = new Runnable() {
					@Override
					public void run() {
						ClassifyTab owner = (ClassifyTab) getOwner();
						Result result;
						Instances test;
						owner.startBusy("Reevaluate on test...");
						try {
							MLUtils.prepareData(owner.getTestData());
							test       = new Instances(owner.getTestData());
							test.setClassIndex(owner.getTestData().classIndex());
							String msg = header.equalHeadersMsg(test);
							if (msg != null)
								throw new IllegalArgumentException("Model's training set and current test set are not compatible:\n" + msg);
							owner.log(OptionUtils.toCommandLine(classifier));
							owner.log("Testset: " + test.relationName());
							owner.log("Class-index: " + test.classIndex());
							result = Evaluation.evaluateModel(classifier, test, "0.0", owner.getVOP());  // TODO what threshold to use?
							owner.addResultToHistory(
								result,
								new Object[]{classifier, new Instances(test, 0)},
								classifier.getClass().getName().replace("meka.classifiers.", "")
							);
							owner.finishBusy();
						}
						catch (Exception e) {
							owner.handleException("Reevaluation failed on test set:", e);
							owner.finishBusy("Reevaluation failed: " + e);
							JOptionPane.showMessageDialog(
								owner,
								"Reevaluation failed:\n" + e,
								"Error",
								JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				((ClassifyTab) getOwner()).start(run);
			}
		};
	}
}
