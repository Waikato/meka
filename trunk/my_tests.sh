CP=".:./target/meka-1.0.jar:./lib/weka.jar:./lib/mulan.jar"
CP=".:./lib/*:./target/meka-1.0.jar"
# Incremental
#java -cp $CP weka.classifiers.multilabel.BRUpdateable -t ~/Data/EMOT.arff -W weka.classifiers.bayes.NaiveBayesUpdateable
# Mulan
#java -cp $CP weka.classifiers.multilabel.MULAN -t data/Music.arff -S BR -W weka.classifiers.bayes.NaiveBayes
echo export CLASSPATH=\"$CP\"
java weka.classifiers.multilabel.CC -f "test.meka" -R -t data/Music.arff -W weka.classifiers.functions.SMO -- -M
java weka.core.Result -f "test.meka"
rm "test.meka"

