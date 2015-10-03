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
 * LogTab.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;
import meka.core.FileUtils;
import meka.gui.core.GUIHelper;
import meka.gui.choosers.MekaFileChooser;
import weka.gui.ExtensionFileFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * For logging output.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class LogTab
		extends AbstractExplorerTab {

	/** for serialization. */
	private static final long serialVersionUID = 7200133227901982729L;

	/** the panel for visualizing the data. */
	protected JTextArea m_TextArea;

	/** the button for clearing the log. */
	protected JButton m_ButtonClear;

	/** the button for saving the log. */
	protected JButton m_ButtonSave;

	/** the formatter for the timestamp. */
	protected SimpleDateFormat m_Formatter;

	/** the filechooser for saving the log. */
	protected MekaFileChooser m_FileChooser;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		ExtensionFileFilter     filter;

		super.initialize();

		m_Formatter   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		m_FileChooser = GUIHelper.newFileChooser();
		filter        = new ExtensionFileFilter(".log", "Log files");
		m_FileChooser.addChoosableFileFilter(filter);
		m_FileChooser.setFileFilter(filter);
		m_FileChooser.setAcceptAllFileFilterUsed(true);
	}

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		JPanel      panel;

		super.initGUI();

		m_TextArea = new JTextArea(20, 40);
		m_TextArea.setFont(GUIHelper.getMonospacedFont());
		m_TextArea.setEditable(false);
		add(new BaseScrollPane(m_TextArea), BorderLayout.CENTER);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(panel, BorderLayout.SOUTH);

		m_ButtonClear = new JButton("Clear", GUIHelper.getIcon("new.gif"));
		m_ButtonClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		panel.add(m_ButtonClear);

		m_ButtonSave = new JButton("Save", GUIHelper.getIcon("save.gif"));
		m_ButtonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		panel.add(m_ButtonSave);
	}

	/**
	 * Returns the title of the tab.
	 *
	 * @return the title
	 */
	@Override
	public String getTitle() {
		return "Log";
	}

	/**
	 * Removes the logging content.
	 */
	protected synchronized void clear() {
		m_TextArea.setText("");
	}

	/**
	 * Saves the logging content.
	 */
	protected synchronized void save() {
		int             retVal;
		File            file;
		String          text;
		FileWriter      fwriter;
		BufferedWriter  bwriter;

		retVal = m_FileChooser.showOpenDialog(this);
		if (retVal != MekaFileChooser.APPROVE_OPTION)
			return;

		file    = m_FileChooser.getSelectedFile();
		text    = m_TextArea.getText();
		fwriter = null;
		bwriter = null;
		try {
			fwriter = new FileWriter(file);
			bwriter = new BufferedWriter(fwriter);
			bwriter.write(text);
			bwriter.newLine();
			bwriter.flush();
			log("Log successfully saved to: " + file);
		}
		catch (Exception e) {
			handleException("Failed to save log output to: " + file, e);
		}
		finally {
			FileUtils.closeQuietly(bwriter);
			FileUtils.closeQuietly(fwriter);
		}
	}

	/**
	 * For logging messages.
	 *
	 * @param tab       the origin of the message
	 * @param msg       the message to output
	 */
	protected synchronized void log(AbstractExplorerTab tab, String msg) {
		m_TextArea.append("[" + m_Formatter.format(new Date()) + "] ");
		if (tab != null)
			m_TextArea.append(tab.getTitle() + ": " + msg);
		else
			m_TextArea.append(msg);
		m_TextArea.append("\n");
	}
}
