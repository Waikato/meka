# meka.classifiers.multilabel.meta.DeepML

## Synopsis
Create a new feature space using a stack of RBMs, then employ a multi-label classifier on top. For more information see:
Jesse Read, Jaako Hollmen: A Deep Interpretation of Classifier Chains. In: Advances in Intelligent Data Analysis {XIII} - 13th International Symposium, {IDA} 2014, 251--262, 2014.

## BibTeX
```
@inproceedings{Read2014,
   author = {Jesse Read and Jaako Hollmen},
   booktitle = {Advances in Intelligent Data Analysis {XIII} - 13th International Symposium, {IDA} 2014},
   pages = {251--262},
   title = {A Deep Interpretation of Classifier Chains},
   year = {2014}
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
