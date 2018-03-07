# meka.classifiers.multilabel.PS

## Synopsis
The Pruned Sets method (PS).
Removes examples with P-infrequent labelsets from the training data, then subsamples these labelsets N time to produce N new examples with P-frequent labelsets. Then train a standard LC classifier. The idea is to reduce the number of unique class values that would otherwise need to be learned by LC. Best used in an Ensemble (e.g., EnsembleML).
For more information see:
Jesse Read, Bernhard Pfahringer, Geoff Holmes: Multi-label Classification Using Ensembles of Pruned Sets. In: ICDM'08: International Conference on Data Mining (ICDM 2008). Pisa, Italy., 2008.

## BibTeX
```
@inproceedings{JesseRead2008,
   author = {Jesse Read, Bernhard Pfahringer, Geoff Holmes},
   booktitle = {ICDM'08: International Conference on Data Mining (ICDM 2008). Pisa, Italy.},
   title = {Multi-label Classification Using Ensembles of Pruned Sets},
   year = {2008}
}
```
## Options
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
