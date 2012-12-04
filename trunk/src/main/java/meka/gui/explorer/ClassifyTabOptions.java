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
 * ClassifyTabOptions.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package meka.gui.explorer;

import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instances;
import java.io.FileInputStream;

import meka.gui.core.ParameterPanel;

/**
 * Panel for options for classification.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ClassifyTabOptions
  extends ParameterPanel {

  /** for serialization. */
  private static final long serialVersionUID = 20374810419572094L;

  /** the text field for the seed value. */
  protected JTextField m_TextSeed;
  
  /** the text field for split percentage. */
  protected JTextField m_TextSplitPercentage;
  
  /** the number of folds for CV. */
  protected JTextField m_TextFolds;
  
  /** the threshold option. */
  protected JTextField m_TextTOP;
  
  /** for randomizing. */
  protected JToggleButton m_ToggleRandomize;
  
  /** for test file. */
  protected JButton m_ButtonFile;
  private JFileChooser fc = new JFileChooser();
  protected Instances m_FileTestset = null;
  
  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    super.initGUI();
    
    m_TextSeed = new JTextField("1", 5);
    addParameter("Random seed", m_TextSeed);
    
    m_TextSplitPercentage = new JTextField("66.0", 5);
    addParameter("Split Percentage", m_TextSplitPercentage);
    
    m_TextFolds = new JTextField("10", 5);
    addParameter("CV folds", m_TextFolds);

	m_TextTOP = new JTextField("PCut1", 5);
    addParameter("Threshold", m_TextTOP);

	m_ToggleRandomize = new JToggleButton("Randomize", false);
    addParameter("Randomize?", m_ToggleRandomize);

	m_ButtonFile = new JButton("Open");
	m_ButtonFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
		  if ( fc.showOpenDialog(ClassifyTabOptions.this) == JFileChooser.APPROVE_OPTION) {
			  try {
				  m_FileTestset = DataSource.read(new FileInputStream(fc.getSelectedFile()));
			  } catch(Exception exp) {
				  System.err.println("[Error] Failed to load file.");
				  exp.printStackTrace();
			  }
		  }
      }
    });
    addParameter("Test File", m_ButtonFile);
  }
  
  /**
   * Sets the Test File option
   * 
   * @param B	the Randomize value to use
   */
  public void setTestFile(Instances file) {
	  m_FileTestset = file;
  }
  
  /**
   * Returns the currently selected Test File (if any).
   * 
   * @return		the Randomize value
   */
  public Instances getTestFile() {
      return m_FileTestset;
  }
  
  /**
   * Sets the Randomize option
   * 
   * @param B	the Randomize value to use
   */
  public void setRandomize(boolean B) {
	  m_ToggleRandomize.setSelected(B);
  }
  
  /**
   * Returns the currently set Randomize value.
   * 
   * @return		the Randomize value
   */
  public boolean getRandomize() {
	  // should probably do some checks here!
      return m_ToggleRandomize.isSelected();
  }
  
  /**
   * Sets the threshold option
   * 
   * @param value	the threshold value to use
   */
  public void setTOP(String value) {
    m_TextTOP.setText("" + value);
  }
  
  /**
   * Returns the currently set seed value.
   * 
   * @return		the threshold value
   */
  public String getTOP() {
	  // should probably do some checks here!
      return m_TextTOP.getText();
  }
  
  /**
   * Sets the seed value.
   * 
   * @param value	the seed value to use
   */
  public void setSeed(int value) {
    m_TextSeed.setText("" + value);
  }
  
  /**
   * Returns the currently set seed value.
   * 
   * @return		the seed value
   */
  public int getSeed() {
    int		result;
    
    try {
      result = Integer.parseInt(m_TextSeed.getText());
    }
    catch (Exception e) {
      System.err.println("Failed to parse seed value: " + m_TextSeed.getText());
      e.printStackTrace();
      result = 1;
    }
    
    return result;
  }
  
  /**
   * Sets the percentage value.
   * 
   * @param value	the percentage value to use
   */
  public void setSplitPercentage(double value) {
    m_TextSplitPercentage.setText("" + value);
  }
  
  /**
   * Returns the currently set percentage value.
   * 
   * @return		the percentage value
   */
  public double getSplitPercentage() {
    double		result;
    
    try {
      result = Double.parseDouble(m_TextSplitPercentage.getText());
    }
    catch (Exception e) {
      System.err.println("Failed to parse percentage value: " + m_TextSplitPercentage.getText());
      e.printStackTrace();
      result = 1;
    }
    
    return result;
  }
  
  /**
   * Sets the folds value.
   * 
   * @param value	the folds value to use
   */
  public void setFolds(int value) {
    m_TextFolds.setText("" + value);
  }
  
  /**
   * Returns the currently set folds value.
   * 
   * @return		the folds value
   */
  public int getFolds() {
    int		result;
    
    try {
      result = Integer.parseInt(m_TextFolds.getText());
    }
    catch (Exception e) {
      System.err.println("Failed to parse folds value: " + m_TextFolds.getText());
      e.printStackTrace();
      result = 1;
    }
    
    return result;
  }
}
