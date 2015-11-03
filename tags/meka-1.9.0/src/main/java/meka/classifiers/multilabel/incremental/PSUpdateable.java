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
  	
	protected int m_Counter = 0;
	protected int m_Limit = 1000;
	protected int m_Support = 10;
	protected int L = -1;

	protected HashMap<LabelSet,Integer> combinations = null;
	protected Instances batch = null;
	protected Instance m_InstanceTemplate = null;
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
		m_Counter = D.numInstances();

		if (m_Counter > getLimit())  {
			// build
			if (getDebug()) System.out.println("init build @ "+D.numInstances());
			combinations = PSUtils.countCombinationsSparse(D,L);
			MLUtils.pruneCountHashMap(combinations,m_P);
			// { NEW (we don't want more than m_Support classes!)
			int p = m_P;
			while(combinations.size() > getSupport()) {
				//System.out.println("double prune!");
				m_P++;
				MLUtils.pruneCountHashMap(combinations,m_P);
			}
			super.buildClassifier(D);
			m_P = p;
			// } NEW
		}
		else {
			// start batch
			if (getDebug()) System.out.println("start batch @ "+D.numInstances());
			batch = new Instances(D);
			// we will predict the majority labelset until we have a large enough batch
			mlu.buildClassifier(D);
		}
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {

		if (m_Counter < getLimit()) {

			// store example
			batch.add(x);
			mlu.updateClassifier(x);
			// build when we're ready
			if (++m_Counter >= getLimit()) {
				// bulid PS
				combinations = PSUtils.countCombinationsSparse(batch,L);
				//combinations.prune(m_P);
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
		}
		else {
			// update PS
			for (Instance x_i : PSUtils.PSTransformation(x,L,combinations,m_N)) {
				// update internal sl classifier (e.g. naive bayes)
				((UpdateableClassifier)m_Classifier).updateClassifier(x_i);
			}
		}
		
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		if (m_Counter < getLimit()) {
			// return most common combination
			return mlu.distributionForInstance(x);
		}
		else {
			// return PS prediction
			return super.distributionForInstance(x);
		}
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'I', getLimit());
		OptionUtils.add(result, 'S', getSupport());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setLimit(OptionUtils.parse(options, 'I', 1000));
		setSupport(OptionUtils.parse(options, 'S', 10));
		super.setOptions(options);
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tSets the buffer size        \n\tdefault: 1000", "I", 1, "-I <value>"));
		result.addElement(new Option("\tSets the max. num. of combs.\n\tdefault: 10", "S", 1, "-S <value>"));
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
		return "XXX.";
	}

	public int getSupport() {
		return m_Support;
	}

	public void setSupport(int m_Support) {
		this.m_Support = m_Support;
	}

	public String supportTipText() {
		return "XXX.";
	}

	public static void main(String args[]) {
		IncrementalEvaluation.runExperiment(new PSUpdateable(),args);
	}
}
