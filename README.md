# Meka

The MEKA project provides an open source implementation of methods for multi-label learning and evaluation.

http://meka.sourceforge.net/

## Using Meka

See the `Tutorial.pdf` for detailed information on obtaining, using and extending MEKA.
For a list of included methods and command line examples for them, 
	see: http://meka.sourceforge.net/methods.html


## Release Notes for (the Upcoming) Version 1.9.1

Improvements since the last release, for the up and coming release (several of these thanks to Joerg Wicker):

* Evaluation can handle missing values
* New classifiers
* `BR` now runs faster on large datasets
* PCC now outputs probabilistic info (as it should)
* Bug fix with labelset print-outs in evaluation at particular verbosity levels
* Classifier `BaggingMLUpdateableADWIN` removed to free dependence of MOA
* `-T` option is now available for incremental classifiers, evaluating the
  classifier in its current state (or after training with `-t` finished) on
  the test set provided with this option.
* The loading of the test test in the *Classify* tab got moved into the menu,
  to make it more obvious.
* The *Classify* tab now allows the loading of serialized models and their
  evaluation against the loaded test set.
* The *Arff Viewer* got renamed to *Data Viewer* as it is a customized version
  of Weka's Arff Viewer, with correct visualization of the class attributes
  (also sports support for *recent files* and filechooser with directory
  shortcuts).
* ...

## TODO

Things on the to-do list for future versions of Meka:

* More user control to the `-verbosity` flag
* Support for multi-target regression
* Add Nemenyi test for latex saver
* Include a confusion matrix in output
* Use 0.5 threshold as default; and force 0.5 (or pre-set ad-hoc) whenever user has *not* supplied additionally the training set with the load-from-disk option
* Use `printf` to replace `doubleToString` throughout
* For incremental evaluation
	* Add option for prequentialbasic, prequentialwindow, window-based
	* Check options for split-percentage and supervision
	* Check size of the first evaluation window 
	* The trainset/split in the GUI could indicate how much of the data is used for the initial training set
	* Need to change info about window to sampling frequency
	* With `Randomize=true` -- randomize?
* Update Tutorial with newer references
* Package manager -- with Mulan as a package
* `PS` should take `-P 1` as the default
* Change `EnsembleML` to `Ensemble`
* `CC`
	* Add an option to `CNode.java` to use the distribution information, rather than the nominal value, as an attribute.
	* use `Range` to specify a fixed chain in the options
* Add `multitarget.RAkELd`
* The 'type' (`ML`,`MT`,`CV`) is not a very elegant way to do things
* Check the use of Filters with Meka classifiers
* Wrapper for Clus
* Use a matrix for storing all values in `Result` (sparse matrix in the case of multi-label).
* Generate Markdown from the classifier code (e.g., the globalInfo, tipText and technical info)
* Better confidence outputs for multi-target methods, the full distribution should be available
* More classifiers!

