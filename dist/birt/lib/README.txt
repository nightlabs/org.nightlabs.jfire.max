lib directory for the JFire BIRT integration:

Except for the oda.jar all jars come as they are from the lib directory
of the BIRT runtime deployment package and all are registered as <module><java>
in JFireReporting's application.xml. 

The oda.jar was build by taking the oda.jar of the databools project,
that is deployed with BIRT, and removing all entries except the interfaces
directly in org.eclipse.datatools.connectivity.oda.
Doing so allows JFireReporting to use the interfaces defined their. Although
the BIRT report generation and rendering needs classes out of the oda.jar and 
will first try to load them with the J2EE classloader (osgi.parentClassloader=fwk), 
the interfaces (hopefully) don't have outside dependencies and won't make problems 
if loaded already by the J2EE classloader and not the BIRT-OSGI Bundle-classloader.

