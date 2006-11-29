package org.nightlabs.jfire.jbpm.graph.def;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.idgenerator.IDGenerator;

public abstract class AbstractActionHandler
implements ActionHandler
{
	private PersistenceManager persistenceManager;

	protected PersistenceManager getPersistenceManager()
	{
		if (persistenceManager == null)
			persistenceManager = new Lookup(IDGenerator.getOrganisationID()).getPersistenceManager();

		return persistenceManager;
	}

	/**
	 * Do not override this! Implement {@link #doExecute(ExecutionContext)} instead.
	 */
	public void execute(ExecutionContext executionContext)
	throws Exception
	{
		// we implement this method and delegate to doExecute in order to allow for later pre-/post-work
		doExecute(executionContext);
	}

	protected abstract void doExecute(ExecutionContext executionContext)
	throws Exception; 
}
