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
 * ClassifyTab.java
 * Copyright (C) 2012-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.classifiers.multilabel.incremental.IncrementalEvaluation;
import meka.core.MLUtils;
import meka.core.Result;
import meka.gui.core.GUIHelper;
import meka.gui.core.ResultHistoryList;
import meka.gui.explorer.classify.AbstractClassifyResultHistoryPlugin;
import meka.gui.goe.GenericObjectEditor;
import weka.core.Instances;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Random;

/**
 * Simple panel for performing classification.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ClassifyTab
		extends AbstractThreadedExplorerTab {

	/** for serialization. */
	private static final long serialVersionUID = 2158821659456232147L;

	/**
	 * Customizer for the history's popup menu.
	 *
	 * @author  fracpete (fracpete at waikato dot ac dot nz)
	 * @version $Revision$
	 */
	public class HistoryCustomizer
			implements Serializable, ResultHistoryList.ResultHistoryPopupMenuCustomizer {

		private static final long serialVersionUID = -4620304034855328840L;

		/**
		 * Allows to customize the popup menu for the result history.
		 *
		 * @param history the list this popup menu is for
		 * @param index the index of the select item from the history
		 * @param menu the menu to customize
		 */
		@Override
		public void customizePopupMenu(ResultHistoryList history, int index, JPopupMenu menu) {
			menu.addSeparator();
			AbstractClassifyResultHistoryPlugin.populateMenu(ClassifyTab.this, history, index, menu);
		}
	}

	/** cross-validation. */
	public final static String TYPE_CROSSVALIDATION = "Cross-validation";

	/** train/test split. */
	public final static String TYPE_TRAINTESTSPLIT = "Train/test split";

	/** incremental batch train/test split. */
	public final static String TYPE_INCREMENTAL    = "Prequential (incremental)";

	/** the GOE for the classifier. */
	protected GenericObjectEditor m_GenericObjectEditor;

	/** the text area for displaying the results. */
	protected JTextArea m_TextAreaResults;

	/** the result history. */
	protected ResultHistoryList m_ResultHistoryList;

	/** the button for running an experiment. */
	protected JButton m_ButtonStart;

	/** the button for stopping an experiment. */
	protected JButton m_ButtonStop;

	/** the type of experiment to perform. */
	protected JComboBox m_ComboBoxExperiment;

	/** the button for the options dialog. */
	protected JButton m_ButtonOptions;

	/** the seed value. */
	protected int m_Seed;

	/** the percentage split. */
	protected double m_SplitPercentage;

	/** the threshold option. */
	protected String m_TOP;

	/** the verbosity option. */
	protected String m_VOP;

	/** the randomize option. */
	protected boolean m_Randomize;

	/** the number of folds. */
	protected int m_Folds;

	/** the test Instances. */
	protected Instances m_TestInstances;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();

		m_GenericObjectEditor = new GenericObjectEditor(true);
		m_GenericObjectEditor.setClassType(MultiLabelClassifier.class);
		m_GenericObjectEditor.setValue(new meka.classifiers.multilabel.BR());

		m_Seed            = 1;
		m_SplitPercentage = 66.0;
		m_Folds           = 10;
		m_Randomize       = true;
		m_TOP             = "PCut1";
		m_VOP             = "3";
		m_TestInstances   = null;
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		JPanel panel;
		JPanel panelSplit;
		JPanel panelLeft;
		JPanel panelEval;

		super.initGUI();

		panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Classifier"));
		panel.add(m_GenericObjectEditor.getCustomPanel(), BorderLayout.CENTER);
		add(panel, BorderLayout.NORTH);

		panelSplit = new JPanel(new BorderLayout());
		add(panelSplit, BorderLayout.CENTER);

		panelLeft = new JPanel(new BorderLayout());
		panelSplit.add(panelLeft, BorderLayout.WEST);

		panelEval = new JPanel(new BorderLayout());
		panelEval.setBorder(BorderFactory.createTitledBorder("Evaluation"));
		panelLeft.add(panelEval, BorderLayout.NORTH);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelEval.add(panel, BorderLayout.CENTER);

		m_ComboBoxExperiment = new JComboBox(
				new String[]{
						TYPE_TRAINTESTSPLIT,
						TYPE_CROSSVALIDATION,
						TYPE_INCREMENTAL
				});
		m_ComboBoxExperiment.setSelectedIndex(0);
		panel.add(m_ComboBoxExperiment);
		m_ButtonOptions = new JButton("...");
		m_ButtonOptions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showOptions();
			}
		});
		panel.add(m_ButtonOptions);

		panel = new JPanel(new GridLayout(1, 2));
		panelEval.add(panel, BorderLayout.SOUTH);

		m_ButtonStart = new JButton("Start");
		m_ButtonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startClassification();
			}
		});
		panel.add(m_ButtonStart, BorderLayout.SOUTH);

		m_ButtonStop = new JButton("Stop");
		m_ButtonStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopClassification();
			}
		});
		panel.add(m_ButtonStop);

		m_ResultHistoryList = new ResultHistoryList();
		m_ResultHistoryList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				displayResults();
			}
		});
		m_ResultHistoryList.setPopupMenuCustomizer(new HistoryCustomizer());
		panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(m_ResultHistoryList), BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createTitledBorder("History"));
		panelLeft.add(panel);

		m_TextAreaResults = new JTextArea();
		m_TextAreaResults.setFont(GUIHelper.getMonospacedFont());
		panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(m_TextAreaResults), BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createTitledBorder("Result"));
		panelSplit.add(panel, BorderLayout.CENTER);
	}

	/**
	 * Starts the classification.
	 */
	protected void startClassification() {
		String		type;
		Runnable		run;
		final Instances	data;

		if (m_ComboBoxExperiment.getSelectedIndex() == -1)
			return;

		data = new Instances(getData());
		if (m_Randomize)
			data.randomize(new Random(m_Seed));
		type = m_ComboBoxExperiment.getSelectedItem().toString();
		run  = null;

		if (type.equals(TYPE_CROSSVALIDATION)) {
			run = new Runnable() {
				@Override
				public void run() {
					MultiLabelClassifier classifier;
					Result result;
					startBusy("Cross-validating...");
					try {
						classifier = (MultiLabelClassifier) m_GenericObjectEditor.getValue();
						//System.out.println("data.classIndex() "+data.classIndex());
						result = Evaluation.cvModel(classifier, data, m_Folds, m_TOP, m_VOP);
						addResultToHistory(
								result,
								classifier,
								classifier.getClass().getName().replace("meka.classifiers.", "")
						);
						finishBusy("");
					}
					catch (Exception e) {
						System.err.println("Evaluation failed:");
						e.printStackTrace();
						finishBusy("Evaluation failed: " + e);
						JOptionPane.showMessageDialog(
								ClassifyTab.this,
								"Evaluation failed (CV):\n" + e,
								"Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};
		}
		else if (type.equals(TYPE_TRAINTESTSPLIT)) {
			run = new Runnable() {
				@Override
				public void run() {
					MultiLabelClassifier classifier;
					Result result;
					int trainSize;
					Instances train;
					Instances test;
					startBusy("Train/test split...");
					try {
						if (m_TestInstances == null) {
							trainSize  = (int) (data.numInstances() * m_SplitPercentage / 100.0);
							train      = new Instances(data, 0, trainSize);
							test       = new Instances(data, trainSize, data.numInstances() - trainSize);
						}
						else {
							train      = new Instances(data);
							MLUtils.prepareData(m_TestInstances);
							test       = new Instances(m_TestInstances);
							test.setClassIndex(data.classIndex());
						}
						classifier = (MultiLabelClassifier) m_GenericObjectEditor.getValue();
						//System.out.println("data.classIndex() "+train.classIndex());
						result     = Evaluation.evaluateModel(classifier, train, test, m_TOP, m_VOP);
						addResultToHistory(
								result,
								classifier,
								classifier.getClass().getName().replace("meka.classifiers.", "")
						);
						finishBusy("");
					}
					catch (Exception e) {
						System.err.println("Evaluation failed (train/test split):");
						e.printStackTrace();
						finishBusy("Evaluation failed: " + e);
						JOptionPane.showMessageDialog(
								ClassifyTab.this,
								"Evaluation failed:\n" + e,
								"Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};
		}
		else if (type.equals(TYPE_INCREMENTAL)) {
			run = new Runnable() {
				@Override
				public void run() {
					MultiLabelClassifier classifier;
					Result result;
					startBusy("Incremental...");
					try {
						classifier = (MultiLabelClassifier) m_GenericObjectEditor.getValue();
						//System.out.println("data.classIndex() "+data.classIndex());
						result    = IncrementalEvaluation.evaluateModelPrequentialBasic(classifier, data, 20, 1., m_TOP, m_VOP);
						addResultToHistory(
								result,
								classifier,
								classifier.getClass().getName().replace("meka.classifiers.", "")
						);
						finishBusy("");
					}
					catch (Exception e) {
						System.err.println("Evaluation failed (incremental splits):");
						e.printStackTrace();
						finishBusy("Evaluation failed: " + e);
						JOptionPane.showMessageDialog(
								ClassifyTab.this,
								"Evaluation failed:\n" + e,
								"Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};
		}

		start(run);
	}

	/**
	 * Stops the classification, if running.
	 */
	protected void stopClassification() {
		if (isRunning()) {
			stop();
			startBusy("Evaluation interrupted!");
		}
	}

	/**
	 * Brings up the dialog with the classification options.
	 */
	protected void showOptions() {
		final JDialog		dialog;
		final ClassifyTabOptions 	panel;
		final JButton		buttonOK;
		final JButton		buttonCancel;
		JPanel			panelButtons;

		if (GUIHelper.getParentDialog(this) != null)
			dialog = new JDialog(GUIHelper.getParentDialog(this), ModalityType.DOCUMENT_MODAL);
		else
			dialog = new JDialog(GUIHelper.getParentFrame(this), true);
		dialog.setTitle("Options");
		dialog.getContentPane().setLayout(new BorderLayout());
		panel = new ClassifyTabOptions();
		panel.setSeed(m_Seed);
		panel.setFolds(m_Folds);
		panel.setSplitPercentage(m_SplitPercentage);
		panel.setTOP(m_TOP);
		panel.setVOP(m_VOP);
		panel.setRandomize(m_Randomize);
		panel.setTestFile(null);
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		dialog.getContentPane().add(panelButtons, BorderLayout.SOUTH);
		buttonOK = new JButton("OK");
		buttonOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_Seed            = panel.getSeed();
				m_SplitPercentage = panel.getSplitPercentage();
				m_Folds           = panel.getFolds();
				m_TOP             = panel.getTOP();
				m_VOP             = panel.getVOP();
				m_Randomize 	  = panel.getRandomize();
				m_TestInstances   = panel.getTestFile();
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		panelButtons.add(buttonOK);
		buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		panelButtons.add(buttonCancel);
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	/**
	 * Adds the result to the history.
	 *
	 * @param result the result to add
	 * @param payload the payload to add
	 * @param suffix the suffix to add
	 */
	protected void addResultToHistory(final Result result, final Object payload, final String suffix) {
		Runnable run;

		run = new Runnable() {
			@Override
			public void run() {
				m_ResultHistoryList.addResult(result, payload, suffix);
			}
		};
		SwingUtilities.invokeLater(run);
	}

	/**
	 * Displays the selected results.
	 */
	protected void displayResults() {
		Result result;

		if (m_ResultHistoryList.getSelectedIndex() == -1) {
			m_TextAreaResults.setText("");
			return;
		}

		result = m_ResultHistoryList.getResultAt(m_ResultHistoryList.getSelectedIndex());
		if (result == null)
			return;

		m_TextAreaResults.setText(result.toString());
	}

	/**
	 * Returns the title of the tab.
	 *
	 * @return the title
	 */
	@Override
	public String getTitle() {
		return "Classify";
	}

	/**
	 * Gets called when the data changed.
	 */
	@Override
	protected void update() {
		m_ButtonStart.setEnabled(hasData());
		m_ButtonStop.setEnabled(isRunning());
	}

	/**
	 * Gets called when the thread starts.
	 */
	@Override
	protected void executionStarted() {
		m_ButtonStart.setEnabled(false);
		m_ButtonStop.setEnabled(true);
	}

	/**
	 * Gets called when the thread finishes or gets stopped.
	 *
	 * @param t if the execution generated an exception, null if no errors
	 */
	@Override
	protected void executionFinished(Throwable t) {
		m_ButtonStart.setEnabled(true);
		m_ButtonStop.setEnabled(false);
		if (t != null) {
			System.err.println("Execution failed:");
			t.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Execution failed:\n" + t,
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
