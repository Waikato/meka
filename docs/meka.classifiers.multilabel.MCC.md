# meka.classifiers.multilabel.MCC

## Synopsis
Classifier Chains with Monte Carlo optimization. For more information see:
Jesse Read, Luca Martino, David Luengo: Efficient Monte Carlo Optimization for Multi-label Classifier Chains. In: ICASSP'13: International Conference on Acoustics, Speech, and Signal Processing, 2013.

Jesse Read, Luca Martino, David Luengo (2013). Efficient Monte Carlo Optimization for Multi-dimensional Classifier Chains. Elsevier Pattern Recognition..

## BibTeX
```
@inproceedings{Read2013,
   author = {Jesse Read and Luca Martino and David Luengo},
   booktitle = {ICASSP'13: International Conference on Acoustics, Speech, and Signal Processing},
   title = {Efficient Monte Carlo Optimization for Multi-label Classifier Chains},
   year = {2013}
}

@article{Read2013,
   author = {Jesse Read and Luca Martino and David Luengo},
   journal = {Elsevier Pattern Recognition},
   title = {Efficient Monte Carlo Optimization for Multi-dimensional Classifier Chains},
   year = {2013}
}
```
## Options
* `-Is <value>`

  The number of iterations to search the chain space at train time.
  default: 0

* `-Iy <value>`

  The number of iterations to search the output space at test time.
  default: 10

* `-P <value>`

  Sets the payoff function. Any of those listed in regular evaluation output will do (e.g., 'Exact match').
  default: Exact match

* `-S <value>`

  The seed value for randomizing the data.
  (default: 0)

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
