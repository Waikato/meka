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
 * SaveModel.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.gui.explorer.classify;

import meka.core.MultiLabelDrawable;
import meka.gui.core.ResultHistoryList;
import weka.gui.graphvisualizer.GraphVisualizer;
import weka.gui.treevisualizer.Node;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeBuild;
import weka.gui.treevisualizer.TreeVisualizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

/**
 * Allows the user to displays graphs if available.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ShowGraphs
		extends AbstractClassifyResultHistoryPlugin {
	private static final long serialVersionUID = -1152575716154907544L;

	/**
	 * Returns the group of the plugin. Used for the grouping the menu items.
	 *
	 * @return          the group
	 */
	public String getGroup() {
		return "Visualization";
	}

	/**
	 * Returns the name of the plugin. Used for the menu item text.
	 *
	 * @return          the name
	 */
	@Override
	public String getName() {
		return "Show graph(s)";
	}

	/**
	 * Checks whether the current item can be handled. Disables/enables the menu item.
	 *
	 * @param history   the current history
	 * @param index     the selected history item
	 * @return          true if can be handled
	 */
	@Override
	public boolean handles(ResultHistoryList history, int index) {
		return (getClassifier(history, index) instanceof MultiLabelDrawable);
	}

	/**
	 * Returns the action lister to use in the menu.
	 *
	 * @param history   the current history
	 * @param index     the selected history item
	 * @return          the listener
	 */
	@Override
	public ActionListener getActionListener(final ResultHistoryList history, final int index) {
		final MultiLabelDrawable d = (MultiLabelDrawable) getClassifier(history, index);

		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Map<Integer,String> graphs;
				Map<Integer,Integer> types;
				java.util.List<Integer> keys;
				try {
					types = d.graphType();
					graphs = d.graph();
					keys = new ArrayList<Integer>(types.keySet());
				}
				catch (Exception ex) {
					System.err.println("Failed to obtain graph(s):");
					ex.printStackTrace();
					return;
				}
				JDialog dialog = new JDialog((Frame) null, history.getSuffixAt(index), false);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				JTabbedPane tabbed = new JTabbedPane();
				dialog.getContentPane().setLayout(new BorderLayout());
				dialog.getContentPane().add(tabbed, BorderLayout.CENTER);
				for (Integer label: keys) {
					int type = types.get(label);
					JComponent comp = null;
					switch (type) {
						case MultiLabelDrawable.TREE:
							TreeBuild b = new TreeBuild();
							PlaceNode2 arrange = new PlaceNode2();
							Node top = b.create(new StringReader(graphs.get(label)));
							comp = new TreeVisualizer(null, top, arrange);
							break;
						case MultiLabelDrawable.BayesNet:
							GraphVisualizer g = new GraphVisualizer();
							g.readDOT(new StringReader(graphs.get(label)));
							comp = g;
							break;
						default:
							System.err.println("Unsupported graph type for label " + label + ": " + type);
					}
					if (comp != null)
						tabbed.addTab("" + label, comp);
				}
				dialog.setSize(800, 600);
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
			}
		};
	}
}
