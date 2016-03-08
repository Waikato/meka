/*
 *	ClassifierTemplate.java
 *
 *
 *  Copyright (c) 2016, Fernando Benites 
 *  
 *  Adapted from NaiveBayes.java
 *  Copyright (C) 1999 Eibe Frank,Len Trigg
 */
package meka.classifiers.multilabel.neurofuzzy;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;



import meka.classifiers.multilabel.*;
import weka.classifiers.Classifier;
//import weka.classifiers.Evaluation;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.neurofuzzy.ARAMNetworkClass;
import meka.classifiers.multitarget.MultiTargetClassifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;


/**
 * ****REPLACE THE FOLLOWING WITH SIMILAR INFORMATION.
 * Class for a ML-ARAM classifier.
 * <p> *
 * Valid options are:<p>
 *
 * -P <br>
 * Use a different generalization value.<p>
 * 
 * -K <br>
 * Use ML rankings to class.<p>
 *
 * -V <br>
 * Define the number of voters.<p>
 *
 * @author Fernando Benites (Fernando.Benites@uni.konstanz.de)
 * @version $Revision: 1.0 $
 */
public class WvARAM extends ARAMNetworkClass 
  implements OptionHandler, WeightedInstancesHandler, UpdateableClassifier, Randomizable,
  TechnicalInformationHandler, MultiLabelClassifier
  {

 //**** THIS IS WHERE CLASSIFIER WEIGHTS ETC GO ****
 //define stuff like weight matrices, classifier parameters etc.
 //e.g., protected double rho_a_bar=0.0;

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3824500093693707048L;
	ARAMNetworkClass[] networks=null;
	int numberofnetworks=5;
	int numClasses=0;
	double roa=0.9;
	boolean m_userankstoclass=false;
    boolean fastaram=true;
    boolean sparsearam=false;
    boolean sparsearamH=false;
    boolean sparsearamHT=false;
    boolean tfastaram=true;
    int m_seed = 42;
	DistributionCalc[] dc=null ;
	long intclass =0;
	PrintWriter fwneurons=null;
	boolean saveneuronsactivity=false;
	String fsna=null;
	protected String activity_report="";

    public WvARAM(int fnumberofnetworks) {
    	numberofnetworks=fnumberofnetworks;
	}

    public WvARAM(){
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
		//testCapabilities(D);
		Random r = new Random(m_seed);
		// System.out.println("Version TNG"+tfastaram);
		if (fastaram){
			networks = new ARAMNetworkfast[numberofnetworks];
		}
		else if(sparsearam){
				networks = new ARAMNetworkSparse[numberofnetworks];
		}
		else if(sparsearamH){
					networks = new ARAMNetworkSparseV[numberofnetworks];
		}else if(sparsearamHT){
			networks = new ARAMNetworkSparseHT[numberofnetworks];
}else{
			networks = new ARAMNetwork[numberofnetworks];
		}
		numClasses = D.classIndex();


	// Copy the instances so we don't mess up the original data.
	// Function calls do not deep copy the arguments..
	//Instances m_Instances = new Instances(instances);
	
	// Use the enumeration of instances to train classifier.
	// Do any sanity checks (e.g., missing attributes etc here
	// before calling updateClassifier for the actual learning
		
		if (tfastaram){
			BuildClassifier[] bc= new BuildClassifier[numberofnetworks];
			for (int i=0; i< numberofnetworks;i++){
				List<Integer> list = new ArrayList<Integer>();
				for (int j=0; j<D.numInstances();j++){
					list.add(j);
				}
				java.util.Collections.shuffle(list,r);
				if (fastaram){
			networks[i]=new ARAMNetworkfast();
				}
				else if(sparsearam){
					networks[i]=new ARAMNetworkSparse();
				}
				else if(sparsearamH){
					networks[i]=new ARAMNetworkSparseV();
				}
				else if(sparsearamHT){
					networks[i]=new ARAMNetworkSparseHT();
				}
			else{

				networks[i]=new ARAMNetwork();
				
			}
				networks[i].order=list;
		networks[i].roa=roa;
		  bc[i] = new BuildClassifier(networks[i]);
		  
		bc[i].setinstances(D);
		bc[i].start();
		//D.randomize(r);
			}

			  for (int i=0; i< numberofnetworks;i++){
				  bc[i].join();
					 networks[i]=bc[i].m_network;

						networks[i].learningphase=false;
					
				}
			  
			  
		}
		else{
		for (int i=0; i< numberofnetworks;i++){
				if (fastaram){
			networks[i]=new ARAMNetworkfast();
				}
				else if(sparsearam){
					networks[i]=new ARAMNetworkSparse();
				}
				else if(sparsearamH){
					networks[i]=new ARAMNetworkSparseV();
				}
				else if(sparsearamHT){
					networks[i]=new ARAMNetworkSparseHT();
				}
			else{

				networks[i]=new ARAMNetwork();
				
			}
			networks[i].roa=roa;
			networks[i].buildClassifier(D);
			networks[i].learningphase=false;
			D.randomize(r);
		}
		}
	dc =new  DistributionCalc[numberofnetworks];
	// Alternatively, you can put the training logic within this method,
	// rather than updateClassifier(...). However, if you omit the 
	// updateClassifier(...) method, you should remove 
	// UpdateableClassifier from the class declaration above.
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

	  double[] dist = new double[numClasses];
	//	long before_test = System.currentTimeMillis();
		//long after_test=0;
		//long after_test1=0;
	  int donetime=0;
	  boolean[] ddone=new boolean[numberofnetworks];
	  if(saveneuronsactivity && fwneurons==null){
			try {
		    fwneurons = new PrintWriter(new BufferedWriter(new FileWriter(fsna)));
			} catch (IOException e) {
				e.printStackTrace();

				System.exit(1);
			    //exception handling left as an exercise for the reader
			}
		  
	  }
	  if (tfastaram){
	//	  System.out.println("fastaram");
	  for (int i=0; i< numberofnetworks;i++){
		  if (dc[i]==null){
			  networks[i].activity_report=activity_report;
	  dc[i] = new DistributionCalc(networks[i]);
	  dc[i].id=i;
	  
		  }
	  dc[i].setinstance(instance);
	  }
	  for (int i=0; i< numberofnetworks;i++){
		  ddone[i]=false;
	  if(dc[i].reuse){
			dc[i].gathered=false;
			synchronized(dc[i]){
		  dc[i].doNotify();
			}
	  }else{
			dc[i].gathered=false;
		  dc[i].start();
	  }
	//  System.out.println("start "+i+" "+dc[i].do_classify+" "+dc[i].gathered+ " "+intclass);
	  }
	//	after_test = System.currentTimeMillis();
	  int clsnr=0;
	  int counter=0;
	  while(clsnr<numberofnetworks){
	  for (int i=0; i< numberofnetworks;i++){
			synchronized(dc[i]){
		if(dc[i].do_classify==2 && dc[i].gathered!=true){
			clsnr+=1;
		//	dc[i].suspend();
			  for (int j=0; j< numClasses;j++){
					dist[j]+=dc[i].m_dist[j];
					}
				dc[i].gathered=true;
				ddone[i]=true;
				donetime+=counter;
				if(saveneuronsactivity){
					
						//fwneurons.println(dc[i].m_network.nrinstclassified+":"+i+":"+Arrays.toString(dc[i].m_network.getneuronsactivity())+":"+Arrays.toString(dc[i].m_network.getneuronsactivity()));
				}
			
		}
			}
	  }
	  if (clsnr==numberofnetworks){
		  break;
	  }
	  Thread.sleep(1);
	  counter+=1;
	  if((clsnr)/((float)numberofnetworks)>0.75 && counter>20 && counter==2*(donetime/((float)clsnr))){
		  for (int i=0; i< numberofnetworks;i++){
			  if (ddone[i]){
				  continue;
			  }
				synchronized(dc[i]){
			if (dc[i].sleep&& dc[i].do_classify!=2){
				  System.out.println("Error in counter at "+intclass+" clr "+clsnr+" counter "+counter+ "restarting "+i);
				dc[i].notify();
				}
			}
		  }
	  }
	  if(counter%1000==0 && counter >0){
		  
	  }
	  if(this.getDebug()) { 
	  if(counter%100==0){
		  System.out.println("Error in counter at "+intclass+" clr "+clsnr+" counter "+counter);
		  
	  }
	  }
	  }
	  }
	  else{
		//	after_test = System.currentTimeMillis();
			for (int i=0; i< numberofnetworks;i++){

				  networks[i].activity_report=activity_report;
			double[] tdist=networks[i].distributionForInstance(instance);
			for (int j=0; j< numClasses;j++){
			dist[j]+=tdist[j];
			}
			if(saveneuronsactivity){
	/*			int[] naia=networks[i].getneuronsactivated();
		//		fwneurons.println(networks[i].nrinstclassified-1+":"+i+":"+Arrays.toString(naia)+":"+Arrays.toString(networks[i].getneuronsactivity()));
				for(int it=0;it<naia.length;it++){
					fwneurons.print(naia[it]+"#value:");					
					int t1=0;
					for(int k=0;k<networks[i].weightsB[naia[it]].length/2;k++){
						if (networks[i].weightsB[naia[it]][k]!=0){
							if(t1==0){
								fwneurons.print(networks[i].weightsB[naia[it]][k]+"#positions:");
								t1=1;
							}
							fwneurons.print(k+",");
						}
					}
					fwneurons.print("\n");
					
				//fwneurons.println(naia[it]+":"+Arrays.toString(Arrays.copyOfRange(networks[i].weightsB[naia[it]],0,networks[i].numClasses/2)));
				}*/
		}
		}
		  
	  }


 // after_test1 = System.currentTimeMillis();
	//	System.out.println("start:"+(after_test-before_test)/1000.0);
	//	System.out.println("testing:"+(after_test1-after_test)/1000.0);
	  intclass+=1;
	  if(this.getDebug()) { if (intclass%100==0){
		  System.out.println(".");
		  
	  }}
		if(m_userankstoclass) {
			return ARAMm_Ranking2Class(dist);
			
		}
	  return dist;
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
	@Override
  public Enumeration listOptions() {
   //These are just examples, modify to suit your algorithm
    Vector newVector = new Vector();

    newVector.addElement(
	    new Option("\tChange generalization parameter Rho\n",
			       "P", 0,"-P"));
    newVector.addElement(
    	    new Option("\tChange threshold to select activated neurons (no-winner-takes-all)\n",
    			       "THR", 0,"-THR"));
    newVector.addElement(
	    new Option("\tUse ranking to class function special dev. for ARAM.\n",
		       "K", 0,"-K"));
    newVector.addElement(
    	    new Option("\tFast ARAM.\n",
    		       "F", 0,"-F"));
    newVector.addElement(
    new Option("\tthreaded ARAM.\n",
		       "TF", 0,"-TF"));
	newVector.addElement(new Option("\tVotersr\n\t ", "V", 5, "-V <value>"));
	newVector.addElement(new Option("\t Save neurons activity ARAM.\n",
		       "NA", 0,"-NA"));
	newVector.addElement(new Option("\t Save neurons activity in network ARAM.\n",
		       "Rt", 0,"-Rt"));
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
      m_userankstoclass= Utils.getFlag("K",options);
      numberofnetworks = (Utils.getOptionPos("V",options) >= 0) ? Integer.parseInt(Utils.getOption("V", options)) : numberofnetworks;
      threshold = (Utils.getOptionPos("THR",options) >= 0) ? Double.parseDouble(Utils.getOption("THR", options)) : threshold;
      fastaram = Utils.getFlag("F",options);
      sparsearam = Utils.getFlag("Sp",options);
      sparsearamH = Utils.getFlag("SpH",options);
      sparsearamHT = Utils.getFlag("SpHT",options);
      tfastaram = Utils.getFlag("TF",options);
      fsna = Utils.getOption("NA", options);
      activity_report = (Utils.getOptionPos("Rt",options) >= 0) ? Utils.getOption("Rt", options) : "";
      if (fsna!=null && fsna!=""){
    	  saveneuronsactivity=true;
      }
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
   // String [] options = null;

	    
	    Vector<String> result = new Vector<String>();

	    result.add("-P");
	    result.add(Double.toString(roa));
	    result.add("-THR");
	    result.add(Double.toString(threshold));

	    if (m_userankstoclass)
	    	result.add("-K");
	    
	    result.add("-V");
	    result.add(Integer.toString(numberofnetworks));
	    
	    if (fastaram)
	    	result.add("-F");
	    if (sparsearam)
	    	result.add("-Sp");
	    if (sparsearamH)
	    	result.add("-SpH");
	    if (sparsearamHT)
	    	result.add("-SpHT");
	    if (tfastaram)
	    	result.add("-TF");
	      if (fsna!=null && fsna!="" && fsna.length()>0){
	    result.add("-NA");
	    result.add(""+ fsna);
	      }
		  if (!activity_report.isEmpty() ){
			  result.add("-Rt");
			  result.add("" +activity_report);
		  }
	    
	   // Collections.addAll(result, super.getOptions());
	    
	    return (String[]) result.toArray(new String[result.size()]);	

//    try{
//	options =weka.core.Utils.splitOptions("-P "+roa+(m_userankstoclass?" -K":"")+" -V "+numberofnetworks);
//    }catch (Exception ex) {
//	System.out.println(ex.getMessage());
//    }
//    return options;
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
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	//@Override
	public String globalInfo() {
		return "Voted ML-ARAM. " + "For more information see:\n" + getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		//TechnicalInformation	additional;
		
		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Fernando Benites");
		result.setValue(Field.TITLE, "HARAM: a Hierarchical ARAM neural network for large-scale text classification.");
		result.setValue(Field.BOOKTITLE, "HDM 2015, 3rd International Workshop on High "+
                "Dimensional Data Mining, In conjunction with"+
                "the IEEE International Conference on Data"+
                "Mining (IEEE ICDM 2015), 14 November 2015");
		result.setValue(Field.YEAR, "2015");
		  
		return result;
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
  

@Override
public void updateClassifier(Instance instance) throws Exception {
	// TODO Auto-generated method stub
	for (int i=0; i< numberofnetworks;i++){
		networks[i].updateClassifier(instance);		
	}
}
@Override
public void setSeed(int seed) {
	m_seed=seed;
	
	
}

@Override
public int getSeed() {
	
	return m_seed;
}

public boolean getThreadedVoters(){
	
	return tfastaram;
}

public void setThreadedVoters(boolean fThreadedVoters){
	tfastaram=fThreadedVoters;
}

public boolean getFastLearn(){
	
	return fastaram;
}

public void setFastLearn(boolean ffastaram){
	fastaram=ffastaram;
	sparsearam=!ffastaram;
	sparsearamH=!ffastaram;
	sparsearamHT=!ffastaram;
}
public boolean getSparse(){
	
	return sparsearam;
}

public void setSparse(boolean fsparsearam){
	fastaram=!fsparsearam;
	sparsearam=fsparsearam;
	sparsearamH=!fsparsearam;
	sparsearamHT=!fsparsearam;
}

public boolean getSparseH(){
	
	return sparsearamH;
}

public void setSparseHT(boolean fsparsearam){
	fastaram=!fsparsearam;
	sparsearam=!fsparsearam;
	sparsearamH=!fsparsearam;
	sparsearamHT=fsparsearam;
}
public boolean getSparseHT(){
	
	return sparsearamHT;
}

public void setSparseH(boolean fsparsearam){
	sparsearamH=fsparsearam;
}

public int getVoters(){
	
	return numberofnetworks;
}

public void setVoters(int fvoter){
	numberofnetworks=fvoter;
}


public double getVigilancy(){
	
	return roa;
}

public void setVigilancy(double vigilancy){
	roa=vigilancy;
}

public void setThreshold(double fthreshold){
	threshold=fthreshold;
}

public double getThreshold(){
	
	return threshold;
}


public boolean getNeuronsActivity(){
	
	return saveneuronsactivity;
}

public void setNeuronsActivity(boolean fsaveneuronsactivity){
	saveneuronsactivity=fsaveneuronsactivity;
}

public String getNeuronsActivityFileName(){
	
	return fsna;
}

public void setNeuronsActivityFileName(String ffsna){
	fsna=ffsna;
}

public void freeNetworks (){
	if(tfastaram){
	for(int i=0;i<dc.length;i++){
		dc[i].destroy();		
		dc[i]=null;
	}
	dc=null;
	}
}

public void destroy(){
	freeNetworks();
	networks=null;
	
}
/**
 * Main method for testing this class.
 *
 * @param argv the options
 */
public static void main(String [] argv) {

  try {
  	Evaluation.runExperiment(new WvARAM(), argv);
  } catch (Exception e) {
    e.printStackTrace();
    System.err.println(e.getMessage());
  }
  System.out.println("Done");
}
public class DistributionCalc extends Thread
{
	ARAMNetworkClass m_network =null;
	Instance m_inst=null;
	double[] m_dist=null;
	int do_classify=0;
	boolean reuse=false;
	boolean sleep=true;
	boolean sleep2=false;
	boolean doexit=false;
	int id=0;//
	private Object lock = null;

	boolean gathered=false;
	  public DistributionCalc(ARAMNetworkClass network)
	  {
		  m_network=network;
		  lock=new Object();
	  } 
	  public void setinstance(Instance inst)
	  {
		  m_inst=inst;
		  do_classify=1;
		  //System.out.println("new instance "+id);
	  }
  public void run()
  {
	  while (true){
		  sleep=false;
		  //System.out.println("start classify again "+id);
		try {
			m_dist = m_network.distributionForInstance(m_inst);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  //System.out.println("check "+id+" "+do_classify+" "+gathered);
		  synchronized (this) {
          try {
      		do_classify=2;
    		reuse=true;
    		sleep=true;
    		gathered=false;
        	  sleep2=true;
              this.wait();
              if(doexit){
            	  return;
              }
          } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
		  }
		
  }
}

  public void doNotify() {
      synchronized(this) {
          this.notify();
          sleep2=false;
      }
  }
  public void destroy(){
      synchronized(this) {
    	  doexit=true;
          this.notify();
          sleep2=false;
      }
  }
}
public class DistributionCalcM extends Thread
{
	ARAMNetworkClass m_network =null;
	Instances m_inst=null;
	double[] m_dist=null;
	double[][] results=null;
	int do_classify=0;
	boolean reuse=false;
	boolean sleep=true;
	boolean sleep2=false;
	boolean doexit=false;
	int id=0;//
	private Object lock = null;

	boolean gathered=false;
	  public DistributionCalcM(ARAMNetworkClass network)
	  {
		  m_network=network;
		  lock=new Object();
	  } 
	  public void setinstances(Instances inst)
	  {
		  m_inst=inst;
		  do_classify=1;
		  //System.out.println("new instance "+id);
	  }
  public void run()
  {
	  int L=m_inst.classIndex();
	  int N=m_inst.numInstances();
	  results=new double[N][L];
	  //System.out.println("start classify again "+id);
		try {
			for (int i = 0, c = 0; i < N; i++) {

				//if(m_network.getDebug()) { int t = i*50/m_inst.numInstances(); if(t > c) { System.out.print("#"); c = t; } }

				// No cheating allowed; clear all class information
				AbstractInstance x = (AbstractInstance)((AbstractInstance) m_inst.instance(i)).copy(); 
				for(int v = 0; v < m_inst.classIndex(); v++) 
					x.setValue(v,0.0);

			double y[] = m_network.distributionForInstance(x);
			for(int j=0;j<numClasses;j++){
				results[i][j]=y[j];
			}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		
  }
}

public class BuildClassifier extends Thread
{
	ARAMNetworkClass m_network =null;
	Instances m_inst=null;
	double[] m_dist=null;
	  public BuildClassifier(ARAMNetworkClass network)
	  {
		  m_network=network;
	  } 
	  public void setinstances(Instances inst)
	  {
		  m_inst=inst;
	  }
  public void run()
  {
	try {
		m_network.buildClassifier(m_inst);
		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	
  }
}
@Override
public double[][] distributionForInstanceM(Instances inst) throws Exception {
	// TODO Auto-generated method stub
	DistributionCalcM[] dct=null ;
	  int L=numClasses;
	  int N=inst.numInstances();
	dct =new  DistributionCalcM[numberofnetworks];
	 double[][] results=new double[N][L];
	for (int i=0; i< numberofnetworks;i++){

		  dct[i] = new DistributionCalcM(networks[i]);
		  dct[i].id=i;
		  dct[i].setinstances(inst);
		  dct[i].start();
	}
	for (int k=0; k< numberofnetworks;k++){
		dct[k].join();
			for (int i = 0, c = 0; i < N; i++) {
				for(int j=0;j<numClasses;j++){
					results[i][j]+=dct[k].results[i][j];
				}
			}
		}
		return results;
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
public String getModel() {
	// TODO Auto-generated method stub
	return "";
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


public String thresholdTipText() {
	return "Set threshold to select activited neurons (no-winner-takes-all)";
}
public String fastLearnTipText() {
	return "Only one sweap over the trainig data";
}





public String neuronsActivityTipText() {
	return "Save the activity of neurons per sample";
}
public String neuronsActivityFileNameTipText() {
	return "Filename for saving the activity of neurons per sample";
}
public String sparseTipText() {
	return "Sparse implementation";
}
public String sparseHTipText() {
	return "Sparse other implementation";
}
  public String sparseHTTipText() {
  	return "Sparse other implementation";
  }
public String seedTipText() {
	return "seed when shuffling";
}
public String threadedTipText() {
	return "Use threads";
}
public String threadedVotersTipText() {
	return "Use threads on voters";
}
public String vigilancyTipText() {
	return "Vigilance parameter of ARAM";
}
public String votersTipText() {
	return "How many voters?";
}
}










