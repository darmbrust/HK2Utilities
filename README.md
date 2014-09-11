HK2Utilities
============

Runtime scanning and initializing utilities for HK2 so you don't have to rely on hk2-locator/default files created by the inhabitant-generator.

To enable runtime scanning and HK2 configuration - near the beginning of your program - probably in the main(String[] args) method - do this:

HK2RuntimeInitializer.init("Your Locator Name", false, "my.package.one", "some.other.package"));


This will scan the specified packages (on the current classpath) looking for any @Service annotated classes, and configure them into HK2 properly.

Note - with versions of HK2 prior to 2.3.0, the default code above is impacted by https://java.net/jira/browse/HK2-187 - there is an alternate implementation - 
HK2RuntimeInitializerCustom - which is not impacted by this bug - but it has other limitations.  See the javadoc for details.

With HK2 2.3.0 and newer, the alternate implementation / workaround is no longer necessary.
