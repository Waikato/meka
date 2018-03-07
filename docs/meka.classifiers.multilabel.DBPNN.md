# meka.classifiers.multilabel.DBPNN

## Synopsis
A Deep Back-Propagation Neural Network. For more information see:
Geoffrey Hinton, Ruslan Salakhutdinov (2006). Reducing the Dimensionality of Data with Neural Networks. Science. 313(5786):504-507.

## BibTeX
```
@article{Hinton2006,
   author = {Geoffrey Hinton and Ruslan Salakhutdinov},
   journal = {Science},
   number = {5786},
   pages = {504-507},
   title = {Reducing the Dimensionality of Data with Neural Networks},
   volume = {313},
   year = {2006}
}
```
## Options
* `-N <value>`

    Sets the number of RBMs
    default: 2

* `-H <value>`

    Sets the number of hidden units
    default: 10

* `-E <value>`

    Sets the maximum number of epochs
    default: 1000	(auto-cut-out)

* `-r <value>`

    Sets the learning rate (tyically somewhere between 'very small' and 0.1)
    default: 0.1

* `-m <value>`

    Sets the momentum (typically somewhere between 0.1 and 0.9)
    default: 0.1

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
