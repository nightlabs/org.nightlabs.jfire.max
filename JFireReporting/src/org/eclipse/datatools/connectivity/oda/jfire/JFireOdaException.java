/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

package org.eclipse.datatools.connectivity.oda.jfire;



public class JFireOdaException extends Exception
{

    public JFireOdaException()
    {
        m_isCauseSet = false;
        m_cause = null;
        m_sqlState = null;
        m_vendorCode = 0;
        m_nextException = null;
    }

    public JFireOdaException(String message)
    {
        super(message);
        m_isCauseSet = false;
        m_cause = null;
        m_sqlState = null;
        m_vendorCode = 0;
        m_nextException = null;
    }

    public JFireOdaException(String message, String sqlState)
    {
        super(message);
        m_isCauseSet = false;
        m_cause = null;
        m_sqlState = sqlState;
        m_vendorCode = 0;
        m_nextException = null;
    }

    public JFireOdaException(String message, String sqlState, int vendorCode)
    {
        super(message);
        m_isCauseSet = false;
        m_cause = null;
        m_sqlState = sqlState;
        m_vendorCode = vendorCode;
        m_nextException = null;
    }

    public JFireOdaException(Throwable cause)
    {
        m_isCauseSet = false;
        m_cause = null;
        initCause(cause);
    }

    public String getSQLState()
    {
        return m_sqlState;
    }

    public int getErrorCode()
    {
        return m_vendorCode;
    }

    public JFireOdaException getNextException()
    {
        return m_nextException;
    }

    public void setNextException(JFireOdaException nextException)
    {
        JFireOdaException ex;
        for(ex = this; ex.m_nextException != null; ex = ex.m_nextException);
        ex.m_nextException = nextException;
    }

    public Throwable initCause(Throwable cause)
        throws IllegalArgumentException, IllegalStateException
    {
        if(m_isCauseSet)
            throw new IllegalStateException("initCause() cannot be called multiple times.");
        if(this == cause)
        {
            throw new IllegalArgumentException("This cannot be caused by itself.");
        } else
        {
            m_cause = cause;
            m_isCauseSet = true;
            return this;
        }
    }

    public Throwable getCause()
    {
        return m_cause;
    }

    public String toString()
    {
        String message = super.toString();
        Throwable cause = getCause();
        if(cause != null)
        {
            String causeString = cause.toString();
            if(causeString != null && causeString.length() > 0)
                message = (new StringBuilder(String.valueOf(message))).append(" ;\n    ").append(causeString).toString();
        }
        if(getNextException() != null)
            message = (new StringBuilder(String.valueOf(message))).append(" ;\n    ").append(getNextException()).toString();
        return message;
    }

    private static final long serialVersionUID = 1L;
    private String m_sqlState;
    private int m_vendorCode;
    private JFireOdaException m_nextException;
    private boolean m_isCauseSet;
    private Throwable m_cause;
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