## Synopsis
Extremely Randomised Forest of HOMER trees.

## BibTeX
```
@inproceedings{Li2017,
   address = {Cham},
   author = {Li, Jinxia and Zheng, Yihan and Han, Chao and Wu, Qingyao and Chen, Jian},
   booktitle = {Intelligence Science and Big Data Engineering},
   editor = {Sun, Yi and Lu, Huchuan and Zhang, Lihe and Yang, Jian and Huang, Hua},
   pages = {450--460},
   publisher = {Springer International Publishing},
   title = {Extremely Randomized Forest with Hierarchy of Multi-label Classifiers},
   year = {2017},
   ISBN = {978-3-319-67777-4}
}
```
## Options
* `-T threshold`

    Prediction threshold

* `-I <num>`

    Sets the number of models (default 10)

* `-P <size percentage>`

    Size of each bag, as a percentage of total training size (default 67)

* `-S <seed>`

    Random number seed for sampling (default 1)

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
