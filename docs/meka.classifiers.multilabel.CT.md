# meka.classifiers.multilabel.CT

## Synopsis
CC in a trellis structure (rather than a cascaded chain). You set the width and type/connectivity of the trellis, and optionally change the payoff function which guides the placement of nodes (labels) within the trellis.

## BibTeX
```
@article{JesseRead2015,
   author = {Jesse Read, Luca Martino, David Luengo, Pablo Olmos},
   journal = {Pattern Recognition},
   title = {Scalable multi-output label prediction: From classifier chains to classifier trellises},
   year = {2015},
   URL = {http://www.sciencedirect.com/science/article/pii/S0031320315000084}
}
```
## Options
* `-H <value>`

  Determines the width of the trellis (use 0 for chain; use -1 for a square trellis, i.e., width of sqrt(number of labels)).

* `-L <value>`

  Determines the neighbourhood density (the number of neighbours for each node in the trellis). Default = 1, BR = 0.

* `-X <value>`

  The dependency heuristic to use in rearranging the trellis (applicable if chain iterations > 0), default: Ibf (Mutual Information, fast binary version for multi-label data)

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
