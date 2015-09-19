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
 * GenericObjectEditor.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.goe;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Project;
import meka.core.PropsUtils;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.logging.Logger;
import weka.gui.HierarchyPropertyParser;
import weka.gui.PropertyDialog;
import weka.gui.beans.PluginManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyEditor;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 * An extended GOE to cater for the multi-label classifiers.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class GenericObjectEditor
		extends weka.gui.GenericObjectEditor {

	/** Contains the editor properties. */
	protected static Properties MEKA_EDITOR_PROPERTIES;

	/** the properties files containing the class/editor mappings. */
	public static final String MEKA_GUIEDITORS_PROPERTY_FILE = "meka/gui/goe/MekaEditors.props";

	/** whether to output some debugging information. */
	public static boolean DEBUG = "true".equals(System.getenv("MEKA_DEBUG"));

	/** whether the MEAK Editors were already registered. */
	protected static boolean m_MekaEditorsRegistered;

	public static void determineAllClasses() {
		weka.gui.GenericObjectEditor.determineClasses();
		try {
			GenericPropertiesCreator creator = new GenericPropertiesCreator();
			try {
				creator.execute(false);
				MEKA_EDITOR_PROPERTIES = creator.getOutputProperties();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(
						null,
						"Could not determine the properties for the generic object\n"
								+ "editor. This exception was produced:\n"
								+ e.toString(),
						"MEKA GenericObjectEditor",
						JOptionPane.ERROR_MESSAGE);
			}

			if (MEKA_EDITOR_PROPERTIES == null) {
				JOptionPane.showMessageDialog(
						null,
						"Could not initialize the MEKA GenericPropertiesCreator. ",
						"MEKA GenericObjectEditor",
						JOptionPane.ERROR_MESSAGE);
			}
			else {
				PluginManager.addFromProperties(MEKA_EDITOR_PROPERTIES);
				Enumeration<String> keys = (Enumeration<String>) MEKA_EDITOR_PROPERTIES.propertyNames();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					// merged property?
					if (EDITOR_PROPERTIES.containsKey(key))
						EDITOR_PROPERTIES.setProperty(
								key,
								EDITOR_PROPERTIES.getProperty(key) + "," + MEKA_EDITOR_PROPERTIES.getProperty(key));
					else
						EDITOR_PROPERTIES.setProperty(
								key,
								MEKA_EDITOR_PROPERTIES.getProperty(key));
				}
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(
					null,
					"Could not initialize the MEKA GenericPropertiesCreator. "
							+ "This exception was produced:\n"
							+ e.toString(),
					"MEKA GenericObjectEditor",
					JOptionPane.ERROR_MESSAGE);
		}
		if (DEBUG)
			System.out.println("start<GenericObjectEditor.determineAllClasses()>\n" + PropsUtils.toString(EDITOR_PROPERTIES, null) + "end<GenericObjectEditor.determineAllClasses()>\n");
	}

	/**
	 * Determines all the classes, WEKA and MEKA (latter always dynamic).
	 */
	static {
		if (DEBUG)
			PropsUtils.DEBUG = true;
		determineAllClasses();
	}

	/**
	 * registers all the editors in WEKA and MEKA.
	 */
	public static void registerAllEditors() {
		Properties 		props;
		Enumeration 	enm;
		String 		name;
		String 		value;

		registerEditors();

		if (m_MekaEditorsRegistered)
			return;

		System.err.println("---Registering MEKA Editors---");
		m_MekaEditorsRegistered = true;

		// load properties
		try {
			props = PropsUtils.read(MEKA_GUIEDITORS_PROPERTY_FILE);
			if (DEBUG)
				System.out.println("start<GenericObjectEditor.registerAllEditors()>\n" + PropsUtils.toString(props, null) + "end<GenericObjectEditor.registerAllEditors()>\n");
		}
		catch (Exception e) {
			props = new Properties();
			e.printStackTrace();
		}

		enm = props.propertyNames();
		while (enm.hasMoreElements()) {
			name  = enm.nextElement().toString();
			value = props.getProperty(name, "");

			registerEditor(name, value);
		}
	}

	/**
	 * Default constructor.
	 */
	public GenericObjectEditor() {
		super();
	}

	/**
	 * Constructor that allows specifying whether it is possible
	 * to change the class within the editor dialog.
	 *
	 * @param canChangeClassInDialog whether the user can change the class
	 */
	public GenericObjectEditor(boolean canChangeClassInDialog) {
		super(canChangeClassInDialog);
	}

	/**
	 * Called when the class of object being edited changes.
	 *
	 * @return the hashtable containing the HierarchyPropertyParsers for the root
	 *         elements
	 */
	protected Hashtable<String, HierarchyPropertyParser> getClassesFromProperties() {
		String className = m_ClassType.getName();
		if (className.startsWith("meka.")) {
			Hashtable<String, HierarchyPropertyParser> hpps = new Hashtable<String, HierarchyPropertyParser>();
			Hashtable<String,String> typeOptions = sortClassesByRoot(EDITOR_PROPERTIES.getProperty(className));
			try {
				Enumeration<String> enm = typeOptions.keys();
				while (enm.hasMoreElements()) {
					String root = enm.nextElement();
					String typeOption = typeOptions.get(root);
					HierarchyPropertyParser hpp = new HierarchyPropertyParser();
					hpp.build(typeOption, ", ");
					hpps.put(root, hpp);
				}
			} catch (Exception ex) {
				Logger.log(weka.core.logging.Logger.Level.WARNING, "Invalid property: "
						+ typeOptions);
			}
			if (DEBUG)
				System.out.println("Meka classes: " + hpps);
			return hpps;
		}
		return super.getClassesFromProperties();
	}

	/**
	 * Returns a popup menu that allows the user to change the class of object.
	 *
	 * @return a JPopupMenu that when shown will let the user choose the class
	 */
	public JPopupMenu getChooseClassPopupMenu() {
		if (DEBUG)
			System.out.println("Objectnames: " + m_ObjectNames);
		return super.getChooseClassPopupMenu();
	}

	/**
	 * For testing only.
	 *
	 * @param args	ignored
	 */
	public static void main(String[] args) {
		Project.initialize();
		try {
			registerAllEditors();
			GenericObjectEditor ce = new GenericObjectEditor(true);
			ce.setClassType(MultiLabelClassifier.class);
			Object initial = new meka.classifiers.multilabel.BR();
			if (args.length > 0){
				ce.setClassType(Class.forName(args[0]));
				if (args.length > 1) {
					initial = Class.forName(args[1]).newInstance();
					ce.setValue(initial);
				}
				else {
					ce.setDefaultValue();
				}
			}
			else {
				ce.setValue(initial);
			}

			PropertyDialog pd = new PropertyDialog((Frame) null, ce, 100, 100);
			pd.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					PropertyEditor pe = ((PropertyDialog)e.getSource()).getEditor();
					Object c = pe.getValue();
					String options = "";
					if (c instanceof OptionHandler)
						options = Utils.joinOptions(((OptionHandler)c).getOptions());
					System.out.println(c.getClass().getName() + " " + options);
					System.exit(0);
				}
			});
			pd.setVisible(true);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		}
	}
}
