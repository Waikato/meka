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

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;

import meka.classifiers.multilabel.cc.CNode;
import meka.classifiers.multilabel.cc.Trellis;
import weka.core.Instance;
import weka.core.Instances;
import meka.core.A;
import meka.core.StatUtils;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.Utils;

/**
 * CDT.java - Conditional Dependency Trellis.
 * Like CDN, but with a trellis structure (like CT) rather than a fully connected network.
 * @see CDN
 * @see CT
 * @author 	Jesse Read
 * @version	January 2014
 */
public class CDT extends CDN {

	protected int m_Width = -1;
	protected int m_Connectivity = 1;
	protected String m_DependencyPayoff = "_";

	Trellis trel = null;

	protected CNode nodes[] = null;

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		int L = D.classIndex();
		int d = D.numAttributes()-L;
		u = new Random(getSeed());
		if (m_Width < 0)
			m_Width = (int)Math.sqrt(L);

		nodes = new CNode[L];
		/*
		 * Make the Trellis.
		 */
		if (getDebug())
			System.out.println("Make Trellis of width "+m_Width);
		int indices[] = A.make_sequence(L);
		A.shuffle(indices, new Random(getSeed()));
		trel = new Trellis(indices, m_Width, m_Connectivity);
		if (getDebug())
			System.out.println("==>\n"+trel.toString());

		/* NEW  - ORDER THE TRELLIS */
		if (!m_DependencyPayoff.equals("_"))
			trel = CT.orderTrellis(trel,StatUtils.margDepMatrix(D,m_DependencyPayoff),u);

		/*
		 * Build Trellis
		 */
		if (getDebug())
			System.out.println("Build Trellis");

		if (getDebug())
			System.out.println("nodes: "+Arrays.toString(trel.indices));

		for(int j = 0; j < L; j++) {
			int jv = trel.indices[j];
			if (getDebug()) {
				System.out.println("Build Node h_"+jv+"] : P(y_"+jv+" | x_[1:d], y_"+Arrays.toString(trel.getNeighbours(j))+")");
			}
			nodes[jv] = new CNode(jv, null, trel.getNeighbours(j));
			nodes[jv].build(D,m_Classifier);
		}

	}

	@Override
	public double[] distributionForInstance(Instance x) throws Exception {

		int L = x.classIndex();
		double y[] = new double[L];			// for sampling
		double y_marg[] = new double[L];	// for collectiing marginal

		int sequence[] = A.make_sequence(L);

		double likelihood[] = new double[L];

		for(int i = 0; i < I; i++) {
			Collections.shuffle(Arrays.asList(sequence));
			for(int j : sequence) {
				// sample
				y[j] = nodes[j].sample(x,y,u);
				// collect marginals
				if (i > (I - I_c)) {
					y_marg[j] += y[j];
				}
				// else still burning in
			}
		}
		// finish, calculate marginals
		for(int j = 0; j < L; j++) {
			y_marg[j] /= I_c;
		}

		return y_marg;
	}

	/* NOTE: these options in common with CT */

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tThe width of the trellis.\n\tdefault: "+m_Width, "H", 1, "-H <value>"));
		newVector.addElement(new Option("\tThe density/type of the trellis.\n\tdefault: "+m_Connectivity+"\n\trange: 0-3 (0=BR)", "L", 1, "-L <value>"));
		newVector.addElement(new Option("\tThe dependency payoff function.\n\tdefault: "+m_DependencyPayoff+"(random)\n\t", "X", 1, "-X <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		m_Width = (Utils.getOptionPos('H',options) >= 0) ? Integer.parseInt(Utils.getOption('H', options)) : m_Width;
		m_Connectivity = (Utils.getOptionPos('L',options) >= 0) ? Integer.parseInt(Utils.getOption('L', options)) : m_Connectivity;
		m_DependencyPayoff = (Utils.getOptionPos('X',options) >= 0) ? Utils.getOption('X', options) : m_DependencyPayoff;

		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {

		ArrayList<String> result;
	  	result = new ArrayList<String>(Arrays.asList(super.getOptions()));
	  	result.add("-H");
	  	result.add("" + m_Width);
		result.add("-L");
	  	result.add("" + m_Connectivity);
		result.add("-P");
	  	result.add("" + m_DependencyPayoff);
		return result.toArray(new String[result.size()]);
	}

	/** 
	 * GetI - Get the neighbourhood type (number of neighbours for each node).
	 */
	public int getType() {
		return m_Connectivity;
	}

	/** 
	 * SetI - Sets the neighbourhood type (number of neighbours for each node).
	 */
	public void setType(int c) {
		m_Connectivity = c;
	}

	/** 
	 * GetH - Get the trellis width.
	 */
	public int getWidth() {
		return m_Width;
	}

	/** 
	 * SetH - Sets the trellis width.
	 */
	public void setWidth(int h) {
		m_Width = h;
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new CDT(), args);
	}

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"A Conditional Dependency Trellis. Like CDN, but with a trellis structure (like CT) rather than a fully connected network."
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Yuhong Guoand and Suicheng Gu");
		result.setValue(Field.TITLE, "Multi-Label Classification Using Conditional Dependency Networks");
		result.setValue(Field.BOOKTITLE, "IJCAI '11");
		result.setValue(Field.YEAR, "2011");

		result.add(new CT().getTechnicalInformation());

		return result;
	}

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}
}

