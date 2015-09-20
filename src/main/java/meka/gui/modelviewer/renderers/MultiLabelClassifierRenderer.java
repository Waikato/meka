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
 * MultiLabelClassifierRenderer.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.modelviewer.renderers;

import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.gui.core.GUIHelper;
import weka.core.ClassDiscovery;
import weka.core.Utils;

import javax.swing.*;
import java.awt.*;

/**
 * Renders MultiLabelClassifier objects.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MultiLabelClassifierRenderer
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
		return (ClassDiscovery.hasInterface(MultiLabelClassifier.class, cls));
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
		JTextArea               text;
		MultiLabelClassifier    cls;
		StringBuilder           content;

		cls  = (MultiLabelClassifier) obj;
		content = new StringBuilder();
		content.append("Command-line\n");
		content.append("============\n\n");
		content.append(Utils.toCommandLine(cls) + "\n\n");
		content.append("Model\n");
		content.append("=====\n\n");
		content.append(cls.getModel());
		text = new JTextArea();
		text.setFont(GUIHelper.getMonospacedFont());
		text.setText(content.toString());
		text.setCaretPosition(0);
		panel.add(new BaseScrollPane(text), BorderLayout.CENTER);

		return null;
	}
}
