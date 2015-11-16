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
 * ApprovalDialog.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Base class for approval dialogs, i.e., dialogs that require the
 * user to click on Yes, No or Cancel. Closing the dialog is considered
 * a Cancel operation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ApprovalDialog
	extends MekaDialog {

	/** the approve option. */
	public final static int APPROVE_OPTION = JOptionPane.YES_OPTION;

	/** the discard option. */
	public final static int DISCARD_OPTION = JOptionPane.NO_OPTION;

	/** the cancel option. */
	public final static int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;

	/** the content panel. */
	protected JPanel m_PanelContent;

	/** the buttons panel. */
	protected JPanel m_PanelButtons;

	/** the approve button. */
	protected JButton m_ButtonApprove;

	/** the discard button. */
	protected JButton m_ButtonDiscard;

	/** the cancel button. */
	protected JButton m_ButtonCancel;

	/** the option selected by the user (CANCEL_OPTION, APPROVE_OPTION). */
	protected int m_Option;

	/**
	 * Creates a modeless dialog without a title and without a specified Frame
	 * owner.
	 */
	public ApprovalDialog() {
		super();
	}

	/**
	 * Creates a modeless dialog without a title with the specified Dialog as
	 * its owner.
	 *
	 * @param owner	the owning dialog
	 */
	public ApprovalDialog(Dialog owner) {
		super(owner);
	}

	/**
	 * Creates a dialog with the specified owner Dialog and modality.
	 *
	 * @param owner	the owning dialog
	 * @param modality	the type of modality
	 */
	public ApprovalDialog(Dialog owner, ModalityType modality) {
		super(owner, modality);
	}

	/**
	 * Creates a modeless dialog with the specified title and with the specified
	 * owner dialog.
	 *
	 * @param owner	the owning dialog
	 * @param title	the title of the dialog
	 */
	public ApprovalDialog(Dialog owner, String title) {
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
	public ApprovalDialog(Dialog owner, String title, ModalityType modality) {
		super(owner, title, modality);
	}

	/**
	 * Creates a modeless dialog without a title with the specified Frame as
	 * its owner.
	 *
	 * @param owner	the owning frame
	 */
	public ApprovalDialog(Frame owner) {
		super(owner);
	}

	/**
	 * Creates a dialog with the specified owner Frame, modality and an empty
	 * title.
	 *
	 * @param owner	the owning frame
	 * @param modal	whether the dialog is modal or not
	 */
	public ApprovalDialog(Frame owner, boolean modal) {
		super(owner, modal);
	}

	/**
	 * Creates a modeless dialog with the specified title and with the specified
	 * owner frame.
	 *
	 * @param owner	the owning frame
	 * @param title	the title of the dialog
	 */
	public ApprovalDialog(Frame owner, String title) {
		super(owner, title);
	}

	/**
	 * Creates a dialog with the specified owner Frame, modality and title.
	 *
	 * @param owner	the owning frame
	 * @param title	the title of the dialog
	 * @param modal	whether the dialog is modal or not
	 */
	public ApprovalDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		getContentPane().setLayout(new BorderLayout());

		m_PanelContent = new JPanel(new BorderLayout());
		getContentPane().add(m_PanelContent, BorderLayout.CENTER);

		m_PanelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(m_PanelButtons, BorderLayout.SOUTH);

		m_ButtonApprove = new JButton("OK");
		m_ButtonApprove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_Option = APPROVE_OPTION;
				setVisible(false);
			}
		});
		m_PanelButtons.add(m_ButtonApprove);

		m_ButtonDiscard = new JButton("Discard");
	    m_ButtonDiscard.setVisible(false);
		m_ButtonDiscard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_Option = DISCARD_OPTION;
				setVisible(false);
			}
		});
		m_PanelButtons.add(m_ButtonDiscard);

		m_ButtonCancel = new JButton("Cancel");
		m_ButtonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_Option = CANCEL_OPTION;
				setVisible(false);
			}
		});
		m_PanelButtons.add(m_ButtonCancel);
	}

	/**
	 * Hook method just before the dialog is made visible.
	 */
	@Override
	protected void beforeShow() {
		super.beforeShow();

		m_Option = CANCEL_OPTION;
	}

	/**
	 * Returns the selected option.
	 *
	 * @return			the option
	 */
	public int getOption() {
		return m_Option;
	}

	/**
	 * Returns the approve button.
	 *
	 * @return		the button
	 */
	public JButton getApproveButton() {
		return m_ButtonApprove;
	}

	/**
	 * Sets the visbility of the approve button.
	 *
	 * @param value	true if to display button
	 */
	public void setApproveVisible(boolean value) {
		m_ButtonApprove.setVisible(value);
	}

	/**
	 * Returns the visibility of the approve button.
	 *
	 * @return		true if visible
	 */
	public boolean isApproveVisible() {
		return m_ButtonApprove.isVisible();
	}

	/**
	 * Sets the caption/text of the approve button.
	 *
	 * @param value	the new text
	 */
	public void setApproveCaption(String value) {
		m_ButtonApprove.setText(value);
	}

	/**
	 * Returns the caption/text of the approve button.
	 *
	 * @return		the current text
	 */
	public String getApproveCaption() {
		return m_ButtonApprove.getText();
	}

	/**
	 * Sets the mnemonic of the approve button.
	 *
	 * @param value	the new mnemonic, e.g., KeyEvent.VK_O
	 */
	public void setApproveMnemonic(int value) {
		m_ButtonApprove.setMnemonic(value);
	}

	/**
	 * Returns the mnemonic of the approve button.
	 *
	 * @return		the current mnemonic, e.g., KeyEvent.VK_O
	 */
	public int getApproveMnemonic() {
		return m_ButtonApprove.getMnemonic();
	}

	/**
	 * Returns the discard button.
	 *
	 * @return		the button
	 */
	public JButton getDiscardButton() {
		return m_ButtonDiscard;
	}

	/**
	 * Sets the visbility of the discard button.
	 *
	 * @param value	true if to display button
	 */
	public void setDiscardVisible(boolean value) {
		m_ButtonDiscard.setVisible(value);
	}

	/**
	 * Returns the visibility of the discard button.
	 *
	 * @return		true if visible
	 */
	public boolean isDiscardVisible() {
		return m_ButtonDiscard.isVisible();
	}

	/**
	 * Sets the caption/text of the discard button.
	 *
	 * @param value	the new text
	 */
	public void setDiscardCaption(String value) {
		m_ButtonDiscard.setText(value);
	}

	/**
	 * Returns the caption/text of the discard button.
	 *
	 * @return		the current text
	 */
	public String getDiscardCaption() {
		return m_ButtonDiscard.getText();
	}

	/**
	 * Sets the mnemonic of the discard button.
	 *
	 * @param value	the new mnemonic, e.g., KeyEvent.VK_D
	 */
	public void setDiscardMnemonic(int value) {
		m_ButtonDiscard.setMnemonic(value);
	}

	/**
	 * Returns the mnemonic of the discard button.
	 *
	 * @return		the current mnemonic, e.g., KeyEvent.VK_D
	 */
	public int getDiscardMnemonic() {
		return m_ButtonDiscard.getMnemonic();
	}

	/**
	 * Returns the cancel button.
	 *
	 * @return		the button
	 */
	public JButton getCancelButton() {
		return m_ButtonCancel;
	}

	/**
	 * Sets the visbility of the cancel button.
	 *
	 * @param value	true if to display button
	 */
	public void setCancelVisible(boolean value) {
		m_ButtonCancel.setVisible(value);
	}

	/**
	 * Returns the visibility of the cancel button.
	 *
	 * @return		true if visible
	 */
	public boolean isCancelVisible() {
		return m_ButtonCancel.isVisible();
	}

	/**
	 * Sets the caption/text of the Cancel button.
	 *
	 * @param value	the new text
	 */
	public void setCancelCaption(String value) {
		m_ButtonCancel.setText(value);
	}

	/**
	 * Returns the caption/text of the Cancel button.
	 *
	 * @return		the current text
	 */
	public String getCancelCaption() {
		return m_ButtonCancel.getText();
	}

	/**
	 * Sets the mnemonic of the Cancel button.
	 *
	 * @param value	the new mnemonic, e.g., KeyEvent.VK_C
	 */
	public void setCancelMnemonic(int value) {
		m_ButtonCancel.setMnemonic(value);
	}

	/**
	 * Returns the mnemonic of the Cancel button.
	 *
	 * @return		the current mnemonic, e.g., KeyEvent.VK_C
	 */
	public int getCancelMnemonic() {
		return m_ButtonCancel.getMnemonic();
	}

	/**
	 * Returns the panel to add the content to.
	 *
	 * @return			the content panel
	 */
	public JPanel getContentPanel() {
		return m_PanelContent;
	}

	/**
	 * Returns a basic (modal) confirmation dialog (yes/no/cancel).
	 *
	 * @param owner	the owner of the dialog
	 */
	public static ApprovalDialog getConfirmationDialog(Dialog owner) {
		return getConfirmationDialog(owner, ModalityType.DOCUMENT_MODAL);
	}

	/**
	 * Returns a basic confirmation dialog (yes/no/cancel).
	 *
	 * @param owner	the owner of the dialog
	 * @param modal	the modality of the dialog
	 */
	public static ApprovalDialog getConfirmationDialog(Dialog owner, ModalityType modal) {
		ApprovalDialog	result;

		result = new ApprovalDialog(owner, modal);
		result.setApproveCaption("Yes");
		result.setApproveMnemonic(KeyEvent.VK_Y);
		result.setDiscardCaption("No");
		result.setDiscardMnemonic(KeyEvent.VK_N);
		result.setDiscardVisible(true);

		return result;
	}

	/**
	 * Returns a basic (modal) confirmation dialog (yes/no/cancel).
	 *
	 * @param owner	the owner of the dialog
	 */
	public static ApprovalDialog getConfirmationDialog(Frame owner) {
		return getConfirmationDialog(owner, true);
	}

	/**
	 * Returns a basic confirmation dialog (yes/no/cancel).
	 *
	 * @param owner	the owner of the dialog
	 * @param modal	whether to create a modal dialog
	 */
	public static ApprovalDialog getConfirmationDialog(Frame owner, boolean modal) {
		ApprovalDialog	result;

		result = new ApprovalDialog(owner, modal);
		result.setApproveCaption("Yes");
		result.setApproveMnemonic(KeyEvent.VK_Y);
		result.setDiscardCaption("No");
		result.setDiscardMnemonic(KeyEvent.VK_N);
		result.setDiscardVisible(true);

		return result;
	}

	/**
	 * Returns a basic (modal) info dialog (ok).
	 *
	 * @param owner	the owner of the dialog
	 */
	public static ApprovalDialog getInformationDialog(Dialog owner) {
		return getInformationDialog(owner, ModalityType.DOCUMENT_MODAL);
	}

	/**
	 * Returns a basic info dialog (ok).
	 *
	 * @param owner	the owner of the dialog
	 * @param modal	the modality of the dialog
	 */
	public static ApprovalDialog getInformationDialog(Dialog owner, ModalityType modal) {
		ApprovalDialog	result;

		result = new ApprovalDialog(owner, modal);
		result.setApproveVisible(true);
		result.setDiscardVisible(false);
		result.setCancelVisible(false);

		return result;
	}

	/**
	 * Returns a basic (modal) info dialog (ok).
	 *
	 * @param owner	the owner of the dialog
	 */
	public static ApprovalDialog getInformationDialog(Frame owner) {
		return getInformationDialog(owner, true);
	}

	/**
	 * Returns a basic info dialog (ok).
	 *
	 * @param owner	the owner of the dialog
	 * @param modal	whether to create a modal dialog
	 */
	public static ApprovalDialog getInformationDialog(Frame owner, boolean modal) {
		ApprovalDialog	result;

		result = new ApprovalDialog(owner, modal);
		result.setApproveVisible(true);
		result.setDiscardVisible(false);
		result.setCancelVisible(false);

		return result;
	}
}
