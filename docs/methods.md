# Methods in MEKA

The description of methods given here is only a summary, with some command-line examples. All examples assume that the `classpath` is already set, e.g., by using the command line flag `-cp target/meka-X.Y.Z.jar:lib/*` (in Linux). See the Tutorial for more information. 
All examples are given usen `-t data.arff` which implies a train/test split. For other options, see the Tutorial.


###  Binary Relevance Methods 

Binary relevance methods create an individual model for each label. This means that each model is a simply binary problem, but many labels means many models which can easily fill up memory.

|Class                  |  | Name                                         | Description  / Notes                                                                     | Examples
|-----------------------|--|----------------------------------------------|------------------------------------------------------------------------------------------|---------
| BR                    |  | Binary Relevance                             | Individual classifiers                                                                   | java meka.classifiers.multilabel.BR -t data.arff -W weka.classifiers.functions.SMO              
| CC                    |  | Classifier Chains                            | .. linked in a cascaded chain, random node order                                         | java meka.classifiers.multilabel.CC -t data.arff -W weka.classifiers.functions.SMO             
| CT                    |  | Classifier Trellis                           | .. linked in a trellis, connectivity `L`, heuristic `X`                                  | java meka.classifiers.multilabel.CT -L 2 -X Ibf -t data.arff -W weka.classifiers.functions.SMO              
| CDN                   |p | Classifier Dependency Network                | Fully-connected, undirected, `I` iterations, `Ic` of which for collecting marginals      | java meka.classifiers.multilabel.CDN -I 1000 -Ic 100 -t data.arff -W weka.classifiers.functions.SMO \-- -M
| CDT                   |p | Classifier Dependency Trellis                | .. in a trellis structure of connectivity `L`, heuristic `X`                             | java meka.classifiers.multilabel.CDT -I 1000 -Ic 100 -L 3 -t data.arff -W weka.classifiers.functions.SMO \-- -M              
| meta.BaggingML        |m | Ensembles of Classifier Chains (ECC)         | A Bagging ensemble of `I` chains, bagging `P` % of the instances                         | java meka.classifiers.multilabel.meta.BaggingML -I 10 -P 100 -t data.arff -W meka.classifiers.multilabel.CC \-- -W weka.classifiers.functions.SMO
| meta.RandomSubspaceML |m | Subspace Ensembles of Classifier Chains      | .. where each chain is given only a `A` percent of the attributes                        | java meka.classifiers.multilabel.meta.RandomSubspaceML -I 10 -P 80 -A 50 -t data.arff -W meka.classifiers.multilabel.CC \-- -W weka.classifiers.functions.SMO
| PCC                   |p | Probabilistic Classifier Chains              | A classifier chain with Bayes Optimal inference, (exponential trials)                    | java meka.classifiers.multilabel.PCC -t data.arff -W weka.classifiers.functions.Logistic
| MCC                   |p | Monte-Carlo Classifier Chains                | .. with Monte Carlo search, maximum of `Iy` inference trials and `Is` chain-order trails | java meka.classifiers.multilabel.MCC -Iy 100 -Is 10 -t data.arff -W weka.classifiers.functions.Logistic             
| PMCC                  |p | Population of Monte-Carlo Classifier Chains  | .. a population of `M` chains is kept (not just the best)                                | java meka.classifiers.multilabel.PMCC -Iy 100 -Is 50 -M 10 -t data.arff -W weka.classifiers.functions.Logistic             
| BCC                   |  | Bayesian Classifier Chains                   | Tree connectivity based on Maximum Spanning Tree algorithm                               | java meka.classifiers.multilabel.BCC -t data.arff -W weka.classifiers.bayes.NaiveBayes

Where:

* _m_ indicates a meta method, can be used with any other Meka classifier. Only examples are given here. 
* _p_ indicates probabilistic base classifier required (at least, recommended) -- a classifier which can output a probability between `0` and `1` for each label.
* _s_ indicates a semi-supervised method, which will use the test data (minus labels) to help train.

###  Label Powerset Methods 

Label powerset inspired classifiers generally provide excellent performance, although only some parameterizations will be able to scale up to larger datasets. In most situations, `RAkEL` is a good option. `PS` methods work best when there are only a few typical combinations of labels, and most combinations occur only once and can be pruned away. `EPS` will work better. MEKA's implementation of `RAkELd` is in fact a combination of `RAkEL` + `PS`, and should scale up to thousands or even tens of thousands of labels (with enough pruning).

|Class             |   | Name                                 | Description  / Notes                                                                  | Examples
|------------------|---|--------------------------------------|---------------------------------------------------------------------------------------|---------
| MajorityLabelset |   | Majority Lableset                    | Always predicts most common labelset                                                  | java meka.classifiers.multilabel.MajorityLabelset -t data.arff
| LC               |   | Label Combination / Label Powerset   | As a multi-class problem                                                              | java meka.classifiers.multilabel.LC -t data.arff -W weka.classifiers.functions.SMO             
| PS               |   | Pruned Sets (Pruned Label Powerset)  | .. with `P` infrequent classes pruned, but up to `N` subcopies reintroduced           | java meka.classifiers.multilabel.PS -P 1 -N 1 -t data.arff -W weka.classifiers.functions.SMO               
| meta.EnsembleML  | m | Ensembles of Pruned Sets (EPS)       | .. in an ensemble, `P` can be selected from a range                                   | java meka.classifiers.multilabel.meta.EnsembleML -t data.arff -W meka.classifiers.multilabel.PS -P 1-5 -N 1 -W weka.classifiers.functions.SMO
| PSt              | p | Pruned Sets thresholded              | .. with an internal threshold                                                         | java meka.classifiers.multilabel.PS -P 1 -N 1 -t data.arff -W weka.classifiers.functions.SMO \-- -M            
| NSR	           |   | Nearest Set Replacement (NSR)        | Multi-target version of PS                                                            | java meka.classifiers.multitarget.NSR -P 1 -N 1 -t data.arff -W weka.classifiers.functions.SMO
| SCC	           |   | Super Class/Node Classifier          | Creates super nodes, based on `I` simulated annealing iterations, `V` internal split validations, and runs NSR on them.  | java meka.classifiers.multitarget.SCC -I 1000 -V 10 -P 1 -t data.arff -W meka.classifiers.multitarget.CC \-- -W weka.classifiers.functions.SMO
| RAkEL            |   | Random k-labEL Pruned Sets           | .. in subsets of size `k`                                                             | java meka.classifiers.multilabel.RAkEL -P 1 -N 1 -k 3 -t data.arff -W weka.classifiers.functions.SMO
| RAkELd           |   | Disjoint Random Pruned Sets          | .. which are non-overlapping                                                          | java meka.classifiers.multilabel.RAkELd -P 1 -N 1 -k 3 -t data.arff -W weka.classifiers.functions.SMO
| SERAkELd         |   | Subspace Ensembles of `RAkELd`       | .. an a subset ensemble                                                               | java meka.classifiers.multilabel.meta.RandomSubspaceML -I 10 -P 60 -A 50 -t data.arff -W meka.classifiers.multilabel.RAkELd \-- -P 3 -N 1 -k 5 -W weka.classifiers.functions.SMO
| HASEL            |   | Hierachical Label Sets               | .. disjoint subsets defined by a hierarchy in the dataset, e.g., `@attribute C.C4`    | java meka.classifiers.multilabel.HASEL -t Enron.arff -P 3 -N 1 -W weka.classifiers.functions.SMO
| MULAN -S RAkEL1  |   | Random k-labEL Sets                  | MULAN's implementation (no pruning); 10 models, subsets of size [half the number of labels] | java meka.classifiers.multilabel.MULAN -t data.arff -S RAkEL1
| MULAN -S RAkEL2  |   | Random k-labEL Sets                  | MULAN's implementation (no pruning); [2 times the number of labels] models, subsets of size 3 | java meka.classifiers.multilabel.MULAN -t data.arff -S RAkEL2
| meta.SubsetMapper| m | Subset Mapper                        | Like ECOCs. Maps predictions to nearest known label combination (from training set)   | java meka.classifiers.multilabel.meta.SubsetMapper -t data.arff -W meka.classifiers.multilabel.BR \-- -W weka.classifiers.functions.SMO


### Pairwise and Threshold Methods 

Pairwise methods can work well, but they are very sensitive to the number of labels. One-vs-rest classifiers (e.g., `RT`) can be faster with large numbers of labels, but may not perform as well.

|Class          |  | Name                                 | Description  / Notes                                                           | Examples
|---------------|--|--------------------------------------|--------------------------------------------------------------------------------|---------
| MULAN -S CLR  |  | Calibrated Label Ranking             | Compares each pair, `01` vs `10`, with a calbrated label                       | java meka.classifiers.multilabel.MULAN -t data.arff -S CLR
| FW            |  | Fourclass Pairwise                   | Compares each pair, `00`,`01`,`10`,`11`, with threshold                        | java meka.classifiers.multilabel.FW -t data.arff -W weka.classifiers.functions.SMO  
| RT            |p | Rank + Threshold                     | Duplicates multi-label examples into examples with one label each (_one vs. rest_). Trains a multi-class classifier, and uses a threshold to reconstitute a multi-label classification                          | java meka.classifiers.multilabel.RT -t data.arff -W weka.classifiers.bayes.NaiveBayes

### Other Methods 

Semi-supervised methods, when you want to use the testing data (labels are removed first) to help train, neural-network based methods (algorithm adaptation, no base classifier supplied), and `deep' methods which create a higher level feature representation during training.

<!-- .Other Methods (Deep, Neural Network based, and Semi-supervised Methods) --> 

|Class          |     | Name                                 | Description  / Notes                       | Examples
|---------------|-----|--------------------------------------|--------------------------------------------|----------------
| EM            | mps | Expectation Maximization             | Uses the classic EM algorithm              | java meka.classifiers.multilabel.meta.EM -t data.arff -I 100 -W meka.classifiers.multilabel.BR \-- -W weka.classifiers.bayes.NaiveBayes
| CM            | ms  | Classification Maximization          | .. a non-probabilistic version thereof     | java meka.classifiers.multilabel.meta.CM -t data.arff -I 100 -W meka.classifiers.multilabel.BR \-- -W weka.classifiers.bayes.NaiveBayes
| BPNN          |     | Back Propagation Neural Network      | Multi-Layer Perceptron with `H` hidden nodes, `E` iterations  | java meka.classifiers.multilabel.BPNN -t data.arff -H 20 -E 1000 -r 0.01 -m 0.2
| DBPNN         |     | Deep Back Propagation Neural Network | .. and with `N` layers, using RBMs for pretraining | java meka.classifiers.multilabel.DBPNN -t data.arff -N 3 -H 20 -E 1000 -r 0.01 -m 0.2
| DeepML        | m   | Deep Multi-label                     | .. with any ML classifier on top           | java meka.classifiers.multilabel.meta.DeepML -t data.arff -N 2 -H 20 -E 1000 -r 0.01 -m 0.2 -W meka.classifiers.multilabel.CC \-- -W weka.classifiers.functions.SMO

### Updateable Methods for Data Streams

Incremental methods suitable for data streams: classifying instances and updating a classifier one instance at a time. Note that accuracy is recorded in windows. Use `-B` to set the number of windows, and higher verbosity, e.g., `-verbosity 5` to see the accuracy reported also per window. These methods are adapted versions of regular multi-label methods. 

|Class          |   | Name                                 | Description  / Notes                             | Examples
|---------------|---|--------------------------------------|--------------------------------------------------|-----------
|BRUpdateable   |   | Updateable Binary Relevance          | Use with any _updateable_ base classifier        | java meka.classifiers.multilabel.incremental.BRUpdateable -B 10 -t data.arff -W weka.classifiers.trees.HoeffdingTree
|CCUpdateable   |   | Updateable Classifier Chains         | Use with any _updateable_ base classifier        | java meka.classifiers.multilabel.incremental.BRUpdateable -B 50 -t data.arff -W weka.classifiers.trees.HoeffdingTree
|PSUpdateable   |   | Updateable Pruned Sets               | .. with initial buffer of size `I`, and max support `S`  | java meka.classifiers.multilabel.incremental.PSUpdateable -B 10 -I 500 -S 20 -t data.arff -W weka.classifiers.bayes.NaiveBayesUpdateable



### Notes on Scalability

Scalability in general depends on 

* The number of features: try doing feature selection first (see `WEKA` documentation, for example) and create a new ARFF file -- or use a subset meta method ensemble `RandomSubspaceML`
* The number of instances: use an ensemble method `EnsembleML` with tiny subsets (e.g., `-P 20` for 20 percent), or `RandomSubspaceML`
* The number of labels: if over 1000 labels, use a label powerset method, if labelling is very dense and noisy (many label combinations), use a binary relevance method
* The base classifier: Most methods already come with a 'sensible' default classifier, for example `J48` as a base classifier for problem transformation methods, and `CC` as a default classifier for many ensemble methods. To use the default classifier simply leave out the `-W` option. 
However, the default classifier is not necessarily the fastest, or the best for your data! WEKA's `SMO` usually provides good results, but for large datasets you may have to use something like `NaiveBayes` or `SGD`. Keep in mind that some classes (like `SMO`) make many binary problems internally to deal with the multi-class case (i.e., as produced by label powerset methods). 

