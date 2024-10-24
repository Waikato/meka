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
 * ExperimentExample.java
 * Copyright (C) 2015-2024 University of Waikato, Hamilton, NZ
 */

package mekaexamples.experiment;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.CC;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.events.LogEvent;
import meka.events.LogListener;
import meka.experiment.DefaultExperiment;
import meka.experiment.Experiment;
import meka.experiment.datasetproviders.DatasetProvider;
import meka.experiment.datasetproviders.LocalDatasetProvider;
import meka.experiment.datasetproviders.MultiDatasetProvider;
import meka.experiment.evaluationstatistics.KeyValuePairs;
import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.RepeatedRuns;
import meka.experiment.events.ExecutionStageEvent;
import meka.experiment.events.IterationNotificationEvent;
import meka.experiment.events.StatisticsNotificationEvent;
import meka.experiment.statisticsexporters.*;
import weka.core.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates an experiment using BR and CC classifiers, evaluating them on the
 * user-supplied datasets.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ExperimentExample {

  protected static String errorMsg(String error) {
    if (error == null)
      return "success!";
    else
      return error;
  }

  public static void main(String[] args) throws Exception {
    if (args.length == 0)
      throw new IllegalArgumentException("Requirement arguments: <dataset1> [<dataset2> [...]]");

    String tmpDir = System.getProperty("java.io.tmpdir");
    System.out.println("Using tmp dir: " + tmpDir);

    Experiment exp = new DefaultExperiment();
    // classifiers
    exp.setClassifiers(new MultiLabelClassifier[]{
            new BR(),
            new CC()
    });
    // datasets
    List<File> files = new ArrayList<>();
    for (String f: args)
      files.add(new File(f));
    LocalDatasetProvider dp1 = new LocalDatasetProvider();
    dp1.setDatasets(files.toArray(new File[0]));
    LocalDatasetProvider dp2 = new LocalDatasetProvider();
    dp2.setDatasets(new File[]{
            new File("src/main/data/solar_flare.arff"),
    });
    MultiDatasetProvider mdp = new MultiDatasetProvider();
    mdp.setProviders(new DatasetProvider[]{dp1, dp2});
    exp.setDatasetProvider(mdp);
    // output of metrics
    KeyValuePairs kvp = new KeyValuePairs();
    kvp.setFile(new File(tmpDir + "/mekaexp.txt"));
    kvp.getFile().delete();  // remove old run
    exp.setStatisticsHandler(kvp);
    // evaluation
    RepeatedRuns eval = new RepeatedRuns();
    eval.setEvaluator(new CrossValidation());
    exp.setEvaluator(eval);
    // stage
    exp.addExecutionStageListener((ExecutionStageEvent e) -> System.err.println("[STAGE] " + e.getStage()));
    // iterations
    exp.addIterationNotificationListener((IterationNotificationEvent e) -> System.err.println("[ITERATION] " + Utils.toCommandLine(e.getClassifier()) + " --> " + e.getDataset().relationName()));
    // statistics
    exp.addStatisticsNotificationListener((StatisticsNotificationEvent e) -> System.err.println("[STATISTICS] #" + e.getStatistics().size()));
    // log events
    exp.addLogListener((LogEvent e) -> System.err.println("[LOG] " + e.getSource().getClass().getName() + ": " + e.getMessage()));
    // output options
    System.out.println("Setup:\n" + OptionUtils.toCommandLine(exp) + "\n");
    // execute
    String msg = exp.initialize();
    System.out.println("initialize: " + errorMsg(msg));
    if (msg != null)
      return;
    msg = exp.run();
    System.out.println("run: " + errorMsg(msg));
    msg = exp.finish();
    System.out.println("finish: " + errorMsg(msg));
    // export them
    TabSeparated tabsepAgg = new TabSeparated();
    tabsepAgg.setFile(new File(tmpDir + "/mekaexp-agg.tsv"));
    SimpleAggregate aggregate = new SimpleAggregate();
    aggregate.setSuffixMean("");
    aggregate.setSuffixStdDev(" (stdev)");
    aggregate.setSkipCount(true);
    aggregate.setSkipMean(false);
    aggregate.setSkipStdDev(false);
    aggregate.setExporter(tabsepAgg);
    TabSeparated tabsepFull = new TabSeparated();
    tabsepFull.setFile(new File(tmpDir + "/mekaexp-full.tsv"));
    TabSeparatedMeasurement tabsepHL = new TabSeparatedMeasurement();
    tabsepHL.setMeasurement("Hamming loss");
    tabsepHL.setFile(new File(tmpDir + "/mekaexp-HL.tsv"));
    TabSeparatedMeasurement tabsepZOL = new TabSeparatedMeasurement();
    tabsepZOL.setMeasurement("ZeroOne loss");
    tabsepZOL.setFile(new File(tmpDir + "/mekaexp-ZOL.tsv"));
    MultiExporter multiexp = new MultiExporter();
    multiexp.setExporters(new EvaluationStatisticsExporter[]{aggregate, tabsepFull, tabsepHL, tabsepZOL});
    multiexp.addLogListener((LogEvent e) -> System.err.println("[EXPORT] " + e.getSource().getClass().getName() + ": " + e.getMessage()));
    System.out.println(OptionUtils.toCommandLine(multiexp));
    msg = multiexp.export(exp.getStatistics());
    System.out.println("export: " + errorMsg(msg));
  }
}
