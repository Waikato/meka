# meka.classifiers.multilabel.incremental.PSUpdateable

## Synopsis
Updateable PS

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
* `-I <value>`

    Sets the buffer size
    default: 1000

* `-support <value>`

    Sets the max. num. of combs.
    default: 10

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
    (default: weka.classifiers.trees.HoeffdingTree)

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

* **Options specific to classifier weka.classifiers.trees.HoeffdingTree:**

* `-L`

    The leaf prediction strategy to use. 0 = majority class, 1 = naive Bayes, 2 = naive Bayes adaptive.
    (default = 2)

* `-S`

    The splitting criterion to use. 0 = Gini, 1 = Info gain
    (default = 1)

* `-E`

    The allowable error in a split decision - values closer to zero will take longer to decide
    (default = 1e-7)

* `-H`

    Threshold below which a split will be forced to break ties
    (default = 0.05)

* `-M`

    Minimum fraction of weight required down at least two branches for info gain splitting
    (default = 0.01)

* `-G`

    Grace period - the number of instances a leaf should observe between split attempts
    (default = 200)

* `-N`

    The number of instances (weight) a leaf should observe before allowing naive Bayes to make predictions (NB or NB adaptive only)
    (default = 0)

* `-P`

    Print leaf models when using naive Bayes at the leaves.
