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
import meka.classifiers.multitarget.CR;
import meka.core.MatrixUtils;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.matrix.Matrix;
import weka.core.matrix.SingularValueDecomposition;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

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

	private static final long serialVersionUID = 3761303322465321039L;

	/*
	 * The shift matrix, used in the training and prediction
	 */
	protected Matrix m_Shift;

	/*
	 * Pattern Instances, needed to transform an Instance object
	 * for the prediction step
	 */
	protected Instances m_PatternInstances;

	/*
	 * The transformation matrix which is generated using SVD.
	 */
	protected Matrix m_v = null;

	/*
	 * The size of the compresed / transformed matrix.
	 */
	protected int m_Size = getDefaultSize();

	/**
	 * Returns the global information of the classifier.
	 *
	 * @return Global information of the classfier
	 */
	public String globalInfo() {
		return
			"PLST - Principle Label Space Transformation. Uses SVD to generate a matrix "
				+ "that transforms the label space. This implementation is adapted from the "
				+ "MatLab implementation provided by the authors.\n\n"
				+ "https://github.com/hsuantien/mlc_lsdr\n\n"
				+ "For more information see:\n "
				+ getTechnicalInformation();
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
	 * The default size, set to 5.
	 *
	 * @return the default size.
	 */
	protected int getDefaultSize(){
		return 3;
	}

	/**
	 * Returns the size of the compressed labels.
	 *
	 * @return The size of the compressed labels, i.e., the number of columns.
	 */
	public int getSize(){
		return m_Size;
	}

	/**
	 * Sets the size of the compressed labels.
	 *
	 * @param size The size of the compressed labels, i.e., the number of columns.
	 */
	public void setSize(int size){
		this.m_Size = size;
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
   * Returns an instance of a TechnicalInformation object, containing
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   *
   * @return the technical information about this class
   */
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
	super.setOptions(options);
    }



    
    /**
     * The method to transform the labels into another set of latent labels,
     * typically a compression method is used, e.g., Boolean matrix decomposition
     * in the case of MLC-BMaD, or matrix multiplication based on SVD for PLST.
     *
     * @param D the instances to transform into new instances with transformed labels. The
     * Instances consist of features and original labels.
     * @return The resulting instances. Instances consist of features and transformed labels.
     */
	@Override
	public Instances transformLabels(Instances D) throws Exception{
		Instances features = this.extractPart(D, false);
		Instances labels = this.extractPart(D, true);


		Matrix labelMatrix = MatrixUtils.instancesToMatrix(labels);
		
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
					// The algorithm needs 1/-1 coding, so let's
					// change the matrix here
					labelMatrix.set(j,i, -1.0);
				}
			}
			averages[i] = sum / column.length;
		}

		double[][] shiftMatrix =
			new double[1][labels.numAttributes()];

		shiftMatrix[0] = averages;

		// remember shift for prediction
		this.m_Shift = new Matrix(shiftMatrix);

		double[][] shiftTrainMatrix =
			new double[labels.numInstances()][labels.numAttributes()];
		
		for (int i = 0; i < labels.numInstances(); i++){
			shiftTrainMatrix[i] = averages;
		}

		Matrix trainShift = new Matrix(shiftTrainMatrix);

		SingularValueDecomposition svd =
			new SingularValueDecomposition(labelMatrix.minus(trainShift));

	
		
		// The paper uses U here, but the implementation by the authors uses V, so
		// we used V here too.
		m_v = svd.getV();


		//remove columns so only size are left
		double[][] newArr = new double[m_v.getRowDimension()][this.getSize()];

		for (int i =0; i < newArr.length; i++){
		    for (int j =0; j < newArr[i].length; j++){
			newArr[i][j] = m_v.getArray()[i][j];
		    }
		}
		
		m_v = new Matrix(newArr);

		// now the multiplication (last step of the algorithm)
		Matrix compressed = MatrixUtils.instancesToMatrix(labels).times(this.m_v);
		
		// and transform it to Instances
		ArrayList<Attribute> attinfos = new ArrayList<Attribute>();

		for (int i = 0; i < compressed.getColumnDimension(); i++) {

			Attribute att = new Attribute("att"+ i);
			attinfos.add(att);
		}

		// create pattern instances (also used in prediction) note: this is a regression
		// problem now, labels are not binary
		this.m_PatternInstances = new Instances("compressedlabels",
			attinfos,
			compressed.getRowDimension());

		// fill result Instances
		Instances result=
			Instances.mergeInstances(MatrixUtils.matrixToInstances(compressed, m_PatternInstances),
				features);

		result.setClassIndex(this.getSize());
		return result;
	}

    /**
     * Transforms the predictions of the internal classifier back to the original labels.
     *
     * @param y The predictions that should be transformed back. The array consists only of
     * the predictions as they are returned from the internal classifier.
     * @return The transformed predictions.
     */
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
		
		Matrix multiplied = yMat.times(this.m_v.transpose()).plus(m_Shift);

		double[] res = new double[multiplied.getColumnDimension()];

		// change back from -1/1 coding to 0/1
		for (int i = 0; i < res.length; i++) {
			res[i] = multiplied.getArray()[0][i]<0.0 ? 0.0 : 1.0;
		}

		return res;
	}

    /**
     * Transforms the instance in the prediction process before given to the internal multi-label
     * or multi-target classifier. The instance is passed having the original set of labels, these
     * must be replaced with the transformed labels (attributes) so that the internla classifier
     * can predict them.
     *
     * @param x The instance to transform. Consists of features and labels.
     * @return The transformed instance. Consists of features and transformed labels.
     */
	@Override
	public Instance transformInstance(Instance x) throws Exception{
		Instances tmpInst = new Instances(x.dataset());

		tmpInst.delete();
		tmpInst.add(x);

		Instances features = this.extractPart(tmpInst, false);

		Instances labels = new Instances(this.m_PatternInstances);

		labels.add(new DenseInstance(labels.numAttributes()));

		Instances result = Instances.mergeInstances(labels, features);

		result.setClassIndex(labels.numAttributes());

		return result.instance(0);
	}

	/**
	 * Returns a string representation of the model.
	 *
	 * @return      the model
	 */
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
		AbstractMultiLabelClassifier.evaluation(new PLST(), args);
	}
}
