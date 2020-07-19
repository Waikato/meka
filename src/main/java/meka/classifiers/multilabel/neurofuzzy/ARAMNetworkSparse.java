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
import java.util.Enumeration;
import java.util.Vector;
import java.util.Arrays;
import java.util.HashMap;

import meka.classifiers.multilabel.*;
import weka.classifiers.Classifier;
import meka.classifiers.multilabel.Evaluation;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import meka.core.MLUtils;
import weka.core.WeightedInstancesHandler;
import weka.core.RevisionUtils;

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
public class ARAMNetworkSparse extends ARAMNetworkClass {

 //**** THIS IS WHERE CLASSIFIER WEIGHTS ETC GO ****
 //define stuff like weight matrices, classifier parameters etc.
 //e.g., protected double rho_a_bar=0.0;

    SparseVector[] weightsA = null;
    double[] sweightsA = null;
    double sweightsA0;
    SparseVector[] weightsB = null;
	HashMap<String, Vector> hmclasses = null;
	int snumFeatures=0;
	int snumClasses=0;
	int numinstances=0;


    public ARAMNetworkSparse(int fnumFeatures, int fnumClasses, double fro, double fthreshold) {
	initARAM(fnumFeatures, fnumClasses,  fro,  fthreshold);
	}

    public ARAMNetworkSparse(){
    }
    private void initARAM(int fnumFeatures, int fnumClasses, double fro, double fthreshold){
		numFeatures = fnumFeatures;
		snumFeatures = (int)(0.5*numFeatures);
		numClasses = fnumClasses;
		snumClasses= (int)(0.5*numClasses);
		threshold = fthreshold;
		weightsA = new SparseVector[1];
		weightsA[0] = new SparseVector(numFeatures);
		sweightsA = new double[1];
		sweightsA[0]=0;
		for(int i=0;i<numFeatures;i++){
			sweightsA[0]+=1;
		}
		sweightsA0=sweightsA[0];
		weightsB = new SparseVector[1];
		weightsB[0] = new SparseVector(snumClasses);
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
   * @param D set of instances serving as training data
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
		int num_features=(int) (snumFeatures);
		double[] data = new double[num_features];
		double[] labels = new double[num_classes];
		int numChanges = 0;
		int numCategories_1=numCategories -1;
		numinstances+=1;
		
    if (!instance.classIsMissing()) {
     //Do the weight updates using the instance.

			double suminputA=0;
			double suminputB=0;
			for (int j = 0; j <num_features; j++) {
				data[j] = instance.value(num_classes+j);
				suminputA+=1;
				//if (data[j]<0 || data[j]>1){
				//	System.out.println("Data not normalized, this will cause error!");
				//}
				
			}
			for (int j = 0; j < num_classes ; j++) {
				labels[j] = instance.value(j);
				suminputB+=labels[j];
			}
			
			SortPair[] cateacti = ARTActivateCategories(data,labels);
			java.util.Arrays.sort(cateacti);
			boolean resonance = false;
			int currentSortedIndex = 0;
			int currentCategory = -1;
			double matchA = 0;
			double matchB = 0;

			
			while (!resonance && currentSortedIndex<cateacti.length) {

				currentCategory = cateacti[currentSortedIndex]
						.getOriginalIndex();
				if (currentCategory == numCategories_1) {
					matchB=1;
					matchA=1;
				}
				else{
				matchA = ART_Calculate_MatchA(data, weightsA[currentCategory],suminputA);
				if (weightsB[currentCategory].sum() == 0) {
					matchB = 1;
				} else {
					matchB = ART_Calculate_MatchB(labels,
							weightsB[currentCategory],suminputB);

				}
					
				}
				if (matchA >= roa && matchB >= rob) {
					if (currentCategory == numCategories_1) {

						if (currentSortedIndex == maxNumCategories) {
							System.out
									.println("WARNING: The maximum number of categories has been reached.");
							resonance = true;
						} else {
							// Add a new category
							sweightsA[currentCategory]=0;
							for (int j = 0; j < snumFeatures; j++) {
								weightsA[currentCategory].put(j,data[j]);
								sweightsA[currentCategory]+=data[j];
							}
							for (int j = snumFeatures; j < numFeatures; j++) {
								double da=data[j-snumFeatures];
								weightsA[currentCategory].put(j, da);
								sweightsA[currentCategory]+=1-da;
								
							
							}

							for (int j = 0; j < weightsB[currentCategory].size(); j++) {
								weightsB[currentCategory].put(j,labels[j]);
							}
							String s = Arrays.toString(labels);
							if (hmclasses.containsKey(s)){
								hmclasses.get(s).add(currentCategory);
							hmclasses.put(s,hmclasses.get(s)); 
							}else{
								Vector v = new Vector();
								v.add(currentCategory);
								hmclasses.put(s,v);
							}
							ARAMm_Add_New_Category();
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
				for (int j = 0; j < snumFeatures; j++) {
					weightsA[numCategories_1].put(j, data[j]);
					sweightsA[numCategories_1]+=data[j];
				}
				for (int j = snumFeatures; j < numFeatures; j++) {
					double da=data[j-snumFeatures];
					weightsA[numCategories_1].put(j, da);
					sweightsA[numCategories_1]+=1-da;
					
				
				}

				for (int j = 0; j < weightsB[numCategories_1].size(); j++) {
					weightsB[numCategories_1].put(j, labels[j]);
				}
				String s = Arrays.toString(labels);
				if (hmclasses.containsKey(s)){
					hmclasses.get(s).add(numCategories_1);
				hmclasses.put(s,hmclasses.get(s)); 
				}else{
					Vector v = new Vector();
					v.add(numCategories_1);
					hmclasses.put(s,v);
				}
				ARAMm_Add_New_Category();
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
		int num_features=(int) (snumFeatures);
		double[] dist = new double[num_classes];
		double[] currentData = new double[numFeatures];
		double[] ranking = new double[num_classes];
			for (int j = 0; j < num_features; j++) {
				currentData[j] = instance.value(num_classes+j);
				currentData[num_features+j] = 1 - currentData[j];
			}
			SortPair[] sortedActivations = ARTActivateCategories(currentData);

			java.util.Arrays.sort(sortedActivations);
			double diff_act = sortedActivations[0].getValue()
					- sortedActivations[numCategories - 2].getValue();
			int largest_activ = 1;
			double activ_change = 0;
			for (int i = 1; i < sortedActivations.length; i++) {
				activ_change = (sortedActivations[0].getValue() - sortedActivations[i]
						.getValue())
						/ sortedActivations[0].getValue();
				if (activ_change > threshold * diff_act) {
					break;
				}
				largest_activ = largest_activ + 1;
			}
			// % largest_activ =5;
			double[] best_matches = new double[largest_activ];
			java.util.Arrays.fill(best_matches, 1);
			for (int i = 0; i < largest_activ; i++) {
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
				for (int j = 0; j < num_classes; j++) {
					ranking[j] = ranking[j]
							+ best_matches[i] * weightsB[currentCategory].get(j);
				}
			}
			if(m_userankstoclass) {
				return ARAMm_Ranking2Class(ranking);
				
			}
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
	private SortPair[] ARTActivateCategories(double[] Data) {
		SortPair[] catacti = new SortPair[numCategories-1];
		// double[] catacti=new double[numCategories];
		double[] matchVector = new double[numFeatures];
		for (int i = 0; i < numCategories-1; i++) {
			double sumvector = 0;
		//	double sumweight = 0;
			for (int j = 0; j < (int)snumFeatures; j++) {
				double wa=weightsA[i].get(j);
				matchVector[j] = ((Data[j] < wa) ? Data[j]
						: wa);
				sumvector += matchVector[j];
				
			//	sumweight += weightsA[i][j];
			}
			for (int j = snumFeatures; j < numFeatures; j++) {

				double wa=1-weightsA[i].get(j);
				double da=1-Data[j-snumFeatures];
				matchVector[j] = (((da) < wa) ? da: wa);
				sumvector += matchVector[j];
			}
			//sumweight=sweightsA[i]; 
			catacti[i] = new SortPair(sumvector / (alpha + sweightsA[i]), i);
		//	catacti[i] = new SortPair(sumvector / (alpha + sumweight), i);
			//System.out.println("sumweight "+(sumweight-sweightsA[i]));
		}
		return catacti;
	}
	private SortPair[] ARTActivateCategories(double[] Data, double[] labels) {
		String s = Arrays.toString(labels);
		Vector lclasses = hmclasses.get(s);
		SortPair[] catacti = null;
		if (lclasses==null||lclasses.size()==0){
			catacti=new SortPair[1];
			catacti[0] = new SortPair(1,numCategories-1);
			return catacti;
		}
		catacti = new SortPair[lclasses.size()];
		// double[] catacti=new double[numCategories];
		double[] matchVector = new double[numFeatures];
		for (int i = 0; i < lclasses.size(); i++) {
			double sumvector = 0;
		//	double sumweight = 0;
			int k = ((Integer)lclasses.get(i)).intValue();
			for (int j = 0; j < snumFeatures; j++) {
				double wa=weightsA[k].get(j);
				matchVector[j] = ((Data[j] < wa) ? Data[j]
						: wa);
				sumvector += matchVector[j];
			//	sumweight += weightsA[k][j];
			}
			for (int j = snumFeatures; j < numFeatures; j++) {

				double wa=1-weightsA[k].get(j);
				double da=1-Data[j-snumFeatures];
				matchVector[j] = (((da) < wa) ? da: wa);
				sumvector += matchVector[j];
			}
			
			//sumweight=sweightsA[k];
			//catacti[i] = new SortPair(sumvector / (alpha + sumweight), k);
			//System.out.println("sumweight "+(sumweight-sweightsA[k]));
			catacti[i] = new SortPair(sumvector / (alpha + sweightsA[k]), k);
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
  public Enumeration listOptions() {
   //These are just examples, modify to suit your algorithm
    Vector newVector = new Vector(2);

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
  

 //****MORE GUI RELATED STUFF AND PARAMETER ACCESS METHODS
//  /**
//   * Returns the tip text for this property
//   * @return tip text for this property suitable for
//   * displaying in the explorer/experimenter gui
//   */
//  public String useKernelEstimatorTipText() {
//    return "Use a kernel estimator for numeric attributes rather than a "
//      +"normal distribution.";
//  }
//  /**
//   * Gets if kernel estimator is being used.
//   *
//   * @return Value of m_UseKernelEstimatory.
//   */
//  public boolean getUseKernelEstimator() {
//    
//    return m_UseKernelEstimator;
//  }
//  
//  /**
//   * Sets if kernel estimator is to be used.
//   *
//   * @param v  Value to assign to m_UseKernelEstimatory.
//   */
//  public void setUseKernelEstimator(boolean v) {
//    
//    m_UseKernelEstimator = v;
//    if (v) {
//      setUseSupervisedDiscretization(false);
//    }
//  }
//  
//  /**
//   * Returns the tip text for this property
//   * @return tip text for this property suitable for
//   * displaying in the explorer/experimenter gui
//   */
//  public String useSupervisedDiscretizationTipText() {
//    return "Use supervised discretization to convert numeric attributes to nominal "
//      +"ones.";
//  }
//
//  /**
//   * Get whether supervised discretization is to be used.
//   *
//   * @return true if supervised discretization is to be used.
//   */
//  public boolean getUseSupervisedDiscretization() {
//    
//    return m_UseDiscretization;
//  }
//  
//  /**
//   * Set whether supervised discretization is to be used.
//   *
//   * @param newblah true if supervised discretization is to be used.
//   */
//  public void setUseSupervisedDiscretization(boolean newblah) {
//    
//    m_UseDiscretization = newblah;
//    if (newblah) {
//      setUseKernelEstimator(false);
//    }
//  }
  
  /**
   * Main method for testing this class.
   *
   * @param argv the options
   */
	private double ARAMm_Update_Weights(double[] data, double[] labels,
			int category) {
		double weightChange = 0;
		sweightsA[category]=0;
		for (int i = 0; i < snumFeatures; i++) {
			double wa=weightsA[category].get(i);
			if (data[i] < wa ){
			wa = (learningRate * data[i])
					+ (1 - learningRate) * wa;
			weightsA[category].put(i, wa);
			}
			sweightsA[category]+=wa;

		}
		//above
		for (int i = snumFeatures; i < numFeatures; i++) {
			double wa=1-weightsA[category].get(i);
			double da=1-data[i-snumFeatures];
			if (da < wa ){
			wa = (learningRate * da)
					+ (1 - learningRate) * wa;
			weightsA[category].put(i, 1-wa);
			}
			sweightsA[category]+=wa;

		}
		for (int i = 0; i < snumClasses; i++) {
			double wb=weightsB[category].get(i);
		    if(weightblearnmethod== 0){
		    	weightsB[category].put(i, labels[i] + wb);
	        weightChange = 1;
		    }else{
	      //  %normalise
	        if ( labels[i]< wb){
		    	weightsB[category].put(i, (learningRate * labels[i] )+ (1 - learningRate) *wb);  
	            weightChange = 1;
	        }
		    }
		}
		return weightChange;
	}

	private double ART_Calculate_MatchA(double[] Data, SparseVector fweights, double suminput
			) {

		if (suminput == 0) {
			return 0.0;
		}
		int lnumFeatures = Data.length;
		if (lnumFeatures*2 != fweights.size()) {
			return 0.0;
		}
		double[] matchVector = new double[numFeatures];
		double summatch = 0;
		//double suminput = 0;
		for (int j = 0; j < lnumFeatures; j++) {
			double w = fweights.get(j);
			matchVector[j] = ((Data[j] < w) ? Data[j] :w);
			summatch += matchVector[j];
			//suminput += Data[j];
		}
		for (int j = snumFeatures; j < numFeatures; j++) {

			double w =1- fweights.get(j);
			double da= 1-Data[j-snumFeatures];
			matchVector[j] = ((da< w) ? da :w);
			summatch += matchVector[j];
		}
//		if (suminput == 0) {
//			return 0.0;
//		}
		return summatch / suminput;
	}

	private double ART_Calculate_MatchB(double[] Data, SparseVector fweights, double suminput
			) {

		if (suminput == 0) {
			return 0.0;
		}
		int lnumFeatures = Data.length;
		if (lnumFeatures != fweights.size()) {
			return 0.0;
		}
		double[] matchVector = new double[lnumFeatures];
		double summatch = 0;
		//double suminput = 0;
		for (int j = 0; j < lnumFeatures; j++) {
			double w = fweights.get(j);
			matchVector[j] = ((Data[j] < w) ? Data[j] :w);
			summatch += matchVector[j];
			//suminput += Data[j];
		}
		return summatch / suminput;
	}

	private void ARAMm_Add_New_Category() {

		weightsA = Arrays.copyOf(weightsA, numCategories + 1);
		sweightsA = Arrays.copyOf(sweightsA, numCategories + 1);
		weightsB = Arrays.copyOf(weightsB, numCategories + 1);
		weightsA[numCategories] = new SparseVector((int)numFeatures);
		//sweightsA[numCategories] = new double();
		weightsB[numCategories] = new SparseVector((int)snumClasses);
		//Arrays.fill(weightsA[numCategories], 1.0);
		//Arrays.fill(weightsB[numCategories], 0.0);
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
public double[][] distributionForInstanceM(Instances i) throws Exception {
	// TODO Auto-generated method stub
	return null;
}

@Override
public String getModel() {
	// TODO Auto-generated method stub
	return null;
}
  

}











