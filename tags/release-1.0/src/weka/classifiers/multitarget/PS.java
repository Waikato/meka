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
		try {
			m_Classifier.buildClassifier(convertInstances(D,L));
		} catch(Exception e) {
			if (m_P > 0) {
				m_P--;
				System.out.println("trying with P = "+m_P+" ...");
				buildClassifier(D);
			}
			else {
				System.out.println("give up ...");
				System.out.println(""+e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();

		//if there is only one class (as for e.g. in some hier. mtds) predict it
		//if(L == 1) return new double[]{1.0};

		Instance x_sl = convertInstance(x,L);							// the sl instance
		x_sl.setDataset(m_InstancesTemplate);							// where y in {comb_1,comb_2,...,comb_k}

		double w[] = m_Classifier.distributionForInstance(x_sl);		// w[j] = p(y_j) for each j = 1,...,L
		int max_j  = Utils.maxIndex(w);									// j of max w[j]
		//int max_j = (int)m_Classifier.classifyInstance(x_sl);			// where comb_i is selected
		String y_max = m_InstancesTemplate.classAttribute().value(max_j);									// comb_i e.g. "0+3+0+0+1+2+0+0"

		double y[] = Arrays.copyOf(MLUtils.toDoubleArray(MLUtils.decodeValue(y_max)),L*2);					// "0+3+0+0+1+2+0+0" -> [0.0,3.0,0.0,...,0.0]

		HashMap<Double,Double> votes[] = new HashMap[L];
		for(int j = 0; j < L; j++) {
			votes[j] = new HashMap<Double,Double>();
		}

		for(int i = 0; i < w.length; i++) {
			double y_i[] = MLUtils.toDoubleArray(MLUtils.decodeValue(m_InstancesTemplate.classAttribute().value(i)));
			for(int j = 0; j < y_i.length; j++) {
				votes[j].put(y_i[j] , votes[j].containsKey(y_i[j]) ? votes[j].get(y_i[j]) + w[i] : w[i]);
			}
		}

		// some confidence information
		for(int j = 0; j < L; j++) {
			y[j+L] = votes[j].size() > 0 ? Collections.max(votes[j].values()) : 0.0;
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


	@Override
	public Instances convertInstances(Instances D, int L) throws Exception {

		//Gather combinations
		HashMap<String,Integer> distinctCombinations = MLUtils.classCombinationCounts(D);
		if(getDebug())
			System.out.println("Found "+distinctCombinations.size()+" unique combinations");

		//Prune combinations
		MLUtils.pruneCountHashMap(distinctCombinations,m_P);
		if(getDebug())
			System.out.println("Pruned to "+distinctCombinations.size()+" with P="+m_P);

		// Remove all class attributes
		Instances D_ = MLUtils.deleteAttributesAt(new Instances(D),MLUtils.gen_indices(L));
		// Add a new class attribute
		D_.insertAttributeAt(new Attribute("CLASS", new ArrayList(distinctCombinations.keySet())),0); // create the class attribute
		D_.setClassIndex(0);

		//Add class values
		for (int i = 0; i < D.numInstances(); i++) {
			String y = MLUtils.encodeValue(MLUtils.toIntArray(D.instance(i),L));
			// add it
			if(distinctCombinations.containsKey(y)) 	//if its class value exists
				D_.instance(i).setClassValue(y);
			// decomp
			else if(m_N > 0) { 
				String d_subsets[] = getTopNSubsets(y,distinctCombinations,m_N);
				for (String s : d_subsets) {
					int w = distinctCombinations.get(s);
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

		if (getDebug())
			System.out.println(""+D_);

		return D_;
	}

	public static String[] decodeValue(String a) {
		return a.split("\\+");
	}

	/**
	 * GetTopNSubsets.
	 * We take only the subsets which differ by a single class value, then rank by frequency, and take the top N
	 */
	public static String[] getTopNSubsets(String y, final HashMap <String,Integer>masterCombinations, int N) {
		String y_bits[] = y.split("\\+");
		ArrayList<String> Y = new ArrayList<String>();  
		for(String y_ : masterCombinations.keySet()) {
			if(MLUtils.bitDifference(y_bits,y_.split("\\+")) <= 1) {
				Y.add(y_);
			}
		}
		Collections.sort(Y,new Comparator<String>(){
			public int compare(String s1, String s2) {
			// @note this is just done by the count, @todo: could add further conditions
			return (masterCombinations.get(s1) > masterCombinations.get(s2) ? -1 : (masterCombinations.get(s1) > masterCombinations.get(s2) ? 1 : 0));
			} 
		}
		);
		String Y_strings[] = (String[])Y.toArray(new String[Y.size()]);
		//System.out.println("returning "+N+"of "+Arrays.toString(Y_strings));
		return Arrays.copyOf(Y_strings,Math.min(N,Y_strings.length));
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new weka.classifiers.multitarget.PS(),args);
	}

}
