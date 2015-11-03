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
 * PlainTextRenderer.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.modelviewer.renderers;

import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;
import weka.core.ClassDiscovery;
import weka.core.Instances;
import weka.gui.arffviewer.ArffSortedTableModel;
import weka.gui.arffviewer.ArffTable;

import javax.swing.*;
import java.awt.*;

/**
 * Renders Instances objects.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class InstancesRenderer
		extends AbstractObjectRenderer {

	private static final long serialVersionUID = -3528006886476495175L;

	/**
	 * Checks whether the renderer can handle the specified class.
	 *
	 * @param cls		the class to check
	 * @return		true if the renderer can handle this type of object
	 */
	@Override
	public boolean handles(Class cls) {
		return (ClassDiscovery.isSubclass(Instances.class, cls));
	}

	/**
	 * Performs the actual rendering.
	 *
	 * @param obj		the object to render
	 * @param panel	the panel to render into
	 * @return		null if successful, otherwise error message
	 */
	@Override
	protected String doRender(Object obj, JPanel panel) {
		ArffTable       table;
		Instances       data;

		data = (Instances) obj;
		// bug in weka, cannot render empty dataset
		if (data.numInstances() == 0)
			return new PlainTextRenderer().render(obj, panel);

        table = new ArffTable(new ArffSortedTableModel(data));
		panel.add(new BaseScrollPane(table), BorderLayout.CENTER);

		return null;
	}
}
