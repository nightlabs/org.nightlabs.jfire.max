package org.nightlabs.jfire.jbpm.graph.def;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.idgenerator.IDGenerator;

public abstract class AbstractActionHandler
implements ActionHandler
{
	/**
	 * This variable name references the toString()-representation of the {@link ObjectID} which
	 * references the instance of {@link Statable} for which the {@link ProcessInstance} has been
	 * created.
	 */
	public static final String VARIABLE_NAME_STATABLE_ID = "statableID";

	private PersistenceManager persistenceManager = null;

	protected PersistenceManager getPersistenceManager()
	{
		if (persistenceManager == null)
			persistenceManager = new Lookup(IDGenerator.getOrganisationID()).getPersistenceManager();

		return persistenceManager;
	}

	private ObjectID statableID = null;

	protected ObjectID getStatableID()
	{
		if (statableID == null) {
			String statableIDStr = (String) executionContext.getVariable(VARIABLE_NAME_STATABLE_ID);
			statableID = ObjectIDUtil.createObjectID(statableIDStr);
		}
		return statableID;
	}

	private Statable statable = null;

	protected Statable getStatable()
	{
		if (statable == null)
			statable = (Statable) getPersistenceManager().getObjectById(getStatableID());

		return statable;
	}

	private ExecutionContext executionContext;

	private void clear()
	{
		persistenceManager = null;
		statableID = null;
		statable = null;
		executionContext = null;
	}

	/**
	 * Do not override this! Implement {@link #doExecute(ExecutionContext)} instead.
	 */
	public final void execute(ExecutionContext executionContext)
	throws Exception
	{
		this.executionContext = executionContext;
		try {
			// we implement this method and delegate to doExecute in order to allow for later pre-/post-work
			doExecute(executionContext);
		} finally {
			clear();
		}
	}

	protected abstract void doExecute(ExecutionContext executionContext)
	throws Exception; 
}
