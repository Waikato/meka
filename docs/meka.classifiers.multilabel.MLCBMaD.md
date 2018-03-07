# meka.classifiers.multilabel.MLCBMaD

## Synopsis
MLC-BMaD - Multi-Label Classification using Boolean Matrix Decomposition. Transforms the labels using a Boolean matrix decomposition, the first resulting matrix are used as latent labels and a classifier is trained to predict them. The second matrix is used in a multiplication to decompress the predicted latent labels.
For more information see:
J"org Wicker, Bernhard Pfahringer, Stefan Kramer: Multi-Label Classification using Boolean Matrix Decomposition. In: Proceedings of the 27th Annual ACM Symposium on Applied Computing, 179-186, 2012.

## BibTeX
```
@inproceedings{J"orgWicker2012,
   author = {J"org Wicker, Bernhard Pfahringer, Stefan Kramer},
   booktitle = {Proceedings of the 27th Annual ACM Symposium on Applied Computing},
   pages = {179-186},
   title = {Multi-Label Classification using Boolean Matrix Decomposition},
   year = {2012}
}
```
## Options
* `-size <value>`

    Size of the compressed matrix. Should be
    less than the number of labels and more than 1.
    (default: 20)

* `-threshold <value>`

    Threshold for the matrix decompositon, what is considered frequent.
    Between 0 and 1.
    (default: 0.5)

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
