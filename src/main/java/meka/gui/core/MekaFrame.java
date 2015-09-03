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
 * MekaFrame.java
 * Copyright (C) 2008-2015 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import meka.core.OS;

import javax.swing.*;
import java.awt.*;

/**
 * A frame that loads the size and location from the props file automatically.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11583 $
 */
public class MekaFrame
		extends JFrame {

	/** for serialization. */
	private static final long serialVersionUID = -4853427519044621963L;

	/** the maximization fix listener. */
	protected MaximizationFixWindowListener m_MaximizationFixWindowListener;

	/**
	 * Initializes the frame with no title.
	 */
	public MekaFrame() {
		this("");
	}

	/**
	 * Initializes the frame with the given title.
	 *
	 * @param title	the title of the frame
	 */
	public MekaFrame(String title) {
		super(title);

		performInitialization();
	}

	/**
	 * Initializes the frame with no title.
	 *
	 * @param gc		the graphics configuration to use
	 */
	public MekaFrame(GraphicsConfiguration gc) {
		this("", gc);
	}

	/**
	 * Initializes the frame with the specified title.
	 *
	 * @param title	the title of the frame
	 * @param gc		the graphics configuration to use
	 */
	public MekaFrame(String title, GraphicsConfiguration gc) {
		super(title, gc);

		performInitialization();
	}

	/**
	 * Contains all the initialization steps to perform.
	 */
	protected void performInitialization() {
		initialize();
		initGUI();
		finishInit();
	}

	/**
	 * For initializing members.
	 */
	protected void initialize() {
		m_MaximizationFixWindowListener = new MaximizationFixWindowListener(
				this,
				GUIHelper.getProperties().getProperty("UseFrameMaximizationFix", "" + OS.isLinux()).equals("true"),
				Integer.parseInt(GUIHelper.getProperties().getProperty("FrameMaximizationFixDelay", "200")));
	}

	/**
	 * For initializing the GUI.
	 */
	protected void initGUI() {
		if (GUIHelper.getLogoIcon() != null)
			setIconImage(GUIHelper.getLogoIcon().getImage());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		addWindowStateListener(m_MaximizationFixWindowListener);
	}

	/**
	 * Updates the bounds of the window.
	 *
	 * @param x		the new x of the frame
	 * @param y		the new y of the frame
	 * @param width	the new width of the frame
	 * @param height	the new height of the frame
	 */
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (m_MaximizationFixWindowListener != null)
			m_MaximizationFixWindowListener.updateBounds(x, y, width, height);
		super.setBounds(x, y, width, height);
	}

	/**
	 * finishes the initialization, by setting size/location.
	 */
	protected void finishInit() {
	}

	/**
	 * Hook method just before the dialog is made visible.
	 */
	protected void beforeShow() {
	}

	/**
	 * Hook method just after the dialog was made visible.
	 */
	protected void afterShow() {
	}

	/**
	 * Hook method just before the dialog is hidden.
	 */
	protected void beforeHide() {
	}

	/**
	 * Hook method just after the dialog was hidden.
	 */
	protected void afterHide() {
	}

	/**
	 * closes/shows the dialog.
	 *
	 * @param value	if true then display the dialog, otherwise close it
	 */
	@Override
	public void setVisible(boolean value) {
		if (value)
			beforeShow();
		else
			beforeHide();

		super.setVisible(value);

		if (value)
			afterShow();
		else
			afterHide();
	}
}
