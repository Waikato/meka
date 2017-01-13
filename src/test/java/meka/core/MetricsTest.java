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

        // all wrong

        // always exactly 50% right

        // all correct w missing 50%

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

        double[][] pred = new double[][]{new double[]{1.0,0.0,1.0},
                                         new double[]{0.0,1.0,0.0}};

        int[][] predInt = new int[][]{new int[]{1,0,1},
                                      new int[]{0,1,0}};

        int[][] real = new int[][]{new int[]{-1,-1,-1},
                                   new int[]{-1,-1,-1}};
        
        
        TestMetricObject tmo = new TestMetricObject(pred,
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

    public void testP_ExactMatch(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_exactMatch,
                         Metrics.P_ExactMatch(tmo.real,
                                              tmo.predInt));
        }
    }
    
    public void testL_ZeroOne(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_zeroOne,
                         Metrics.L_ZeroOne(tmo.real,
                                           tmo.predInt));
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
                                           tmo.predInt));
        }
    }
    public void testp_hamming(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_hamming,
                         Metrics.P_Hamming(tmo.real,
                                           tmo.predInt));
        }
    }
    public void testp_harmonic(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_harmonic,
                         Metrics.P_Harmonic(tmo.real,
                                            tmo.predInt));
        }
    }
    public void testp_accuracy(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_accuracy,
                         Metrics.P_Accuracy(tmo.real,
                                            tmo.predInt));
        }
    }
    public void testp_jaccardindex(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_jaccardindex,
                         Metrics.P_JaccardIndex(tmo.real,
                                                tmo.predInt));
        }
    }
    public void testl_jaccardist(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_jaccardist,
                         Metrics.L_JaccardDist(tmo.real,
                                               tmo.predInt));
        }
    }
    public void testl_loglossl(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_loglossl,
                         Metrics.L_LogLossL(tmo.real,
                                            tmo.pred));
        }
    }
    public void testl_loglossd(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_loglossd,
                         Metrics.L_LogLossD(tmo.real,
                                            tmo.pred));
        }
    }
    
    public void testp_precisionmacro(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_precisionmacro,
                         Metrics.P_PrecisionMacro(tmo.real,
                                                  tmo.predInt));
        }
    }
    public void testp_recallmacro(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_recallmacro,
                         Metrics.P_RecallMacro(tmo.real,
                                               tmo.predInt));
        }
    }

    public void testp_precisionmicro(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_precisionmicro,
                         Metrics.P_PrecisionMicro(tmo.real,
                                                  tmo.predInt));
        }
    }

    public void testp_recallmicro(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_recallmicro,
                         Metrics.P_RecallMicro(tmo.real,
                                               tmo.predInt));
        }
    }
    public void testp_fmicroavg(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_fmicroavg,
                         Metrics.P_FmicroAvg(tmo.real,
                                             tmo.predInt));
        }
    }
    public void testp_fmacroavgl(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_fmacroavgl,
                         Metrics.P_FmacroAvgL(tmo.real,
                                              tmo.predInt));
        }
    }
    public void testp_fmacroavgd(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_fmacroavgd,
                         Metrics.P_FmacroAvgD(tmo.real,
                                              tmo.predInt));
        }
    }
    public void testl_oneerror(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_oneerror,
                         Metrics.L_OneError(tmo.real,
                                            tmo.pred));
        }
    }
    public void testp_averageprecision(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_averageprecision,
                         Metrics.P_AveragePrecision(tmo.real,
                                                    tmo.pred));
        }
    }
    public void testl_rankloss(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.l_rankloss,
                         Metrics.L_RankLoss(tmo.real,
                                            tmo.pred));
        }
    }
    public void testp_macroauprc(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_macroauprc,
                         Metrics.P_macroAUPRC(tmo.real,
                                              tmo.pred));
        }
    }
    public void testp_macroauroc(){
        for(TestMetricObject tmo : tmos){
            assertEquals(tmo.p_macroauroc,
                         Metrics.P_macroAUROC(tmo.real,
                                              tmo.pred));
        }
    }

    public void testp_microa(){
        for(TestMetricObject tmo : tmos){
            
            Instances curvedata = Metrics.curveDataMicroAveraged(tmo.real,
                                                                 tmo.pred);

            if (curvedata != null) {

                

            
                assertEquals(tmo.p_microauroc,
                             ThresholdCurve.getROCArea(curvedata));
                
                assertEquals(tmo.p_microauprc,
                             ThresholdCurve.getPRCArea(curvedata));
            }
            // TODO What if it is null? For example when all is missing?

        }
    }
    public void testl_levenshteindistance(){
        for(TestMetricObject tmo : tmos){


            assertEquals(tmo.l_levenshteindistance,
                         Metrics.L_LevenshteinDistance(tmo.real,
                                                       tmo.predInt));
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
