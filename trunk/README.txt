See the Tutorial.pdf for detailed information on obtaining, using and extending MEKA.
For a list of included methods and 'quick-start' command line examples, 
	see: http://meka.sourceforge.net/methods.html

Release Notes
-------------

This is a relatively minor release. There are no fundamental changes to the internals.

However, many classifiers have been revised in some way since the last release, and there are several new classifiers:

	- CT: classifier trellis
	- CDT: conditional depndency trellis
	- HASEL: a RAkEL-like algorithm, that partitions labelsets based on a dataset defined hierarchy

Also, there have been several small but helpful changes to the GUI:

	- An indicator of when an experiment is in progress
	- Label variables are set in *bold* to distinguish from the input/attribute variables
	- Cleaner output
	- Bookmarks in the load-file dialog.
	- Minor bug fixes
	- Default classifiers have been set sensibly (not ZeroR for everything as before)
	- Some 'missing' classifier options are now present for manipulation

And a lot of minor changes throughout

	- Sensible default classifiers for the command line 
	- Updated tutorial 
	- Improved documentation all-around (tutorial, javadoc and code comments
			and list of methods with examples: http://meka.sourceforge.net/methods.html)
