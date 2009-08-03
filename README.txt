The JFireJbpmAOP plug-in must be placed inside the JFireJbpmEAR.ear and it must be named
JFireJbpmAOP.aop (and *not* JFireJbpmAOP.jar). Since it contains only one single class
and our build process does not easily allow for the required file extension ".aop",
we currently place it into the EAR in a manually exported, binary form.

Chairat Kongarayawetchakun <chairat at nightlabs dot com>
2009-08-03
