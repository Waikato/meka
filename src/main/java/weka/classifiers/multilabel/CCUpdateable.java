package weka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.multilabel.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;

/**
 * CCUpdateable.java - Updateable version of CC.
 *
 * The Classifier Chains Method Updateable (must be given an UpdateableClassifier base classifier)
 * @see CC.java
 * @author 		Jesse Read (jesse@tsc.uc3m.es)
 * @version 	September, 2011
 */
public class CCUpdateable extends CC implements UpdateableClassifier {

	@Override
	public String globalInfo() {
		return "Updateable CC\nMust be run with an Updateable base classifier.";
	}

	protected ULink root = null;

	protected class ULink {

		private ULink next = null;
		private AbstractClassifier classifier = null;
		public Instances _template = null;
		private int index = -1;
		private int value = -1;
		private int excld[]; // to contain the indices to delete
		private int j = 0;

		public ULink(int chain[], int j, Instances train) throws Exception {
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
			this.value = chain[j];
			int c_index = value; 
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
				next = new ULink(chain, ++j, train);
		}

		protected void update(Instance x) throws Exception {

			Instance x_ = (Instance)x.copy();
			x_.setDataset(null);

			// delete all except one (leaving a binary problem)
			// delete all the attributes (and track where our index ends up)
			int c_index = this.value;
			for(int i = excld.length-1; i >= 0; i--) {
				x_.deleteAttributeAt(excld[i]);
				if (excld[i] < this.index)
					c_index--; 
			}
			x_.setDataset(this._template);

			((UpdateableClassifier)this.classifier).updateClassifier(x_);

			if (next != null)
				next.update(x);
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

	public void buildClassifier(Instances D) throws Exception {
		int L = D.classIndex();

		int indices[] = getChain();
		if (indices == null) {
			indices = MLUtils.gen_indices(L);
			MLUtils.randomize(indices,new Random(m_S));
		}
		if(getDebug()) System.out.print(":- Chain (");
		root = new ULink(indices,0,D);
		if (getDebug()) System.out.println(" ) -:");
	}

	@Override
	public void updateClassifier(Instance x) throws Exception {
		if (root!=null)
			root.update(x);
		else
			throw new Exception("Train to update chain, but chain not build yet");
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		root.classify(x);
		return MLUtils.toDoubleArray(x,L);
	}

	public static void main(String args[]) {
		WindowIncrementalEvaluator.evaluation(new CCUpdateable(),args);
	}

}
