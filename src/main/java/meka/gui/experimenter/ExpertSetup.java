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
 * ExpertSetup.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.experimenter;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.ObjectUtils;
import meka.core.OptionUtils;
import meka.experiment.Experiment;
import meka.experiment.datasetproviders.DatasetProvider;
import meka.experiment.datasetproviders.LocalDatasetProvider;
import meka.experiment.evaluationstatistics.EvaluationStatisticsHandler;
import meka.experiment.evaluationstatistics.KeyValuePairs;
import meka.experiment.evaluators.Evaluator;
import meka.experiment.evaluators.RepeatedRuns;
import meka.gui.core.ListWithButtons;
import meka.gui.core.MarkdownDialog;
import meka.gui.core.ParameterPanel;
import meka.gui.goe.GenericObjectEditor;
import meka.gui.goe.GenericObjectEditorDialog;
import weka.gui.JListHelper;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Tab for setting up a experiment in expert mode.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExpertSetup
  extends AbstractSetupTab {

	private static final long serialVersionUID = 3556506064253273853L;

	/** the parameters of the experiment. */
	protected ParameterPanel m_ParameterPanel;

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

	/** the GOE for the datasets. */
	protected GenericObjectEditor m_GOEDatasets;

	/** the GOE for the evaluator. */
	protected GenericObjectEditor m_GOEEvaluator;

	/** the GOE for the statistics handler. */
	protected GenericObjectEditor m_GOEStatisticsHandler;

	/** the button for the notes. */
	protected JButton m_ButtonNotes;

	/** the notes. */
	protected String m_Notes;

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		JPanel      panel;

		super.initGUI();

		panel = new JPanel(new BorderLayout());
		m_PanelSetup.add(panel, BorderLayout.CENTER);

		m_ModelClassifiers = new DefaultListModel<>();
		m_ListClassifiers = new ListWithButtons();
		m_ListClassifiers.getList().setModel(m_ModelClassifiers);
		m_ListClassifiers.getList().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateButtons();
			}
		});
		panel.add(m_ListClassifiers, BorderLayout.CENTER);

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

		m_ParameterPanel = new ParameterPanel();
		panel.add(m_ParameterPanel, BorderLayout.SOUTH);

		m_GOEDatasets = new GenericObjectEditor(true);
		m_GOEDatasets.setClassType(DatasetProvider.class);
		m_GOEDatasets.setValue(new LocalDatasetProvider());
		m_GOEDatasets.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setModified(true);
			}
		});
		m_ParameterPanel.addParameter("Datasets", setPreferredSize(m_GOEDatasets.getCustomPanel()));

		m_GOEEvaluator = new GenericObjectEditor(true);
		m_GOEEvaluator.setClassType(Evaluator.class);
		m_GOEEvaluator.setValue(new RepeatedRuns());
		m_GOEEvaluator.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setModified(true);
			}
		});
		m_ParameterPanel.addParameter("Evaluator", setPreferredSize(m_GOEEvaluator.getCustomPanel()));

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

		m_ButtonNotes = new JButton("...");
		m_ButtonNotes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editNotes();
			}
		});
		m_ParameterPanel.addParameter("Notes", m_ButtonNotes);
	}

	/**
	 * Returns the title of the tab.
	 *
	 * @return the title
	 */
	@Override
	public String getTitle() {
		return "Setup (expert)";
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

		// other
		m_ButtonNotes.setEnabled(editable);
	}

	/**
	 * Lets the user add a classifier.
	 */
	protected void addClassifier() {
		GenericObjectEditorDialog   dialog;

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
	 * Displays dialog for entering notes in Markdown.
	 */
	protected void editNotes() {
		MarkdownDialog dialog;

		if (getParentDialog() != null)
			dialog = new MarkdownDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
		else
			dialog = new MarkdownDialog(getParentFrame(), true);
		dialog.setTitle("Edit notes");
		dialog.setMarkdown(m_Notes);
		dialog.setSize(600, 400);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		if (dialog.getOption() != MarkdownDialog.APPROVE_OPTION)
			return;
		m_Notes = dialog.getMarkdown();
		setModified(true);
		updateButtons();
	}

	/**
	 * Resets the interface.
	 */
	protected void clear() {
		m_ModelClassifiers.removeAllElements();
		m_GOEDatasets.setValue(new LocalDatasetProvider());
		m_GOEEvaluator.setValue(new RepeatedRuns());
		m_GOEStatisticsHandler.setValue(new KeyValuePairs());
	}

	/**
	 * Checks whether this type of experiment is handled by this tab.
	 *
	 * @param exp       the experiment to check
	 * @return          true if handled
	 */
	protected boolean handlesExperiment(Experiment exp) {
		return true;
	}

	/**
	 * Maps the experiment onto the parameters.
	 */
	protected void fromExperiment() {
		m_ModelClassifiers.removeAllElements();
		for (MultiLabelClassifier classifier: m_Experiment.getClassifiers())
			m_ModelClassifiers.addElement(OptionUtils.toCommandLine(classifier));

		m_GOEDatasets.setValue(m_Experiment.getDatasetProvider());
		m_GOEEvaluator.setValue(m_Experiment.getEvaluator());
		m_GOEStatisticsHandler.setValue(m_Experiment.getStatisticsHandler());
		m_Notes = m_Experiment.getNotes();
	}

	/**
	 * Stores the parameters in an experiment.
	 *
	 * @return          the generated experiment
	 */
	protected Experiment toExperiment() {
		Experiment              result;
		MultiLabelClassifier[]  classifiers;
		int                     i;

		result = (Experiment) ObjectUtils.deepCopy(m_Experiment);
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
		result.setDatasetProvider((DatasetProvider) m_GOEDatasets.getValue());
		result.setEvaluator((Evaluator) m_GOEEvaluator.getValue());
		result.setStatisticsHandler((EvaluationStatisticsHandler) m_GOEStatisticsHandler.getValue());
		result.setNotes(m_Notes);

		return result;
	}
}
