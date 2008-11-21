This zip is build from the ReportEngine folder of the runtime package of BIRT.
It has three directories:
  configuration
  lib
  plugins

Unfortunately there are some changes required.

Register jars from lib:
  All jars from the lib directory need to be registered in 
  the application.xml with the correct jar version.

The oda.jar: 
  The oda.jar was build by taking the oda.jar of the datatools project
  that is deployed with BIRT (the bundle jar with the package and version), 
  and removing all entries except the interfaces directly in the package
  org.eclipse.datatools.connectivity.oda.
  Additionally the interfaces have to be removed from this bundle jar
  in order to preven classloading problems
  
Classloader configuration: 
  In the config.ini within the configuration directory a section should
  be added that configures the BIRT OSGI runtime to use the parent
  class-loader first:
  osgi.parentClassloader=fwk

Hide the birt runtime for the remote classloader
  Place a file calle clrepository.xml in the directories 
  configuration and plugins.
  Use the following content:
  
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE classloader-repository
PUBLIC "-//NightLabs GmbH//DTD ClassLoader Repository 1.1//EN"
"http://www.nightlabs.de/dtd/clrepository_1_1.dtd">

<classloader-repository>
	<publish ignore="true"/>
</classloader-repository>


Then tar.gz all into the file birt-runtime.tar.gz (note that the tar should have a subfolder birt)

 