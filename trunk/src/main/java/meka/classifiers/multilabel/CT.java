package meka.classifiers.multilabel;

import meka.classifiers.multilabel.cc.CNode;
import meka.classifiers.multilabel.cc.Trellis;
import meka.core.OptionUtils;
import weka.core.*;
import meka.core.A;
import meka.core.StatUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import java.util.*;

/**
 * CT - Classifier Trellis. 
 * CC in a trellis structure (rather than a cascaded chain). You set the width and type/connectivity/density of the trellis, and optionally change the dependency heuristic which guides the placement of nodes (labels) within the trellis.
 * @author	Jesse Read
 * @version September 2015
 */
public class CT extends MCC implements TechnicalInformationHandler {

	private static final long serialVersionUID = -5773951599734753129L;

	protected int m_Width = -1;
	protected int m_Density = 1;
	protected String m_DependencyMetric = "Ibf";

	Trellis trel = null;

	private String info = "";

	public String toString() {
		return info;
	}

	@Override
	public String globalInfo() {
		return "CC in a trellis structure (rather than a cascaded chain). You set the width and type/connectivity of the trellis, and optionally change the payoff function which guides the placement of nodes (labels) within the trellis.";
	}

	@Override
	public void buildClassifier(Instances D) throws Exception {

		int L = D.classIndex();
		int d = D.numAttributes()-L;
		m_R = new Random(getSeed());
		int width = m_Width;

		if (m_Width < 0) {
			// If no width specified for the trellis, use sqrt(L)
			width = (int)Math.sqrt(L);
			if (getDebug()) System.out.println("Setting width to "+width);
		}
		else if (m_Width == 0) {
			// 0-width is not possible, use it to indicate a width of L
			width = L;
			if (getDebug()) System.out.println("Setting width to "+width);
		}

		/*
		 * Make the Trellis. Start with a random structure.
		 */
		if (getDebug())
			System.out.println("Make Trellis");

		int indices[] = A.make_sequence(L);

		A.shuffle(indices, m_R);

		trel = new Trellis(indices, width, m_Density);

		long start = System.currentTimeMillis();

		/*
		 * If specified, try and reorder the nodes in the trellis (i.e., get a superior structure)
		 */
		if (m_Is > 0) {
			double I[][] =  StatUtils.margDepMatrix(D,m_DependencyMetric);

			/*
			 * Get dependency Matrix
			 */
			if (getDebug()) 
				System.out.println("Got "+m_DependencyMetric+"-type Matrix in "+((System.currentTimeMillis() - start)/1000.0)+"s");

			// ORDER THE TRELLIS ACCORDING TO THE DEPENDENCY MATRIX
			trel = orderTrellis(trel,I,m_R);
		}

		info = String.valueOf((System.currentTimeMillis() - start)/1000.0);

		if (getDebug()) System.out.println("\nTrellis built in: "+info+"s");

		/*
		 * Build Trellis
		 */
		if (getDebug())
			System.out.println("Build Trellis");

		nodes = new CNode[L];
		for(int jv : trel.indices) {
			if (getDebug()) {
				System.out.print(" -> "+jv);
				//System.out.println("Build Node h_"+jv+"] : P(y_"+jv+" | x_[1:d], y_"+Arrays.toString(trel.trellis[jv])+")");
			}
			nodes[jv] = new CNode(jv, null, trel.trellis[jv]);
			nodes[jv].build(D,m_Classifier);
		}
		if (getDebug()) 
			System.out.println();

		// So we can use the MCC.java and CC.java framework
		confidences = new double[L];
		m_Chain = trel.indices;
	}

	/**
	 * OrderTrellis - order the trellis according to marginal label dependencies.
	 * @param	trel	a randomly initialised trellis
	 * @param	I		a matrix of marginal pairwise dependencies
	 * @param	rand	a random seed
	 * @return	the modified trellis
	 * TODO: move to Trellis.java ?
	 */
	public static Trellis orderTrellis(Trellis trel, double I[][], Random rand) {

		int L = I.length;
		int Y[] = new int[L];

		/*
		 * Make list of indices
		 */
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i : trel.indices) {
			list.add(new Integer(i));
		}

		/*
		 * Take first index, and proceed
		 */
		Y[0] = list.remove(rand.nextInt(L)); 
		//if (getDebug()) 
		//	System.out.print(" "+String.format("%4d", Y[0]));
		// @todo: update(I,j_0) to make faster
		for(int j = 1; j < L; j++) {

		//	if (getDebug() && j % m_Width == 0) 
		//		System.out.println();

			double max_w = -1.;
			int j_ = -1;
			for(int j_prop : list) {
				double w = trel.weight(Y,j,j_prop,I);
				if (w >= max_w) {
					max_w = w;
					j_ = j_prop;
				}
			}
			list.remove(new Integer(j_));

		//	if (getDebug()) {
		//		System.out.print(" "+String.format("%4d", j_));
		//	}

			Y[j] = j_;
			// @todo: update(I,j_), because it will be a parent now
		}
		//if (getDebug())
		//	System.out.println();

		trel = new Trellis(Y, trel.WIDTH, trel.TYPE);
		return trel;
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
		return "Determines the neighbourhood density (the number of neighbours for each node in the trellis). Default = 1, BR = 0.";
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
	 * GetDependency - Get the type of depependency to use in rearranging the trellis
	 */
	public String getDependencyMetric() {
		return m_DependencyMetric;
	}

	/** 
	 * SetDependency - Sets the type of depependency to use in rearranging the trellis
	 */
	public void setDependencyMetric(String m) {
		m_DependencyMetric = m;
	}

	public String dependencyMetricTipText() {
		return "The dependency heuristic to use in rearranging the trellis (applicable if chain iterations > 0), default: Ibf (Mutual Information, fast binary version for multi-label data)";
	}


	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Jesse Read, Luca Martino, David Luengo, Pablo Olmos");
		result.setValue(Field.TITLE, "Scalable multi-output label prediction: From classifier chains to classifier trellises");
		result.setValue(Field.JOURNAL, "Pattern Recognition");
		result.setValue(Field.URL, "http://www.sciencedirect.com/science/article/pii/S0031320315000084");
		result.setValue(Field.YEAR, "2015");

		return result;
	}

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
		setDependencyMetric(OptionUtils.parse(options, 'X', "Ibf"));
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

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new CT(), args);
	}
}
