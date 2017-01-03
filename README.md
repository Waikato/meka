# Meka

The MEKA project provides an open source implementation of methods for multi-label learning and evaluation.

http://meka.sourceforge.net/

## Documentation

See http://meka.sourceforge.net/#documentation for sources of documentation regarding MEKA.

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

## TODO

The following is a list of changes in mind for future versions of Meka. 

The Meka developers never have enough time to implement everything that should be in Meka. If you have made some Meka-related code you would like to see in Meka, or would like to help with any of the following list, please get in touch with the developers. 

* More user control to the `-verbosity` flag
* Support for multi-target regression
* Add Nemenyi test for latex saver
* Add documentation to Tutorial regarding `./src/main/resources/meka/gui/goe/MekaPropertiesCreator.props`
* Include a confusion matrix in output
* Use 0.5 threshold as default; and force 0.5 (or pre-set ad-hoc) whenever user has *not* supplied additionally the training set with the load-from-disk option
* For incremental evaluation
	* Add option for prequentialbasic, prequentialwindow, window-based
	* Check options for split-percentage and supervision
	* Check size of the first evaluation window 
	* The trainset/split in the GUI could indicate how much of the data is used for the initial training set
	* Need to change info about window to sampling frequency
	* With `Randomize=true` -- randomize?
* Update Tutorial with newer references
* Package manager -- with Mulan / Clus as packages
* `PS` should take `-P 1` as the default
* Change `EnsembleML` class to `Ensemble` (etc.)
* `CC`
	* Add an option to `CNode.java` to use the distribution information, rather than the nominal value, as an attribute.
	* use `Range` to specify a fixed chain in the options
* The 'type' (`ML`,`MT`,`CV`) should be rethought
* Use `printf` to replace `doubleToString` throughout
* Check the use of Filters with Meka classifiers
* Use a matrix for storing all values in `Result` (sparse matrix in the case of multi-label).
* Generate Markdown from the classifier code (e.g., the globalInfo, tipText and technical info)
* Better confidence outputs for multi-target methods, the full posterior distribution should be available
* Instances reader for multi-label libSVM datasets: http://www.csie.ntu.edu.tw/~cjlin/libsvmtools/datasets/multilabel.html
* Document in tutorial: 
	- new ways to save predictions, for both GUI (`Export Predictions to CSV`) and also from the CLI (`-predictions`), and note: not yet cross validation
	- classifier must be descendend of MultiLabelClassifier
	- should add new folder hierarchy in `./src/main/resources/meka/gui/goe/MekaPropertiesCreator.props` to see it in the GUI
	- document better usage of Maven and Eclipse
* Plugin/Wrapper for CLUS
* Autocreation of `eval.txt` was a bad idea (runtime is always different, becomes Modified file), should reverse this
* More classifiers!

