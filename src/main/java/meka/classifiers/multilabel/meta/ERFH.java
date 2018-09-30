/**
 * 
 */
package meka.classifiers.multilabel.meta;

import java.util.*;

import meka.classifiers.multilabel.*;
import meka.classifiers.multilabel.meta.HOMER.ClusterLabelSplitter;
import meka.core.*;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

/**
 * Extremely Randomised Forest with HOMER trees algorithm.
 * 
 * @author Aaron Keesing
 */
public class ERFH extends BaggingML implements Randomizable, TechnicalInformationHandler {

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

    protected double threshold = 0.4;

    public ERFH() {
        m_Classifier = new HOMER();
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

    /* (non-Javadoc)
     * @see meka.classifiers.multilabel.AbstractMultiLabelClassifier#buildClassifier(weka.core.Instances)
     */
    @Override
    public void buildClassifier(Instances D) throws Exception {
        testCapabilities(D);

        if (getDebug())
            System.out.println("Building " + m_NumIterations + " HOMER trees.");
        m_Classifiers = ProblemTransformationMethod.makeCopies((MultiLabelClassifier)m_Classifier, m_NumIterations);

        for(int i = 0; i < m_NumIterations; i++) {
            Random r = new Random(m_Seed + i);
            Instances bag = new Instances(D);
            ((HOMER)m_Classifiers[i]).setSeed(m_Seed + i);
            if(getDebug())
                System.out.print("" + i + " ");

            int ixs[] = new int[D.numInstances()];
            for(int j = 0; j < ixs.length; j++)
                ixs[r.nextInt(ixs.length)]++;
            for(int j = 0; j < ixs.length; j++) {
                if (ixs[j] > 0) {
                    Instance instance = D.instance(j);
                    instance.setWeight(ixs[j]);
                    bag.add(instance);
                }
            }

            ((HOMER)m_Classifiers[i]).setLabelSplitter(new VariableKLabelSplitter(m_Seed));
            RandomForest rf = new RandomForest();
            rf.setSeed(m_Seed + i);
            rf.setMaxDepth(r.nextInt(5) + 1);
            rf.setBagSizePercent(r.nextInt(21) + 60);
            rf.setNumIterations(r.nextInt(3) + 1);
            ((BR)((HOMER)m_Classifiers[i]).getClassifier()).setClassifier(rf);
            m_Classifiers[i].buildClassifier(bag);
        }
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
        OptionUtils.add(result, super.getOptions());
        return OptionUtils.toArray(result);
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        super.setOptions(options);
        threshold = OptionUtils.parse(options, "t", 0.4);
    }

    @Override
    public Enumeration<Option> listOptions() {
        Vector<Option> options = new Vector<>();
        options.add(new Option(thresholdTipText(), "threshold", 1, "-t threshold"));
        OptionUtils.add(options, super.listOptions());
        return options.elements();
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
