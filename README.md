# Meka

The MEKA project provides an open source implementation of methods for multi-label learning and evaluation.

http://waikato.github.io/meka/

## Documentation

See http://waikato.github.io/meka/documentation/ for sources of documentation regarding MEKA.

In particular, 

* See the `Tutorial.pdf` for detailed information on obtaining, using and extending MEKA.
* For a list of included methods and command line examples for them, see: http://meka.sourceforge.net/methods.html
* For examples on how to use MEKA in your Java code: https://github.com/Waikato/meka/tree/master/src/main/java/mekaexamples

If you have a specific question, ask on Meka's mailing list

* Check if it is already answered: http://sourceforge.net/mailarchive/forum.php?forum_name=meka-list
* Write it to meka-list@lists.sourceforge.net

## Changes in Version 1.9.1

* Added a folder `mekaexamples` with examples of how to use Meka from Java code
* Evaluation can handle missing values
* `BR` now runs faster on large datasets
* `PCC` now outputs probabilistic info (as it should)
* Bug fix with labelset print-outs in evaluation at particular verbosity levels
* Classifier `BaggingMLUpdateableADWIN` removed to free dependence of MOA
* `-T` option is now available for incremental classifiers, evaluating the
  classifier in its current state (or after training with `-t` finished) on
  the test set provided with this option.
* The loading of the test test in the *Classify* tab got moved into the menu,
  to make it more obvious.
* The *Classify* tab now allows the loading of serialized models and their
  evaluation against the loaded test set.
* The *Classify* tab now allows to make predictions on a loaded test set
  using the selected model from the result history.
* The *Arff Viewer* got renamed to *Data Viewer* as it is a customized version
  of Weka's Arff Viewer, with correct visualization of the class attributes
  (also sports support for *recent files* and filechooser with directory
  shortcuts).
* New classifiers (Boolean Matrix Factorization, Neurofuzzy methods)
* Added `-predictions` option to evaluation (batch and incremental) to 
  allow output of predictions generated on test set to a file. Using the
  `-no-eval` option, the evaluation can be skipped, e.g., when there are no
  class labels in the test set.
* Added an 'Export Predictions (CSV)' plugin option to the GUI to save
  all predictions along with true label relevances to a CSV file 
* Moved issues in the TODO section of this README to github as Issues

## Bugs, and Future Enhancements

A list of current Issues in Meka (known bugs, planned improvements, feature wishlist) can be found at https://github.com/Waikato/meka/issues

The Meka developers never have enough time to implement everything that should be in Meka. If you have made some Meka-related code you would like to see in Meka, or would like to help with any of the existing issues, please get in touch with the developers. 


