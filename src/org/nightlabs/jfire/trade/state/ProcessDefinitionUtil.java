package org.nightlabs.jfire.trade.state;

import java.io.IOException;
import java.net.URL;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
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

		// we add the events+actionhandlers
		ActionHandlerNodeEnter.register(jbpmProcessDefinition);
//		Action action = new Action(new Delegation(ActionHandlerNodeEnter.class.getName()));
//		action.setName(ActionHandlerNodeEnter.class.getName());
//
//		Event event = new Event("node-enter");
//		event.addAction(action);
//
//		jbpmProcessDefinition.addEvent(event);

		return ProcessDefinition.storeProcessDefinition(pm, null, jbpmProcessDefinition);
	}
}
