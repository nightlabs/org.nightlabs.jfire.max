/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

package org.eclipse.datatools.connectivity.oda.jfire;



// Referenced classes of package org.eclipse.datatools.connectivity.oda:
//            OdaException, IConnection, IResultSet

public interface IDataSetMetaData
{

    public abstract IConnection getConnection()
        throws JFireOdaException;

    public abstract IResultSet getDataSourceObjects(String s, String s1, String s2, String s3)
        throws JFireOdaException;

    public abstract int getDataSourceMajorVersion()
        throws JFireOdaException;

    public abstract int getDataSourceMinorVersion()
        throws JFireOdaException;

    public abstract String getDataSourceProductName()
        throws JFireOdaException;

    public abstract String getDataSourceProductVersion()
        throws JFireOdaException;

    public abstract int getSQLStateType()
        throws JFireOdaException;

    public abstract boolean supportsMultipleResultSets()
        throws JFireOdaException;

    public abstract boolean supportsMultipleOpenResults()
        throws JFireOdaException;

    public abstract boolean supportsNamedResultSets()
        throws JFireOdaException;

    public abstract boolean supportsNamedParameters()
        throws JFireOdaException;

    public abstract boolean supportsInParameters()
        throws JFireOdaException;

    public abstract boolean supportsOutParameters()
        throws JFireOdaException;

    public abstract int getSortMode();

    public static final int sqlStateXOpen = 0;
    public static final int sqlStateSQL99 = 1;
    public static final int sortModeNone = 0;
    public static final int sortModeSingleOrder = 1;
    public static final int sortModeColumnOrder = 2;
    public static final int sortModeSingleColumn = 3;
}


/*
	DECOMPILATION REPORT

	Decompiled from: E:\Java\Workspaces\JFire-Max-Trunk_2010_06_28\target-jee-jfire-max-jboss\target\server\default\lib\oda.jar
	Total time: 0 ms
	Jad reported messages/errors:
The class file version is 49.0 (only 45.3, 46.0 and 47.0 are supported)
	Exit status: 0
	Caught exceptions:
*/