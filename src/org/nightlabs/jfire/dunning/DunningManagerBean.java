package org.nightlabs.jfire.dunning;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.book.AccountantDelegate;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.dunning.book.BookDunningLetterMoneyTransfer;
import org.nightlabs.jfire.dunning.book.LocalBookDunningLetterAccountantDelegate;
import org.nightlabs.jfire.dunning.book.PartnerBookDunningLetterAccountantDelegate;
import org.nightlabs.jfire.dunning.id.DunningConfigCustomerID;
import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.dunning.id.DunningFeeAdderID;
import org.nightlabs.jfire.dunning.id.DunningFeeTypeID;
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.id.AnchorID;
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

	//DunningConfigCustomer
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public DunningConfigCustomer storeDunningConfigCustomer(DunningConfigCustomer dunningConfigCustomer, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, dunningConfigCustomer, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<DunningConfigCustomer> getDunningConfigCustomers(Collection<DunningConfigCustomerID> dunningConfigCustomerIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, dunningConfigCustomerIDs, DunningConfigCustomer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<DunningConfigCustomerID> getDunningConfigCustomerIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(DunningConfigCustomer.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
		} finally {
			pm.close();
		}
	}

	//DunningProcess
	@RolesAllowed("_Guest_")
	@Override
	public DunningProcess getDunningProcess(DunningProcessID dunningProcessID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(DunningProcess.class);
			return (DunningProcess) pm.detachCopy(pm.getObjectById(dunningProcessID));
		} finally {
			pm.close();
		}
	}

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

	//DunningFeeType
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public DunningFeeType storeDunningFeeType(DunningFeeType dunningFeeType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, dunningFeeType, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<DunningFeeType> getDunningFeeTypes(Collection<DunningFeeTypeID> dunningFeeTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, dunningFeeTypeIDs, DunningFeeType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<DunningFeeTypeID> getDunningFeeTypeIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(DunningFeeType.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	public void processDunning(DunningConfig dunningConfig, Date date)
	throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			DunningAutoMode autoMode = dunningConfig.getDunningAutoMode();
			if (autoMode != DunningAutoMode.none) {
				//All Overdue Invoices
				Collection<Invoice> overdueInvoices = 
					Invoice.getOverdueInvoices(pm, dunningConfig.getOrganisationID(), new Date());
				for (Invoice inv : overdueInvoices) {
					LegalEntity customer = inv.getCustomer();
					Currency currency = inv.getCurrency();

					//Get DunningProcess
					DunningProcess dunningProcess = 
						DunningProcess.getDunningProcessByCustomerAndCurrency(pm, (AnchorID)JDOHelper.getObjectId(customer), (CurrencyID)JDOHelper.getObjectId(currency));
					if (dunningProcess == null/* || !dunningProcess.isActive()*/) { //TODO: do we need to check if it's active? if there is the dunning process?
						//Get DunningConfig for the Customer
						DunningConfig customerDunningConfig = DunningConfigCustomer.getDunningConfigByCustomer(pm, (AnchorID)JDOHelper.getObjectId(customer));
						//Create new DunningProcess
						dunningProcess = 
							new DunningProcess(dunningConfig.getOrganisationID(), IDGenerator.nextIDString(DunningProcess.class), customerDunningConfig == null ? dunningConfig : customerDunningConfig);
					}

					dunningProcess.processInvoice(inv, date);
					pm.makePersistent(dunningProcess);
				}

				//Generate Letters
				Collection<DunningProcess> activeDunningProcesses = DunningProcess.getActiveDunningProcessesByDunningConfig(pm, (DunningConfigID)JDOHelper.getObjectId(dunningConfig));
				for (DunningProcess activeProcess : activeDunningProcesses) {
					if (activeProcess.getCoolDownEnd().before(new Date())) {
						boolean isFinalized = autoMode == DunningAutoMode.createAndFinalize;
						activeProcess.createDunningLetter(isFinalized);
						pm.makePersistent(activeProcess);
					}
				}
			}
		}
		finally {
			pm.close();
		}
	}
	
	@RolesAllowed("_Guest_")
	public void processAutomaticDunning(TaskID taskID)
	throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			Task task = (Task) pm.getObjectById(taskID);
			DunningConfig dunningConfig =  (DunningConfig) task.getParam();
			processDunning(dunningConfig, new Date()); //TODO remove hardcoding!?
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
		String organisationIDStr = getOrganisationID();
		String baseName = "org.nightlabs.jfire.dunning.resource.messages";
		ClassLoader loader = DunningManagerBean.class.getClassLoader();

		DunningInterestCalculator dunningInterestCalculator;
		DunningFeeAdder dunningFeeAdder;
		try {
			pm.getExtent(DunningFeeAdderCustomerFriendly.class);
			// check, whether the datastore is already initialized
			try {
				pm.getObjectById(DunningFeeAdderCustomerFriendly.ID);
				return; // already initialized
			} catch (JDOObjectNotFoundException x) {
				//datastore not yet initialized
				dunningFeeAdder = pm.makePersistent(new DunningFeeAdderCustomerFriendly(organisationIDStr, DunningFeeAdderCustomerFriendly.ID.dunningFeeAdderID));
				
				pm.getExtent(DunningInterestCalculator.class);
				dunningInterestCalculator = pm.makePersistent(new DunningInterestCalculatorCustomerFriendly(organisationIDStr, DunningInterestCalculatorCustomerFriendly.ID.dunningInterestCalculatorID));
				
				pm.getExtent(DunningFeeType.class);
				DunningFeeType defaultDunningFeeType = new DunningFeeType(organisationIDStr, IDGenerator.nextID(DunningFeeType.class));
				defaultDunningFeeType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.dunning.DunningFeeType.default.name");
				defaultDunningFeeType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.dunning.DunningFeeType.default.description");
				defaultDunningFeeType = pm.makePersistent(defaultDunningFeeType);
				
				pm.getExtent(DunningConfig.class);
				DunningConfig defaultDunningConfig = new DunningConfig(organisationIDStr, IDGenerator.nextIDString(DunningConfig.class), DunningAutoMode.createAndFinalize);
				defaultDunningConfig.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.dunning.DunningConfig.default.name");
				defaultDunningConfig.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.dunning.DunningConfig.default.description");
				
				
				//Step1
				ProcessDunningStep processStep1 = new ProcessDunningStep(organisationIDStr, IDGenerator.nextIDString(AbstractDunningStep.class), defaultDunningConfig, 1);
				processStep1.addFeeType(defaultDunningFeeType);

				InvoiceDunningStep invStep1 = new InvoiceDunningStep(organisationIDStr, IDGenerator.nextIDString(AbstractDunningStep.class), defaultDunningConfig, 1);
				invStep1.setPeriodOfGraceMSec(TimeUnit.DAYS.toMillis(31));
				invStep1.setInterestPercentage(new BigDecimal(0));

				//Step2
				ProcessDunningStep processStep2 = new ProcessDunningStep(organisationIDStr, IDGenerator.nextIDString(AbstractDunningStep.class), defaultDunningConfig, 2);
				processStep2.addFeeType(defaultDunningFeeType);

				InvoiceDunningStep invStep2 = new InvoiceDunningStep(organisationIDStr, IDGenerator.nextIDString(AbstractDunningStep.class), defaultDunningConfig, 2);
				invStep2.setPeriodOfGraceMSec(TimeUnit.DAYS.toMillis(31));
				invStep2.setInterestPercentage(new BigDecimal(4));

				//Step3
				ProcessDunningStep processStep3 = new ProcessDunningStep(organisationIDStr, IDGenerator.nextIDString(AbstractDunningStep.class), defaultDunningConfig, 3);

				InvoiceDunningStep invStep3 = new InvoiceDunningStep(organisationIDStr, IDGenerator.nextIDString(AbstractDunningStep.class), defaultDunningConfig, 3);
				invStep3.setPeriodOfGraceMSec(TimeUnit.DAYS.toMillis(31));
				invStep3.setInterestPercentage(new BigDecimal(4));

				//
				defaultDunningConfig.addInvoiceDunningStep(invStep1);
				defaultDunningConfig.addInvoiceDunningStep(invStep2);
				defaultDunningConfig.addInvoiceDunningStep(invStep3);

				defaultDunningConfig.addProcessDunningStep(processStep1);
				defaultDunningConfig.addProcessDunningStep(processStep2);
				defaultDunningConfig.addProcessDunningStep(processStep3);

				defaultDunningConfig.setDunningInterestCalculator(dunningInterestCalculator);
				defaultDunningConfig.setDunningFeeAdder(dunningFeeAdder);

				pm.makePersistent(defaultDunningConfig);
				// Create the process definitions.
				defaultDunningConfig.readProcessDefinition(DunningConfig.class.getResource("jbpm/letter/"));
				
				defaultDunningConfig.initTimerTask();
				
//				DunningConfigCustomer dcc = new DunningConfigCustomer(dccID.organisationID, dccID.dunningConfigCustomerID, defaultDunningConfig, LegalEntity.getAnonymousLegalEntity(pm));
//				pm.makePersistent(dcc);
				
				Accounting accounting = Accounting.getAccounting(pm);
				
				AccountantDelegate localBookDunningLetterAccountantDelegate = new LocalBookDunningLetterAccountantDelegate(accounting.getMandator(), IDGenerator.nextIDString(AccountantDelegate.class)); 
				accounting.getLocalAccountant().setAccountantDelegate(BookDunningLetterMoneyTransfer.class, localBookDunningLetterAccountantDelegate);
				
				AccountantDelegate partnerBookDunningLetterAccountantDelegate = new PartnerBookDunningLetterAccountantDelegate(organisationIDStr, IDGenerator.nextIDString(AccountantDelegate.class));
				accounting.getPartnerAccountant().setAccountantDelegate(BookDunningLetterMoneyTransfer.class, partnerBookDunningLetterAccountantDelegate);
				
				pm.makePersistent(accounting);
			}
		} finally {
			pm.close();
		}
	}
}