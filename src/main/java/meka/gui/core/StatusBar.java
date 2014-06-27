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
 * StatusBar.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Statusbar for displaying short notifications and an animated icon, 
 * e.g., when busy doing calculations.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class StatusBar
	extends MekaPanel {

	/** for serialization. */
	private static final long serialVersionUID = 2942070600827649700L;

	/** the runnable for the animation. */
	public static class Animation
	  	implements Runnable {
		
		/** the JLabel used for displaying the icon. */
		protected JLabel m_LabelIcon;
		
		/** whether the animation is still running. */
		protected boolean m_Running;
		
		/** the delay between changing the icon in msec. */
		protected int m_Delay;
		
		/**
		 * Initializes the runnable.
		 * 
		 * @param labelIcon	the label to use for displaying the icon
		 * @param delay		the delay between icon changes in msec
		 */
		public Animation(JLabel labelIcon, int delay) {
			super();
			m_LabelIcon = labelIcon;
			m_Delay     = delay;
		}
		
		/**
		 * Shows the specified
		 */
		protected void show(final int index) {
			Runnable run = new Runnable() {
				@Override
				public void run() {
					m_LabelIcon.setIcon(GUIHelper.getIcon("MEKA_icon_animated" + index + ".png"));
				}
			};
			SwingUtilities.invokeLater(run);
		}
		
		/**
		 * Performs the animation.
		 */
		@Override
		public void run() {
			int index = 1;
			m_Running = true;
			while (m_Running) {
				index++;
				try {
					synchronized(this) {
						wait(m_Delay);
					}
					show(index);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				if (index == 4)
					index = 0;
			}
			show(1);
		}
		
		/**
		 * Stops the animation.
		 */
		public void stopAnimation() {
			m_Running = false;
		}
	}
	
	/** for displaying the status text. */
	protected JLabel m_LabelText;

	/** the panel for the text. */
	protected JPanel m_PanelText;

	/** the panel for the icon. */
	protected JPanel m_PanelIcon;
	
	/** for displaying the icon. */
	protected JLabel m_LabelIcon;

	/** the runnable for animating the icon. */
	protected Animation m_Animation;
	
	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		super.initGUI();

		setLayout(new BorderLayout());

		m_PanelText = new JPanel(new FlowLayout(FlowLayout.LEFT));
		m_LabelText = new JLabel(" ");
		m_PanelText.add(m_LabelText);
		m_PanelText.setBorder(BorderFactory.createLoweredBevelBorder());
		add(m_PanelText, BorderLayout.CENTER);

		m_PanelIcon = new JPanel(new BorderLayout());
		m_LabelIcon = new JLabel(GUIHelper.getIcon("MEKA_icon_animated1.png"));
		m_PanelIcon.add(m_LabelIcon, BorderLayout.CENTER);
		m_PanelIcon.setBorder(BorderFactory.createLoweredBevelBorder());
		add(m_PanelIcon, BorderLayout.EAST);
		
		m_PanelIcon.setMinimumSize(new Dimension(22, 22));
	}

	/**
	 * Displays the specified status message.
	 * 
	 * @param msg		the message to display
	 */
	public void showStatus(String msg) {
		if ((msg == null) || msg.isEmpty())
			msg = " ";
		m_LabelText.setText(msg);
	}

	/**
	 * Clears status message.
	 */
	public void clearStatus() {
		showStatus("");
	}

	/**
	 * 
	 */
	protected Runnable createAnimation() {
		return new Runnable() {
			@Override
			public void run() {
			}
		};
	}
	
	/**
	 * Starts the animated icon, without setting status message.
	 */
	public void startBusy() {
		if (m_Animation != null)
			return;
		m_Animation = new Animation(m_LabelIcon, 1000);
		new Thread(m_Animation).start();
	}

	/**
	 * Starts the animated icon, setting the specified status message.
	 * 
	 * @param msg		the message to display
	 */
	public void startBusy(String msg) {
		showStatus(msg);
		startBusy();
	}

	/**
	 * Stops the animated icon, without setting status message.
	 */
	public void finishBusy() {
		if (m_Animation == null)
			return;
		m_Animation.stopAnimation();
		m_Animation = null;
	}

	/**
	 * Stops the animated icon, setting the specified status message.
	 * 
	 * @param msg		the message to display
	 */
	public void finishBusy(String msg) {
		showStatus(msg);
		finishBusy();
	}
}
