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
 * GUIHelper.java
 * Copyright (C) 2012-2015 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import weka.core.Utils;
import weka.gui.ConverterFileChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Properties;

/**
 * A little helper class for GUI related stuff.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class GUIHelper {

	/** the name of the props file. */
	public final static String FILENAME = "GUIHelper.props";

	/** the directory with the images. */
	public final static String IMAGE_DIR = "meka/gui/images/";

	/** the empty icon name. */
	public final static String EMPTY_ICON = "empty.gif";

	/** the mnemonic character indicator. */
	public final static char MNEMONIC_INDICATOR = '_';

	/** the properties. */
	protected static Properties m_Properties;

	/**
	 * Checks whether the image is available.
	 *
	 * @param name	the name of the image (filename without path but with
	 * 			extension)
	 * @return		true if image exists
	 */
	public static boolean hasImageFile(String name) {
		return (getImageFilename(name) != null);
	}

	/**
	 * Adds the path of the images directory to the name of the image.
	 *
	 * @param name	the name of the image to add the path to
	 * @return		the full path of the image
	 */
	public static String getImageFilename(String name) {
		String		result;
		URL				url;

		result = null;

		try {
			url = ClassLoader.getSystemClassLoader().getResource(IMAGE_DIR + name);
			if (url != null)
				result = IMAGE_DIR + name;
		}
		catch (Exception e) {
			// ignored
		}

		return result;
	}

	/**
	 * Returns an ImageIcon for the given class.
	 *
	 * @param cls		the class to get the icon for (gif, png or jpg)
	 * @return		the ImageIcon or null if none found
	 */
	public static ImageIcon getIcon(Class cls) {
		if (hasImageFile(cls.getName() + ".gif"))
			return getIcon(cls.getName() + ".gif");
		else if (hasImageFile(cls.getName() + ".png"))
			return getIcon(cls.getName() + ".png");
		else if (hasImageFile(cls.getName() + ".jpg"))
			return getIcon(cls.getName() + ".jpg");
		else
			return null;
	}

	/**
	 * Returns an ImageIcon from the given name.
	 *
	 * @param name	the filename without path
	 * @return		the ImageIcon or null if not available
	 */
	public static ImageIcon getIcon(String name) {
		String	filename;

		filename = getImageFilename(name);
		if (filename != null)
			return new ImageIcon(ClassLoader.getSystemClassLoader().getResource(filename));
		else
			return null;
	}

	/**
	 * Returns an ImageIcon from the given name.
	 *
	 * @param filename	the filename
	 * @return		the ImageIcon or null if not available
	 */
	public static ImageIcon getExternalIcon(String filename) {
		ImageIcon	result;

		try {
			result = new ImageIcon(ClassLoader.getSystemClassLoader().getResource(filename));
		}
		catch (Exception e) {
			result = null;
		}

		return result;
	}

	/**
	 * Returns the ImageIcon for the empty icon.
	 *
	 * @return		the ImageIcon
	 */
	public static ImageIcon getEmptyIcon() {
		return getIcon(EMPTY_ICON);
	}

	/**
	 * Returns an ImageIcon of the logo (large image).
	 *
	 * @return		the logo or null if none available
	 */
	public static ImageIcon getLogoImage() {
		return getIcon("MEKA.png");
	}

	/**
	 * Returns an ImageIcon of the logo (icon sized image).
	 *
	 * @return		the logo or null if none available
	 */
	public static ImageIcon getLogoIcon() {
		return getIcon("MEKA_icon.png");
	}

	/**
	 * Returns the system wide Monospaced font.
	 *
	 * @return		the font
	 */
	public static Font getMonospacedFont() {
		return new Font("monospaced", Font.PLAIN, 14);
	}

	/**
	 * Tries to determine the parent this panel is part of.
	 *
	 * @param cont	the container to get the parent for
	 * @param parentClass	the class of the parent to obtain
	 * @return		the parent if one exists or null if not
	 */
	public static Object getParent(Container cont, Class parentClass) {
		Container	result;
		Container	parent;

		result = null;

		parent = cont;
		while (parent != null) {
			if (parentClass.isInstance(parent)) {
				result = parent;
				break;
			}
			else {
				parent = parent.getParent();
			}
		}

		return result;
	}

	/**
	 * Tries to determine the frame the container is part of.
	 *
	 * @param cont	the container to get the frame for
	 * @return		the parent frame if one exists or null if not
	 */
	public static Frame getParentFrame(Container cont) {
		return (Frame) getParent(cont, Frame.class);
	}

	/**
	 * Tries to determine the dialog this panel is part of.
	 *
	 * @param cont	the container to get the dialog for
	 * @return		the parent dialog if one exists or null if not
	 */
	public static Dialog getParentDialog(Container cont) {
		return (Dialog) getParent(cont, Dialog.class);
	}

	/**
	 * Returns the mnemonic for this caption, preceded by an underscore "_".
	 *
	 * @param caption	the caption to extract
	 * @return		the extracted mnemonic, \0 if none available
	 * @see		#MNEMONIC_INDICATOR
	 */
	public static char getMnemonic(String caption) {
		int		pos;

		pos = caption.indexOf(MNEMONIC_INDICATOR);
		if ((pos > -1) && (pos < caption.length() - 1))
			return caption.charAt(pos + 1);
		else
			return '\0';
	}

	/**
	 * Initializes the properties if necessary.
	 */
	public static synchronized Properties getProperties() {
		String      filename;

		if (m_Properties == null) {
			filename = "meka/gui/core/" + FILENAME;
			try {
				m_Properties = Utils.readProperties(filename);
			}
			catch (Exception e) {
				System.err.println("Failed to read properties: " + filename);
				e.printStackTrace();
				m_Properties = new Properties();
			}
		}

		return m_Properties;
	}

	/**
	 * Copies the given transferable to the system's clipboard.
	 *
	 * @param t		the transferable to copy
	 */
	public static void copyToClipboard(Transferable t) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
	}

	/**
	 * Copies the given string to the system's clipboard.
	 *
	 * @param s		the string to copy
	 */
	public static void copyToClipboard(String s) {
		copyToClipboard(new TransferableString(s));
	}

	/**
	 * Copies the given image to the system's clipboard.
	 *
	 * @param img		the image to copy
	 */
	public static void copyToClipboard(BufferedImage img) {
		copyToClipboard(new TransferableImage(img));
	}

	/**
	 * Checks whether the specified "flavor" can be obtained from the clipboard.
	 *
	 * @param flavor	the type of data to look for
	 * @return		true if the data can be obtained, false if not available
	 */
	public static boolean canPasteFromClipboard(DataFlavor flavor) {
		Clipboard clipboard;
		boolean		result;

		try {
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			result    = clipboard.isDataFlavorAvailable(flavor);
		}
		catch (Exception e) {
			result = false;
		}

		return result;
	}

	/**
	 * Checks whether a string can be obtained from the clipboard.
	 *
	 * @return		true if string can be obtained, false if not available
	 */
	public static boolean canPasteStringFromClipboard() {
		return canPasteFromClipboard(DataFlavor.stringFlavor);
	}

	/**
	 * Obtains an object from the clipboard.
	 *
	 * @param flavor	the type of object to obtain
	 * @return		the obtained object, null if not available
	 */
	public static Object pasteFromClipboard(DataFlavor flavor) {
		Clipboard 		clipboard;
		Object		result;
		Transferable	content;

		result = null;

		try {
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			content   = clipboard.getContents(null);
			if ((content != null) && (content.isDataFlavorSupported(flavor)))
				result = content.getTransferData(flavor);
		}
		catch (Exception e) {
			result = null;
		}

		return result;
	}

	/**
	 * Obtains a string from the clipboard.
	 *
	 * @return		the obtained string, null if not available
	 */
	public static String pasteStringFromClipboard() {
		Clipboard 		clipboard;
		String		result;
		Transferable	content;

		result = null;

		try {
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			content   = clipboard.getContents(null);
			if ((content != null) && (content.isDataFlavorSupported(DataFlavor.stringFlavor)))
				result = (String) content.getTransferData(DataFlavor.stringFlavor);
		}
		catch (Exception e) {
			result = null;
		}

		return result;
	}

	/**
	 * Returns the default frame dimensions.
	 *
	 * @return the dimensions
	 */
	public static Dimension getDefaultFrameDimensions() {
		return new Dimension(
				Integer.parseInt(getProperties().getProperty("Default.Width",  "800")),
				Integer.parseInt(getProperties().getProperty("Default.Height", "600"))
		);
	}

	/**
	 * Returns the default frame dimensions for this class.
	 *
	 * @param cls the class to get the default dimensions for
	 * @return the dimensions, default ones if not found
	 * @see #getDefaultFrameDimensions()
	 */
	public static Dimension getDefaultFrameDimensions(Class cls) {
		return getDefaultDimensions(cls.getName(), 800, 600);
	}

	/**
	 * Returns the default dimensions for this prefix.
	 *
	 * @param prefix the prefix (+ .Height, .Width) to get the dimensions for
	 * @param defaultWidth the default width
	 * @param defaultHeight the default height
	 * @return the dimensions, default ones if not found
	 */
	public static Dimension getDefaultDimensions(String prefix, int defaultWidth, int defaultHeight) {
		if (!getProperties().containsKey(prefix + ".Width") || !getProperties().containsKey(prefix + ".Height"))
			return new Dimension(defaultWidth, defaultHeight);

		return new Dimension(
				Integer.parseInt(getProperties().getProperty(prefix + ".Width",  "" + defaultWidth)),
				Integer.parseInt(getProperties().getProperty(prefix + ".Height", "" + defaultHeight))
		);
	}

	/**
	 * Returns whether this application needs to be packed, rather than use a specific window size.
	 *
	 * @return true if to be packed
	 */
	public static boolean getPackFrame(Class cls) {
		return getProperties().getProperty(cls.getName() + ".Pack",  "false").equals("true");
	}

	/**
	 * Returns a new instance of a filechooser for datasets including the bookmarks panel.
	 *
	 * @return the file chooser
	 */
	public static ConverterFileChooser newConverterFileChooser() {
		ConverterFileChooser        result;
		FileChooserBookmarksPanel   bookmarks;

		result = new ConverterFileChooser(System.getProperty("user.home"));
		bookmarks = new FileChooserBookmarksPanel();
		bookmarks.setOwner(result);
		bookmarks.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
		result.setAccessory(bookmarks);
		result.setPreferredSize(getDefaultDimensions("FileChooser", 750, 500));


		return result;
	}

	/**
	 * Returns a new instance of a filechooser including the bookmarks panel.
	 *
	 * @return the file chooser
	 */
	public static JFileChooser newFileChooser() {
		JFileChooser                result;
		FileChooserBookmarksPanel   bookmarks;

		result = new JFileChooser(System.getProperty("user.home"));
		bookmarks = new FileChooserBookmarksPanel();
		bookmarks.setOwner(result);
		bookmarks.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
		result.setAccessory(bookmarks);
		result.setPreferredSize(getDefaultDimensions("FileChooser", 750, 500));


		return result;
	}
}
