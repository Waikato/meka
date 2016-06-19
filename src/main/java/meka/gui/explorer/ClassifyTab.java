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

import com.googlecode.jfilechooserbookmarks.core.Utils;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.IncrementalMultiLabelClassifier;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.classifiers.multilabel.incremental.IncrementalEvaluation;
import meka.core.MLUtils;
import meka.core.OptionUtils;
import meka.core.Result;
import meka.gui.core.GUIHelper;
import meka.gui.core.ResultHistoryList;
import meka.gui.explorer.classify.AbstractClassifyResultHistoryPlugin;
import meka.gui.explorer.classify.AbstractClassifyTabMenuItem;
import meka.gui.goe.GenericObjectEditor;
import weka.core.Instances;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

	/** supplied test set. */
	public final static String TYPE_SUPPLIEDTESTSET = "Supplied test set";

	/** incremental batch train/test split. */
	public final static String TYPE_BINCREMENTAL = "Batch-incremental";

	/** incremental pequential. */
	public final static String TYPE_PREQUENTIAL = "Prequential (incremental)";

	/** the panel for the GOE. */
	protected JPanel m_PanelGOE;

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

	/** the number of samples for prequential evaluation. */
	protected int m_Samples;

	/** the panel with the options. */
	protected ClassifyTabOptions m_ClassifyTabOptions;

	/** the test Instances. */
	protected Instances m_TestInstances;

	/** the last non-incremental classifier in use. */
	protected MultiLabelClassifier m_LastNonIncrementalClassifier;

	/** the last incremental classifier in use. */
	protected IncrementalMultiLabelClassifier m_LastIncrementalClassifier;

	/** the custom menu. */
	protected JMenu m_Menu;

	/** the additional menu items. */
	protected HashMap<AbstractClassifyTabMenuItem, JMenuItem> m_AdditionalMenuItems;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();

		m_LastNonIncrementalClassifier = new meka.classifiers.multilabel.BR();
		m_LastIncrementalClassifier    = new meka.classifiers.multilabel.incremental.BRUpdateable();

		m_GenericObjectEditor = new GenericObjectEditor(true);
		m_GenericObjectEditor.setClassType(MultiLabelClassifier.class);
		m_GenericObjectEditor.setValue(m_LastNonIncrementalClassifier);

		m_Seed                = 1;
		m_SplitPercentage     = 66.0;
		m_Folds               = 10;
		m_Samples             = 10;
		m_Randomize           = true;
		m_TOP                 = "PCut1";
		m_VOP                 = "3";
		m_TestInstances       = null;
		m_ClassifyTabOptions  = null;
		m_AdditionalMenuItems = new HashMap<>();
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

		m_PanelGOE = new JPanel(new BorderLayout());
		m_PanelGOE.setBorder(BorderFactory.createTitledBorder("Classifier"));
		add(m_PanelGOE, BorderLayout.NORTH);

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
						TYPE_SUPPLIEDTESTSET,
						TYPE_CROSSVALIDATION,
						TYPE_PREQUENTIAL,
						TYPE_BINCREMENTAL
				});
		m_ComboBoxExperiment.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateGOE();
			}
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
	 * Updates the GOE for the classifier.
	 */
	protected void updateGOE() {
		String  type;

		type = m_ComboBoxExperiment.getSelectedItem().toString();
		switch (type) {
			case TYPE_CROSSVALIDATION:
			case TYPE_SUPPLIEDTESTSET:
			case TYPE_TRAINTESTSPLIT:
				if (m_GenericObjectEditor.getClassType() == IncrementalMultiLabelClassifier.class) {
					m_LastIncrementalClassifier = (IncrementalMultiLabelClassifier) m_GenericObjectEditor.getValue();
					m_GenericObjectEditor = new GenericObjectEditor(true);
					m_GenericObjectEditor.setClassType(MultiLabelClassifier.class);
					m_GenericObjectEditor.setValue(m_LastNonIncrementalClassifier);
				}
				break;

			case TYPE_BINCREMENTAL:
			case TYPE_PREQUENTIAL:
				if (m_GenericObjectEditor.getClassType() != IncrementalMultiLabelClassifier.class) {
					m_LastNonIncrementalClassifier = (MultiLabelClassifier) m_GenericObjectEditor.getValue();
					m_GenericObjectEditor = new GenericObjectEditor(true);
					m_GenericObjectEditor.setClassType(IncrementalMultiLabelClassifier.class);
					m_GenericObjectEditor.setValue(m_LastIncrementalClassifier);
				}
				break;

			default:
				throw new IllegalStateException("Unhandled evaluation type: " + type);
		}

		m_PanelGOE.removeAll();
		m_PanelGOE.add(m_GenericObjectEditor.getCustomPanel(), BorderLayout.CENTER);

		invalidate();
		revalidate();
		repaint();
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

		switch (type) {
			case TYPE_CROSSVALIDATION:
				run = new Runnable() {
					@Override
					public void run() {
						MultiLabelClassifier classifier;
						Result result;
						startBusy("Cross-validating...");
						try {
							classifier = (MultiLabelClassifier) m_GenericObjectEditor.getValue();
							log(OptionUtils.toCommandLine(classifier));
							log("Dataset: " + data.relationName());
							log("Class-index: " + data.classIndex());
							result = Evaluation.cvModel(classifier, data, m_Folds, m_TOP, m_VOP);
							addResultToHistory(
									result,
									new Object[]{classifier, new Instances(data, 0)},
									classifier.getClass().getName().replace("meka.classifiers.", "")
							);
							finishBusy();
						}
						catch (Exception e) {
							handleException("Evaluation failed:", e);
							finishBusy("Evaluation failed: " + e);
							JOptionPane.showMessageDialog(
									ClassifyTab.this,
									"Evaluation failed (CV):\n" + e,
									"Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				break;

			case TYPE_TRAINTESTSPLIT:
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
							trainSize  = (int) (data.numInstances() * m_SplitPercentage / 100.0);
							train      = new Instances(data, 0, trainSize);
							test       = new Instances(data, trainSize, data.numInstances() - trainSize);
							classifier = (MultiLabelClassifier) m_GenericObjectEditor.getValue();
							log(OptionUtils.toCommandLine(classifier));
							log("Dataset: " + train.relationName());
							log("Class-index: " + train.classIndex());
							result     = Evaluation.evaluateModel(classifier, train, test, m_TOP, m_VOP);
							addResultToHistory(
									result,
									new Object[]{classifier, new Instances(train, 0)},
									classifier.getClass().getName().replace("meka.classifiers.", "")
							);
							finishBusy();
						}
						catch (Exception e) {
							handleException("Evaluation failed (train/test split):", e);
							finishBusy("Evaluation failed: " + e);
							JOptionPane.showMessageDialog(
									ClassifyTab.this,
									"Evaluation failed:\n" + e,
									"Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				break;

			case TYPE_SUPPLIEDTESTSET:
				run = new Runnable() {
					@Override
					public void run() {
						MultiLabelClassifier classifier;
						Result result;
						int trainSize;
						Instances train;
						Instances test;
						startBusy("Supplied test...");
						try {
							train      = new Instances(data);
							MLUtils.prepareData(m_TestInstances);
							test       = new Instances(m_TestInstances);
							test.setClassIndex(data.classIndex());
							String msg = train.equalHeadersMsg(test);
							if (msg != null)
								throw new IllegalArgumentException("Train and test set are not compatible:\n" + msg);
							classifier = (MultiLabelClassifier) m_GenericObjectEditor.getValue();
							log(OptionUtils.toCommandLine(classifier));
							log("Dataset: " + train.relationName());
							log("Class-index: " + train.classIndex());
							result     = Evaluation.evaluateModel(classifier, train, test, m_TOP, m_VOP);
							addResultToHistory(
									result,
									new Object[]{classifier, new Instances(train, 0)},
									classifier.getClass().getName().replace("meka.classifiers.", "")
							);
							finishBusy();
						}
						catch (Exception e) {
							handleException("Evaluation failed (train/test split):", e);
							finishBusy("Evaluation failed: " + e);
							JOptionPane.showMessageDialog(
									ClassifyTab.this,
									"Evaluation failed:\n" + e,
									"Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				break;

			case TYPE_BINCREMENTAL:
				run = new Runnable() {
					@Override
					public void run() {
						MultiLabelClassifier classifier;
						Result result;
						startBusy("Incremental...");
						try {
							classifier = (MultiLabelClassifier) m_GenericObjectEditor.getValue();
							log(OptionUtils.toCommandLine(classifier));
							log("Dataset: " + data.relationName());
							log("Class-index: " + data.classIndex());
							result = IncrementalEvaluation.evaluateModelBatchWindow(classifier, data, m_Samples, 1., m_TOP, m_VOP);
							addResultToHistory(
									result,
									new Object[]{classifier, new Instances(data, 0)},
									classifier.getClass().getName().replace("meka.classifiers.", "")
							);
							finishBusy();
						}
						catch (Exception e) {
							handleException("Evaluation failed (incremental splits):", e);
							finishBusy("Evaluation failed: " + e);
							JOptionPane.showMessageDialog(
									ClassifyTab.this,
									"Evaluation failed:\n" + e,
									"Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				break;

			case TYPE_PREQUENTIAL:
				run = new Runnable() {
					@Override
					public void run() {
						MultiLabelClassifier classifier;
						Result result;
						startBusy("Incremental...");
						try {
							classifier = (MultiLabelClassifier) m_GenericObjectEditor.getValue();
							log(OptionUtils.toCommandLine(classifier));
							log("Dataset: " + data.relationName());
							log("Class-index: " + data.classIndex());
							result    = IncrementalEvaluation.evaluateModelPrequentialBasic(classifier, data, (data.numInstances()/(m_Samples+1)), 1., m_TOP, m_VOP);
							addResultToHistory(
									result,
									new Object[]{classifier, new Instances(data, 0)},
									classifier.getClass().getName().replace("meka.classifiers.", "")
							);
							finishBusy();
						}
						catch (Exception e) {
							handleException("Evaluation failed (incremental splits):", e);
							finishBusy("Evaluation failed: " + e);
							JOptionPane.showMessageDialog(
									ClassifyTab.this,
									"Evaluation failed:\n" + e,
									"Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				break;

			default:
				throw new IllegalStateException("Unhandled evaluation type: " + type);
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
		final JDialog   dialog;
		final JButton	buttonOK;
		final JButton	buttonCancel;
		JPanel			panelButtons;

		if (GUIHelper.getParentDialog(this) != null)
			dialog = new JDialog(GUIHelper.getParentDialog(this), ModalityType.DOCUMENT_MODAL);
		else
			dialog = new JDialog(GUIHelper.getParentFrame(this), true);
		dialog.setTitle("Options");
		dialog.getContentPane().setLayout(new BorderLayout());
		if (m_ClassifyTabOptions == null) {
			m_ClassifyTabOptions = new ClassifyTabOptions();
			m_ClassifyTabOptions.setSeed(m_Seed);
			m_ClassifyTabOptions.setFolds(m_Folds);
			m_ClassifyTabOptions.setSamples(m_Samples);
			m_ClassifyTabOptions.setSplitPercentage(m_SplitPercentage);
			m_ClassifyTabOptions.setTOP(m_TOP);
			m_ClassifyTabOptions.setVOP(m_VOP);
			m_ClassifyTabOptions.setRandomize(m_Randomize);
			m_ClassifyTabOptions.setTestFile(null);
		}
		dialog.getContentPane().add(m_ClassifyTabOptions, BorderLayout.CENTER);
		panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		dialog.getContentPane().add(panelButtons, BorderLayout.SOUTH);
		buttonOK = new JButton("OK");
		buttonOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_Seed            = m_ClassifyTabOptions.getSeed();
				m_SplitPercentage = m_ClassifyTabOptions.getSplitPercentage();
				m_Folds           = m_ClassifyTabOptions.getFolds();
				m_Samples         = m_ClassifyTabOptions.getSamples();
				m_TOP             = m_ClassifyTabOptions.getTOP();
				m_VOP             = m_ClassifyTabOptions.getVOP();
				m_Randomize 	  = m_ClassifyTabOptions.getRandomize();
				m_TestInstances   = m_ClassifyTabOptions.getTestFile();
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
	public void addResultToHistory(final Result result, final Object payload, final String suffix) {
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
		m_ButtonStart.setEnabled(hasData());
		m_ButtonStop.setEnabled(false);
		if (t != null) {
			handleException("Execution failed:", t);
			JOptionPane.showMessageDialog(
					this,
					"Execution failed:\n" + t,
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the current classifier.
	 *
	 * @return the classifier
	 */
	public MultiLabelClassifier getClassifier() {
		return (MultiLabelClassifier) m_GenericObjectEditor.getValue();
	}

	/**
	 * Sets the test instances.
	 *
	 * @param value the test instances, null if to remove
	 */
	public void setTestData(Instances value) {
		m_TestInstances = value;
	}

	/**
	 * Returns the test instances, if any.
	 *
	 * @return the test instances, null if none loaded
	 */
	public Instances getTestData() {
		return m_TestInstances;
	}

	/**
	 * Returns the threshold option.
	 *
	 * @return the option
	 */
	public String getTOP() {
		return m_TOP;
	}

	/**
	 * Returns the verbosity option.
	 *
	 * @return the option
	 */
	public String getVOP() {
		return m_VOP;
	}

	/**
	 * Returns an optional menu to be added to the Explorer menu.
	 *
	 * @return the menu
	 */
	public JMenu getMenu() {
		JMenu 								menu;
		JMenuItem							menuitem;
		List<String> 						menuitemclasses;
		AbstractClassifyTabMenuItem			menuitemplugin;
		List<AbstractClassifyTabMenuItem>	list;
		String								group;

		if (m_Menu == null) {
			menu = new JMenu("Classify");
			menu.setMnemonic('C');
			menu.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					updateMenu();
				}
			});

			menuitemclasses = GenericObjectEditor.getClassnames(AbstractClassifyTabMenuItem.class.getName());
			for (String menuitemclass: menuitemclasses) {
				try {
					menuitemplugin = (AbstractClassifyTabMenuItem) Class.forName(menuitemclass).newInstance();
					menuitem = new JMenuItem(menuitemplugin.getName());
					if (menuitemplugin.getIcon() == null)
						menuitem.setIcon(GUIHelper.getEmptyIcon());
					else
						menuitem.setIcon(GUIHelper.getIcon(menuitemplugin.getIcon()));
					menuitem.addActionListener(menuitemplugin.getActionListener(this));
					m_AdditionalMenuItems.put(menuitemplugin, menuitem);
				}
				catch (Exception e) {
					log("Failed to process menu item class: " + menuitemclass + "\n" + Utils.throwableToString(e));
				}
			}
			list = new ArrayList<>(m_AdditionalMenuItems.keySet());
			Collections.sort(list);
			group = "";
			for (AbstractClassifyTabMenuItem item: list) {
				if (!group.equals(item.getGroup())) {
					if (!group.isEmpty())
						menu.addSeparator();
					group = item.getGroup();
				}
				menu.add(m_AdditionalMenuItems.get(item));
			}

			m_Menu = menu;
		}

		return m_Menu;
	}

	/**
	 * Updates the enabled/disabled state of the menu items.
	 */
	protected void updateMenu() {
		JMenuItem		menuitem;

		if (m_Menu == null)
			return;

		for (AbstractClassifyTabMenuItem menuitemplugin: m_AdditionalMenuItems.keySet()) {
			menuitem = m_AdditionalMenuItems.get(menuitemplugin);
			menuitemplugin.update(this, menuitem);
		}
	}
}
