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

import meka.classifiers.multilabel.*;
import meka.classifiers.multilabel.meta.HOMER.ClusterLabelSplitter;
import meka.core.*;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

/**
 * Extremely Randomised Forest with HOMER trees algorithm.
 * 
 * @author Aaron Keesing
 */
public class ERFH extends MetaProblemTransformationMethod implements Randomizable, TechnicalInformationHandler {

    /**
     * The same as {@link ClusterLabelSplitter} except uses a random number of
     * splits at each node of the tree, within fixed bounds.
     */
    public static class VariableKLabelSplitter extends ClusterLabelSplitter {
        private static final long serialVersionUID = 9211371179003763478L;
        private Random r;

        public VariableKLabelSplitter(int seed) {
            super(seed);
            r = new Random(seed);
        }

        @Override
        public Collection<Set<Integer>> splitLabels(int k, Collection<Integer> labels, Instances D) {
            return super.splitLabels(r.nextInt(5) + 2, labels, D);
        }
    }

    private static final long serialVersionUID = 5482843422132209885L;

    private SingleClassifierEnhancer metaClassifier;

    /**
     * Threshold probability for committee classification.
     */
    protected double threshold = 0.4;
    protected String metaClassifierString = "BinaryRelevance";

    /**
     * Construct a default ERFH instance with default settings.
     */
    public ERFH() {
        m_Classifier = new RandomForest();
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String thresholdTipText() {
        return "Prediction threshold for the multi-label classifier distribution.";
    }

    public void setMetaClassifier(SingleClassifierEnhancer classifier) {
        metaClassifier = classifier;
    }

    public String getMetaClassifierString() {
        return metaClassifierString;
    }

    public void setMetaClassifierString(String str) {
        if (str.equals("ClassifierChains")) {
            metaClassifier = new CC();
        } else {
            metaClassifier = new BR();
        }
    }

    public String metaClassifierStringTipText() {
        return "Meta-classifier for each HOMER node. One of {BinaryRelevance, ClassifierChains}";
    }

    /**
     * Builds each HOMER tree using bagging, while randomising the settings for
     * the classifier at each node of the tree.
     * 
     * @param instances
     *            the instances to train with
     */
    @Override
    public void buildClassifier(Instances D) throws Exception {
        testCapabilities(D);
        int numInstances = D.numInstances();

        if (getDebug())
            System.out.print("Building " + m_NumIterations + " HOMER trees:");
        m_Classifiers = new HOMER[m_NumIterations];
        for(int i = 0; i < m_NumIterations; i++) {
            Random r = new Random(m_Seed + i);
            Instances bag = new Instances(D, 0);    
            m_Classifiers[i] = new HOMER();
            ((HOMER)m_Classifiers[i]).setClassifier(ProblemTransformationMethod.makeCopy(metaClassifier));
            ((HOMER)m_Classifiers[i]).setSeed(m_Seed + i);
            ((HOMER)m_Classifiers[i]).setLabelSplitter(new VariableKLabelSplitter(m_Seed + i));
            if (getDebug())
                System.out.print(" " + i);

            for (int j = 0; j < numInstances; j++)
                bag.add(D.get(r.nextInt(numInstances)));

            if (m_Classifier instanceof RandomForest) {
                RandomForest rf = (RandomForest)m_Classifier;
                rf.setSeed(m_Seed + i);
                rf.setMaxDepth(20 + r.nextInt(20));
                rf.setBagSizePercent(80 + r.nextInt(21));
                rf.setNumIterations(30 + r.nextInt(30));
            } else if (m_Classifier instanceof Randomizable) {
                ((Randomizable)m_Classifier).setSeed(m_Seed + i);
            }
            ((SingleClassifierEnhancer)((HOMER)m_Classifiers[i]).getClassifier()).setClassifier(AbstractClassifier.makeCopy(m_Classifier));
            m_Classifiers[i].buildClassifier(bag);
        }
        
        if (getDebug())
            System.out.println();
    }

    /**
     * Calculate the classification probabilities for each label based on the
     * average committee probabilities per label and the given threshold.
     */
    @Override
    public double[] distributionForInstance(Instance x) throws Exception {
        double[] p = super.distributionForInstance(x);
        for (int i = 0; i < p.length; i++)
            p[i] = p[i] > threshold ? 1 : 0;
        return p;
    }

    @Override
    public String[] getOptions() {
        List<String> result = new ArrayList<>();
        OptionUtils.add(result, "t", threshold);
        OptionUtils.add(result, "M", metaClassifierString);
        OptionUtils.add(result, super.getOptions());
        return OptionUtils.toArray(result);
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        super.setOptions(options);
        threshold = OptionUtils.parse(options, "t", 0.4);
        setMetaClassifierString(OptionUtils.parse(options, "M", ""));
    }

    @Override
    public Enumeration<Option> listOptions() {
        Vector<Option> options = new Vector<>();
        options.add(new Option(thresholdTipText(), "threshold", 1, "-t threshold"));
        options.add(new Option(metaClassifierStringTipText(), "metaClassifier", 1, "-M string"));
        OptionUtils.add(options, super.listOptions());
        return options.elements();
    }

    @Override
    public String globalInfo() {
        return "Extremely Randomised Forest of HOMER trees.";
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        TechnicalInformation info = new TechnicalInformation(Type.INPROCEEDINGS);
        info.setValue(Field.AUTHOR, "Li, Jinxia and Zheng, Yihan and Han, Chao and Wu, Qingyao and Chen, Jian");
        info.setValue(Field.EDITOR, "Sun, Yi and Lu, Huchuan and Zhang, Lihe and Yang, Jian and Huang, Hua");
        info.setValue(Field.TITLE, "Extremely Randomized Forest with Hierarchy of Multi-label Classifiers");
        info.setValue(Field.BOOKTITLE, "Intelligence Science and Big Data Engineering");
        info.setValue(Field.YEAR, "2017");
        info.setValue(Field.PUBLISHER, "Springer International Publishing");
        info.setValue(Field.ADDRESS, "Cham");
        info.setValue(Field.PAGES, "450--460");
        info.setValue(Field.ISBN, "978-3-319-67777-4");
        return info;
    }

    public static void main(String[] args) {
        ProblemTransformationMethod.runClassifier(new ERFH(), args);
    }
}
