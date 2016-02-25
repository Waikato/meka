# Meka

The MEKA project provides an open source implementation of methods for multi-label learning and evaluation.

http://meka.sourceforge.net/

## Using Meka

See the `Tutorial.pdf` for detailed information on obtaining, using and extending MEKA.
For a list of included methods and command line examples for them, 
	see: http://meka.sourceforge.net/methods.html


## Release Notes, Version 1.9.1

Improvements since the last version:

* Evaluation can handle missing values
* New classifiers (thanks to Joerg!)
* PCC now outputs probabilistic info (as it should)
* Bug fix with labelset print-outs in evaluation
* ...

## TODO

A list of points flagged for improvement in future versions of Meka:

* Add more user control to the `-verbosity` flag
* Support for multi-target regression
* Nemenyi test for latex saver
* Output confusion matrix
* Use 0.5 threshold as default; and force 0.5 (or preset ad-hoc) whenever user has *not* supplied additionally the training set wiht load
* Use `printf`-style printing instead of `Utils.doubleToString` output.
* Use a matrix for storing all values in Result (sparse matrix in the case of multi-label).
* Incremental evaluation
	* Add option for prequentialbasic, prequentialwindow, window-based
	* Check options for split-percentage and supervision
	* Check, the first window is/may be a different size
	* The trainset/split in the GUI could indicate how much of the data is used for the initial training set
	* need to change info about window to sampling frequency
	* With `Randomize=true` -- randomize?
* Update Tutorial with newer references
* Package manager -- with Mulan as a package
* PS should take `-P 1` as the default
* Change `EnsembleML` to `Ensemble`
* CC
	* Add an option to `CNode.java` to use the distribution information, rather than the nominal value, as an attribute.
	* use `Range` to specify a fixed chain in the options!
* Add `multitarget.RAkELd`
* The 'type' (`ML`,`MT`,`CV`) is not a very elegant way to do things
* Check the use of Filters
* Wrapper for Clus
* Generate Markdown from the classifier code (e.g., the globalInfo, tipText and technical info)
* Better confidence outputs for multi-target methods, the full distribution should be available
* More classifiers!

