/*
 *	ClassifierTemplate.java
 *
 *  <<Your Name Here>>
 *  CN 710
 *  Dept. of Cognitive & Neural Systems
 *  Boston University
 *  <<Date here>>
 *
 *  Copyright (c) 2006, Boston University 
 *  
 *  Adapted from NaiveBayes.java
 *  Copyright (C) 1999 Eibe Frank,Len Trigg
 */
package meka.classifiers.multilabel.neurofuzzy;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.Arrays;

import meka.classifiers.multilabel.*;
import weka.classifiers.Classifier;
//import weka.classifiers.Evaluation;
import meka.classifiers.multilabel.Evaluation;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import meka.core.MLUtils;
import meka.core.MultiLabelDrawable;
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
public class WARAM extends ARAMNetworkClass 
  implements  MultiLabelClassifierThreaded, OptionHandler, WeightedInstancesHandler, UpdateableClassifier, MultiLabelDrawable {

 //**** THIS IS WHERE CLASSIFIER WEIGHTS ETC GO ****
 //define stuff like weight matrices, classifier parameters etc.
 //e.g., protected double rho_a_bar=0.0;

    public WARAM(int fnumFeatures, int fnumClasses, double fro, double fthreshold) {
	initARAM(fnumFeatures, fnumClasses,  fro,  fthreshold);
	}

    public WARAM(){
    }
    private void initARAM(int fnumFeatures, int fnumClasses, double fro, double fthreshold){
		numFeatures = fnumFeatures;
		numClasses = fnumClasses;
		threshold = fthreshold;
		weightsA = new double[1][numFeatures];
		Arrays.fill(weightsA[0], 1);
		weightsB = new double[1][numClasses];
		Arrays.fill(weightsB[0], 0);
		numCategories = 1;



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
      // swap attributes to fit MEKA
		testCapabilities(D);

		int L = D.classIndex();
		int featlength =  (D.numAttributes() -L)*2;
		int numSamples = D.numInstances();
		int classlength = L * 2;

		System.out.println("Using rho="+roa);
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
	Enumeration enumInsts = D.enumerateInstances();
	while (enumInsts.hasMoreElements()) {
		Instance instance = (Instance) enumInsts.nextElement();
		updateClassifier(instance);
	}
    System.out.println("Training done, used "+numCategories+" neurons.");
	
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
		
		int num_classes=(int) (0.5 * numClasses);
		int num_features=(int) (0.5 * numFeatures);
		double[] data = new double[numFeatures];
		double[] labels = new double[numClasses];
		int numChanges = 0;
    if (!instance.classIsMissing()) {
     //Do the weight updates using the instance.

			for (int j = 0; j <num_features; j++) {
				data[j] = instance.value(num_classes+j);
				data[j+num_features] = 1 - data[j];
			}
			for (int j = 0; j < num_classes ; j++) {
				labels[j] = instance.value(j);
				labels[j+num_classes] = 1 - labels[j];
			}
			SortPair[] cateacti = ARTActivateCategories(data);java.util.Arrays.sort(cateacti);
			boolean resonance = false;
			int currentSortedIndex = 0;
			int currentCategory = -1;
			double matchA = 0;
			double matchB = 0;

			while (!resonance) {

				currentCategory = cateacti[currentSortedIndex]
						.getOriginalIndex();
				matchA = ART_Calculate_Match(data, weightsA[currentCategory]);
				if (sumArray(weightsB[currentCategory]) == 0) {
					matchB = 1;
				} else {
					matchB = ART_Calculate_Match(labels,
							weightsB[currentCategory]);

				}
				if (matchA >= roa && matchB >= rob) {
					if (currentCategory == numCategories -1) {

						if (currentSortedIndex == maxNumCategories) {
							System.out
									.println("WARNING: The maximum number of categories has been reached.");
							resonance = true;
						} else {
							// Add a new category
							for (int j = 0; j < data.length; j++) {
								weightsA[currentCategory][j] = data[j];
							}

							for (int j = 0; j < weightsB[currentCategory].length; j++) {
								weightsB[currentCategory][j] = labels[j];
							}
							ARAMm_Add_New_Category();
							// fprintf(FileID,'Add a new category of %d\n',
							// network.numCategories);
							// Increment the number of changes since we added a
							// new category.
							numChanges = numChanges + 1;
							resonance = true;
						}
					} else {
						// % Update weights
						double weightChange = ARAMm_Update_Weights(data,
								labels, currentCategory);
						if (weightChange == 1) {
							numChanges += 1;
						}

						resonance = true;
					}
				} else {
					currentSortedIndex += 1;
					resonance = false;
				}

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

      
		int num_classes=(int) (0.5 * numClasses);
		int num_features=(int) (0.5 * numFeatures);
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
			for (int i = 1; i < numCategories; i++) {
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
			for (int i = 0; i < largest_activ; i++) {
				best_matches[i] = best_matches[i] / sum_mat;
				currentCategory = sortedActivations[i].getOriginalIndex();
				// % Fill return vector with weightB values
				for (int j = 0; j < num_classes; j++) {
					ranking[j] = ranking[j]
							+ best_matches[i] * weightsB[currentCategory][j];
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
		SortPair[] catacti = new SortPair[numCategories];
		// double[] catacti=new double[numCategories];
		double[] matchVector = new double[numFeatures];
		for (int i = 0; i < numCategories; i++) {
			double sumvector = 0;
			double sumweight = 0;
			for (int j = 0; j < numFeatures; j++) {
				matchVector[j] = ((Data[j] < weightsA[i][j]) ? Data[j]
						: weightsA[i][j]);
				sumvector += matchVector[j];
				sumweight += weightsA[i][j];
			}

			catacti[i] = new SortPair(sumvector / (alpha + sumweight), i);
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
		for (int i = 0; i < numFeatures; i++) {
			if (data[i] < weightsA[category][i]){
			weightsA[category][i] = (learningRate * data[i])
					+ (1 - learningRate) * weightsA[category][i];
			}

		}
		for (int i = 0; i < numClasses; i++) {
		    if(weightblearnmethod== 0){
		    	weightsB[category][i] = labels[i] + weightsB[category][i];
	        weightChange = 1;
		    }else{
	      //  %normalise
	        if ( labels[i]< weightsB[category][i]){
	        	weightsB[category][i]  = (learningRate * labels[i] )+ (1 - learningRate) *weightsB[category][i];  
	            weightChange = 1;
	        }
		    }
		}
		return weightChange;
	}

	private double ART_Calculate_Match(double[] Data, double[] fweights) {

		int lnumFeatures = Data.length;
		if (lnumFeatures != fweights.length) {
			return 0.0;
		}
		double[] matchVector = new double[lnumFeatures];
		double summatch = 0;
		double suminput = 0;
		for (int j = 0; j < lnumFeatures; j++) {
			matchVector[j] = ((Data[j] < fweights[j]) ? Data[j] : fweights[j]);
			summatch += matchVector[j];
			suminput += Data[j];
		}
		if (suminput == 0) {
			return 0.0;
		}
		return summatch / suminput;
	}

	private void ARAMm_Add_New_Category() {

		weightsA = Arrays.copyOf(weightsA, numCategories + 1);
		weightsB = Arrays.copyOf(weightsB, numCategories + 1);
		weightsA[numCategories] = new double[numFeatures];
		weightsB[numCategories] = new double[numClasses];
		Arrays.fill(weightsA[numCategories], 1.0);
		Arrays.fill(weightsB[numCategories], 0.0);
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
    	Evaluation.runExperiment((MultiLabelClassifier) new WARAM(), argv);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
  }

@Override
public String getModel() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Map<Integer, Integer> graphType() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Map<Integer, String> graph() throws Exception {
	// TODO Auto-generated method stub
	return null;
}

@Override
public void setDebug(boolean debug) {
	// TODO Auto-generated method stub
	
}

@Override
public boolean getDebug() {
	// TODO Auto-generated method stub
	return false;
}

@Override
public String debugTipText() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Capabilities getCapabilities() {
	// TODO Auto-generated method stub
	return null;
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
}








class SortPair implements Comparable<SortPair> {
	private int originalIndex;
	private double value;

	public SortPair(double value, int originalIndex) {
		this.value = value;
		this.originalIndex = originalIndex;
	}

	public int compareTo(SortPair o) {
		return Double.compare(o.getValue(), value);
	}

	public int getOriginalIndex() {
		return originalIndex;
	}

	public double getValue() {
		return value;
	}
}






