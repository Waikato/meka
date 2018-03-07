# meka.classifiers.multilabel.CDT

## Synopsis
A Conditional Dependency Trellis. Like CDN, but with a trellis structure (like CT) rather than a fully connected network.For more information see:
Yuhong Guoand, Suicheng Gu (2011). Multi-Label Classification Using Conditional Dependency Networks.

Jesse Read, Luca Martino, David Luengo, Pablo Olmos (2015). Scalable multi-output label prediction: From classifier chains to classifier trellises. Pattern Recognition.. URL http://www.sciencedirect.com/science/article/pii/S0031320315000084.

## BibTeX
```
@article{Guoand2011,
   author = {Yuhong Guoand and Suicheng Gu},
   booktitle = {IJCAI '11},
   title = {Multi-Label Classification Using Conditional Dependency Networks},
   year = {2011}
}

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

    Determines the neighbourhood density (the number of neighbours for each node in the trellis).

* `-X <value>`

    The dependency heuristic to use in rearranging the trellis (None by default).

* `-I <value>`

    The total number of iterations.
    default: 1000

* `-Ic <value>`

    The number of collection iterations.
    default: 100

* `-S <value>`

    The seed value for randomization.

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

**Options specific to classifier weka.classifiers.trees.J48:**

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
