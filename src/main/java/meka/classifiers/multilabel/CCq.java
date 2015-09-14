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

package meka.classifiers.multilabel;

import meka.core.MLUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * The Classifier Chains  Method - Random Subspace ('quick') Version.
 * This version is able to downsample the number of training instances across the binary models.<br>
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer. Vol. 85(3), pp 333-359. (May 2011).
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version January 2009
 */
public class CCq extends ProblemTransformationMethod implements Randomizable, TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = 7881602808389873411L;

	/** The downsample ratio*/
	protected double m_DownSampleRatio = 0.75;

	/** The random generator */
	protected int m_S = 0;
	
	protected Random m_Random = new Random(m_S);

	/** The number of classes*/
	protected int m_NumClasses = -1;

	protected QLink root = null;

	protected class QLink {

		private QLink next = null;
		private Classifier classifier = null;
		public Instances _template = null;
		private int index = -1;
		private int excld[]; // to contain the indices to delete
		private int j = 0; //@temp

		public QLink(int chain[], int j, Instances train) throws Exception {
			this.j = j;

			this.index = chain[j];

			// sort out excludes [4|5,1,0,2,3]
			this.excld = Arrays.copyOfRange(chain,j+1,chain.length); 
			// sort out excludes [0,1,2,3,5]
			Arrays.sort(this.excld); 

			this.classifier = AbstractClassifier.forName(getClassifier().getClass().getName(),((AbstractClassifier)getClassifier()).getOptions());

			Instances new_train = new Instances(train);

			// delete all except one (leaving a binary problem)
			if(getDebug()) System.out.print(" "+this.index);
			new_train.setClassIndex(-1); 
			// delete all the attributes (and track where our index ends up)
			int c_index = chain[j]; 
			for(int i = excld.length-1; i >= 0; i--) {
				new_train.deleteAttributeAt(excld[i]);
				if (excld[i] < this.index)
					c_index--; 
			}
			new_train.setClassIndex(c_index); 

			/* BEGIN downsample for this link */
			new_train.randomize(m_Random);
			int numToRemove = new_train.numInstances() - (int)Math.round(new_train.numInstances() * m_DownSampleRatio);
			for(int i = 0, removed = 0; i < new_train.numInstances(); i++) {
				if (new_train.instance(i).classValue() <= 0.0) {
					new_train.instance(i).setClassMissing();
					if (++removed >= numToRemove)
						break;
				}
			}
			new_train.deleteWithMissingClass();
			/* END downsample for this link */

			_template = new Instances(new_train,0);

			this.classifier.buildClassifier(new_train);
			new_train = null;

			if(j+1 < chain.length) 
				next = new QLink(chain, ++j, train);
		}

		private void classify(Instance test) throws Exception {
			// copy
			Instance copy = (Instance)test.copy();
			copy.setDataset(null);

			// delete attributes we don't need
			for(int i = excld.length-1; i >= 0; i--) {
				copy.deleteAttributeAt(this.excld[i]);
			}

			//set template
			copy.setDataset(this._template);

			//set class
			test.setValue(this.index,(int)(this.classifier.classifyInstance(copy))); 

			//carry on
			if (next!=null) next.classify(test);
		}

		@Override
		public String toString() {
			return (next == null) ? String.valueOf(this.index) : String.valueOf(this.index)+">"+next.toString();
		}

	}

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"The Classifier Chains  Method - Random Subspace ('quick') Version.\n"
				+ "This version is able to downsample the number of training instances across the binary models."
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank");
		result.setValue(Field.TITLE, "Classifier Chains for Multi-label Classification");
		result.setValue(Field.JOURNAL, "Machine Learning Journal");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.VOLUME, "85");
		result.setValue(Field.NUMBER, "3");
		result.setValue(Field.PAGES, "333-359");
		
		return result;
	}

	public int getSeed() {
		return m_S;
	}

	public void setSeed(int s) {
		m_S = s;
		m_Random = new Random(m_S);
	}

	public String seedTipText() {
		return "The seed value for randomization.";
	}

	/** Set the downsample ratio  */
	public void setDownSampleRatio(double r) {
		m_DownSampleRatio = r;
	}

	/** Get the downsample ratio  */
	public double getDownSampleRatio() {
		return m_DownSampleRatio;
	}

	public String downSampleRatioTipText() {
		return "The down sample ratio (0-1).";
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the downsampling ratio        \n\tdefault: "+m_DownSampleRatio+"\t(of original)", "P", 1, "-P <value>"));
		newVector.addElement(new Option("\tThe seed value for randomization\n\tdefault: 0", "S", 1, "-S <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		String tmpStr; 
		tmpStr = Utils.getOption('P', options);
		if (tmpStr.length() != 0) 
			setDownSampleRatio(Double.parseDouble(tmpStr));
		else
		  setDownSampleRatio(0.75);

		tmpStr = Utils.getOption('S', options);
		if (tmpStr.length() > 0)
			setSeed(Integer.parseInt(tmpStr));
		else
			setSeed(0);

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 2];
		int current = 0;
		options[current++] = "-P";
		options[current++] = "" + m_DownSampleRatio;
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;

	}

	@Override
	public void buildClassifier(Instances Train) throws Exception {
	  	testCapabilities(Train);
	  	
		this.m_NumClasses = Train.classIndex();

		int indices[] = MLUtils.gen_indices(m_NumClasses);
		MLUtils.randomize(indices,new Random(m_S));
		if(getDebug()) System.out.print(":- Chain (");
		root = new QLink(indices,0,Train);
		if (getDebug()) System.out.println(" ) -:");
	}

	@Override
	public double[] distributionForInstance(Instance test) throws Exception {
		root.classify(test);
		return MLUtils.toDoubleArray(test,m_NumClasses);
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new CCq(), args);
	}
}
