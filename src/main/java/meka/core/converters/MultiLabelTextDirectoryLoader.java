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
 * MultiLabelTextDirectoryLoader.java
 * Copyright (C) 2006-2017 University of Waikato, Hamilton, New Zealand
 *
 */

package meka.core.converters;

import weka.core.Attribute;
import weka.core.CommandlineRunnable;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.AbstractLoader;
import weka.core.converters.BatchConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Loads text files in a directory.<br>
 * First sub-directory level is used for the class attribute names, the second level for the labels for each class attribute.<br>
 * <br>
 * Example:<br>
 * /text-dataset<br>
 *   /class1<br>
 *     /0<br>
 *       3.txt<br>
 *       5.txt<br>
 *     /1<br>
 *       1.txt<br>
 *       2.txt<br>
 *       4.txt<br>
 *  /class2<br>
 *     /0<br>
 *       1.txt<br>
 *       4.txt<br>
 *     /1<br>
 *       2.txt<br>
 *       3.txt<br>
 *       5.txt<br>
 * <br>
 * Will generate something like this:<br>
 * <br>
 * &#64;relation 'example: -C 2'<br>
 * <br>
 * &#64;attribute &#64;&#64;class-class1&#64;&#64; {0,1}<br>
 * &#64;attribute &#64;&#64;class-class2&#64;&#64; {0,1}<br>
 * &#64;attribute file-ID string<br>
 * &#64;attribute text string<br>
 * <br>
 * &#64;data<br>
 * 1,0,1.txt,'file 1\n'<br>
 * 1,1,2.txt,'file 2\n'<br>
 * 0,1,3.txt,'file 3\n'<br>
 * 1,0,4.txt,'file 4\n'<br>
 * 0,1,5.txt,'file 5\n'<br>
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 *
 * <pre> -D
 *  Enables debug output.
 *  (default: off)</pre>
 *
 * <pre> -F
 *  Stores the filename in an additional attribute.
 *  (default: off)</pre>
 *
 * <pre> -dir &lt;directory&gt;
 *  The directory to work on.
 *  (default: current directory)</pre>
 *
 * <pre> -charset &lt;charset name&gt;
 *  The character set to use, e.g UTF-8.
 *  (default: use the default character set)</pre>
 *
 <!-- options-end -->
 *
 * Based on code from Weka's TextDirectoryLoader
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class MultiLabelTextDirectoryLoader
	extends AbstractLoader
	implements BatchConverter, OptionHandler, CommandlineRunnable {

	/** for serialization */
	private static final long serialVersionUID = 2592118773712247647L;

	public static final String FILE_ID = "file-ID";

	public static final String TEXT = "text";

	/** Holds the determined structure (header) of the data set. */
	protected Instances m_structure = null;

	/** Holds the source of the data set. */
	protected File m_sourceFile = new File(System.getProperty("user.dir"));

	/** whether to print some debug information */
	protected boolean m_Debug = false;

	/** whether to include the filename as an extra attribute */
	protected boolean m_OutputFilename = false;

	/**
	 * The charset to use when loading text files (default is to just use the
	 * default charset).
	 */
	protected String m_charSet = "";

	/**
	 * default constructor
	 */
	public MultiLabelTextDirectoryLoader() {
		// No instances retrieved yet
		setRetrieval(NONE);
	}

	/**
	 * Returns a string describing this loader
	 *
	 * @return a description of the evaluator suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return
			"Loads text files in a directory.\n"
				+ "First sub-directory level is used for the class attribute names, "
				+ "the second level for the labels for each class attribute.\n"
				+ "\n"
				+ "Example:\n"
				+ "/text-dataset\n"
				+ "  /class1\n"
				+ "    /0\n"
				+ "      3.txt\n"
				+ "      5.txt\n"
				+ "    /1\n"
				+ "      1.txt\n"
				+ "      2.txt\n"
				+ "      4.txt\n"
				+ " /class2\n"
				+ "    /0\n"
				+ "      1.txt\n"
				+ "      4.txt\n"
				+ "    /1\n"
				+ "      2.txt\n"
				+ "      3.txt\n"
				+ "      5.txt\n"
				+ "\n"
				+ "Will generate something like this:\n\n"
				+ "@relation 'example: -C 2'\n"
				+ "\n"
				+ "@attribute @@class-class1@@ {0,1}\n"
				+ "@attribute @@class-class2@@ {0,1}\n"
				+ "@attribute file-ID string\n"
				+ "@attribute text string\n"
				+ "\n"
				+ "@data\n"
				+ "1,0,1.txt,'file 1\\n'\n"
				+ "1,1,2.txt,'file 2\\n'\n"
				+ "0,1,3.txt,'file 3\\n'\n"
				+ "1,0,4.txt,'file 4\\n'\n"
				+ "0,1,5.txt,'file 5\\n'\n";

	}

	/**
	 * Lists the available options
	 *
	 * @return an enumeration of the available options
	 */
	@Override
	public Enumeration<Option> listOptions() {

		Vector<Option> result = new Vector<Option>();

		result.add(new Option("\tEnables debug output.\n" + "\t(default: off)",
			"D", 0, "-D"));

		result.add(new Option("\tStores the filename in an additional attribute.\n"
			+ "\t(default: off)", "F", 0, "-F"));

		result.add(new Option("\tThe directory to work on.\n"
			+ "\t(default: current directory)", "dir", 0, "-dir <directory>"));

		result.add(new Option("\tThe character set to use, e.g UTF-8.\n\t"
			+ "(default: use the default character set)", "charset", 1,
			"-charset <charset name>"));

		return result.elements();
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 *
	 <!-- options-start -->
	 * Valid options are: <p>
	 *
	 * <pre> -D
	 *  Enables debug output.
	 *  (default: off)</pre>
	 *
	 * <pre> -F
	 *  Stores the filename in an additional attribute.
	 *  (default: off)</pre>
	 *
	 * <pre> -dir &lt;directory&gt;
	 *  The directory to work on.
	 *  (default: current directory)</pre>
	 *
	 * <pre> -charset &lt;charset name&gt;
	 *  The character set to use, e.g UTF-8.
	 *  (default: use the default character set)</pre>
	 *
	 <!-- options-end -->
	 *
	 * @param options the options
	 * @throws Exception if options cannot be set
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		setDebug(Utils.getFlag("D", options));

		setOutputFilename(Utils.getFlag("F", options));

		setDirectory(new File(Utils.getOption("dir", options)));

		String charSet = Utils.getOption("charset", options);
		m_charSet = "";
		if (charSet.length() > 0) {
			m_charSet = charSet;
		}
	}

	/**
	 * Gets the setting
	 *
	 * @return the current setting
	 */
	@Override
	public String[] getOptions() {
		Vector<String> options = new Vector<String>();

		if (getDebug()) {
			options.add("-D");
		}

		if (getOutputFilename()) {
			options.add("-F");
		}

		options.add("-dir");
		options.add(getDirectory().getAbsolutePath());

		if (m_charSet != null && m_charSet.length() > 0) {
			options.add("-charset");
			options.add(m_charSet);
		}

		return options.toArray(new String[options.size()]);
	}

	/**
	 * the tip text for this property
	 *
	 * @return the tip text
	 */
	public String charSetTipText() {
		return "The character set to use when reading text files (eg UTF-8) - leave"
			+ " blank to use the default character set.";
	}

	/**
	 * Set the character set to use when reading text files (an empty string
	 * indicates that the default character set will be used).
	 *
	 * @param charSet the character set to use.
	 */
	public void setCharSet(String charSet) {
		m_charSet = charSet;
	}

	/**
	 * Get the character set to use when reading text files. An empty string
	 * indicates that the default character set will be used.
	 *
	 * @return the character set name to use (or empty string to indicate that the
	 *         default character set will be used).
	 */
	public String getCharSet() {
		return m_charSet;
	}

	/**
	 * Sets whether to print some debug information.
	 *
	 * @param value if true additional debug information will be printed.
	 */
	public void setDebug(boolean value) {
		m_Debug = value;
	}

	/**
	 * Gets whether additional debug information is printed.
	 *
	 * @return true if additional debug information is printed
	 */
	public boolean getDebug() {
		return m_Debug;
	}

	/**
	 * the tip text for this property
	 *
	 * @return the tip text
	 */
	public String debugTipText() {
		return "Whether to print additional debug information to the console.";
	}

	/**
	 * Sets whether the filename will be stored as an extra attribute.
	 *
	 * @param value if true the filename will be stored in an extra attribute
	 */
	public void setOutputFilename(boolean value) {
		m_OutputFilename = value;
		reset();
	}

	/**
	 * Gets whether the filename will be stored as an extra attribute.
	 *
	 * @return true if the filename is stored in an extra attribute
	 */
	public boolean getOutputFilename() {
		return m_OutputFilename;
	}

	/**
	 * the tip text for this property
	 *
	 * @return the tip text
	 */
	public String outputFilenameTipText() {
		return "Whether to store the filename in an additional attribute.";
	}

	/**
	 * Returns a description of the file type, actually it's directories.
	 *
	 * @return a short file description
	 */
	public String getFileDescription() {
		return "Directories";
	}

	/**
	 * get the Dir specified as the source
	 *
	 * @return the source directory
	 */
	public File getDirectory() {
		return new File(m_sourceFile.getAbsolutePath());
	}

	/**
	 * sets the source directory
	 *
	 * @param dir the source directory
	 * @throws IOException if an error occurs
	 */
	public void setDirectory(File dir) throws IOException {
		setSource(dir);
	}

	/**
	 * Resets the loader ready to read a new data set
	 */
	@Override
	public void reset() {
		m_structure = null;
		setRetrieval(NONE);
	}

	/**
	 * Resets the Loader object and sets the source of the data set to be the
	 * supplied File object.
	 *
	 * @param dir the source directory.
	 * @throws IOException if an error occurs
	 */
	@Override
	public void setSource(File dir) throws IOException {
		reset();

		if (dir == null) {
			throw new IOException("Source directory object is null!");
		}

		m_sourceFile = dir;
		if (!dir.exists() || !dir.isDirectory()) {
			throw new IOException("Directory '" + dir + "' not found");
		}
	}

	/**
	 * Generates a class attribute name which should avoid clashes with
	 * attribute names generated by StringToWordVector filter.
	 *
	 * @param name  the name
	 * @return      the generated name
	 */
	protected String createClassAttributeName(String name) {
		return "@@class-" + name + "@@";
	}

	/**
	 * Determines and returns (if possible) the structure (internally the header)
	 * of the data set as an empty set of instances.
	 *
	 * @return the structure of the data set as an empty set of Instances
	 * @throws IOException if an error occurs
	 */
	@Override
	public Instances getStructure() throws IOException {
		if (getDirectory() == null) {
			throw new IOException("No directory/source has been specified");
		}

		// determine class labels, i.e., sub-dirs
		if (m_structure == null) {
			ArrayList<Attribute> atts = new ArrayList<Attribute>();
			ArrayList<String> classes = new ArrayList<String>();
			HashMap<String,ArrayList<String>> classLabels = new HashMap<String,ArrayList<String>>();

			// iterate class attributes
			String[] subdirs = getDirectory().list();
			for (String subdir2 : subdirs) {
				File subdir = new File(getDirectory().getAbsolutePath() + File.separator + subdir2);
				if (subdir.isDirectory()) {
					classes.add(subdir2);
					classLabels.put(subdir2, new ArrayList<String>());
					// iterate class labels
					String[] subsubdirs = subdir.list();
					for (String subsubdir2: subsubdirs) {
						File subsubdir = new File(subdir.getAbsolutePath() + File.separator + subsubdir2);
						if (subsubdir.isDirectory())
							classLabels.get(subdir2).add(subsubdir2);
					}
				}
			}

			// make sure that the names of the class attributes are unlikely to
			// clash with any attribute created via the StringToWordVector filter
			List<String> classesSorted = new ArrayList<String>(classLabels.keySet());
			Collections.sort(classesSorted);
			for (String key: classesSorted) {
				Collections.sort(classLabels.get(key));
				atts.add(new Attribute(createClassAttributeName(key), classLabels.get(key)));
			}
			if (m_OutputFilename)
				atts.add(new Attribute(FILE_ID, (ArrayList<String>) null));
			atts.add(new Attribute(TEXT, (ArrayList<String>) null));

			String relName = getDirectory().getAbsolutePath().replaceAll("/", "_");
			relName = relName.replaceAll("\\\\", "_").replaceAll(":", "_");
			m_structure = new Instances(relName + ": -C " + classesSorted.size(), atts, 0);
			m_structure.setClassIndex(m_structure.numAttributes() - 1);
		}

		return m_structure;
	}

	/**
	 * Return the full data set. If the structure hasn't yet been determined by a
	 * call to getStructure then method should do so before processing the rest of
	 * the data set.
	 *
	 * @return the structure of the data set as an empty set of Instances
	 * @throws IOException if there is no source or parsing fails
	 */
	@Override
	public Instances getDataSet() throws IOException {
		if (getDirectory() == null) {
			throw new IOException("No directory/source has been specified");
		}

		Instances data = getStructure();

		// ID -> file
		Map<String,File> fileIDs = new HashMap<String,File>();
		// class attr -> label -> files
		Map<String,Map<String,Set<String>>> files = new HashMap<String,Map<String,Set<String>>>();

		// iterate class attributes
		String[] subdirs = getDirectory().list();
		for (String subdir2 : subdirs) {
			File subdir = new File(getDirectory().getAbsolutePath() + File.separator + subdir2);
			if (subdir.isDirectory()) {
				files.put(subdir2, new HashMap<String,Set<String>>());
				// iterate class labels
				String[] subsubdirs = subdir.list();
				for (String subsubdir2: subsubdirs) {
					File subsubdir = new File(subdir.getAbsolutePath() + File.separator + subsubdir2);
					if (subsubdir.isDirectory()) {
						files.get(subdir2).put(subsubdir2, new HashSet<String>());
						// iterate files for label
						File[] labelFiles = subsubdir.listFiles();
						for (File labelFile: labelFiles) {
							if (!fileIDs.containsKey(labelFile.getName()))
								fileIDs.put(labelFile.getName(), labelFile);
							files.get(subdir2).get(subsubdir2).add(labelFile.getName());
						}
					}
				}
			}
		}

		List<String> fileIDsSorted = new ArrayList<>(fileIDs.keySet());
		Collections.sort(fileIDsSorted);
		for (String id: fileIDsSorted) {
			try {
				double[] values = new double[data.numAttributes()];
				Arrays.fill(values, Utils.missingValue());
				int index;

				// text
				File txt = fileIDs.get(id);
				BufferedReader is;
				if ((m_charSet == null) || (m_charSet.length() == 0))
					is = new BufferedReader(new InputStreamReader(new FileInputStream(txt)));
				else
					is = new BufferedReader(new InputStreamReader(new FileInputStream(txt), m_charSet));
				StringBuilder txtStr = new StringBuilder();
				int c;
				while ((c = is.read()) != -1)
					txtStr.append((char) c);
				index = data.attribute(TEXT).index();
				values[index] = data.attribute(index).addStringValue(txtStr.toString());

				// ID
				if (m_OutputFilename) {
					index = data.attribute(FILE_ID).index();
					values[index] = data.attribute(index).addStringValue(id);
				}

				// class attributes
				for (String cls : files.keySet()) {
					Attribute att = data.attribute(createClassAttributeName(cls));
					index = att.index();
					for (String lbl : files.get(cls).keySet()) {
						if (files.get(cls).get(lbl).contains(id)) {
							values[index] = att.indexOfValue(lbl);
							break;
						}
					}
				}

				// add instance
				Instance inst = new DenseInstance(1.0, values);
				data.add(inst);
			}
			catch (Exception e) {
				System.err.println("Failed to process file: " + id + ", " + fileIDs.get(id));
				e.printStackTrace();
			}
		}

		return data;
	}

	/**
	 * Process input directories/files incrementally.
	 *
	 * @param structure ignored
	 * @return never returns without throwing an exception
	 * @throws IOException if a problem occurs
	 */
	@Override
	public Instance getNextInstance(Instances structure) throws IOException {
		throw new IOException("MultiLabelTextDirectoryLoader can't read data sets incrementally.");
	}

	/**
	 * Returns the revision string.
	 *
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 12184 $");
	}

	/**
	 * Perform any setup stuff that might need to happen before commandline
	 * execution. Subclasses should override if they need to do something here
	 *
	 * @throws Exception if a problem occurs during setup
	 */
	@Override
	public void preExecution() throws Exception {
	}

	/**
	 * Perform any teardown stuff that might need to happen after execution.
	 * Subclasses should override if they need to do something here
	 *
	 * @throws Exception if a problem occurs during teardown
	 */
	@Override
	public void postExecution() throws Exception {
	}

	/**
	 * Execute the supplied object.
	 *
	 * @param toRun the object to execute
	 * @param args any options to pass to the object
	 * @throws Exception if a problem occurs.
	 */
	@Override
	public void run(Object toRun, String[] args) throws IllegalArgumentException {
		if (!(toRun instanceof MultiLabelTextDirectoryLoader))
			throw new IllegalArgumentException("Object to execute is not a MultiLabelTextDirectoryLoader!");

		MultiLabelTextDirectoryLoader loader = (MultiLabelTextDirectoryLoader) toRun;
		if (args.length > 0) {
			try {
				loader.setOptions(args);
				System.out.println(loader.getDataSet());
			}
			catch (Exception e) {
				System.err.println("Failed to set options: " + Utils.arrayToString(args));
				e.printStackTrace();
			}
		}
		else {
			System.err.println("\nUsage:\n" + "\tMultiLabelTextDirectoryLoader [options]\n" + "\n" + "Options:\n");
			Enumeration<Option> enm = new MultiLabelTextDirectoryLoader().listOptions();
			while (enm.hasMoreElements()) {
				Option option = enm.nextElement();
				System.err.println(option.synopsis());
				System.err.println(option.description());
			}
			System.err.println();
		}
	}

	/**
	 * Main method.
	 *
	 * @param args should contain the name of an input file.
	 */
	public static void main(String[] args) {
		MultiLabelTextDirectoryLoader loader = new MultiLabelTextDirectoryLoader();
		loader.run(loader, args);
	}
}
