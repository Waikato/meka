See the Tutorial.pdf for detailed information on obtaining, using and extending MEKA.

Release Notes
-------------

This is a major release, most classifiers have been revised in some way since the last release. 
Changes involve rewrites to improve in effeciency, or just minor updates to the java documentation.

The main changes include,

	- Faster evaluation code
	- Faster implementation of most classifiers
	- New consolidated implementation of the CC and LC/PS family of classifiers
	- New classifier implementations, including
		* RAkEL 
		* RAkELd (disjoint sets)
	- Cleaner, more comprehensible output of evaluation stats

For example, the family of classifier chains methods (CC, PCC, MCC, BCC) now inherit common classes and make use of a common CNode and CCUtils class. 
This makes it much easier to share functionality among them.
Similar changes were made to the LP family (LC, PS, PSt), which now share tools in a PSUtils class, and have all been rewritten to use a LabelSet class.
This translates to scalability improvements for large labelsets.

Note that the predictive results of these classifiers may change minorly from those obtained by earlier versions, but these changes are not statistically significant.
