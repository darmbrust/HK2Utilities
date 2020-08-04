Release command looks like:

Note that this project is currently configured to release to maven central, via sonatype.  You must have a sonatype account to do this, and 
a GPG signing key (publicly registered).  See http://central.sonatype.org/pages/apache-maven.html#deploying-to-ossrh-with-apache-maven-introduction

If you do not want to release to that repo, don't enable the publicRelease profile, and set the parameter -DnoDeploy

mvn jgitflow:release-start jgitflow:release-finish -DreleaseVersion=1.6.0 -DdevelopmentVersion=1.6.1-SNAPSHOT -PpublicRelease
