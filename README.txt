[Compiling]

To compile meka, and produce meka.jar:
	ant jar

[Running]

For example, just type:
	java -cp meka.jar:lib/weka.jar weka.classifiers.multilabel.BR -t data/Music.arff -W weka.classifiers.bayes.NaiveBayes

Or, if you have meka.jar and weka.jar in your classpath, just:
	java weka.classifiers.multilabel.BR -t data/Music.arff -W weka.classifiers.bayes.NaiveBayes

BR is the multi-label classifier, which in this case takes NaiveBayes as a base classifier. 

Run the following if you want to know what options are possible:
	java weka.classifiers.multilabel.BR -h
