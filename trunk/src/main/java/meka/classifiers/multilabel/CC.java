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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;
import meka.core.CCUtils;
import meka.core.MLUtils;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

/**
 * CC.java - The Classifier Chains Method.
 * <br>
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer. Vol. 85(3), pp 333-359. (May 2011).
 * <br>
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. In Proc. of 20th European Conference on Machine Learning (ECML 2009). Bled, Slovenia, September 2009.
 * <br>
 *
 * This class will be replaced by CCe, an extended version which makes use of the 'CNode' class and is more suitable for probabilistic classification, and more flexible so that it can be used easily in related schemes.
 * This class will stay for now until it is certain that published results can be reproduced exactly with CCe.
 *
 * @see meka.classifiers.multilabel.CCe
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 * @version January 2009
 */
@Deprecated
public class CC extends MultilabelClassifier implements Randomizable, TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = 9045384089402626007L;
	
	protected Link root = null;

	protected class Link implements Serializable {

		private Link next = null;
		private AbstractClassifier classifier = null;
		public Instances _template = null;
		private int index = -1;
		private int excld[]; // to contain the indices to delete
		private int j = 0; //@temp

		public Link(int chain[], int j, Instances train) throws Exception {
			this.j = j;

			this.index = chain[j];

			// sort out excludes [4|5,1,0,2,3]
			this.excld = Arrays.copyOfRange(chain,j+1,chain.length); 
			// sort out excludes [0,1,2,3,5]
			Arrays.sort(this.excld); 


			//Instances new_train = new Instances(train);
			Instances new_train = CCUtils.linkTransform(train,j,this.index,this.excld);

			_template = new Instances(new_train,0);

			this.classifier = (AbstractClassifier)AbstractClassifier.forName(getClassifier().getClass().getName(),((AbstractClassifier)getClassifier()).getOptions());
			this.classifier.buildClassifier(new_train);
			new_train = null;

			if(j+1 < chain.length) 
				next = new Link(chain, ++j, train);
		}

		protected void classify(Instance test) throws Exception {
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

	protected int m_S = 0;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"The Classifier Chains Method."
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		TechnicalInformation	additional;
		
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank");
		result.setValue(Field.TITLE, "Classifier Chains for Multi-label Classification");
		result.setValue(Field.JOURNAL, "Machine Learning Journal");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.VOLUME, "85");
		result.setValue(Field.NUMBER, "3");
		result.setValue(Field.PAGES, "333-359");
		
		additional = new TechnicalInformation(Type.INPROCEEDINGS);
		additional.setValue(Field.AUTHOR, "Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank");
		additional.setValue(Field.TITLE, "Classifier Chains for Multi-label Classification");
		additional.setValue(Field.BOOKTITLE, "20th European Conference on Machine Learning (ECML 2009). Bled, Slovenia, September 2009");
		additional.setValue(Field.YEAR, "2009");

		result.add(additional);
    
		return result;
	}

	public void setSeed(int s) {
		m_S = s;
	}

	public int getSeed() {
		return m_S;
	}

	public String seedTipText() {
	  return "The seed value for randomizing the data.";
	}

	protected int m_Chain[] = null;

	public void setChain(int chain[]) {
		m_Chain = chain;
	}

	public int[] getChain() {
		return m_Chain;
	}
	
	public String chainTipText() {
	  return "This option was meant to be called by other classifiers.";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		int L = D.classIndex();

		int indices[] = getChain();
		if (indices == null) {
			indices = MLUtils.gen_indices(L);
			MLUtils.randomize(indices,new Random(m_S));
			//setChain(indices);
		}
		if(getDebug()) System.out.print(":- Chain (");
		root = new Link(indices,0,D);
		if (getDebug()) System.out.println(" ) -:");
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		root.classify(x);
		return MLUtils.toDoubleArray(x,L);
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tThe seed value for randomization\n\tdefault: 0", "S", 1, "-S <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
	  String	tmpStr;
	  
	  
	  tmpStr = Utils.getOption('S', options);
	  if (tmpStr.length() > 0)
	    m_S = Integer.parseInt(tmpStr);
	  else
	    m_S = 0;

	  super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
	  	ArrayList<String> result;
	  	result = new ArrayList<String>(Arrays.asList(super.getOptions()));
	  	result.add("-S");
	  	result.add("" + m_S);
		return result.toArray(new String[result.size()]);

	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CC(),args);
	}
}
