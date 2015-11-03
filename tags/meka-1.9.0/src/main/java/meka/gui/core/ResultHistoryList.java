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
 * ResultHistoryList.java
 * Copyright (C) 2012-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import meka.core.Result;
import meka.gui.choosers.MekaFileChooser;
import weka.gui.ExtensionFileFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A specialized {@link JList} to display results.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ResultHistoryList
	extends JList {
	
	/** for serialization. */
	private static final long serialVersionUID = 8655627570641911582L;

	/**
	 * Model for results histories.
	 * 
	 * @author  fracpete (fracpete at waikato dot ac dot nz)
	 * @version $Revision$
	 */
	public static class ResultHistoryModel
	  extends AbstractListModel {

		/** for serialization. */
		private static final long serialVersionUID = -9179459138851904317L;
		
		/** the underlying history list. */
		protected ResultHistory m_History;
		
		/**
		 * Initializes the model with an empty history.
		 */
		public ResultHistoryModel() {
			this(new ResultHistory());
		}
		
		/**
		 * Initializes the model with the history.
		 * 
		 * @param history the history to use
		 */
		public ResultHistoryModel(ResultHistory history) {
			m_History = history;
		}

		/**
		 * Clears the history.
		 */
		public void clear() {
			int to;
			to = m_History.size();
			m_History.clear();
			fireIntervalRemoved(this, 0, to);
		}
		
		/**
		 * Returns the number of history entries.
		 * 
		 * @return the number of entries
		 */
		@Override
		public int getSize() {
			return m_History.size();
		}

		/**
		 * Adds the element to the history.
		 * 
		 * @param result the item to add
		 * @param payload the payload to add
		 * @param suffix the suffix to add
		 */
		public void addElement(Result result, Object payload, String suffix) {
			m_History.add(result, payload, suffix);
			fireIntervalAdded(this, m_History.size() - 1, m_History.size() - 1);
		}

		/**
		 * Returns the element at the specified location.
		 * 
		 * @param index the location
		 * @return the item
		 */
		@Override
		public Object getElementAt(int index) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String element = formatter.format(getTimestampAt(index));
			String suffix = getSuffixAt(index);
			if ((suffix != null) && (suffix.trim().length() > 0))
				element += ": " + suffix;
			return element;
		}

		/**
		 * Returns the element at the specified location.
		 * 
		 * @param index the location
		 * @return the item
		 */
		public Date getTimestampAt(int index) {
			return m_History.getTimestamp(index);
		}

		/**
		 * Returns the element at the specified location.
		 * 
		 * @param index the location
		 * @return the item
		 */
		public Result getResultAt(int index) {
			return m_History.get(index);
		}

		/**
		 * Returns the payload at the specified location.
		 *
		 * @param index the location
		 * @return the payload
		 */
		public Object getPayloadAt(int index) {
			return m_History.getPayload(index);
		}

		/**
		 * Returns the suffix at the specified location.
		 *
		 * @param index the location
		 * @return the suffix
		 */
		public String getSuffixAt(int index) {
			return m_History.getSuffix(index);
		}

		/**
		 * Removes the element at the specified location.
		 * 
		 * @param index the location
		 * @return the removed item
		 */
		public Result removeElementAt(int index) {
			Result result;
			result = m_History.remove(index);
			fireIntervalRemoved(this, index, index);
			return result;
		}
		
		/**
		 * Returns the underlying history.
		 * 
		 * @return the history
		 */
		public ResultHistory getHistory() {
			return m_History;
		}
	}

	/**
	 * The interface for classes that can customize the popup menu of the
	 * result history.
	 *
	 * @author  fracpete (fracpete at waikato dot ac dot nz)
	 * @version $Revision$
	 */
	public interface ResultHistoryPopupMenuCustomizer {

		/**
		 * Allows to customize the popup menu for the result history.
		 *
		 * @param history the list this popup menu is for
		 * @param index the index of the select item from the history
		 * @param menu the menu to customize
		 */
		public void customizePopupMenu(ResultHistoryList history, int index, JPopupMenu menu);
	}
	
	/** the file chooser for saving the output. */
	protected MekaFileChooser m_FileChooser;

	/** the popup menu customizer. */
	protected ResultHistoryPopupMenuCustomizer m_PopupMenuCustomizer;

	/**
	 * Initializes the list.
	 */
	public ResultHistoryList() {
		super(new ResultHistoryModel());
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (MouseUtils.isRightClick(e)) {
					JPopupMenu menu = createPopup(e);
					if (menu != null)
						menu.show(ResultHistoryList.this, e.getX(), e.getY());
				} else {
					super.mouseClicked(e);
				}
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				int[] indices = getSelectedIndices();
				if ((indices.length > 0) && (e.getKeyCode() == KeyEvent.VK_DELETE)) {
					for (int i = indices.length - 1; i >= 0; i--)
						removeItem(indices[i]);
					e.consume();
				}
				else {
					super.keyTyped(e);
				}
			}
		});

		m_FileChooser = GUIHelper.newFileChooser();
		ExtensionFileFilter filter = new ExtensionFileFilter("txt", "Meka results format (*.txt)");
		m_FileChooser.addChoosableFileFilter(filter);
		m_FileChooser.setFileFilter(filter);
		m_FileChooser.setMultiSelectionEnabled(false);

		m_PopupMenuCustomizer = null;
	}

	/**
	 * Sets the customizer to use for the popup menu.
	 *
	 * @param value the customizer, null to unset
	 */
	public void setPopupMenuCustomizer(ResultHistoryPopupMenuCustomizer value) {
		m_PopupMenuCustomizer = value;
	}

	/**
	 * Returns the current customizer for the popup menu.
	 *
	 * @return the customizer, null if none set
	 */
	public ResultHistoryPopupMenuCustomizer getPopupMenuCustomizer() {
		return m_PopupMenuCustomizer;
	}
	
	/**
	 * Creates a popup menu for the selected item.
	 * 
	 * @param e the event that triggered the method
	 * @return null if no popup available
	 */
	protected JPopupMenu createPopup(MouseEvent e) {
		JPopupMenu result;
		final int index;
		Rectangle rect;
		JMenuItem menuitem;
		
		result = null;
		
		index = locationToIndex(e.getPoint());
		if (index == -1)
			return null;

		rect = getCellBounds(index, index);
		if (!rect.contains(e.getPoint()))
			return null;
		
		result = new JPopupMenu();
		
		menuitem = new JMenuItem("Save...");
		menuitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save(index);
			}
		});
		result.add(menuitem);

		result.addSeparator();
		
		menuitem = new JMenuItem("Remove");
		menuitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeItem(index);
			}
		});
		result.add(menuitem);
		
		menuitem = new JMenuItem("Remove all");
		menuitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((ResultHistoryModel) getModel()).clear();
			}
		});
		result.add(menuitem);

		// customize popup menu?
		if (m_PopupMenuCustomizer != null)
			m_PopupMenuCustomizer.customizePopupMenu(this, index, result);

		return result;
	}
	
	/**
	 * Brings up a dialog to save the specified item to a file.
	 * 
	 * @param index the index of the item to save
	 * @return true if successfully saved
	 */
	protected boolean save(int index) {
		boolean result;
		int retVal;
		Result res;
		File file;
		
		retVal = m_FileChooser.showSaveDialog(this);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return false;
		
		file = m_FileChooser.getSelectedFile();
		res  = getResultAt(index);
		try {
			Result.writeResultToFile(res, file.getAbsolutePath());
			result = true;
		}
		catch (Exception e) {
			result = false;
			System.err.println("Failed to write result to file '" + file + "':");
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					this, 
					"Failed to write result to file '" + file + "':\n" + e, 
					"Error saving",
					JOptionPane.ERROR_MESSAGE);
		}
		
		return result;
	}
	
	/**
	 * Removes the item at the specified location.
	 * 
	 * @param index the location to remove
	 */
	protected void removeItem(int index) {
		((ResultHistoryModel) getModel()).removeElementAt(index);
		if (index >= getModel().getSize())
			index--;
		if (index >= 0)
			setSelectedIndex(index);
	}
	
	/**
	 * Returns the timestamp at the specified location.
	 * 
	 * @param index the location to return
	 * @return the timestamp
	 */
	protected Date getTimestampAt(int index) {
		return  (Date) ((ResultHistoryModel) getModel()).getElementAt(index);
	}

	/**
	 * Adds the element to the history.
	 * 
	 * @param result the item to add
	 * @param payload the payload to add
	 * @param suffix the suffix to add
	 */
	public void addResult(Result result, Object payload, String suffix) {
		((ResultHistoryModel) getModel()).addElement(result, payload, suffix);
		setSelectedIndex(getModel().getSize() - 1);
	}

	/**
	 * Returns the result at the specified location.
	 * 
	 * @param index the location
	 * @return the result
	 */
	public Result getResultAt(int index) {
		return ((ResultHistoryModel) getModel()).getResultAt(index);

	}

	/**
	 * Returns the payload at the specified location.
	 *
	 * @param index the location
	 * @return the payload
	 */
	public Object getPayloadAt(int index) {
		return ((ResultHistoryModel) getModel()).getPayloadAt(index);

	}

	/**
	 * Returns the suffix at the specified location.
	 *
	 * @param index the location
	 * @return the suffix
	 */
	public String getSuffixAt(int index) {
		return ((ResultHistoryModel) getModel()).getSuffixAt(index);

	}

	/**
	 * Sets the model to use, must derived from {@link ResultHistoryModel}.
	 * 
	 * @param model the model to use
	 */
	@Override
	public void setModel(ListModel model) {
		if (model instanceof ResultHistoryModel)
			super.setModel(model);
		else
			throw new IllegalArgumentException("Model is not derived from " + ResultHistoryModel.class.getName() + "!");
	}
}
