See the Tutorial.pdf for detailed information on obtaining, using and extending MEKA.
For a list of included methods and 'quick-start' command line examples, 
	see: http://meka.sourceforge.net/methods.html

Release Notes, Version 1.7.6
----------------------------

This is a minor release, fixing a few minor issues. 

	- Updateable classifiers are now moved to subfolders incremental/ and incremental/meta
	- Updateable classifiers are now set with a sensible default classifier (HoeffdingTree), and BRUpdateable in the case of meta incremental classifiers
	- Javadoc comments are cleaned up
	- Some unused branches of weka/ and moa/ were removed
	- Some overly stringent unit tests were changed
	- Recent changes are reflected in the tutorial

