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

package meka.classifiers.multilabel.incremental;

import meka.classifiers.incremental.IncrementalEvaluation;
import meka.classifiers.multilabel.IncrementalMultiLabelClassifier;
import meka.classifiers.multilabel.PS;
import meka.core.LabelSet;
import meka.core.MLUtils;
import meka.core.OptionUtils;
import meka.core.PSUtils;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.trees.HoeffdingTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;

import java.util.*;

/**
 * PSUpdateable.java - Pruned Sets Updateable.
 * Can be given any base classifier, since it must rebuild when the buffer is full anyway.
 * <br>
 * While the initial training set is being buffered, it will predict the majority labelset. Note that this version buffers training examples, not just combinations.
 * @see PS
 * @author 		Jesse Read
 * @version 	September, 2011
 */
public class PSUpdateable extends PS implements IncrementalMultiLabelClassifier {

	/** for serialization. */
  	private static final long serialVersionUID = -3909203248118831224L;
  	
	protected int m_Limit = 1000;
	protected int m_Support = 10;
	protected int L = -1;

	protected HashMap<LabelSet,Integer> combinations = null;
	protected Instances batch = null;
	protected MajorityLabelsetUpdateable mlu = new MajorityLabelsetUpdateable();

	@Override
	public String globalInfo() {
		return "Updateable PS";
	}

	public PSUpdateable() {
		// default classifier for GUI
		this.m_Classifier = new HoeffdingTree();
	}

	@Override
	protected String defaultClassifierString() {
		// default classifier for CLI
		return "weka.classifiers.trees.HoeffdingTree";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		L = D.classIndex();
		batch = new Instances(D);

		if (batch.numInstances() >= getLimit())  {
			// if we have at least the limit, build!
			if (getDebug()) System.out.println("Train on instances 0 ... "+batch.numInstances());
			combinations = PSUtils.countCombinationsSparse(batch,L);
			MLUtils.pruneCountHashMap(combinations,m_P);
			// { NEW (we don't want more than m_Support classes!)
			int p = m_P;
			while(combinations.size() > getSupport()) {
				//System.out.println("double prune!");
				m_P++;
				MLUtils.pruneCountHashMap(combinations,m_P);
			}
			super.buildClassifier(batch);
			m_P = p;
			// } NEW
			mlu = null; // We won't be needing the majority set classifier!
		}
		else {
			// otherwise we don't have enough yet, initialize the collection batch
			if (getDebug()) System.out.println("Continue collection batch from instance "+batch.numInstances());
			// we will predict the majority labelset until we have a large enough batch
			mlu.buildClassifier(batch);
		}
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {

		if (batch.numInstances() < getLimit() && mlu != null) {

			// store example
			batch.add(x);
			if (batch.numInstances() >= getLimit()) {
				// we have enough instances to bulid PS!
				combinations = PSUtils.countCombinationsSparse(batch,L);
				MLUtils.pruneCountHashMap(combinations,m_P);
				// { NEW (we don't want more than m_Support classes!) -- note, the while loop is a slow way to do this
				int p = m_P;
				while(combinations.size() > getSupport()) {
					m_P++;
					MLUtils.pruneCountHashMap(combinations,m_P);
				}
				super.buildClassifier(batch);
				m_P = p;
				// } NEW
				batch.clear();
				mlu = null;
			}
			else {
				// not enough instances in the batch yet, just update the majority-label classifier
				mlu.updateClassifier(x);
			}
		}
		else {
			// update PS ...
			for (Instance x_i : PSUtils.PSTransformation(x,L,combinations,m_N,super.m_InstancesTemplate)) {
				// update internal sl classifier (e.g. naive bayes)
				((UpdateableClassifier)m_Classifier).updateClassifier(x_i);
			}
		}
		
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		if (mlu != null) {
			// we're still using the majority-labelset classifier, use it to return the most common combination
			return mlu.distributionForInstance(x);
		}
		else {
			// we've built PS already, return a PS prediction!
			return super.distributionForInstance(x);
		}
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'I', getLimit());
		OptionUtils.add(result, "support", getSupport());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setLimit(OptionUtils.parse(options, 'I', 1000));
		setSupport(OptionUtils.parse(options, "support", 10));
		super.setOptions(options);
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tSets the buffer size        \n\tdefault: 1000", "I", 1, "-I <value>"));
		result.addElement(new Option("\tSets the max. num. of combs.\n\tdefault: 10", "support", 1, "-support <value>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	public int getLimit() {
		return m_Limit;
	}

	public void setLimit(int m_Limit) {
		this.m_Limit = m_Limit;
	}

	public String limitTipText() {
		return "The buffer size (num. of instances to collect before training PS).";
	}

	public int getSupport() {
		return m_Support;
	}

	public void setSupport(int m_Support) {
		this.m_Support = m_Support;
	}

	public String supportTipText() {
		return "The maximum number of class values (i.e., label combinations) to consider.";
	}

	public static void main(String args[]) {
		IncrementalEvaluation.runExperiment(new PSUpdateable(),args);
	}
}
