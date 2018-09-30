package meka.classifiers.multilabel.meta;

import java.io.Serializable;
import java.util.*;

import meka.classifiers.multilabel.*;
import meka.core.*;
import weka.classifiers.AbstractClassifier;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

/**
 * HOMER (Hierarchy Of Multi-label classifiERs algorithm.
 * 
 * This algorithm divides the multilabel classification problem into a tree
 * structure where at each level the labels are paritioned into k subsets and
 * one multilabel classifier classifies the labels per subset, using k
 * metalabels representing disjoint sets of labels.
 * 
 * @author Aaron Keesing
 */
public class HOMER extends ProblemTransformationMethod implements Randomizable, TechnicalInformationHandler {

    /**
     * Utility class for storing node info.
     */
    private static class HOMERNode {
        private static int _id = 0;

        private int id;
        private List<HOMERNode> children;
        private Set<Integer> labels;
        private MultiLabelClassifier classifier;
        private Instances instances;

        public HOMERNode() {
            id = _id++;
            children = new ArrayList<>();
            labels = new HashSet<>();
        }

        public void buildClassifier() throws Exception {
            classifier.buildClassifier(instances);
        }

        public List<HOMERNode> getChildren() {
            return children;
        }

        public MultiLabelClassifier getClassifier() {
            return classifier;
        }

        public void setClassifier(MultiLabelClassifier classifier) {
            this.classifier = classifier;
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

        public int getId() {
            return id;
        }
    }

    /**
     * Interface that represents a label splitter, that partitions a set of
     * labels into {@link #k} disjoint subsets.
     */
    public interface LabelSplitter extends Serializable {
        /**
         * Partition {@code labels} into non-empty disjoint subsets of labels.
         *
         * @param parentLabels
         *              the labels of the parent node
         * @param D
         *              the dataset that defines the labels
         * @return a partition of the labels in {@code parentLabels}
         */
        public Collection<Set<Integer>> splitLabels(int k, Collection<Integer> labels, Instances D);
    }

    /**
     * A class for randomly splitting the labels into k disjoint subsets.
     */
    public static class RandomLabelSplitter implements LabelSplitter {
        private static final long serialVersionUID = -4151444787544325296L;
        private Random r;

        public RandomLabelSplitter(int seed) {
            r = new Random(seed);
        }

        @Override
        public Collection<Set<Integer>> splitLabels(int k, Collection<Integer> labels, Instances D) {
            List<Set<Integer>> result = new ArrayList<>();
            int L = labels.size();
            List<Integer> labelList = Arrays.asList(labels.toArray(new Integer[0]));
            Collections.shuffle(labelList, r);
            int idx = 0;
            if (L <= k) {
                for (int i = 0; i < L; i++) {
                    Set<Integer> labelSet = new HashSet<>(1);
                    labelSet.add(labelList.get(idx++));
                    result.add(labelSet);
                }
            } else {
                for (int i = 0; i < k; i++) {
                    Set<Integer> labelSet = new HashSet<>(L/k);
                    for (int x = 0; x < (i < L%k ? L/k+1 : L/k); x++)
                        labelSet.add(labelList.get(idx++));
                    result.add(labelSet);
                }
            }
            return result;
        }
    }

    /**
     * A class for splitting labels into equal clusters based on label similarity.
     * Label similarity is calculated by treating the labels as column vectors.
     * This uses the balances k-mean algorithm, which is an extension of the
     * k-means algorithm.
     */
    public static class ClusterLabelSplitter implements LabelSplitter {
        private static final long serialVersionUID = 3545709733670034266L;
        private double[][] labelVectors = null;
        private Random r;

        public ClusterLabelSplitter(int seed) {
            r = new Random(seed);
        }

        @Override
        public Collection<Set<Integer>> splitLabels(int k, Collection<Integer> labels, Instances D) {
            int L = D.classIndex();
            int numD = D.numInstances();
            if (labelVectors == null) {
                labelVectors = new double[L][numD];
                for (int i = 0; i < numD; i++)
                    for (int l = 0; l < L; l++)
                        labelVectors[l][i] = D.get(i).value(l);
            }

            List<Integer> labelList = new ArrayList<>(labels);
            int idx = 0;
            int Ln = labels.size();

            if (Ln <= k) {
                List<Set<Integer>> result = new ArrayList<>(Ln);
                for (int i = 0; i < Ln; i++)
                    result.add(new HashSet<>(Arrays.asList(labelList.get(idx++))));
                return result;
            }

            List<List<Integer>> clusters = new ArrayList<>(k);
            List<Integer> centres = new ArrayList<>(k);
            Collections.shuffle(labelList, r);
            for (int i = 0; i < k; i++) {
                clusters.add(new ArrayList<>(L/k));
                centres.add(labelList.get(idx++));
            }

            double[][] d = new double[L][k];
            for (int it = 0; it < 2; it++) {
                for (Integer l : labels) {
                    for (int i = 0; i < k; i++) {
                        /* Calculate distance to cluster centres */
                        double[] vci = labelVectors[centres.get(i)];
                        double[] vl = labelVectors[l];
                        double sum = 0;
                        for (int j = 0; j < numD; j++)
                            sum += (vci[j] - vl[j])*(vci[j] - vl[j]);
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
                        Cj.sort(new Comparator<Integer>() {
                            @Override
                            public int compare(Integer o1, Integer o2) {
                                return Double.compare(d[o1][_minj], d[o2][_minj]);
                            }
                        });

                        int size = Cj.size();
                        if (size > Math.ceil(Ln/(double)k)) {
                            nu = Cj.remove(size-1);
                            d[nu][minj] = Double.MAX_VALUE;
                        } else {
                            finished = true;
                        }
                    }
                }
                for (int i = 0; i < k; i++) {
                    double[] cent = new double[numD];
                    for (Integer l : clusters.get(i)) {
                        /* Calculate distance to cluster centres */
                        double[] vl = labelVectors[l];
                        for (int j = 0; j < numD; j++)
                            cent[j] += vl[j];
                    }
                    for (int j = 0; j < numD; j++)
                        cent[j] /= clusters.get(i).size();
                }
            }
            List<Set<Integer>> clusterSets = new ArrayList<>(clusters.size());
            for (List<Integer> cluster : clusters)
                clusterSets.add(new HashSet<>(cluster));
            return clusterSets;
        }
    }

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
     * The threshold to use for the multi-label classifier distribution at each
     * node. 
     */
    protected double threshold = 0.3;

    /**
     * Root node of the HOMER tree.
     */
    protected HOMERNode root;

    /**
     * The label splitter.
     */
    protected LabelSplitter labelSplitter = new RandomLabelSplitter(0);

    /**
     * Create a new HOMER classifier with default settings.
     */
    public HOMER() {
        m_Classifier = new meka.classifiers.multilabel.BR();
    }

    @Override
    protected String defaultClassifierString() {
        return meka.classifiers.multilabel.BR.class.getName();
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
    public String globalInfo() {
        return "HOMER tree algorithm. For more information see:\n" + getTechnicalInformation().toString();
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String kTipText() {
        return "The number of partitions per level.";
    }

    @Override
    public void setSeed(int seed) {
        this.seed = seed;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    public String seedTipText() {
        return "The seed to set.";
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
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
            return labelSplitter.getClass().getName();
    }

    public LabelSplitter getLabelSplitter() {
        return labelSplitter;
    }

    public void setLabelSplitterString(String str) {
        if (str.equals("cluster"))
            labelSplitter = new ClusterLabelSplitter(getSeed());
        else
            labelSplitter = new RandomLabelSplitter(getSeed());
    }

    public void setLabelSplitter(LabelSplitter labelSplitter) {
        this.labelSplitter = labelSplitter;
    }

    public String labelSplitterTipText() {
        return "The label splitter class to use.";
    }

    /**
     * Build the HOMER tree.
     * 
     * @param node
     *              the root node of the tree/subtree
     * @throws Exception
     *              the {@link AbstractClassifier#makeCopy(weka.classifiers.Classifier)} fails
     */
    private void buildTree(HOMERNode node, Instances D) throws Exception {
        if (getDebug()) {
            System.out.print("Building subtree of node " + node.getId() + " with labels: ");
            for (Integer label : node.getLabels())
                System.out.print(label.toString() + ",");
            System.out.println();
        }

        List<HOMERNode> children = node.getChildren();
        children.clear();

        Collection<Set<Integer>> labelSplits = labelSplitter.splitLabels(k, node.getLabels(), D);
        for (Set<Integer> labelSet : labelSplits) {
            HOMERNode child = new HOMERNode();
            child.setClassifier((MultiLabelClassifier)AbstractClassifier.makeCopy(m_Classifier));
            child.setLabels(labelSet);
            children.add(child);
            if (labelSet.size() > 1) // Leaves don't need to recurse down any further
                buildTree(child, D);
        }
    }

    /**
     * Train the HOMER tree.
     * 
     * @param node
     *              the root node of the tree/subtree
     * @param D
     *              the instances
     * @throws Exception
     */
    private void trainTree(HOMERNode node, Instances D) throws Exception {
        int L = D.classIndex();
        List<HOMERNode> children = node.getChildren();
        int c = children.size();

        /* Generate meta-label attributes */
        Instances newInstances = F.removeLabels(D, L);
        for (int i = 0; i < c; i++) {
            Attribute att = new Attribute("MetaLabel" + i, Arrays.asList("0", "1"));
            newInstances.insertAttributeAt(att, i);
        }

        /* Set each meta-label value to the disjunction of instance label values */
        Iterator<Instance> it = newInstances.iterator();
        for (int i = 0; it.hasNext(); i++) {
            boolean remove = true;
            Instance next = it.next();
            for (int m = 0; m < c; m++) {
                next.setValue(m, "0");
                for (Integer l : children.get(m).getLabels()) {
                    if (D.get(i).stringValue(l).equals("1")) {
                        next.setValue(m, "1");
                        remove = false;
                        break;
                    }
                }
            }
            if (remove)
                it.remove();
        }
        node.setInstances(newInstances);
        node.buildClassifier();
        if (getDebug())
            System.out.println("Trained node " + node.getId() + " with " + newInstances.size() + " instances.");

        for (HOMERNode child : children) {
            if (child.getLabels().size() > 1)
                trainTree(child, D);
        }
    }

    @Override
    public void buildClassifier(Instances D) throws Exception {
        testCapabilities(D);

        if (!(m_Classifier instanceof MultiLabelClassifier))
            throw new IllegalStateException("Classifier must be a MultiLabelClassifier!");

        int L = D.classIndex();
        root = new HOMERNode();
        root.setClassifier((MultiLabelClassifier)ProblemTransformationMethod.makeCopy(m_Classifier));
        int[] labels = A.make_sequence(L);
        Set<Integer> labelSet = new HashSet<Integer>(L);
        for (int l : labels)
            labelSet.add(l);
        root.setLabels(labelSet);
        buildTree(root, D);
        trainTree(root, D);
        if (getDebug())
            System.out.println("Trained all nodes.");
    }

    /**
     * Classify an instance x.
     * 
     * @param x
     *              the instance to classify
     * @param node
     *              the current tree node
     * @param y
     *              shared array of prediction values
     * @return y
     * @throws Exception
     */
    private double[] classify(Instance x, HOMERNode node, double[] y) throws Exception {
        List<HOMERNode> children = node.getChildren();
        int L = children.size();
        Instance newX = (Instance)x.copy();
        int oldL = newX.classIndex();
        newX.setDataset(null);
        newX = MLUtils.deleteAttributesAt(newX, A.make_sequence(oldL));
        for (int i = 0; i < L; i++)
            newX.insertAttributeAt(i);
        newX.setDataset(node.getInstances());
        double[] metaDist = node.getClassifier().distributionForInstance(newX); // Distribution on meta-labels
        for (int i = 0; i < children.size(); i++) {
            HOMERNode child = children.get(i);
            if (metaDist[i] > threshold) {
                if (child.getLabels().size() == 1)
                    y[child.getLabels().iterator().next()] = 1;
                else
                    classify(newX, child, y);
            } else {
                for (int l : child.getLabels())
                    y[l] = 0;
            }
        }
        return y;
    }

    @Override
    public double[] distributionForInstance(Instance x) throws Exception {
        double[] y = new double[x.classIndex()];
        classify(x, root, y);
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
        threshold = OptionUtils.parse(options, "t", 0.3);
        setLabelSplitterString(OptionUtils.parse(options, "ls", "RandomLabelSplitter"));
    }

    public static void main(String[] args) {
        ProblemTransformationMethod.runClassifier(new HOMER(), args);
    }

}
