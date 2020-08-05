HK2Utilities
============

[![Build Status](https://travis-ci.org/darmbrust/HK2Utilities.svg?branch=develop)](https://travis-ci.org/darmbrust/HK2Utilities)

Runtime scanning and initializing utilities for HK2 so you don't have to rely on hk2-locator/default files created by the inhabitant-generator.

To enable runtime scanning and HK2 configuration - near the beginning of your program - probably in the main(String[] args) method - do this:

HK2RuntimeInitializer.init("Your Locator Name", false, "my.package.one", "some.other.package"));

This will scan the specified packages (on the current classpath) looking for any @Service annotated classes, and configure them into HK2 properly.

From version 1.6 on of this software, only JDK11 or newer is supported.