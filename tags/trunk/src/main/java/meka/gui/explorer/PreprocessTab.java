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
 * PreprocessTab.java
 * Copyright (C) 2012-2015 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import meka.filters.unsupervised.attribute.MekaClassAttributes;
import meka.gui.components.AttributeSelectionPanel;
import meka.gui.goe.GenericObjectEditor;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.AttributeSummaryPanel;
import weka.gui.InstancesSummaryPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * For preprocessing data.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PreprocessTab
  extends AbstractThreadedExplorerTab {

  /** for serialization. */
  private static final long serialVersionUID = -7524660995639441810L;

  /** for filtering the data. */
  protected GenericObjectEditor m_GenericObjectEditor;

  /** the button for applying a filter. */
  protected JButton m_ButtonApplyFilter;
  
  /** the panel for the split view of attributes and selected attribute. */
  protected JPanel m_PanelSplit;
  
  /** Panel to let the user toggle attributes. */
  protected AttributeSelectionPanel m_PanelAttributes;

  /** Button for removing attributes */
  protected JButton m_ButtonRemoveAttributes;
  
  /** Displays simple stats on the working instances */
  protected InstancesSummaryPanel m_PanelDataSummary;

  /** Displays summary stats on the selected attribute */
  protected AttributeSummaryPanel m_PanelAttributeSummary;
  
  /** Panel to let the user select the class attributes. */
  protected AttributeSelectionPanel m_PanelClassAttributes;

  /** the button for setting the class attributes. */
  protected JButton m_ButtonSetClassAttributes;
  
  /**
   * Initializes the tab.
   * 
   * @param owner the Explorer this tab belongs to
   */
  public PreprocessTab(Explorer owner) {
    super(owner);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_GenericObjectEditor = new GenericObjectEditor(true);
    m_GenericObjectEditor.setClassType(Filter.class);
    m_GenericObjectEditor.setValue(new weka.filters.AllFilter());
  }

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    JPanel	panel;
    JPanel	panelLeft;
    JPanel	panelRight;
    
    super.initGUI();

    panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Filter"));
    add(panel, BorderLayout.NORTH);
    panel.add(m_GenericObjectEditor.getCustomPanel(), BorderLayout.CENTER);
    m_ButtonApplyFilter = new JButton("Apply");
    m_ButtonApplyFilter.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
	getOwner().addUndoPoint();
	filterData((Filter) m_GenericObjectEditor.getValue(), null);
      }
    });
    panel.add(m_ButtonApplyFilter, BorderLayout.EAST);
    
    // split view
    m_PanelSplit = new JPanel(new GridLayout(1, 2));
    add(m_PanelSplit, BorderLayout.CENTER);
    panelLeft  = new JPanel(new BorderLayout());
    panelRight = new JPanel(new BorderLayout());
    m_PanelSplit.add(panelLeft);
    m_PanelSplit.add(panelRight);
    
    // left view
    m_PanelDataSummary = new InstancesSummaryPanel();
    m_PanelDataSummary.setBorder(BorderFactory.createTitledBorder("Current data set"));
    panelLeft.add(m_PanelDataSummary, BorderLayout.NORTH);
    
    panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Attributes"));
    panelLeft.add(panel, BorderLayout.CENTER);
    
    m_PanelAttributes = new AttributeSelectionPanel();
    m_PanelAttributes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
	if (e.getValueIsAdjusting())
	  return;
	ListSelectionModel lm = (ListSelectionModel) e.getSource();
	for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
	  if (lm.isSelectedIndex(i)) {
	    m_PanelAttributeSummary.setAttribute(i);
	    break;
	  }
	}
      }
    });
    panel.add(m_PanelAttributes, BorderLayout.CENTER);
    
    m_ButtonRemoveAttributes = new JButton("Remove");
    m_ButtonRemoveAttributes.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
	removeAttributes();
      }
    });
    panel.add(m_ButtonRemoveAttributes, BorderLayout.SOUTH);
    
    // right view
    panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Classes"));
    panelRight.add(panel, BorderLayout.NORTH);
    m_PanelClassAttributes = new AttributeSelectionPanel();
    panel.add(m_PanelClassAttributes, BorderLayout.CENTER);
    
    m_ButtonSetClassAttributes = new JButton("Use class attributes");
    m_ButtonSetClassAttributes.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
	useClassAttributes();
      }
    });
    panel.add(m_ButtonSetClassAttributes, BorderLayout.SOUTH);
    
    panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Selected attribute"));
    panelRight.add(panel, BorderLayout.CENTER);
    m_PanelAttributeSummary = new AttributeSummaryPanel();
    panel.add(m_PanelAttributeSummary, BorderLayout.CENTER);
  }

  /**
   * Filters the data with the specified filter.
   * 
   * @param filter the filter to push the data through
   * @param newName the new relation name, null if to keep current
   */
  protected void filterData(final Filter filter, final String newName) {
    Runnable	run;
    
    run = new Runnable() {
      @Override
      public void run() {
	try {
	  String relName = getData().relationName();
	  filter.setInputFormat(getData());
	  Instances filtered = Filter.useFilter(getData(), filter);
	  if (newName == null)
	    filtered.setRelationName(relName);
	  else
	    filtered.setRelationName(newName);
	  getOwner().notifyTabsDataChanged(PreprocessTab.this, filtered);
	  setData(filtered);
	}
	catch (Exception e) {
	  throw new IllegalStateException(e);
	}
      }
    };
    start(run);
  }

  /**
   * Removes the currently selected attributes.
   */
  protected void removeAttributes() {
    int[] 	indices;
    Remove 	remove;
    
    getOwner().addUndoPoint();
    
    indices = m_PanelAttributes.getSelectedAttributes();
    remove  = new Remove();
    remove.setAttributeIndicesArray(indices);
    filterData(remove, null);
  }
	
  /**
   * Sets the selected attributes as class attributes.
   */
  protected void useClassAttributes() {
    int[] 		indices;
    StringBuilder 	range;
    MekaClassAttributes	catts;
    String		newName;
    
    getOwner().addUndoPoint();
    
    indices = m_PanelClassAttributes.getSelectedAttributes();
    range   = new StringBuilder();
    for (int index: indices) {
      if (range.length() > 0)
	range.append(",");
      range.append((index + 1));
    }
    catts = new MekaClassAttributes();
    newName = getData().relationName().replaceFirst(" -C [0-9]+", " -C " + indices.length);
    try {
      catts.setAttributeIndices(range.toString());
      filterData(catts, newName);
    }
    catch (Exception e) {
      System.err.println("Setting of class attributes failed:");
      e.printStackTrace();
      JOptionPane.showMessageDialog(
	  PreprocessTab.this, 
	  "Setting of class attributes failed:\n" + e,
	  "Error",
	  JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Gets called when the thread starts.
   */
  @Override
  protected void executionStarted() {
    m_ButtonApplyFilter.setEnabled(false);
    m_ButtonRemoveAttributes.setEnabled(false);
    m_PanelDataSummary.setEnabled(false);
    m_PanelAttributes.setEnabled(false);
  }

  /**
   * Gets called when the thread finishes or gets stopped.
   * 
   * @param t if the execution generated an exception, null if no errors
   */
  @Override
  protected void executionFinished(Throwable t) {
    m_ButtonApplyFilter.setEnabled(true);
    m_ButtonRemoveAttributes.setEnabled(true);
    m_PanelDataSummary.setEnabled(true);
    m_PanelAttributes.setEnabled(true);
    if (t != null) {
      System.err.println("Processing failed:");
      t.printStackTrace();
      JOptionPane.showMessageDialog(
	  this, 
	  "Processing failed:\n" + t, 
	  "Error",
	  JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Returns the title of the tab.
   * 
   * @return the title
   */
  @Override
  public String getTitle() {
    return "Preprocess";
  }

  /**
   * Gets called when the data changed.
   */
  @Override
  protected void update() {
    m_ButtonApplyFilter.setEnabled(hasData());
    m_PanelAttributes.setEnabled(hasData());
    m_PanelDataSummary.setEnabled(hasData());
    m_ButtonRemoveAttributes.setEnabled(hasData());
    m_PanelAttributeSummary.setEnabled(hasData());
    m_PanelClassAttributes.setEnabled(hasData());
    m_ButtonSetClassAttributes.setEnabled(hasData());
    
    if (hasData()) {
      m_PanelAttributes.setInstances(getData());
      m_PanelDataSummary.setInstances(getData());
      m_PanelAttributeSummary.setInstances(getData());
      m_PanelClassAttributes.setInstances(getData());
    }
  }
}
