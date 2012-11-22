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

package weka.classifiers.multilabel.meta;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

/**
 * RandomSupspaceML.java - Downsize the attribute space randomly for each ensemble member.
 * <br>
 * As used with CC in: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer. Vol. 85(3), pp 333-359. (May 2011).
 * <br>
 * Previously this class was called <i>BaggingMLq</i>.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */

public class RandomSubspaceML extends MultilabelMetaClassifier implements TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = 3608541911971484299L;

	protected int m_AttSizePercent = 50;

	protected ArrayList m_Indices[] = null;
	protected Instances m_Dsets[] = null;

	@Override
	public void buildClassifier(Instances train) throws Exception {
	  	testCapabilities(train);
	  	
		m_Indices = new ArrayList[m_NumIterations];
		m_Dsets = new Instances[m_NumIterations];

		if (getDebug()) System.out.print("-: Models: ");

		m_Classifiers = AbstractClassifier.makeCopies(m_Classifier,m_NumIterations);

		for(int i = 0; i < m_NumIterations; i++) {
			Random r = new Random(m_Seed+i);
			// DOWNSIZE ATTRIBUTE SPACE
			m_Indices[i] = new ArrayList<Integer>();
			Instances trainCut = new Instances(train);
			trainCut.setClassIndex(-1);
			double d = m_AttSizePercent / 100.0;
			for(int a = trainCut.numAttributes()-1; a >= train.classIndex(); a--) {
				if (r.nextDouble() > d && !m_Indices[i].contains(a)) {
					m_Indices[i].add(a);
				}
			}
			for(Object a : m_Indices[i]) {
				trainCut.deleteAttributeAt((Integer)a);
			}
			Instances bag = new Instances(trainCut,0);
			m_Dsets[i] = bag;
			// END 
			if (m_Classifiers[i] instanceof Randomizable) ((Randomizable)m_Classifiers[i]).setSeed(m_Seed+i);
			if(getDebug()) System.out.print(""+i+" ");

			int ixs[] = new int[trainCut.numInstances()];
			for(int j = 0; j < ixs.length; j++) {
				ixs[r.nextInt(ixs.length)]++;
			}
			for(int j = 0; j < ixs.length; j++) {
				if (ixs[j] > 0) {
					Instance instance = trainCut.instance(j);
					instance.setWeight(ixs[j]);
					bag.add(instance);
				}
			}

			//
			bag.setClassIndex(train.classIndex());
			m_Classifiers[i].buildClassifier(bag);
		}
		if (getDebug()) System.out.println(":-");
	}


	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {

		double r[] = new double[instance.classIndex()];

		for(int i = 0; i < m_NumIterations; i++) {
			// DOWNSIZE ATTRIBUTE SPACE
			Instance copy = (Instance) instance.copy(); 
			copy.setDataset(null);
			for(Object a : m_Indices[i]) 
				copy.deleteAttributeAt((Integer)a);
			copy.setDataset(m_Dsets[i]);
			//
			double d[] = ((MultilabelClassifier)m_Classifiers[i]).distributionForInstance(copy);
			for(int j = 0; j < d.length; j++) {
				r[j] += d[j];
			}
		}

		return r;
	}

	@Override
	public Enumeration listOptions() {
		Vector newVector = new Vector();
		newVector.addElement(new Option("\t@Size of attribute space, as a percentage of total attribute space size (default "+m_AttSizePercent+")", "A", 1, "-A <size percentage>"));
		Enumeration enu = super.listOptions();
		while (enu.hasMoreElements()) {
			newVector.addElement(enu.nextElement());
		}
		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		try { m_AttSizePercent = Integer.parseInt(Utils.getOption('A',options)); } catch(Exception e) { }
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 2];
		int current = 0;
		options[current++] = "-A";
		options[current++] = String.valueOf(m_BagSizePercent);
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new RandomSubspaceML(),args);
	}

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "Combining several multi-label classifiers in an ensemble where the attribute space for each model is a random subset of the original space.";
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

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}
	
	public void setAttSizePercent(int value) {
	  if ((value > 0) && (value <= 100))
	    m_AttSizePercent = value;
	  else {
		  System.err.println("Bad percentage (must be between 1 and 100");
	  }
	}
	
	public int getAttSizePercent() {
	  return m_AttSizePercent;
	}
	
	public String attSizePercentTipText() {
	  return "Size of attribute space, as a percentage of total attribute space size";
	}
}
