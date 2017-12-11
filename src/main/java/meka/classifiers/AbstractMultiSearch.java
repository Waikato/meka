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
 * MultiSearch.java
 * Copyright (C) 2008-2017 University of Waikato, Hamilton, New Zealand
 */

package meka.classifiers;

import meka.core.multisearch.MekaEvaluationFactory;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.classifiers.meta.multisearch.AbstractEvaluationFactory;
import weka.classifiers.meta.multisearch.AbstractEvaluationMetrics;
import weka.classifiers.meta.multisearch.AbstractSearch;
import weka.classifiers.meta.multisearch.AbstractSearch.SearchResult;
import weka.classifiers.meta.multisearch.DefaultSearch;
import weka.classifiers.meta.multisearch.MultiSearchCapable;
import weka.classifiers.meta.multisearch.Performance;
import weka.classifiers.meta.multisearch.PerformanceComparator;
import weka.classifiers.meta.multisearch.TraceableOptimizer;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Debug;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.SelectedTag;
import weka.core.SetupGenerator;
import weka.core.Summarizable;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.setupgenerator.AbstractParameter;
import weka.core.setupgenerator.ParameterGroup;
import weka.core.setupgenerator.Point;
import weka.core.setupgenerator.Space;

import java.io.File;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4521 $
 */
public abstract class AbstractMultiSearch
	extends RandomizableSingleClassifierEnhancer
	implements MultiSearchCapable, AdditionalMeasureProducer, Summarizable, TraceableOptimizer {

	/** for serialization. */
	private static final long serialVersionUID = -5129316523575906233L;

	/** the Classifier with the best setup. */
	protected SearchResult m_BestClassifier;

	/** the evaluation factory to use. */
	protected AbstractEvaluationFactory m_Factory;

	/** the metrics to use. */
	protected AbstractEvaluationMetrics m_Metrics;

	/** the type of evaluation. */
	protected int m_Evaluation;

	/** the log file to use. */
	protected File m_LogFile = new File(".");

	/** the default parameters. */
	protected AbstractParameter[] m_DefaultParameters;

	/** the parameters. */
	protected AbstractParameter[] m_Parameters;

	/** the search algorithm. */
	protected AbstractSearch m_Algorithm;

	/** the current setup generator. */
	protected SetupGenerator m_Generator;

	/** for tracking the setups. */
	protected List<Entry<Integer, Performance>> m_Trace;

	/**
	 * the default constructor.
	 */
	public AbstractMultiSearch() {
		super();

		m_Factory           = newFactory();
		m_Metrics           = m_Factory.newMetrics();
		m_Evaluation        = m_Metrics.getDefaultMetric();
		m_Classifier        = defaultClassifier();
		m_DefaultParameters = defaultSearchParameters();
		m_Parameters        = defaultSearchParameters();
		m_Algorithm         = defaultAlgorithm();
		m_Trace             = new ArrayList<Entry<Integer, Performance>>();

		try {
			m_BestClassifier = new SearchResult();
			m_BestClassifier.classifier = AbstractClassifier.makeCopy(m_Classifier);
		}
		catch (Exception e) {
			System.err.println("Failed to create copy of default classifier!");
			e.printStackTrace();
		}
	}

	/**
	 * Returns a string describing classifier.
	 *
	 * @return a description suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return
			"Performs a search of an arbitrary number of parameters of a classifier "
				+ "and chooses the best combination found.\n"
				+ "The properties being explored are totally up to the user.\n"
				+ "\n"
				+ "E.g., if you have a FilteredClassifier selected as base classifier, "
				+ "sporting a PLSFilter and you want to explore the number of PLS components, "
				+ "then your property will be made up of the following components:\n"
				+ " - filter: referring to the FilteredClassifier's property (= PLSFilter)\n"
				+ " - numComponents: the actual property of the PLSFilter that we want to modify\n"
				+ "And assembled, the property looks like this:\n"
				+ "  filter.numComponents\n"
				+ "\n"
				+ "\n"
				+ "The best classifier setup can be accessed after the buildClassifier "
				+ "call via the getBestClassifier method.\n"
				+ "\n"
				+ "The trace of setups evaluated can be accessed after the buildClassifier "
				+ "call as well, using the following methods:\n"
				+ "- getTrace()\n"
				+ "- getTraceSize()\n"
				+ "- getTraceValue(int)\n"
				+ "- getTraceFolds(int)\n"
				+ "- getTraceClassifierAsCli(int)\n"
				+ "- getTraceParameterSettings(int)\n"
				+ "\n"
				+ "Using the " + ParameterGroup.class.getName() + " parameter, it is "
				+ "possible to group dependent parameters. In this case, all top-level "
				+ "parameters must be of type " + ParameterGroup.class.getName() + ".";
	}

	/**
	 * String describing default classifier.
	 *
	 * @return		the classname of the default classifier
	 */
	@Override
	protected String defaultClassifierString() {
		return defaultClassifier().getClass().getName();
	}

	/**
	 * Returns the default classifier to use.
	 *
	 * @return		the default classifier
	 */
	protected abstract Classifier defaultClassifier();

	/**
	 * Returns the default search parameters.
	 *
	 * @return		the parameters
	 */
	protected abstract AbstractParameter[] defaultSearchParameters();

	/**
	 * Gets an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration listOptions() {
		Vector        	result;
		Enumeration   	en;
		String		desc;
		SelectedTag tag;
		int			i;

		result = new Vector();

		desc  = "";
		for (i = 0; i < m_Metrics.getTags().length; i++) {
			tag = new SelectedTag(m_Metrics.getTags()[i].getID(), m_Metrics.getTags());
			desc  +=   "\t" + tag.getSelectedTag().getIDStr()
				+ " = " + tag.getSelectedTag().getReadable()
				+ "\n";
		}
		result.addElement(new Option(
			"\tDetermines the parameter used for evaluation:\n"
				+ desc
				+ "\t(default: " + new SelectedTag(m_Metrics.getDefaultMetric(), m_Metrics.getTags()) + ")",
			"E", 1, "-E " + Tag.toOptionList(m_Metrics.getTags())));

		result.addElement(new Option(
			"\tA property search setup.\n",
			"search", 1, "-search \"<classname options>\""));

		result.addElement(new Option(
			"\tA search algorithm.\n",
			"algorithm", 1, "-algorithm \"<classname options>\""));

		result.addElement(new Option(
			"\tThe log file to log the messages to.\n"
				+ "\t(default: none)",
			"log-file", 1, "-log-file <filename>"));

		en = super.listOptions();
		while (en.hasMoreElements())
			result.addElement(en.nextElement());

		return result.elements();
	}

	/**
	 * returns the options of the current setup.
	 *
	 * @return		the current options
	 */
	@Override
	public String[] getOptions() {
		int       		i;
		Vector<String>    	result;
		String[] options;

		result = new Vector<String>();

		result.add("-E");
		result.add("" + getEvaluation());

		for (i = 0; i < getSearchParameters().length; i++) {
			result.add("-search");
			result.add(getCommandline(getSearchParameters()[i]));
		}

		result.add("-algorithm");
		result.add(getCommandline(m_Algorithm));

		result.add("-log-file");
		result.add("" + getLogFile());

		options = super.getOptions();
		for (i = 0; i < options.length; i++)
			result.add(options[i]);

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Parses the options for this object.
	 *
	 * @param options	the options to use
	 * @throws Exception	if setting of options fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		String		tmpStr;
		String[]		tmpOptions;
		Vector<String>	search;
		int			i;
		AbstractParameter[]	params;

		tmpStr = Utils.getOption('E', options);
		if (tmpStr.length() != 0)
			setEvaluation(new SelectedTag(tmpStr, m_Metrics.getTags()));
		else
			setEvaluation(new SelectedTag(m_Metrics.getDefaultMetric(), m_Metrics.getTags()));

		search = new Vector<String>();
		do {
			tmpStr = Utils.getOption("search", options);
			if (tmpStr.length() > 0)
				search.add(tmpStr);
		}
		while (tmpStr.length() > 0);
		if (search.size() == 0) {
			for (i = 0; i < m_DefaultParameters.length; i++)
				search.add(getCommandline(m_DefaultParameters[i]));
		}
		params = new AbstractParameter[search.size()];
		for (i = 0; i < search.size(); i++) {
			tmpOptions    = Utils.splitOptions(search.get(i));
			tmpStr        = tmpOptions[0];
			tmpOptions[0] = "";
			params[i]     = (AbstractParameter) Utils.forName(AbstractParameter.class, tmpStr, tmpOptions);
		}
		setSearchParameters(params);

		tmpStr = Utils.getOption("algorithm", options);
		if (!tmpStr.isEmpty()) {
			tmpOptions = Utils.splitOptions(tmpStr);
			tmpStr = tmpOptions[0];
			tmpOptions[0] = "";
			setAlgorithm((AbstractSearch) Utils.forName(AbstractSearch.class, tmpStr, tmpOptions));
		}
		else {
			setAlgorithm(new DefaultSearch());
		}

		tmpStr = Utils.getOption("log-file", options);
		if (tmpStr.length() != 0)
			setLogFile(new File(tmpStr));
		else
			setLogFile(new File(System.getProperty("user.dir")));

		super.setOptions(options);
	}

	/**
	 * Set the base learner.
	 *
	 * @param newClassifier 	the classifier to use.
	 */
	@Override
	public void setClassifier(Classifier newClassifier) {
		super.setClassifier(newClassifier);
		try {
			m_BestClassifier.classifier = AbstractClassifier.makeCopy(m_Classifier);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the explorer/experimenter gui
	 */
	public String searchParametersTipText() {
		return "Defines the search parameters.";
	}

	/**
	 * Sets the search parameters.
	 *
	 * @param value	the parameters
	 */
	public void setSearchParameters(AbstractParameter[] value) {
		m_Parameters = value;
	}

	/**
	 * Returns the search parameters.
	 *
	 * @return		the parameters
	 */
	public AbstractParameter[] getSearchParameters() {
		return m_Parameters;
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the explorer/experimenter gui
	 */
	public String algorithmTipText() {
		return "Defines the search algorithm.";
	}

	/**
	 * Sets the search algorithm.
	 *
	 * @param value	the algorithm
	 */
	public void setAlgorithm(AbstractSearch value) {
		m_Algorithm = value;
	}

	/**
	 * Returns the search algorithm.
	 *
	 * @return		the algorithm
	 */
	public AbstractSearch getAlgorithm() {
		return m_Algorithm;
	}

	/**
	 * Creates the default search algorithm.
	 *
	 * @return	the algorithm
	 */
	public AbstractSearch defaultAlgorithm() {
		DefaultSearch result;

		result = new DefaultSearch();

		return result;
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the explorer/experimenter gui
	 */
	public String evaluationTipText() {
		return
			"Sets the criterion for evaluating the classifier performance and "
				+ "choosing the best one.";
	}

	/**
	 * Returns the underlying tags.
	 *
	 * @return		the tags
	 */
	public Tag[] getMetricsTags() {
		return m_Metrics.getTags();
	}

	/**
	 * Sets the criterion to use for evaluating the classifier performance.
	 *
	 * @param value 	the evaluation criterion
	 */
	public void setEvaluation(SelectedTag value) {
		if (value.getTags() == m_Metrics.getTags()) {
			m_Evaluation = value.getSelectedTag().getID();
		}
	}

	/**
	 * Gets the criterion used for evaluating the classifier performance.
	 *
	 * @return 		the current evaluation criterion.
	 */
	public SelectedTag getEvaluation() {
		return new SelectedTag(m_Evaluation, m_Metrics.getTags());
	}

	/**
	 * Returns the tip text for this property.
	 *
	 * @return 		tip text for this property suitable for
	 * 			displaying in the explorer/experimenter gui
	 */
	public String logFileTipText() {
		return "The log file to log the messages to.";
	}

	/**
	 * Gets current log file.
	 *
	 * @return 		the log file.
	 */
	public File getLogFile() {
		return m_LogFile;
	}

	/**
	 * Sets the log file to use.
	 *
	 * @param value 	the log file.
	 */
	public void setLogFile(File value) {
		m_LogFile = value;
	}

	/**
	 * Returns the integer index.
	 *
	 * @param upper the maximum to use
	 * @return always 0
	 */
	@Override
	public int getClassLabelIndex(int upper) {
		return 0;
	}

	/**
	 * returns the best Classifier setup.
	 *
	 * @return		the best Classifier setup
	 */
	public Classifier getBestClassifier() {
		return m_BestClassifier.classifier;
	}

	/**
	 * Returns the setup generator.
	 *
	 * @return		the generator
	 */
	public SetupGenerator getGenerator() {
		return m_Generator;
	}

	/**
	 * Returns an enumeration of the measure names.
	 *
	 * @return an enumeration of the measure names
	 */
	public Enumeration enumerateMeasures() {
		Vector	result;
		int		i;

		result = new Vector();

		if (getBestValues() != null) {
			for (i = 0; i < getBestValues().dimensions(); i++) {
				if (getBestValues().getValue(i) instanceof Double)
					result.add("measure-" + i);
			}
		}

		return result.elements();
	}

	/**
	 * Returns the value of the named measure.
	 *
	 * @param measureName the name of the measure to query for its value
	 * @return the value of the named measure
	 */
	public double getMeasure(String measureName) {
		if (measureName.startsWith("measure-"))
			return (Double) getBestValues().getValue(Integer.parseInt(measureName.replace("measure-", "")));
		else
			throw new IllegalArgumentException("Measure '" + measureName + "' not supported!");
	}

	/**
	 * Returns the evaluation factory to use.
	 *
	 * @return		the factory
	 */
	protected AbstractEvaluationFactory newFactory() {
		return new MekaEvaluationFactory();
	}

	/**
	 * Returns the factory instance.
	 *
	 * @return		the factory
	 */
	public AbstractEvaluationFactory getFactory() {
		return m_Factory;
	}

	/**
	 * Returns the evaluation metrics.
	 *
	 * @return		the metrics
	 */
	public AbstractEvaluationMetrics getMetrics() {
		return m_Metrics;
	}

	/**
	 * returns the parameter values that were found to work best.
	 *
	 * @return		the best parameter combination
	 */
	public Point<Object> getBestValues() {
		return m_BestClassifier.values;
	}

	/**
	 * returns the points that were found to work best.
	 *
	 * @return		the best points
	 */
	public Point<Object> getBestCoordinates() {
		return m_BestClassifier.performance.getValues();
	}

	/**
	 * Returns default capabilities of the classifier.
	 *
	 * @return		the capabilities of this classifier
	 */
	@Override
	public Capabilities getCapabilities() {
		Capabilities result;
		Capabilities classes;
		Iterator		iter;
		Capability capab;

		result = super.getCapabilities();

		// only nominal and numeric classes allowed
		classes = result.getClassCapabilities();
		iter = classes.capabilities();
		while (iter.hasNext()) {
			capab = (Capability) iter.next();
			if (    (capab != Capability.BINARY_CLASS)
				&& (capab != Capability.NOMINAL_CLASS)
				&& (capab != Capability.NUMERIC_CLASS)
				&& (capab != Capability.DATE_CLASS) )
				result.disable(capab);
		}

		// set dependencies
		for (Capability cap: Capability.values())
			result.enableDependency(cap);

		if (result.getMinimumNumberInstances() < 1)
			result.setMinimumNumberInstances(1);

		result.setOwner(this);

		return result;
	}

	/**
	 * Returns the commandline of the given object.
	 *
	 * @param obj		the object to create the commandline for
	 * @return		the commandline
	 */
	public String getCommandline(Object obj) {
		String	result;

		result = obj.getClass().getName();
		if (obj instanceof OptionHandler)
			result += " " + Utils.joinOptions(((OptionHandler) obj).getOptions());

		return result.trim();
	}

	/**
	 * prints the specified message to stdout if debug is on and can also dump
	 * the message to a log file.
	 *
	 * @param message	the message to print or store in a log file
	 */
	public void log(String message) {
		log(message, false);
	}

	/**
	 * prints the specified message to stdout if debug is on and can also dump
	 * the message to a log file.
	 *
	 * @param message	the message to print or store in a log file
	 * @param onlyLog	if true the message will only be put into the log file
	 * 			but not to stdout
	 */
	public void log(String message, boolean onlyLog) {
		// print to stdout?
		if (getDebug() && (!onlyLog))
			System.out.println(message);

		// log file?
		if (!getLogFile().isDirectory())
			Debug.writeToFile(getLogFile().getAbsolutePath(), message, true);
	}

	/**
	 * generates a table string for all the performances in the space and returns
	 * that.
	 *
	 * @param space		the current space to align the performances to
	 * @param performances	the performances to align
	 * @param type		the type of performance
	 * @return			the table string
	 */
	public String logPerformances(Space space, Vector<Performance> performances, Tag type) {
		StringBuffer	result;
		int			i;

		result = new StringBuffer(type.getReadable() + ":\n");

		result.append(space.toString());
		result.append("\n");
		for (i = 0; i < performances.size(); i++) {
			result.append(performances.get(i).getPerformance(type.getID()));
			result.append("\n");
		}
		result.append("\n");

		return result.toString();
	}

	/**
	 * aligns all performances in the space and prints those tables to the log
	 * file.
	 *
	 * @param space		the current space to align the performances to
	 * @param performances	the performances to align
	 */
	public void logPerformances(Space space, Vector<Performance> performances) {
		int		i;

		for (i = 0; i < m_Metrics.getTags().length; i++)
			log("\n" + logPerformances(space, performances, m_Metrics.getTags()[i]), true);
	}

	/**
	 * Returns the size of m_Trace, which is technically the amount of
	 * setups that where tested in order to find the best.
	 */
	public int getTraceSize() {
		return m_Trace.size();
	}

	/**
	 * Returns the CLI string of a given item in the trace.
	 *
	 * @param index the index of the trace item to obtain
	 */
	public String getTraceClassifierAsCli(int index) {
		return getCommandline(m_Trace.get(index).getValue().getClassifier());
	}

	/**
	 * Returns the performance score of a given item in the trace.
	 *
	 * @param index the index of the trace item to obtain
	 */
	public Double getTraceValue(int index) {
		return m_Trace.get(index).getValue().getPerformance();
	}

	/**
	 * Returns the parameter settings in structured way
	 *
	 * @param index the index of the trace item to obtain
	 * @return the parameter settings
	 */
	public List<Entry<String, Object>> getTraceParameterSettings(int index) {
		List<Entry<String, Object>> parameterSettings = new ArrayList<Entry<String,Object>>();
		List<String> dimensions = m_Algorithm.getSearchDimensions();
		for (int i = 0; i < dimensions.size(); ++i) {
			String parameter = dimensions.get(i);
			Object value = m_Trace.get(index).getValue().getValues().getValue(i);
			Entry<String, Object> current = new AbstractMap.SimpleEntry<String,Object>(parameter,value);
			parameterSettings.add(i, current);
		}

		return parameterSettings;
	}

	/**
	 * Returns the folds of a given item in the trace.
	 *
	 * @param index the index of the trace item to obtain
	 */
	public Integer getTraceFolds(int index) {
		return m_Trace.get(index).getKey();
	}

	/**
	 * Returns the full trace.
	 */
	public List<Entry<Integer, Performance>> getTrace() {
		return m_Trace;
	}

	/**
	 * Groups the parameters, i.e., when using ParameterGroup objects.
	 *
	 * @return		the groups
	 */
	protected List<AbstractParameter[]> groupParameters() {
		List<AbstractParameter[]>	result;
		int				groupCount;
		int				i;

		result = new ArrayList<AbstractParameter[]>();

		groupCount = 0;
		for (i = 0; i < m_Parameters.length; i++) {
			if (m_Parameters[i] instanceof ParameterGroup)
				groupCount++;
		}
		if ((groupCount > 0) && (m_Parameters.length != groupCount))
			throw new IllegalStateException(
				"Cannot mix " + ParameterGroup.class.getName() + " with other parameter types!");

		if (groupCount > 0) {
			for (i = 0; i < m_Parameters.length; i++)
				result.add(((ParameterGroup) m_Parameters[i]).getParameters());
		}
		else {
			result.add(m_Parameters);
		}

		return result;
	}

	/**
	 * TestCapabilities.
	 * Make sure the training data is suitable.
	 * @param D	the data
	 */
	public void testCapabilities(Instances D) throws Exception {
		// get the classifier's capabilities, enable all class attributes and do the usual test
		Capabilities cap = getCapabilities();
		cap.enableAllClasses();
		//getCapabilities().testWithFail(D);
		// get the capabilities again, test class attributes individually
		int L = D.classIndex();
		for(int j = 0; j < L; j++) {
			Attribute c = D.attribute(j);
			cap.testWithFail(c,true);
		}
	}

	/**
	 * builds the classifier.
	 *
	 * @param data        the training instances
	 * @throws Exception  if something goes wrong
	 */
	public void buildClassifier(Instances data) throws Exception {
		int				i;
		SearchResult result;
		List<AbstractParameter[]>	groups;
		List<SearchResult>		results;
		PerformanceComparator comp;

		// can classifier handle the data?
		testCapabilities(data);

		// remove instances with missing class
		data = new Instances(data);
		data.deleteWithMissingClass();

		m_Trace.clear();
		groups  = groupParameters();
		results = new ArrayList<SearchResult>();
		for (i = 0; i < groups.size(); i++) {
			if (groups.size() > 1)
				log("\n---> group #" + (i+1));

			m_Generator = new SetupGenerator();
			m_Generator.setBaseObject(this);
			m_Generator.setParameters(groups.get(i).clone());
			m_Generator.setBaseObject((Serializable) getClassifier());

			m_Algorithm.setOwner(this);
			result = m_Algorithm.search(data);
			results.add(result);

			m_Trace.addAll(m_Algorithm.getTrace());
		}

		// find best classifier among groups
		result = results.get(0);
		if (results.size() > 1) {
			comp = new PerformanceComparator(getEvaluation().getSelectedTag().getID(), getMetrics());
			for (i = 1; i < results.size(); i++) {
				if (comp.compare(results.get(i).performance, result.performance) < 0)
					result = results.get(i);
			}
		}
		m_BestClassifier = result;

		// train classifier
		log("\n---> train best - start");
		log(Utils.toCommandLine(m_BestClassifier));
		m_Classifier = AbstractClassifier.makeCopy(m_BestClassifier.classifier);
		m_Classifier.buildClassifier(data);

		log("\n---> train best - end");

		if (m_Debug) {
			log("\n---> Trace (format: #. folds/performance - setup)");
			for (i = 0; i < getTraceSize(); i++)
				log((i + 1) + ". " + getTraceFolds(i) + "/" + getTraceValue(i) + " - " + getTraceClassifierAsCli(i));
		}
	}

	/**
	 * Returns the distribution for the given instance.
	 *
	 * @param instance 	the test instance
	 * @return 		the distribution array
	 * @throws Exception 	if distribution can't be computed successfully
	 */
	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		return m_Classifier.distributionForInstance(instance);
	}

	/**
	 * returns a string representation of the classifier.
	 *
	 * @return a string representation of the classifier
	 */
	@Override
	public String toString() {
		StringBuilder	result;
		int			i;

		result = new StringBuilder();

		if (getBestValues() == null) {
			result.append("No search performed yet.");
		}
		else {
			result.append(this.getClass().getName() + ":\n"
				+ "Classifier: " + getCommandline(getBestClassifier()) + "\n\n");
			for (i = 0; i < m_Parameters.length; i++)
				result.append((i+1) + ". parameter: " + m_Parameters[i] + "\n");
			result.append("Evaluation: " + getEvaluation().getSelectedTag().getReadable() + "\n"
				+ "Coordinates: " + getBestCoordinates() + "\n");

			result.append("Values: " + getBestValues() + "\n\n" + m_Classifier.toString());

			if (m_Debug) {
				result.append("\n\nTrace (format: #. folds/performance - setup):\n");
				for (i = 0; i < getTraceSize(); i++) {
					result.append("\n" + (i + 1) + ". " + getTraceFolds(i) + "/" + getTraceValue(i) + " - " + getTraceClassifierAsCli(i));
				}
			}
		}

		return result.toString();
	}

	/**
	 * Returns a string that summarizes the object.
	 *
	 * @return 		the object summarized as a string
	 */
	public String toSummaryString() {
		String	result;

		result = "Best classifier: " + getCommandline(getBestClassifier());

		return result;
	}

	/**
	 * Returns a string representation of the model.
	 *
	 * @return      the model
	 */
	public String getModel() {
		return toString();
	}
}
