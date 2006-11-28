package org.nightlabs.jfire.trade.state;

import java.io.IOException;
import java.net.URL;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;

public class ProcessDefinitionUtil
{
	protected ProcessDefinitionUtil()
	{
	}

	public static ProcessDefinition storeProcessDefinition(PersistenceManager pm, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition = ProcessDefinition.readProcessDefinition(jbpmProcessDefinitionURL);

		// TODO we should add the events+actionhandlers here!

		return ProcessDefinition.storeProcessDefinition(pm, null, jbpmProcessDefinition);
	}
}
