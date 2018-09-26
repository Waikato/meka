package meka.classifiers.multilabel;

import java.util.*;

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
     * 
     * @author Aaron Keesing
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

    private static final long serialVersionUID = 3358633077067198326L;
    
    protected int k = 5;
    protected int seed = 0;
    protected HOMERNode root;
    
    private Random r;
    
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
        return "HOMER tree algorithm.";
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
    
    /**
     * Build the HOMER tree.
     * 
     * @param node
     *              the root node of the tree/subtree
     * @throws Exception
     *              the {@link AbstractClassifier#makeCopy(weka.classifiers.Classifier)} fails
     */
    private void buildTree(HOMERNode node) throws Exception {
        if (getDebug()) {
            System.out.print("Building subtree of node " + node.getId() + " with labels: ");
            for (Integer label : node.getLabels())
                System.out.print(label.toString() + ",");
            System.out.println();
        }

        int L = node.getLabels().size();
        int[] labelArray = new int[L];
        int idx = 0;
        for (Integer l : node.getLabels())
            labelArray[idx++] = l;
        A.shuffle(labelArray, r);

        List<HOMERNode> children = node.getChildren();
        children.clear();
        idx = 0;

        if (L <= k) {
            /* All children are leaf nodes */
            for (int i = 0; i < L; i++) {
                HOMERNode child = new HOMERNode();
                child.setClassifier((MultiLabelClassifier)AbstractClassifier.makeCopy(m_Classifier));
                child.setLabels(new HashSet<>(Arrays.asList(labelArray[idx++])));
                children.add(child);
            }
        } else {
            for (int i = 0; i < k; i++) {
                HOMERNode child = new HOMERNode();
                child.setClassifier((MultiLabelClassifier)AbstractClassifier.makeCopy(m_Classifier));
                int[] childLabels = new int[i < L%k ? L/k+1 : L/k];
                for (int x = 0; x < childLabels.length; x++)
                    childLabels[x] = labelArray[idx++];
                Set<Integer> labelSet = new HashSet<>(childLabels.length);
                for (int l : childLabels)
                    labelSet.add(l);
                child.setLabels(labelSet);
                children.add(child);
                if (L/k > 1 || i < L%k) // Leaves don't need to recurse down any further
                    buildTree(child);
            }
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
        root.setClassifier((MultiLabelClassifier)AbstractClassifier.makeCopy(m_Classifier));
        int[] labels = A.make_sequence(L);
        Set<Integer> labelSet = new HashSet<Integer>(L);
        for (int l : labels)
            labelSet.add(l);
        root.setLabels(labelSet);
        r = new Random(getSeed());
        buildTree(root);
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
            if (metaDist[i] > 0.3) {
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
        OptionUtils.add(options, super.listOptions());
        return options.elements();
    }
    
    @Override
    public String[] getOptions() {
        List<String> result = new ArrayList<>();
        OptionUtils.add(result, 'k', k);
        OptionUtils.add(result, 'S', getSeed());
        OptionUtils.add(result, super.getOptions());
        return OptionUtils.toArray(result);
    }
    
    @Override
    public void setOptions(String[] options) throws Exception {
        super.setOptions(options);
        k = OptionUtils.parse(options, "k", 3);
        setSeed(OptionUtils.parse(options, "S", 0));
    }

    public static void main(String[] args) {
        ProblemTransformationMethod.runClassifier(new HOMER(), args);
    }

}
