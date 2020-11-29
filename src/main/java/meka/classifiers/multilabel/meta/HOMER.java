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

import meka.classifiers.multilabel.*;
import meka.core.*;
import weka.classifiers.*;
import weka.classifiers.meta.Bagging;
import weka.core.*;
import weka.core.TechnicalInformation.*;

import java.io.Serializable;
import java.util.*;

/**
 * HOMER (Hierarchy Of Multi-label classifiERs algorithm.
 * <p>
 * This algorithm divides the multilabel classification problem into a tree structure where at each level the labels are
 * partitioned into k subsets and one multilabel classifier classifies the labels per subset, using k metalabels
 * representing disjoint sets of labels.
 *
 * @author Aaron Keesing
 */
public class HOMER extends ProblemTransformationMethod implements Randomizable, TechnicalInformationHandler {

    private static final long serialVersionUID = 3358633077067198326L;
    /**
     * The splitting factor.
     */
    protected int k = 5;
    /**
     * Random seed.
     */
    protected int seed = 0;
    /**
     * The threshold to use for the multi-label classifier distribution at each node.
     */
    protected String threshold = "0.3";
    /**
     * Root node of the HOMER tree.
     */
    private HOMERNode root;
    /**
     * The label splitter.
     */
    protected LabelSplitter labelSplitter = new RandomLabelSplitter(0);
    /**
     * HOMER node ID counter for debugging purposes.
     */
    private int debugNodeId = 0;

    /**
     * Create a new HOMER classifier with default settings.
     */
    public HOMER() {
        m_Classifier = new meka.classifiers.multilabel.BR();
    }

    public static void main(String[] args) {
        ProblemTransformationMethod.runClassifier(new HOMER(), args);
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String t) {
        threshold = t;
    }

    public LabelSplitter getLabelSplitter() {
        return labelSplitter;
    }

    public void setLabelSplitter(LabelSplitter splitter) {
        this.labelSplitter = splitter;
    }

    public String kTipText() {
        return "The number of partitions per level.";
    }

    public String seedTipText() {
        return "The seed to set.";
    }

    public String labelSplitterTipText() {
        return "The label splitter class to use.";
    }

    public String thresholdTipText() {
        return "The threshold for the multi-label classifier distribution";
    }

    public String getLabelSplitterString() {
        if (labelSplitter instanceof RandomLabelSplitter)
            return "random";
        else if (labelSplitter instanceof ClusterLabelSplitter)
            return "cluster";
        else
            return labelSplitter.getClass().getCanonicalName();
    }

    public void setLabelSplitterString(String str) {
        if (str.equals("cluster"))
            labelSplitter = new ClusterLabelSplitter(getSeed());
        else
            labelSplitter = new RandomLabelSplitter(getSeed());
    }

    @Override
    public String globalInfo() {
        return "HOMER tree algorithm. For more information see:\n" + getTechnicalInformation().toString();
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        TechnicalInformation info = new TechnicalInformation(Type.INPROCEEDINGS);
        info.setValue(Field.AUTHOR, "Tsoumakas, Grigorios and Katakis, Ioannis and Vlahavas, Ioannis");
        info.setValue(Field.TITLE, "Effective and efficient multilabel classification in domains with large number of labels");
        info.setValue(Field.YEAR, "2008");
        info.setValue(Field.PAGES, "53--59");
        info.setValue(Field.BOOKTITLE, "Proc. ECML/PKDD 2008 Workshop on Mining Multidimensional Data (MMD’08)");
        info.setValue(Field.VOLUME, "21");
        info.setValue(Field.ORGANIZATION, "sn");
        return info;
    }

    @Override
    protected String defaultClassifierString() {
        return meka.classifiers.multilabel.BR.class.getName();
    }

    @Override
    public String getModel() {
        return m_Classifier.toString();
    }

    @Override
    public void buildClassifier(Instances D) throws Exception {
        testCapabilities(D);

        if (!(m_Classifier instanceof MultiLabelClassifier))
            throw new IllegalStateException("Classifier must be a MultiLabelClassifier!");

        root = new HOMERNode();
        root.setClassifier(ProblemTransformationMethod.makeCopy(m_Classifier));
        root.setSplitter(labelSplitter);
        root.buildClassifier(D);
        if (getDebug())
            System.out.println("Trained all nodes.");
    }

    @Override
    public double[] distributionForInstance(Instance x) throws Exception {
        double[] y = new double[x.classIndex()];
        root.classify(x, y);
        return y;
    }

    @Override
    public Enumeration<Option> listOptions() {
        Vector<Option> options = new Vector<>();
        options.add(new Option(kTipText(), "k", 1, "-k K"));
        options.add(new Option(seedTipText(), "seed", 1, "-S seed"));
        options.add(new Option(labelSplitterTipText(), "label splitter", 1, "-ls class"));
        options.add(new Option(thresholdTipText(), "threshold", 1, "-t threshold"));
        OptionUtils.add(options, super.listOptions());
        return options.elements();
    }

    @Override
    public String[] getOptions() {
        List<String> result = new ArrayList<>();
        OptionUtils.add(result, "k", k);
        OptionUtils.add(result, "S", seed);
        OptionUtils.add(result, "t", threshold);
        OptionUtils.add(result, "ls", getLabelSplitterString());
        OptionUtils.add(result, super.getOptions());
        return OptionUtils.toArray(result);
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        super.setOptions(options);
        k = OptionUtils.parse(options, "k", 3);
        seed = OptionUtils.parse(options, "S", 0);
        threshold = OptionUtils.parse(options, "t", "0.3");
        setLabelSplitterString(OptionUtils.parse(options, "ls", "RandomLabelSplitter"));
    }

    @Override
    public int getSeed() {
        return seed;
    }

    /**
     * Sets the random seed of this HOMER classifier and {@link #labelSplitter}.
     *
     * @param s seed
     */
    @Override
    public void setSeed(int s) {
        seed = s;
        if (labelSplitter instanceof Randomizable)
            ((Randomizable) labelSplitter).setSeed(s);
    }


    /**
     * Interface that represents a label splitter, that partitions a set of labels into {@code k} disjoint subsets.
     */
    public interface LabelSplitter extends Serializable {

        /**
         * Partition {@code labels} into non-empty disjoint subsets of labels.
         *
         * @param k      maximum number of subsets
         * @param labels the labels to split
         * @param D      the dataset that defines the labels
         * @return a partition of the labels in {@code parentLabels}
         */
        Collection<Set<Integer>> splitLabels(int k, Collection<Integer> labels, Instances D);
    }

    /**
     * Label splitter that also has a random component.
     */
    public abstract static class RandomizableLabelSplitter implements LabelSplitter, Randomizable {

        /**
         * Random number generator used to generate splits.
         */
        protected final Random rng;
        /**
         * Random seed for {@link #rng}.
         */
        protected int seed;

        public RandomizableLabelSplitter(int seed) {
            this.seed = seed;
            rng = new Random(seed);
        }

        @Override
        public int getSeed() {
            return seed;
        }

        @Override
        public void setSeed(int s) {
            seed = s;
            rng.setSeed(s);
        }
    }

    /**
     * A class for randomly splitting the labels into k disjoint subsets.
     */
    public static class RandomLabelSplitter extends RandomizableLabelSplitter {

        private static final long serialVersionUID = -4151444787544325296L;

        public RandomLabelSplitter(int seed) {
            super(seed);
        }

        @Override
        public Collection<Set<Integer>> splitLabels(int k, Collection<Integer> labels, Instances D) {
            List<Set<Integer>> result = new ArrayList<>();
            int L = labels.size();
            List<Integer> labelList = Arrays.asList(labels.toArray(new Integer[0]));
            Collections.shuffle(labelList, rng);
            int idx = 0;
            if (L <= k) {
                for (int i = 0; i < L; i++) {
                    Set<Integer> labelSet = new HashSet<>(1);
                    labelSet.add(labelList.get(idx++));
                    result.add(labelSet);
                }
            } else {
                for (int i = 0; i < k; i++) {
                    Set<Integer> labelSet = new HashSet<>(L / k);
                    for (int x = 0; x < (i < L % k ? L / k + 1 : L / k); x++)
                        labelSet.add(labelList.get(idx++));
                    result.add(labelSet);
                }
            }
            return result;
        }
    }

    /**
     * A class for splitting labels into equal clusters based on label similarity. Label similarity is calculated by
     * treating the labels as column vectors. This uses the balanced k-mean algorithm, which is an extension of the
     * k-means algorithm.
     */
    public static class ClusterLabelSplitter extends RandomizableLabelSplitter {

        private static final long serialVersionUID = 3545709733670034266L;
        private double[][] labelVectors = null;

        public ClusterLabelSplitter(int seed) {
            super(seed);
        }

        @Override
        public Collection<Set<Integer>> splitLabels(int k, Collection<Integer> labels, Instances D) {
            int L = D.classIndex();
            int n = D.numInstances();
            if (labelVectors == null) {
                labelVectors = new double[L][n];
                for (int i = 0; i < n; i++)
                    for (int l = 0; l < L; l++)
                        labelVectors[l][i] = D.get(i).value(l);
            }

            List<Integer> labelList = new ArrayList<>(labels);
            int idx = 0;
            int Ln = labels.size();

            if (Ln <= k) {
                List<Set<Integer>> result = new ArrayList<>(Ln);
                for (int i = 0; i < Ln; i++)
                    result.add(new HashSet<>(Collections.singletonList(labelList.get(idx++))));
                return result;
            }

            List<List<Integer>> clusters = new ArrayList<>(k);
            int[] centres = new int[k];
            Collections.shuffle(labelList, rng);
            for (int i = 0; i < k; i++) {
                clusters.add(new ArrayList<>(L / k));
                centres[i] = labelList.get(idx++);
            }

            double[][] d = new double[L][k];
            for (int it = 0; it < 2; it++) {
                for (Integer l : labels) {
                    for (int i = 0; i < k; i++) {
                        /* Calculate distance to cluster centres */
                        double[] vci = labelVectors[centres[i]];
                        double[] vl = labelVectors[l];
                        double sum = 0;
                        for (int j = 0; j < n; j++)
                            sum += (vci[j] - vl[j]) * (vci[j] - vl[j]);
                        d[l][i] = Math.sqrt(sum);
                    }

                    boolean finished = false;
                    int nu = l;
                    while (!finished) {
                        int minj = 0;
                        double currMin = Double.MAX_VALUE;
                        for (int j = 0; j < k; j++) {
                            if (d[nu][j] < currMin) {
                                currMin = d[nu][j];
                                minj = j;
                            }
                        }

                        List<Integer> Cj = clusters.get(minj);
                        if (Cj.contains(nu))
                            break;
                        Cj.add(nu);
                        final int _minj = minj;
                        Cj.sort(Comparator.comparingDouble(o -> d[o][_minj]));

                        int size = Cj.size();
                        if (size > Math.ceil(Ln / (double) k)) {
                            nu = Cj.remove(size - 1);
                            d[nu][minj] = Double.MAX_VALUE;
                        } else {
                            finished = true;
                        }
                    }
                }
                for (int i = 0; i < k; i++) {
                    double[] cent = new double[n];
                    for (Integer l : clusters.get(i)) {
                        /* Calculate new cluster centres */
                        double[] vl = labelVectors[l];
                        for (int j = 0; j < n; j++)
                            cent[j] += vl[j];
                    }
                    for (int j = 0; j < n; j++)
                        cent[j] /= clusters.get(i).size();
                }
            }
            Collection<Set<Integer>> clusterSets = new ArrayList<>(clusters.size());
            for (List<Integer> cluster : clusters)
                clusterSets.add(new HashSet<>(cluster));
            return clusterSets;
        }
    }

    /**
     * Utility class for storing node info.
     */
    private class HOMERNode extends SingleClassifierEnhancer {

        private final int id;
        private final List<HOMERNode> children;
        private Set<Integer> labels;
        private Instances instances;
        private double[] thresholds;
        private LabelSplitter splitter;

        public HOMERNode() {
            id = debugNodeId++;
            children = new ArrayList<>();
            labels = new HashSet<>();
        }

        /**
         * Build the HOMER tree.
         *
         * @throws Exception the meta-classifier fails
         */
        private void buildNode() throws Exception {
            if (getDebug()) {
                System.out.print("Building subtree of node " + id + " with labels: ");
                for (Integer label : labels)
                    System.out.print(label.toString() + ",");
                System.out.println();
            }

            Instances culledInstances = new Instances(instances, 0);
            for (Instance i : getInstances()) {
                for (Integer label : labels) {
                    if (i.stringValue(label).equals("1")) {
                        culledInstances.add(i);
                        break;
                    }
                }
            }

            getChildren().clear();
            Collection<Set<Integer>> labelSplits = splitter.splitLabels(k, labels, culledInstances);
            for (Set<Integer> labelSet : labelSplits) {
                HOMERNode child = new HOMERNode();
                child.setClassifier(AbstractClassifier.makeCopy(m_Classifier));
                child.setSplitter(splitter);
                child.setInstances(culledInstances);
                child.setLabels(labelSet);
                getChildren().add(child);
                if (labelSet.size() > 1) // Leaves don't need to recurse down any further
                    child.buildNode();
            }
        }

        /**
         * Train the HOMER tree.
         *
         * @throws Exception If an exception is thrown during label preprocessing, training the base classifier, or training any
         *                   children of this node.
         */
        private void trainNode() throws Exception {
            int L = instances.classIndex();
            int c = children.size();

            /* Generate meta-label attributes */
            Instances newInstances = F.removeLabels(instances, L);
            for (int i = 0; i < c; i++) {
                Attribute att = new Attribute("MetaLabel" + i, Arrays.asList("0", "1"));
                newInstances.insertAttributeAt(att, i);
            }
            newInstances.setClassIndex(c);

            /* Set each meta-label value to the disjunction of instance label values */
            Iterator<Instance> it = newInstances.iterator();
            for (int i = 0; it.hasNext(); i++) {
                boolean remove = true;
                Instance next = it.next();
                for (int m = 0; m < c; m++) {
                    next.setValue(m, "0");
                    for (Integer l : children.get(m).getLabels()) {
                        if (instances.get(i).stringValue(l).equals("1")) {
                            next.setValue(m, "1");
                            remove = false;
                            break;
                        }
                    }
                }
                if (remove)
                    it.remove();
            }
            setInstances(newInstances);
            if (!newInstances.isEmpty()) { // This should rarely not be the case
                Classifier classifier = ((ProblemTransformationMethod) m_Classifier).getClassifier();
                if (newInstances.size() == 1 && classifier instanceof Bagging)
                    ((Bagging) classifier).setBagSizePercent(100);
                m_Classifier.buildClassifier(newInstances);
            }
            if (getDebug())
                System.out.println("Trained node " + getId() + " with " + newInstances.size() + " instances.");

            if (newInstances.size() < c) {
                double[] thresholds = new double[c];
                Arrays.fill(thresholds, 0.5);
                setThresholds(thresholds);
            } else {
                try {
                    Double.parseDouble(threshold);
                    setThresholds(ThresholdUtils.thresholdStringToArray(threshold, c));
                } catch (NumberFormatException e) {
                    Result r = meka.classifiers.multilabel.Evaluation.testClassifier((MultiLabelClassifier) m_Classifier, newInstances);
                    String threshStr = MLEvalUtils.getThreshold(r.predictions, newInstances, threshold);
                    setThresholds(ThresholdUtils.thresholdStringToArray(threshStr, c));
                }
            }

            for (HOMERNode child : children) {
                if (child.getLabels().size() > 1)
                    child.trainNode();
            }
        }

        /**
         * Classify an instance x.
         *
         * @param x the instance to classify
         * @param y shared array of prediction values
         * @throws Exception If this node's classifier or any child classifiers throw an exception.
         */
        public void classify(Instance x, double[] y) throws Exception {
            int c = getChildren().size();
            Instance newX = (Instance) x.copy();
            int oldL = newX.classIndex();
            newX.setDataset(null);
            MLUtils.deleteAttributesAt(newX, A.make_sequence(oldL));
            for (int i = 0; i < c; i++)
                newX.insertAttributeAt(i);
            newX.setDataset(getInstances());
            double[] metaDist;
            if (!getInstances().isEmpty())
                metaDist = getClassifier().distributionForInstance(newX); // Distribution on meta-labels
            else
                metaDist = new double[c];
            double[] thresholds = getThresholds();
            for (int i = 0; i < getChildren().size(); i++) {
                HOMERNode child = getChildren().get(i);
                if (metaDist[i] > thresholds[i]) {
                    if (child.getLabels().size() == 1)
                        y[child.getLabels().iterator().next()] = 1;
                    else
                        child.classify(newX, y);
                } else {
                    for (int l : child.getLabels())
                        y[l] = 0;
                }
            }
        }

        public List<HOMERNode> getChildren() {
            return children;
        }

        public Set<Integer> getLabels() {
            return labels;
        }

        public void setLabels(Set<Integer> labels) {
            this.labels = labels;
        }

        public Instances getInstances() {
            return instances;
        }

        public void setInstances(Instances instances) {
            this.instances = instances;
        }

        public double[] getThresholds() {
            return thresholds;
        }

        public void setThresholds(double[] thresholds) {
            this.thresholds = thresholds;
        }

        public int getId() {
            return id;
        }

        public void setSplitter(LabelSplitter splitter) {
            this.splitter = splitter;
        }

        @Override
        public void buildClassifier(Instances D) throws Exception {
            int L = D.classIndex();
            Set<Integer> labelSet = new HashSet<>(L);
            for (int l = 0; l < L; l++)
                labelSet.add(l);

            setInstances(D);
            setLabels(labelSet);
            buildNode();
            trainNode();
        }
    }
}
