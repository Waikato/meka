## Synopsis
Maniac - Multi-lAbel classificatioN using AutoenCoders.Transforms the labels using layers of autoencoders.For more information see:
J"org Wicker, Andrey Tyukin, Stefan Kramer: A Nonlinear Label Compression and Transformation Method for Multi-Label Classification using Autoencoders. In: The 20th Pacific Asia Conference on Knowledge Discovery and Data Mining (PAKDD), 328-340, 2016.

## BibTeX
```
@inproceedings{J"orgWicker2016,
   author = {J"org Wicker, Andrey Tyukin, Stefan Kramer},
   booktitle = {The 20th Pacific Asia Conference on Knowledge Discovery and Data Mining (PAKDD)},
   pages = {328-340},
   title = {A Nonlinear Label Compression and Transformation Method for Multi-Label Classification using Autoencoders},
   year = {2016}
}
```
## Options
* `-compression <value>`

    Compression factor of the autoencoders, each level of autoencoders will compress the labels to factor times previous layer size.
    (default: 0.85)

* `-numberAutoencoders <value>`

    Number of autoencoders, i.e. number of hidden layers +1. Note that this can be also used as the number of autoencoders to use in the optimization search, autoencoders will be added until this number is reached  and then the best configuration in terms of number of layers is selects.
    (default: 4)

* `-optimizeAE <value>`

    Optimize the number of layers of autoencoders. If set to true the number of layers will internally be optimized using a validation set.
    (default: false)

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
