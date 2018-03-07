# meka.classifiers.multilabel.meta.FilteredClassifier

## Synopsis
Class for running an arbitrary classifier on data that has been passed through an arbitrary filter. Like the classifier, the structure of the filter is based exclusively on the training data and test instances will be processed by the filter without changing their structure.

If unequal instance weights or attribute weights are present, and the filter or the classifier are unable to deal with them, the instances and/or attributes are resampled with replacement based on the weights before they are passed to the filter or the classifier (as appropriate).

## Options
* `-F <filter specification>`

  Full class name of filter to use, followed
  by filter options.
  default: "weka.filters.supervised.attribute.Discretize -R first-last -precision 6"

* `-doNotCheckForModifiedClassAttribute`

  If set, classifier will not check whether the filter modifies the class (use with caution).

* `-S <num>`

  Random number seed.
  (default 1)

* `-W <classifier name>`

  Full name of base classifier.
  (default: meka.classifiers.multilabel.BR)

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

* **Options specific to classifier meka.classifiers.multilabel.BR:**

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

* **Options specific to filter weka.filters.AllFilter:**

* `-output-debug-info`

  If set, filter is run in debug mode and
  may output additional info to the console

* `-do-not-check-capabilities`

  If set, filter capabilities are not checked before filter is built
  (use with caution).
