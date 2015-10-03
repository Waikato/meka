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
 * GenericObjectEditorDialog.java
 * Copyright (C) 2009-2013 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.goe;

import com.googlecode.jfilechooserbookmarks.gui.GUIHelper;
import meka.gui.core.MekaDialog;

import weka.gui.GenericObjectEditor.GOEPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;

/**
 * Displays a GenericObjectEditor.
 * <br><br>
 * Calling code needs to dispose the dialog manually or enable automatic
 * disposal:
 * <pre>
 * GenericObjectEditorDialog dialog = new ...
 * dialog.setDefaultCloseOperation(GenericObjectEditorDialog.DISPOSE_ON_CLOSE);
 * </pre>
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10824 $
 */
public class GenericObjectEditorDialog
		extends MekaDialog
		implements ActionListener {

	/** for serialization. */
	private static final long serialVersionUID = 450801082654308978L;

	/** constant for dialog cancellation. */
	public final static int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;

	/** constant for dialog approval. */
	public final static int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;

	/** the underlying editor. */
	protected PropertyEditor m_Editor;

	/** the current object. */
	protected Object m_Current;

	/** whether the dialog was cancelled or ok'ed. */
	protected int m_Result;

	/**
	 * Creates a modeless dialog without a title with the specified Dialog as
	 * its owner.
	 *
	 * @param owner	the owning dialog
	 */
	public GenericObjectEditorDialog(Dialog owner) {
		super(owner);
	}

	/**
	 * Creates a dialog with the specified owner Dialog and modality.
	 *
	 * @param owner	the owning dialog
	 * @param modality	the type of modality
	 */
	public GenericObjectEditorDialog(Dialog owner, Dialog.ModalityType modality) {
		super(owner, modality);
	}

	/**
	 * Creates a modeless dialog with the specified title and with the specified
	 * owner dialog.
	 *
	 * @param owner	the owning dialog
	 * @param title	the title of the dialog
	 */
	public GenericObjectEditorDialog(Dialog owner, String title) {
		super(owner, title);
	}

	/**
	 * Creates a dialog with the specified title, modality and the specified
	 * owner Dialog.
	 *
	 * @param owner	the owning dialog
	 * @param title	the title of the dialog
	 * @param modality	the type of modality
	 */
	public GenericObjectEditorDialog(Dialog owner, String title, Dialog.ModalityType modality) {
		super(owner, title, modality);
	}

	/**
	 * Creates a modeless dialog without a title with the specified Frame as
	 * its owner.
	 *
	 * @param owner	the owning frame
	 */
	public GenericObjectEditorDialog(Frame owner) {
		super(owner);
	}

	/**
	 * Creates a dialog with the specified owner Frame, modality and an empty
	 * title.
	 *
	 * @param owner	the owning frame
	 * @param modal	whether the dialog is modal or not
	 */
	public GenericObjectEditorDialog(Frame owner, boolean modal) {
		super(owner, modal);
	}

	/**
	 * Creates a modeless dialog with the specified title and with the specified
	 * owner frame.
	 *
	 * @param owner	the owning frame
	 * @param title	the title of the dialog
	 */
	public GenericObjectEditorDialog(Frame owner, String title) {
		super(owner, title);
	}

	/**
	 * Creates a dialog with the specified owner Frame, modality and title.
	 *
	 * @param owner	the owning frame
	 * @param title	the title of the dialog
	 * @param modal	whether the dialog is modal or not
	 */
	public GenericObjectEditorDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	/**
	 * For initializing members.
	 */
	@Override
	protected void initialize() {
		super.initialize();

		m_Editor  = new GenericObjectEditor();
		m_Current = null;
	}

	/**
	 * For initializing the GUI.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		setDefaultCloseOperation(GenericObjectEditorDialog.HIDE_ON_CLOSE);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(m_Editor.getCustomEditor(), BorderLayout.CENTER);
		((GOEPanel) m_Editor.getCustomEditor()).addOkListener(this);

		pack();
	}

	/**
	 * Sets the editor to use.
	 *
	 * @param value	the editor to use
	 */
	public void setEditor(PropertyEditor value) {
		Component view;
		JPanel panelAll;
		JPanel panelButton;
		final JButton buttonOK;

		if (m_Editor.getCustomEditor() instanceof GOEPanel)
			((GOEPanel) m_Editor.getCustomEditor()).removeOkListener(this);
		getContentPane().remove(0);

		m_Editor = value;

		if (m_Editor.getCustomEditor() instanceof GOEPanel)
			((GOEPanel) m_Editor.getCustomEditor()).addOkListener(this);
		if (m_Editor.supportsCustomEditor()) {
			view = (Component) m_Editor.getCustomEditor();
		}
		else {
			view        = GenericObjectEditor.findView(m_Editor);
			panelAll    = new JPanel(new BorderLayout());
			panelAll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
			buttonOK    = new JButton("Close");
			buttonOK.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					GUIHelper.closeParent(buttonOK);
				}
			});
			panelButton.add(buttonOK);
			panelAll.add(panelButton, BorderLayout.SOUTH);
			panelAll.add(view, BorderLayout.CENTER);
			view = panelAll;
		}
		getContentPane().add(view, BorderLayout.CENTER);
		pack();
	}

	/**
	 * Returns the underlying editor.
	 *
	 * @return		the editor in use
	 */
	public PropertyEditor getEditor() {
		return m_Editor;
	}

	/**
	 * Returns whether the underlying editor is GenericObjectEditor.
	 *
	 * @return		true if editor is a GenericObjectEditor one
	 */
	public boolean isGOEEditor() {
		return (m_Editor instanceof GenericObjectEditor);
	}

	/**
	 * Returns the underlying GOE editor.
	 *
	 * @return		the GOE editor in use, or null if other editor used
	 */
	public GenericObjectEditor getGOEEditor() {
		if (m_Editor instanceof GenericObjectEditor)
			return (GenericObjectEditor) m_Editor;
		else
			return null;
	}

	/**
	 * Hook method just before the dialog is made visible.
	 */
	@Override
	protected void beforeShow() {
		super.beforeShow();

		m_Current = m_Editor.getValue();
		// only in case of GOEPanels can be determine whether OK or Cancel was
		// selected.
		if (m_Editor.getCustomEditor() instanceof GOEPanel)
			m_Result = CANCEL_OPTION;
		else
			m_Result = APPROVE_OPTION;
	}

	/**
	 * Sets the current object.
	 *
	 * @param value	the current object, use null for default object
	 */
	public void setCurrent(Object value) {
		if (value == null)
			((GenericObjectEditor) m_Editor).setDefaultValue();
		else
			m_Editor.setValue(value);
		m_Current = value;
	}

	/**
	 * Returns the current object.
	 *
	 * @return		the current object
	 */
	public Object getCurrent() {
		return m_Current;
	}

	/**
	 * Returns whether the dialog got cancelled or approved.
	 *
	 * @return		the result
	 * @see		#APPROVE_OPTION
	 * @see		#CANCEL_OPTION
	 */
	public int getResult() {
		return m_Result;
	}

	/**
	 * Returns whether the dialog got cancelled or approved.
	 *
	 * @return		the result
	 * @see		#APPROVE_OPTION
	 * @see		#CANCEL_OPTION
	 */
	public int getResultType() {
		return m_Result;
	}

	/**
	 * Gets called when the one of the buttons in the GOE panel gets pressed.
	 *
	 * @param e		the event
	 */
	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() instanceof JButton) && (((JButton) e.getSource()).getText().equals("OK"))) {
			m_Current = m_Editor.getValue();
			m_Result  = APPROVE_OPTION;
			setVisible(false);
		}
	}

	/**
	 * Creates a modal dialog for the parent.
	 *
	 * @param parent	the parent to make the dialog modal
	 * @return		the dialog
	 */
	public static GenericObjectEditorDialog createDialog(Container parent) {
		return createDialog(parent, null);
	}

	/**
	 * Creates a modal dialog for the parent with the provided editor.
	 *
	 * @param parent	the parent to make the dialog modal
	 * @param editor	the editor to use
	 * @return		the dialog
	 */
	public static GenericObjectEditorDialog createDialog(Container parent, PropertyEditor editor) {
		return createDialog(parent, editor, null);
	}

	/**
	 * Creates a modal dialog for the parent with the provided editor and initial value.
	 *
	 * @param parent	the parent to make the dialog modal
	 * @param editor	the editor to use, ignored if null
	 * @param value	the value to use, ignored if null
	 * @return		the dialog
	 */
	public static GenericObjectEditorDialog createDialog(Container parent, PropertyEditor editor, Object value) {
		GenericObjectEditorDialog	result;

		if (GUIHelper.getParentDialog(parent) != null)
			result = new GenericObjectEditorDialog(GUIHelper.getParentDialog(parent));
		else
			result = new GenericObjectEditorDialog(GUIHelper.getParentFrame(parent));
		result.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		result.setTitle("Object editor");

		// custom editor?
		if (editor != null)
			result.setEditor(editor);

		// initial value?
		if (value != null)
			result.setCurrent(value);

		return result;
	}

	/**
	 * For testing only.
	 *
	 * @param args	ignored
	 */
	public static void main(String[] args) {
		GenericObjectEditorDialog dialog = new GenericObjectEditorDialog((Frame) null, "Object editor", true);
		dialog.setDefaultCloseOperation(GenericObjectEditorDialog.DISPOSE_ON_CLOSE);
		dialog.getGOEEditor().setClassType(meka.classifiers.multilabel.MultiLabelClassifier.class);
		dialog.getGOEEditor().setCanChangeClassInDialog(true);
		dialog.setCurrent(new meka.classifiers.multilabel.BR());
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		if (dialog.getResult() == APPROVE_OPTION)
			System.out.println(dialog.getCurrent());
	}
}
