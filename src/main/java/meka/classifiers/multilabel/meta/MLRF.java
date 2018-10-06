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

package meka.classifiers.multilabel.meta;

import java.util.*;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import meka.classifiers.multilabel.*;
import meka.core.*;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

/**
 * Multi-label rotation forest.
 * 
 * @author Aaron Keesing
 */
public class MLRF extends MetaProblemTransformationMethod implements Randomizable, TechnicalInformationHandler {

    private static final long serialVersionUID = -4838278545799641207L;

    /**
     * The number of subsets to generate.
     */
    protected int K = 10;
    /**
     * The dimensionality reduction parameter.
     */
    protected int numFeatures = 10;

    /**
     * Construct a default MLRF object.
     */
    public MLRF() {
        m_Classifier = new BR();
        m_BagSizePercent = 75;
    }

    public int getK() {
        return K;
    }

    public void setK(int k) {
        K = k;
    }

    public String kTipText() {
        return "The number of feature subsets to generate.";
    }

    public int getNumFeatures() {
        return numFeatures;
    }

    public void setNumFeatures(int numFeatures) {
        this.numFeatures = numFeatures;
    }

    public String numFeaturesTipText() {
        return "The dimensionality reduction parameter.";
    }

    /**
     * Partitions the features into disjoint subsets.
     * 
     * @param d the number of features
     * @return a list of subsets of indices corresponding to feature sets
     */
    private List<List<Integer>> generateFeatureSubsets(int d) {
        Random r = new Random(m_Seed);
        List<List<Integer>> result = new ArrayList<>(K);
        int[] indices = A.make_sequence(d);
        A.shuffle(indices, r);
        for (int i = 0; i < K; i++) {
            List<Integer> subset = new ArrayList<>();
            for (int j = 0; j < d/K; j++) {
                if (i*(d/K)+j >= d)
                    break;
                subset.add(indices[i*(d/K)+j]);
            }
            Collections.sort(subset);
            result.add(subset);
        }
        return result;
    }

    @Override
    public void buildClassifier(Instances D) throws Exception {
        testCapabilities(D);
        int n = D.numInstances();
        int L = D.classIndex();
        int d = D.numAttributes() - L;

        m_Classifiers = ProblemTransformationMethod.makeCopies((MultiLabelClassifier)m_Classifier, m_NumIterations);
        for (int i = 0; i < m_NumIterations; i++) {
            if (getDebug())
                System.out.println("Building classifier " + i);
            Random r = new Random(m_Seed + i);
            Instances data = new Instances(D);
            data = F.removeLabels(data, L);
            List<List<Integer>> subsets = generateFeatureSubsets(d);

            Matrix dataMatrix = new Matrix(n, d);
            for (int j = 0; j < n; j++)
                for (int k = 0; k < d; k++)
                    dataMatrix.set(j, k, data.get(j).value(L+k));

            Matrix Ri = new Matrix(d, numFeatures*K);
            int totalFeatures = 0;
            int[] reverseMap = new int[d];
            for (int s = 0; s < K; s++) {
                List<Integer> subset = subsets.get(s);
                int m = subset.size();

                Instances bag = new Instances(D, 0);
                int nNew = n*m_BagSizePercent/100;
                for (int j = 0; j < nNew; j++)
                    bag.add(D.get(r.nextInt(n)));

                Matrix mat = new Matrix(nNew, m);
                for (int j = 0; j < nNew; j++)
                    for (int k = 0; k < m; k++) {
                        mat.set(j, k, bag.get(j).value(subset.get(k)+L));
                        reverseMap[subset.get(k)] = totalFeatures+k;
                    }

                SingularValueDecomposition svd = new SingularValueDecomposition(mat);
                Matrix partialV = svd.getV().getMatrix(0, m-1, 0, numFeatures-1);
                Matrix partialSInv = svd.getS().getMatrix(0, numFeatures-1, 0, numFeatures-1).inverse();
                Matrix transformationMatrix = partialV.times(partialSInv);
                Ri.setMatrix(totalFeatures, totalFeatures+m-1, numFeatures*s, numFeatures*(s+1)-1, transformationMatrix);
                totalFeatures += m;
            }

            // TODO: Construct the permuted matrix directly
            Matrix Ria = new Matrix(d, numFeatures*K);
            for (int j = 0; j < d; j++) {
                Matrix row = Ri.getMatrix(j, j, 0, numFeatures*K-1);
                Ria.setMatrix(reverseMap[j], reverseMap[j], 0, numFeatures*K-1, row);
            }

            Matrix transformedData = dataMatrix.times(Ria);
            Instances transformedInstances = F.remove(D, A.make_sequence(L), true);
            for (int m = 0; m < numFeatures*K; m++) {
                transformedInstances.insertAttributeAt(new Attribute("F" + m), L+m);
            }
            for (int j = 0; j < n; j++) {
                for (int m = 0; m < numFeatures*K; m++) {
                    transformedInstances.get(j).setValue(L+m, transformedData.get(j, m));
                }
            }
            transformedInstances.setClassIndex(L);
            m_Classifiers[i].buildClassifier(transformedInstances);
        }
    }

    @Override
    public double[] distributionForInstance(Instance x) throws Exception {
        return super.distributionForInstance(x);
    }

    @Override
    public String[] getOptions() {
        List<String> result = new ArrayList<>();
        OptionUtils.add(result, "K", K);
        OptionUtils.add(result, "k", numFeatures);
        OptionUtils.add(result, super.getOptions());
        return OptionUtils.toArray(result);
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        super.setOptions(options);
        K = OptionUtils.parse(options, "K", 10);
        numFeatures = OptionUtils.parse(options, "k", 10);
    }

    @Override
    public Enumeration<Option> listOptions() {
        Vector<Option> options = new Vector<>();
        options.add(new Option(kTipText(), "K", 1, "-K k"));
        options.add(new Option(numFeaturesTipText(), "k", 1, "-k numFeatures"));
        OptionUtils.add(options, super.listOptions());
        return options.elements();
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        TechnicalInformation info = new TechnicalInformation(Type.ARTICLE);
        info.setValue(Field.AUTHOR, "Elghazel, Haytham and Aussem, Alex and Gharroudi, Ouadie and Saadaoui, Wafa");
        info.setValue(Field.TITLE, "Ensemble multi-label text categorization based on rotation forest and latent semantic indexing");
        info.setValue(Field.JOURNAL, "Expert Systems with Applications");
        info.setValue(Field.VOLUME, "57");
        info.setValue(Field.YEAR, "2016");
        info.setValue(Field.PUBLISHER, "Elsevier");
        info.setValue(Field.PAGES, "1--11");
        return info;
    }

    public static void main(String[] args) {
        ProblemTransformationMethod.runClassifier(new MLRF(), args);
    }
}
