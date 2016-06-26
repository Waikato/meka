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
  *    DataViewerDialog.java
  *    Copyright (C) 2005-2016 University of Waikato, Hamilton, New Zealand
  *
  */

package meka.gui.dataviewer;

import com.googlecode.jfilechooserbookmarks.core.Utils;
import meka.gui.core.GUIHelper;
import weka.core.Instances;
import weka.core.converters.AbstractFileSaver;
import weka.gui.ConverterFileChooser;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * A downsized version of the DataViewer, displaying only one Instances-Object.
 *
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 12697 $ 
 */
public class DataViewerDialog
	extends JDialog
	implements ChangeListener {

	/** for serialization */
	private static final long serialVersionUID = 6747718484736047752L;

	/** Signifies an OK property selection */
	public static final int APPROVE_OPTION = 0;

	/** Signifies a cancelled property selection */
	public static final int CANCEL_OPTION = 1;

	/** the result of the user's action, either OK or CANCEL */
	protected int m_Result = CANCEL_OPTION;

	/** Click to activate the current set parameters */
	protected JButton m_OkButton = new JButton("OK");

	/** Click to cancel the dialog */
	protected JButton m_CancelButton = new JButton("Cancel");

	/** Click to undo the last action */
	protected JButton m_UndoButton = new JButton("Undo");

	/** Click to add a new instance to the end of the dataset */
	protected JButton m_addInstanceButton = new JButton("Add instance");

	/** Click to export the data*/
	protected JButton m_ExportButton = new JButton("Export...");

	/** the panel to display the Instances-object */
	protected DataPanel m_DataPanel = new DataPanel();

	/** the file chooser for loading/saving files. */
	protected ConverterFileChooser m_FileChooser;

	/**
	 * initializes the dialog with the given parent.
	 * Uses {@link java.awt.Dialog.ModalityType#DOCUMENT_MODAL}.
	 *
	 * @param parent the parent for this dialog
	 */
	public DataViewerDialog(Frame parent) {
		this(parent, ModalityType.DOCUMENT_MODAL);
	}

	/**
	 * initializes the dialog with the given parent
	 *
	 * @param parent the parent for this dialog
	 * @param modality the modality
	 */
	public DataViewerDialog(Frame parent, ModalityType modality) {
		super(parent, modality);
		createDialog();
	}

	/**
	 * initializes the dialog with the given parent. Modal dialog.
	 *
	 * @param parent the parent for this dialog
	 */
	public DataViewerDialog(Dialog parent) {
		this(parent, true);
	}

	/**
	 * initializes the dialog with the given parent.
	 *
	 * @param parent the parent for this dialog
	 * @param modal if true then the dialog is modal
	 */
	public DataViewerDialog(Dialog parent, boolean modal) {
		super(parent, modal);
		createDialog();
	}

	/**
	 * creates all the elements of the dialog
	 */
	protected void createDialog() {
		JPanel              panel;

		setTitle("Viewer");

		getContentPane().setLayout(new BorderLayout());

		// DataPanel
		m_DataPanel.addChangeListener(this);
		getContentPane().add(m_DataPanel, BorderLayout.CENTER);

		// Buttons
		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(panel, BorderLayout.SOUTH);
		m_UndoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undo();
			}
		});
		getContentPane().add(panel, BorderLayout.SOUTH);
		m_CancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Result = CANCEL_OPTION;
				setVisible(false);
			}
		});
		m_OkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Result = APPROVE_OPTION;
				setVisible(false);
			}
		});
		m_addInstanceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_DataPanel.addInstanceAtEnd();
			}
		});
		m_ExportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		panel.add(m_addInstanceButton);
		panel.add(m_UndoButton);
		panel.add(m_ExportButton);
		panel.add(m_OkButton);
		panel.add(m_CancelButton);

		pack();
	}

	/**
	 * sets the instances to display
	 */
	public void setInstances(Instances inst) {
		m_DataPanel.setInstances(new Instances(inst));
	}

	/**
	 * returns the currently displayed instances
	 */
	public Instances getInstances() {
		return m_DataPanel.getInstances();
	}

	/**
	 * sets the state of the buttons
	 */
	protected void setButtons() {
		m_OkButton.setEnabled(true);
		m_CancelButton.setEnabled(true);
		m_UndoButton.setEnabled(m_DataPanel.canUndo());
	}

	/**
	 * Exports the data to a file.
	 */
	protected void export() {
		int retVal;
		File file;
		AbstractFileSaver saver;

		if (m_FileChooser == null)
			m_FileChooser = GUIHelper.newConverterFileChooser();

		retVal = m_FileChooser.showSaveDialog(this);
		if (retVal != ConverterFileChooser.APPROVE_OPTION)
			return;

		file  = m_FileChooser.getSelectedFile();
		saver = m_FileChooser.getSaver();
		try {
			saver.setInstances(m_DataPanel.getInstances());
			saver.writeBatch();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(
				this,
				"Failed to save dataset to '" + file + "':\n" + Utils.throwableToString(e),
				"Error saving",
				JOptionPane.ERROR_MESSAGE);
		}
		// TODO
	}

	/**
	 * returns whether the data has been changed
	 *
	 * @return true if the data has been changed
	 */
	public boolean isChanged() {
		return m_DataPanel.isChanged();
	}

	/**
	 * undoes the last action
	 */
	protected void undo() {
		m_DataPanel.undo();
	}

	/**
	 * Invoked when the target of the listener has changed its state.
	 */
	public void stateChanged(ChangeEvent e) {
		setButtons();
	}

	/**
	 * Pops up the modal dialog and waits for Cancel or OK.
	 *
	 * @return either APPROVE_OPTION, or CANCEL_OPTION
	 */
	public int showDialog() {
		m_Result = CANCEL_OPTION;
		setVisible(true);
		setButtons();
		return m_Result;
	}

	/**
	 * Pops up the modal dialog and waits for Cancel or OK.
	 *
	 * @param inst the instances to display
	 * @return either APPROVE_OPTION, or CANCEL_OPTION
	 */
	public int showDialog(Instances inst) {
		setInstances(inst);
		return showDialog();
	}
}
