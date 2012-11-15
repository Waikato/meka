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
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import meka.gui.core.GUIHelper;
import meka.gui.core.ResultHistoryList;
import meka.gui.goe.GenericObjectEditor;
import weka.classifiers.multilabel.Evaluation;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Result;

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

	/** the GOE for the classifier. */
	protected GenericObjectEditor m_GenericObjectEditor;
	
	/** the text area for displaying the results. */
	protected JTextArea m_TextAreaResults;
	
	/** the result history. */
	protected ResultHistoryList m_ResultHistoryList;
	
	/** the button for running an experiment. */
	protected JButton m_ButtonStart;
	
	/**
	 * Initializes the tab.
	 * 
	 * @param owner the Explorer this tab belongs to
	 */
	public ClassifyTab(Explorer owner) {
		super(owner);
	}

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();
		
		m_GenericObjectEditor = new GenericObjectEditor(true);
		m_GenericObjectEditor.setClassType(MultilabelClassifier.class);
		m_GenericObjectEditor.setValue(new weka.classifiers.multilabel.BR());
	}
	
	/**
	 * Initializes the widgets.
	 */
	@Override
	public void initGUI() {
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
		
		m_ButtonStart = new JButton("Start");
		m_ButtonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startClassification();
			}
		});
		panelEval.add(m_ButtonStart, BorderLayout.SOUTH);
		
		m_ResultHistoryList = new ResultHistoryList();
		m_ResultHistoryList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				displayResults();
			}
		});
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
		Runnable run;
		
		run = new Runnable() {
			@Override
			public void run() {
				MultilabelClassifier classifier;
				Result[] results;

				// TODO CV or train/test, random seed
				
				try {
					classifier = (MultilabelClassifier) m_GenericObjectEditor.getValue();
					results = Evaluation.cvModel(classifier, getData(), 10, "C");
					for (Result result: results)
						addResultToHistory(result);
				}
				catch (Exception e) {
					System.err.println("Evaluation failed:");
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							ClassifyTab.this, 
							"Evaluation failed:\n" + e, 
							"Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		
		start(run);
	}
	
	/**
	 * Adds the result to the history.
	 */
	protected void addResultToHistory(final Result result) {
		Runnable run;
		
		run = new Runnable() {
			@Override
			public void run() {
				m_ResultHistoryList.addResult(result);
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
		
		result = (Result) m_ResultHistoryList.getResultAt(m_ResultHistoryList.getSelectedIndex());
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
		return "Classification";
	}
	
	/**
	 * Gets called when the data changed.
	 */
	@Override
	protected void update() {
		m_ButtonStart.setEnabled(hasData());
	}

	/**
	 * Gets called when the thread starts.
	 */
	@Override
	protected void executionStarted() {
		m_ButtonStart.setEnabled(false);
	}

	/**
	 * Gets called when the thread finishes or gets stopped.
	 * 
	 * @param t if the execution generated an exception, null if no errors
	 */
	@Override
	protected void executionFinished(Throwable t) {
		m_ButtonStart.setEnabled(true);
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
