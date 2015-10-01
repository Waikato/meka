See the Tutorial.pdf for detailed information on obtaining, using and extending MEKA.
For a list of included methods and 'quick-start' command line examples, 
	see: http://meka.sourceforge.net/methods.html

Release Notes, Version 1.7.8
----------------------------

Improvements since the last version are as follows:

	* MEKA's build has been switched over from Apache Ant to Apache Maven. 

		- Note: this change affects people working with the source code.
		- Makes life easier with deploying artifacts to Maven Central automatically
		- Better execution of unit tests. 

	* The Evaluation has been heavily reworked
   
		- Evaluation output has been improved, as much in the code, as the visual text output (now prettier). 
		- AUPRC and AUROC are added as evaluation metrics
		- AUPRC and AUROC can be visualised with the 'Show ...' options under the right-click menu of the History panel.
		- Objects like doubles[] can be stored, rather than just Strings and Doubles.
		- In particular there are improvements to cross validation and incremental validation.
		- Cross-fold evaluation now combines all predictions together and then evaluates it (rather than averaging the statistics afterwards).
		- Incremental evaluation is basic prequential (interleaved train then test) with a GUI option for the number of samples
		- Incremental validation displays metrics in the GUI sampled over time in addition to those overall. 
			These can be plotted with by selecting 'Incremental Performance' from the right-click menu in the History panel.
		- Note: the earlier evaluation scheme (which was window-based prequential) is still available in the code. 

	* The seed used to randomize a dataset is no longer passed on to Randomizeable classifiers -- they must use their own.

		- This means that the results of the `Randomizeable` classifiers will be different now when a dataset is randomized.

	* It is easier now to add functionality to Result History objects. 
	
		- The Classify tab now automatically discovers its result history plugins at runtime. 
		- These have to be derived from `meka.gui.explorer.classify.AbstractClassifyResultHistoryPlugin` and placed in the `meka.gui.explorer.classify` package. 
		- New functionality (Show Graph, Save Graph, Save Model, Save CSV, Incremental Performance, Show ROC, etc.) is using this architecture

	* The Explorer tabs are now plugins and get discovered dynamically at runtime. 
	
		- This makes it easy for other people to add more tabs (i.e., meka packages), simply derived from `meka.gui.explorer.AbstractExplorerTab` 
			and placed in the `meka.gui.explorer` package.

	* A GUIChooser class is now available: `meka.gui.guichooser.GUIChooser`
		
		- This is in preparation for a future Experimenter. 
		- It features dynamic discovery of menu items as well: 
			- They need to be derived from `meka.gui.guichooser.AbstractMenuItemDefinition` and placed in package `meka.gui.guichooser`. 
			- If you want a "shortcut" button like the Explorer menu definition has, simply let the `isShortcutButton()` method return true.

	* Experimenter in development

	* The `MultilabelClassifier` class is now (more appropriately) renamed `ProblemTransformationMethod`, and there is now a `MultiLabelClassifier` Interface.

	* Tool tips and get/set options thoroughly elabourated throughout classifiers, and respective javadoc comments cleaned up

	* Tutorial updated to reflect changes

	* A number of minor bug fixes, e.g., 

		- bug fixed in PSt when empty labelset appears


