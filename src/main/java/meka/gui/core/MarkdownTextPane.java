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
 * MarkdownTextPane.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import com.googlecode.jfilechooserbookmarks.gui.BasePanel;
import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;
import org.markdownj.MarkdownProcessor;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.Document;
import java.awt.BorderLayout;

/**
 * Renders Markdown text.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MarkdownTextPane
	extends BasePanel {

	private static final long serialVersionUID = -3021897813785552183L;

	/** the markdown processor. */
	protected MarkdownProcessor m_Processor;

	/** the markdown text. */
	protected String m_Markdown;

	/** for rendering the markdown. */
	protected JEditorPane m_PaneView;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();

		m_Processor = new MarkdownProcessor();
		m_Markdown  = "";
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		setLayout(new BorderLayout());

		m_PaneView = new JEditorPane();
		m_PaneView.setEditable(false);
		m_PaneView.setContentType("text/html");
		add(new BaseScrollPane(m_PaneView), BorderLayout.CENTER);
	}

	/**
	 * Sets the Markdown text and renders it.
	 *
	 * @param value	the markdown text
	 */
	public void setText(String value) {
		String	html;

		if (value == null)
			value = "";
		m_Markdown = value;

		html = m_Processor.markdown(m_Markdown);
		try {
			m_PaneView.setText("<html>" + html + "</html>");
			m_PaneView.setCaretPosition(0);
		}
		catch (Exception e) {
			System.err.println("Failed to update preview!");
			e.printStackTrace();
		}
	}

	/**
	 * Returns the Markdown text.
	 *
	 * @return		the markdown text
	 */
	public String getText() {
		return m_Markdown;
	}

	/**
	 * Sets whether the text pane is editable or not.
	 *
	 * @param value if true the text pane is editable
	 */
	public void setEditable(boolean value) {
		m_PaneView.setEditable(value);
	}

	/**
	 * Returns whether the text pane is editable or not.
	 *
	 * @return true if the text pane is editable
	 */
	public boolean isEditable() {
		return m_PaneView.isEditable();
	}

	/**
	 * Returns the underlying document.
	 *
	 * @return		the document
	 */
	public Document getDocument() {
		return m_PaneView.getDocument();
	}

	/**
	 * Sets the position of the cursor.
	 *
	 * @param value	the position
	 */
	public void setCaretPosition(int value) {
		m_PaneView.setCaretPosition(value);
	}

	/**
	 * Returns the current position of the cursor.
	 *
	 * @return		the cursor position
	 */
	public int getCaretPosition() {
		return m_PaneView.getCaretPosition();
	}

	/**
	 * Sets the position of the cursor at the end.
	 */
	public void setCaretPositionLast() {
		setCaretPosition(getDocument().getLength());
	}

	/**
	 * For testing only.
	 *
	 * @param args	ignored
	 */
	public static void main(String[] args) {
		MarkdownTextPane pane = new MarkdownTextPane();
		pane.setText("# Markdown test\n\n* item 1\n* item 2\n\n## Other stuff\n*italic* __bold__");
		JFrame frame = new JFrame("Markdown test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(pane, BorderLayout.CENTER);
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
