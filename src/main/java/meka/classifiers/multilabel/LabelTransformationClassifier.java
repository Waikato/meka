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

import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Abstract label transformation classifiers, all classes that transform the labels 
 * should inherit from this classifier. The general algorithm is a transformation 
 * of the labels into a new set of labels given a specific transformation method. 
 * A new multi-label classifier is then trained on the new set of latent labels.
 * The prediction works the other way around, the multi-label classifier predicts
 * the set of latent labels, and a specific transformation method transforms the 
 * predicted latent labels back into the original label dimension. 
 *
 * Implementing classes need to implement three methods, first the transformation 
 * method used for the training process, second a transformation method to
 * create a new instance as input for the prediction of the multi-label classifier, 
 * and third, a method transforming the predicted labels back into the original 
 * dimension. 
 *
 * @author 	Joerg Wicker (wicker@uni-mainz.de)
 */
public abstract class LabelTransformationClassifier
    extends SingleClassifierEnhancer
    implements MultiLabelClassifier {

    /** for serialization. */
    private static final long serialVersionUID = 1L;
    
    /**
     * The method to transform the labels into another set of latent labels, 
     * typically a compression method is used, e.g., Boolean matrix decomposition 
     * in the case of MLC-BMaD, or matrix multiplication based on SVD for PLST. 
     *
     * @param D the instances to transform into new instances with transformed labels. The 
     * Instances consist of features and original labels.
     * @return The resulting instances. Instances consist of features and transformed labels. 
     */
    public abstract Instances transformLabels(Instances D) throws Exception;

    /**
     * Transforms the instance in the prediction process before given to the internal multi-label
     * or multi-target classifier. The instance is passed having the original set of labels, these
     * must be replaced with the transformed labels (attributes) so that the internla classifier 
     * can predict them.
     *
     * @param x The instance to transform. Consists of features and labels. 
     * @return The transformed instance. Consists of features and transformed labels.
     */
    public abstract Instance transformInstance(Instance x) throws Exception;

    /**
     * Transforms the predictions of the internal classifier back to the original labels. 
     *
     * @param y The predictions that should be transformed back. The array consists only of 
     * the predictions as they are returned from the internal classifier.
     * @return The transformed predictions.
     */
    public abstract double[] transformPredictionsBack(double[] y);


    /** 
     * Default constructor using BR.
     */
    protected LabelTransformationClassifier() {
	m_Classifier = new BR();
    }

    @Override
    protected String defaultClassifierString() {
	return BR.class.getName();
    }
    
    @Override
    public void setClassifier(Classifier newClassifier) {
	if (newClassifier instanceof MultiLabelClassifier)
	    super.setClassifier(newClassifier);
	else
	    System.err.println(
			       "Base classifier must implement " + MultiLabelClassifier.class.getName()
			       + ", provided: " + newClassifier.getClass().getName());
    }

    /**
     * Returns a new set of instances either only with the labels (labels = true) or
     * only the features (labels = false)
     *
     * @param inst The input instances.
     * @param labels Return labels (true) or features (false)
     */
    protected Instances extractPart(Instances inst, boolean labels) throws Exception{
	//TODO Maybe alreade exists somewhere in Meka?
	
	Remove remove = new Remove();
	remove.setAttributeIndices("first-"+(inst.classIndex()));
	remove.setInvertSelection(labels);
	remove.setInputFormat(inst);
	return Filter.useFilter(inst, remove);
    }

    @Override
    public void buildClassifier(Instances D) throws Exception {
	testCapabilities(D);

	int L = D.classIndex();

	if(getDebug()) System.out.print("transforming labels with size: "+L+" baseModel: "+m_Classifier.getClass().getName()+" ");

	Instances transformed_D = this.transformLabels(D);

	m_Classifier.buildClassifier(transformed_D);
    }

    @Override
    public double[] distributionForInstance(Instance x) throws Exception {
	Instance x_transformed = this.transformInstance(x);
	
	double[] y_transformed = m_Classifier.distributionForInstance(x_transformed);
	
	double[] y = this.transformPredictionsBack(y_transformed);

	return y;
    }

    @Override
    public String getRevision() {
	return RevisionUtils.extract("$Revision: 9117 $");
    }

    /**
     * TestCapabilities.
     * Make sure the training data is suitable.
     * @param D	the data
     */
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
