# meka.classifiers.multilabel.MajorityLabelset

## Synopsis
Majority Labelset Classifier: Always predict the combination of labels which occurs most frequently in the training set.

## Options
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
