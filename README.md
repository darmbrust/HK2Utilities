HK2Utilities
============

[![Build Status](https://travis-ci.org/darmbrust/HK2Utilities.svg?branch=develop)](https://travis-ci.org/darmbrust/HK2Utilities) [![Dependency Status](https://www.versioneye.com/user/projects/5a83a8a10fb24f7034ac6399/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/5a83a8a10fb24f7034ac6399)

Runtime scanning and initializing utilities for HK2 so you don't have to rely on hk2-locator/default files created by the inhabitant-generator.

To enable runtime scanning and HK2 configuration - near the beginning of your program - probably in the main(String[] args) method - do this:

HK2RuntimeInitializer.init("Your Locator Name", false, "my.package.one", "some.other.package"));

This will scan the specified packages (on the current classpath) looking for any @Service annotated classes, and configure them into HK2 properly.

Release command looks like:

Note that this project is currently configured to release to maven central, via sonatype.  You must have a sonatype account to do this, and 
a GPG signing key (publicly registered).  See http://central.sonatype.org/pages/apache-maven.html#deploying-to-ossrh-with-apache-maven-introduction

If you do not want to release to that repo, don't enable the publicRelease profile, and set the parameter -DnoDeploy

mvn jgitflow:release-start jgitflow:release-finish -DreleaseVersion=1.6.0 -DdevelopmentVersion=1.6.1-SNAPSHOT -PpublicRelease
