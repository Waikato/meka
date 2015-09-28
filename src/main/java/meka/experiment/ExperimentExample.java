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
import meka.experiment.datasetproviders.LocalDatasetProvider;
import meka.experiment.evaluationstatistics.KeyValuePairs;
import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.RepeatedRuns;
import meka.experiment.events.IterationNotificationEvent;
import meka.experiment.events.IterationNotificationListener;
import meka.experiment.events.LogEvent;
import meka.experiment.events.LogListener;
import meka.experiment.statisticsexporters.*;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.RemoveByName;
import weka.filters.unsupervised.attribute.RenameAttribute;

import java.io.File;

/**
 * Just for testing the experiment framework.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExperimentExample {
	public static void main(String[] args) throws Exception {
		Experiment exp = new DefaultExperiment();
		// classifiers
		exp.setClassifiers(new MultiLabelClassifier[]{
				new BR(),
				new CC()
		});
		// datasets
		LocalDatasetProvider dp = new LocalDatasetProvider();
		dp.setDatasets(new File[]{
				new File("src/main/data/Music.arff"),
				new File("src/main/data/solar_flare.arff"),
		});
		exp.setDatasetProvider(dp);
		// output of metrics
		KeyValuePairs sh = new KeyValuePairs();
		sh.setFile(new File(System.getProperty("java.io.tmpdir") + "/mekaexp.txt"));
		exp.setStatisticsHandler(sh);
		// evaluation
		RepeatedRuns eval = new RepeatedRuns();
		eval.setEvaluator(new CrossValidation());
		exp.setEvaluator(eval);
		// iterations
		exp.addIterationNotificationListener(new IterationNotificationListener() {
			@Override
			public void nextIteration(IterationNotificationEvent e) {
				System.err.println("[ITERATION] " + Utils.toCommandLine(e.getClassifier()) + " --> " + e.getDataset().relationName());
			}
		});
		// log events
		exp.addLogListener(new LogListener() {
			@Override
			public void logMessage(LogEvent e) {
				System.err.println("[LOG] " + e.getSource().getClass().getName() + ": " + e.getMessage());
			}
		});
		// execute
		String msg = exp.initialize();
		System.out.println("initialize: " + msg);
		if (msg != null)
			return;
		msg = exp.run();
		System.out.println("run: " + msg);
		msg = exp.finish();
		System.out.println("finish: " + msg);
		// print stats (also stored in file)
		System.out.println("statistics:\n" + exp.getStatistics());
		// export them
		TabSeparated tabsepAgg = new TabSeparated();
		tabsepAgg.setFile(new File(System.getProperty("java.io.tmpdir") + "/mekaexp-agg.tsv"));
		RemoveByName remove = new RemoveByName();
		remove.setExpression(".*(" + SimpleAggregate.SUFFIX_COUNT + "|" + SimpleAggregate.SUFFIX_STDEV + ")$");
		RenameAttribute rename = new RenameAttribute();
		rename.setFind(SimpleAggregate.SUFFIX_MEAN + "$");
		rename.setReplace("");
		MultiFilter multi = new MultiFilter();
		multi.setFilters(new Filter[]{remove, rename});
		WekaFilter filter = new WekaFilter();
		filter.setFilter(multi);
		filter.setExporter(tabsepAgg);
		SimpleAggregate aggregate = new SimpleAggregate();
		aggregate.setExporter(filter);
		TabSeparated tabsepFull = new TabSeparated();
		tabsepFull.setFile(new File(System.getProperty("java.io.tmpdir") + "/mekaexp-full.tsv"));
		TabSeparatedMeasurement tabsepHL = new TabSeparatedMeasurement();
		tabsepHL.setMeasurement("Hamming loss");
		tabsepHL.setFile(new File(System.getProperty("java.io.tmpdir") + "/mekaexp-HL.tsv"));
		TabSeparatedMeasurement tabsepZOL = new TabSeparatedMeasurement();
		tabsepZOL.setMeasurement("ZeroOne loss");
		tabsepZOL.setFile(new File(System.getProperty("java.io.tmpdir") + "/mekaexp-ZOL.tsv"));
		MultiExporter multiexp = new MultiExporter();
		multiexp.setExporters(new EvaluationStatisticsExporter[]{aggregate, tabsepFull, tabsepHL, tabsepZOL});
		msg = multiexp.export(exp.getStatistics());
		System.out.println("export: " + msg);
	}
}
