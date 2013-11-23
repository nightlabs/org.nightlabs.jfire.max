/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

package org.eclipse.datatools.connectivity.oda.jfire;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

// Referenced classes of package org.eclipse.datatools.connectivity.oda:
//            OdaException, IResultSetMetaData, IResultSet, IParameterMetaData, 
//            SortSpec

public interface IQuery
{

    public abstract void prepare(String s)
        throws JFireOdaException;

    public abstract void setAppContext(Object obj)
        throws JFireOdaException;

    public abstract void setProperty(String s, String s1)
        throws JFireOdaException;

    public abstract void close()
        throws JFireOdaException;

    public abstract void setMaxRows(int i)
        throws JFireOdaException;

    public abstract int getMaxRows()
        throws JFireOdaException;

    public abstract IResultSetMetaData getMetaData()
        throws JFireOdaException;

    public abstract IResultSet executeQuery()
        throws JFireOdaException;

    public abstract void clearInParameters()
        throws JFireOdaException;

    public abstract void setInt(String s, int i)
        throws JFireOdaException;

    public abstract void setInt(int i, int j)
        throws JFireOdaException;

    public abstract void setDouble(String s, double d)
        throws JFireOdaException;

    public abstract void setDouble(int i, double d)
        throws JFireOdaException;

    public abstract void setBigDecimal(String s, BigDecimal bigdecimal)
        throws JFireOdaException;

    public abstract void setBigDecimal(int i, BigDecimal bigdecimal)
        throws JFireOdaException;

    public abstract void setString(String s, String s1)
        throws JFireOdaException;

    public abstract void setString(int i, String s)
        throws JFireOdaException;

    public abstract void setDate(String s, Date date)
        throws JFireOdaException;

    public abstract void setDate(int i, Date date)
        throws JFireOdaException;

    public abstract void setTime(String s, Time time)
        throws JFireOdaException;

    public abstract void setTime(int i, Time time)
        throws JFireOdaException;

    public abstract void setTimestamp(String s, Timestamp timestamp)
        throws JFireOdaException;

    public abstract void setTimestamp(int i, Timestamp timestamp)
        throws JFireOdaException;

    public abstract void setBoolean(String s, boolean flag)
        throws JFireOdaException;

    public abstract void setBoolean(int i, boolean flag)
        throws JFireOdaException;

    public abstract void setObject(String s, Object obj)
        throws JFireOdaException;

    public abstract void setObject(int i, Object obj)
        throws JFireOdaException;

    public abstract void setNull(String s)
        throws JFireOdaException;

    public abstract void setNull(int i)
        throws JFireOdaException;

    public abstract int findInParameter(String s)
        throws JFireOdaException;

    public abstract IParameterMetaData getParameterMetaData()
        throws JFireOdaException;

//    public abstract void setSortSpec(SortSpec sortspec)
//        throws OdaException;

//    public abstract SortSpec getSortSpec()
//        throws OdaException;

//    public abstract void setSpecification(QuerySpecification queryspecification)
//        throws OdaException, UnsupportedOperationException;
//
//    public abstract QuerySpecification getSpecification();
//
//    public abstract String getEffectiveQueryText();
//
//    public abstract void cancel()
//        throws OdaException, UnsupportedOperationException;
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