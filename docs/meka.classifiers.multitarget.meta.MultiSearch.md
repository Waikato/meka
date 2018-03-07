# meka.classifiers.multitarget.meta.MultiSearch

## Synopsis
Performs a search of an arbitrary number of parameters of a classifier and chooses the best combination found.
The properties being explored are totally up to the user.

E.g., if you have a FilteredClassifier selected as base classifier, sporting a PLSFilter and you want to explore the number of PLS components, then your property will be made up of the following components:
 - filter: referring to the FilteredClassifier's property (= PLSFilter)
 - numComponents: the actual property of the PLSFilter that we want to modify
And assembled, the property looks like this:
  filter.numComponents


The best classifier setup can be accessed after the buildClassifier call via the getBestClassifier method.

The trace of setups evaluated can be accessed after the buildClassifier call as well, using the following methods:
- getTrace()
- getTraceSize()
- getTraceValue(int)
- getTraceFolds(int)
- getTraceClassifierAsCli(int)
- getTraceParameterSettings(int)

Using the weka.core.setupgenerator.ParameterGroup parameter, it is possible to group dependent parameters. In this case, all top-level parameters must be of type weka.core.setupgenerator.ParameterGroup.

## Options
* `-E <ACC|JIDX|HSCORE|EM|JDIST|HLOSS|ZOLOSS|HARSCORE|OE|RLOSS|AVGPREC|LOGLOSSL|LOGLOSSD|F1MICRO|F1MACROEX|F1MACROLBL|AUPRC|AUROC|LCARD|LDIST>`

  Determines the parameter used for evaluation:
  ACC = Accuracy
  JIDX = Jaccard index
  HSCORE = Hamming score
  EM = Exact match
  JDIST = Jaccard distance
  HLOSS = Hamming loss
  ZOLOSS = ZeroOne loss
  HARSCORE = Harmonic score
  OE = One error
  RLOSS = Rank loss
  AVGPREC = Avg precision
  LOGLOSSL = Log Loss (lim. L)
  LOGLOSSD = Log Loss (lim. D)
  F1MICRO = F1 (micro averaged)
  F1MACROEX = F1 (macro averaged by example)
  F1MACROLBL = F1 (macro averaged by label)
  AUPRC = AUPRC (macro averaged)
  AUROC = AUROC (macro averaged)
  LCARD = Label cardinality (predicted)
  LDIST = Levenshtein distance
  (default: ACC)

* `-search "<classname options>"`

  A property search setup.

* `-algorithm "<classname options>"`

  A search algorithm.

* `-log-file <filename>`

  The log file to log the messages to.
  (default: none)

* `-S <num>`

  Random number seed.
  (default 1)

* `-W <classifier name>`

  Full name of base classifier.
  (default: meka.classifiers.multitarget.RAkELd)

* `-output-debug-info`

  If set, classifier is run in debug mode and
  may output additional info to the console

* `-do-not-check-capabilities`

  If set, classifier capabilities are not checked before classifier is built
  (use with caution).

* `-num-decimal-places`

  The number of decimal places for the output of numbers in the model (default 2).

* `-batch-size`

  The desired batch size for batch prediction  (default 100).

* **Options specific to classifier meka.classifiers.multitarget.RAkELd:**

* `-k <num>`

  The number of labels in each partition -- should be 1 <= k < (L/2) where L is the total number of labels.

* `-P <value>`

  Sets the pruning value, defining an infrequent labelset as one which occurs <= P times in the data (P = 0 defaults to LC).
  default: 0	(LC)

* `-N <value>`

  Sets the (maximum) number of frequent labelsets to subsample from the infrequent labelsets.
  default: 0	(none)
  n	N = n
  -n	N = n, or 0 if LCard(D) >= 2
  n-m	N = random(n,m)

* `-S <value>`

  The seed value for randomization
  default: 0

* `-W <classifier name>`

  Full name of base classifier.
  (default: weka.classifiers.trees.J48)

* `-output-debug-info`

  If set, classifier is run in debug mode and
  may output additional info to the console

* `-do-not-check-capabilities`

  If set, classifier capabilities are not checked before classifier is built
  (use with caution).

* `-num-decimal-places`

  The number of decimal places for the output of numbers in the model (default 2).

* `-batch-size`

  The desired batch size for batch prediction  (default 100).

* **Options specific to classifier weka.classifiers.trees.J48:**

* `-U`

  Use unpruned tree.

* `-O`

  Do not collapse tree.

* `-C <pruning confidence>`

  Set confidence threshold for pruning.
  (default 0.25)

* `-M <minimum number of instances>`

  Set minimum number of instances per leaf.
  (default 2)

* `-R`

  Use reduced error pruning.

* `-N <number of folds>`

  Set number of folds for reduced error
  pruning. One fold is used as pruning set.
  (default 3)

* `-B`

  Use binary splits only.

* `-S`

  Do not perform subtree raising.

* `-L`

  Do not clean up after the tree has been built.

* `-A`

  Laplace smoothing for predicted probabilities.

* `-J`

  Do not use MDL correction for info gain on numeric attributes.

* `-Q <seed>`

  Seed for random data shuffling (default 1).

* `-doNotMakeSplitPointActualValue`

  Do not make split point actual value.

* `-output-debug-info`

  If set, classifier is run in debug mode and
  may output additional info to the console

* `-do-not-check-capabilities`

  If set, classifier capabilities are not checked before classifier is built
  (use with caution).

* `-num-decimal-places`

  The number of decimal places for the output of numbers in the model (default 2).

* `-batch-size`

  The desired batch size for batch prediction  (default 100).
