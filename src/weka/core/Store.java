package weka.core;

import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.evaluation.*;

import weka.classifiers.multilabel.*;

import java.util.*;
import java.io.*;

/**
 * Store. 
 * For storing results. Having all the confidence predictions and all the true labels stored
 * allows us to run evaluation again under different threshold settings without rebuilding
 * the models, UPDATE 2011: as well as look at 
 *
 * For more on the evaluation and threshold selection implemented here; see: 
 * Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer (2011).
 * Jesse Read, <i>Scalable Multi-label Classification</i>. PhD Thesis, University of Waikato, Hamilton, New Zealand (2010).
 *
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 * @version	August 2011 - 
 	* Updated threshold calibration method.
	* Option to output misclassification information.
	* New plain-text format where
	  header consits of lines:
		   L=no. of labels
           v:param=value to store in `vals' HashMap
		   i:param=value to store in `info' HashMap
	  and where entries consist of e.g.:
           [2,3]:[0.3,0.2,0.4]
	  (indicies of actual (relevant) labels, and real-valued confidence predictions for each label).
	  Look at an output file for an example.
 */
public class Store implements Serializable {

	private static final long serialVersionUID = 1L;

	public int J = 0;

	public ArrayList<double[]> predictions = null;
	public ArrayList<short[]> actuals = null;

	public HashMap<String,Double> output = new HashMap<String,Double>();
	public HashMap<String,String> info = new HashMap<String,String>();
	public HashMap<String,Double> vals = new HashMap<String,Double>();

	// Construct an empty Store
	public Store(int J) {
		predictions = new ArrayList<double[]>();
		actuals = new ArrayList<short[]>();
		this.J = J;
	}

	// Construct an empty Store with space reserved for n records
	public Store(int n, int J) {
		predictions = new ArrayList<double[]>(n);
		actuals = new ArrayList<short[]>(n);
		this.J = J;
	}

	public final int size() {
		return predictions.size();
	}

	public void clear() {
		predictions.clear(); 
		predictions = null;
		actuals.clear(); 
		actuals = null; 
		System.gc(); 
	}

	public String toString() {
		return hashmapToString(info) + "\n------\n" + hashmapToString(vals) + "\n------\n" + hashmapToString(output);
	}

	protected static final String hashmapToString(HashMap<String,?> hm) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> itr = hm.keySet().iterator();
		while(itr.hasNext()) {
			String k = itr.next();
			sb.append(Utils.padLeft(k,20)).append(" : ").append(hm.get(k)).append('\n');
		}
		return sb.toString();
	}

	public final void addResult(double pred[], Instance real) {

		// compact the actual values
		short index_list[] = new short[pred.length];
		int c = 0;
		for(short j = 0; j < index_list.length; j++) {
			if(real.value(j) > 0.0)
				index_list[c++] = j;
		}
		index_list = Arrays.copyOf(index_list,c);

		// add entry
		predictions.add(pred);
		actuals.add(index_list);

	}

	public final int[] rowActual(int i) {
		short values[] = actuals.get(i);
		int real[] = new int[J];
		for(short idx : values) 
			real[idx] = 1;
		return real;
	}

	public final double[] rowRanking(int i) {
		return predictions.get(i);
	}

	public final int[] rowPrediction(int i, double t) {
		double ranking[] = rowRanking(i);
		int predicted[] = new int[ranking.length];
		for(int j = 0; j < ranking.length; j++) {
			if (ranking[j] >= t) 
				predicted[j] = 1;
		}
		return predicted;
	}

	public final void addValue(String op, double v) {
		Double freq = vals.get(op);
		vals.put(op,(freq == null) ? v : freq + v);
	}

	public final void setValue(String op, double v) {
		vals.put(op,v);
	}

	public final double getValue(String op) {
		return vals.get(op);
	}

	public final void setInfo(String op, String val) {
		info.put(op,val);
	}

	public final String getInfo(String op) {
		return info.get(op);
	}

	// ********************************************************************************************************
	//                     STATIC METHODS
	// ********************************************************************************************************
	//

	public static final void extractStats (Store s, Instances train, Instances test) {
		s.setValue("Num_train",train.numInstances());
		s.setValue("Num_test",test.numInstances());
		s.setValue("LCard_train",MLUtils.labelCardinality(train,s.J));
		s.setValue("LCard_test",MLUtils.labelCardinality(test,s.J));
		s.setInfo("MCom_comb",MLUtils.mostCommonCombination(train,s.J));
	}

	// AUTOMATIC THRESHOLD CALIBRATION
	public static double findSwitchPoint(Store s) { 
		int L = s.J;
		int N = s.predictions.size();
		// make a big array, and fill it with all confidence outputs, then sort it
		double big[] = new double[L*N];
		for(int i = 0, c = 0; i < N; i++) {
			for (double d : s.rowRanking(i)) {
				big[c++] = d; 
			}
		}
		Arrays.sort(big);
		//System.err.println(""+Arrays.toString(big));
		// to get a label cardinality of LCard_train
		double l_card = s.vals.get("LCard_train");
		int i = (N*L) - (int)Math.round(l_card * (double)(N));
		// the value t which we want is the one which separates away values 0,...,i from i+1,...,n
		double t = Math.max(((double)(big[i]+big[Math.max(i+1,N-1)]))/2.0 , 0.00001);
		//System.err.println("between [ "+big[i]+" , "+big[i-1]+" ] = "+t);
		return t;
	}

	/**
	 * Calculate Performance Measures. 
	 * A threshold will be calculated first.
	 */
	public static HashMap<String,Double> calculate(Store s) {
		return calculate(s,findSwitchPoint(s));
	}

	/**
	 * Calculate Performance Measures. 
	 * A threshold is supplied.
	 */
	public static HashMap<String,Double> calculate(Store s, double t) {

		double N = (double)s.predictions.size();
		int fp = 0, tp = 0, tn = 0, fn = 0;
		int p_sum_total = 0, r_sum_total = 0;
		double log_loss_D = 0.0, log_loss_L = 0.0;
		int set_empty_total = 0, set_inter_total = 0; 
		int exact_match = 0, one_error = 0, coverage = 0; 
		double accuracy = 0.0, f1_macro_D = 0.0, f1_macro_L = 0.0;
		int hloss_total = 0;
		int[] o_tp = new int[s.J], o_fp = new int[s.J], o_fn = new int[s.J], o_tn = new int[s.J];
		int[] d_tp = new int[(int)N], d_fp = new int[(int)N], d_fn = new int[(int)N], d_tn = new int[(int)N];
		//double average_accuracy_online = 0.0;

		for(int i = 0; i < N; i++) {
			int actual[] = s.rowActual(i);
			double ranking[] = s.rowRanking(i);
			//int pred[] = s.rowPrediction(i,t);

			int pred[] = new int[actual.length];
			for(int j = 0; j < s.J; j++) {
				pred[j] = (ranking[j] >= t) ? 1 : 0;
			}

			//System.out.println("act"+Arrays.toString(actual));
			//System.out.println("prd"+Arrays.toString(pred));

			// calculate
			int p_sum = 0, r_sum = 0;
			int set_union = 0;
			int set_inter = 0;
			int doc_inter = 0;
			int doc_union = 0;
			for(int j = 0; j < s.J; j++) {
				int p = pred[j];
				int R = actual[j];
				if (p==1) {
					p_sum++;
					// predt 1, real 1
					if(R==1) {
						r_sum++;
						tp++;
						o_tp[j]++;			// f1 macro (L)
						d_tp[i]++;			// f1 macro (D)
						set_inter++;
						set_union++;
					}
					// predt 1, real 0
					else {
						fp++;
						o_fp[j]++;			// f1 macro (L)
						d_fp[i]++;			// f1 macro (D)
						hloss_total++;
						set_union++;
					}
				}
				else {                      
					// predt 0, real 1
					if(R==1) { 
						r_sum++;
						fn++;
						o_fn[j]++;			// f1 macro (L)
						d_fn[i]++;			// f1 macro (D)
						hloss_total++;
						set_union++;
					}
					// predt 0, real 0
					else {   
						tn++;
						o_tn[j]++;			// f1 macro (L)
						d_tn[i]++;			// f1 macro (D)
					}
				}

				// log losses: 
				log_loss_D += calcLogLoss((double)R,ranking[j],Math.log(N));
				log_loss_L += calcLogLoss((double)R,ranking[j],Math.log(s.J));
			}

			set_inter_total += set_inter;

			p_sum_total += p_sum; 
			r_sum_total += r_sum;

			if(set_union > 0)	//avoid NaN
				accuracy += ((double)set_inter / (double)set_union);
			//System.out.println(""+set_inter+","+set_union);

			if (p_sum <= 0)	//empty set
				set_empty_total ++;

			// exact match (eval. by example)
			if(set_inter == set_union)
				exact_match++;

			// f1 macro average by example
			if (p_sum > 0 && r_sum > 0 && set_inter > 0) {
				double prec = (double)set_inter / (double)p_sum;
				double rec = (double)set_inter / (double)r_sum;
				if (prec > 0 || rec > 0) {
					f1_macro_D += ((2.0 * prec * rec) / (prec + rec));
				}
			}

			//one error: how many times the top ranked label is NOT in the label set
			if(actual[Utils.maxIndex(ranking)] <= 0)
				one_error++;

		}

		double fms[] = new double[s.J];                                                                                                                        
		for (int j = 0; j < s.J; j++) {                                                                                                                        
			// micro average
			if(o_tp[j] <= 0)                                                                                             
				fms[j] = 0.0;                                                                                                                                  
			else {                                                                                                                                             
				double prec = (double)o_tp[j] / ((double)o_tp[j]+(double)o_fp[j]);                                                          
				double recall = (double)o_tp[j] / ((double)o_tp[j]+(double)o_fn[j]);                                                          
				fms[j] = 2 * ((prec*recall) / (prec+recall));                                                                                                                  
			}                                                                                                                                                  
		}               

		double precision = (double)set_inter_total / (double)p_sum_total;
		double recall = (double)set_inter_total / (double)r_sum_total;
		s.output.put("Accuracy"			,(accuracy/N));
		s.output.put("F1_micro"			,(2.0 * precision * recall) / (precision + recall));
		s.output.put("F1_macro_D"		,(f1_macro_D/N));
		s.output.put("F1_macro_L"		,(Utils.sum(fms)/(double)s.J));
		s.output.put("H_loss"			,((double)hloss_total/((double)N*(double)s.J)));
		s.output.put("H_acc"			,1.0-((double)hloss_total/((double)N*(double)s.J)));
		s.output.put("LCard_pred"		,(double)p_sum_total/N);
		s.output.put("LCard_real"		,(double)r_sum_total/N);
		s.output.put("LCard_diff"		,Math.abs((((double)p_sum_total/N)-(double)r_sum_total/N)));
		s.output.put("Coverage"			,((double)coverage/N));
		s.output.put("One_error"		,((double)one_error/N));
		s.output.put("Exact_match"		,((double)exact_match/N));
		s.output.put("ZeroOne_loss"		,1.0-((double)exact_match/N));
		s.output.put("LogLossD"			,(log_loss_D/N));
		s.output.put("LogLossL"			,(log_loss_L/N));
		s.output.put("Empty"			,(double)set_empty_total/(double)N);
		s.output.put("EmptyAccuracy"	,(accuracy/(N-set_empty_total)));
		s.output.put("EmptyMacroF1"		,f1_macro_D/(N-(double)set_empty_total));
		s.output.put("Build_time"		,s.vals.get("Build_time"));
		s.output.put("Test_time"		,s.vals.get("Test_time"));
		s.output.put("Total_time"		,s.vals.get("Build_time") + s.vals.get("Test_time"));
		s.output.put("TPR"				,(double)tp / (double)(tp + fn));
		s.output.put("FPR"				,(double)fp / (double)(fp + tn));
		s.output.put("Precision"		,precision);
		s.output.put("Recall"			,recall);
		s.output.put("Threshold"        ,t);
		s.output.put("AUPRC"        	,0.0); // "@see (`Hierarchical Multi-label Classification' by Vens et al, 2008);

		return s.output;

	}

	public static double calcLogLoss(double R, double P, double C) {
		// base 2 ?
		double ans = Math.min(Utils.eq(R,P) ? 0.0 : -( (R * Math.log(P)) + ((1.0 - R) * Math.log(1.0 - P)) ),C);
		return (Double.isNaN(ans) ? 0.0 : ans);
	}

	public static HashMap<String,Double> analyze(Store s) {

		double loss[] = new double[s.J]; 

		for(int i = 0; i < s.predictions.size(); i++) {
			int actual[] = s.rowActual(i);
			double ranking[] = s.rowRanking(i);
			for(int j = 0; j < s.J; j++) {
				loss[j] += Math.abs((Math.round((double)actual[j])-ranking[j]));
			}
		}

		for(int j = 0; j < s.J; j++) {
			System.out.println(""+j+"\t"+loss[j]);
		}

		return null;

	}

	// write out as plain text
	public final static void writeStoreToFile(Store s, String fname) throws Exception {
		PrintWriter outer = new PrintWriter(new BufferedWriter(new FileWriter(fname)));
		StringBuilder sb = new StringBuilder();  
		sb.append("L=").append(s.J).append('\n');
		for (String k : s.info.keySet()) {
			sb.append("i:").append(k).append('=').append(s.info.get(k)).append('\n');
		}
		for (String k : s.vals.keySet()) {
			sb.append("v:").append(k).append('=').append(s.vals.get(k)).append('\n');
		}
		double N = (double)s.predictions.size();
		for(int i = 0; i < N; i++) {
			short inds[] = s.actuals.get(i);
			sb.append("[");
			for(int j_i : inds) 
				sb.append(""+j_i+",");
			sb.deleteCharAt(sb.length()-1);
			sb.append("]");
			double y[] = s.predictions.get(i);
			sb.append(":[");
			for(double y_j : y) 
				sb.append(""+y_j+",");
			sb.deleteCharAt(sb.length()-1);
			sb.append("]");
			sb.append("\n");
		}
		outer.write(sb.toString());
		outer.close();
	}

	private final static double[] string2doublearray(String sarray[]) throws Exception {
		if (sarray != null) {
			double doublearray[] = new double[sarray.length];
			for (int i = 0; i < sarray.length; i++) {
				doublearray[i] = Double.parseDouble(sarray[i]);
			}
			return doublearray;
		}
		return null;
	}

	private final static short[] string2shortarray(String sarray[]) throws Exception {
		if (sarray != null) {
			short intarray[] = new short[sarray.length];
			for (int i = 0; i < sarray.length; i++) {
				intarray[i] = (short)Integer.parseInt(sarray[i]);
			}
			return intarray;
		}
		return null;
	}

	// read in from plain text
	public final static Store readStoreFromFile(String filename) throws Exception {
		BufferedReader in = new BufferedReader( new FileReader(filename) );
		String line = null;
		Store r = null;
		try {
			while (( ( line = in.readLine() ) ) != null ){
				if (line.startsWith("[")) {
					// data
					int s1 = line.indexOf('[');
					int e1 = line.indexOf(']');
					int s2 = line.indexOf('[',s1+1);
					int e2 = line.indexOf(']',e1+1);
					String s_array[] = line.substring(s1+1,e1).split(",");
					String y_array[] = line.substring(s2+1,e2).split(",");
					r.predictions.add(string2doublearray(y_array));
					r.actuals.add(string2shortarray(s_array));
				}
				else if (line.startsWith("i")) {
					// info
					line = line.substring(line.indexOf(':')+1);
					int idx = line.indexOf('=');
					r.info.put(line.substring(0,idx),line.substring(idx+1));
				}
				else if (line.startsWith("v")) {
					// vals
					line = line.substring(line.indexOf(':')+1);
					int idx = line.indexOf('=');
					r.vals.put(line.substring(0,idx),Double.parseDouble(line.substring(idx+1)));
				}
				else if (line.startsWith("L")) {
					// L=
					int idx = line.indexOf('=');
					r = new Store(Integer.parseInt(line.substring(line.indexOf('=')+1)));
				}
			}
		} finally{
			if ( in != null)  in.close();
		}

		return r;
	}

	/* write out as plain text
	public static void writeOut(String data, String file) {
		try {
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(data);
			out.close();
		} catch (Exception e){ 
			System.err.println("Error: " + e.getMessage());
		}
	}
	*/

	public static void main (String args[]) {
		if (args.length < 2) {
			System.out.println("Example Usage:");
			System.out.println("");
			System.out.println(" to calibrate a threshold automatically; print results; and exit");
			System.out.println("     java weka.core.Store <store_file.dat> c");
			System.out.println("");
			System.out.println(" to set a threshold of 0.1; print results; and exit");
            System.out.println("     java weka.core.Store <store_file.dat> t 0.1");
			System.out.println("");
			System.out.println(" to calibrate a threshold automatically; print results; STORE the threshold; and exit");
			System.out.println("     java weka.core.Store <store_file.dat> c");
			System.out.println("");
			System.out.println(" to set a threshold of 0.1; print results; STORE the threshold; and exit");
            System.out.println("     java weka.core.Store <store_file.dat> t 0.1");
            System.out.println(" to print all results (using the previously STORED threshold):");
            System.out.println("     java weka.core.Store <store_file.dat> +");
			System.out.println("");
			System.out.println(" to print the result for Accuracy (using the previously STORED threshold):");
            System.out.println("     java weka.core.Store <store_file.dat> Accuracy");
			System.out.println("");
            System.out.println(" to print out the predictions");
            System.out.println("     java weka.core.Store <store_file.dat> p");
			System.out.println("");
			System.out.println(" to print out confusion information");
            System.out.println("     java weka.core.Store <store_file.dat> P");
			System.exit(1);
		}
		try {
			Store r = Store.readStoreFromFile(args[0]);
			HashMap<String,Double> o = r.output;

			// SET A THRESHOLD

			if (args[1].equalsIgnoreCase("T")) {
				System.out.println("Evaluating using threshold "+args[2]);
				o = Store.calculate(r,Double.parseDouble(args[2]));
				System.out.println(""+r);
				if (args[1].equals("T")) {
					r.setValue("Threshold",r.output.get("Threshold"));
					System.out.println("Writing out again (t="+r.getValue("Threshold")+") ...");
					Store.writeStoreToFile(r,args[0]);
				}
				System.exit(0);
			}

			// CALIBRATE A THRESHOLD

			if (args[1].equalsIgnoreCase("C")) {
				System.out.println("Evaluating using automatically-calibrated threshold ");
				o = Store.calculate(r);
				System.out.println(""+r);
				if (args[1].equals("C")) {
					r.setValue("Threshold",r.output.get("Threshold"));
					System.out.println("Writing out again (t="+r.getValue("Threshold")+") ...");
					Store.writeStoreToFile(r,args[0]);
				}
				System.exit(0);
			}

			// if we get this far, we must have calculated a threshold

			if (r.vals.get("Threshold") == null) {
				System.err.println("Error: need to set a threshold with option C or T first");
			}

			// PRINT (previously obtained) RESULTS

			if (args[1].equalsIgnoreCase("R")) {
				o = Store.calculate(r,r.getValue("Threshold"));
				// print the result just for measure 'args[1]'
				if (args.length > 2)
					System.out.println(o.get(args[2]));
				// print the full results
				else
					System.out.println(""+r);
			}

			// PRINT PREDICTIONS using previously obtained threshold

			if (args[1].equalsIgnoreCase("P")) {
				o = Store.calculate(r,r.getValue("Threshold"));
				int N = r.predictions.size();
				int L = r.J;
				double t = r.output.get("Threshold");
				System.out.println("N_test="+N+"; L="+L+"; threshold="+t+"; "+("total_time(s)="+r.output.get("Total_time")));
				for(int i = 0; i < r.size(); i++) {
					//System.out.println(Arrays.toString(r.rowActual(i))+":"+Arrays.toString(r.rowRanking(i))+":"+Arrays.toString(r.rowPrediction(i,t)));
					System.out.println(Arrays.toString(r.rowActual(i))+" vs f_"+t+"(y) = "+Arrays.toString(r.rowPrediction(i,t)));
				}
			}

			// PRINT CONFUSION information

			if (args[1].equalsIgnoreCase("X")) {
				double t = r.getValue("Threshold");
				HashMap<String,HashSet> hm = new HashMap<String,HashSet>();
				for(int i = 0; i < r.size(); i++) {
					String y = MLUtils.toBitString(r.rowActual(i));
					String y_pred = MLUtils.toBitString(r.rowPrediction(i,t));
					// if y != y_pred
					if (!y.equals(y_pred)) {
						// add to the error list
						HashSet<String> hs = (hm.get(y) == null ? new HashSet<String>() : hm.get(y));
						hs.add(y_pred);
						hm.put(y,hs);
					}
				}
				for (String y : hm.keySet()) {
					System.out.println("Set "+y+" was commonly confused with "+hm.get(y));
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

