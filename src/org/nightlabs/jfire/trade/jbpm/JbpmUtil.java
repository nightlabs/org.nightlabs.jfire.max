package org.nightlabs.jfire.trade.jbpm;

import java.io.IOException;
import java.net.URL;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;

public class JbpmUtil
{
	protected JbpmUtil() { }

	public static ProcessDefinition storeProcessDefinition(PersistenceManager pm, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition = ProcessDefinition.readProcessDefinition(jbpmProcessDefinitionURL);

		// we add the events+actionhandlers
		ActionHandlerNodeEnter.register(jbpmProcessDefinition);

		// store it
		return ProcessDefinition.storeProcessDefinition(pm, null, jbpmProcessDefinition);
	}
}
