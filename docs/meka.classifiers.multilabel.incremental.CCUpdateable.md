# meka.classifiers.multilabel.incremental.CCUpdateable

## Synopsis
Updateable CC
Must be run with an Updateable base classifier.

## BibTeX
```
@article{JesseRead2011,
   author = {Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank},
   journal = {Machine Learning Journal},
   number = {3},
   pages = {333-359},
   title = {Classifier Chains for Multi-label Classification},
   volume = {85},
   year = {2011}
}

@inproceedings{JesseRead2009,
   author = {Jesse Read, Bernhard Pfahringer, Geoff Holmes, Eibe Frank},
   booktitle = {20th European Conference on Machine Learning (ECML 2009). Bled, Slovenia, September 2009},
   title = {Classifier Chains for Multi-label Classification},
   year = {2009}
}
```
## Options
* `-S <value>`

    The seed value for randomizing the data.
    (default: 0)

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

**Options specific to classifier weka.classifiers.trees.HoeffdingTree:**

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
