package weka.classifiers.multitarget;

/**
 * The Pruned Sets (PS) method. Multi-target version. 
 * Because pruned sets are duplicated as the closest sets, rather than subsets.
 * Note: currently can only handle 10 values (or fewer) per target variable.
 * @see		weka.classifiers.multilabel.BR
 * @version	Feb 2012
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 */
import weka.classifiers.*;
import weka.classifiers.multilabel.*;
import weka.core.*;
import java.util.*;

public class PS extends weka.classifiers.multilabel.PS implements MultiTargetClassifier {

	@Override
	public void buildClassifier(Instances D) throws Exception {
		int L = D.classIndex();
		m_Classifier.buildClassifier(processInstances(D,L));
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		//if there is only one class (as for e.g. in some hier. mtds) predict it
		if(L == 1) return new double[]{1.0};

		Instance x_sl = convertInstance(x,L);						// the sl instance
		x_sl.setDataset(m_InstancesTemplate);						// where y in {comb_1,comb_2,...,comb_k}

		double w[] = m_Classifier.distributionForInstance(x_sl);
		int max  = Utils.maxIndex(w);
		//int max = (int)m_Classifier.classifyInstance(x_sl);			// where comb_i is selected
		String y_max = m_InstancesTemplate.classAttribute().value(max);	// comb_i e.g. "03001200"

		double y[] = Arrays.copyOf(MLUtils.fromBitString(y_max),L*2);					// "03001200" -> [0.0,3.0,0.0,...,0.0]

		HashMap<Integer,Double> votes[] = new HashMap[L];
		for(int j = 0; j < L; j++) {
			votes[j] = new HashMap<Integer,Double>();
		}

		for(int i = 0; i < w.length; i++) {
			int y_i[] = MLUtils.toIntArray(m_InstancesTemplate.classAttribute().value(i));
			for(int j = 0; j < y_i.length; j++) {
				votes[j].put(y_i[j] , votes[j].containsKey(y_i[j]) ? votes[j].get(y_i[j]) + w[i] : w[i]);
			}
		}

		for(int j = 0; j < L; j++) {
			//System.out.println("votes[j] = "+votes[j]);
			y[j+L] = Collections.max(votes[j].values());
		}

		return y;
	}

	@Override
	public double[] convertDistribution(double y_sl[], int L) {
		double y_ml[] = new double[L];
		for(int i = 0; i < y_sl.length; i++) {
			if(y_sl[i] > 0.0) {
				double d[] = MLUtils.fromBitString(m_InstancesTemplate.classAttribute().value(i));
				for(int j = 0; j < d.length; j++) {
					if(d[j] > 0.0)
						y_ml[j] = 1.0;
				}
			}
		}
		return y_ml;
	}

	public Instances processInstances(Instances D, int L) throws Exception {

		//Gather combinations
		HashMap<String,Integer> distinctCombinations = MLUtils.countCombinations(D,L);

		//Prune combinations
		MLUtils.pruneCountHashMap(distinctCombinations,m_P);

		//Create class attribute
		Set<String> values = new HashSet<String>();
		for(String y : distinctCombinations.keySet()) 
			values.add(y);

		//Filter Remove all class attributes
		Instances D_ = MLUtils.deleteAttributesAt(new Instances(D),MLUtils.gen_indices(L));
		D_.insertAttributeAt(new Attribute("C", new ArrayList(values)),0);
		D_.setClassIndex(0);

		//Add class values
		for (int i = 0; i < D.numInstances(); i++) {
			String y = MLUtils.toBitString(D.instance(i),L);
			// add it
			if(values.contains(y)) 	//if its class value exists
				D_.instance(i).setClassValue(y);
			// decomp
			else if(m_N > 0) { 
				String d_subsets[] = getTopNSubsets(y,distinctCombinations,m_N);
				for (String s : d_subsets) {
					Instance copy = (Instance)(D_.instance(i)).copy();
					copy.setClassValue(s);
					copy.setWeight(1.0 / d_subsets.length);
					D_.add(copy);
				}
			}
		}

		// remove with missing class
		D_.deleteWithMissingClass();

		// keep the header of new dataset for classification
		m_InstancesTemplate = new Instances(D_, 0);

		return D_;
	}

	// get the n _closest_ values to y (may be suitable for MT only)
	public String[] getTopNSubsets(String y, HashMap <String,Integer>all, int n) {
		ArrayList<String> Y = new ArrayList<String>();  
		// add
		for(String y_ : all.keySet()) {
			if(MLUtils.bitDifference(y,y_) <= 1) {
				Y.add(y_);
			}
		}
		return (String[])Y.toArray(new String[Y.size()]);
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new weka.classifiers.multitarget.PS(),args);
	}

}
