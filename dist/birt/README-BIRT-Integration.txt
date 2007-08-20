lib directory for the JFire BIRT integration:

Except for the oda.jar all jars come as they are from the lib directory
of the BIRT runtime deployment package and all are registered as <module><java>
in JFireReporting's application.xml. 

The oda.jar was build by taking the oda.jar of the datatools project,
that is deployed with BIRT, and removing all entries except the interfaces
directly in org.eclipse.datatools.connectivity.oda.
Doing so allows JFireReporting to use the interfaces defined there. Although
the BIRT report generation and rendering needs classes out of the oda.jar and 
will first try to load them with the J2EE classloader (osgi.parentClassloader=fwk 
must be set in the config.ini), 
the interfaces (hopefully) don't have outside dependencies and won't make problems 
if loaded already by the J2EE classloader and not the BIRT-OSGI Bundle-classloader.

The interfaces (or better all files) in the package org.eclipse.datatools.connectivity.oda 
must be additionally removed from the org.eclipse.datatools.connectivity.oda.jar provided 
in the plugins directory and the export of this package has to be removed from the 
Manifest.MF in this plugin as well.

