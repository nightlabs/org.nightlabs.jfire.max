package org.nightlabs.jfire.jbpm.graph.def;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.SecurityReflector;

public abstract class AbstractActionHandler
implements ActionHandler
{
	protected static class LastNodeEnterTransitionCarrier
	{
		public String transitionName;
	}

	private static final long serialVersionUID = 1L;

	/**
	 * This variable name references the toString()-representation of the {@link ObjectID} which
	 * references the instance of {@link Statable} for which the {@link ProcessInstance} has been
	 * created.
	 */
	public static final String VARIABLE_NAME_STATABLE_ID = "statableID";

	private static ThreadLocal<Map<String,LastNodeEnterTransitionCarrier>> organisationID2lastNodeEnterTransitionCarrier = new ThreadLocal<Map<String,LastNodeEnterTransitionCarrier>>() {
		@Override
		protected Map<String,LastNodeEnterTransitionCarrier> initialValue()
		{
			return new HashMap<String, LastNodeEnterTransitionCarrier>();
		}
	};

////	// TODO JPOX WORKAROUND: We should get only one real PersistenceManager within one transaction
////	// so that every of the "virtual" PersistenceManagers sees the same data. Currently this is not
////	// the case (they use separate data), so we bind the PersistenceManager here to a ThreadLocal.
////	private static ThreadLocal<PersistenceManager> persistenceManagerThreadLocal = new ThreadLocal<PersistenceManager>();
//	private static ThreadLocal<Map<String, PersistenceManager>> persistenceManagerThreadLocal = new ThreadLocal<Map<String,PersistenceManager>>() {
//		@Override
//		protected Map<String, PersistenceManager> initialValue()
//		{
//			return new HashMap<String, PersistenceManager>();
//		}
//	};

	private static LastNodeEnterTransitionCarrier getLastNodeEnterTransitionCarrier() {
		Map<String,LastNodeEnterTransitionCarrier> m = organisationID2lastNodeEnterTransitionCarrier.get();
		String currentOrganisationID = IDGenerator.getOrganisationID();
		LastNodeEnterTransitionCarrier lastNodeEnterTransitionCarrier = m.get(currentOrganisationID);
		if (lastNodeEnterTransitionCarrier == null) {
			lastNodeEnterTransitionCarrier = new LastNodeEnterTransitionCarrier();
			m.put(currentOrganisationID, lastNodeEnterTransitionCarrier);
		}
		return lastNodeEnterTransitionCarrier;
	}

	/**
	 * This method allows to find out which transition was used for the last node-enter event. It is managed
	 * on a per-Thread base. A typical use case is to leave a Node via a certain transition depending on the
	 * name of the transition that was used to enter it (e.g. simply the same name).
	 *
	 * @return Returns either <code>null</code>, if <code>ActionHandlerNodeEnter</code> was never triggered on
	 *		the current thread or the name of the last transition that was used to enter a node on the current thread.
	 */
	public static String getLastNodeEnterTransitionName() {
		return getLastNodeEnterTransitionCarrier().transitionName;
	}

	public static void setLastNodeEnterTransitionName(String transitionName) {
		getLastNodeEnterTransitionCarrier().transitionName = transitionName;
	}

	private PersistenceManager persistenceManager = null;

	protected PersistenceManager getPersistenceManager()
	{
//		// TODO JPOX WORKAROUND: begin
//		// If the PMF would return a delegate to the SAME PM in all calls within the same transaction,
//		// it would be sufficient to cache it only in the local object instance (solely for performance reasons).
//		// But since we get multiple PMs which don't see each others data, we must ensure that we always work with the same one.
//		String currentOrganisationID = null;
//		if (persistenceManager == null) {
//			currentOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
//			persistenceManager = persistenceManagerThreadLocal.get().get(currentOrganisationID);
//		}
//
//		if (persistenceManager != null && persistenceManager.isClosed())
//			persistenceManager = null;
//
//		if (persistenceManager == null) {
//			if (currentOrganisationID == null)
//				currentOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
//
//			persistenceManager = new Lookup(currentOrganisationID).getPersistenceManager();
//
//			persistenceManagerThreadLocal.get().put(currentOrganisationID, persistenceManager);
//		}
//		// TODO JPOX WORKAROUND: end

		if (persistenceManager == null) {
			String currentOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			persistenceManager = new Lookup(currentOrganisationID).createPersistenceManager();
		}

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
		if (persistenceManager != null) {
			try {
				persistenceManager.close();
			} catch (Exception x) {
				Logger.getLogger(AbstractActionHandler.class).error("Exception while closing PersistenceManager!", x);
			}
		}

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
