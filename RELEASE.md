RELEASE
=======

The release making process is a two-stage process:

1. Deploy artifacts to Maven Central
2. Create release archive

Prerequisites
-------------

- You need a Sonatype account, which you can create here:

  https://central.sonatype.com/publishing/

- The Sonatype user needs to be added to the MEKA project - **TODO**

- Artifacts get signed using GPG, so you need to have a GPG key and deploy it

  - Create a GPG key

    http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven/

  - Set up Maven (pom.xml and your Maven profile)

    http://central.sonatype.org/pages/apache-maven.html#gpg-signed-components


Deploying artifacts
-------------------

- Ensure all changes have been committed and all tests succeed

- update `\version` tag in `src/main/latex/Tutorial/Tutorial.tex`

- switch to Java 11

- run the following command (and accept or change the version numbers):
  ```
  mvn release:prepare release:perform
  ```
- log into sonatype

  https://central.sonatype.com/publishing/deployments

- click on *Publish* if valid, otherwise *Drop*

- wait till the *Deployment* switches from *Publishing* to *Published*, keep
  refreshing via the *Refresh* button

- *Release* the repository with a message like *new release X.Y.Z*

- The artifacts will get transferred to Maven Central in the background, should happen
  within a few minutes. You can check the following URL whether they have been pushed out:

  https://repo1.maven.org/maven2/net/sf/meka/meka/

- log out of sonatype


Troubleshooting
---------------

- In case artifacts shouldn't get deployed for some reason (give it a few hours), go
  to the following URL and open a ticket:

  https://issues.sonatype.org/


Release archive
---------------

- once the artifacts have been deployed successfully and are available from
  Maven Central, you can create a release archive

- update the version in the `release.xml` file to match the version of the just
  released Meka artifacts (also remove the `-SNAPSHOT` suffix) and commit changes

- run the following command to create the release archive in the `target` directory:
  ```
  mvn -f release.xml clean install
  ```

- update the version in the `release.xml` to the new `-SNAPSHOT` version of the
  `pom.xml` file and commit changes

- upload the release archive to sf.net (sftp://frs.sourceforge.net/home/frs/project/meka/) 
  and github (https://github.com/Waikato/meka/releases/)



Update documentation
--------------------

* update artifact version (`maven.md`)
* add bullet point in `news.md`, pointing to new release
* Update methods using the following command (add any additional methods to git),
  which will place markdown files in the `docs` directory for each classifier:

  ```
  meka.doc.OutputClassHierarchyMarkdown -skip-title -output-dir ./docs -superclass meka.classifiers.multilabel.MultiLabelClassifier
  ```
  
  Replace the TOC items in `mkdocs.yml` below `Methods` with the ones output
  on stdout from the above command. 

* any changes committed trigger a rebuild of the documentation via  
  [this](https://github.com/Waikato/weka-wiki/blob/master/.github/workflows/main.yml)
  [Github Action](https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions)
  