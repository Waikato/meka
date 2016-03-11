/*
 *
 *  
 *  Adapted from NaiveBayes.java
 *  
 *  Copyright (C) 2016 Fernando Benites
 *  @author Fernando Benites
 */
package meka.classifiers.multilabel.neurofuzzy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.classifiers.multilabel.ProblemTransformationMethod;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

/**
 * ****REPLACE THE FOLLOWING WITH SIMILAR INFORMATION.
 * Class for a Naive Bayes classifier using estimator classes. Numeric 
 * estimator precision values are chosen based on analysis of the 
 * training data. For this reason, the classifier is not an 
 * UpdateableClassifier (which in typical usage are initialized with zero 
 * training instances) -- if you need the UpdateableClassifier functionality,
 * use the NaiveBayesUpdateable classifier. The NaiveBayesUpdateable
 * classifier will  use a default precision of 0.1 for numeric attributes
 * when buildClassifier is called with zero training instances.
 * <p>
 * For more information on Naive Bayes classifiers, see<p>
 *
 * George H. John and Pat Langley (1995). <i>Estimating
 * Continuous Distributions in Bayesian Classifiers</i>. Proceedings
 * of the Eleventh Conference on Uncertainty in Artificial
 * Intelligence. pp. 338-345. Morgan Kaufmann, San Mateo.<p>
 *
 * Valid options are:<p>
 *
 * -K <br>
 * Use kernel estimation for modelling numeric attributes rather than
 * a single normal distribution.<p>
 *
 * -D <br>
 * Use supervised discretization to process numeric attributes.<p>
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @author Rushi Bhatt (rushi@cns.bu.edu)
 * @version $Revision: 1.16 $
 * Modified by Rushi for use as a CN710 template
 */
public class ARAMNetworkSparseHT extends ARAMNetworkClass {

 //**** THIS IS WHERE CLASSIFIER WEIGHTS ETC GO ****
 //define stuff like weight matrices, classifier parameters etc.
 //e.g., protected double rho_a_bar=0.0;

    HashMap[] weightsA = null;
    HashSet[] upweightsA=null;
    double[] sweightsA = null;
    double sweightsA0;
    HashMap[] weightsB = null;
	HashMap<String, Vector> hmclasses = null;
	int snumFeatures=0;
	int snumClasses=0;
	int numinstances=0;
	int activated=0;

    public ARAMNetworkSparseHT(int fnumFeatures, int fnumClasses, double fro, double fthreshold) {
	initARAM(fnumFeatures, fnumClasses,  fro,  fthreshold);
	}

    public ARAMNetworkSparseHT(){
    }
    private void initARAM(int fnumFeatures, int fnumClasses, double fro, double fthreshold){
		numFeatures = fnumFeatures;
		snumFeatures = (int)(0.5*numFeatures);
		numClasses = fnumClasses;
		snumClasses= (int)(0.5*numClasses);
		threshold = fthreshold;
		weightsA = new HashMap[1];
		weightsA[0] = new HashMap();
		upweightsA = new HashSet[1];
		upweightsA[0] = new HashSet();
		sweightsA = new double[1];
		sweightsA[0]=0;
		for(int i=0;i<numFeatures;i++){
			sweightsA[0]+=1;
		}
		sweightsA0=sweightsA[0];
		weightsB = new HashMap[1];
		weightsB[0] = new HashMap();
		numCategories = 1;
		hmclasses = new HashMap<String, Vector>();



    }
  /**
   * Returns a string describing this classifier
   * @return a description of the classifier suitable for
   * displaying in the explorer/experimenter gui.
   * ****MODIFY WITH CORRECT INFORMATION****
   */
  public String globalInfo() {
    return "This is ARAM.";
  }
  /**
   * Generates the classifier.
   *
   * @param instances set of instances serving as training data 
   * @exception Exception if the classifier has not been generated 
   * successfully
   */
   
  public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();
		int featlength =  (D.numAttributes() -L)*2;
		int numSamples = D.numInstances();
		int classlength = L * 2;
		if (this.order==null){

			order = new ArrayList<Integer>();
			for (int j=0; j<D.numInstances();j++){
				order.add(j);
			}
		}

		if (numFeatures==-1){
		    initARAM( featlength,classlength ,roa , threshold );
			}else{
		if (featlength != numFeatures) {
			return ;

		}
		if (classlength != numClasses) {
			return ;

		}}

	// Copy the instances so we don't mess up the original data.
	// Function calls do not deep copy the arguments..
	//Instances m_Instances = new Instances(instances);
	
	// Use the enumeration of instances to train classifier.
	// Do any sanity checks (e.g., missing attributes etc here
	// before calling updateClassifier for the actual learning
		for(int i=0; i<D.numInstances();i++){
			Instance instance = D.get(order.get(i));
			updateClassifier(instance);
		}
    System.out.println("Training done, used "+numCategories+" neurons with rho ="+roa+".");
	
	// Alternatively, you can put the training logic within this method,
	// rather than updateClassifier(...). However, if you omit the 
	// updateClassifier(...) method, you should remove 
	// UpdateableClassifier from the class declaration above.
  }


 // ****THIS IS THE WEIGHT UPDATE ROUTINE. MODIFY TO CHANGE THE ALGORITHM****
  /**
   * Updates the classifier with the given instance.
   *
   * @param instance the new training instance to include in the model 
   * @exception Exception if the instance could not be incorporated in
   * the model.
   */
  public void updateClassifier(Instance instance) throws Exception {
   //called once for each instance.
	  if(!learningphase){
			return;
		}
		
		int num_classes=(int) (snumClasses);
		HashMap<Integer,Double> data = new HashMap<Integer,Double>();
		HashMap<Integer, Double> labels = new HashMap<Integer, Double>();
		int numChanges = 0;
		int numCategories_1=numCategories -1;
		numinstances+=1;
		
    if (!instance.classIsMissing()) {
     //Do the weight updates using the instance.

			for (Integer tj=0; tj<instance.numValues(); tj++){
				int j=instance.index(tj);
				double  da = instance.value(j);
				if (da==0){
					continue;
				}
				if(j<num_classes){
					labels.put(j, da);
				}else{
			data.put(j-num_classes, da);
				
			}
			}
			
			SortPair2[] cateacti = ARTActivateCategories(data,labels);
			java.util.Arrays.sort(cateacti);
			boolean resonance = false;
			int currentSortedIndex = 0;
			int currentCategory = -1;
			double matchA = 0;

			
			while (!resonance && currentSortedIndex<cateacti.length) {

				currentCategory = cateacti[currentSortedIndex]
						.getOriginalIndex();
				if (currentCategory == numCategories_1) {
					matchA=1;
				}
				else{
				matchA =  (cateacti[currentSortedIndex].getRawValue()/
									snumFeatures);
		//		System.out.println("Ma: "+matchA+" "+cateacti[currentSortedIndex].getValue()
		//				+" "+numinstances+" "+currentCategory+" S:"+sweightsA[currentCategory]);
				
				}
				if (matchA >= roa) {
					if (currentCategory == numCategories_1) {

						if (currentSortedIndex == maxNumCategories) {
							System.out
									.println("WARNING: The maximum number of categories has been reached.");
							resonance = true;
						} else {
							// Add a new category
							sweightsA[currentCategory]=0;
							Set<Integer> s1=data.keySet();
							for (Integer j :s1) {
								Double da=data.get(j);
								weightsA[currentCategory].put(j,da);
								weightsA[currentCategory].put(j+snumFeatures, da);
								upweightsA[currentCategory].add(j+snumFeatures);
							}
							Set<Integer> s2=weightsA[currentCategory].keySet();
							int count=0;
							for (int j :s2) {
								double da=(Double)weightsA[currentCategory].get(j);
								if (j<snumFeatures){
									sweightsA[currentCategory]+=da;
								}
								else{
									sweightsA[currentCategory]+=1-da;
									count+=1;
									
								}
							}
							sweightsA[currentCategory]+=snumFeatures-count;

							s1=labels.keySet();
							for (int j :s1) {
								weightsB[currentCategory].put(j,labels.get(j));
							}
							String s = labels.keySet().toString();
							if (hmclasses.containsKey(s)){
								hmclasses.get(s).add(currentCategory);
							hmclasses.put(s,hmclasses.get(s)); 
							}else{
								Vector<Integer> v = new Vector<Integer>();
								v.add(currentCategory);
								hmclasses.put(s,v);
							}
							ARAMm_Add_New_Category();
							//System.out.println(numinstances+" "+numCategories);
							
							// fprintf(FileID,'Add a new category of %d\n',
							// network.numCategories);
							// Increment the number of changes since we added a
							// new category.
							numChanges = numChanges + 1;
							resonance = true;
							break;
						}
					} else {
						// % Update weights
						double weightChange = ARAMm_Update_Weights(data,
								labels, currentCategory);
						//System.out.println(numinstances+" "+currentCategory+" S:"+sweightsA[currentCategory]);
						//sumArrayF(this.weightsA[1]);
						if (weightChange == 1) {
							numChanges += 1;
						}

						resonance = true;
						break;
					}
				} else {
					currentSortedIndex += 1;
					resonance = false;
				}

			}
			if(!resonance && currentSortedIndex>=cateacti.length)
			{
				// Add a new category
				sweightsA[numCategories_1]=0;
				Set<Integer> s1=data.keySet();
				int count=0;
				for (int j: s1) {
					double da=data.get(j);
					weightsA[numCategories_1].put(j, da);
					sweightsA[numCategories_1]+=da;
					weightsA[numCategories_1].put(j+snumFeatures, da);
					sweightsA[numCategories_1]+=1-da;
					upweightsA[numCategories_1].add(j+snumFeatures);
					count+=1;
				}
				sweightsA[numCategories_1]+=snumFeatures-count;
				s1=labels.keySet();
				for (int j : s1) {
					weightsB[numCategories_1].put(j, labels.get(j));
				}
				String s = labels.keySet().toString();
				if (hmclasses.containsKey(s)){
					hmclasses.get(s).add(numCategories_1);
				hmclasses.put(s,hmclasses.get(s)); 
				}else{
					Vector<Integer> v = new Vector<Integer>();
					v.add(numCategories_1);
					hmclasses.put(s,v);
				}
				ARAMm_Add_New_Category();
				//System.out.println(numinstances+" "+numCategories);
				// fprintf(FileID,'Add a new category of %d\n',
				// network.numCategories);
				// Increment the number of changes since we added a
				// new category.
				numChanges = numChanges + 1;
				
				
			}
      }
  }



 //****THIS IS THE CLASSIFICATION ROUTINE. MODIFY TO CHANGE THE ALGORITHM****
 //****classifyInstance() uses this method, so implement the 
 //****nuts-and-bolts of your algorithm here. 
  /**
   * Calculates the class membership probabilities for the given test 
   * instance.
   *
   * @param instance the instance to be classified
   * @return predicted class probability distribution
   * @exception Exception if there is a problem generating the prediction
   */
  public double[] distributionForInstance(Instance instance) throws Exception {

		int num_classes=(int) (snumClasses);
		double[] ranking = new double[num_classes];

	  //  long startMilli = System.currentTimeMillis();
//			for (int j = 0; j < num_features; j++) {
//
//				double dt=instance.value(num_classes+j);
//				if (dt!=0){
//					currentData.put(j, dt);
//				}
//			}
			//TODO use instance here
			SortPair[] sortedActivations = ARTActivateCategories(instance);

			java.util.Arrays.sort(sortedActivations);
			double s0=sortedActivations[0].getValue();
			double diff_act = s0
					- sortedActivations[numCategories - 2].getValue();
			int largest_activ = 1;
			double activ_change = 0;
			for (int i = 1; i < sortedActivations.length; i++) {
				activ_change = (s0 - sortedActivations[i]
						.getValue())
						/ s0;
				if (activ_change > threshold * diff_act) {
					break;
				}
				largest_activ = largest_activ + 1;
			}
			// % largest_activ =5;
			double[] best_matches = new double[largest_activ];
			java.util.Arrays.fill(best_matches, 1);
			best_matches[0]=s0;
			for (int i = 1; i < largest_activ; i++) {
				// % best_matches(i) = matches(sortedCategories(i));
				best_matches[i] = sortedActivations[i].getValue();
			}
			// % min_mat = min(best_matches);
			// % max_mat = max(best_matches);

			double sum_mat = sumArray(best_matches);
			int currentCategory = 0;
			this.neuronsactivated=new int[largest_activ];
			this.neuronsactivity=new double[largest_activ];
			for (int i = 0; i < largest_activ; i++) {
				this.neuronsactivity[i]=best_matches[i];
				best_matches[i] = best_matches[i] / sum_mat;
				currentCategory = sortedActivations[i].getOriginalIndex();
				this.neuronsactivated[i]=currentCategory;
				// % Fill return vector with weightB values
				Set <Integer> s1= weightsB[currentCategory].keySet();
				for (int j :s1) {
					ranking[j] = ranking[j]
							+ best_matches[i] * (Double)weightsB[currentCategory].get(j);
				}
			}
			this.nrinstclassified+=1;
			if(m_userankstoclass) {
				return ARAMm_Ranking2Class(ranking);
				
			}

		  //  long endMilli = System.currentTimeMillis();
			return ranking;
	  
  }

	public double[] ARAMm_Ranking2Class(double[] rankings) {

		int columns=rankings.length;
		double[] classes= new double[columns ];
		
			SortPair[] sortedRanks = new SortPair[columns];
	    	for (int j=0;j<columns;j++){
	    		sortedRanks[j]= new SortPair(rankings[j],j);
	    	}
			java.util.Arrays.sort(sortedRanks);
//			sortedActivations[0].getValue()sortedActivations[i].getOriginalIndex()
			SortPair[] change=new SortPair[columns-1];
			
	            for(int j =1; j<columns;j++){
	                change[j-1] = new SortPair(sortedRanks[j-1].getValue()-sortedRanks[j].getValue(),j);
	            }
			java.util.Arrays.sort(change);
		//	double val= change[0].getValue();
			int ind=change[0].getOriginalIndex();
	            for (int j =0; j<ind;j++){
	                classes[sortedRanks[j].getOriginalIndex()] = 1;
	            }
		return classes;
	}
	private SortPair[] ARTActivateCategories(Instance Data) {
		SortPair[] catacti = new SortPair[numCategories-1];
		// double[] catacti=new double[numCategories];
		//Set<Integer> s1=new HashSet<Integer>();
		//Set<Integer> s1=Data.keySet();
	  //  long startMilli = System.currentTimeMillis();
		
		for (int i = 0; i < numCategories-1; i++) {
		//    long startMilliss = System.nanoTime();
			double sumvector = 0;
		//	double sumweight = 0;
			int count=0;
			List<Integer> s2=new ArrayList<Integer>(upweightsA[i]);
			//List<Integer> s2=new ArrayList<Integer>(weightsA[i].keySet());
			//for (Integer j: s1) {
			//double da=(Double)Data.get(j);
			long st3 =0;
			long st4 =0;
				for (int tj=0; tj<Data.numValues(); tj++){
				    long startMillisst = System.nanoTime();
					int sj=Data.index(tj);
					double  da = Data.value(sj);
					Integer j=sj-snumClasses;
					//s1.add(j);
					if (da==0){

						//s2.remove((Integer)j);
						continue;
					}
				    long st10 = System.nanoTime();
				count+=1;
			    long st1 = System.nanoTime();

				Double wa=(Double)weightsA[i].get(j);
			    long st1a = System.nanoTime();
				if(wa!=null){
				sumvector += ((da < wa) ?da
						: wa);
				
				//s2.remove((Integer)j);
				}
			    long st2 = System.nanoTime();

				Integer j1=j+snumFeatures;
				Double wat=(Double)weightsA[i].get(j1);
				if(wat!=null){
					wat=1-wat;
				double dat=1-da;
				sumvector += (((dat) < wat) ? dat: wat);
			     st3 = System.nanoTime();
				s2.remove((Integer)j1);
			     st4 = System.nanoTime();
				}else{
					sumvector+=1-da;
				}
			//    long endMillisst = System.nanoTime();
			 int jti=1;
			   // System.out.println("it took " + (endMillisst - startMillisst) + " milli(s)");
			}
			for (Integer j: s2) {
				double wat=1-(Double)weightsA[i].get(j);
				sumvector +=wat;
				count+=1;
				
				
			}
			sumvector+=snumFeatures-count;
			//sumweight=sweightsA[i]; 
		   // long endMilliss = System.nanoTime();
			catacti[i] = new SortPair(sumvector / (alpha + sweightsA[i]), i);
		    //System.out.println("it took " + (endMilliss - startMilliss) + " milli(s)");
		//	catacti[i] = new SortPair(sumvector / (alpha + sumweight), i);
			//System.out.println("sumweight "+(sumweight-sweightsA[i]));
			//if(activated==121){
			//	System.out.println(i+ " "+sumvector+" "+sweightsA[i]);
			//}
		}
	   // long endMilli = System.currentTimeMillis();
		//activated+=1;
		return catacti;
	}
	private SortPair2[] ARTActivateCategories(Map<Integer, Double> Data, HashMap<Integer, Double> labels) {
		
		String s = labels.keySet().toString();		
		Vector lclasses = (Vector)hmclasses.get(s);
		SortPair2[] catacti = null;
		if (lclasses==null||lclasses.size()==0){
			catacti=new SortPair2[1];
			catacti[0] = new SortPair2(1,numCategories-1,1);
			return catacti;
		}
		catacti = new SortPair2[lclasses.size()];
		// double[] catacti=new double[numCategories];	
		for (int i = 0; i < lclasses.size(); i++) {
			double sumvector = 0;
		//	double sumweight = 0;
			int k = ((Integer)lclasses.get(i)).intValue();
			List<Integer> s2=new ArrayList<Integer>(upweightsA[k]);
			

			int counter=0;
			//double dt = instance.value(num_classes+j);
			
			for (Map.Entry<Integer, Double> entry : Data.entrySet()) {
				int j=entry.getKey();
				double da=(Double)entry.getValue();
//			for (Integer tj=0; tj<Data.numValues(); tj++){
//				int j=Data.index(tj);
//				
//				double  da = Data.value(j-snumClasses);
//				if (da==0){
//					s2.remove((Integer)j);
//					continue;
//				}
				Double wa=(Double)weightsA[k].get(j);
				if(wa!=null){
				if(wa==0){
					continue;
				}
				sumvector +=  ((da < wa) ? da
						: wa);
			//	s2.remove((Integer)j);
				
				}
				Integer j1=j+snumFeatures;
				double dat=1-da;
				Double wat=(Double)weightsA[k].get(j1);
				if(wat!=null){
					wat=1-wat;
			//	sumweight += weightsA[k][j];
				sumvector +=(((dat) < wat) ? dat: wat);
				s2.remove((Object)j1);
				
				counter+=1;
				}else{
					sumvector += dat;
					counter+=1;
					//s2.remove((Integer)j1);
							
				}

			}		
			for (Integer j: s2) {
					counter+=1;
					sumvector += 1-(Double)weightsA[k].get(j);
					
					
			}
			sumvector += snumFeatures-counter;
			//sumweight=sweightsA[k];
			//catacti[i] = new SortPair(sumvector / (alpha + sumweight), k);
			//System.out.println("sumweight "+(sumweight-sweightsA[k]));
			catacti[i] = new SortPair2(sumvector / (alpha + sweightsA[k]), k,sumvector);
		}
		return catacti;
	}

 // ****YOU SHOULDN'T NEED TO CHANGE THIS
  /**
   * Classifies the given test instance. The instance has to belong to a
   * dataset when it's being classified. Note that a classifier MUST
   * implement either this or distributionForInstance().
   *
   * @param instance the instance to be classified
   * @return the predicted most likely class for the instance or 
   * Instance.missingValue() if no prediction is made
   * @exception Exception if an error occurred during the prediction
   */
  public double classifyInstance(Instance instance) throws Exception {

		double[] dist = distributionForInstance(instance);
		if (dist == null) {
			throw new Exception("Null distribution predicted");
		}
		switch (instance.classAttribute().type()) {
			case Attribute.NOMINAL:
				double max = 0;
				int maxIndex = 0;
	
				for (int i = 0; i < dist.length; i++) {
					if (dist[i] > max) {
						maxIndex = i;
						max = dist[i];
					}
				}
				if (max > 0) {
					return maxIndex;
				} else {
				    //return Instance.missingValue();
				}
			case Attribute.NUMERIC:
				return dist[0];
		default:
		    return -1;
		}
		
  }


 // ****ANY OPTIONS/PARAMETERS GO HERE****
  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration<Option> listOptions() {
   //These are just examples, modify to suit your algorithm
    Vector<Option> newVector = new Vector<Option>(2);

    newVector.addElement(
	    new Option("\tChange generalization parameter Rho\n",
			       "P", 0,"-P"));
    newVector.addElement(
	    new Option("\tUse ranking to class function special dev. for ARAM.\n",
		       "K", 0,"-K"));
    return newVector.elements();
  }

 //****OPTIONS HERE SHOULD MATCH THOSE ADDED ABOVE****
  /**
   * Parses a given list of options. Valid options are:<p>
   *
   * -K <br>
   * Use kernel estimation for modelling numeric attributes rather than
   * a single normal distribution.<p>
   *
   * -D <br>
   * Use supervised discretization to process numeric attributes.
   *
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
   //These are just examples, modify to suit your algorithm
	//    boolean k = Utils.getFlag('K', options);
	//    boolean d = Utils.getFlag('D', options);
	//    if (k && d) {
	//      throw new IllegalArgumentException(
	//    		  "Can't use both kernel density estimation and discretization!");
	//    }
	//    setUseSupervisedDiscretization(d);
	//    setUseKernelEstimator(k);
      roa = (Utils.getOptionPos("P",options) >= 0) ? Double.parseDouble(Utils.getOption("P", options)) : roa;
      m_userankstoclass= (Utils.getOptionPos("K",options) >= 0);
	  super.setOptions(options);
  }

 //****MORE OPTION PARSING STUFF****
  /**
   * Gets the current settings of the classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String [] getOptions() {
   //These are just examples, modify to suit your algorithm
    String [] options = new String [3];


    try{
 options =weka.core.Utils.splitOptions("-P 0.9 -K");
    }catch (Exception ex) {
	System.out.println(ex.getMessage());
    }
    return options;
  }

 //****ANY INFORMATION LIKE NO. OF UNITS ETC PRINTED HERE
  /**
   * Returns a description of the classifier.
   *
   * @return a description of the classifier as a string.
   */
  public String toString() {
   //These are just examples, modify to suit your algorithm
    StringBuffer text = new StringBuffer();

    text.append("ML ARAM classifier");
//    if (m_Instances == null) {
//      text.append(": No model built yet.");
//    } else {
//      try {
//	for (int i = 0; i < m_Distributions[0].length; i++) {
//	  text.append("\n\nClass " + m_Instances.classAttribute().value(i) +
//		      ": Prior probability = " + Utils.
//		      doubleToString(m_ClassDistribution.getProbability(i),
//				     4, 2) + "\n\n");
//	  Enumeration enumAtts = m_Instances.enumerateAttributes();
//	  int attIndex = 0;
//	  while (enumAtts.hasMoreElements()) {
//	    Attribute attribute = (Attribute) enumAtts.nextElement();
//	    text.append(attribute.name() + ":  " 
//			+ m_Distributions[attIndex][i]);
//	    attIndex++;
//	  }
//	}
//      } catch (Exception ex) {
//	text.append(ex.getMessage());
//      }
//    }

    return text.toString();
  }
  
  
  /**
   * Main method for testing this class.
   *
   * @param argv the options
   */
	private double ARAMm_Update_Weights(HashMap<Integer, Double> data, HashMap<Integer, Double> labels,
			int category) {
		double weightChange = 0;
		sweightsA[category]=0;
		Set<Integer> s1=new TreeSet<Integer>(data.keySet());
		Set<Integer> s2=new HashSet<Integer>(weightsA[category].keySet());
		int count=0;
		
		for (Integer i: s1) {
			if (i>=snumFeatures){
				continue;
			}
			double da=data.get(i);
			Double wa=(Double)weightsA[category].get(i);
			if(wa!=null){
			if (da < wa ){
			wa = (learningRate * da)
					+ (1 - learningRate) * wa;
			if(wa==0){
				weightsA[category].remove(i);
			}else{
			weightsA[category].put(i, wa);
			}
			}
			sweightsA[category]+=wa;
			s2.remove(i);
			}
			double dat=1-da;
		//}
		//	for (Integer i: s1) {
				int j1= i+snumFeatures;
		//		double dat=1-data.get(j1-snumFeatures);
				Double wat=(Double)weightsA[category].get(j1);
		//	Double wat=(Double)weightsA[category].get(j1);
			if(wat!=null){
			wat=1-wat;
			if (dat < wat ){
			wat = ((learningRate * dat)
					+ (1 - learningRate) * wat);
			if (wat==1){
				weightsA[category].remove(j1);
				upweightsA[category].remove(j1);
			}else{
			weightsA[category].put(j1, 1-wat);
			upweightsA[category].add(j1);
			count+=1;
			}
			}else{
				if (wat!=1){
					count+=1;
					
				}
			
				
			}
			sweightsA[category]+=wat;

			s2.remove(j1);
			}else{
				wat=(learningRate * dat);
				if (wat==1){
					weightsA[category].remove(j1);
					upweightsA[category].remove(j1);
					
				}else{
				weightsA[category].put((Integer)j1,(Double) (1-wat));
				upweightsA[category].add(j1);
				count+=1;
				}
				sweightsA[category]+=wat;
				
	}

		}
		for (Integer i: s2) {

			if (i<snumFeatures){
				try{
					weightsA[category].remove(i);	

					upweightsA[category].remove(i);
				}catch(Exception e){
					e.getClass();
				}	
						
			}
			else{
				double wat=1-(Double)weightsA[category].get(i);
				sweightsA[category]+=wat;
				count+=1;
			}
		}
		
		sweightsA[category]+=snumFeatures-count;

		s1=labels.keySet();
		
		for (Integer i: s1) {
			double lb=labels.get(i);
			Double wb=(Double)weightsB[category].get(i);
			if(wb!=null){
		    if(weightblearnmethod== 0){
		    	weightsB[category].put(i, lb + wb);
	        weightChange = 1;
		    }else{
	      //  %normalise
	        if ( lb< wb){
		    	weightsB[category].put(i, (learningRate * lb )+ (1 - learningRate) *wb);  
	            weightChange = 1;
	            
	        }
		    }}
		}
		return weightChange;
	}

	
	

	private void ARAMm_Add_New_Category() {

		weightsA = Arrays.copyOf(weightsA, numCategories + 1);
		sweightsA = Arrays.copyOf(sweightsA, numCategories + 1);
		weightsB = Arrays.copyOf(weightsB, numCategories + 1);
		upweightsA = Arrays.copyOf(upweightsA, numCategories + 1);
		weightsA[numCategories] = new HashMap<Integer, Double>();
		weightsB[numCategories] = new HashMap<Integer, Double>();
		upweightsA[numCategories] = new HashSet();
		sweightsA[numCategories]=sweightsA0;
		numCategories += 1;

	}

	private double sumArray(double[] arr) {
		int num = arr.length;
		double result = 0;
		for (int i = 0; i < num; i++) {
			result += arr[i];
		}
		return result;
	}
  public static void main(String [] argv) {

    try {
    	Evaluation.runExperiment(((MultiLabelClassifier) new WvARAM()), argv);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
  }
  public double[][] distributionForInstanceM(Instances i) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

  
  class SortPair2  implements Comparable<SortPair2> {
		private int originalIndex;
		private double value;
		private double rawvalue;

		public SortPair2(double value, int originalIndex, double rawvalue) {
			this.value = value;
			this.originalIndex = originalIndex;
			this.rawvalue = rawvalue;
		}

		public int compareTo(SortPair2 o) {
			return Double.compare(o.getValue(), value);
		}

		public int getOriginalIndex() {
			return originalIndex;
		}

		public double getValue() {
			return value;
		}
		public double getRawValue() {
			return rawvalue;
		}

  }


@Override
public boolean isThreaded() {
	// TODO Auto-generated method stub
	return false;
}

@Override
public void setThreaded(boolean setv) {
	// TODO Auto-generated method stub
	
}

@Override
public String getModel() {
	// TODO Auto-generated method stub
	return null;
}

}











