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
 * LookAndFeel.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package meka.gui.laf;

import meka.core.Project;
import meka.core.PropsUtils;
import weka.core.PluginManager;
import weka.core.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * For installing the look and feel of the application.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class LookAndFeel {

	/** the properties file. */
	public final static String FILENAME = "meka/gui/laf/LookAndFeel.props";

	public static final String KEY_THEME = "Theme";

	/**
	 * Installs the look and feel.
	 */
	public static boolean install() {
		boolean         result;
		Properties          props;
		String              theme;
		AbstractLookAndFeel laf;

		result = false;
		try {
			props = PropsUtils.read(FILENAME);
			theme = props.getProperty(KEY_THEME);
			if (theme != null) {
				laf = (AbstractLookAndFeel) Utils.forName(AbstractLookAndFeel.class, theme, null);
				laf.install();
				System.out.println("Installed look and feel: " + theme);
				result = true;
			}
		}
		catch (Exception e) {
			System.err.println("Failed to install look and feel from props file, using system as default!");
		}

		if (!result)
			new SystemLookAndFeel().install();

		return result;
	}

	/**
	 * Installs the specified look and feel as the new default one.
	 *
	 * @param laf       the look and feel to install
	 */
	public static void install(AbstractLookAndFeel laf) {
		StringBuilder   comments;
		Properties      props;

		comments = new StringBuilder();
		comments.append("The classname of the look and feel to use\n");
		for (AbstractLookAndFeel l : getAvailable())
			comments.append("Theme=").append(l.getClass().getName()).append("\n");

		props = new Properties();
		props.setProperty(KEY_THEME, laf.getClass().getName());
		PropsUtils.write(props, comments.toString(), Project.expandFile(new File(FILENAME).getName()).getAbsolutePath());
	}

	/**
	 * Returns the available look and feels.
	 *
	 * @return      the available look and feels
	 */
	public static List<AbstractLookAndFeel> getAvailable() {
		List<AbstractLookAndFeel>   result;
		Set<String> classes;

		result = new ArrayList<>();
		classes = PluginManager.getPluginNamesOfType(AbstractLookAndFeel.class.getName());
		for (String cls: classes) {
			try {
				result.add((AbstractLookAndFeel) Utils.forName(AbstractLookAndFeel.class, cls, null));
			}
			catch (Exception e) {
				System.err.println("Failed to instantiate look and feel: " + cls);
			}
		}

		Collections.sort(result, new Comparator<AbstractLookAndFeel>() {
			@Override
			public int compare(AbstractLookAndFeel o1, AbstractLookAndFeel o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		return result;
	}
}
