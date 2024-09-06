## Synopsis
HOMER tree algorithm. For more information see:
Tsoumakas, Grigorios, Katakis, Ioannis, Vlahavas, Ioannis: Effective and efficient multilabel classification in domains with large number of labels. In: Proc. ECML/PKDD 2008 Workshop on Mining Multidimensional Data (MMD’08), 53--59, 2008.

## BibTeX
```
@inproceedings{Tsoumakas2008,
   author = {Tsoumakas, Grigorios and Katakis, Ioannis and Vlahavas, Ioannis},
   booktitle = {Proc. ECML/PKDD 2008 Workshop on Mining Multidimensional Data (MMD’08)},
   organization = {sn},
   pages = {53--59},
   title = {Effective and efficient multilabel classification in domains with large number of labels},
   volume = {21},
   year = {2008}
}
```
## Options
* `-k K`

    The number of partitions per level.

* `-S seed`

    The seed to set.

* `-ls class`

    The label splitter class to use.

* `-t threshold`

    The threshold for the multi-label classifier distribution

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

**Options specific to classifier meka.classifiers.multilabel.BR:**

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
