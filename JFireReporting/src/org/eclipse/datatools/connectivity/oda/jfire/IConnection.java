/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   IConnection.java

package org.eclipse.datatools.connectivity.oda.jfire;

import java.util.Locale;
import java.util.Properties;

// Referenced classes of package org.eclipse.datatools.connectivity.oda:
//            OdaException, IDataSetMetaData, IQuery

public interface IConnection
{

    public abstract void open(Properties properties)
        throws JFireOdaException;

    public abstract void setAppContext(Object obj)
        throws JFireOdaException;

    public abstract void close()
        throws JFireOdaException;

    public abstract boolean isOpen()
        throws JFireOdaException;

    public abstract IDataSetMetaData getMetaData(String s)
        throws JFireOdaException;

    public abstract IQuery newQuery(String s)
        throws JFireOdaException;

    public abstract int getMaxQueries()
        throws JFireOdaException;

    public abstract void commit()
        throws JFireOdaException;

    public abstract void rollback()
        throws JFireOdaException;

    public abstract void setLocale(Locale locale)
    	throws JFireOdaException;
//    public abstract void setLocale(ULocale ulocale)
//        throws OdaException;
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