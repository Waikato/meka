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

/**
 * ExperimentExample.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package meka.experiment;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.CC;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.events.LogEvent;
import meka.events.LogListener;
import meka.experiment.datasetproviders.DatasetProvider;
import meka.experiment.datasetproviders.LocalDatasetProvider;
import meka.experiment.datasetproviders.MultiDatasetProvider;
import meka.experiment.evaluationstatistics.KeyValuePairs;
import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.RepeatedRuns;
import meka.experiment.events.*;
import meka.experiment.statisticsexporters.*;
import weka.core.Utils;

import java.io.File;

/**
 * Just for testing the experiment framework.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExperimentExample {
	public static void main(String[] args) throws Exception {
		String tmpDir = System.getProperty("java.io.tmpdir");
		System.out.println("Using tmp dir: " + tmpDir);

		Experiment exp = new DefaultExperiment();
		// classifiers
		exp.setClassifiers(new MultiLabelClassifier[]{
				new BR(),
				new CC()
		});
		// datasets
		LocalDatasetProvider dp1 = new LocalDatasetProvider();
		dp1.setDatasets(new File[]{
				new File("src/main/data/Music.arff"),
		});
		LocalDatasetProvider dp2 = new LocalDatasetProvider();
		dp2.setDatasets(new File[]{
				new File("src/main/data/solar_flare.arff"),
		});
		MultiDatasetProvider mdp = new MultiDatasetProvider();
		mdp.setProviders(new DatasetProvider[]{dp1, dp2});
		exp.setDatasetProvider(mdp);
		// output of metrics
		// not-threadsafe (incremental file writing!)
		KeyValuePairs sh = new KeyValuePairs();
		sh.setFile(new File(tmpDir + "/mekaexp.txt"));
		exp.setStatisticsHandler(sh);
		// threadsafe
		/*
		Serialized sh = new Serialized();
		sh.setFile(new File(tmpDir + "/mekaexp2.ser"));
		exp.setStatisticsHandler(sh);
		*/
		// evaluation
		RepeatedRuns eval = new RepeatedRuns();
		eval.setEvaluator(new CrossValidation());
		exp.setEvaluator(eval);
		// stage
		exp.addExecutionStageListener(new ExecutionStageListener() {
			@Override
			public void experimentStage(ExecutionStageEvent e) {
				System.err.println("[STAGE] " + e.getStage());
			}
		});
		// iterations
		exp.addIterationNotificationListener(new IterationNotificationListener() {
			@Override
			public void nextIteration(IterationNotificationEvent e) {
				System.err.println("[ITERATION] " + Utils.toCommandLine(e.getClassifier()) + " --> " + e.getDataset().relationName());
			}
		});
		// statistics
		exp.addStatisticsNotificationListener(new StatisticsNotificationListener() {
			@Override
			public void statisticsAvailable(StatisticsNotificationEvent e) {
				System.err.println("[STATISTICS] #" + e.getStatistics().size());
			}
		});
		// log events
		exp.addLogListener(new LogListener() {
			@Override
			public void logMessage(LogEvent e) {
				System.err.println("[LOG] " + e.getSource().getClass().getName() + ": " + e.getMessage());
			}
		});
		// output options
		System.out.println("Setup:\n" + OptionUtils.toCommandLine(exp) + "\n");
		// execute
		String msg = exp.initialize();
		System.out.println("initialize: " + msg);
		if (msg != null)
			return;
		msg = exp.run();
		System.out.println("run: " + msg);
		msg = exp.finish();
		System.out.println("finish: " + msg);
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
		multiexp.addLogListener(new LogListener() {
			@Override
			public void logMessage(LogEvent e) {
				System.err.println("[EXPORT] " + e.getSource().getClass().getName() + ": " + e.getMessage());
			}
		});
		System.out.println(OptionUtils.toCommandLine(multiexp));
		msg = multiexp.export(exp.getStatistics());
		System.out.println("export: " + msg);
	}
}
