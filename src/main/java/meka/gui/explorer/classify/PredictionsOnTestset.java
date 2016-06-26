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
 * PredictionsOnTestset.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package meka.gui.explorer.classify;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.classifiers.multitarget.MultiTargetClassifier;
import meka.core.MLUtils;
import meka.gui.core.GUIHelper;
import meka.gui.core.ResultHistoryList;
import meka.gui.dataviewer.DataViewerDialog;
import meka.gui.explorer.ClassifyTab;
import weka.core.Instance;
import weka.core.Instances;

import javax.swing.JOptionPane;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * Makes predictions on the current test set (if any loaded).
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PredictionsOnTestset
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
		return "Predictions on test set";
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
						Instances test;
						owner.startBusy("Predictions on test...");
						try {
							MLUtils.prepareData(owner.getTestData());
							test       = new Instances(owner.getTestData());
							test.setClassIndex(owner.getTestData().classIndex());
							String msg = header.equalHeadersMsg(test);
							if (msg != null)
								throw new IllegalArgumentException("Model's training set and current test set are not compatible:\n" + msg);
							// collect predictions
							Instances predicted = new Instances(test, 0);
							for (int i = 0; i < test.numInstances(); i++) {
								double pred[] = classifier.distributionForInstance(test.instance(i));
								// Cut off any [no-longer-needed] probabalistic information from MT classifiers.
								if (classifier instanceof MultiTargetClassifier)
									pred = Arrays.copyOf(pred, test.classIndex());
								Instance predInst = (Instance) test.instance(i).copy();
								for (int j = 0; j < pred.length; j++)
									predInst.setValue(j, pred[j]);
								predicted.add(predInst);
								if ((i+1) % 100 == 0)
									owner.showStatus("Predictions on test (" + (i+1) + "/" + test.numInstances() + ")...");
							}
							owner.finishBusy();
							// display predictions
							DataViewerDialog dialog = new DataViewerDialog(GUIHelper.getParentFrame(owner), ModalityType.MODELESS);
							dialog.setDefaultCloseOperation(DataViewerDialog.DISPOSE_ON_CLOSE);
							dialog.setInstances(predicted);
							dialog.setSize(800, 600);
							dialog.setLocationRelativeTo(owner);
							dialog.setVisible(true);
						}
						catch (Exception e) {
							owner.handleException("Predictions failed on test set:", e);
							owner.finishBusy("Predictions failed: " + e);
							JOptionPane.showMessageDialog(
								owner,
								"Predictions failed:\n" + e,
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
