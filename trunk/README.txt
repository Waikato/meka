[Notes]
-VERSION refers to the current version of meka, e.g., -1.0

[Compiling]

To compile meka, and produce meka-VERSION.jar:
	ant jar

[Running]

For example, just type:
	java -cp "lib/*" weka.classifiers.multilabel.BR -t data/Music.arff -W weka.classifiers.bayes.NaiveBayes

Or, if you have meka-VERSION.jar and weka.jar in your classpath, just:
	java weka.classifiers.multilabel.BR -t data/Music.arff -W weka.classifiers.bayes.NaiveBayes

BR is the multi-label classifier, which in this case takes NaiveBayes as a base classifier. 

Run the following if you want to know what options are possible:
	java weka.classifiers.multilabel.BR -h

For starting the MEKA Explorer, use the following command:
- Windows
  start.bat
- Linux/Unix/Mac
  ./start.sh
