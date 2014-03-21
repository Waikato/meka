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

package meka.classifiers.multitarget;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import meka.classifiers.multilabel.*;
import meka.classifiers.multilabel.meta.*;
import meka.classifiers.multitarget.*;
import meka.classifiers.multitarget.meta.*;
import weka.classifiers.functions.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.supervised.attribute.*;
import meka.filters.multilabel.SuperNodeFilter;
import weka.attributeSelection.*;
import weka.filters.*;
import weka.core.*;
import meka.core.MLUtils;
import meka.core.MLEvalUtils;
import meka.core.StatUtils;
import meka.core.Result;
import meka.core.A;
import meka.core.M;
import meka.core.MetaLabelUtils;
import java.util.*;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

/**
 * SCC.java - Super Class Classifier
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 * @version	June 2012
 */
public class SCC extends MultilabelClassifier implements Randomizable, MultiTargetClassifier, TechnicalInformationHandler {

	private SuperNodeFilter f = new SuperNodeFilter();

	private int m_P = 1;
	private int m_N = 0;
	private int m_L = 2;
	private int m_I = 1000;
	private int m_O = 0;

	private static final int i_SPLIT = 67;
	private static final String i_ErrFn = "Exact match";

	private Random rand = null;
	//private MT mt = null;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"Super Class Classifier (SCC).\n"
				+ "Like a multi-target-capable PS. Removes examples with P-infrequent labelsets from the training data, then makes super classes out of what's left; and then trains a standard ML classifier on them.\n"
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Jesse Read, Concha Blieza, Pedro Larranaga");
		result.setValue(Field.TITLE, "Multi-Dimensional Classification with Super-Classes");
		result.setValue(Field.JOURNAL, "IEEE Transactions on Knowledge and Data Engineering");
		result.setValue(Field.YEAR, "2013");
		
		return result;
	}

	private double rating(int partition[][], double M[][]) {
		return rating(partition,M,0.0);
	}

	/**
	 * Rating - Return a rating for the super-class 'partition', under dataset D.
	 */
	private double rating (int partition[][], Instances D) {
		int L = D.classIndex();
		int N = D.numInstances();

		// create and fill factor nodes according to 'partition'
		Factor F[] = new Factor[partition.length];	
		for(int n = 0; n < partition.length; n++) {
			F[n] = new Factor();								// create a factor node
			for(int j : partition[n]) {
				//System.out.println("add node for : "+j);
				F[n].addNode(j,D.attribute(j).numValues());								// add indices
			}
			F[n].fillNodes(D);									// fill with probabilities
			//System.out.println("F[n] = "+F[n]);
		}

		// get a probability value for the full set of data instances
		double p = 0.0;
		for(int i = 0; i < N; i++) {
			int path_i[] = MLUtils.toIntArray(D.instance(i),L);
			double p_ = 1.0;
			for(int n = 0; n < partition.length; n++) {
				double c = F[n].p_path(path_i);
				//System.out.println("P("+Arrays.toString(partition[n])+")="+c);
				p_ *= F[n].p_path(path_i);
			}
			p += p_;
		}
		return p/N;
	}

	/**
	 * Rating - Return a score for the super-class 'partition' using the pairwise info in 'M'
	 * @param	partition	super-class partition, e.g., [[0,3],[2],[1,4]]
	 * @param	M			pariwise information \propto M[j][k]
	 * @param	CRITICAL	a critical value to use
	 *
	 * CRITICAL = 2.706; 
	 * CRITICAL = 6.251;
	 * @Note: For now, assume 3 DOF (multi-label classification)
	 * @todo set CRITICAL into M, then this can be a generic function
	 */
	private double rating(int partition[][], double M[][], double CRITICAL) {

		int L = M.length;

		double S[][] = new double[L][L];			// sums
		boolean T[][] = new boolean[L][L];			// together ? 

		double sumTogether = 0.0, sumApart = 0.0;

		// for each combo ...
		for(int i = 0; i < partition.length; i++) {
			Arrays.sort(partition[i]);
			double n = partition[i].length;
			// ... add the AVG score for each together-pair
			for(int j = 0; j < n; j++) {
				for(int k = j+1; k < n; k++) {
					T[partition[i][j]][partition[i][k]] = true;
				}
			}
			//score += tot / ((n*(n-1))/2);
		}

		// for each non-together pair ...
		for(int j = 0; j < L; j++) {
			for(int k = j+1; k < L; k++) {
				if (T[j][k]) 
					sumTogether += (M[j][k] - CRITICAL);
				else
					sumApart += (M[j][k] - CRITICAL);
			}
		}

		return sumTogether - sumApart;
	}

	// return [0,...,L-1]
	private int[][] defaultCombinations(int L) {
		int partition[][] = new int[L][];
		for(int j = 0; j < L; j++) {
			partition[j] = new int[]{j};
		}
		return partition;
	}

	/**
	 * MutateCombinations - mutate the 'partition'.
	 */
	private int[][] mutateCombinations(int partition[][], Random r) {

		int from = r.nextInt(partition.length);
		int i = r.nextInt(partition[from].length);

		int to = r.nextInt(partition.length);

		if (to == from) {
			// create new list, add
			partition = Arrays.copyOf(partition,partition.length+1);
			partition[partition.length-1] = new int[]{partition[from][i]};
			to = partition.length + 1;
			// delete original
			partition[from] = A.delete(partition[from],i);
		}
		else {
			// make new slot, copy
			partition[to] = A.add(partition[to],partition[from][i]);
			// delete original
			partition[from] = A.delete(partition[from],i);
		}
		// if empty ...
		if (partition[from].length <= 0) {
			// delete it
			partition[from] = partition[partition.length-1];
			partition = Arrays.copyOf(partition,partition.length-1);
		}

		return partition;
	}

	/**
	 * CheckData - used for debugging purposes.
	 */
	public void checkData(Instances D) {
		int L = D.classIndex();
		HashSet counts[] = new HashSet[L];
		for(int j = 0; j < L; j++) {
			counts[j] = new HashSet<Integer>();
			for(Instance x : D) {
				int k = (int)x.value(j);
				counts[j].add(k);
			}
		}
		for(int j = 0; j < L; j++) {
			System.out.println(""+j+" = "+counts[j]);
			if (counts[j].size() < 2) {
				System.out.println("OK, this is a problem ...");
				//System.exit(1);
			}
		}
	}

	/**
	 * Train classifier h, on dataset D, under super-class partition 'partition'.
	 */
	public void trainClassifier(Classifier h, Instances D, int partition[][]) throws Exception {
		f  = new SuperNodeFilter();
		f.setIndices(partition);
		f.setP(m_P >= 0 ? m_P : rand.nextInt(Math.abs(m_P)));
		f.setN(m_L >= 0 ? m_L : rand.nextInt(Math.abs(m_L)));
		Instances D_ = f.process(D);
		//checkData(D_);
		if (getDebug()) {
			int N = D.numInstances();
			int U = MLUtils.numberOfUniqueCombinations(D);
			System.out.println("PS("+f.getP()+","+m_L+") reduced: "+N+" -> "+D_.numInstances()+" / "+U+" -> "+MLUtils.numberOfUniqueCombinations(D_));
			//System.out.println("E_acc P "+f.getP()+" "+(D_.numInstances()/(double)N) +" "+(MLUtils.numberOfUniqueCombinations(D_)/(double)U));
			//m_Info = "P="+f.getP()+"; %N="+(D_.numInstances()/(double)N) +"; %C"+(MLUtils.numberOfUniqueCombinations(D_)/(double)U)+"; size(partition)="+partition.length;
		}

		m_InstancesTemplate = D_;
		m_Classifier.buildClassifier(D_); // build on the processed batch
	}

	/**
	 * Test classifier h, on dataset D, under super-class partition 'partition'.
	 */
	public Result testClassifier(Classifier h, Instances D_train, Instances D_test, int partition[][]) throws Exception {

		trainClassifier(m_Classifier,D_train,partition);

		Result result = Evaluation.testClassifier((MultilabelClassifier)h, D_test);

		if (h instanceof MultiTargetClassifier || Evaluation.isMT(D_test)) {
			result.setInfo("Type","MT");
		}
		else if (h instanceof MultilabelClassifier) {
			result.setInfo("Threshold",MLEvalUtils.getThreshold(result.predictions,D_train,"PCut1"));
			result.setInfo("Type","ML");
		}

		result.setValue("N_train",D_train.numInstances());
		result.setValue("N_test",D_test.numInstances());
		result.setValue("LCard_train",MLUtils.labelCardinality(D_train));
		result.setValue("LCard_test",MLUtils.labelCardinality(D_test));

		//result.setValue("Build_time",(after - before)/1000.0);
		//result.setValue("Test_time",(after_test - before_test)/1000.0);
		//result.setValue("Total_time",(after_test - before)/1000.0);

		result.setInfo("Classifier_name",h.getClass().getName());
		//result.setInfo("Classifier_ops", Arrays.toString(h.getOptions()));
		result.setInfo("Classifier_info",h.toString());
		result.setInfo("Dataset_name",MLUtils.getDatasetName(D_test));

		result.output = Result.getStats(result,"1");
		return result;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {

		int N = D.numInstances();						// only for printouts
		int U = MLUtils.numberOfUniqueCombinations(D);	// only for printouts
		int L = D.classIndex(); 
		rand = new Random(m_S); 

		// 0. SPLIT INTO TRAIN AND VALIDATION SET/S
		Instances D_r = new Instances(D);
		D_r.randomize(rand);
		Instances D_train = new Instances(D_r,0,D_r.numInstances()*i_SPLIT/100);
		Instances D_test = new Instances(D_r,D_train.numInstances(),D_r.numInstances()-D_train.numInstances());

		// 1. BUILD BR or EBR
		if (getDebug()) System.out.print("1. BUILD & Evaluate BR: ");
		CR cr = new CR();
		cr.setClassifier(((MultilabelClassifier)m_Classifier).getClassifier()); // assume PT
		Result result_1 = Evaluation.evaluateModel((MultilabelClassifier)cr,D_train,D_test,"PCut1","5"); 
		double acc1 = result_1.output.get(i_ErrFn);
		if (getDebug()) System.out.println(" "+acc1);

		int partition[][] = MetaLabelUtils.generatePartition(MLUtils.gen_indices(L),rand); 

		// 2. SELECT / MODIFY INDICES (using LEAD technique)
		if (getDebug()) System.out.println("2. GET ERR-CHI-SQUARED MATRIX: ");
		double MER[][] = StatUtils.condDepMatrix(D_test,result_1);
		if (getDebug()) System.out.println(M.toString(MER));

		/* 
		 * 3. SIMULATED ANNEALING
		 * Always accept if best, progressively less likely accept otherwise.
		 */
		if (getDebug()) System.out.println("3. COMBINE NODES TO FIND THE BEST COMBINATION ACCORDING TO CHI");
		double w = rating(partition,MER);
		if (getDebug()) System.out.println("@0 : "+MetaLabelUtils.toString(partition)+ "\t("+w+")");
		
		for(int i = 0; i < m_I; i++) {
			int partition_[][] = mutateCombinations(M.deep_copy(partition),rand);
			double w_ = rating(partition_,MER); // this is really p_MER(partition_)
			 if (w_ > w) {
				 // ACCEPT
				 partition = partition_;
				 w = w_;
				 if (getDebug()) System.out.println("@"+i+" : "+MetaLabelUtils.toString(partition)+ "\t("+w+")");
			 }
			 else {
				 // MAYBE ACCEPT
				 double diff = Math.abs(w_-w);
				 double p = (2.*(1. - sigma(diff*i/1000.))); 
				 if (p > rand.nextDouble()) {
					 // OK, ACCEPT NOW
					 if (getDebug()) System.out.println("@"+i+" : "+MetaLabelUtils.toString(partition_)+ "\t("+w_+")*");
					 partition = partition_;
					 w = w_;
				 }
			 }

		}

		/*
		 * METHOD 2
		 * refine the set we started with above, with a few iterations.
		 * we mutate a set, and accept whenever the classification performance is GREATER
		 */
		if (m_N > 0) {
			if (getDebug()) System.out.println("4. REFINING THE INITIAL SET WITH SOME OLD-FASHIONED INTERNAL EVAL");
			// Build & evaluate the classifier with the latest partition
			result_1 = testClassifier((MultilabelClassifier)m_Classifier,D_train,D_test,partition);
			w = result_1.output.get(i_ErrFn);
			if (getDebug()) System.out.println("@0 : "+MetaLabelUtils.toString(partition)+ "\t("+w+")");
			for(int i = 0; i < m_N; i++) {
				int partition_[][] = mutateCombinations(M.deep_copy(partition),rand);
				// Build the classifier with the new combination
				trainClassifier(m_Classifier,D_train,partition);
				// Evaluate on D_test
				Result result_2 = testClassifier((MultilabelClassifier)m_Classifier,D_train,D_test,partition_);
				double w_ = result_2.output.get(i_ErrFn);
				if (w_ > w) {
					w = w_;
					partition = partition_;
					if (getDebug()) System.out.println("@"+(i+1)+"' : "+MetaLabelUtils.toString(partition)+ "\t("+w+")");
				}
			}
		}

		// 4. DECIDE HOW GOOD THEY ARE, COMPARE EACH LABEL TO BR-result?
		if (getDebug()) System.out.println("4. TRAIN "+MetaLabelUtils.toString(partition));
		trainClassifier(m_Classifier,D,partition);

		if (getDebug()) {
			//System.out.println("E_acc P "+m_P+" "+(mt.m_InstancesTemplate.numInstances()/(double)N) +" "+(MLUtils.numberOfUniqueCombinations(mt.m_InstancesTemplate)/(double)U));
		}
		// 5. MOVE ON ...
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		//return mt.distributionForInstance(x);
		int L = x.classIndex();
		double y[] = new double[L*2];

		// Convert (x,y) to (x_,y_)
		int L_ = m_InstancesTemplate.classIndex(); // == L-NUM
		Instance x_ = MLUtils.setTemplate(x,f.getTemplate(),m_InstancesTemplate);

		// Get a classification y_ = h(x_)
		double y_[] = null;
		try {
			y_ = ((MultilabelClassifier)m_Classifier).distributionForInstance(x_);
		} catch(Exception e) {
			System.err.println("EXCEPTION !!! setting to "+Arrays.toString(y_));
			return y;
			//e.printStackTrace();
			//System.exit(1);
		}

		// For each super node ...
		for(int j = 0; j < L_; j++) {

			int idxs[] = SuperNodeFilter.decodeClasses(m_InstancesTemplate.attribute(j).name());						 	// 3,4	(partition)
			String vals[] = SuperNodeFilter.decodeValue(m_InstancesTemplate.attribute(j).value((int)Math.round(y_[j]))); 	// 1,0	(clases)

			for(int i = 0; i < idxs.length; i++) {
				y[idxs[i]] = x.dataset().attribute(idxs[i]).indexOfValue(vals[i]); 		// y_j = v
				y[idxs[i]+L] = y_[j+L_];												// P(Y_j = v), hence, MUST be a multi-target classifier
			}
		}

		return y;
	}

	protected int m_S = 0;

	@Override
	public void setSeed(int s) {
		m_S = s;
	}

	@Override
	public int getSeed() {
		return m_S;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new SCC(),args);
	}

	/**
	 * Sigmoid / Logistic function
	 */
	public static final double sigma(double a) {
		return 1.0/(1.0+Math.exp(-a));
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tSets the number of simulated annealing iterations\n\tdefault: "+m_I, "I", 1, "-I <value>"));
		newVector.addElement(new Option("\tSets the number of connections\n\tdefault: "+m_N, "N", 1, "-N <value>"));
		newVector.addElement(new Option("\tSets the pruning number for PS\n\tdefault: "+m_P, "P", 1, "-P <value>"));
		newVector.addElement(new Option("\tSets the limit for PS (was N) \n\tdefault: "+m_L, "L", 1, "-L <value>"));
		newVector.addElement(new Option("\tAnother random open option.\n\tdefault: "+m_O, "O", 1, "-O <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		m_I = (Utils.getOptionPos('I',options) >= 0) ? Integer.parseInt(Utils.getOption('I', options)) : m_I;
		m_L = (Utils.getOptionPos('L',options) >= 0) ? Integer.parseInt(Utils.getOption('L', options)) : m_L;
		m_N = (Utils.getOptionPos('N',options) >= 0) ? Integer.parseInt(Utils.getOption('N', options)) : m_N;
		m_P = (Utils.getOptionPos('P',options) >= 0) ? Integer.parseInt(Utils.getOption('P', options)) : m_P;
		m_O = (Utils.getOptionPos('O',options) >= 0) ? Integer.parseInt(Utils.getOption('O', options)) : m_O;
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 10];
		int current = 0;
		options[current++] = "-I";
		options[current++] = String.valueOf(m_I);
		options[current++] = "-N";
		options[current++] = String.valueOf(m_N);
		options[current++] = "-P";
		options[current++] = String.valueOf(m_P);
		options[current++] = "-L";
		options[current++] = String.valueOf(m_L);
		options[current++] = "-O";
		options[current++] = String.valueOf(m_O);
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;

	}

}
