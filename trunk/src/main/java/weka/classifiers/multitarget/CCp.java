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

package weka.classifiers.multitarget;

import java.util.Arrays;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MLUtils;
import weka.core.Utils;

/**
 * CCp.java - Multitarget CC with probabilistic output.
 * <br>
 * This version includes probabilistic output in the distributionForInstance, like other MT methods.
 * <br>
 * i.e.: y[j+L] := P(y[j]|x) (this is usefull when used in an ensemble).
 * <br>
 * @see		CC
 * @version	March 2012
 * @author 	Jesse Read (jesse@tsc.uc3m.es)
 */

public class CCp extends weka.classifiers.multilabel.CC implements MultiTargetClassifier {

	/** for serialization. */
	private static final long serialVersionUID = 7139310187485439658L;
	
	protected weka.classifiers.multitarget.CCp.Link root = null;

	protected class Link {

		private weka.classifiers.multitarget.CCp.Link next = null;
		private AbstractClassifier classifier = null;
		public Instances _template = null;
		private int index = -1;
		private int excld[]; // to contain the indices to delete
		private int j = 0; //@temp

		public Link(int chain[], int j, Instances train) throws Exception {
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
				next = new weka.classifiers.multitarget.CCp.Link(chain, ++j, train);
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

			// round
			for(int k = 0; k < this.j; k++) {
				copy.setValue(j,Math.round(copy.value(k)));
			}

			//set class
			double dist[] = this.classifier.distributionForInstance(copy);
			int max_index = (int)Utils.maxIndex(dist);
			confidences[this.index] = dist[max_index];
			test.setValue(this.index,max_index);

			//carry on
			if (next!=null) next.classify(test);
		}

		@Override
		public String toString() {
			return (next == null) ? String.valueOf(this.index) : String.valueOf(this.index)+">"+next.toString();
		}

	}

	protected int m_S = 0;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"CC method with probabilistic output (CCp).\n"
				+ "This version includes probabilistic output in the distributionForInstance, like other MT methods.\n"
				+ "i.e.: y[j+L] := P(y[j]|x) (this is usefull when used in an ensemble).";
	}

	@Override
	public void setSeed(int s) {
		m_S = s;
	}

	@Override
	public int getSeed() {
		return m_S;
	}

	protected int m_Chain[] = null;

	@Override
	public void setChain(int chain[]) {
		m_Chain = chain;
	}

	@Override
	public int[] getChain() {
		return m_Chain;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		int L = D.classIndex();

		int indices[] = getChain();
		if (indices == null) {
			indices = MLUtils.gen_indices(L);
			MLUtils.randomize(indices,new Random(m_S));
		}
		if(getDebug()) System.out.print(":- Chain (");
		root = new weka.classifiers.multitarget.CCp.Link(indices,0,D);
		if (getDebug()) System.out.println(" ) -:");
	}

	protected double confidences[] = null;
	
	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		confidences = new double[L];
		root.classify(x);
		double y[] = new double[L*2];
		for(int j = 0; j < L; j++) {
			y[j] = x.value(j);
			y[j+L] = confidences[j]; // <--- this is the extra line
		}
		return y;
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CCp(),args);
	}
}
