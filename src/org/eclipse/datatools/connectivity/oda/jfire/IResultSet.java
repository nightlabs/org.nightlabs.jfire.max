/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

package org.eclipse.datatools.connectivity.oda.jfire;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

// Referenced classes of package org.eclipse.datatools.connectivity.oda:
//            OdaException, IResultSetMetaData, IBlob, IClob

public interface IResultSet
{

    public abstract IResultSetMetaData getMetaData()
        throws JFireOdaException;

    public abstract void close()
        throws JFireOdaException;

    public abstract void setMaxRows(int i)
        throws JFireOdaException;

    public abstract boolean next()
        throws JFireOdaException;

    public abstract int getRow()
        throws JFireOdaException;

    public abstract String getString(int i)
        throws JFireOdaException;

    public abstract String getString(String s)
        throws JFireOdaException;

    public abstract int getInt(int i)
        throws JFireOdaException;

    public abstract int getInt(String s)
        throws JFireOdaException;

    public abstract double getDouble(int i)
        throws JFireOdaException;

    public abstract double getDouble(String s)
        throws JFireOdaException;

    public abstract BigDecimal getBigDecimal(int i)
        throws JFireOdaException;

    public abstract BigDecimal getBigDecimal(String s)
        throws JFireOdaException;

    public abstract Date getDate(int i)
        throws JFireOdaException;

    public abstract Date getDate(String s)
        throws JFireOdaException;

    public abstract Time getTime(int i)
        throws JFireOdaException;

    public abstract Time getTime(String s)
        throws JFireOdaException;

    public abstract Timestamp getTimestamp(int i)
        throws JFireOdaException;

    public abstract Timestamp getTimestamp(String s)
        throws JFireOdaException;

    public abstract IBlob getBlob(int i)
        throws JFireOdaException;

    public abstract IBlob getBlob(String s)
        throws JFireOdaException;

    public abstract IClob getClob(int i)
        throws JFireOdaException;

    public abstract IClob getClob(String s)
        throws JFireOdaException;

    public abstract boolean getBoolean(int i)
        throws JFireOdaException;

    public abstract boolean getBoolean(String s)
        throws JFireOdaException;

    public abstract Object getObject(int i)
        throws JFireOdaException;

    public abstract Object getObject(String s)
        throws JFireOdaException;

    public abstract boolean wasNull()
        throws JFireOdaException;

    public abstract int findColumn(String s)
        throws JFireOdaException;
}


/*
	DECOMPILATION REPORT

	Decompiled from: E:\Java\Workspaces\JFire-Max-Trunk_2010_06_28\target-jee-jfire-max-jboss\target\server\default\lib\oda.jar
	Total time: 15 ms
	Jad reported messages/errors:
The class file version is 49.0 (only 45.3, 46.0 and 47.0 are supported)
	Exit status: 0
	Caught exceptions:
*/