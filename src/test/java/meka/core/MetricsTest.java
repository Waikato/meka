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

/*
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */

package meka.core;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

import weka.core.Utils;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;


/**
 * Tests the metrics. Run from the command line with:<p/>
 * java meka.core.TestMetrics
 *
 * @author Joerg Wicker (jw@joerg-wicker.org)
 * @version $Revision: 117 $
 */
public class MetricsTest 
    extends TestCase {

    private ArrayList<TestMetricObject> tmos;
  

    /**
     * Initializes the test.
     * 
     * @param name the name of the test
     */
    public MetricsTest(String name) { 
        super(name);  
    }



    protected void setUp(){
        tmos = new ArrayList<TestMetricObject>();
        // all correct

        
        double[][] pred = new double[][]{new double[]{1.0,0.0,1.0,0.0},
                                         new double[]{0.0,1.0,0.0,1.0},
                                         new double[]{1.0,0.0,1.0,0.0},
                                         new double[]{0.0,1.0,0.0,1.0}};

        int[][] predInt = new int[][]{new int[]{1,0,1,0},
                                      new int[]{0,1,0,1},
                                      new int[]{1,0,1,0},
                                      new int[]{0,1,0,1}};

        int[][] real = new int[][]{new int[]{1,0,1,0},
                                   new int[]{0,1,0,1},
                                   new int[]{1,0,1,0},
                                   new int[]{0,1,0,1}};
        
        
        TestMetricObject tmo = new TestMetricObject(pred,
                                                    predInt,
                                                    real,
                                                    1.0, // exactmatch
                                                    0.0, // zeroone
                                                    0.0, // l_hamming
                                                    1.0, // p_hamming
                                                    1.0, // p_harmonic
                                                    1.0, // p_accuracy
                                                    1.0, // p_jaccardind
                                                    0.0, // l_jaccarddist
                                                    0.0, // l_loglossl
                                                    0.0, // l_loglossd
                                                    1.0, // precision_mac
                                                    1.0, // recall_mac
                                                    1.0, // precision_mic
                                                    1.0, // recall_mic
                                                    1.0, // fmicro
                                                    1.0, // fmicrol
                                                    1.0, // fmicrod
                                                    0.0, // oneerror
                                                    1.0, // avgprec 
                                                    0.0, // rankloss
                                                    1.0, // macroauprc
                                                    1.0, // macroauroc
                                                    1.0, // microauprc
                                                    1.0, // microauroc
                                                    0.0); // levenshtein
        tmos.add(tmo);


        // all wrong

        
        pred = new double[][]{new double[]{1.0,0.0,1.0},
                              new double[]{0.0,1.0,0.0},
                              new double[]{1.0,0.0,1.0},
                              new double[]{0.0,1.0,0.0}};
        
        predInt = new int[][]{new int[]{1,0,1},
                              new int[]{0,1,0},
                              new int[]{1,0,1},
                              new int[]{0,1,0}};
        
        real = new int[][]{new int[]{0,1,0},
                           new int[]{1,0,1},
                           new int[]{0,1,0},
                           new int[]{1,0,1}};
        
        
        tmo = new TestMetricObject(pred,
                                   predInt,
                                   real,
                                   0.0, // exactmatch
                                   1.0, // zeroone
                                   1.0, // l_hamming
                                   0.0, // p_hamming
                                   0.0, // p_harmonic
                                   0.0, // p_accuracy
                                   0.0, // p_jaccardind
                                   1.0, // l_jaccarddist
                                   Math.log(3.0), // l_loglossl
                                   Math.log(4.0), // l_loglossd
                                   0.0, // precision_mac
                                   0.0, // recall_mac
                                   0.0, // precision_mic
                                   0.0, // recall_mic
                                   0.0, // fmicro
                                   0.0, // fmicrol
                                   0.0, // fmicrod
                                   1.0, // oneerror
                                   0.5, // avgprec 
                                   1.0, // rankloss
                                   0.5, // macroauprc
                                   0.0, // macroauroc
                                   0.5, // microauprc
                                   0.0, // microauroc
                                   2.0/3.0); // levenshtein
        tmos.add(tmo);



        
        // always exactly 50% right

        pred = new double[][]{new double[]{1.0,0.0,1.0,0.0},
                              new double[]{0.0,1.0,0.0,1.0},
                              new double[]{1.0,0.0,1.0,0.0},
                              new double[]{0.0,1.0,0.0,1.0}};
        
        predInt = new int[][]{new int[]{1,0,1,0},
                              new int[]{0,1,0,1},
                              new int[]{1,0,1,0},
                              new int[]{0,1,0,1}};
        
        real = new int[][]{new int[]{0,1,0,1},
                           new int[]{0,1,0,1},
                           new int[]{1,0,1,0},
                           new int[]{1,0,1,0}};
        
        
        tmo = new TestMetricObject(pred,
                                   predInt,
                                   real,
                                   0.5, // exactmatch
                                   0.5, // zeroone
                                   0.5, // l_hamming
                                   0.5, // p_hamming
                                   0.5, // p_harmonic
                                   0.5, // p_accuracy
                                   0.5, // p_jaccardind
                                   0.5, // l_jaccarddist
                                   Math.log(4.0)/2.0, // l_loglossl
                                   Math.log(4.0)/2.0, // l_loglossd
                                   0.5, // precision_mac
                                   0.5, // recall_mac
                                   0.5, // precision_mic
                                   0.5, // recall_mic
                                   0.5, // fmicro
                                   0.5, // fmicrol
                                   0.5, // fmicrod
                                   0.5, // oneerror
                                   0.75, // avgprec 
                                   0.5, // rankloss
                                   0.5, // macroauprc
                                   0.5, // macroauroc
                                   0.5, // microauprc
                                   0.5, // microauroc
                                   0.25); // levenshtein
        tmos.add(tmo);


        
        
        // all correct w missing 50%

        pred = new double[][]{new double[]{1.0,0.0,1.0,0.0},
                              new double[]{0.0,1.0,0.0,1.0},
                              new double[]{1.0,0.0,1.0,0.0},
                              new double[]{0.0,1.0,0.0,1.0}};

        predInt = new int[][]{new int[]{1,0,1,0},
                              new int[]{0,1,0,1},
                              new int[]{1,0,1,0},
                              new int[]{0,1,0,1}};

        real = new int[][]{new int[]{-1,0,1,-1},
                           new int[]{0,-1,-1,1},
                           new int[]{1,-1,-1,0},
                           new int[]{-1,1,0,-1}};
        
        
        tmo = new TestMetricObject(pred,
                                   predInt,
                                   real,
                                   1.0, // exactmatch
                                   0.0, // zeroone
                                   0.0, // l_hamming
                                   1.0, // p_hamming
                                   1.0, // p_harmonic
                                   1.0, // p_accuracy
                                   1.0, // p_jaccardind
                                   0.0, // l_jaccarddist
                                   0.0, // l_loglossl
                                   0.0, // l_loglossd
                                   1.0, // precision_mac
                                   1.0, // recall_mac
                                   1.0, // precision_mic
                                   1.0, // recall_mic
                                   1.0, // fmicro
                                   1.0, // fmicrol
                                   1.0, // fmicrod
                                   0.0, // oneerror
                                   1.0, // avgprec 
                                   0.0, // rankloss
                                   1.0, // macroauprc
                                   1.0, // macroauroc
                                   1.0, // microauprc
                                   1.0, // microauroc
                                   0.0); // levenshtein
        tmos.add(tmo);



        
        // all wrong w missing 50%

        // always exactly 50% right w missing 50%


        // all correct w missing 50%, one label all missing

        // all wrong w missing 50%, one label all missing

        // always exactly 50% right w missing 50%, one label all missing



        // all correct, but one label all wrong

        // all wrong, but one label all right

        // always exactly 50% right, but one label all wrong

        // always exactly 50% right, but one label all right

        

        // all correct w missing 25%

        // all wrong w missing 25%

        // always exactly 50% right w missing 25%


        // all correct w missing 75%

        // all wrong w missing 75%

        // always exactly 50% right w missing 75%


        // missing 100%

        pred = new double[][]{new double[]{1.0,0.0,1.0},
                              new double[]{0.0,1.0,0.0},
                              new double[]{1.0,0.0,1.0},
                              new double[]{0.0,1.0,0.0}};

        predInt = new int[][]{new int[]{1,0,1},
                              new int[]{0,1,0},
                              new int[]{1,0,1},
                              new int[]{0,1,0}};

        real = new int[][]{new int[]{-1,-1,-1},
                           new int[]{-1,-1,-1},
                           new int[]{-1,-1,-1},
                           new int[]{-1,-1,-1}};
        
        
        tmo = new TestMetricObject(pred,
                                   predInt,
                                   real,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN,
                                   Double.NaN);
        tmos.add(tmo);
        
    }

    /**
     * Tests avarage precision for one row
     */
    public void testP_AveragePrecision(){
        // confidences: [0.1,0.6,0.3,0.4,0.2,0.5]
        // real ys:     [0,1,0,1,0,1]
        // rank =       [  6,  1,  4,  3,  5,  2]
        // relevant lambdas: [1,3,5]
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.6
        // rank is = 1
        // fraction is = 1.0
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.4
        // rank is = 3
        // fraction is = 1.0
        // compute fraction for lambda = 5
        // Corresponding confidence was = 0.5
        // rank is = 2
        // fraction is = 1.0

        assertEquals(1.0000000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,1,0,1,0,1},
                                                new double[]{0.1,0.6,0.3,0.4,0.2,0.5}
                                                ), 0.00001
                     );
      
        // confidences: [1.0,1.0,1.0]
        // real ys:     [1,1,1]
        // rank =       [  3,  3,  3]
        // relevant lambdas: [0,1,2]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 1.0
        // rank is = 3
        // fraction is = 1.0
        // compute fraction for lambda = 1
        // Corresponding confidence was = 1.0
        // rank is = 3
        // fraction is = 1.0
        // compute fraction for lambda = 2
        // Corresponding confidence was = 1.0
        // rank is = 3
        // fraction is = 1.0

        assertEquals(1.0000000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,1},
                                                new double[]{1.0,1.0,1.0}
                                                ), 0.00001
                     );
      

        assertEquals(1.0000000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,0,0},
                                                new double[]{0.3,0.2,0.4}
                                                ), 0.00001
                     );
      
        // confidences: [0.1,0.2,0.3]
        // real ys:     [1,1,1]
        // rank =       [  3,  2,  1]
        // relevant lambdas: [0,1,2]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.1
        // rank is = 3
        // fraction is = 1.0
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.2
        // rank is = 2
        // fraction is = 1.0
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.3
        // rank is = 1
        // fraction is = 1.0

        assertEquals(1.0000000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,1},
                                                new double[]{0.1,0.2,0.3}
                                                ), 0.00001
                     );
      
        // confidences: [0.3,0.1,0.2]
        // real ys:     [1,1,1]
        // rank =       [  1,  3,  2]
        // relevant lambdas: [0,1,2]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.3
        // rank is = 1
        // fraction is = 1.0
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.1
        // rank is = 3
        // fraction is = 1.0
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.2
        // rank is = 2
        // fraction is = 1.0

        assertEquals(1.0000000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,1},
                                                new double[]{0.3,0.1,0.2}
                                                ), 0.00001
                     );
      
        // confidences: [0.1,0.9,0.2]
        // real ys:     [0,1,0]
        // rank =       [  3,  1,  2]
        // relevant lambdas: [1]
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.9
        // rank is = 1
        // fraction is = 1.0

        assertEquals(1.0000000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,1,0},
                                                new double[]{0.1,0.9,0.2}
                                                ), 0.00001
                     );
      
        // confidences: [0.1,0.7,0.9]
        // real ys:     [0,1,1]
        // rank =       [  3,  2,  1]
        // relevant lambdas: [1,2]
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.7
        // rank is = 2
        // fraction is = 1.0
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.9
        // rank is = 1
        // fraction is = 1.0

        assertEquals(1.0000000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,1,1},
                                                new double[]{0.1,0.7,0.9}
                                                ), 0.00001
                     );
      
        // confidences: [0.8,0.7,0.9]
        // real ys:     [0,1,1]
        // rank =       [  2,  3,  1]
        // relevant lambdas: [1,2]
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.7
        // rank is = 3
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.9
        // rank is = 1
        // fraction is = 1.0

        assertEquals(0.8333333333, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,1,1},
                                                new double[]{0.8,0.7,0.9}
                                                ), 0.00001
                     );
      
        // confidences: [0.0,0.0,0.0,0.0]
        // real ys:     [1,1,1,1]
        // rank =       [  4,  4,  4,  4]
        // relevant lambdas: [0,1,2,3]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.0
        // rank is = 4
        // fraction is = 1.0
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.0
        // rank is = 4
        // fraction is = 1.0
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.0
        // rank is = 4
        // fraction is = 1.0
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.0
        // rank is = 4
        // fraction is = 1.0

        assertEquals(1.0000000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,1,1},
                                                new double[]{0.0,0.0,0.0,0.0}
                                                ), 0.00001
                     );
      
        // confidences: [0.5975452777972018,0.3332183994766498,0.3851891847407185,0.984841540199809,0.8791825178724801,0.9412491794821144,0.27495396603548483,0.12889715087377673]
        // real ys:     [1,1,0,1,1,0,1,0]
        // rank =       [  4,  6,  5,  1,  3,  2,  7,  8]
        // relevant lambdas: [0,1,3,4,6]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.5975452777972018
        // rank is = 4
        // fraction is = 0.75
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.3332183994766498
        // rank is = 6
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.984841540199809
        // rank is = 1
        // fraction is = 1.0
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.8791825178724801
        // rank is = 3
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.27495396603548483
        // rank is = 7
        // fraction is = 0.7142857142857143

        assertEquals(0.7595238095, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,0,1,1,0,1,0},
                                                new double[]{0.5975452777972018,0.3332183994766498,0.3851891847407185,0.984841540199809,0.8791825178724801,0.9412491794821144,0.27495396603548483,0.12889715087377673}
                                                ), 0.00001
                     );
      
        // confidences: [0.10449068625097169,0.6251463634655593,0.4107961954910617,0.7763122912749325,0.990722785714783,0.4872328470301428,0.7462414053223305,0.7331520701949938]
        // real ys:     [0,1,0,0,1,0,1,1]
        // rank =       [  8,  5,  7,  2,  1,  6,  3,  4]
        // relevant lambdas: [1,4,6,7]
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.6251463634655593
        // rank is = 5
        // fraction is = 0.8
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.990722785714783
        // rank is = 1
        // fraction is = 1.0
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.7462414053223305
        // rank is = 3
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.7331520701949938
        // rank is = 4
        // fraction is = 0.75

        assertEquals(0.8041666667, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,1,0,0,1,0,1,1},
                                                new double[]{0.10449068625097169,0.6251463634655593,0.4107961954910617,0.7763122912749325,0.990722785714783,0.4872328470301428,0.7462414053223305,0.7331520701949938}
                                                ), 0.00001
                     );
      
        // confidences: [0.13393984058689223,0.0830623982249149,0.9785743401478403,0.7223571191888487,0.7150310138504744,0.14322038530059678,0.4629578184224229,0.004485602182885184]
        // real ys:     [1,0,1,0,1,1,1,1]
        // rank =       [  6,  7,  1,  2,  3,  5,  4,  8]
        // relevant lambdas: [0,2,4,5,6,7]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.13393984058689223
        // rank is = 6
        // fraction is = 0.8333333333333334
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.9785743401478403
        // rank is = 1
        // fraction is = 1.0
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.7150310138504744
        // rank is = 3
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 5
        // Corresponding confidence was = 0.14322038530059678
        // rank is = 5
        // fraction is = 0.8
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.4629578184224229
        // rank is = 4
        // fraction is = 0.75
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.004485602182885184
        // rank is = 8
        // fraction is = 0.75

        assertEquals(0.8000000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,0,1,0,1,1,1,1},
                                                new double[]{0.13393984058689223,0.0830623982249149,0.9785743401478403,0.7223571191888487,0.7150310138504744,0.14322038530059678,0.4629578184224229,0.004485602182885184}
                                                ), 0.00001
                     );
      
        // confidences: [0.9715469888517128,0.8657458802140383,0.6125811047098682,0.17898798452881726,0.21757041220968598,0.8544871670422907,0.009673497300974332,0.6922930069529333]
        // real ys:     [0,1,0,1,0,1,1,1]
        // rank =       [  1,  2,  5,  7,  6,  3,  8,  4]
        // relevant lambdas: [1,3,5,6,7]
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.8657458802140383
        // rank is = 2
        // fraction is = 0.5
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.17898798452881726
        // rank is = 7
        // fraction is = 0.5714285714285714
        // compute fraction for lambda = 5
        // Corresponding confidence was = 0.8544871670422907
        // rank is = 3
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.009673497300974332
        // rank is = 8
        // fraction is = 0.625
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.6922930069529333
        // rank is = 4
        // fraction is = 0.75

        assertEquals(0.6226190476, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,1,0,1,0,1,1,1},
                                                new double[]{0.9715469888517128,0.8657458802140383,0.6125811047098682,0.17898798452881726,0.21757041220968598,0.8544871670422907,0.009673497300974332,0.6922930069529333}
                                                ), 0.00001
                     );
      
        // confidences: [0.945333238959629,0.014236355103667941,0.3942035527773311,0.8537907753080728,0.7860424508145526,0.993471955005814,0.883104405981479,0.17029153024770394]
        // real ys:     [1,1,1,1,0,0,1,0]
        // rank =       [  2,  8,  6,  4,  5,  1,  3,  7]
        // relevant lambdas: [0,1,2,3,6]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.945333238959629
        // rank is = 2
        // fraction is = 0.5
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.014236355103667941
        // rank is = 8
        // fraction is = 0.625
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.3942035527773311
        // rank is = 6
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.8537907753080728
        // rank is = 4
        // fraction is = 0.75
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.883104405981479
        // rank is = 3
        // fraction is = 0.6666666666666666

        assertEquals(0.6416666667, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,1,1,0,0,1,0},
                                                new double[]{0.945333238959629,0.014236355103667941,0.3942035527773311,0.8537907753080728,0.7860424508145526,0.993471955005814,0.883104405981479,0.17029153024770394}
                                                ), 0.00001
                     );
      
        // confidences: [0.44142677367579175,0.46208799028599445,0.8528274665994607,0.501834850205735,0.9919429804102169,0.9692699099404161,0.35310607217911816,0.047265869196129406]
        // real ys:     [1,0,1,1,1,0,1,1]
        // rank =       [  6,  5,  3,  4,  1,  2,  7,  8]
        // relevant lambdas: [0,2,3,4,6,7]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.44142677367579175
        // rank is = 6
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.8528274665994607
        // rank is = 3
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.501834850205735
        // rank is = 4
        // fraction is = 0.75
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.9919429804102169
        // rank is = 1
        // fraction is = 1.0
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.35310607217911816
        // rank is = 7
        // fraction is = 0.7142857142857143
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.047265869196129406
        // rank is = 8
        // fraction is = 0.75

        assertEquals(0.7579365079, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,0,1,1,1,0,1,1},
                                                new double[]{0.44142677367579175,0.46208799028599445,0.8528274665994607,0.501834850205735,0.9919429804102169,0.9692699099404161,0.35310607217911816,0.047265869196129406}
                                                ), 0.00001
                     );
      
        // confidences: [0.9891171507514055,0.7674421030154899,0.5013973510122299,0.2555253108964435,0.30915818724818767,0.8482805002723425,0.052084538173983286,0.010175454536229256]
        // real ys:     [0,0,0,0,0,0,1,0]
        // rank =       [  1,  3,  4,  6,  5,  2,  7,  8]
        // relevant lambdas: [6]
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.052084538173983286
        // rank is = 7
        // fraction is = 0.14285714285714285

        assertEquals(0.1428571429, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,0,0,0,0,0,1,0},
                                                new double[]{0.9891171507514055,0.7674421030154899,0.5013973510122299,0.2555253108964435,0.30915818724818767,0.8482805002723425,0.052084538173983286,0.010175454536229256}
                                                ), 0.00001
                     );
      
        // confidences: [0.3078931676344727,0.5316085562487977,0.9188142018385732,0.27721002606871137,0.8742622102831944,0.6098815135127635,0.9086392096967358,0.04449062015679506]
        // real ys:     [0,1,0,1,1,1,0,1]
        // rank =       [  6,  5,  1,  7,  3,  4,  2,  8]
        // relevant lambdas: [1,3,4,5,7]
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.5316085562487977
        // rank is = 5
        // fraction is = 0.6
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.27721002606871137
        // rank is = 7
        // fraction is = 0.5714285714285714
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.8742622102831944
        // rank is = 3
        // fraction is = 0.3333333333333333
        // compute fraction for lambda = 5
        // Corresponding confidence was = 0.6098815135127635
        // rank is = 4
        // fraction is = 0.5
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.04449062015679506
        // rank is = 8
        // fraction is = 0.625

        assertEquals(0.5259523810, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,1,0,1,1,1,0,1},
                                                new double[]{0.3078931676344727,0.5316085562487977,0.9188142018385732,0.27721002606871137,0.8742622102831944,0.6098815135127635,0.9086392096967358,0.04449062015679506}
                                                ), 0.00001
                     );
      
        // confidences: [0.36636074451399603,0.47763691175692136,0.7039697053426346,0.3227677982432213,0.011654838276547785,0.7010389381824046,0.7453528603915509,0.6072882485626178]
        // real ys:     [1,0,0,0,1,0,1,0]
        // rank =       [  6,  5,  2,  7,  8,  3,  1,  4]
        // relevant lambdas: [0,4,6]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.36636074451399603
        // rank is = 6
        // fraction is = 0.3333333333333333
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.011654838276547785
        // rank is = 8
        // fraction is = 0.375
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.7453528603915509
        // rank is = 1
        // fraction is = 1.0

        assertEquals(0.5694444444, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,0,0,0,1,0,1,0},
                                                new double[]{0.36636074451399603,0.47763691175692136,0.7039697053426346,0.3227677982432213,0.011654838276547785,0.7010389381824046,0.7453528603915509,0.6072882485626178}
                                                ), 0.00001
                     );
      
        // confidences: [0.003913530617220884,0.26489582745247164,0.8928169571561851,0.2975682649107815,0.4289044793297375,0.6875516381838638,0.4084964347010259,0.3530524994432197]
        // real ys:     [0,1,0,0,1,0,0,1]
        // rank =       [  8,  7,  1,  6,  3,  2,  4,  5]
        // relevant lambdas: [1,4,7]
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.26489582745247164
        // rank is = 7
        // fraction is = 0.42857142857142855
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.4289044793297375
        // rank is = 3
        // fraction is = 0.3333333333333333
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.3530524994432197
        // rank is = 5
        // fraction is = 0.4

        assertEquals(0.3873015873, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,1,0,0,1,0,0,1},
                                                new double[]{0.003913530617220884,0.26489582745247164,0.8928169571561851,0.2975682649107815,0.4289044793297375,0.6875516381838638,0.4084964347010259,0.3530524994432197}
                                                ), 0.00001
                     );
      
    
        // confidences: [0.9,0.4,0.7,0.7,0.3,0.2,0.5,0.4]
        // real ys:     [1,1,0,1,1,0,1,0]
        // rank =       [  1,  6,  3,  3,  7,  8,  4,  6]
        // relevant lambdas: [0,1,3,4,6]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.9
        // rank is = 1
        // fraction is = 1.0
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.4
        // rank is = 6
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.7
        // rank is = 3
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.3
        // rank is = 7
        // fraction is = 0.7142857142857143
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.5
        // rank is = 4
        // fraction is = 0.75

        assertEquals(0.7595238095, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,0,1,1,0,1,0},
                                                new double[]{0.9,0.4,0.7,0.7,0.3,0.2,0.5,0.4}
                                                ), 0.00001
                     );
      
        // confidences: [0.2,0.0,0.3,0.2,0.2,0.3,0.5,0.5]
        // real ys:     [1,0,1,0,0,0,0,0]
        // rank =       [  7,  8,  4,  7,  7,  4,  2,  2]
        // relevant lambdas: [0,2]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.2
        // rank is = 7
        // fraction is = 0.2857142857142857
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.3
        // rank is = 4
        // fraction is = 0.25

        assertEquals(0.2678571429, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,0,1,0,0,0,0,0},
                                                new double[]{0.2,0.0,0.3,0.2,0.2,0.3,0.5,0.5}
                                                ), 0.00001
                     );
      
        // confidences: [0.0,0.5,0.5,0.0,0.8,0.1,0.4,0.6]
        // real ys:     [0,0,1,0,0,0,1,1]
        // rank =       [  8,  4,  4,  8,  1,  6,  5,  2]
        // relevant lambdas: [2,6,7]
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.5
        // rank is = 4
        // fraction is = 0.5
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.4
        // rank is = 5
        // fraction is = 0.6
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.6
        // rank is = 2
        // fraction is = 0.5

        assertEquals(0.5333333333, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,0,1,0,0,0,1,1},
                                                new double[]{0.0,0.5,0.5,0.0,0.8,0.1,0.4,0.6}
                                                ), 0.00001
                     );
      
        // confidences: [0.8,0.7,0.3,0.7,0.7,0.8,0.5,0.8]
        // real ys:     [1,0,1,0,1,1,1,1]
        // rank =       [  3,  6,  8,  6,  6,  3,  7,  3]
        // relevant lambdas: [0,2,4,5,6,7]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.8
        // rank is = 3
        // fraction is = 1.0
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.3
        // rank is = 8
        // fraction is = 0.75
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.7
        // rank is = 6
        // fraction is = 0.6666666666666666
        // compute fraction for lambda = 5
        // Corresponding confidence was = 0.8
        // rank is = 3
        // fraction is = 1.0
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.5
        // rank is = 7
        // fraction is = 0.7142857142857143
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.8
        // rank is = 3
        // fraction is = 1.0

        assertEquals(0.8551587302, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,0,1,0,1,1,1,1},
                                                new double[]{0.8,0.7,0.3,0.7,0.7,0.8,0.5,0.8}
                                                ), 0.00001
                     );
      
        // confidences: [0.7,0.1,0.6,0.0,0.7,0.2,0.5,0.0]
        // real ys:     [1,0,0,0,0,0,0,1]
        // rank =       [  2,  6,  3,  8,  2,  5,  4,  8]
        // relevant lambdas: [0,7]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.7
        // rank is = 2
        // fraction is = 0.5
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.0
        // rank is = 8
        // fraction is = 0.25

        assertEquals(0.3750000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,0,0,0,0,0,0,1},
                                                new double[]{0.7,0.1,0.6,0.0,0.7,0.2,0.5,0.0}
                                                ), 0.00001
                     );
      
        // confidences: [0.1,0.1,0.3,0.0,0.8,0.4,0.6,0.3]
        // real ys:     [1,1,1,1,1,0,0,0]
        // rank =       [  7,  7,  5,  8,  1,  3,  2,  5]
        // relevant lambdas: [0,1,2,3,4]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.1
        // rank is = 7
        // fraction is = 0.5714285714285714
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.1
        // rank is = 7
        // fraction is = 0.5714285714285714
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.3
        // rank is = 5
        // fraction is = 0.4
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.0
        // rank is = 8
        // fraction is = 0.625
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.8
        // rank is = 1
        // fraction is = 1.0

        assertEquals(0.6335714286, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,1,1,1,0,0,0},
                                                new double[]{0.1,0.1,0.3,0.0,0.8,0.4,0.6,0.3}
                                                ), 0.00001
                     );
      
        // confidences: [0.7,0.7,0.1,0.7,0.8,0.3,0.0,0.5]
        // real ys:     [1,1,1,1,0,0,1,0]
        // rank =       [  4,  4,  7,  4,  1,  6,  8,  5]
        // relevant lambdas: [0,1,2,3,6]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.7
        // rank is = 4
        // fraction is = 0.75
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.7
        // rank is = 4
        // fraction is = 0.75
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.1
        // rank is = 7
        // fraction is = 0.5714285714285714
        // compute fraction for lambda = 3
        // Corresponding confidence was = 0.7
        // rank is = 4
        // fraction is = 0.75
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.0
        // rank is = 8
        // fraction is = 0.625

        assertEquals(0.6892857143, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,1,1,0,0,1,0},
                                                new double[]{0.7,0.7,0.1,0.7,0.8,0.3,0.0,0.5}
                                                ), 0.00001
                     );
      
        // confidences: [0.1,0.4,0.0,0.5,0.6,0.4,0.4,0.2]
        // real ys:     [1,1,1,0,1,0,0,0]
        // rank =       [  7,  5,  8,  2,  1,  5,  5,  6]
        // relevant lambdas: [0,1,2,4]
        // compute fraction for lambda = 0
        // Corresponding confidence was = 0.1
        // rank is = 7
        // fraction is = 0.42857142857142855
        // compute fraction for lambda = 1
        // Corresponding confidence was = 0.4
        // rank is = 5
        // fraction is = 0.4
        // compute fraction for lambda = 2
        // Corresponding confidence was = 0.0
        // rank is = 8
        // fraction is = 0.5
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.6
        // rank is = 1
        // fraction is = 1.0

        assertEquals(0.5821428571, 
                     Metrics.P_AveragePrecision(
                                                new int[]{1,1,1,0,1,0,0,0},
                                                new double[]{0.1,0.4,0.0,0.5,0.6,0.4,0.4,0.2}
                                                ), 0.00001
                     );
      
        // confidences: [0.5,0.2,0.4,0.9,0.7,0.1,0.1,0.3]
        // real ys:     [0,0,0,0,1,0,1,1]
        // rank =       [  3,  6,  4,  1,  2,  8,  8,  5]
        // relevant lambdas: [4,6,7]
        // compute fraction for lambda = 4
        // Corresponding confidence was = 0.7
        // rank is = 2
        // fraction is = 0.5
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.1
        // rank is = 8
        // fraction is = 0.375
        // compute fraction for lambda = 7
        // Corresponding confidence was = 0.3
        // rank is = 5
        // fraction is = 0.4

        assertEquals(0.4250000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,0,0,0,1,0,1,1},
                                                new double[]{0.5,0.2,0.4,0.9,0.7,0.1,0.1,0.3}
                                                ), 0.00001
                     );
      
        // confidences: [0.3,0.7,0.8,0.8,0.6,0.8,0.0,0.5]
        // real ys:     [0,0,0,0,0,0,1,0]
        // rank =       [  7,  4,  3,  3,  5,  3,  8,  6]
        // relevant lambdas: [6]
        // compute fraction for lambda = 6
        // Corresponding confidence was = 0.0
        // rank is = 8
        // fraction is = 0.125

        assertEquals(0.1250000000, 
                     Metrics.P_AveragePrecision(
                                                new int[]{0,0,0,0,0,0,1,0},
                                                new double[]{0.3,0.7,0.8,0.8,0.6,0.8,0.0,0.5}
                                                ), 0.00001
                     );

    }

    public void testUtilSort(){
        int[] real = {0,1,0,1,0,1};
        double[] pred = {0.4,
                         0.1,
                         0.5,
                         0.2,
                         0.6,
                         0.3};

        int[] sorted = Utils.sort(pred);

        assertTrue(Arrays.toString(sorted),
                   Arrays.equals(new int[]{1,3,5,0,2,4},sorted));

        
        
        // for(int i =0; i < sorted.length; sorted++){
        //     assertEquals(new int[]{3,0,4,1,5,2},sorted);
        // }
        
        
    }

    
    public void testP_ExactMatch(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_exactMatch,
                         Metrics.P_ExactMatch(tmo.real,
                                              tmo.predInt), 0.00000001);
        }
    }
    
    public void testL_ZeroOne(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_zeroOne,
                         Metrics.L_ZeroOne(tmo.real,
                                           tmo.predInt), 0.00000001);
        }
    }
    
    public static Test suite() {
        return new TestSuite(MetricsTest.class);
    }

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }


    public void testl_hamming(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_hamming,
                         Metrics.L_Hamming(tmo.real,
                                           tmo.predInt), 0.00000001);
        }
    }
    public void testp_hamming(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_hamming,
                         Metrics.P_Hamming(tmo.real,
                                           tmo.predInt), 0.00000001);
        }
    }
    public void testp_harmonic(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_harmonic,
                         Metrics.P_Harmonic(tmo.real,
                                            tmo.predInt), 0.00000001);
        }
    }
    public void testp_accuracy(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_accuracy,
                         Metrics.P_Accuracy(tmo.real,
                                            tmo.predInt), 0.00000001);
        }
    }
    public void testp_jaccardindex(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_jaccardindex,
                         Metrics.P_JaccardIndex(tmo.real,
                                                tmo.predInt), 0.00000001);
        }
    }
    public void testl_jaccardist(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_jaccardist,
                         Metrics.L_JaccardDist(tmo.real,
                                               tmo.predInt), 0.00000001);
        }
    }
    public void testl_loglossl(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_loglossl,
                         Metrics.L_LogLossL(tmo.real,
                                            tmo.pred), 0.00000001);
        }
    }
    public void testl_loglossd(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_loglossd,
                         Metrics.L_LogLossD(tmo.real,
                                            tmo.pred), 0.00000001);
        }
    }
    
    public void testp_precisionmacro(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_precisionmacro,
                         Metrics.P_PrecisionMacro(tmo.real,
                                                  tmo.predInt), 0.00000001);
        }
    }
    public void testp_recallmacro(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_recallmacro,
                         Metrics.P_RecallMacro(tmo.real,
                                               tmo.predInt), 0.00000001);
        }
    }

    public void testp_precisionmicro(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_precisionmicro,
                         Metrics.P_PrecisionMicro(tmo.real,
                                                  tmo.predInt), 0.00000001);
        }
    }

    public void testp_recallmicro(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_recallmicro,
                         Metrics.P_RecallMicro(tmo.real,
                                               tmo.predInt), 0.00000001);
        }
    }
    public void testp_fmicroavg(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_fmicroavg,
                         Metrics.P_FmicroAvg(tmo.real,
                                             tmo.predInt), 0.00000001);
        }
    }
    public void testp_fmacroavgl(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_fmacroavgl,
                         Metrics.P_FmacroAvgL(tmo.real,
                                              tmo.predInt), 0.00000001);
        }
    }
    public void testp_fmacroavgd(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_fmacroavgd,
                         Metrics.P_FmacroAvgD(tmo.real,
                                              tmo.predInt), 0.00000001);
        }
    }
    public void testl_oneerror(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_oneerror,
                         Metrics.L_OneError(tmo.real,
                                            tmo.pred), 0.00000001);
        }
    }
    public void testp_averageprecision(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_averageprecision,
                         Metrics.P_AveragePrecision(tmo.real,
                                                    tmo.pred), 0.00000001);
        }
    }
    public void testl_rankloss(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_rankloss,
                         Metrics.L_RankLoss(tmo.real,
                                            tmo.pred), 0.00000001);
        }
    }
    public void testp_macroauprc(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_macroauprc,
                         Metrics.P_macroAUPRC(tmo.real,
                                              tmo.pred), 0.00000001);
        }
    }
    public void testp_macroauroc(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_macroauroc,
                         Metrics.P_macroAUROC(tmo.real,
                                              tmo.pred), 0.00000001);
        }
    }

    public void testp_microa(){
        for(TestMetricObject tmo : tmos){
            
            Instances curvedata = Metrics.curveDataMicroAveraged(tmo.real,
                                                                 tmo.pred);

            if (curvedata != null) {

                

            
                assertEquals(tmo.p_microauroc,
                             ThresholdCurve.getROCArea(curvedata), 0.00000001);
                
                assertEquals(tmo.p_microauprc,
                             ThresholdCurve.getPRCArea(curvedata), 0.00000001);
            }
            // TODO What if it is null? For example when all is missing?

        }
    }
    public void testl_levenshteindistance(){
        for(TestMetricObject tmo : tmos){


            assertEquals(tmo.l_levenshteindistance,
                         Metrics.L_LevenshteinDistance(tmo.real,
                                                       tmo.predInt), 0.00000001);
        }
    }

    
    
    private class TestMetricObject{

        public double[][] pred;
        public int[][] predInt;
        public int[][] real;
        public double p_exactMatch;
        public double l_zeroOne;
        public double l_hamming;
        public double p_hamming;
        public double p_harmonic;
        public double p_accuracy;
        public double p_jaccardindex;
        public double l_jaccardist;
        public double l_loglossl;
        public double l_loglossd;
        public double p_precisionmacro;
        public double p_recallmacro;
        public double p_precisionmicro;
        public double p_recallmicro;
        public double p_fmicroavg;
        public double p_fmacroavgl;
        public double p_fmacroavgd;
        public double l_oneerror;
        public double p_averageprecision;
        public double l_rankloss;
        public double p_macroauprc;
        public double p_macroauroc;
        public double p_microauprc;
        public double p_microauroc;
        public double l_levenshteindistance;
    
        
        public TestMetricObject(double[][] pred,
                                int[][] predInt,
                                int[][] real,
                                double p_exactMatch,
                                double l_zeroOne,
                                double l_hamming,
                                double p_hamming,
                                double p_harmonic,
                                double p_accuracy,
                                double p_jaccardindex,
                                double l_jaccardist,
                                double l_loglossl,
                                double l_loglossd,
                                double p_precisionmacro,
                                double p_recallmacro,
                                double p_precisionmicro,
                                double p_recallmicro,
                                double p_fmicroavg,
                                double p_fmacroavgl,
                                double p_fmacroavgd,
                                double l_oneerror,
                                double p_averageprecision,
                                double l_rankloss,
                                double p_macroauprc,
                                double p_macroauroc,
                                double p_microauprc,
                                double p_microauroc,
                                double l_levenshteindistance){
            this.pred = pred;
            this.predInt = predInt;
            this.real = real;
            this.p_exactMatch = p_exactMatch;
            this.l_zeroOne = l_zeroOne;
            this.l_hamming = l_hamming;
            this.p_hamming = p_hamming;
            this.p_harmonic = p_harmonic;
            this.p_accuracy = p_accuracy;
            this.p_jaccardindex = p_jaccardindex;
            this.l_jaccardist = l_jaccardist;
            this.l_loglossl = l_loglossl;
            this.l_loglossd = l_loglossd;
            this.p_precisionmacro = p_precisionmacro;
            this.p_recallmacro = p_recallmacro;
            this.p_precisionmicro = p_precisionmicro;
            this.p_recallmicro = p_recallmicro;
            this.p_fmicroavg = p_fmicroavg;
            this.p_fmacroavgl = p_fmacroavgl;
            this.p_fmacroavgd = p_fmacroavgd;
            this.l_oneerror = l_oneerror;
            this.p_averageprecision = p_averageprecision;
            this.l_rankloss = l_rankloss;
            this.p_macroauprc = p_macroauprc;
            this.p_macroauroc = p_macroauroc;
            this.p_microauprc = p_microauprc;
            this.p_microauroc = p_microauroc;
            this.l_levenshteindistance = l_levenshteindistance;


            
        }

        
    }
}
