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
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */

package meka.gui.core;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * A little helper class for GUI related stuff.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class GUIHelper {

	/** the directory with the images. */
	public final static String IMAGE_DIR = "meka/gui/images/";
	
	/** the empty icon name. */
	public final static String EMPTY_ICON = "empty.gif";
	
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
		return new Font("monospaced", Font.PLAIN, 12);
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
}
