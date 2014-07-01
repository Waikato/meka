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
 * Example.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package meka.experiment;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.DefaultListModel;

import meka.classifiers.multilabel.BCC;
import meka.classifiers.multilabel.BR;
import meka.gui.goe.GenericObjectEditor;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Range;
import weka.experiment.InstancesResultListener;
import weka.experiment.PairedCorrectedTTester;
import weka.experiment.PairedTTester;
import weka.experiment.PropertyNode;
import weka.experiment.ResultMatrixPlainText;
import weka.experiment.SplitEvaluator;

/**
 * Just for testing the experiment API.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Example {

	/**
	 * Performs a random split on the datassets.
	 * 
	 * @param files			the files to use in the experiment
	 * @param key			the statistic to evaluate on, eg "Accuracy"
	 * @throws Exception	if experiment fails for some reason
	 */
	public static void randomSplit(File[] files, String key) throws Exception {
	    System.out.println("Configuring random split...");
	    
		MekaExperiment exp = new MekaExperiment();
		
		// data
		DefaultListModel datasets = new DefaultListModel();
		for (File file: files)
			datasets.addElement(file);
		exp.setDatasets(datasets);
	    
		// evaluator
		SplitEvaluator se = new MekaClassifierSplitEvaluator();
	    Classifier sec = ((MekaClassifierSplitEvaluator) se).getClassifier();

	    // splitter
	    MekaRandomSplitResultProducer rsrp = new MekaRandomSplitResultProducer();
	    rsrp.setRandomizeData(true);
	    rsrp.setTrainPercent(66.0);
	    rsrp.setSplitEvaluator(se);

	    PropertyNode[] propertyPath = new PropertyNode[2];
	    try {
	    	propertyPath[0] = new PropertyNode(
	    			se, 
	    			new PropertyDescriptor("splitEvaluator",
	    					MekaRandomSplitResultProducer.class),
	    					MekaRandomSplitResultProducer.class);
	    	propertyPath[1] = new PropertyNode(
	    			sec, 
	    			new PropertyDescriptor("classifier",
	    					se.getClass()),
	    					se.getClass());
	    }
	    catch (IntrospectionException e) {
	    	e.printStackTrace();
	    }

	    exp.setResultProducer(rsrp);
	    exp.setPropertyPath(propertyPath);
	    
	    exp.setRunLower(1);
	    exp.setRunUpper(10);
	    
	    // classifiers
	    exp.setPropertyArray(new Classifier[]{
	    		new BR(), 
	    		new BCC()
	    }); 
	    
	    // output
	    InstancesResultListener irl = new InstancesResultListener();
	    File outfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "meka_rs.arff");
	    System.out.println("Storing results in: " + outfile);
	    irl.setOutputFile(outfile);
	    exp.setResultListener(irl);

	    // run
	    System.out.println("Initializing...");
	    exp.initialize();
	    System.out.println("Running...");
	    exp.runExperiment();
	    System.out.println("Finishing...");
	    exp.postProcess();
	    
	    // calculate statistics and output them
	    System.out.println("Evaluating...");
	    PairedTTester tester = new PairedCorrectedTTester();
	    Instances result = new Instances(new BufferedReader(new FileReader(irl.getOutputFile())));
	    tester.setInstances(result);
	    tester.setSortColumn(-1);
	    tester.setRunColumn(result.attribute("Key_Run").index());
	    //tester.setFoldColumn(result.attribute("Key_Fold").index());
	    tester.setResultsetKeyColumns(
		new Range(
		    "" 
		    + (result.attribute("Key_Dataset").index() + 1)));
	    tester.setDatasetKeyColumns(
		new Range(
		    "" 
		    + (result.attribute("Key_Scheme").index() + 1)
		    + ","
		    + (result.attribute("Key_Scheme_options").index() + 1)
		    + ","
		    + (result.attribute("Key_Scheme_version_ID").index() + 1)));
	    tester.setResultMatrix(new ResultMatrixPlainText());
	    tester.setDisplayedResultsets(null);       
	    tester.setSignificanceLevel(0.05);
	    tester.setShowStdDevs(true);
	    // fill result matrix (but discarding the output)
	    tester.multiResultsetFull(0, result.attribute(key).index());
	    // output results for reach dataset
	    System.out.println("\nResult:");
	    int compareCol = result.attribute(key).index();
	    System.out.println(tester.header(compareCol));
	    System.out.println(tester.multiResultsetFull(0, compareCol));
	}

	/**
	 * Performs a cross-validation on the datassets.
	 * 
	 * @param files			the files to use in the experiment
	 * @param key			the statistic to evaluate on, eg "Accuracy"
	 * @throws Exception	if experiment fails for some reason
	 */
	public static void crossValidation(File[] files, String key) throws Exception {
	    System.out.println("Configuring cross-validation...");
	    
		MekaExperiment exp = new MekaExperiment();
		
		// data
		DefaultListModel datasets = new DefaultListModel();
		for (File file: files)
			datasets.addElement(file);
		exp.setDatasets(datasets);
	    
		// evaluator
		SplitEvaluator se = new MekaClassifierSplitEvaluator();
	    Classifier sec = ((MekaClassifierSplitEvaluator) se).getClassifier();

	    // splitter
	    MekaCrossValidationSplitResultProducer cvrp = new MekaCrossValidationSplitResultProducer();
	    cvrp.setNumFolds(10);
	    cvrp.setSplitEvaluator(se);

	    PropertyNode[] propertyPath = new PropertyNode[2];
	    try {
	    	propertyPath[0] = new PropertyNode(
	    			se, 
	    			new PropertyDescriptor("splitEvaluator",
	    					MekaCrossValidationSplitResultProducer.class),
	    					MekaCrossValidationSplitResultProducer.class);
	    	propertyPath[1] = new PropertyNode(
	    			sec, 
	    			new PropertyDescriptor("classifier",
	    					se.getClass()),
	    					se.getClass());
	    }
	    catch (IntrospectionException e) {
	    	e.printStackTrace();
	    }

	    exp.setResultProducer(cvrp);
	    exp.setPropertyPath(propertyPath);
	    
	    exp.setRunLower(1);
	    exp.setRunUpper(10);
	    
	    // classifiers
	    exp.setPropertyArray(new Classifier[]{
	    		new BR(), 
	    		new BCC()
	    }); 
	    
	    // output
	    InstancesResultListener irl = new InstancesResultListener();
	    File outfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "meka_cv.arff");
	    System.out.println("Storing results in: " + outfile);
	    irl.setOutputFile(outfile);
	    exp.setResultListener(irl);

	    // run
	    System.out.println("Initializing...");
	    exp.initialize();
	    System.out.println("Running...");
	    exp.runExperiment();
	    System.out.println("Finishing...");
	    exp.postProcess();
	    
	    // calculate statistics and output them
	    System.out.println("Evaluating...");
	    PairedTTester tester = new PairedCorrectedTTester();
	    Instances result = new Instances(new BufferedReader(new FileReader(irl.getOutputFile())));
	    tester.setInstances(result);
	    tester.setSortColumn(-1);
	    tester.setRunColumn(result.attribute("Key_Run").index());
	    tester.setFoldColumn(result.attribute("Key_Fold").index());
	    tester.setResultsetKeyColumns(
		new Range(
		    "" 
		    + (result.attribute("Key_Dataset").index() + 1)));
	    tester.setDatasetKeyColumns(
		new Range(
		    "" 
		    + (result.attribute("Key_Scheme").index() + 1)
		    + ","
		    + (result.attribute("Key_Scheme_options").index() + 1)
		    + ","
		    + (result.attribute("Key_Scheme_version_ID").index() + 1)));
	    tester.setResultMatrix(new ResultMatrixPlainText());
	    tester.setDisplayedResultsets(null);       
	    tester.setSignificanceLevel(0.05);
	    tester.setShowStdDevs(true);
	    // fill result matrix (but discarding the output)
	    tester.multiResultsetFull(0, result.attribute(key).index());
	    // output results for reach dataset
	    System.out.println("\nResult:");
	    int compareCol = result.attribute(key).index();
	    System.out.println(tester.header(compareCol));
	    System.out.println(tester.multiResultsetFull(0, compareCol));
	}
	
	/**
	 * Performs random split and cross-validation experiment on the datasets
	 * provided as arguments.
	 * 
	 * @param args			the stats key and datasets to use in the experiment
	 * @throws Exception	if experiment fails for some reason
	 */
	public static void main(String[] args) throws Exception {
		GenericObjectEditor.registerAllEditors();
		if (args.length < 2)
			throw new IllegalArgumentException(
					"Insufficient parameters!\n"
					+ "Required: <statskey> dataset1 [dataset2, ...]\n"
					+ "Example: Accuracy /some/where/Music.arff");
		String key = args[0];
		File[] files = new File[args.length - 1];
		for (int i = 1; i < args.length; i++)
			files[i-1] = new File(args[i]);
		randomSplit(files, key);
		crossValidation(files, key);
	}
}
