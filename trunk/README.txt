See the Tutorial.pdf for detailed information on obtaining, using and extending MEKA.

Release Notes:

* This release fixes some GUI bugs:
	- conflicts affecting the list of weka.classifiers vs meka.classifiers
	- selecting a multi-label classifier as a base classifier (in, e.g., BaggingML)
	- save and save-as an ARFF file in the Explorer

* Other recent changes (also affecting the last few releases since 1.5.0):
	- The CC code is being consolidated, the CC.java code will eventually be replaced by the CCe.java code (CC-'extended').
      (if you are writing a CC-based classifier, extend CCe for now) -- so far results are identical.
	- Similar applies for MCCe.
	- The PS code is being upgraded. PSe is the new code, PS should represent exactly results in the relevant papers
	  (they results are currently not identical, but these differences are due to randomness -- and not statistically significant)
	- Many small changes to evaluation (prettier output, more efficient, new verbosity option)
	- Some evaluation options can no longer be put into the arff files -- this was causing problems under some combinations.

