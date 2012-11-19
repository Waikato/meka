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
 * MekaPanel.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.JPanel;

/**
 * Extended {@link JPanel}.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MekaPanel
extends JPanel {

	/** for serialization. */
	private static final long serialVersionUID = 8958333625051395461L;

	/**
	 * Initializes the GUI.
	 */
	public MekaPanel() {
		super();
		initialize();
		initGUI();
		finishInit();
	}

	/**
	 * Initializes the members.
	 */
	protected void initialize() {
	}

	/**
	 * Initializes the widgets.
	 */
	protected void initGUI() {
		setLayout(new BorderLayout());
	}

	/**
	 * Finishes the initialization.
	 */
	protected void finishInit() {
	}

	/**
	 * Tries to determine the frame this panel is part of.
	 *
	 * @return		the parent frame if one exists or null if not
	 */
	public Frame getParentFrame() {
		return GUIHelper.getParentFrame(this);
	}

	/**
	 * Tries to determine the dialog this panel is part of.
	 *
	 * @return		the parent dialog if one exists or null if not
	 */
	public Dialog getParentDialog() {
		return GUIHelper.getParentDialog(this);
	}

	/**
	 * Closes the parent dialog/frame.
	 * Dispose the parent!
	 */
	public void closeParent() {
		Dialog		dialog;
		Frame			frame;

		if (getParentDialog() != null) {
			dialog = getParentDialog();
			dialog.setVisible(false);
			dialog.dispose();
		}
		else if (getParentFrame() != null) {
			frame = getParentFrame();
			frame.setVisible(false);
			frame.dispose();
		}
	}
}
