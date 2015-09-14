package meka.classifiers.multilabel;

import weka.classifiers.*;
import meka.classifiers.multilabel.*;
import meka.classifiers.multilabel.cc.CNode;
import meka.classifiers.multilabel.cc.Trellis;
import weka.core.*;
import meka.core.A;
import meka.core.F;
import meka.core.MLUtils;
import meka.core.CCUtils;
import meka.core.StatUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import java.util.*;
import java.io.Serializable;

/**
 * CT - Classifier Trellis. 
 * CC in a trellis structure (rather than a cascaded chain). You set the width and type/connectivity of the trellis, and optionally change the payoff function which guides the placement of nodes (labels) within the trellis.
 * @author	Jesse Read
 * @version April 2014
 */
public class CT extends MCC implements TechnicalInformationHandler {

	protected int m_Width = -1;
	protected int m_Connectivity = 1;

	// @TODO: use MCC's P instead
	protected String m_DependencyPayoff = "Ibf";

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

		if (m_Width < 0) {
			// If no width specified for the trellis, use sqrt(L)
			m_Width = (int)Math.sqrt(L);
			if (getDebug()) System.out.println("Setting width to "+m_Width);
		}

		/*
		 * Make the Trellis. Start with a random structure.
		 */
		if (getDebug())
			System.out.println("Make Trellis");

		int indices[] = A.make_sequence(L);

		A.shuffle(indices, m_R);

		trel = new Trellis(indices, m_Width, m_Connectivity);

		long start = System.currentTimeMillis();

		/*
		 * If specified, try and reorder the nodes in the trellis (i.e., get a superior structure)
		 */
		if (m_Is > 0) {
			double I[][] =  StatUtils.margDepMatrix(D,m_DependencyPayoff);

			/*
			 * Get dependency Matrix
			 */
			if (getDebug()) 
				System.out.println("Got "+m_DependencyPayoff+"-type Matrix in "+((System.currentTimeMillis() - start)/1000.0)+"s");

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

		Vector newVector = new Vector();
		newVector.addElement(new Option("\tThe width of the trellis.\n\tdefault: "+m_Width+" (sqrt[number of labels])", "H", 1, "-H <value>"));
		newVector.addElement(new Option("\tThe density/type of the trellis.\n\tdefault: "+m_Connectivity+"\n\trange: 0-3 (0=BR)", "L", 1, "-L <value>"));
		newVector.addElement(new Option("\tThe dependency payoff function.\n\tdefault: "+m_DependencyPayoff+"\n\t", "X", 1, "-X <value>"));

		Enumeration enu = super.listOptions();

		while (enu.hasMoreElements()) 
			newVector.addElement(enu.nextElement());

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		m_Width = (Utils.getOptionPos('H',options) >= 0) ? Integer.parseInt(Utils.getOption('H', options)) : m_Width;
		if (getDebug()) System.out.println("Width set as: "+m_Width);
		m_Connectivity = (Utils.getOptionPos('L',options) >= 0) ? Integer.parseInt(Utils.getOption('L', options)) : m_Connectivity;
		if (getDebug()) System.out.println("Trellis Type: "+m_Connectivity);
		m_DependencyPayoff = (Utils.getOptionPos('X',options) >= 0) ? Utils.getOption('X', options) : m_DependencyPayoff;
		if (getDebug()) System.out.println("Dependency Type: "+m_DependencyPayoff);

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
		result.add("-X");
	  	result.add("" + m_DependencyPayoff);
		return result.toArray(new String[result.size()]);
	}

	public static void main(String args[]) {
		MultilabelClassifier.evaluation(new CT(),args);
	}
}
