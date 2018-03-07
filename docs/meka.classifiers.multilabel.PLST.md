## Synopsis
PLST - Principle Label Space Transformation. Uses SVD to generate a matrix that transforms the label space. This implementation is adapted from the MatLab implementation provided by the authors.

https://github.com/hsuantien/mlc_lsdr

For more information see:
 Farbound Tai, Hsuan-Tien Lin: Multilabel classification with principal label space transformation. In: Neural Computation, 2508-2542, 2012.

## BibTeX
```
@inproceedings{Tai2012,
   author = {Farbound Tai and Hsuan-Tien Lin},
   booktitle = {Neural Computation},
   number = {9},
   pages = {2508-2542},
   title = {Multilabel classification with principal label space transformation},
   volume = {24},
   year = {2012}
}
```
## Options
* `-size <value>`

    Size of the compressed matrix. Should be
    less than the number of labels and more than 1.
    (default: 3)

* `-W <classifier name>`

    Full name of base classifier.
    (default: meka.classifiers.multitarget.CR)

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

**Options specific to classifier meka.classifiers.multitarget.CR:**

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

**Options specific to classifier weka.classifiers.functions.LinearRegression:**

* `-S <number of selection method>`

    Set the attribute selection method to use. 1 = None, 2 = Greedy.
    (default 0 = M5' method)

* `-C`

    Do not try to eliminate colinear attributes.

* `-R <double>`

    Set ridge parameter (default 1.0e-8).

* `-minimal`

    Conserve memory, don't keep dataset header and means/stdevs.
    Model cannot be printed out if this option is enabled.	(default: keep data)

* `-additional-stats`

    Output additional statistics.

* `-output-debug-info`

    If set, classifier is run in debug mode and
    may output additional info to the console

* `-do-not-check-capabilities`

    If set, classifier capabilities are not checked before classifier is built
    (use with caution).

* `-num-decimal-places`

    The number of decimal places for the output of numbers in the model (default 4).

* `-batch-size`

    The desired batch size for batch prediction  (default 100).
