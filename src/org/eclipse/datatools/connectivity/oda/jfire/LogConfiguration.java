/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

package org.eclipse.datatools.connectivity.oda.jfire;


public class LogConfiguration
{

    public LogConfiguration(String dataSourceId, int logLevel, String logDirectory, String logPrefix, String formatterClassName)
    {
        m_dataSourceId = dataSourceId;
        setLogConfiguration(logLevel, logDirectory, logPrefix, formatterClassName);
    }

    public LogConfiguration(int logLevel, String logDirectory, String logPrefix, String formatterClassName)
    {
        setLogConfiguration(logLevel, logDirectory, logPrefix, formatterClassName);
    }

    LogConfiguration()
    {
    }

    private void setLogConfiguration(int logLevel, String logDirectory, String logPrefix, String formatterClassName)
    {
        m_logLevel = logLevel;
        if(logDirectory != null && logDirectory.length() == 0)
            logDirectory = null;
        m_logDirectory = logDirectory;
        if(logPrefix != null && logPrefix.length() == 0)
            logPrefix = null;
        m_logPrefix = logPrefix;
        if(formatterClassName != null && formatterClassName.length() == 0)
            formatterClassName = null;
        m_formatterClassName = formatterClassName;
    }

    public String getDataSourceId()
    {
        return m_dataSourceId;
    }

    public String getFormatterClassName()
    {
        return m_formatterClassName;
    }

    public String getLogDirectory()
    {
        return m_logDirectory;
    }

    public int getLogLevel()
    {
        return m_logLevel;
    }

    public String getLogPrefix()
    {
        return m_logPrefix;
    }

    private String m_dataSourceId;
    private int m_logLevel;
    private String m_logDirectory;
    private String m_logPrefix;
    private String m_formatterClassName;
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