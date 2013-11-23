package org.nightlabs.jfire.jbpm.internal;

import java.io.Serializable;

import org.jbpm.JbpmConfiguration;

public class JbpmConfigurationCarrier
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private transient JbpmConfiguration jbpmConfiguration;

	public JbpmConfigurationCarrier(JbpmConfiguration jbpmConfiguration)
	{
		this.jbpmConfiguration = jbpmConfiguration;
	}

	public JbpmConfiguration getJbpmConfiguration()
	{
		return jbpmConfiguration;
	}
}
