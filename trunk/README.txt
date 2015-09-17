See the Tutorial.pdf for detailed information on obtaining, using and extending MEKA.
For a list of included methods and 'quick-start' command line examples, 
	see: http://meka.sourceforge.net/methods.html

Release Notes, Version 1.7.8
----------------------------

Improvements since the last version are as follows:

	* MEKA now uses maven (instead of ant, as previously) for compilation. 0w00

	* MEKA's build has been switched over from Apache Ant to Apache Maven. 

		Mainly to make life easier with deploying artifacts to Maven Central automatically, (rather than by hand), 
		and better execution of unit tests. 
		This change affects people working with the source code.

	* The seed used to randomize a dataset is no longer passed on to Randomizeable classifiers -- they must use their own.

		This means that the results of some Randomizeable classifier will be different now when a dataset is randomized.

	* Evaluation output is improved
   
		Evaluation output has been improved, as much in the code, as the visual text output. 
		Basically, objects like doubles[] can be stored, rather than just Strings and Doubles.
		The improvement is overall, but in particular noticeable improvements to cross validation and incremental validation.
		Incremental validation now also displays per-window metrics in the GUI in addition to those of the final window.
		Cross-fold evaluation now combines all predictions together and then evaluates it (rather than averaging the statistics afterwards).

	* Incremental evaluation is now basic prequential (interleaved train then test) 

		The earlier evaluation scheme (which was window-based prequential) is still available in the code, 

	* AUPRC and AUROC are added as evaluation metrics

	* It is easier now to add functionality to result history objects. 
	
		The Classify tab now automatically discovers its result history plugins at runtime. 
		These have to be derived from "meka.gui.explorer.classify.AbstractClassifyResultHistoryPlugin" and placed in the "meka.gui.explorer.classify" package. 
		The current functionality (show graph, save graph and save model) are using this architecture

	* The Explorer tabs are now plugins and get discovered dynamically at runtime. 
	
		This makes it easy for other people to add more tabs (i.e., meka packages), simply derived from
		"meka.gui.explorer.AbstractExplorerTab" and placed in the "meka.gui.explorer" package.

	* A GUIChooser class is now available: meka.gui.guichooser.GUIChooser
		
		This is in preparation for a future Experimenter. It features dynamic discovery of menu items as well: 
		they need to be derived from "meka.gui.guichooser.AbstractMenuItemDefinition" and placed in package "meka.gui.guichooser". 
		If you want a "shortcut" button like the Explorer menu definition has, simply let the "isShortcutButton()" method return true.

	* The MultilabelClassifier class is now (more appropriately) renamed ProblemTransformationMethod, and there is now a MultiLabelClassifier Interface.

	* Tutorial updated to reflecct changes

	* Minor bug fixes

