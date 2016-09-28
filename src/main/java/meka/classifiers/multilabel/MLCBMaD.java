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


import meka.core.OptionUtils;
import org.kramerlab.bmad.algorithms.BooleanMatrixDecomposition;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * MLC-BMaD - Multi-Label Classification using Boolean Matrix Decomposition. Transforms 
 * the labels using a Boolean matrix decomposition, the first resulting matrix are 
 * used as latent labels and a classifier is trained to predict them. The second matrix is 
 * used in a multiplication to decompress the predicted latent labels. 
 * <br>
 * See: J&ouml;rg Wicker, Bernhard Pfahringer, Stefan Kramer. <i>Multi-label Classification Using Boolean Matrix Decomposition</i>. Proceedings of the 27th Annual ACM Symposium on Applied Computing, pp. 179–186, ACM, 2012.
 *
 * @author 	Joerg Wicker (jw@oerg-wicker.org)
 */
public class MLCBMaD extends LabelTransformationClassifier implements TechnicalInformationHandler {

    protected static final long serialVersionUID = 585507197229071545L;

    /**
     * The upper matrix. Decomposition is done such that Y=Y'*M, this is M.
     */
    protected Instances uppermatrix = null;

    /**
     * The compressed matrix. Decomposition is done such that Y=Y'*M, this is Y'.
     */
    protected Instances compressedMatrix = null;

    /**
     * The size of the compressed matrix, i.e., the number of columns of Y'.
     */
    protected int size = getDefaultSize();

    /**
     * The threshold t of the decomposition process, see the paper for details. Sets 
     * the minimum frequency to be considered a frequent coocurence. Between 0 
     * (all are frequent) and 1 (must be in all rows to be frequent). 
     */
    protected double threshold = getDefaultThreshold();

    /**
     * Default threshold = 0.5, has to be in at least half of the rows.
     *
     * @return the default threshold.
     */
    protected double getDefaultThreshold(){
	return 0.5;
    }

    /**
     * Default size = 20, seems to be a good choice for most data sets. 
     * 
     * @return the tooltip
     */
    protected int getDefaultSize(){
	return 20;
    }

    /**
     * Returns the size of the compressed labels.
     *
     * @return The size of the compressed labels, i.e., the number of columns.
     */
    public int getSize(){
	return size;
    }

    /**
     * Sets the size of the compressed labels.
     *
     * @param size The size of the compressed labels, i.e., the number of columns.
     */
    public void setSize(int size){
	this.size = size;
    }

    /**
     * The tooltip for the size.
     */
    public String sizeTipText(){
	return "Size of the compressed matrix. Should be \n"
	    + "less than the number of labels and more than 1.";
    }

    /**
     * Getter for the threshold for Boolean matrix decomposition.
     * 
     * @return the threshold for the Boolean matrix decomposition.
     */
    public double getThreshold(){
	return threshold;
    }

    /**
     * Sets the threshold for the Boolean matrix decomposition.
     * 
     * @param threshold the threshold for the Boolean matrix decomposition.
     */
    public void setThreshold(double threshold){
	this.threshold = threshold;
    }

    /** 
     * Tooltip for the threshold.
     *
     * @return Description of the threshold for Boolean matrix decomposition.
     */
    public String thresholdTipText(){
	return "Threshold for the matrix decompositon, what is considered frequent."
	    + "\n Between 0 and 1.";
    }

    /** 
     * Returns the global information of the classifier.
     * 
     * @return Global information of the classfier
     */
    public String globalInfo() {
	return
	    "MLC-BMaD - Multi-Label Classification using Boolean Matrix Decomposition. Transforms "
	    + "the labels using a Boolean matrix decomposition, the first resulting matrix are "
	    + "used as latent labels and a classifier is trained to predict them. The second matrix is "
	    + "used in a multiplication to decompress the predicted latent labels.\n"
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
			      sizeTipText(),
			      ""+getDefaultSize(),
			      "size");

	OptionUtils.addOption(newVector,
			      thresholdTipText(),
			      ""+getDefaultThreshold(),
			      "threshold");

	OptionUtils.add(newVector, super.listOptions());

	return OptionUtils.toEnumeration(newVector);
    }

    /**
     * Returns an array with the options of the classifier.
     * 
     * @return Array of options.
     */
    public String[] getOptions(){
	List<String> result = new ArrayList<>();
	OptionUtils.add(result, "size", getSize());
	OptionUtils.add(result, "threshold", getThreshold());
	OptionUtils.add(result, super.getOptions());
	return OptionUtils.toArray(result);
    }

    /**
     * Sets the options to the given values in the array.
     *
     * @param options The options to be set.
     */
    public void setOptions(String[] options) throws Exception {
	setSize(OptionUtils.parse(options, "size", getDefaultSize()));
	setThreshold(OptionUtils.parse(options, "threshold", getDefaultThreshold()));
	super.setOptions(options);
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
	TechnicalInformation	result;

	result = new TechnicalInformation(Type.INPROCEEDINGS);
	result.setValue(Field.AUTHOR, "J\"org Wicker, Bernhard Pfahringer, Stefan Kramer");
	result.setValue(Field.TITLE, "Multi-Label Classification using Boolean Matrix Decomposition");
	result.setValue(Field.BOOKTITLE, "Proceedings of the 27th Annual ACM Symposium on Applied Computing");
	result.setValue(Field.YEAR, "2012");
	result.setValue(Field.PAGES, "179-186");

	return result;
    }

    @Override
    public Instance transformInstance(Instance x) throws Exception{
	Instances tmpInst = new Instances(x.dataset());

	tmpInst.delete();
	tmpInst.add(x);
	
	Instances features = this.extractPart(tmpInst, false);

	Instances pseudoLabels = new Instances(this.compressedMatrix);
	Instance tmpin = pseudoLabels.instance(0);
	pseudoLabels.delete();

	pseudoLabels.add(tmpin);

	for ( int i = 0; i< pseudoLabels.classIndex(); i++) {
	    pseudoLabels.instance(0).setMissing(i);
	}

	Instances newDataSet = Instances.mergeInstances(pseudoLabels, features);
	newDataSet.setClassIndex(this.size);

	
	return newDataSet.instance(0);
    }

    @Override
    public Instances transformLabels(Instances D) throws Exception{

	Instances features = this.extractPart(D, false);
	Instances labels = this.extractPart(D, true);

	BooleanMatrixDecomposition bmd =
			BooleanMatrixDecomposition.BEST_CONFIGURED(this.threshold);
	Tuple<Instances, Instances> res = bmd.decompose(labels, this.size);
	
	this.compressedMatrix = res._1;
	this.uppermatrix = res._2;
	
	Instances result= Instances.mergeInstances(compressedMatrix,
						   features);
	result.setClassIndex(this.getSize());

	return result;
    }

    @Override
    public double[] transformPredictionsBack(double[] y){
	byte[] yByteArray = new byte[y.length];

	for(int i = 0; i < y.length; i++){
	    yByteArray[i] = y[i]>=0.5 ? BooleanMatrix.TRUE:BooleanMatrix.FALSE;
	}

	BooleanMatrix yMatrix =
			new BooleanMatrix( new byte[][]{yByteArray});
	BooleanMatrix reconstruction =
			yMatrix.booleanProduct(new BooleanMatrix(this.uppermatrix));

	
	double[] result = new double[reconstruction.getWidth()];
	
	for(int i = 0; i < reconstruction.getWidth(); i++){
	    result[i] = reconstruction.apply(0,i) == BooleanMatrix.TRUE  ? 1.0:0.0;

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
	AbstractMultiLabelClassifier.evaluation(new MLCBMaD(), args);
    }
}
