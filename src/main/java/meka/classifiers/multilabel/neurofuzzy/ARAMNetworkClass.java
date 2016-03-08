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
import java.util.List;
import java.util.Vector;
import java.util.Arrays;
import java.util.HashMap;



import meka.classifiers.multilabel.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
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
public abstract class  ARAMNetworkClass extends SingleClassifierEnhancer implements MultiLabelClassifierThreaded,
IncrementalMultiLabelClassifier{

    public int numFeatures = -1;
    public int numClasses = -1;
    public double threshold=0.02;
    int numCategories = 0;
    double roa = 0.9;
    double rob = 1.0;
    double alpha = 0.0001;
    double[][] weightsA = null;
    double[][] weightsB = null;
    double[] sweightsA =null;
    double sweightsA0=0;
    int learningRate = 1;
    int weightblearnmethod= 0;
    int maxNumCategories = 20000;
    boolean m_userankstoclass=false;
	boolean learningphase=true;
	protected int[] neuronsactivated=null;
	protected double[] neuronsactivity=null;
	List<Integer> order=null;
	int nrinstclassified=0;
	protected String activity_report="";


 //**** THIS IS WHERE CLASSIFIER WEIGHTS ETC GO ****
 //define stuff like weight matrices, classifier parameters etc.
 //e.g., protected double rho_a_bar=0.0;

  /**
   * Returns a string describing this classifier
   * @return a description of the classifier suitable for
   * displaying in the explorer/experimenter gui.
   * ****MODIFY WITH CORRECT INFORMATION****
   */
  /**
   * Generates the classifier.
   *
   * @param instances set of instances serving as training data 
   * @exception Exception if the classifier has not been generated 
   * successfully
   */
  
  //public int[] getneuronsactivated();
  
 // public double[] getneuronsactivity();

	
	public void testCapabilities(Instances D) throws Exception {
		// get the classifier's capabilities, enable all class attributes and do the usual test
		Capabilities cap = getCapabilities();
		cap.enableAllClasses();
		//getCapabilities().testWithFail(D);
		// get the capabilities again, test class attributes individually
		int L = D.classIndex();
		for(int j = 0; j < L; j++) {
			Attribute c = D.attribute(j);
			cap.testWithFail(c,true);
		}
	}

}











