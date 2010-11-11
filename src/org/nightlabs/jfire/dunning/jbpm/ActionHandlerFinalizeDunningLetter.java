package org.nightlabs.jfire.dunning.jbpm;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;

public class ActionHandlerFinalizeDunningLetter 
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doExecute(ExecutionContext executionContext)
			throws Exception {
		PersistenceManager pm = getPersistenceManager();
		
	}

}
