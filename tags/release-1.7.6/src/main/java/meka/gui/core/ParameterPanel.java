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
 * ParameterPanel.java
 * Copyright (C) 2010-2012 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.core;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

/**
 * A panel that lists one parameter (label and component or just
 * AbstractChooserPanel) per row. The sizes of the labels get automatically
 * adjusted.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ParameterPanel
  extends JPanel {

  /** for serialization. */
  private static final long serialVersionUID = 7164103981772081436L;

  /** the panels that make up the rows. */
  protected Vector<JPanel> m_Rows;

  /** the labels. */
  protected Vector<JLabel> m_Labels;

  /** the parameters. */
  protected Vector<Component> m_Parameters;

  /** the horizontal gap. */
  protected int m_GapHorizontal;

  /** the vertical gap. */
  protected int m_GapVertical;

  /** the preferred dimensions for JSpinner components. */
  protected Dimension m_PreferredDimensionJSpinner;

  /**
   * Initializes the panel.
   */
  public ParameterPanel() {
    this(0, 0);
  }

  /**
   * Initializes the panel.
   *
   * @param hgap	the horizontal gap in pixel
   * @param vgap	the vertical gap in pixel
   */
  public ParameterPanel(int hgap, int vgap) {
    super();
    initialize();
    initGUI();

    m_GapHorizontal = hgap;
    m_GapVertical   = vgap;

    update();
  }

  /**
   * Initializes the members.
   */
  protected void initialize() {
    m_Rows                       = new Vector<JPanel>();
    m_Labels                     = new Vector<JLabel>();
    m_Parameters                 = new Vector<Component>();
    m_PreferredDimensionJSpinner = new Dimension(100, 20);
  }

  /**
   * Initializes the members.
   */
  protected void initGUI() {
    update();
  }

  /**
   * Removes all parameters.
   */
  public void clearParameters() {
    m_Rows.clear();
    m_Labels.clear();
    m_Parameters.clear();
    update();
  }

  /**
   * Sets the preferred dimension for JSpinner and derived classes.
   *
   * @param value	the preferred dimensions (do not use 0 for height!)
   */
  public void setPreferredDimensionJSpinner(Dimension value) {
    m_PreferredDimensionJSpinner = (Dimension) value.clone();
    update();
  }

  /**
   * Returns the preferred dimension for JSpinner and derived classes.
   *
   * @return		the preferred dimensions
   */
  public Dimension getPreferredDimensionJSpinner() {
    return m_PreferredDimensionJSpinner;
  }

  /**
   * Adds the label and component as new row at the end.
   *
   * @param label	the label to add, the mnemonic to use is preceded by "_"
   * @param comp	the component to add
   */
  public void addParameter(String label, Component comp) {
    addParameter(-1, label, comp);
  }

  /**
   * Inserts the label and component as new row at the specified row.
   *
   * @param label	the label to add, the mnemonic to use is preceded by "_"
   * @param comp	the component to add
   * @param index	the row index to insert the label/editfield at, -1 will
   * 			add the component at the end
   */
  public void addParameter(int index, String label, Component comp) {
    JLabel	lbl;
    JPanel	panel;

    lbl = new JLabel(label.replace("" + GUIHelper.MNEMONIC_INDICATOR, ""));
    lbl.setDisplayedMnemonic(GUIHelper.getMnemonic(label));
    lbl.setLabelFor(comp);

    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(lbl);
    panel.add(comp);

    if (index == -1) {
      m_Rows.add(panel);
      m_Labels.add(lbl);
      m_Parameters.add(comp);
    }
    else {
      m_Rows.add(index, panel);
      m_Labels.add(index, lbl);
      m_Parameters.add(index, comp);
    }

    update();
  }

  /**
   * Removes the parameter at the specified location.
   *
   * @param index	the row index
   */
  public void removeParameter(int index) {
    m_Rows.remove(index);
    m_Labels.remove(index);
    m_Parameters.remove(index);
    update();
  }

  /**
   * Returns the parameter component at the specified location.
   *
   * @param index	the row index
   * @return		the requested component
   */
  public Component getParameter(int index) {
    return m_Parameters.get(index);
  }

  /**
   * Returns the number of parameters currently displayed.
   *
   * @return		the number of rows
   */
  public int getParameterCount() {
    return m_Parameters.size();
  }

  /**
   * Returns the label for the parameter at the specified location.
   *
   * @param index	the row index
   * @return		the requested label
   */
  public JLabel getLabel(int index) {
    return m_Labels.get(index);
  }

  /**
   * Updates the layout.
   */
  protected void update() {
    int		i;
    Dimension	preferred;

    setLayout(new GridLayout(m_Rows.size(), 1, m_GapHorizontal, m_GapVertical));

    for (i = 0; i < m_Rows.size(); i++)
      add(m_Rows.get(i));

    // set preferred dimensions for JSpinners
    for (i = 0; i < m_Rows.size(); i++) {
      if (m_Parameters.get(i) instanceof JSpinner)
	((JSpinner) m_Parameters.get(i)).setPreferredSize((Dimension) m_PreferredDimensionJSpinner.clone());
    }

    // determine largest preferred size
    preferred = new Dimension(0, 0);
    for (i = 0; i < m_Rows.size(); i++) {
      if (m_Labels.get(i).getPreferredSize().getWidth() > preferred.getWidth())
	preferred = m_Labels.get(i).getPreferredSize();
    }

    // update preferred sizes
    for (i = 0; i < m_Rows.size(); i++)
      m_Labels.get(i).setPreferredSize(preferred);
  }

  /**
   * Sets the enabled state of the panel.
   *
   * @param enabled	if true then the parameters will be editable
   */
  @Override
  public void setEnabled(boolean enabled) {
    int		i;

    for (i = 0; i < m_Parameters.size(); i++)
      m_Parameters.get(i).setEnabled(enabled);

    super.setEnabled(enabled);
  }
}
