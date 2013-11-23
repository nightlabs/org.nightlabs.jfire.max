/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

package org.eclipse.datatools.connectivity.oda.jfire;

import java.io.Reader;

// Referenced classes of package org.eclipse.datatools.connectivity.oda:
//            OdaException

public interface IClob
{

    public abstract Reader getCharacterStream()
        throws JFireOdaException;

    public abstract String getSubString(long l, int i)
        throws JFireOdaException;

    public abstract long length()
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