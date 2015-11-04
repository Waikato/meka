RELEASE
=======

The release making process is a two-stage process:

1. Deploy artifacts to Maven Central
2. Create release archive

Prerequisites
-------------

- You need a Sonatype account, which you can create here:

  https://issues.sonatype.org/

- The Sonatype user needs to be added to the MEKA project, just open the
  following ticket again and specify the user that needs adding:

  https://issues.sonatype.org/browse/OSSRH-9969

- Artifacts get signed using GPG, so you need to have a GPG key and deploy it

  - Create a GPG key

    http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven/

  - Set up Maven (pom.xml and your Maven profile)

    http://central.sonatype.org/pages/apache-maven.html#gpg-signed-components


Deploying artifacts
-------------------

- Ensure all changes have been committed and all tests succeed

- run the following command (and accept or change the version numbers):
  ```
  mvn release:prepare release:perform
  ```
- log into sonatype

  https://oss.sonatype.org/

- select the *Staging repositories*

- scroll down and check the *netsfmeka* repository

- check deployed artifacts on the *Content* tab (bottom of the screen) using the
  *Archive Browser* once a jar has been selected

- *Close* the repository (top of the screen) with a message like *new release X.Y.Z*

- artifacts will get prepared for being release, which will take some time, keep
  clicking on *Refresh* till the *Release* action is available and *Close* is disabled

- *Release* the repository with a message like *new release X.Y.Z*

- The artifacts will get transferred to Maven Central in the background, should happen
  within 15min. You can check the following URL whether they have been pushed out:

  http://repo1.maven.org/maven2/net/sf/meka/meka/

- The artifacts might not be searchable through http://search.maven.org/ for a few
  hours, as the search index only gets updated every 3 hours or so

- log out of sonatype


Troubleshooting
---------------

- In case artifacts shouldn''t get deployed for some reason (give it a few hours), go
  to the following URL and open a ticket:

  https://issues.sonatype.org/


Release archive
---------------

- once the artifacts have been deployed successfully and are available from
  Maven Central, you can create a release archive

- update the version in the `release.xml` file to match the version of the just
  released Meka artifacts (also remove the -SNAPSHOT suffix) and commit changes

- run the following command to create the release archive in the `target` directory:
  ```
  mvn -f release.xml clean install
  ```

- update the version in the `release.xml` to the new `-SNAPSHOT` version of the
  `pom.xml` file and commit changes

- upload the release archive to sf.net



