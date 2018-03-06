A collection of multi-label and multi-target datasets is 
[available here](https://sourceforge.net/projects/meka/files/Datasets/).
Even more datasets are available at the [MULAN Website](http://mulan.sourceforge.net/datasets.html) 
(note that MULAN indexes labels as the final attributes, whereas MEKA indexs as the beginning). 
See the [MEKA Tutorial](https://sourceforge.net/projects/meka/files/meka-1.9.1/Tutorial.pdf) 
for more information.

The following text datasets have been created / compiled into WEKA's ARFF format 
using the [StringToWordVector](http://weka.sourceforge.net/doc/weka/filters/unsupervised/attribute/StringToWordVector.html) 
filter. Also available are [train/test splits](https://sourceforge.net/projects/meka/files/Datasets/Train-test%20Splits) 
and the [original raw prefiltered text](https://sourceforge.net/projects/meka/files/Datasets/Prefiltered/).

Dataset         | L   | N      | LC   | PU    |Description and Original Source(s)
----------------|----:|-------:|-----:|------:|----------------------------------
[Enron](http://sourceforge.net/projects/meka/files/Datasets/ENRON-F.arff/download) | 53  |   1702 | 3.39 | 0.442 | A subset of the [Enron Email Dataset](http://www-2.cs.cmu.edu/~enron/), as labelled by the [UC Berkeley Enron Email Analysis Project](http://bailando.sims.berkeley.edu/enron_email.html)
[Slashdot](http://sourceforge.net/projects/meka/files/Datasets/SLASHDOT-F.arff/download) | 22  |   3782 | 1.18 | 0.041 | Article titles and partial blurbs mined from [Slashdot.org](http://slashdot.org/search.pl)
[Language Log](http://sourceforge.net/projects/meka/files/Datasets/LLOG-F.arff/download) | 75  |   1460 | 1.18 | 0.208 | Articles posted on the [Language Log](http://languagelog.ldc.upenn.edu/nll/)
[IMDB (Updated)](http://sourceforge.net/projects/meka/files/Datasets/IMDB-F.arff/download) | 28  | 120919 | 2.00 | 0.037 | Movie plot text summaries labelled with genres sourced from the [Internet Movie Database](http://www.imdb.com/interfaces#plain) interface, labeled with genres.

Key:

* **N** = The number of examples (training+testing) in the datasets
* **L** = The number of predefined labels relevant to this dataset
* **LC** = Label Cardinality. Average number of labels assigned per document
* **PU** = Percentage of documents with Unique label combinations