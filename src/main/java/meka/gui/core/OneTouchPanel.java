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
 * OneTouchPanel.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package meka.gui.core;

import com.googlecode.jfilechooserbookmarks.gui.BasePanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel that can hide its content and make it visible using "arrow" buttons.
 * The actual content gets added the panel accessible via {@link #getContentPanel()}.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class OneTouchPanel
	extends BasePanel {

	private static final long serialVersionUID = 1075582220201234307L;

	/**
	 * The location for the buttons when content is hidden.
	 *
	 * @author FracPete (fracpete at waikato dot ac dot nz)
	 * @version $Revision$
	 */
	public enum Location {
		TOP,
		BOTTOM,
		LEFT,
		RIGHT
	}

	/** the location of the button when hidden. */
	protected Location m_Location;

	/** the icon when the content is visible. */
	protected String m_IconVisible;

	/** the icon when the content is hidden. */
	protected String m_IconHidden;

	/** the tooltip when open. */
	protected String m_ToolTipVisible;

	/** the tooltip when hidden. */
	protected String m_ToolTipHidden;

	/** the panel for adding the actual content. */
	protected BasePanel m_PanelContent;

	/** the button for hiding/showing the content. */
	protected JButton m_ButtonVisibility;

	/**
	 * Initializes the panel.
	 *
	 * @param location	the location of the button when content is hidden
	 */
	public OneTouchPanel(Location location) {
		super();
		m_Location = location;
		initialize();
		initGUI();
		finishInit();
	}

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		if (m_Location == null)
			return;

		super.initialize();

		m_ToolTipVisible = null;
		m_ToolTipHidden  = null;

		switch (m_Location) {
			case TOP:
				m_IconVisible = "arrow-head-up.png";
				m_IconHidden  = "arrow-head-down.png";
				break;
			case BOTTOM:
				m_IconVisible = "arrow-head-down.png";
				m_IconHidden  = "arrow-head-up.png";
				break;
			case LEFT:
				m_IconVisible = "arrow-head-left.png";
				m_IconHidden  = "arrow-head-right.png";
				break;
			case RIGHT:
				m_IconVisible = "arrow-head-right.png";
				m_IconHidden  = "arrow-head-left.png";
				break;
			default:
				throw new IllegalStateException("Unhandled location: " + m_Location);
		}
	}

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initGUI() {
		JPanel	panel;

		if (m_Location == null)
			return;

		super.initGUI();

		setLayout(new BorderLayout());

		m_PanelContent = new BasePanel(new BorderLayout());
		add(m_PanelContent, BorderLayout.CENTER);

		m_ButtonVisibility = new JButton(GUIHelper.getIcon(m_IconVisible));
		m_ButtonVisibility.setBorder(BorderFactory.createEmptyBorder());
		m_ButtonVisibility.setPreferredSize(new Dimension(18, 18));
		m_ButtonVisibility.setBorderPainted(false);
		m_ButtonVisibility.setContentAreaFilled(false);
		m_ButtonVisibility.setFocusPainted(false);
		m_ButtonVisibility.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setContentVisible(!isContentVisible());
			}
		});

		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
		panel.add(m_ButtonVisibility);
		switch (m_Location) {
			case TOP:
				m_PanelContent.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
				add(panel, BorderLayout.NORTH);
				break;
			case BOTTOM:
				m_PanelContent.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
				add(panel, BorderLayout.SOUTH);
				break;
			case LEFT:
				m_PanelContent.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
				add(panel, BorderLayout.WEST);
				break;
			case RIGHT:
				m_PanelContent.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
				add(panel, BorderLayout.EAST);
				break;
			default:
				throw new IllegalStateException("Unhandled location: " + m_Location);
		}
	}

	/**
	 * Finalizes the initialization.
	 */
	@Override
	protected void finishInit() {
		if (m_Location == null)
			return;

		super.finishInit();

		updateToolTip();
	}

	/**
	 * The panel for adding the actual content.
	 *
	 * @return		the panel
	 */
	public BasePanel getContentPanel() {
		return m_PanelContent;
	}

	/**
	 * Sets whether to show the content or hide it.
	 *
	 * @param value	true if to show the content, false to hide it
	 */
	public void setContentVisible(boolean value) {
		if (value)
			m_ButtonVisibility.setIcon(GUIHelper.getIcon(m_IconVisible));
		else
			m_ButtonVisibility.setIcon(GUIHelper.getIcon(m_IconHidden));
		m_PanelContent.setVisible(value);
		updateToolTip();
	}

	/**
	 * Returns whether the content is visible.
	 *
	 * @return		true if visible
	 */
	public boolean isContentVisible() {
		return m_PanelContent.isVisible();
	}

	/**
	 * Updates the tooltip.
	 */
	protected void updateToolTip() {
		if (isContentVisible())
			m_ButtonVisibility.setToolTipText(m_ToolTipVisible);
		else
			m_ButtonVisibility.setToolTipText(m_ToolTipHidden);
	}

	/**
	 * Sets the button tooltip to use when the content is visible.
	 *
	 * @param value	the tooltip, null to turn off tooltip
	 */
	public void setToolTipVisible(String value) {
		m_ToolTipVisible = value;
		updateToolTip();
	}

	/**
	 * Returns the button tooltip in use when the content is visible.
	 *
	 * @return		the tooltip, null if turned off
	 */
	public String getToolTipVisible() {
		return m_ToolTipVisible;
	}

	/**
	 * Sets the button tooltip to use when the content is hidden.
	 *
	 * @param value	the tooltip, null to turn off tooltip
	 */
	public void setToolTipHidden(String value) {
		m_ToolTipHidden = value;
		updateToolTip();
	}

	/**
	 * Returns the button tooltip in use when the content is hidden.
	 *
	 * @return		the tooltip, null if turned off
	 */
	public String getToolTipHidden() {
		return m_ToolTipHidden;
	}
}
