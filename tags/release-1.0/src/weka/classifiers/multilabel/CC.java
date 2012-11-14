package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.multilabel.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;
import java.io.*;

/**
 * Classifier Chains method.
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. In Proc. of 20th European Conference on Machine Learning (ECML 2009). Bled, Slovenia, September 2009.
 * See: Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank. <i>Classifier Chains for Multi-label Classification</i>. Machine Learning Journal. Springer. 2011.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 * @version January 2009
 */
public class CC extends MultilabelClassifier implements Randomizable {

	protected Link2 root = null;

	protected class Link2 implements Serializable {

		private Link2 next = null;
		private AbstractClassifier classifier = null;
		public Instances _template = null;
		private int index = -1;
		private int excld[]; // to contain the indices to delete
		private int j = 0; //@temp

		public Link2(int chain[], int j, Instances train) throws Exception {
			this.j = j;

			this.index = chain[j];

			// sort out excludes [4|5,1,0,2,3]
			this.excld = Arrays.copyOfRange(chain,j+1,chain.length); 
			// sort out excludes [0,1,2,3,5]
			Arrays.sort(this.excld); 

			this.classifier = (AbstractClassifier)AbstractClassifier.forName(getClassifier().getClass().getName(),((AbstractClassifier)getClassifier()).getOptions());

			Instances new_train = new Instances(train);

			// delete all except one (leaving a binary problem)
			if(getDebug()) System.out.print(" "+this.index);
			new_train.setClassIndex(-1); 
			// delete all the attributes (and track where our index ends up)
			int c_index = chain[j]; 
			for(int i = excld.length-1; i >= 0; i--) {
				new_train.deleteAttributeAt(excld[i]);
				if (excld[i] < this.index)
					c_index--; 
			}
			new_train.setClassIndex(c_index); 

			_template = new Instances(new_train,0);

			this.classifier.buildClassifier(new_train);
			new_train = null;

			if(j+1 < chain.length) 
				next = new Link2(chain, ++j, train);
		}

		protected void classify(Instance test) throws Exception {
			// copy
			Instance copy = (Instance)test.copy();
			copy.setDataset(null);

			// delete attributes we don't need
			for(int i = excld.length-1; i >= 0; i--) {
				copy.deleteAttributeAt(this.excld[i]);
			}

			//set template
			copy.setDataset(this._template);

			//set class
			test.setValue(this.index,(int)(this.classifier.classifyInstance(copy))); 

			//carry on
			if (next!=null) next.classify(test);
		}

		public String toString() {
			return (next == null) ? String.valueOf(this.index) : String.valueOf(this.index)+">"+next.toString();
		}

	}

	protected int m_S = 0;

	public void setSeed(int s) {
		m_S = s;
	}

	public int getSeed() {
		return m_S;
	}

	protected int m_Chain[] = null;

	public void setChain(int chain[]) {
		m_Chain = chain;
	}

	public int[] getChain() {
		return m_Chain;
	}

	public void buildClassifier(Instances D) throws Exception {
		int L = D.classIndex();

		int indices[] = getChain();
		if (indices == null) {
			indices = MLUtils.gen_indices(L);
			MLUtils.randomize(indices,new Random(m_S));
		}
		if(getDebug()) System.out.print(":- Chain (");
		root = new Link2(indices,0,D);
		if (getDebug()) System.out.println(" ) -:");
	}

	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		root.classify(x);
		return MLUtils.toDoubleArray(x,L);
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CC(),args);
	}
}
