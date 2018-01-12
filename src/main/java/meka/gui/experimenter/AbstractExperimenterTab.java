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
 * AbstractExperimenterTab.java
 * Copyright (C) 2015-2018 University of Waikato, Hamilton, NZ
 */

package meka.gui.experimenter;

import meka.experiment.Experiment;
import meka.experiment.events.ExecutionStageEvent;
import meka.experiment.events.ExecutionStageListener;
import meka.gui.core.MekaPanel;
import weka.core.PluginManager;

import javax.swing.JMenu;
import java.util.List;

/**
 * Ancestor for experimenter tabs.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractExperimenterTab
		extends MekaPanel
		implements ExecutionStageListener {

	private static final long serialVersionUID = -2307399042422053536L;

	/** the owning experimenter. */
	protected Experimenter m_Owner;
	
	/** the current experiment. */
	protected Experiment m_Experiment;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();
		
		m_Experiment = null;
	}		

	/**
	 * Sets the Experimenter instance this tab belongs to.
	 *
	 * @param value the Experimenter instance
	 */
	public void setOwner(Experimenter value) {
		m_Owner = value;
	}

	/**
	 * Returns the Experimenter instance this tab belongs to.
	 * 
	 * @return the Experimenter instance
	 */
	public Experimenter getOwner() {
		return m_Owner;
	}
	
	/**
	 * Returns the title of the tab.
	 * 
	 * @return the title
	 */
	public abstract String getTitle();

	/**
	 * Returns an optional menu to be added to the Experimenter menu.
	 * <br>
	 * Default implementation returns null.
	 *
	 * @return the menu
	 */
	public JMenu getMenu() {
		return null;
	}

	/**
	 * Returns whether experiment is currently present.
	 * 
	 * @return true if experiment is loaded
	 */
	public boolean hasExperiment() {
		return (m_Experiment != null);
	}
	
	/**
	 * Returns the current experiment.
	 * 
	 * @return the experiment, can be null if none set
	 */
	public Experiment getExperiment() {
		return m_Experiment;
	}
	
	/**
	 * Sets the experiment to use.
	 * 
	 * @param value the experiment to use
	 */
	public void setExperiment(Experiment value) {
		m_Experiment = value;
		update();
	}
	
	/**
	 * Gets called when the experiment changed.
	 * <p>
	 * Default implementation does nothing.
	 */
	protected void update() {
	}

	/**
	 * Displays the specified status message.
	 *
	 * @param msg		the message to display
	 */
	public void showStatus(String msg) {
		m_Owner.getStatusBar().showStatus(msg);
	}

	/**
	 * Clears status message.
	 */
	public void clearStatus() {
		m_Owner.getStatusBar().clearStatus();
	}

	/**
	 * Starts the animated icon, without setting status message.
	 */
	public void startBusy() {
		m_Owner.getStatusBar().startBusy();
	}

	/**
	 * Starts the animated icon, setting the specified status message.
	 *
	 * @param msg		the message to display
	 */
	public void startBusy(String msg) {
		log(msg);
		m_Owner.getStatusBar().startBusy(msg);
	}

	/**
	 * Stops the animated icon, without setting status message.
	 */
	public void finishBusy() {
		log("Finished");
		m_Owner.getStatusBar().finishBusy("");
	}

	/**
	 * Stops the animated icon, setting the specified status message.
	 *
	 * @param msg		the message to display
	 */
	public void finishBusy(String msg) {
		log(msg);
		m_Owner.getStatusBar().finishBusy(msg);
	}

	/**
	 * Returns all the available tabs.
	 *
	 * @return          the classnames of the tabs
	 */
	public static List<String> getTabs() {
		return PluginManager.getPluginNamesOfTypeList(AbstractExperimenterTab.class.getName());
	}

	/**
	 * For logging messages. Uses stderr if no listeners defined.
	 *
	 * @param msg       the message to output
	 */
	protected synchronized void log(String msg) {
		m_Owner.log(this, msg);
	}

	/**
	 * Logs the stacktrace along with the message on the log tab and returns a
	 * combination of both of them as string.
	 *
	 * @param msg		the message for the exception
	 * @param t		the exception
	 * @return		the full error message (message + stacktrace)
	 */
	public String handleException(String msg, Throwable t) {
		return m_Owner.handleException(this, msg, t);
	}

	/**
	 * Gets called when the experiment enters a new stage.
	 * <br>
	 * Default implementation does nothing.
	 *
	 * @param e         the event
	 */
	public void experimentStage(ExecutionStageEvent e) {
	}
}
