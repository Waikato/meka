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
 *    MekaExperiment.java
 *    Copyright (C) 1999-2014 University of Waikato, Hamilton, New Zealand
 *
 */

package meka.experiment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import meka.core.MLUtils;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ConverterUtils;
import weka.core.xml.KOML;
import weka.core.xml.XMLOptions;
import weka.experiment.Experiment;
import weka.experiment.xml.XMLExperiment;

/**
 * Holds all the necessary configuration information for a standard type
 * experiment. This object is able to be serialized for storage on disk.
 * 
 * <!-- options-start -->
 * Valid options are:
 * <p>
 * 
 * <pre>
 * -L &lt;num&gt;
 *  The lower run number to start the experiment from.
 *  (default 1)
 * </pre>
 * 
 * <pre>
 * -U &lt;num&gt;
 *  The upper run number to end the experiment at (inclusive).
 *  (default 10)
 * </pre>
 * 
 * <pre>
 * -T &lt;arff file&gt;
 *  The dataset to run the experiment on.
 *  (required, may be specified multiple times)
 * </pre>
 * 
 * <pre>
 * -P &lt;class name&gt;
 *  The full class name of a ResultProducer (required).
 *  eg: weka.experiment.RandomSplitResultProducer
 * </pre>
 * 
 * <pre>
 * -D &lt;class name&gt;
 *  The full class name of a ResultListener (required).
 *  eg: weka.experiment.CSVResultListener
 * </pre>
 * 
 * <pre>
 * -N &lt;string&gt;
 *  A string containing any notes about the experiment.
 *  (default none)
 * </pre>
 * 
 * <pre>
 * Options specific to result producer weka.experiment.RandomSplitResultProducer:
 * </pre>
 * 
 * <pre>
 * -P &lt;percent&gt;
 *  The percentage of instances to use for training.
 *  (default 66)
 * </pre>
 * 
 * <pre>
 * -D
 * Save raw split evaluator output.
 * </pre>
 * 
 * <pre>
 * -O &lt;file/directory name/path&gt;
 *  The filename where raw output will be stored.
 *  If a directory name is specified then then individual
 *  outputs will be gzipped, otherwise all output will be
 *  zipped to the named file. Use in conjuction with -D. (default splitEvalutorOut.zip)
 * </pre>
 * 
 * <pre>
 * -W &lt;class name&gt;
 *  The full class name of a SplitEvaluator.
 *  eg: weka.experiment.ClassifierSplitEvaluator
 * </pre>
 * 
 * <pre>
 * -R
 *  Set when data is not to be randomized and the data sets' size.
 *  Is not to be determined via probabilistic rounding.
 * </pre>
 * 
 * <pre>
 * Options specific to split evaluator weka.experiment.ClassifierSplitEvaluator:
 * </pre>
 * 
 * <pre>
 * -W &lt;class name&gt;
 *  The full class name of the classifier.
 *  eg: weka.classifiers.bayes.NaiveBayes
 * </pre>
 * 
 * <pre>
 * -C &lt;index&gt;
 *  The index of the class for which IR statistics
 *  are to be output. (default 1)
 * </pre>
 * 
 * <pre>
 * -I &lt;index&gt;
 *  The index of an attribute to output in the
 *  results. This attribute should identify an
 *  instance in order to know which instances are
 *  in the test set of a cross validation. if 0
 *  no output (default 0).
 * </pre>
 * 
 * <pre>
 * -P
 *  Add target and prediction columns to the result
 *  for each fold.
 * </pre>
 * 
 * <pre>
 * Options specific to classifier weka.classifiers.rules.ZeroR:
 * </pre>
 * 
 * <pre>
 * -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * All options after -- will be passed to the result producer.
 * <p>
 * 
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10376 $
 */
public class MekaExperiment
	extends Experiment {

	/** for serialization */
	static final long serialVersionUID = 44945596742646663L;

	/** the total number of classes. */
	protected int m_TotalNumClasses = 0;
	
	/**
	 * Prepares an experiment for running, initializing current iterator settings.
	 * 
	 * @throws Exception if an error occurs
	 */
	@Override
	public void initialize() throws Exception {
		m_TotalNumClasses = 0;
		for (int i = 0; i < getDatasets().getSize(); i++) {
			int numClasses = MLUtils.peekClassIndex((File) getDatasets().getElementAt(i));
			m_TotalNumClasses = Math.max(m_TotalNumClasses, numClasses);
		}
		System.err.println("Total number of classes: " + m_TotalNumClasses);
		if (m_ResultProducer instanceof MekaResultProducer)
			((MekaResultProducer) m_ResultProducer).setTotalNumClasses(m_TotalNumClasses);

		super.initialize();
	}

	/**
	 * Carries out the next iteration of the experiment.
	 * 
	 * @throws Exception if an error occurs
	 */
	@Override
	public void nextIteration() throws Exception {

		if (m_UsePropertyIterator) {
			if (m_CurrentProperty != m_PropertyNumber) {
				setProperty(0, m_ResultProducer);
				m_CurrentProperty = m_PropertyNumber;
			}
		}

		if (m_CurrentInstances == null) {
			File currentFile = (File) getDatasets().elementAt(m_DatasetNumber);
			AbstractFileLoader loader = ConverterUtils.getLoaderForFile(currentFile);
			loader.setFile(currentFile);
			Instances data = new Instances(loader.getDataSet());
			MLUtils.prepareData(data);
			m_CurrentInstances = data;
			m_ResultProducer.setInstances(m_CurrentInstances);
		}

		m_ResultProducer.doRun(m_RunNumber);

		advanceCounters();
	}

	/**
	 * Loads an experiment from a file.
	 * 
	 * @param filename the file to load the experiment from
	 * @return the experiment
	 * @throws Exception if loading fails
	 */
	public static MekaExperiment read(String filename) throws Exception {
		MekaExperiment result;

		// KOML?
		if ((KOML.isPresent())
				&& (filename.toLowerCase().endsWith(KOML.FILE_EXTENSION))) {
			result = (MekaExperiment) KOML.read(filename);
		}
		// XML?
		else if (filename.toLowerCase().endsWith(".xml")) {
			XMLExperiment xml = new XMLExperiment();
			result = (MekaExperiment) xml.read(filename);
		}
		// binary
		else {
			FileInputStream fi = new FileInputStream(filename);
			ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(fi));
			result = (MekaExperiment) oi.readObject();
			oi.close();
		}

		return result;
	}

	/**
	 * Writes the experiment to disk.
	 * 
	 * @param filename the file to write to
	 * @param exp the experiment to save
	 * @throws Exception if writing fails
	 */
	public static void write(String filename, MekaExperiment exp) throws Exception {
		// KOML?
		if ((KOML.isPresent())
				&& (filename.toLowerCase().endsWith(KOML.FILE_EXTENSION))) {
			KOML.write(filename, exp);
		}
		// XML?
		else if (filename.toLowerCase().endsWith(".xml")) {
			XMLExperiment xml = new XMLExperiment();
			xml.write(filename, exp);
		}
		// binary
		else {
			FileOutputStream fo = new FileOutputStream(filename);
			ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(
					fo));
			oo.writeObject(exp);
			oo.close();
		}
	}

	/**
	 * Configures/Runs the Experiment from the command line.
	 * 
	 * @param args command line arguments to the Experiment.
	 */
	public static void main(String[] args) {
		try {
			weka.core.WekaPackageManager.loadPackages(false);
			MekaExperiment exp = null;
			// get options from XML?
			String xmlOption = Utils.getOption("xml", args);
			if (!xmlOption.equals("")) {
				args = new XMLOptions(xmlOption).toArray();
			}

			String expFile = Utils.getOption('l', args);
			String saveFile = Utils.getOption('s', args);
			boolean runExp = Utils.getFlag('r', args);
			boolean verbose = Utils.getFlag("verbose", args);
			if (expFile.length() == 0) {
				exp = new MekaExperiment();
				try {
					exp.setOptions(args);
					Utils.checkForRemainingOptions(args);
				} catch (Exception ex) {
					ex.printStackTrace();
					String result = "Usage:\n\n" + "-l <exp|xml file>\n"
							+ "\tLoad experiment from file (default use cli options).\n"
							+ "\tThe type is determined, based on the extension ("
							+ FILE_EXTENSION + " or .xml)\n" + "-s <exp|xml file>\n"
							+ "\tSave experiment to file after setting other options.\n"
							+ "\tThe type is determined, based on the extension ("
							+ FILE_EXTENSION + " or .xml)\n" + "\t(default don't save)\n"
							+ "-r\n" + "\tRun experiment (default don't run)\n"
							+ "-xml <filename | xml-string>\n"
							+ "\tget options from XML-Data instead from parameters.\n"
							+ "-verbose\n" + "\toutput progress information to std out." + "\n";
					Enumeration<Option> enm = ((OptionHandler) exp).listOptions();
					while (enm.hasMoreElements()) {
						Option option = enm.nextElement();
						result += option.synopsis() + "\n";
						result += option.description() + "\n";
					}
					throw new Exception(result + "\n" + ex.getMessage());
				}
			} else {
				exp = read(expFile);

				// allow extra datasets to be added to pre-loaded experiment from
				// command line
				String dataName;
				do {
					dataName = Utils.getOption('T', args);
					if (dataName.length() != 0) {
						File dataset = new File(dataName);
						exp.getDatasets().addElement(dataset);
					}
				} while (dataName.length() != 0);

			}
			System.err.println("Experiment:\n" + exp.toString());

			if (saveFile.length() != 0) {
				write(saveFile, exp);
			}

			if (runExp) {
				System.err.println("Initializing...");
				exp.initialize();
				System.err.println("Iterating...");
				exp.runExperiment(verbose);
				System.err.println("Postprocessing...");
				exp.postProcess();
			}

		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 10376 $");
	}
}
