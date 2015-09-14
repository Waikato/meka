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

package meka.classifiers.multilabel.meta;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import java.util.Arrays;

import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.core.A;
import meka.core.F;
import meka.core.MLUtils;
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
 * RandomSubspaceML.java - Subsample the attribute space and instance space randomly for each ensemble member. 
 * Basically a generalized version of Random Forests. It is computationally cheaper than EnsembleML for the same number of models.
 * <br>
 * As used with CC in: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer. Vol. 85(3), pp 333-359. (May 2011).
 * <br>
 * In earlier versions of Meka this class was called <i>BaggingMLq</i> and used Bagging procedure. Now it uses a simple ensemble cut.
 * <br>
 *
 * @author 	Jesse Read 
 * @version	June 2014
 */


public class RandomSubspaceML extends MetaProblemTransformationMethod implements TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = 3608541911971484299L;

	protected int m_AttSizePercent = 50;

	protected int m_IndicesCut[][] = null;
	protected Instances m_InstancesTemplates[] = null;
	protected Instance m_InstanceTemplates[] = null;

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		m_InstancesTemplates = new Instances[m_NumIterations];
		m_InstanceTemplates = new Instance[m_NumIterations];

		if (getDebug()) System.out.println("-: Models: ");

		m_Classifiers = ProblemTransformationMethod.makeCopies((ProblemTransformationMethod) m_Classifier, m_NumIterations);

		Random r = new Random(m_Seed);

		int N_sub = (D.numInstances()*m_BagSizePercent/100);

		int L = D.classIndex();
		int d = D.numAttributes() - L;
		int d_new = d * m_AttSizePercent / 100;
		m_IndicesCut = new int[m_NumIterations][];

		for(int i = 0; i < m_NumIterations; i++) {

			// Downsize the instance space (exactly like in EnsembleML.java)

			if (getDebug()) 
				System.out.print("\t"+(i+1)+": ");
			D.randomize(r);
			Instances D_cut = new Instances(D,0,N_sub);
			if (getDebug()) 
				System.out.print("N="+D.numInstances()+" -> N'="+D_cut.numInstances()+", ");

			// Downsize attribute space

			D_cut.setClassIndex(-1);
			int indices_a[] = A.make_sequence(L,d+L);
			A.shuffle(indices_a,r);
			indices_a = Arrays.copyOfRange(indices_a,0,d-d_new);
			Arrays.sort(indices_a);
			m_IndicesCut[i] = A.invert(indices_a,D.numAttributes());
			D_cut = F.remove(D_cut,indices_a,false);
			D_cut.setClassIndex(L);
			if (getDebug()) 
				System.out.print(" A:="+(D.numAttributes() - L)+" -> A'="+(D_cut.numAttributes() - L)+" ("+m_IndicesCut[i][L]+",...,"+m_IndicesCut[i][m_IndicesCut[i].length-1]+")");

			// Train multi-label classifier

			if (m_Classifiers[i] instanceof Randomizable) ((Randomizable)m_Classifiers[i]).setSeed(m_Seed+i);
			if(getDebug()) System.out.println(".");

			m_Classifiers[i].buildClassifier(D_cut);
			m_InstanceTemplates[i] = D_cut.instance(1);
			m_InstancesTemplates[i] = new Instances(D_cut,0);
		}
		if (getDebug()) System.out.println(":-");
	}


	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();
		double p[] = new double[L];

		for(int i = 0; i < m_NumIterations; i++) {
			// Use a template Instance from training, and copy values over
			// (this is faster than copying x and cutting it to shape)
			Instance x_ = (Instance) m_InstanceTemplates[i];
			MLUtils.copyValues(x_,x,m_IndicesCut[i]);
			x_.setDataset(m_InstancesTemplates[i]);

			// TODO, use generic voting scheme somewhere?
			double d[] = ((ProblemTransformationMethod)m_Classifiers[i]).distributionForInstance(x_);
			for(int j = 0; j < d.length; j++) {
				p[j] += d[j];
			}
		}

		return p;
	}

	@Override
	public Enumeration listOptions() {
		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSize of attribute space, as a percentage of total attribute space size (must be between 1 and 100, default: "+m_AttSizePercent+")", "A", 1, "-A <size percentage>"));
		Enumeration enu = super.listOptions();
		while (enu.hasMoreElements()) {
			newVector.addElement(enu.nextElement());
		}
		return newVector.elements();
	}

	@Override
    public void setOptions(String[] options) throws Exception {

        String tmpStr;

        tmpStr = Utils.getOption('A', options);
        if (tmpStr.length() != 0)
            setAttSizePercent(Integer.parseInt(tmpStr));

        super.setOptions(options);
    }

	@Override
	public String [] getOptions() {
		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 2];
		int current = 0;
		options[current++] = "-A";
		options[current++] = String.valueOf(m_AttSizePercent);
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new RandomSubspaceML(), args);
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

	/** 
     * Sets the percentage of attributes to sample from the original set.
     */
	public void setAttSizePercent(int value) {
		m_AttSizePercent = value;
	}

    /** 
     * Gets the percentage of attributes to sample from the original set.
     */
	public int getAttSizePercent() {
		return m_AttSizePercent;
	}
	
	public String attSizePercentTipText() {
		return "Size of attribute space, as a percentage of total attribute space size";
	}
}
