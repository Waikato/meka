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
 * MarkdownTextAreaWithPreview.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import com.googlecode.jfilechooserbookmarks.gui.BasePanel;
import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;
import org.markdownj.MarkdownProcessor;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Text area for handling Markdown with code and preview tabs.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MarkdownTextAreaWithPreview
	extends BasePanel {

	private static final long serialVersionUID = -1823780286250700366L;

	/** the tabbed pane. */
	protected JTabbedPane m_TabbedPane;

	/** the text area for writing markdown code. */
	protected JTextArea m_TextCode;

	/** the preview. */
	protected JEditorPane m_PanePreview;

	/** the markdown processor. */
	protected MarkdownProcessor m_Processor;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();

		m_Processor = new MarkdownProcessor();
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		setLayout(new BorderLayout());

		m_TabbedPane = new JTabbedPane();
		add(m_TabbedPane, BorderLayout.CENTER);

		m_TextCode = new JTextArea();
		m_TextCode.setFont(GUIHelper.getMonospacedFont());
		m_TabbedPane.addTab("Write", new BaseScrollPane(m_TextCode));

		m_PanePreview = new JEditorPane();
		m_PanePreview.setEditable(false);
		m_PanePreview.setContentType("text/html");
		m_TabbedPane.addTab("Preview", new BaseScrollPane(m_PanePreview));

		m_TabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				update();
			}
		});
	}

	/**
	 * Sets the markdown code to display.
	 *
	 * @param value	the markdown code
	 */
	public void setText(String value) {
		if (value == null)
			value = "";
		m_TextCode.setText(value);
		update();
	}

	/**
	 * Returns the markdown code to display.
	 *
	 * @return		the markdown code
	 */
	public String getText() {
		return m_TextCode.getText();
	}

	/**
	 * Returns the underlying document.
	 *
	 * @return		the document
	 */
	public Document getDocument() {
		return m_TextCode.getDocument();
	}

	/**
	 * Returns the underlying text.
	 *
	 * @return		the underlying text
	 */
	public String getSelectedText() {
		return m_TextCode.getSelectedText();
	}

	/**
	 * Sets the rows.
	 *
	 * @param value	the rows
	 */
	public void setRows(int value) {
		m_TextCode.setRows(value);
	}

	/**
	 * Returns the rows.
	 *
	 * @return		the rows
	 */
	public int getRows() {
		return m_TextCode.getRows();
	}

	/**
	 * Sets the columns.
	 *
	 * @param value	the columns
	 */
	public void setColumns(int value) {
		m_TextCode.setColumns(value);
	}

	/**
	 * Returns the columns.
	 *
	 * @return		the columns
	 */
	public int getColumns() {
		return m_TextCode.getColumns();
	}

	/**
	 * Sets whether the text area is editable or not.
	 *
	 * @param value	if true the text area is editable
	 */
	public void setEditable(boolean value) {
		m_TextCode.setEditable(value);
	}

	/**
	 * Returns whether the text area is editable or not.
	 *
	 * @return		true if the text area is editable
	 */
	public boolean isEditable() {
		return m_TextCode.isEditable();
	}

	/**
	 * Sets whether to line wrap or not.
	 *
	 * @param value	if true line wrap is enabled
	 */
	public void setLineWrap(boolean value) {
		m_TextCode.setLineWrap(value);
	}

	/**
	 * Returns whether line wrap is enabled.
	 *
	 * @return		true if line wrap wrap is enabled
	 */
	public boolean getLineWrap() {
		return m_TextCode.getLineWrap();
	}

	/**
	 * Sets whether to word wrap or not.
	 *
	 * @param value	if true word wrap is enabled
	 */
	public void setWrapStyleWord(boolean value) {
		m_TextCode.setWrapStyleWord(value);
	}

	/**
	 * Returns whether word wrap is enabled.
	 *
	 * @return		true if word wrap wrap is enabled
	 */
	public boolean getWrapStyleWord() {
		return m_TextCode.getWrapStyleWord();
	}

	/**
	 * Sets the text font.
	 *
	 * @param value	the font
	 */
	public void setTextFont(Font value) {
		m_TextCode.setFont(value);
	}

	/**
	 * Returns the text font in use.
	 *
	 * @return		the font
	 */
	public Font getTextFont() {
		return m_TextCode.getFont();
	}

	/**
	 * Sets the caret position.
	 *
	 * @param pos 	the position (0-based)
	 */
	public void setCaretPosition(int pos) {
		m_TextCode.setCaretPosition(pos);
	}

	/**
	 * Returns the current caret position.
	 *
	 * @return		the position (0-based)
	 */
	public int getCaretPosition() {
		return m_TextCode.getCaretPosition();
	}

	/**
	 * Updates the markdown display.
	 */
	protected void update() {
		String	html;

		html = m_Processor.markdown(getText());
		try {
			m_PanePreview.setText("<html>" + html + "</html>");
			m_PanePreview.setCaretPosition(0);
		}
		catch (Exception e) {
			System.err.println("Failed to update preview!");
			e.printStackTrace();
		}
	}

	/**
	 * For testing only.
	 *
	 * @param args	ignored
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("Markdown test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new MarkdownTextAreaWithPreview(), BorderLayout.CENTER);
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
