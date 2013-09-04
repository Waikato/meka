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

import weka.core.OptionHandler;
import weka.core.Utils;
import weka.gui.PropertyDialog;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyEditor;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JOptionPane;

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
  public static final String MEKA_GUIEDITORS_PROPERTY_FILE = "meka/gui/goe/GUIEditors.props";

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
  }

  /** 
   * Determines all the classes, WEKA and MEKA (latter always dynamic).
   */
  static {
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
      props = Utils.readProperties(MEKA_GUIEDITORS_PROPERTY_FILE);
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
   * For testing only.
   * 
   * @param args	ignored
   */
  public static void main(String[] args) {
    try {
      registerAllEditors();
      GenericObjectEditor ce = new GenericObjectEditor(true);
      ce.setClassType(meka.classifiers.multilabel.MultilabelClassifier.class);
      Object initial = new meka.classifiers.multilabel.BR();
      if (args.length > 0){
	ce.setClassType(Class.forName(args[0]));
	if(args.length > 1){
	  initial = Class.forName(args[1]).newInstance();
	  ce.setValue(initial);
	}
	else
	  ce.setDefaultValue();
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
