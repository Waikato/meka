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
 * MarkdownDialog.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import java.awt.Dialog;
import java.awt.Frame;

/**
 * Dialog for editing/previewing Markdown.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MarkdownDialog
  extends ApprovalDialog {

	/** the markdown widget. */
	protected MarkdownTextAreaWithPreview m_TextMarkdown;

	/**
	 * Creates a modeless dialog without a title and without a specified Frame
	 * owner.
	 */
	public MarkdownDialog() {
		super();
	}

	/**
	 * Creates a modeless dialog without a title with the specified Dialog as
	 * its owner.
	 *
	 * @param owner	the owning dialog
	 */
	public MarkdownDialog(Dialog owner) {
		super(owner);
	}

	/**
	 * Creates a dialog with the specified owner Dialog and modality.
	 *
	 * @param owner	the owning dialog
	 * @param modality	the type of modality
	 */
	public MarkdownDialog(Dialog owner, ModalityType modality) {
		super(owner, modality);
	}

	/**
	 * Creates a modeless dialog with the specified title and with the specified
	 * owner dialog.
	 *
	 * @param owner	the owning dialog
	 * @param title	the title of the dialog
	 */
	public MarkdownDialog(Dialog owner, String title) {
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
	public MarkdownDialog(Dialog owner, String title, ModalityType modality) {
		super(owner, title, modality);
	}

	/**
	 * Creates a modeless dialog without a title with the specified Frame as
	 * its owner.
	 *
	 * @param owner	the owning frame
	 */
	public MarkdownDialog(Frame owner) {
		super(owner);
	}

	/**
	 * Creates a dialog with the specified owner Frame, modality and an empty
	 * title.
	 *
	 * @param owner	the owning frame
	 * @param modal	whether the dialog is modal or not
	 */
	public MarkdownDialog(Frame owner, boolean modal) {
		super(owner, modal);
	}

	/**
	 * Creates a modeless dialog with the specified title and with the specified
	 * owner frame.
	 *
	 * @param owner	the owning frame
	 * @param title	the title of the dialog
	 */
	public MarkdownDialog(Frame owner, String title) {
		super(owner, title);
	}

	/**
	 * Creates a dialog with the specified owner Frame, modality and title.
	 *
	 * @param owner	the owning frame
	 * @param title	the title of the dialog
	 * @param modal	whether the dialog is modal or not
	 */
	public MarkdownDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		m_TextMarkdown = new MarkdownTextAreaWithPreview();
		m_TextMarkdown.setLineWrap(true);
		m_TextMarkdown.setWrapStyleWord(true);
		getContentPane().add(m_TextMarkdown);
	}

	/**
	 * Sets the markdown text.
	 *
	 * @param value			the markdown text
	 */
	public void setMarkdown(String value) {
		m_TextMarkdown.setText(value);
	}

	/**
	 * Returns the markdown text.
	 *
	 * @return				the markdown text
	 */
	public String getMarkdown() {
		return m_TextMarkdown.getText();
	}
}
