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

import meka.classifiers.multilabel.cc.CNode;
import meka.classifiers.multilabel.cc.Trellis;
import meka.core.A;
import meka.core.OptionUtils;
import meka.core.StatUtils;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.*;

/**
 * CDT.java - Conditional Dependency Trellis.
 * Like CDN, but with a trellis structure (like CT) rather than a fully connected network.
 * @see CDN
 * @see CT
 * @author 	Jesse Read
 * @version	January 2014
 */
public class CDT extends CDN {

	private static final long serialVersionUID = -1237783546336254364L;

	protected int m_Width = -1;
	protected int m_Density = 1;
	protected String m_DependencyMetric = "None";

	Trellis trel = null;

	protected CNode nodes[] = null;

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		int L = D.classIndex();
		int d = D.numAttributes()-L;
		m_R = new Random(getSeed());
		int width = m_Width;

		if (m_Width < 0)
			width = (int)Math.sqrt(L);
		else if (m_Width == 0) {
			width = L;
		}

		nodes = new CNode[L];
		/*
		 * Make the Trellis.
		 */
		if (getDebug())
			System.out.println("Make Trellis of width "+m_Width);
		int indices[] = A.make_sequence(L);
		A.shuffle(indices, new Random(getSeed()));
		trel = new Trellis(indices, width, m_Density);
		if (getDebug())
			System.out.println("==>\n"+trel.toString());

		/* Rearrange the Trellis */
		if (!m_DependencyMetric.equals("None"))
			trel = CT.orderTrellis(trel,StatUtils.margDepMatrix(D,m_DependencyMetric),m_R);

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
				y[j] = nodes[j].sample(x,y,m_R);
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
		Vector result = new Vector();
		result.addElement(new Option("\t"+widthTipText(), "H", 1, "-H <value>"));
		result.addElement(new Option("\t"+densityTipText(), "L", 1, "-L <value>"));
		result.addElement(new Option("\t"+dependencyMetricTipText(), "X", 1, "-X <value>"));
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setWidth(OptionUtils.parse(options, 'H', -1));
		setDensity(OptionUtils.parse(options, 'L', 1));
		setDependencyMetric(OptionUtils.parse(options, 'X', "None"));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'H', getWidth());
		OptionUtils.add(result, 'L', getDensity());
		OptionUtils.add(result, 'X', getDependencyMetric());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	/** 
	 * GetDensity - Get the neighbourhood density (number of neighbours for each node).
	 */
	public int getDensity() {
		return m_Density;
	}

	/** 
	 * SetDensity - Sets the neighbourhood density (number of neighbours for each node).
	 */
	public void setDensity(int c) {
		m_Density = c;
	}

	public String densityTipText() {
		return "Determines the neighbourhood density (the number of neighbours for each node in the trellis).";
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

	public String widthTipText() {
		return "Determines the width of the trellis (use 0 for chain; use -1 for a square trellis, i.e., width of sqrt(number of labels)).";
	}

	/** 
	 * GetDependency - Get the type of depependency to use in rearranging the trellis (None by default)
	 */
	public String getDependencyMetric() {
		return m_DependencyMetric;
	}

	/** 
	 * SetDependency - Sets the type of depependency to use in rearranging the trellis (None by default)
	 */
	public void setDependencyMetric(String m) {
		m_DependencyMetric = m;
	}

	public String dependencyMetricTipText() {
		return "The dependency heuristic to use in rearranging the trellis (None by default).";
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

