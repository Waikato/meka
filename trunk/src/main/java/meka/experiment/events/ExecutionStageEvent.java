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
 * ExecutionStageEvent.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment.events;

import meka.experiment.Experiment;

import java.util.EventObject;

/**
 * Event that gets sent by an experiment when it enters a new stage in the execution.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExecutionStageEvent
  extends EventObject {

	private static final long serialVersionUID = 7732581989591408787L;

	/**
	 * The stages of an experiment.
	 *
	 * @author FracPete (fracpete at waikato dot ac dot nz)
	 * @version $Revision$
	 */
	public enum Stage {
		INITIALIZE,
		RUN,
		FINISH
	}

	/** the stage. */
	protected Stage m_Stage;

	/**
	 * Gets called when the experiment enters a new stage.
	 *
	 * @param source        the experiment that triggered the event
	 * @param stage         the stage
	 */
	public ExecutionStageEvent(Experiment source, Stage stage) {
		super(source);

		m_Stage = stage;
	}

	/**
	 * Returns the associated experiment.
	 *
	 * @return      the experiment
	 */
	public Experiment getExperiment() {
	  return (Experiment) getSource();
	}

	/**
	 * Returns the stage.
	 *
	 * @return      the stage
	 */
	public Stage getStage() {
		return m_Stage;
	}
}
