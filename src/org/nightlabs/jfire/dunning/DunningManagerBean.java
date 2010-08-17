package org.nightlabs.jfire.dunning;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.dunning.id.DunningFeeAdderID;
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An EJB session bean provides methods for managing every objects used in the dunning.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class DunningManagerBean extends BaseSessionBeanImpl
implements DunningManagerRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(DunningManagerBean.class);

	//DunningConfig
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public DunningConfig storeDunningConfig(DunningConfig dunningConfig, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, dunningConfig, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}
	
	@RolesAllowed("_Guest_")
	@Override
	public List<DunningConfig> getDunningConfigs(Collection<DunningConfigID> dunningConfigIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, dunningConfigIDs, DunningConfig.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<DunningConfigID> getDunningConfigIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(DunningConfig.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
		} finally {
			pm.close();
		}
	}
	
	//DunningProcess
	@RolesAllowed("_Guest_")
	@Override
	public List<DunningProcess> getDunningProcesses(Collection<DunningProcessID> dunningProcessIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, dunningProcessIDs, DunningProcess.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<DunningProcessID> getDunningProcessIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(DunningProcess.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
		} finally {
			pm.close();
		}
	}
	
	//DunningFeeAdder
	@RolesAllowed("_Guest_")
	@Override
	public DunningFeeAdder getDunningFeeAdder(DunningFeeAdderID dunningFeeAdderID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(DunningFeeAdder.class);
			return (DunningFeeAdder) pm.detachCopy(pm.getObjectById(dunningFeeAdderID));
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<DunningFeeAdder> getDunningFeeAdders(Set<DunningFeeAdderID> dunningFeeAdderIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, dunningFeeAdderIDs, DunningFeeAdder.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	//DunningInterestCalculator
	@RolesAllowed("_Guest_")
	@Override
	public DunningInterestCalculator getDunningInterestCalculator(DunningInterestCalculatorID dunningInterestCalculatorID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(DunningInterestCalculator.class);
			return (DunningInterestCalculator) pm.detachCopy(pm.getObjectById(dunningInterestCalculatorID));
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<DunningInterestCalculator> getDunningInterestCalculators(Set<DunningInterestCalculatorID> dunningInterestCalculatorIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, dunningInterestCalculatorIDs, DunningInterestCalculator.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void initTimerTaskAutomaticDunning(PersistenceManager pm)
	throws Exception
	{
//		try {
//			TaskID _taskID = TaskID.create(getOrganisationID(), DunningConfig.TASK_TYPE_ID_PROCESS_DUNNING, taskID);
//			Task task = (Task) pm.getObjectById(_taskID);
//		} finally {
//			pm.close();
//		}
	}
	
	public void processAutomaticDunning(TaskID taskID)
	throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			for (Iterator<DunningProcess> dunningProcesses = pm.getExtent(DunningProcess.class).iterator(); dunningProcesses.hasNext(); ) {
				DunningProcess dunningProcess = dunningProcesses.next();
				if (!getOrganisationID().equals(dunningProcess.getOrganisationID()))
					continue;
				
			}
		} finally {
			pm.close();
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(DunningFeeAdder.class);
			// check, whether the datastore is already initialized
			try {
				pm.getObjectById(DunningFeeAdderCustomerFriendly.ID);
				return; // already initialized
			} catch (JDOObjectNotFoundException x) {
				// datastore not yet initialized
			}

			// create and persist the AccountTypes
			DunningFeeAdder dunningFeeAdder;
			dunningFeeAdder = pm.makePersistent(new DunningFeeAdderCustomerFriendly(DunningFeeAdderCustomerFriendly.ID.organisationID, DunningFeeAdderCustomerFriendly.ID.dunningFeeAdderID));
			
			pm.getExtent(DunningInterestCalculator.class);
			
			DunningInterestCalculator dunningInterestCalculator;
			dunningInterestCalculator = pm.makePersistent(new DunningInterestCalculatorCustomerFriendly(DunningInterestCalculatorCustomerFriendly.ID.organisationID, DunningInterestCalculatorCustomerFriendly.ID.dunningInterestCalculatorID));
		} finally {
			pm.close();
		}
	}
}