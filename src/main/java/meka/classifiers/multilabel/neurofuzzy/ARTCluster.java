package meka.classifiers.multilabel.neurofuzzy;


import java.util.Arrays;

import meka.classifiers.multilabel.*;
import weka.core.Instance;
import weka.core.Instances;

//based on ARTCluster_v3.py from DAMIART
public class ARTCluster {
	public double vigilance=0.7;
    double[][] weightsA = null;
    double[] weightsAsum = null;
    int numCategories=0;
    int numFeatures=0;
    double learningRate=1;
    Integer[][] Ids = null;
    int maxidsperc=100000;
    int maxids=3;
    final int maxNumCategories=100000;

    public ARTCluster(int fnumFeatures, double fro) {
    	numFeatures=fnumFeatures;
    	vigilance=fro;
    }
    public void learn(double[][] data, int[] ids ){
    	
    	int startc=0;
        if (weightsA==null|| weightsA.length == 0){
        	addsample(data[0],ids[0],-1);
            startc = 1;
        }
        else{
            startc = 0;
            }
        //parameter to assure prototype does not get too big
        if (maxids==0){
            
            maxids=3;
        }
        
        for(int i = 0; i < data.length; i++) {
        	
            if (i%1000==0){
            	System.out.println("Processing"+i+weightsA.length);
            }
            int found=0;
            double[] activationn=new double[weightsA.length];
            double[] activationi=new double[weightsA.length];
                
            double[] fc=data[0];
            
            double fcs = sum(fc);
            
            SortPair[] sortedActivations=new SortPair[weightsA.length];
            for(int i2 = 0; i2 < weightsA.length; i2++) {
                    double minnfs = ART_Calculate_Match(weightsA[i2], fc);
                    activationi[i2] =minnfs/fcs;
                    activationn[i2] =minnfs/weightsAsum[i2];
                    sortedActivations[i2] = new SortPair(activationn[i2], i2);
            }
            if (max(activationn) == 0){
            	addsample(data[i],ids[i],fcs);
                continue;
            }
            java.util.Arrays.sort(sortedActivations);
			int currentCategory = -1;
			int currentSortedIndex = 0;
			boolean resonance = false;
			while (!resonance) {

				currentCategory = sortedActivations[currentSortedIndex]
						.getOriginalIndex();
				if (activationi[currentCategory]>vigilance){
					if (currentCategory == numCategories -1) {
						if (currentSortedIndex == maxNumCategories) {
							System.out
							.println("WARNING: The maximum number of categories has been reached.");
					resonance = true;
				} else {
		        	addsample(data[i],ids[i],-1);
					resonance = true;
					
				}
					
				}else{
					if(Ids[currentCategory].length>maxidsperc){
						currentSortedIndex += 1;
						resonance = false;
						
						
					}
				
					update_Weights(data[i], ids[i],
							currentCategory);
					
				}
				}else{
					if (currentCategory == numCategories -1) {
			        	addsample(data[i],ids[i],-1);
						resonance = true;
					}else{
					currentSortedIndex += 1;
					resonance = false;
					}
					
				}
                
			}

        }
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

    public void addsample(double[] sample, int id, double sum){
    	numFeatures=sample.length;
    	if (weightsA==null)
    	{
    		weightsA = new double[1][numFeatures];
    		Arrays.fill(weightsA[0], 1);
    		weightsAsum = new double[1];
    		Ids = new Integer[1][1];
    		Ids[numCategories][0]=id;
    		
    		}
    	else{
    	weightsA = Arrays.copyOf(weightsA, numCategories + 1);
		weightsA[numCategories] = new double[numFeatures];
    	weightsAsum = Arrays.copyOf(weightsAsum, numCategories + 1);
		Ids = Arrays.copyOf(Ids, numCategories + 1);
		Ids[numCategories]=new Integer[1];
		Ids[numCategories][0]=id;
    	}
	    int category=numCategories;
	    if (sum<0){
		    weightsAsum[numCategories] = 0;
		for (int i = 0; i < numFeatures; i++) {
			if (sample[i] < weightsA[category][i]){
			weightsA[category][i] = (learningRate * sample[i])
					+ (1 - learningRate) * weightsA[category][i];
			weightsAsum[numCategories]+=weightsA[category][i];
			}

		}
	    }else{
		    weightsAsum[numCategories] = sum;
			for (int i = 0; i < numFeatures; i++) {
				if (sample[i] < weightsA[category][i]){
				weightsA[category][i] = (learningRate * sample[i])
						+ (1 - learningRate) * weightsA[category][i];
				}

			}
	    	
	    }
		numCategories+=1;
    	
    }
    public void update_Weights(double[] sample, int id, int category){

		for (int i = 0; i < numFeatures; i++) {
			if (sample[i] < weightsA[category][i]){
			weightsA[category][i] = (learningRate * sample[i])
					+ (1 - learningRate) * weightsA[category][i];
			weightsAsum[numCategories]+=weightsA[category][i];
			}

		}
		int maxl=Ids[category].length;
		Ids[category] = Arrays.copyOf(Ids[category],maxl  + 1);
		Ids[category][maxl]=id;
    	
    }
    	
    public SortPair[] activate(double[] data){
    	
    	SortPair[] activations= new SortPair[weightsA.length];
    	for(int i = 0; i < weightsA.length; i++) {
    		
    		activations[i]=new SortPair(ART_Calculate_Match(data,weightsA[i]), i);
    		
    	}
		return activations;

    }

    double sum(double[] data){
    	double result=0;
    	for(int i = 0; i < data.length; i++) {
    		
    		result+=data[i];
    	}
    	return result;
    }
    double max(double[] data){
    	double result=0;
    	for(int i = 0; i < data.length; i++) {
    		if(result<data[i])
    			result=data[i];
    	}
    	return result;
    }
}
