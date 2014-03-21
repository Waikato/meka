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

package meka.classifiers.multilabel;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import meka.classifiers.multilabel.*;
import meka.classifiers.multilabel.cc.CNode;
import meka.classifiers.multitarget.*;
import weka.core.*;
import meka.core.A;
import meka.core.M;
import meka.core.StatUtils;
import meka.core.Result;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.util.*;
import mst.*;
import java.io.Serializable;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

/**
 * BCC.java - Bayesian Classifier Chains.
 * Creates a maximum spanning tree based on marginal label dependence; then employs a CC classifier. The original paper used Naive Bayes as a base classifier, hence the name. 
 * <br>
 * See Zaragoza et al. "Bayesian Classifier Chains for Multi-dimensional Classification. IJCAI 2011.
 * </br>
 * @author	Jesse Read
 * @version June 2013
 */
public class BCC extends MultilabelClassifier implements Randomizable, TechnicalInformationHandler {

	protected CNode nodes[] = null;
	int sequence[] = null;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"Bayesian Classifier Chains (BCC).\n"
				+ "Creates a maximum spanning tree based on marginal label dependence. Then employs CC.\n"
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Julio H. Zaragoza et al.");
		result.setValue(Field.TITLE, "Bayesian Chain Classifiers for Multidimensional Classification");
		result.setValue(Field.BOOKTITLE, "IJCAI'11: International Joint Conference on Artificial Intelligence.");
		result.setValue(Field.YEAR, "2011");
		
		return result;
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {
		testCapabilities(D);

		int L = D.classIndex();
		int d = D.numAttributes()-L;

		/*
		 * Measure unconditional label dependencies (frequencies).
		 */
		if (getDebug())
			System.out.println("Get unconditional dependencies ...");
		double CD[][] = null;
		if (m_DependencyType > 0) {
			// New Option
			if (getDebug()) System.out.println("The 'LEAD' method for finding conditional dependence.");
			Instances D_r = new Instances(D);
			D_r.randomize(new Random(getSeed()));
			Instances D_train = new Instances(D_r,0,D_r.numInstances()*60/100);
			Instances D_test = new Instances(D_r,D_train.numInstances(),D_r.numInstances()-D_train.numInstances());
			BR br = new BR();
			br.setClassifier(getClassifier()); 
			Result r = Evaluation.evaluateModel((MultilabelClassifier)br,D_train,D_test,"PCut1","1"); 
			CD = StatUtils.LEAD2(D_test,r);
		}
		else {	
			// Old/default Option
			if (getDebug()) System.out.println("The Frequency method for finding marginal dependence.");
			CD = StatUtils.margDepMatrix(D,"I");
		}

		if (getDebug()) System.out.println(M.toString(CD));

		/*
		 * Make a fully connected graph, each edge represents the
		 * dependence measured between the pair of labels.
		 */
		CD = M.multiply(CD,-1); // because we want a *maximum* spanning tree
		if (getDebug())
			System.out.println("Make a graph ...");
		EdgeWeightedGraph G = new EdgeWeightedGraph((int)L);
		for(int i = 0; i < L; i++) {
			for(int j = i+1; j < L; j++) {
				Edge e = new Edge(i, j, CD[i][j]);
				G.addEdge(e);
			}
		}

		/*
		 * Run an off-the-shelf MST algorithm to get a MST.
		 */
		if (getDebug())
			System.out.println("Get an MST ...");
		KruskalMST mst = new KruskalMST(G);

		/*
		 * Define graph connections based on the MST.
		 */
		int paM[][] = new int[L][L];
		for (Edge e : mst.edges()) {
			int j = e.either();
			int k = e.other(j);
			paM[j][k] = 1;
			paM[k][j] = 1;
			//StdOut.println(e);
		}
		if (getDebug()) System.out.println(M.toString(paM));

		/*
		 *  Turn the DAG into a Tree with the m_Seed-th node as root
		 */
		int root = m_Seed;
		if (getDebug())
			System.out.println("Make a Tree from Root "+root);
		int paL[][] = new int[L][0];
		int visted[] = new int[L];
		Arrays.fill(visted,-1);
		visted[root] = 0;
		treeify(root,paM,paL, visted);
		if (getDebug()) {
			for(int i = 0; i < L; i++) {
				System.out.println("pa_"+i+" = "+Arrays.toString(paL[i]));
			}
		}
		sequence = Utils.sort(visted);
		if (getDebug())
			System.out.println("sequence: "+Arrays.toString(sequence));
	   /*
		* Bulid a classifier 'tree' based on the Tree
		*/
      if (getDebug()) System.out.println("Build Classifier Tree ...");
	   nodes = new CNode[L];
	   for(int j : sequence) {
		   if (getDebug()) 
				System.out.println("\t node h_"+j+" : P(y_"+j+" | x_[1:"+d+"], y_"+Arrays.toString(paL[j])+")");
		   nodes[j] = new CNode(j, null, paL[j]);
		   nodes[j].build(D, m_Classifier);
	   }

	   if (getDebug()) System.out.println(" * DONE * ");

	   /* 
		* Notes ...
		   paL[j] = new int[]{};            // <-- BR !!
		   paL[j] = MLUtils.gen_indices(j); // <-- CC !!
	   */
	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {
		int L = x.classIndex();
		double y[] = new double[L];

		for(int j : sequence) {
			// h_j : x,pa_j -> y_j
			y[j] = nodes[j].classify((Instance)x.copy(),y); 
		}

		return y;
	}

	/**
	 * Treeify - make a tree given the structure defined in paM[][], using the root-th node as root.
	 */
	public void treeify(int root, int paM[][], int paL[][], int visited[]) {
		int children[] = new int[]{};
		for(int j = 0; j < paM[root].length; j++) {
			if (paM[root][j] == 1) {
				if (visited[j] < 0) {
					children = A.add(children,j);
					paL[j] = A.add(paL[j],root);
					visited[j] = visited[Utils.maxIndex(visited)] + 1;
				}
				// set as visited
				//paM[root][j] = 0;
			}
		}
		// go through again
		for(int child : children) {
			treeify(child,paM,paL,visited);
		}
	}

	/**
	 * ToString - like M.toString() but for int[][]
	public static String toString(int M[][]) {
		StringBuilder sb = new StringBuilder();  
		for(int j = 0; j < M.length; j++) {
			for(int k = 0; k < M[j].length; k++) {
				sb.append(""+M[j][k]+" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	*/
	
	protected int m_Seed = 0;

	@Override
	public int getSeed() {
		return m_Seed;
	}

	@Override
	public void setSeed(int s) {
		m_Seed = s;
	}

	/* 
	 * TODO: Make a generic abstract -dependency_user- class that has this option, and extend it here
	 */
	
	int m_DependencyType = 0;

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tThe way to measure dependencies.\n\tdefault: "+m_DependencyType+" (frequencies only)", "X", 1, "-X <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		m_DependencyType = (Utils.getOptionPos('X',options) >= 0) ? Integer.parseInt(Utils.getOption('X', options)) : m_DependencyType;

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		String [] superOptions = super.getOptions();
		String [] options = new String [superOptions.length + 2];
		int current = 0;
		options[current++] = "-X";
		options[current++] = String.valueOf(m_DependencyType);
		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		return options;

	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new BCC(),args);
	}
}
