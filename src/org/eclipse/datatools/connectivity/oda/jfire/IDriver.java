/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

package org.eclipse.datatools.connectivity.oda.jfire;



// Referenced classes of package org.eclipse.datatools.connectivity.oda:
//            OdaException, IConnection, LogConfiguration

public interface IDriver
{

    public abstract IConnection getConnection(String s)
        throws JFireOdaException;

    public abstract void setLogConfiguration(LogConfiguration logconfiguration)
        throws JFireOdaException;

    public abstract int getMaxConnections()
        throws JFireOdaException;

    public abstract void setAppContext(Object obj)
        throws JFireOdaException;
}


/*
	DECOMPILATION REPORT

	Decompiled from: E:\Java\Workspaces\JFire-Max-Trunk_2010_06_28\target-jee-jfire-max-jboss\target\server\default\lib\oda.jar
	Total time: 16 ms
	Jad reported messages/errors:
The class file version is 49.0 (only 45.3, 46.0 and 47.0 are supported)
	Exit status: 0
	Caught exceptions:
*/