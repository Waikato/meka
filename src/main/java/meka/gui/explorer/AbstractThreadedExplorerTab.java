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
 * AbstractThreadedExplorerTab.java
 * Copyright (C) 2012-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import javax.swing.SwingUtilities;

/**
 * Supports long-running tasks in a separate thread.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractThreadedExplorerTab
	extends AbstractExplorerTab {
	
	/** for serialization. */
	private static final long serialVersionUID = -1827939348802826100L;
	
	/**
	 * For execution the long-running process.
	 * 
	 * @author  fracpete (fracpete at waikato dot ac dot nz)
	 * @version $Revision$
	 */
	public static class WorkerThread
	  extends Thread {
		
		/** the tab that this thread belongs to. */
		protected AbstractThreadedExplorerTab m_Owner;
		
		/**
		 * Initializes the thread.
		 * 
		 * @param owner the tab this thread belongs to
		 * @param run the code to execute
		 */
		public WorkerThread(AbstractThreadedExplorerTab owner, Runnable run) {
			super(run);
			m_Owner = owner;
		}
		
		/**
		 * Returns the tab this thread belongs to.
		 * 
		 * @return the owner
		 */
		public AbstractThreadedExplorerTab getOwner() {
			return m_Owner;
		}
		
		@Override
		public void run() {
			try {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						m_Owner.executionStarted();
					}
				};
				SwingUtilities.invokeLater(r);
				super.run();
			}
			catch (final Throwable t) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
					  if (t instanceof ThreadDeath)
						m_Owner.executionFinished(null);
					  else
						m_Owner.executionFinished(t);
						m_Owner.executionFinalized();
					}
				};
				SwingUtilities.invokeLater(r);
				return;
			}

			Runnable r = new Runnable() {
				@Override
				public void run() {
					m_Owner.executionFinished(null);
					m_Owner.executionFinalized();
				}
			};
			SwingUtilities.invokeLater(r);
		}
	}
	
	/** the thread for running tasks. */
	protected WorkerThread m_Task;

	/**
	 * Initializes the members.
	 */
	@Override
	protected void initialize() {
		super.initialize();
		
		m_Task = null;
	}
	
	/**
	 * Checks whether a task is currently running.
	 */
	public boolean isRunning() {
		return (m_Task != null);
	}
	
	/**
	 * Starts the task.
	 * 
	 * @param run the code to execute
	 */
	public void start(Runnable run) {
		m_Task = new WorkerThread(this, run);
		m_Task.start();
	}
	
	/**
	 * Stops the execution.
	 */
	public void stop() {
		if (m_Task != null) {
		    m_Task.stop();
		}
	}
	
	/**
	 * Gets called when the thread starts.
	 */
	protected abstract void executionStarted();
	
	/**
	 * Gets called when the thread finishes or gets stopped.
	 * 
	 * @param t if the execution generated an exception, null if no errors
	 */
	protected abstract void executionFinished(Throwable t);
	
	/**
	 * Gets called when the thread has finished completely.
	 */
	protected void executionFinalized() {
		m_Task = null;
	}
}
