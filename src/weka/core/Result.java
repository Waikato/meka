package weka.core;

import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.evaluation.*;

import weka.classifiers.multilabel.*;

import java.util.*;
import java.io.*;

/**
 * Result. 
 * For storing predictions alongside true labels, for evaluation. 
 * Stored in plaintext format where header consits of lines:
		   L=no. of labels
           v:param=value to store in 'vals' HashMap
		   i:param=value to store in 'info' HashMap
	  and where entries consist of e.g.:
           [1,0,1]:[0.3,0.2,0.4] 
	  where true labels : label predictions or confidences. Look at an output file for an example.
 * For more on the evaluation and threshold selection implemented here; see: 
 * Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer (2011).
 * Jesse Read, <i>Scalable Multi-label Classification</i>. PhD Thesis, University of Waikato, Hamilton, New Zealand (2010).
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 * @version	March 2012 - Multi-target Compatible
 */
public class Result { // implements Serializable {

	private static final long serialVersionUID = 1L;

	public int L = 0;

	public ArrayList<double[]> predictions = null;
	public ArrayList<int[]> actuals = null;

	public HashMap<String,Double> output = new LinkedHashMap<String,Double>();
	public HashMap<String,String> info = new LinkedHashMap<String,String>();
	public HashMap<String,Double> vals = new LinkedHashMap<String,Double>();

	public Result() {
		predictions = new ArrayList<double[]>();
		actuals = new ArrayList<int[]>();
	}

	public Result(int L) {
		predictions = new ArrayList<double[]>();
		actuals = new ArrayList<int[]>();
		this.L = L;
	}

	public Result(int N, int L) {
		predictions = new ArrayList<double[]>(N);
		actuals = new ArrayList<int[]>(N);
		this.L = L;
	}

	public int size() {
		return predictions.size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();  
		for (String v : output.keySet()) {
			sb.append(Utils.padLeft(v,20));
			sb.append(" : ");
			sb.append(Utils.doubleToString(output.get(v),5,3));
			sb.append('\n');
		}
		return MLUtils.hashmapToString(info) + "\n" + sb.toString() + "\n" + MLUtils.hashmapToString(vals);
	}

	// add entry
	public void addResult(double pred[], Instance real) {
		predictions.add(pred);
		actuals.add(MLUtils.toIntArray(real,pred.length));
	}

	public int[] rowActual(int i) {
		return actuals.get(i);
	}

	public double[] rowRanking(int i) {
		return predictions.get(i);
	}

	public int[] rowPrediction(int i, double t) {
		return MLUtils.toIntArray(rowRanking(i),t);
	}

	public double[][] allPredictions() {
		double Y[][] = new double[predictions.size()][];
		for(int i = 0; i < predictions.size(); i++) {
			Y[i] = rowRanking(i);
		}
		return Y;
	}

	public int[][] allPredictions(double t) {
		int Y[][] = new int[predictions.size()][];
		for(int i = 0; i < predictions.size(); i++) {
			Y[i] = rowPrediction(i,t);
		}
		return Y;
	}

	public void addValue(String op, double v) {
		Double freq = vals.get(op);
		vals.put(op,(freq == null) ? v : freq + v);
	}

	public void setValue(String op, double v) {
		vals.put(op,v);
	}

	public double getValue(String op) {
		return vals.get(op);
	}

	public void setInfo(String op, String val) {
		info.put(op,val);
	}

	public String getInfo(String op) {
		return info.get(op);
	}

	// ********************************************************************************************************
	//                     STATIC METHODS
	// ********************************************************************************************************
	//

	/**
	 * Get a single result.
	 * For a mean+/-var result for 'op' under cross validation.
	 */
	public static final String getValues(String op, Result rs[]) {
		double values[] = new double[rs.length];
		for(int i = 0; i < rs.length; i++) {
			values[i] = rs[i].vals.get(op);
		}
		return Utils.doubleToString(Utils.mean(values),5,3)+" +/- "+Utils.doubleToString(Math.sqrt(Utils.variance(values)),5,3);
	}

	/**
	 * Get Performance Measures. 
	 */
	public static HashMap<String,Double> getStats(Result r) {
		if (r.getInfo("Type").equalsIgnoreCase("MT"))
			return MLEvalUtils.getMTStats(r.predictions,r.actuals);
		else 
			return MLEvalUtils.getMLStats(r.predictions, r.actuals, r.getInfo("Threshold"));
	}

	/**
	 * Get Average Performance Measures. 
	 * Across multiple results.
	 */
	public static HashMap<String,double[]> getStats(Result r[]) {

		HashMap<String,double[]> o = new HashMap<String,double[]>(); 
		for(int i = 0; i < r.length; i++) {
			HashMap<String,Double> o_i = getStats(r[i]);
			for(String k : o_i.keySet()) {
				double values[] = o.containsKey(k) ? o.get(k) : new double[r.length];
				values[i] = o_i.get(k);
				o.put(k,values);
			}
		}
		return o;
	}

	// write out as plain text
	public static void writeResultToFile(Result s, String fname) throws Exception {
		PrintWriter outer = new PrintWriter(new BufferedWriter(new FileWriter(fname)));
		StringBuilder sb = new StringBuilder();  
		sb.append("L=").append(s.L).append('\n');		// @TODO "i:"+s.info.toString()
		for (String k : s.info.keySet()) {
			sb.append("i:").append(k).append('=').append(s.info.get(k)).append('\n');
		}
		for (String k : s.vals.keySet()) {				// @TODO "v:"+s.vals.toString()
			sb.append("v:").append(k).append('=').append(s.vals.get(k)).append('\n');
		}
		double N = (double)s.predictions.size();
		for(int i = 0; i < N; i++) {
			sb.append(Arrays.toString(s.actuals.get(i))+":"+Arrays.toString(s.predictions.get(i))+"\n");
		}
		outer.write(sb.toString());
		outer.close();
	}

	// read in from plain text
	public static Result readResultFromFile(String filename) throws Exception {
		BufferedReader in = new BufferedReader( new FileReader(filename) );
		Result r = null;

		try {
			String line = null;
			while (( ( line = in.readLine() ) ) != null ){
				if (line.startsWith("[")) {
					String arrays[] = line.split(":");
					r.predictions.add(MLUtils.toDoubleArray(arrays[1]));
					r.actuals.add(MLUtils.toIntArray(arrays[0]));
				}
				else if (line.startsWith("i")) { // info
					line = line.substring(line.indexOf(':')+1);
					int idx = line.indexOf('=');
					r.info.put(line.substring(0,idx),line.substring(idx+1));
				}
				else if (line.startsWith("v")) { // vals
					line = line.substring(line.indexOf(':')+1);
					int idx = line.indexOf('=');
					r.vals.put(line.substring(0,idx),Double.parseDouble(line.substring(idx+1)));
				}
				else if (line.startsWith("L")) { // L=
					r = new Result(Integer.parseInt(line.substring(line.indexOf('=')+1)));
				}
			}
		} finally{
			if ( in != null)  in.close();
		}

		return r;
	}

	public static void main (String args[]) {

		// CROSS-FOLD-VALIDATION
		if (Utils.getOptionPos('f',args) >= 0 && Utils.getOptionPos('x',args) >= 0) {

			// Read folds in from file.meka.0 , ... , file.meka.<numFolds-1>
			int numFolds = 0;
			try {
				numFolds = Integer.parseInt(Utils.getOption('x',args));
			} catch(Exception e) {
				System.err.println("Failed to parse the number of folds");
				e.printStackTrace();
				System.exit(1);
			}
			Result fold[] = new Result[numFolds];
			String basename = null;
			try {
				basename = Utils.getOption('f',args);
				for(int i = 0; i < numFolds; i++) {
					fold[i] = Result.readResultFromFile(basename+"."+i);
					// check for threshold
					if (fold[i].getInfo("Threshold")==null) {
						System.out.println("Having to calculate a threshold ...");
						System.exit(1);
					}
				}
			} catch(Exception e) {
				System.err.println("Error finding/loading files ... Was looking for "+basename+".0 ... "+basename+"."+numFolds);
				e.printStackTrace();
				System.exit(1);
			}
			// Create a super-Result, and get the results
			Result r = new Result();
			r.info = fold[0].info;
			for(String v : fold[0].vals.keySet()) {
				// add to super-result
				r.info.put(v,Result.getValues(v,fold));
			}

			// Print out Results
			HashMap<String,double[]> o = Result.getStats(fold);
			for(String s : o.keySet()) {
				double values[] = o.get(s);
				r.info.put(s,Utils.doubleToString(Utils.mean(values),5,3)+" +/- "+Utils.doubleToString(Math.sqrt(Utils.variance(values)),5,3));
			}
			r.setInfo("Type","CV");
			System.out.println(r.toString());
		}

		// REGULAR-VALIDATION
		else if (Utils.getOptionPos('f',args) >= 0) {

			Result r = null;
			try {
				r = Result.readResultFromFile(Utils.getOption('f',args));
			} catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			HashMap<String,Double> o = Result.getStats(r);
			r.output = o;
			System.out.println(r.toString());

		}
		else {
			System.out.println("You must supply the filename with -f <filename>");
		}

	}
}

