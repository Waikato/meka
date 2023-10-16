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

/*
 * Copyright (C) 2023 Aaron Keesing et al
 */

package meka.classifiers.multilabel.meta;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.core.A;
import meka.core.F;
import meka.core.MLUtils;
import meka.core.OptionUtils;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;

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

	private DenseMatrix[] R;

	public MLRF() {
		m_Classifier = new BR();
		m_BagSizePercent = 75;
	}

	@Override
	public String globalInfo() {
		return "Multi-label rotation forest.";
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

	@Override
	protected String defaultClassifierString() {
		return "meka.classifiers.multilabel.BR";
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
			for (int j = 0; j < d / K; j++) {
				if (i * (d / K) + j >= d)
					break;
				subset.add(indices[i * (d / K) + j]);
			}
			Collections.sort(subset);
			result.add(subset);
		}
		return result;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);
		int numInstances = D.numInstances();
		int L = D.classIndex();
		int d = D.numAttributes() - L;

		m_Classifiers = ProblemTransformationMethod.makeCopies((MultiLabelClassifier) m_Classifier, m_NumIterations);
		R = new DenseMatrix[m_NumIterations];
		Instances transformedInstances = F.remove(D, A.make_sequence(L), true);
		int nNew = numInstances * m_BagSizePercent / 100;

		Matrix dataMatrix;
		try {
			dataMatrix = new DenseMatrix(numInstances, d);
			for (int i = 0; i < numInstances; i++)
				for (int j = 0; j < d; j++)
					dataMatrix.set(i, j, D.get(i).value(L + j));
		}
		catch (OutOfMemoryError e) {
			int[][] nz = new int[numInstances][];
			for (int i = 0; i < numInstances; i++) {
				Instance inst = transformedInstances.get(i);
				nz[i] = new int[inst.numValues()];
				for (int j = 0; j < inst.numValues(); j++)
					nz[i][j] = inst.index(j);
			}
			dataMatrix = new CompRowMatrix(numInstances, d, nz);
			for (int i = 0; i < numInstances; i++) {
				Instance inst = transformedInstances.get(i);
				for (int j = 0; j < inst.numValues(); j++)
					dataMatrix.set(i, nz[i][j], inst.valueSparse(j));
			}
		}
		DenseMatrix transformedData = new DenseMatrix(numInstances, numFeatures * K);

		// Instance attributes after dimensionality reduction
		for (int m = 0; m < numFeatures * K; m++)
			transformedInstances.insertAttributeAt(new Attribute("F" + m), L + m);
		transformedInstances.setClassIndex(L);
		m_InstancesTemplate = transformedInstances;

		for (int i = 0; i < m_NumIterations; i++) {
			if (getDebug())
				System.out.print("Building classifier " + i + ": subsets ");
			Random r = new Random(m_Seed + i);

			R[i] = new DenseMatrix(d, numFeatures * K);
			List<List<Integer>> subsets = generateFeatureSubsets(d);
			for (int subId = 0; subId < K; subId++) {
				if (getDebug())
					System.out.print(subId + " ");
				List<Integer> subset = subsets.get(subId);
				int subSize = subset.size();
				DenseMatrix mat = new DenseMatrix(nNew, subSize);
				// Bootstrap sample from data
				for (int j = 0; j < nNew; j++) {
					int sampId = r.nextInt(numInstances);
					for (int k = 0; k < subSize; k++)
						mat.set(j, k, dataMatrix.get(sampId, subset.get(k)));
				}

				// Latent semantic indexing
				SVD svd = new SVD(nNew, subSize);
				svd = svd.factor(mat);
				DenseMatrix transformationMatrix = getTransformationMatrix(svd, subSize);

				// Insert each subset transformation matrix into full rotation matrix R[i]
				for (int j = 0; j < subSize; j++) {
					Matrix row = Matrices.getSubMatrix(transformationMatrix,
						new int[]{j},
						A.make_sequence(numFeatures));
					Matrices.getSubMatrix(R[i],
						new int[]{subset.get(j)},
						A.make_sequence(numFeatures * subId, numFeatures * (subId + 1))).set(row);
				}
			}

			// Transform data and train classifier on transformed instances
			dataMatrix.mult(R[i], transformedData);
			for (int m = 0; m < numFeatures * K; m++)
				for (int j = 0; j < numInstances; j++)
					transformedInstances.get(j).setValue(L + m, transformedData.get(j, m));
			if (getDebug())
				System.out.print("Training base multi-label classifier on transformed data.");
			m_Classifiers[i].buildClassifier(transformedInstances);
			if (getDebug())
				System.out.println();
		}
	}

	private DenseMatrix getTransformationMatrix(SVD svd, int m) {
		double[] singVals = svd.getS();
		Matrix partialVt = Matrices.getSubMatrix(svd.getVt(), A.make_sequence(numFeatures), A.make_sequence(m));
		DenseMatrix partialSInv = new DenseMatrix(numFeatures, numFeatures);
		for (int j = 0; j < numFeatures; j++)
			if (singVals[j] > 0)
				partialSInv.set(j, j, 1 / singVals[j]);

		DenseMatrix transformationMatrix = new DenseMatrix(m, numFeatures);
		partialVt.transAmult(partialSInv, transformationMatrix);
		return transformationMatrix;
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		double[] dist = new double[L];
		int d = x.numAttributes() - L;

		DenseMatrix vec = new DenseMatrix(1, d);
		for (int i = 0; i < d; i++)
			vec.set(0, i, x.value(L + i));

		DenseMatrix transformedInstance = new DenseMatrix(1, numFeatures * K);
		Instance x_ = (Instance) x.copy();
		x_.setDataset(null);
		MLUtils.keepAttributesAt(x_, A.make_sequence(L), L + d);
		for (int i = 0; i < numFeatures * K; i++)
			x_.insertAttributeAt(L + i);
		x_.setDataset(m_InstancesTemplate);

		for (int h = 0; h < m_NumIterations; h++) {
			vec.mult(R[h], transformedInstance);
			for (int i = 0; i < numFeatures * K; i++)
				x_.setValue(L + i, transformedInstance.get(0, i));
			double[] hdist = m_Classifiers[h].distributionForInstance(x_);
			for (int i = 0; i < L; i++)
				dist[i] += hdist[i];
		}

		for (int i = 0; i < L; i++)
			dist[i] /= m_NumIterations;
		return dist;
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
		K = OptionUtils.parse(options, "K", 10);
		numFeatures = OptionUtils.parse(options, "k", 10);
		super.setOptions(options);
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
		info.setValue(Field.TITLE,
			"Ensemble multi-label text categorization based on rotation forest and latent semantic indexing");
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
