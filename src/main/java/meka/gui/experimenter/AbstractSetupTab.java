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
 * AbstractSetupTab.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.experimenter;

import meka.experiment.Experiment;
import meka.experiment.events.ExecutionStageEvent;
import meka.gui.goe.GenericObjectEditor;
import meka.gui.goe.GenericObjectEditorDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Ancestor for tabs that setup experiments.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractSetupTab
  extends AbstractExperimenterTab {

	private static final long serialVersionUID = 3556506064253273853L;

	/** the message that experiment cannot be handled. */
	protected JPanel m_PanelUnsupported;

	/** the panel for the setup. */
	protected JPanel m_PanelSetup;

	/** the button for applying the setup. */
	protected JButton m_ButtonApply;

	/** the button for reverting the setup. */
	protected JButton m_ButtonRevert;

	/**
	 * Initializes the widgets.
	 */
	@Override
	protected void initGUI() {
		JPanel      panel;
		JLabel      label;

		super.initGUI();

		m_PanelUnsupported = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label = new JLabel("This experiment is not supported by this tab!");
		label.setForeground(Color.RED.darker());
		m_PanelUnsupported.add(label);
		m_PanelUnsupported.setVisible(false);
		add(m_PanelUnsupported, BorderLayout.NORTH);

		m_PanelSetup = new JPanel(new BorderLayout());
		add(m_PanelSetup, BorderLayout.CENTER);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(panel, BorderLayout.SOUTH);

		m_ButtonApply = new JButton("Apply");
		m_ButtonApply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});
		panel.add(m_ButtonApply);

		m_ButtonRevert = new JButton("Revert");
		m_ButtonRevert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				revert();
			}
		});
		panel.add(m_ButtonRevert);
	}

	/**
	 * Finishes the GUI setup.
	 */
	@Override
	protected void finishInit() {
		super.finishInit();
		clear();
		updateButtons();
	}

	/**
	 * Sets the preferred size of the panel.
	 *
	 * @param panel         the panel to update
	 * @return              the updated panel
	 */
	protected JPanel setPreferredSize(JPanel panel) {
		panel.setPreferredSize(new Dimension(600, 25));
		return panel;
	}

	/**
	 * Updates the enabled state of the buttons.
	 */
	protected void updateButtons() {
		boolean     present;
		boolean     running;
		boolean     editable;
		boolean     handled;

		present  = (m_Experiment != null);
		running  = present && m_Experiment.isRunning();
		handled  = !m_PanelUnsupported.isVisible();
		editable = present && handled && !running;

		// actions
		m_ButtonApply.setEnabled(editable);
		m_ButtonRevert.setEnabled(editable);
	}

	/**
	 * Returns the title of the tab.
	 *
	 * @return the title
	 */
	@Override
	public String getTitle() {
		return "Setup (expert)";
	}

	/**
	 * Returns a GOE dialog.
	 *
	 * @param cls       the class hierarchy
	 * @param value     the current value
	 * @return          the dialog
	 */
	protected GenericObjectEditorDialog getGOEDialog(Class cls, Object value) {
		GenericObjectEditorDialog result;

		if (getParentDialog() != null)
			result = new GenericObjectEditorDialog(getParentDialog(), Dialog.ModalityType.DOCUMENT_MODAL);
		else
			result = new GenericObjectEditorDialog(getParentFrame(), true);
		result.setDefaultCloseOperation(GenericObjectEditorDialog.DISPOSE_ON_CLOSE);
		result.setEditor(new GenericObjectEditor(true));
		result.getGOEEditor().setClassType(cls);
		result.getGOEEditor().setValue(value);

		return result;
	}

	/**
	 * Resets the interface.
	 */
	protected abstract void clear();

	/**
	 * Checks whether this type of experiment is handled by this tab.
	 *
	 * @param exp       the experiment to check
	 * @return          true if handled
	 */
	protected abstract boolean handlesExperiment(Experiment exp);

	/**
	 * Maps the experiment onto the parameters.
	 */
	protected abstract void fromExperiment();

	/**
	 * Stores the parameters in an experiment.
	 *
	 * @return          the generated experiment
	 */
	protected abstract Experiment toExperiment();

	/**
	 * Applies the settings to the experiment.
	 */
	protected void apply() {
		m_Experiment = toExperiment();
		getOwner().notifyTabsExperimentChanged(this, m_Experiment);
	}

	/**
	 * Reverts the settings to the experiment ones.
	 */
	protected void revert() {
		fromExperiment();
	}

	/**
	 * Gets called when the experiment changed.
	 */
	@Override
	protected void update() {
		super.update();

		if (m_Experiment == null) {
			clear();
		}
		else {
			if (handlesExperiment(m_Experiment)) {
				m_PanelUnsupported.setVisible(false);
				clear();
				fromExperiment();
			} else {
				m_PanelUnsupported.setVisible(true);
				clear();
			}
		}
		updateButtons();
	}

	/**
	 * Gets called when the experiment enters a new stage.
	 *
	 * @param e         the event
	 */
	public void experimentStage(ExecutionStageEvent e) {
		super.experimentStage(e);
		updateButtons();
	}
}
