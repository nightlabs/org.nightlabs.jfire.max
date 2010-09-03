/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

package org.eclipse.datatools.connectivity.oda.jfire;



// Referenced classes of package org.eclipse.datatools.connectivity.oda:
//            OdaException

public interface IParameterMetaData
{

    public abstract int getParameterCount()
        throws JFireOdaException;

    public abstract int getParameterMode(int i)
        throws JFireOdaException;

    public abstract String getParameterName(int i)
        throws JFireOdaException;

    public abstract int getParameterType(int i)
        throws JFireOdaException;

    public abstract String getParameterTypeName(int i)
        throws JFireOdaException;

    public abstract int getPrecision(int i)
        throws JFireOdaException;

    public abstract int getScale(int i)
        throws JFireOdaException;

    public abstract int isNullable(int i)
        throws JFireOdaException;

    public static final int parameterModeUnknown = 0;
    public static final int parameterModeIn = 1;
    public static final int parameterModeInOut = 2;
    public static final int parameterModeOut = 3;
    public static final int parameterNullableUnknown = 0;
    public static final int parameterNoNulls = 1;
    public static final int parameterNullable = 2;
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