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

import weka.core.Instance;
import weka.core.Instances;
import weka.core.matrix.Matrix;
import weka.core.matrix.SingularValueDecomposition;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import meka.classifiers.multitarget.CR;

import weka.filters.unsupervised.attribute.Remove;
import weka.core.DenseInstance;
import java.util.List;
import java.util.ArrayList;
import weka.core.Attribute;



/**
 * PLST - Principal Label Space Transformation. Uses SVD to generate a matrix
 * that transforms the label space. This implementation is adapted from the 
 * MatLab implementation provided by the authors at 
 * <a href="https://github.com/hsuantien/mlc_lsdr">Github</a>
 * <br>
 * See: Farbound Tai and Hsuan-Tien Lin. Multilabel classification with 
 * principal label space transformation. Neural Computation, 24(9):2508--2542, 
 * September 2012. 
 * 
 * @author 	Joerg Wicker (wicker@uni-mainz.de)
 */
public class PLST extends LabelTransformationClassifier implements TechnicalInformationHandler{

    protected static final long serialVersionUID = 1L;

    
    /*
     * The shift matrix, used in the training and prediction
     */
    private Matrix shift;
  
    @Override
    protected String defaultClassifierString() {
	return CR.class.getName();
    }

    /*
     * Pattern Instances, needed to transform an Instance object
     * for the prediction step
     */
    private Instances patternInstances;

    /*
     * The transformation matrix which is generated using SVD.
     */
    protected Matrix m_v = null;
    /*
     * The size of the compresed / transformed matrix.
     */
    protected int size = getDefaultSize();

    /**
     * The default size, set to 5.
     *
     * @return the default size.
     */
    protected int getDefaultSize(){
	return 5;
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
     * 
     * @return the tooltip.
     */
    public String sizeTipText(){
	return "Size of the compressed matrix. Should be \n"
	    + "less than the number of labels and more than 1.";
    }
    
    /** 
     * Returns the global information of the classifier.
     * 
     * @return Global information of the classfier
     */
    public String globalInfo() {
	return
	    "PLST - Principle Label Space Transformation. Uses SVD to generate a matrix "
	    + "that transforms the label space. This implementation is adapted from the "
	    + "MatLab implementation provided by the authors.\n"
	    + "For more information see:\n "
	    + getTechnicalInformation();
    }


    @Override
    public TechnicalInformation getTechnicalInformation() {
	TechnicalInformation	result;
	
	result = new TechnicalInformation(Type.INPROCEEDINGS);
	result.setValue(Field.AUTHOR, "Farbound Tai and Hsuan-Tien Lin");
	result.setValue(Field.TITLE, "Multilabel classification with principal label space transformation");
	result.setValue(Field.BOOKTITLE, "Neural Computation");
	result.setValue(Field.YEAR, "2012");
	result.setValue(Field.PAGES, "2508-2542");
	result.setValue(Field.VOLUME, "24");
	result.setValue(Field.NUMBER, "9");
	
	return result;
    }

    /**
     * Helper method that transforma an Instances object to a Matrix object.
     *
     * @param inst The Instances to transform.
     * @return  The resulting Matrix object.
     */
    private Matrix instancesToMatrix(Instances inst){
	// TODO: maybe already exists somewhere in Weka?
	double[][] darr = new double[inst.numInstances()][inst.numAttributes()];
	for (int i =0 ; i < inst.numAttributes(); i++) {
	    for (int j = 0; j < inst.attributeToDoubleArray(i).length; j++) {
		darr[j][i] = inst.attributeToDoubleArray(i)[j];
	    }
	}
	return new Matrix(darr);
    }


    /**
     * Helper method that transforma a Matrix object to an Instances object.
     *
     * @param inst The Matrix to transform.
     * @return  The resulting Instances object.
     */
   private Instances matrixToInstances(Matrix mat, Instances patternInst){
	// TODO: maybe already exists somewhere in Weka?

	Instances result = new Instances(patternInst);
	for (int i = 0; i < mat.getRowDimension(); i++) {
	    double[] row =  mat.getArray()[i];
	    DenseInstance denseInst = new DenseInstance(1.0, row);
	    result.add(denseInst);
	}
	
	return result;
    }

    @Override
    public Instances transformLabels(Instances D) throws Exception{
		
    	Instances features = this.extractPart(D, false);
    	Instances labels = this.extractPart(D, true);

	// first, lets do the preprocessing as in the original implementation

	double[] averages = new double[labels.numAttributes()];
	
	for (int i = 0; i < labels.numAttributes(); i++){
	    double[] column = labels.attributeToDoubleArray(i);
	    double sum =0.0;
	    for(int j = 0; j < column.length; j++){
		if(column[j] == 1.0){
		    sum += 1.0;
		} else {
		    sum += -1;
		}
	    }
	    averages[i] = sum / column.length;
	}


	
	double[][] shiftMatrix =
            new double[1][labels.numAttributes()];

	shiftMatrix[0] = averages;	

	// remember shift for prediction
	
	this.shift = new Matrix(shiftMatrix);

	double[][] shiftTrainMatrix =
            new double[labels.numInstances()][labels.numAttributes()];

	for (int i = 0; i < labels.numInstances(); i++){
	    shiftTrainMatrix[i] = averages;
	}

	Matrix trainShift = new Matrix(shiftTrainMatrix);
	
    	SingularValueDecomposition svd =
	    new SingularValueDecomposition(this.instancesToMatrix(labels).plus(trainShift));

	// The paper uses U here, but the implementation by the authors uses V, so
	// we used V here too.
	
	m_v = svd.getV();

	
	//remove columns so only size are left

	double[][] newArr = new double[m_v.getRowDimension()][this.getSize()];

	for (int i =0; i < newArr.length; i++){
	    for (int j =0; j < newArr[i].length; j++){
		newArr[i][j] = m_v.getArray()[i][j] == 1 ? 1.0 : -1.0;
	    }
	}
	
	
	m_v = new Matrix(newArr);
	

	// now the multiplication (last step of the algorithm)
	
	Matrix compressed = this.instancesToMatrix(labels).times(this.m_v);

	// and transform it to Instances
	
	ArrayList<Attribute> attinfos = new ArrayList<Attribute>();


	for (int i = 0; i < compressed.getColumnDimension(); i++) {
	 
	    Attribute att = new Attribute("att"+ i);
	    attinfos.add(att);
	}

	// create pattern instances (also used in prediction) note: this is a regression
	// problem now, labels are not binary
	
	this.patternInstances = new Instances("compressedlabels",
					      attinfos,
					      compressed.getRowDimension());


	// fill result Instances
	
	Instances result=
	    Instances.mergeInstances(this.matrixToInstances(compressed, patternInstances),
				     features);
	

	result.setClassIndex(this.getSize());
     	return result;
     }


    
    
    @Override
    public double[] transformPredictionsBack(double[] y){

	// y consists of predictions and maxindex, we need only predictions
	
	double[] predictions = new double[y.length/2];

	for (int i = 0; i < predictions.length; i++){
	    predictions[i] = y[predictions.length+i];
	    
	}
	
	double[][] dataArray = new double[1][predictions.length];

	dataArray[0] = predictions;
	
	Matrix yMat = new Matrix(dataArray);

	
	
	Matrix multiplied = yMat.times(this.m_v.transpose()).plus(shift);

	double[] res = new double[multiplied.getColumnDimension()];
	
	for (int i = 0; i < res.length; i++) {
	    res[i] = multiplied.getArray()[0][i]<0.0 ? 0.0 : 1.0;
	}

	
    	return res;

    }


    @Override
    public Instance transformInstance(Instance x) throws Exception{

	Instances tmpInst = new Instances(x.dataset());
	
	tmpInst.delete();
	tmpInst.add(x);
	
	Instances features = this.extractPart(tmpInst, false);
	
	Instances labels = new Instances(this.patternInstances);

	labels.add(new DenseInstance(labels.numAttributes()));
	
	Instances result = Instances.mergeInstances(labels, features);

	
	result.setClassIndex(labels.numAttributes());
	
	return result.instance(0);
    }

    @Override
    public String getModel(){
	return "";
    }



    /**
     * Main method for testing.
     * @param args - Arguments passed from the command line
     **/
    public static void main(String[] args) throws Exception{
	AbstractMultiLabelClassifier.evaluation(new PLST(), args);
    }

}
