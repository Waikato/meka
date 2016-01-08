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

package meka.classifiers.multilabel;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multitarget.CR;
import meka.core.OptionUtils;
import meka.core.Result;
import org.kramerlab.autoencoder.math.matrix.Mat;
import org.kramerlab.autoencoder.neuralnet.autoencoder.Autoencoder;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.SparseToNonSparse;

/**
 * Maniac - Multi-lAbel classificatioN using AutoenCoders. Transforms 
 * the labels using layers of autoencoders. 
 * <br>
 * See: J&ouml;rg Wicker, Andrey Tyukin, Stefan Kramer. <i>A Nonlinear Label 
 * Compression and Transformation Method for Multi-Label Classification using 
 * Autoencoders</i>. The 20th Pacific Asia Conference on Knowledge Discovery 
 * and Data Mining (PAKDD), 2016.
 *
 * @author 	Joerg Wicker (wicker@uni-mainz.de)
 */
public class Maniac extends LabelTransformationClassifier implements TechnicalInformationHandler {

    protected static final long serialVersionUID = 585507197229071545L;

    /**
     * The autoencoder that is trained, and used to compress and decompress the labels/
     */
    private Autoencoder ae;
    
    /**
     * Template of the compressed labels, used in the prediction. 
     */
    private Instances compressedTemplateInst;

    /**
     * Flag to tell if the number of autoencoders should be optimized. Multiple layers are tested and 
     * the best setting is chosen. The maximum number of autoencoders can be set via the numberAutoencoders
     * variable.
     */
    protected boolean optimizeAE = getDefaultOptimizeAE();

    /**
     * The compression factor, i.e. the compression from one layer to the next. 0.85 means for example, 
     * that from one layer with 100 labels, on the next layer 85 are left.
     */
    protected double compression = getDefaultCompression();

    
    /**
     * Sets the autoencoder (for using a trained one, e.g. done in optimization).
     *
     * @param ae The autoencoder
     */
    protected void setAE(Autoencoder ae){
	this.ae = ae;
    }

    /**
     * Returns the autoencoder of the class (used for compression of labels).
     *
     * @return the autoencoder that is used for compression.
     */
    private Autoencoder getAE(){
	return this.ae;
    }

    /**
     * Number of autoencoders to train, i.e. number of hidden layers + 1. Note that this can be 
     * also used as the number of autoencoders to use in the optimization search, autoencoders will be added
     * until this number is reached and then the best configuration in terms of number of layers is selects.
     */
    protected int numberAutoencoders = getDefaultNumberAutoencoders();

    /**
     * Returns the default number of autoencoders, set to 4, which seems to 
     * be good choice for most problems.
     */
    protected int getDefaultNumberAutoencoders(){
	return 4;
    }

    /**
     * Get the <code>numberAutoencoders</code> value.
     *
     * @return an <code>in</code> value
     */
    public final int getNumberAutoencoders() {
	return numberAutoencoders;
    }

    /**
     * Set the <code>numberAutoencoders</code> value.
     *
     * @param numberAutoencoders The new NumberAutoencoders value.
     */
    public final void setNumberAutoencoders(final int numberAutoencoders) {
	this.numberAutoencoders = numberAutoencoders;
    }

    /**
     * Gives the tooltip for numberAutoencoders.
     * @return the tooltip for numberAutoencoders.
     */
    public String numberAutoencodersToolTip(){
	return "Number of autoencoders, i.e. number of hidden layers "
	    + "+1. Note that this can be also used as the number of "
	    + "autoencoders to use in the optimization search, "
	    + "autoencoders will be added until this number is reached "
	    +" and then the best configuration in terms of number of layers is selects.";
    }
    
    /**
     * Gives the tiptext for numberAutoencoders.
     * @return the tiptext for numberAutoencoders.
     */
    public String numberAutoencodersTipText(){
	return numberAutoencodersToolTip();
    }

    /**
     * Get the <code>Compression</code> value.
     *
     * @return a <code>double</code> value
     */
    public final double getCompression() {
	return compression;
    }

    /**
     * Set the <code>Compression</code> value.
     *
     * @param compression The new Compression value.
     */
    public final void setCompression(final double compression) {
	this.compression = compression;
    }

    /**
     * Returns the default compression, 0.85 seems to be a good value for most settings.
     * 
     * @return The default compression.
     */
    protected double getDefaultCompression(){
	return 0.85;
    }

    /**
     * Gives the tooltip for compression.
     * @return the tooltip for compression.
     */
    public String compressionToolTip(){
	return "Compression factor of the autoencoders, each level "
	    +"of autoencoders will compress the labels to factor times "
	    +"previous layer size.";
    }

    /**
     * Gives the tiptext for compression.
     * @return the tiptext for compression.
     */
    public String compressionTipText(){
	return compressionToolTip();
    }

    /**
     * Gives the tiptext for optimizeAE.
     * @return the tiptext for optimizeAE.
     */    public String optimizeAETipText(){
	return optimizeAEToolTip();
    }

    /**
     * Get the <code>OptimizeAE</code> value.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean isOptimizeAE() {
	return optimizeAE;
    }

    /**
     * Set the <code>OptimizeAE</code> value.
     *
     * @param optimizeAE The new OptimizeAE value.
     */
    public final void setOptimizeAE(final boolean optimizeAE) {
	this.optimizeAE = optimizeAE;
    }
    /**
     * Tge default setting for optimizing the autoencoders. Set to false, as this 
     * is an expensive operation.
     *
     * @return The default flag for optimizing the autoencoders.
     */
    protected boolean getDefaultOptimizeAE(){
	return false;
    }

    /**
     * Gives the tooltip for OptimizeAE.
     * @return the tooltip for OptimizeAE.
     */
    public String optimizeAEToolTip(){
	return "Optimize the number of layers of autoencoders. If set to true "
	    +"the number of layers will internally be optimized using a validation "
	    +"set.";
    }
    
    /** 
     * Returns the global information of the classifier.
     * 
     * @return Global information of the classfier
     */
    public String globalInfo() {
	return
	    "Maniac - Multi-lAbel classificatioN using AutoenCoders."
	    + "Transforms the labels using layers of autoencoders."
	    + "For more information see:\n"
	    + getTechnicalInformation();
    }

    /**
     * Returns an enumeration of the options.  
     *
     * @return Enumeration of the options.
     */
    public Enumeration listOptions() {
	Vector newVector = new Vector();

	OptionUtils.addOption(newVector,
			      compressionTipText(),
			      ""+getDefaultCompression(),
			      "compression");
	
	OptionUtils.addOption(newVector,
			      numberAutoencodersTipText(),
			      ""+getDefaultNumberAutoencoders(),
			      "numberAutoencoders");

	OptionUtils.addOption(newVector,
			      optimizeAETipText(),
			      ""+getDefaultOptimizeAE(),
			      "optimizeAE");


	
	OptionUtils.add(newVector, super.listOptions());

	return OptionUtils.toEnumeration(newVector);
    }

    /**
     * Change default classifier to CR with Linear Regression as base as this classifier
     * uses numeric values in the compressed labels.
     */
    protected Classifier getDefaultClassifier() {
	CR cr = new CR();
	LinearRegression lr = new LinearRegression();
	cr.setClassifier(lr);
	return cr;
    }

    /**
     * Returns an array with the options of the classifier.
     * 
     * @return Array of options.
     */
    public String[] getOptions(){
	List<String> result = new ArrayList<>();
	OptionUtils.add(result, "compression", getCompression());
	OptionUtils.add(result, "optimizeAE", isOptimizeAE());
	OptionUtils.add(result, "numberAutoencoders", getNumberAutoencoders());
	OptionUtils.add(result, super.getOptions());
	return OptionUtils.toArray(result);
    }

    /**
     * Sets the options to the given values in the array.
     *
     * @param options The options to be set.
     */
    public void setOptions(String[] options) throws Exception {
	setCompression(OptionUtils.parse(options, "compression", getDefaultCompression()));
	setNumberAutoencoders(OptionUtils.parse(options, "numberAutoencoders", getDefaultNumberAutoencoders()));
	setOptimizeAE(OptionUtils.parse(options, "optimizeAE", getDefaultOptimizeAE()));
	super.setOptions(options);
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
	TechnicalInformation	result;

	result = new TechnicalInformation(Type.INPROCEEDINGS);
	result.setValue(Field.AUTHOR, "J\"org Wicker, Andrey Tyukin, Stefan Kramer");
	result.setValue(Field.TITLE, "A Nonlinear Label Compression and Transformation Method for Multi-Label Classification using Autoencoders");
	result.setValue(Field.BOOKTITLE, "The 20th Pacific Asia Conference on Knowledge Discovery and Data Mining (PAKDD)");
	result.setValue(Field.YEAR, "2016");
	result.setValue(Field.PAGES, "-");

	return result;
    }

    @Override
    public Instance transformInstance(Instance x) throws Exception{
	
	Instances tmpInst = new Instances(x.dataset());

	tmpInst.delete();
	tmpInst.add(x);
	
	Instances features = this.extractPart(tmpInst, false);

	Instances pseudoLabels = new Instances(this.compressedTemplateInst);
	Instance tmpin = pseudoLabels.instance(0);
	pseudoLabels.delete();
	
	pseudoLabels.add(tmpin);

	for ( int i = 0; i< pseudoLabels.classIndex(); i++) {
	    pseudoLabels.instance(0).setMissing(i);
	}

	Instances newDataSet = Instances.mergeInstances(pseudoLabels, features);
	newDataSet.setClassIndex(pseudoLabels.numAttributes());
	
	return newDataSet.instance(0);
    }

    @Override
    public Instances transformLabels(Instances D) throws Exception{
        // crazy scala-specific stuff that is necessary to access
        // "static" methods from java
        org.kramerlab.autoencoder.package$ autoencoderStatics = 
	    org.kramerlab.autoencoder.package$.MODULE$;
        
        org.kramerlab.autoencoder.wekacompatibility.package$ wekaStatics =
	    org.kramerlab.autoencoder.wekacompatibility.package$.MODULE$;
        
        org.kramerlab.autoencoder.experiments.package$ experimentsStatics = 
	    org.kramerlab.autoencoder.experiments.package$.MODULE$;
	
	int topiter = -1;

	// the optimization is a bit special, since we learn a stream
	// of autoencoders, no need to start from scratch, we just add layers
	if (this.isOptimizeAE()) {
	    Instances train = D.trainCV(3,1);
	    Instances test = D.testCV(3,1);
	    Instances labels = this.extractPart(train, true);
	    
	    // first convert the arff into non sparse form
	    SparseToNonSparse spfilter = new SparseToNonSparse();
	    spfilter.setInputFormat(labels);
	    Instances aeData = Filter.useFilter(labels, spfilter);
	    
	    // now convert it into a format suitable for the autoencoder
	    Mat data = wekaStatics.instancesToMat(aeData);
	    
	    
	    Iterable<Autoencoder> autoencoders = autoencoderStatics
		.deepAutoencoderStream_java(
					    autoencoderStatics.Sigmoid(), // type of neurons.
					    // Sigmoid is ok
					    this.getNumberAutoencoders(), // number of autoencoders = (max hidden layers + 1) /
					    // 2
					    this.getCompression(), // compression from k-th layer to (k+1)-th layer
					    data, // training data 
					    true, // true = L2 Error, false = CrossEntropy
					    autoencoderStatics.HintonsMiraculousStrategy(), true,
					    autoencoderStatics.NoObservers()
					    );
	    	    
	    // test each autoencoder, select the best classifier
	    double bestAccuracy = Double.NEGATIVE_INFINITY;
	    int iteratorcount = 0;
	    topiter = 0;
	    for (Autoencoder a : autoencoders) {
		iteratorcount ++;
		
		
		Maniac candidate = new Maniac();
		candidate.setOptimizeAE(false);
		candidate.setNumberAutoencoders(this.getNumberAutoencoders());
		candidate.setCompression(this.getCompression());
		candidate.setClassifier(this.getClassifier());

		candidate.setAE(a);
		
		Result res = Evaluation.evaluateModel(candidate, train, test);
		double curac = (Double)res.getValue("Accuracy");


		if (bestAccuracy < curac) {
		    bestAccuracy = curac;
		    topiter = iteratorcount;
		}
	    }
	}
	Instances features = this.extractPart(D, false);
	Instances labels = this.extractPart(D, true);
	
	// first convert the arff into non sparse form
	SparseToNonSparse spfilter = new SparseToNonSparse();
	spfilter.setInputFormat(labels);
	Instances aeData = Filter.useFilter(labels, spfilter);
	
	// now convert it into a format suitable for the autoencoder
	Mat data = wekaStatics.instancesToMat(aeData);
	
	if (this.getAE() == null) {
	    Iterable<Autoencoder> autoencoders = autoencoderStatics
		.deepAutoencoderStream_java(
					    autoencoderStatics.Sigmoid(), // type of neurons.
					    // Sigmoid is ok
					    this.getNumberAutoencoders(), // number of autoencoders = (max hidden layers + 1) /
					    // 2
					    this.getCompression(), // compression from k-th layer to (k+1)-th layer
					    data, // training data 
					    true, // true = L2 Error, false = CrossEntropy
					    autoencoderStatics.HintonsMiraculousStrategy(), true,
					    autoencoderStatics.NoObservers()
					    );
	    int itercount=0;
	    for (Autoencoder a : autoencoders) {
		itercount++;
		if(topiter > 0 && itercount == topiter ||
		   itercount == this.getNumberAutoencoders()){
		    this.setAE(a);
		    break;
		}
	    }
	}

	
	
	Mat compressed = this.getAE().compress(data);
	Instances compressedLabels = wekaStatics.matToInstances(compressed);

	// remember the labels to use for the prediction step,
        this.compressedTemplateInst = new Instances(compressedLabels);
	
	Instances result = Instances.mergeInstances(compressedLabels,features);

	result.setClassIndex(compressedLabels.numAttributes());
	
	return result;
    }

    @Override
    public double[] transformPredictionsBack(double[] y){
	Mat matrix = new Mat(1, y.length/2);
	for (int i = 0; i < y.length/2; i++) {
	    matrix.update(0, i, y[y.length/2 + i]);
	}
	Mat reconstruction = this.getAE().decompress(matrix);
	double[] result = new double[reconstruction.toArray()[0].length];
	for(int i = 0; i < result.length; i++){
	    result[i] = reconstruction.apply(0,i);
	}
	return result;
    }
    
    @Override
    public String getModel(){
	return "";
    }
    
    @Override
    public String toString() {
	return getModel();
    }
    
    /**
     * Main method for testing.
     * @param args - Arguments passed from the command line
     **/
    public static void main(String[] args) throws Exception{
	AbstractMultiLabelClassifier.evaluation(new Maniac(), args);
    }
}
