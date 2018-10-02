/**
 * 
 */
package meka.classifiers.multilabel.meta;

import java.util.*;

import meka.classifiers.multilabel.*;
import meka.classifiers.multilabel.meta.HOMER.ClusterLabelSplitter;
import meka.core.*;
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

    protected double threshold = 0.4;
    protected String metaClassifierString = "BinaryRelevance";

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

    /* (non-Javadoc)
     * @see meka.classifiers.multilabel.AbstractMultiLabelClassifier#buildClassifier(weka.core.Instances)
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
                rf.setMaxDepth(r.nextInt(5) + 1);
                rf.setBagSizePercent(r.nextInt(21) + 60);
                rf.setNumIterations(r.nextInt(3) + 1);
            }
            ((SingleClassifierEnhancer)((HOMER)m_Classifiers[i]).getClassifier()).setClassifier(m_Classifier);
            ((Randomizable)((HOMER)m_Classifiers[i]).getClassifier()).setSeed(m_Seed + i);
            m_Classifiers[i].buildClassifier(bag);
        }
        
        if (getDebug())
            System.out.println();
    }

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
