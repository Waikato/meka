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
 * BasicSetup.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.experimenter;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.ObjectUtils;
import meka.core.OptionUtils;
import meka.experiment.DefaultExperiment;
import meka.experiment.Experiment;
import meka.experiment.datasetproviders.LocalDatasetProvider;
import meka.experiment.evaluationstatistics.EvaluationStatisticsHandler;
import meka.experiment.evaluationstatistics.KeyValuePairs;
import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.Evaluator;
import meka.experiment.evaluators.PercentageSplit;
import meka.experiment.evaluators.RepeatedRuns;
import meka.gui.core.GUIHelper;
import meka.gui.core.ListWithButtons;
import meka.gui.core.ParameterPanel;
import meka.gui.goe.GenericObjectEditor;
import meka.gui.goe.GenericObjectEditorDialog;
import weka.gui.ConverterFileChooser;
import weka.gui.JListHelper;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Tab for setting up a basic experiment.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class BasicSetup
		extends AbstractSetupTab {

	private static final long serialVersionUID = 3556506064253273853L;

	/** the classifiers. */
	protected ListWithButtons m_ListClassifiers;

	/** the model with the classifiers. */
	protected DefaultListModel<String> m_ModelClassifiers;

	/** the button for adding a classifier. */
	protected JButton m_ButtonAddClassifier;

	/** the button for removing a classifier. */
	protected JButton m_ButtonRemoveClassifier;

	/** the button for removing all classifiers. */
	protected JButton m_ButtonRemoveAllClassifiers;

	/** the button for editing a classifier. */
	protected JButton m_ButtonEditClassifier;

	/** the button for moving a classifier up. */
	protected JButton m_ButtonMoveUpClassifier;

	/** the button for moving a classifier down. */
	protected JButton m_ButtonMoveDownClassifier;

	/** the datasets. */
	protected ListWithButtons m_ListDatasets;

	/** the model with the datasets. */
	protected DefaultListModel<String> m_ModelDatasets;

	/** the button for adding a dataset. */
	protected JButton m_ButtonAddDataset;

	/** the button for removing a dataset. */
	protected JButton m_ButtonRemoveDataset;

	/** the button for removing all datasets. */
	protected JButton m_ButtonRemoveAllDatasets;

	/** the button for moving a dataset up. */
	protected JButton m_ButtonMoveUpDataset;

	/** the button for moving a dataset down. */
	protected JButton m_ButtonMoveDownDataset;

	/** the file chooser for datasets. */
	protected ConverterFileChooser m_FileChooserDatasets;

	/** for the parameters. */
	protected ParameterPanel m_ParameterPanel;

	/** the number of runs. */
	protected JSpinner m_SpinnerNumRuns;

	/** the type of evaluation. */
	protected JComboBox<String> m_ComboBoxEvaluation;

	/** the number of folds. */
	protected JSpinner m_SpinnerNumFolds;

	/** the percentage for the percentage split. */
	protected JTextField m_TextPercentage;

	/** whether to preserve the order. */
	protected JCheckBox m_CheckBoxPreserveOrder;

	/** the GOE for the statistics handler. */
	protected GenericObjectEditor m_GOEStatisticsHandler;

	/**
	 * Initializes the member variables.
	 */
	@Override
	protected void initialize() {
		super.initialize();

		m_FileChooserDatasets = GUIHelper.newConverterFileChooser();
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		JPanel      panel;
		JPanel      panelClassifiers;
		JPanel      panelDatasets;

		super.initGUI();

		panel = new JPanel(new GridLayout(1, 2));
		m_PanelSetup.add(panel, BorderLayout.CENTER);

		// classifiers
		panelClassifiers = new JPanel(new BorderLayout());
		panelClassifiers.setBorder(BorderFactory.createTitledBorder("Classifiers"));
		panel.add(panelClassifiers);

		m_ModelClassifiers = new DefaultListModel<>();
		m_ListClassifiers = new ListWithButtons();
		m_ListClassifiers.getList().setModel(m_ModelClassifiers);
		m_ListClassifiers.getList().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateButtons();
			}
		});
		panelClassifiers.add(m_ListClassifiers, BorderLayout.CENTER);

		m_ButtonAddClassifier = new JButton("Add...");
		m_ButtonAddClassifier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addClassifier();
			}
		});
		m_ListClassifiers.addToButtonsPanel(m_ButtonAddClassifier);

		m_ButtonEditClassifier = new JButton("Edit...");
		m_ButtonEditClassifier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editClassifier();
			}
		});
		m_ListClassifiers.addToButtonsPanel(m_ButtonEditClassifier);

		m_ListClassifiers.addToButtonsPanel(new JLabel());

		m_ButtonRemoveClassifier = new JButton("Remove");
		m_ButtonRemoveClassifier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeClassifiers(m_ListClassifiers.getList().getSelectedIndices());
			}
		});
		m_ListClassifiers.addToButtonsPanel(m_ButtonRemoveClassifier);

		m_ButtonRemoveAllClassifiers = new JButton("Remove all");
		m_ButtonRemoveAllClassifiers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeClassifiers(null);
			}
		});
		m_ListClassifiers.addToButtonsPanel(m_ButtonRemoveAllClassifiers);

		m_ListClassifiers.addToButtonsPanel(new JLabel());

		m_ButtonMoveUpClassifier = new JButton("Up");
		m_ButtonMoveUpClassifier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JListHelper.moveUp(m_ListClassifiers.getList());
				setModified(true);
			}
		});
		m_ListClassifiers.addToButtonsPanel(m_ButtonMoveUpClassifier);

		m_ButtonMoveDownClassifier = new JButton("Down");
		m_ButtonMoveDownClassifier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JListHelper.moveDown(m_ListClassifiers.getList());
				setModified(true);
			}
		});
		m_ListClassifiers.addToButtonsPanel(m_ButtonMoveDownClassifier);

		// datasets
		panelDatasets = new JPanel(new BorderLayout());
		panelDatasets.setBorder(BorderFactory.createTitledBorder("Datasets"));
		panel.add(panelDatasets);

		m_ModelDatasets = new DefaultListModel<>();
		m_ListDatasets = new ListWithButtons();
		m_ListDatasets.getList().setModel(m_ModelDatasets);
		m_ListDatasets.getList().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateButtons();
			}
		});
		panelDatasets.add(m_ListDatasets, BorderLayout.CENTER);

		m_ButtonAddDataset = new JButton("Add...");
		m_ButtonAddDataset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addDataset();
			}
		});
		m_ListDatasets.addToButtonsPanel(m_ButtonAddDataset);

		m_ListDatasets.addToButtonsPanel(new JLabel());

		m_ButtonRemoveDataset = new JButton("Remove");
		m_ButtonRemoveDataset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeDatasets(m_ListDatasets.getList().getSelectedIndices());
			}
		});
		m_ListDatasets.addToButtonsPanel(m_ButtonRemoveDataset);

		m_ButtonRemoveAllDatasets = new JButton("Remove all");
		m_ButtonRemoveAllDatasets.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeDatasets(null);
			}
		});
		m_ListDatasets.addToButtonsPanel(m_ButtonRemoveAllDatasets);

		m_ListDatasets.addToButtonsPanel(new JLabel());

		m_ButtonMoveUpDataset = new JButton("Up");
		m_ButtonMoveUpDataset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JListHelper.moveUp(m_ListDatasets.getList());
				setModified(true);
			}
		});
		m_ListDatasets.addToButtonsPanel(m_ButtonMoveUpDataset);

		m_ButtonMoveDownDataset = new JButton("Down");
		m_ButtonMoveDownDataset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JListHelper.moveDown(m_ListDatasets.getList());
				setModified(true);
			}
		});
		m_ListDatasets.addToButtonsPanel(m_ButtonMoveDownDataset);

		// parameters
		m_ParameterPanel = new ParameterPanel();
		m_PanelSetup.add(m_ParameterPanel, BorderLayout.SOUTH);

		m_SpinnerNumRuns = new JSpinner();
		((SpinnerNumberModel) m_SpinnerNumRuns.getModel()).setMinimum(1);
		m_SpinnerNumRuns.setValue(10);
		m_SpinnerNumRuns.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setModified(true);
			}
		});
		m_ParameterPanel.addParameter("Runs", m_SpinnerNumRuns);

		m_ComboBoxEvaluation = new JComboBox<>(new String[]{
				"Cross-validation",
				"Percentage split",
		});
		m_ComboBoxEvaluation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = m_ComboBoxEvaluation.getSelectedIndex();
				if (index == -1)
					return;
				m_SpinnerNumFolds.setEnabled(index == 0);
				m_TextPercentage.setEnabled(index == 1);
				setModified(true);
			}
		});
		m_ParameterPanel.addParameter("Evaluation", m_ComboBoxEvaluation);

		m_SpinnerNumFolds = new JSpinner();
		((SpinnerNumberModel) m_SpinnerNumFolds.getModel()).setMinimum(2);
		m_SpinnerNumFolds.setValue(10);
		m_SpinnerNumFolds.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setModified(true);
			}
		});
		m_ParameterPanel.addParameter("Folds", m_SpinnerNumFolds);

		m_TextPercentage = new JTextField("10", 5);
		m_TextPercentage.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				setModified(true);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				setModified(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				setModified(true);
			}
		});
		m_ParameterPanel.addParameter("Split percentage", m_TextPercentage);

		m_CheckBoxPreserveOrder = new JCheckBox();
		m_CheckBoxPreserveOrder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setModified(true);
			}
		});
		m_ParameterPanel.addParameter("Preserve order", m_CheckBoxPreserveOrder);

		m_GOEStatisticsHandler = new GenericObjectEditor(true);
		m_GOEStatisticsHandler.setClassType(EvaluationStatisticsHandler.class);
		m_GOEStatisticsHandler.setValue(new KeyValuePairs());
		m_GOEStatisticsHandler.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setModified(true);
			}
		});
		m_ParameterPanel.addParameter("Statistics", setPreferredSize(m_GOEStatisticsHandler.getCustomPanel()));
	}

	/**
	 * Returns the title of the tab.
	 *
	 * @return the title
	 */
	@Override
	public String getTitle() {
		return "Setup (basic)";
	}

	/**
	 * Updates the enabled state of the buttons.
	 */
	protected void updateButtons() {
		boolean     present;
		boolean     running;
		boolean     editable;
		boolean     handled;

		super.updateButtons();

		present  = (m_Experiment != null);
		running  = present && m_Experiment.isRunning();
		handled  = !m_PanelUnsupported.isVisible();
		editable = present && !running && handled;

		// classifiers
		m_ButtonAddClassifier.setEnabled(editable);
		m_ButtonRemoveClassifier.setEnabled(editable && (m_ListClassifiers.getList().getSelectedIndices().length >= 1));
		m_ButtonRemoveAllClassifiers.setEnabled(editable && (m_ModelClassifiers.getSize() > 0));
		m_ButtonEditClassifier.setEnabled(editable && (m_ListClassifiers.getList().getSelectedIndices().length == 1));
		m_ButtonMoveUpClassifier.setEnabled(editable && JListHelper.canMoveUp(m_ListClassifiers.getList()));
		m_ButtonMoveDownClassifier.setEnabled(editable && JListHelper.canMoveDown(m_ListClassifiers.getList()));

		// datasets
		m_ButtonAddDataset.setEnabled(editable);
		m_ButtonRemoveDataset.setEnabled(editable && (m_ListDatasets.getList().getSelectedIndices().length >= 1));
		m_ButtonRemoveAllDatasets.setEnabled(editable && (m_ModelDatasets.getSize() > 0));
		m_ButtonMoveUpDataset.setEnabled(editable && JListHelper.canMoveUp(m_ListDatasets.getList()));
		m_ButtonMoveDownDataset.setEnabled(editable && JListHelper.canMoveDown(m_ListDatasets.getList()));
	}

	/**
	 * Lets the user add a classifier.
	 */
	protected void addClassifier() {
		GenericObjectEditorDialog dialog;

		dialog = getGOEDialog(MultiLabelClassifier.class, new BR());
		dialog.setTitle("Add classifier");
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		if (dialog.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
			return;

		if (m_ListClassifiers.getList().getSelectedIndex() > -1)
			m_ModelClassifiers.insertElementAt(OptionUtils.toCommandLine(dialog.getCurrent()), m_ListClassifiers.getList().getSelectedIndex());
		else
			m_ModelClassifiers.addElement(OptionUtils.toCommandLine(dialog.getCurrent()));
		m_Modified = true;
		updateButtons();
	}

	/**
	 * Lets the user edit a classifier.
	 */
	protected void editClassifier() {
		GenericObjectEditorDialog   dialog;
		String                      classifier;

		if (m_ListClassifiers.getList().getSelectedIndex() == -1)
			return;

		dialog     = getGOEDialog(MultiLabelClassifier.class, new BR());
		dialog.setTitle("Edit classifier");
		classifier = m_ModelClassifiers.get(m_ListClassifiers.getList().getSelectedIndex());
		try {
			dialog.setCurrent(OptionUtils.fromCommandLine(MultiLabelClassifier.class, classifier));
		}
		catch (Exception e) {
			handleException("Failed to edit classifier: " + classifier, e);
		}
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		if (dialog.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
			return;

		m_ModelClassifiers.setElementAt(OptionUtils.toCommandLine(dialog.getCurrent()), m_ListClassifiers.getList().getSelectedIndex());
		m_Modified = true;
		updateButtons();
	}

	/**
	 * Removes the specified classifiers.
	 *
	 * @param indices       the indices, null to remove all
	 */
	protected void removeClassifiers(int[] indices) {
		int     i;

		if (indices == null) {
			m_ModelClassifiers.removeAllElements();
		}
		else {
			for (i = indices.length - 1; i >= 0; i--)
				m_ModelClassifiers.remove(indices[i]);
		}

		setModified(true);
	}

	/**
	 * Lets the user add a dataset.
	 */
	protected void addDataset() {
		int     retVal;

		retVal = m_FileChooserDatasets.showOpenDialog(this);
		if (retVal != ConverterFileChooser.APPROVE_OPTION)
			return;

		m_ModelDatasets.addElement(m_FileChooserDatasets.getSelectedFile().getAbsolutePath());
		m_Modified = true;
		updateButtons();
	}

	/**
	 * Removes the specified datasets.
	 *
	 * @param indices       the indices, null to remove all
	 */
	protected void removeDatasets(int[] indices) {
		int     i;

		if (indices == null) {
			m_ModelDatasets.removeAllElements();
		}
		else {
			for (i = indices.length - 1; i >= 0; i--)
				m_ModelDatasets.remove(indices[i]);
		}

		setModified(true);
	}

	/**
	 * Resets the interface.
	 */
	protected void clear() {
		m_ModelClassifiers.removeAllElements();
		m_ModelDatasets.removeAllElements();
		m_SpinnerNumRuns.setValue(10);
		m_ComboBoxEvaluation.setSelectedIndex(0);
		m_CheckBoxPreserveOrder.setSelected(false);
		m_TextPercentage.setText("66.6");
		m_GOEStatisticsHandler.setValue(new KeyValuePairs());
	}

	/**
	 * Checks whether this type of experiment is handled by this tab.
	 *
	 * @param exp       the experiment to check
	 * @return          true if handled
	 */
	protected boolean handlesExperiment(Experiment exp) {
		boolean     result;
		Evaluator   evaluator;

		result = (exp instanceof DefaultExperiment);
		result = result && (exp.getDatasetProvider() instanceof LocalDatasetProvider);
		result = result &&
				((exp.getEvaluator() instanceof RepeatedRuns)
						|| (exp.getEvaluator() instanceof CrossValidation)
						|| (exp.getEvaluator() instanceof PercentageSplit));
		if (result && (exp.getEvaluator() instanceof RepeatedRuns)) {
			evaluator = ((RepeatedRuns) exp.getEvaluator()).getEvaluator();
			result = result &&
					((evaluator instanceof CrossValidation)
							|| evaluator instanceof PercentageSplit);
		}

		return result;
	}

	/**
	 * Maps the experiment onto the parameters.
	 */
	@Override
	protected void fromExperiment() {
		LocalDatasetProvider    provider;
		Evaluator               evaluator;
		RepeatedRuns            runs;
		CrossValidation         cv;
		PercentageSplit         split;

		// classifiers
		m_ModelClassifiers.removeAllElements();
		for (MultiLabelClassifier classifier: m_Experiment.getClassifiers())
			m_ModelClassifiers.addElement(OptionUtils.toCommandLine(classifier));

		// datasets
		provider = (LocalDatasetProvider) m_Experiment.getDatasetProvider();
		m_ModelDatasets.removeAllElements();
		for (File dataset: provider.getDatasets())
			m_ModelDatasets.addElement(dataset.getAbsolutePath());

		// evaluator
		evaluator = m_Experiment.getEvaluator();
		if (evaluator instanceof RepeatedRuns) {
			runs = (RepeatedRuns) evaluator;
			m_SpinnerNumRuns.setValue(runs.getUpperRuns() - runs.getLowerRuns() + 1);
			evaluator = ((RepeatedRuns) evaluator).getEvaluator();
		}
		if (evaluator instanceof CrossValidation) {
			cv = (CrossValidation) evaluator;
			m_SpinnerNumFolds.setValue(cv.getNumFolds());
			m_CheckBoxPreserveOrder.setSelected(cv.getPreserveOrder());
		}
		else {
			split = (PercentageSplit) evaluator;
			m_TextPercentage.setText("" + split.getTrainPercentage());
			m_CheckBoxPreserveOrder.setSelected(split.getPreserveOrder());
		}

		// statistics
		m_GOEStatisticsHandler.setValue(m_Experiment.getStatisticsHandler());
	}

	/**
	 * Stores the parameters in an experiment.
	 *
	 * @return          the generated experiment
	 */
	@Override
	protected Experiment toExperiment() {
		Experiment              result;
		MultiLabelClassifier[]  classifiers;
		File[]                  datasets;
		LocalDatasetProvider    provider;
		int                     i;
		RepeatedRuns            runs;
		CrossValidation         cv;
		PercentageSplit         split;

		result = (Experiment) ObjectUtils.deepCopy(m_Experiment);

		// classifiers
		classifiers = new MultiLabelClassifier[m_ModelClassifiers.getSize()];
		for (i = 0; i < m_ModelClassifiers.getSize(); i++) {
			try {
				classifiers[i] = OptionUtils.fromCommandLine(MultiLabelClassifier.class, m_ModelClassifiers.get(i));
			}
			catch (Exception e) {
				handleException("Failed to instantiate classifier: " + m_ModelClassifiers.get(i), e);
				classifiers[i] = new BR();
			}
		}
		result.setClassifiers(classifiers);

		// dataset provider
		datasets = new File[m_ModelDatasets.getSize()];
		for (i = 0; i < m_ModelDatasets.getSize(); i++)
			datasets[i] = new File(m_ModelDatasets.get(i));
		provider = new LocalDatasetProvider();
		provider.setDatasets(datasets);
		result.setDatasetProvider(provider);

		// evaluator
		runs = null;
		if (((Integer) m_SpinnerNumRuns.getValue()) > 1) {
			runs = new RepeatedRuns();
			runs.setLowerRuns(1);
			runs.setUpperRuns((Integer) m_SpinnerNumRuns.getValue());
		}

		cv    = null;
		split = null;
		if (m_ComboBoxEvaluation.getSelectedIndex() == 0) {
			cv = new CrossValidation();
			cv.setNumFolds((Integer) m_SpinnerNumFolds.getValue());
			cv.setPreserveOrder(m_CheckBoxPreserveOrder.isSelected());
		}
		else {
			split = new PercentageSplit();
			split.setTrainPercentage(Double.parseDouble(m_TextPercentage.getText()));
			split.setPreserveOrder(m_CheckBoxPreserveOrder.isSelected());
		}
		if (runs != null) {
			if (cv != null)
				runs.setEvaluator(cv);
			else
				runs.setEvaluator(split);
			result.setEvaluator(runs);
		}
		else {
			if (cv != null)
				result.setEvaluator(cv);
			else
				result.setEvaluator(split);
		}

		// statistics
		result.setStatisticsHandler((EvaluationStatisticsHandler) m_GOEStatisticsHandler.getValue());

		return result;
	}
}
