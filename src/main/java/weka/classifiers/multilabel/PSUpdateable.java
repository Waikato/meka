package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * PSUpdateable.java - Pruned Sets Updateable.
 * Can be given any base classifier, since it must rebuild when the buffer is full anyway.
 * <br>
 * While the initial training set is being buffered, it will predict the majority labelset.
 * <br>
 * Note that this version buffers training examples, not just combinations.
 * @see PS.java
 * @author 		Jesse Read (jesse@tsc.uc3m.es)
 * @version 	September, 2011
 */
public class PSUpdateable extends PS implements UpdateableClassifier {

	private int m_Counter = 0;
	public int m_Limit = 1000;
	public int m_Support = 10;
	protected int L = -1;

	protected HashMap<String,Integer> combinations = null;
	private Instances batch = null;
	private Instance m_InstanceTemplate = null;
	private MajorityLabelsetUpdateable mlu = new MajorityLabelsetUpdateable();

	@Override
	public String globalInfo() {
		return "Updateable PS";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {

		L = D.classIndex();
		m_Counter = D.numInstances();

		if (m_Counter > m_Limit)  {
			// build
			if (getDebug()) System.out.println("init build @ "+D.numInstances());
			combinations = MLUtils.countCombinations(D,L);
			//combinations.prune(m_P);
			MLUtils.pruneCountHashMap(combinations,m_P);
			// { NEW (we don't want more than m_Support classes!)
			int p = m_P;
			while(combinations.size() > m_Support) {
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

		if (m_Counter < m_Limit) {

			// store example
			batch.add(x);
			mlu.updateClassifier(x);
			// build when we're ready
			if (++m_Counter >= m_Limit) {
				// bulid PS
				combinations = MLUtils.countCombinations(batch,L);
				//combinations.prune(m_P);
				MLUtils.pruneCountHashMap(combinations,m_P);
				// { NEW (we don't want more than m_Support classes!) -- note, the while loop is a slow way to do this
				int p = m_P;
				while(combinations.size() > m_Support) {
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
			for (Instance x_i : subsample(x,L,combinations)) {
				// update internal sl classifier (e.g. naive bayes)
				((UpdateableClassifier)m_Classifier).updateClassifier(x_i);
			}
		}
		
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		if (m_Counter < m_Limit) {
			// return most common combination
			return mlu.distributionForInstance(x);
		}
		else {
			// return PS prediction
			return super.distributionForInstance(x);
		}
	}

	public Instance[] subsample(Instance x, int L, HashMap<String,Integer> combinations) {

		Instance x_subsets[] = new Instance[1];

		String comb = MLUtils.toBitString(x,L);

		// add it
		if(combinations.containsKey(comb)) {	//if its class value exists
			// make x a sl instance
			Instance x_sl = convertInstance(x,L);
			x_sl.setClassValue(comb);
			x_subsets[0] = x_sl;
			
			return x_subsets;
		}
		// subsample it
		else if(m_N > 0) { 
			String d_subsets[] = getTopNSubsets(comb,combinations,m_N);
			//System.out.println(">"+m_InstancesTemplate);
			x_subsets = new Instance[d_subsets.length];
			for(int i = 0; i < d_subsets.length; i++) {
				//System.out.println(">"+d_subsets[i]);
				// make x a sl instance
				Instance x_sl = convertInstance(x,L);
				x_sl.setDataset(m_InstancesTemplate);
				x_sl.setClassValue(d_subsets[i]);
				x_subsets[i] = x_sl;
			}
		}
		// skip it
		else {
			return new Instance[]{};
		}
		return x_subsets;
	}

	@Override
	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 4];
		int current = 0;
		options[current++] = "-B";
		options[current++] = "" + m_Limit;
		options[current++] = "-S";
		options[current++] = "" + m_Support;
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;

	}

	@Override
	public void setOptions(String[] options) throws Exception {

		try {
			m_Limit = Integer.parseInt(Utils.getOption('B', options));
		} catch(Exception e) {
			if(getDebug()) System.err.println("Using default m_Limit = "+m_Limit);
		}

		try {
			m_Support = Integer.parseInt(Utils.getOption('S', options));
		} catch(Exception e) {
			if(getDebug()) System.err.println("Using default m_Support = "+m_Support);
		}

		super.setOptions(options);
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the buffer size        \n\tdefault: "+m_Limit+"", "B", 1, "-B <value>"));
		newVector.addElement(new Option("\tSets the max. num. of combs.\n\tdefault: "+m_Support+"", "S", 1, "-S <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	public static void main(String args[]) {
		WindowIncrementalEvaluator.evaluation(new PSUpdateable(),args);
	}

}
