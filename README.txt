Release Notes:
- This is a major release, after many changes. It will likely be followed up shortly with another release after
	- thorough testing
	- respective debugging
	- more documentation

Changes since the last version:
- All meka classifiers are now in meka.classifiers. (instead of weka.classifiers.)
- The CC code is being consolidated, the CC.java code will eventually be replaced by the CCe.java code (CC-'extended').
  (if you are writing a CC-based classifier, extend CCe for now.)
- MCC is not as fast as before, temporarily, due to the previous change -- this will be fixed.
  (this and several other classifiers have been affected by the use of the external 'CNode' class)
- Many small changes to evaluation (prettier output, more efficient, new verbosity option)
- Some evaluation options can no longer be put into the arff files -- this was causing problems under some combinations.
- See the Tutorial.pdf for detailed information on obtaining, using and extending MEKA.
