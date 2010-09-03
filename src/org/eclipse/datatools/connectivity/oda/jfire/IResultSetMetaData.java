/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

package org.eclipse.datatools.connectivity.oda.jfire;



// Referenced classes of package org.eclipse.datatools.connectivity.oda:
//            OdaException

public interface IResultSetMetaData
{

    public abstract int getColumnCount()
        throws JFireOdaException;

    public abstract String getColumnName(int i)
        throws JFireOdaException;

    public abstract String getColumnLabel(int i)
        throws JFireOdaException;

    public abstract int getColumnType(int i)
        throws JFireOdaException;

    public abstract String getColumnTypeName(int i)
        throws JFireOdaException;

    public abstract int getColumnDisplayLength(int i)
        throws JFireOdaException;

    public abstract int getPrecision(int i)
        throws JFireOdaException;

    public abstract int getScale(int i)
        throws JFireOdaException;

    public abstract int isNullable(int i)
        throws JFireOdaException;

    public static final int columnNoNulls = 0;
    public static final int columnNullable = 1;
    public static final int columnNullableUnknown = 2;
}


/*
	DECOMPILATION REPORT

	Decompiled from: E:\Java\Workspaces\JFire-Max-Trunk_2010_06_28\target-jee-jfire-max-jboss\target\server\default\lib\oda.jar
	Total time: 32 ms
	Jad reported messages/errors:
The class file version is 49.0 (only 45.3, 46.0 and 47.0 are supported)
	Exit status: 0
	Caught exceptions:
*/