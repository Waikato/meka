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
 * RecentFilesHandlerWithCommandline.java
 * Copyright (C) 2013-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import meka.core.FileUtils;
import weka.core.Utils;

import java.io.File;
import java.util.HashSet;

/**
 * Recent files handler that stores a commandline alongside the file.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class RecentFilesHandlerWithCommandline<M>
		extends AbstractRecentItemsHandler<M, RecentFilesHandlerWithCommandline.Setup> {

	/** for serialization. */
	private static final long serialVersionUID = -8311791192535405075L;

	/**
	 * Container class for storing file and optionhandler alongside.
	 * Format: file TAB commandline
	 *
	 * @author  fracpete (fracpete at waikato dot ac dot nz)
	 * @version $Revision$
	 */
	public static class Setup {

		/** the file. */
		protected File m_File = null;

		/** the object. */
		protected Object m_Handler = null;

		/**
		 * Initializes the setup container.
		 *
		 * @param file	the file to store
		 * @param handler	the object to store
		 */
		public Setup(File file, Object handler) {
			m_File    = file;
			m_Handler = handler;
		}

		/**
		 * Initializes the setup container using the string.
		 *
		 * @param s		the string to use
		 */
		public Setup(String s) {
			String[]	parts;
			String[]	options;
			String		cname;

					parts = s.split("\t");
			if (parts.length != 2)
				return;

			m_File = new File(parts[0]);

			try {
				options    = Utils.splitOptions(parts[1]);
				cname      = options[0];
				options[0] = "";
				m_Handler  = Utils.forName(Object.class, cname, options);
			}
			catch (Exception e) {
				return;
			}
		}

		/**
		 * Returns the file.
		 *
		 * @return		the file
		 */
		public File getFile() {
			return m_File;
		}

		/**
		 * Returns the handler.
		 *
		 * @return		the handler
		 */
		public Object getHandler() {
			return m_Handler;
		}

		/**
		 * Performs a check whether this setup is valid.
		 *
		 * @return		true if valid setup
		 */
		public boolean check() {
			return (m_File != null) && (m_Handler != null) && m_File.exists();
		}

		/**
		 * Returns the container setup as string.
		 *
		 * @return		the string
		 */
		@Override
		public String toString() {
			if (m_Handler != null)
				return m_File.getAbsolutePath() + "\t" + Utils.toCommandLine(m_Handler);
			else
				return m_File.getAbsolutePath() + "\t";
		}

		/**
		 * Returns the hashCode of the file's absolute path.
		 *
		 * @return		the hashcode, -1 if no file set
		 */
		@Override
		public int hashCode() {
			if (m_File != null)
				return m_File.getAbsolutePath().hashCode();
			else
				return -1;
		}

		/**
		 * Returns true if the other object is also a {@link Setup} instance and
		 * contains the same file.
		 *
		 * @param obj	the other object to compare with
		 * @return		true if exactly the same
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Setup)
				return (obj.hashCode() == hashCode());
			else
				return false;
		}
	}

	/** the property for storing the number of recent files. */
	public final static String RECENTFILES_COUNT = "RecentFilesCount";

	/** the property prefix for a recent file. */
	public final static String RECENTFILES_PREFIX = "RecentFile.";

	/** the minimum number of parent directories to use. */
	protected int m_MinNumParentDirs;

	/**
	 * Initializes the handler with a maximum of 5 items.
	 *
	 * @param propsFile	the props file to store the files in
	 * @param menu	the menu to add the recent files as subitems to
	 */
	public RecentFilesHandlerWithCommandline(String propsFile, M menu) {
		super(propsFile, menu);
	}

	/**
	 * Initializes the handler.
	 *
	 * @param propsFile	the props file to store the files in
	 * @param maxCount	the maximum number of files to keep in menu
	 * @param menu	the menu to add the recent files as subitems to
	 */
	public RecentFilesHandlerWithCommandline(String propsFile, int maxCount, M menu) {
		super(propsFile, maxCount, menu);
	}

	/**
	 * Initializes the handler.
	 *
	 * @param propsFile	the props file to store the files in
	 * @param propPrefix	the properties prefix, use null to ignore
	 * @param maxCount	the maximum number of files to keep in menu
	 * @param menu	the menu to add the recent files as subitems to
	 */
	public RecentFilesHandlerWithCommandline(String propsFile, String propPrefix, int maxCount, M menu) {
		super(propsFile, propPrefix, maxCount, menu);
	}

	/**
	 * Checks the item after obtaining from the props file.
	 * <br><br>
	 * File must exist and handler not null.
	 *
	 * @param item	the item to check
	 * @return		true if checks passed
	 * @see		Setup#check()
	 */
	@Override
	protected boolean check(Setup item) {
		return item.check();
	}

	/**
	 * Determines the minimum number of parent directories that need to be
	 * included in the filename to make the filenames in the menu distinguishable.
	 *
	 * @return		the minimum number of parent directories, -1 means
	 * 			full path
	 */
	protected synchronized int determineMinimumNumberOfParentDirs() {
		int			result;
		HashSet<String>	files;
		int			num;
		int			i;
		int			max;

		result = -1;

		max = 0;
		for (i = 0; i < m_RecentItems.size(); i++)
			max = Math.max(max, FileUtils.getDirectoryDepth(m_RecentItems.get(i).getFile()));

		num = 0;
		do {
			files = new HashSet<String>();
			for (i = 0; i < m_RecentItems.size(); i++)
				files.add(FileUtils.createPartialFilename(m_RecentItems.get(i).getFile(), num));
			if (files.size() == m_RecentItems.size())
				result = num;
			else
				num++;
		}
		while ((files.size() < m_RecentItems.size()) && (num <= max));

		return result;
	}

	/**
	 * Returns the key to use for the counts in the props file.
	 *
	 * @return		the key
	 */
	@Override
	protected String getCountKey() {
		return RECENTFILES_COUNT;
	}

	/**
	 * Returns the key prefix to use for the items in the props file.
	 *
	 * @return		the prefix
	 */
	@Override
	protected String getItemPrefix() {
		return RECENTFILES_PREFIX;
	}

	/**
	 * Turns an object into a string for storing in the props.
	 *
	 * @param obj		the object to convert
	 * @return		the string representation
	 */
	@Override
	protected String toString(Setup obj) {
		return obj.toString();
	}

	/**
	 * Turns the string obtained from the props into an object again.
	 *
	 * @param s		the string representation
	 * @return		the parsed object
	 */
	@Override
	protected Setup fromString(String s) {
		return new Setup(s);
	}

	/**
	 * Hook method which gets executed just before updating the menu.
	 */
	@Override
	protected void preUpdateMenu() {
		super.preUpdateMenu();

		m_MinNumParentDirs = determineMinimumNumberOfParentDirs();
	}

	/**
	 * Generates the text for the menuitem.
	 *
	 * @param index	the index of the item
	 * @param item	the item itself
	 * @return		the generated text
	 */
	@Override
	protected String createMenuItemText(int index, Setup item) {
		return FileUtils.createPartialFilename(item.getFile(), m_MinNumParentDirs);
	}
}
