package org.nightlabs.jfire.dunning;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.clone.DefaultCloneContext;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.CurrencyOrganisationDefault;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransferFactory;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransferFactoryJDO;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.book.AccountantDelegate;
import org.nightlabs.jfire.accounting.book.PartnerPayPayableObjectAccountantDelegate;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.dunning.book.BookDunningLetterMoneyTransfer;
import org.nightlabs.jfire.dunning.book.DunningLetterMoneyTransferFactoryJDO;
import org.nightlabs.jfire.dunning.book.LocalBookDunningLetterAccountantDelegate;
import org.nightlabs.jfire.dunning.book.PartnerBookDunningLetterAccountantDelegate;
import org.nightlabs.jfire.dunning.customerfriendly.DunningConfigInitialiserCustomerFriendly;
import org.nightlabs.jfire.dunning.id.DunningConfigCustomerID;
import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.dunning.id.DunningFeeAdderID;
import org.nightlabs.jfire.dunning.id.DunningFeeTypeID;
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.timepattern.TimePatternFormatException;
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
public class DunningManagerBean
	extends BaseSessionBeanImpl
	implements DunningManagerRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(DunningManagerBean.class);

	public static final String TASK_TYPE_ID_PROCESS_DUNNING = "DunningTask";
	public static final String TASK_ID_PROCESS_DUNNING = "Automatic_Dunning_Task";

	//*************************************************** 
	//****************** DunningConfig ******************
	//*************************************************** 
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
	
	@Override
	public DunningConfig getDefaultDunningConfigOfOrganisation(String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		pm.getFetchPlan().setFetchSize(maxFetchDepth);
		pm.getFetchPlan().setGroups(fetchGroups);
		try {
			return pm.detachCopy( DunningConfig.getDefaultDunningConfig(pm, getOrganisationID()) );
		} finally {
			pm.close();
		}
	}
	
	@Override
	public DunningConfig getDefaultDunningConfigOfOrganisation(String organisationID, String[] fetchGroups, int maxFetchDepth)
	{
		Organisation.assertValidOrganisationID(organisationID);
		PersistenceManager pm = createPersistenceManager();
		pm.getFetchPlan().setFetchSize(maxFetchDepth);
		pm.getFetchPlan().setGroups(fetchGroups);
		try
		{
			return pm.detachCopy( DunningConfig.getDefaultDunningConfig(pm, organisationID) );
		}
		finally
		{
			pm.close();
		}
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
	
	//*************************************************** 
	//************** DunningConfigCustomer **************
	//*************************************************** 
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

	//*************************************************** 
	//****************** DunningProcess *****************
	//*************************************************** 
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

	//*************************************************** 
	//***************** DunningFeeAdder *****************
	//*************************************************** 
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

	//*************************************************** 
	//************ DunningInterestCalculator ************
	//*************************************************** 
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

	//*************************************************** 
	//***************** DunningFeeType ******************
	//*************************************************** 
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

	@Override
	@RolesAllowed("_Guest_")
	public void processAutomaticDunning(TaskID taskID)
		throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try
		{
			Collection<Invoice> overdueInvoices = Invoice.getOverdueInvoices(pm, getOrganisationID(), new Date());
			for (Invoice inv : overdueInvoices)
			{
				LegalEntity customer = inv.getCustomer();
				Currency currency = inv.getCurrency();

				//Get DunningProcess
				DunningProcess dunningProcess = DunningProcess.getDunningProcessByCustomerAndCurrency(
						pm, getOrganisationID(), (AnchorID) JDOHelper.getObjectId(customer), 
						(CurrencyID) JDOHelper.getObjectId(currency)
				);
				
				// create new process if none exists yet.
				if (dunningProcess == null)
				{
					DunningConfig configTemplate = getConfigTemplateForCustomer(pm, customer);
					DunningConfig clonedConfig = new DefaultCloneContext().createClone(configTemplate);
					dunningProcess = new DunningProcess(getOrganisationID(), customer, currency, clonedConfig);
					dunningProcess = pm.makePersistent(dunningProcess);
				}

				if (! dunningProcess.isActive())
				{
					DunningConfig oldConfigToDelete = null;
					if (dunningProcess.getDunningConfig() != null)
					{
						logger.warn("Found a inactive DunningProcess that still has a DunningConfig assigned! Will override old " +
								"config. \n\t process = {} \n\t old config = {}", dunningProcess, dunningProcess.getDunningConfig()
						);
						// do a defered deletion as the getConfigTemplateForCustomer fires a query that would then require the
						// deletion to be flushed to DB before firing the query which in turn would result in a foreign key 
						// violation.
						oldConfigToDelete = dunningProcess.getDunningConfig();
					}
					
					DunningConfig configTemplate = getConfigTemplateForCustomer(pm, customer);
					DunningConfig clonedConfig = new DefaultCloneContext().createClone(configTemplate);
					dunningProcess.setDunningConfig(clonedConfig);
					if (oldConfigToDelete != null)
					{
						pm.deletePersistent(oldConfigToDelete);
					}
				}
				
				dunningProcess.addInvoice(inv);
			}

			//Generate Letters
			Collection<DunningProcess> activeDunningProcesses = DunningProcess.getActiveDunningProcesses(pm, getOrganisationID());
			if (activeDunningProcesses == null || activeDunningProcesses.size() == 0)
				return;
			
			for (DunningProcess activeProcess : activeDunningProcesses)
			{
				DunningLetter newlyCreatedLetter = activeProcess.doAutomaticDunning( User.getUser(pm, getPrincipal()) );
				
				if (newlyCreatedLetter != null && newlyCreatedLetter.isFinalized())
				{
					finalizeDunningLetter(newlyCreatedLetter, null);
				}
			}
		}
		finally
		{
			pm.close();
		}
	}
	
	// this needs to be published to the clients through a puplic method that handles the attaching stuff as well.
	protected void finalizeDunningLetter(DunningLetter dunningLetter, User finalizingUser)
	{
		// setFinalize -> create BookDunningLetterMoneyTransfer and delegate the money flow to it.
		if (dunningLetter == null)
			return;
		
		if (dunningLetter.isFinalized())
		{
			throw new IllegalArgumentException("Given DunningLetter was already finalized! given letter=" + dunningLetter);
		}
		
		// finalize, book & check for integrity of accounts
		PersistenceManager pm = createPersistenceManager();
		try
		{
			if (finalizingUser == null)
				finalizingUser = User.getUser(pm);
			
			dunningLetter.setFinalized(finalizingUser);
			
			BookDunningLetterMoneyTransfer bookingTransfer = new BookDunningLetterMoneyTransfer(
					finalizingUser, dunningLetter.getVendor(), dunningLetter.getCustomer(), dunningLetter
			);
			pm.makePersistent(bookingTransfer);
			HashSet<Anchor> involvedAnchors = new HashSet<Anchor>();
			boolean failed = true;
			try
			{
				bookingTransfer.bookTransfer(finalizingUser, involvedAnchors);
				Anchor.checkIntegrity(Collections.singleton(bookingTransfer), involvedAnchors);
				failed = false;
			}
			finally
			{
				if (failed)
					Anchor.resetIntegrity(Collections.singleton(bookingTransfer), involvedAnchors);
			}
		}
		finally
		{
			pm.close();
		}

		DunningConfig dunningConfig = dunningLetter.getDunningRun().getDunningProcess().getDunningConfig();
		List<DunningLetterNotifier> notifiers = dunningConfig.getLevel2LetterNotifiers().get(dunningLetter.getDunningLevel());
		if (notifiers != null)
		{
			for (DunningLetterNotifier notifier : notifiers)
				notifier.triggerNotifier(dunningLetter);
		}
	}

	private DunningConfig getConfigTemplateForCustomer(PersistenceManager pm, LegalEntity customer)
	{
		DunningConfig dunningConfig = null;
		//Get DunningConfig for the Customer
		dunningConfig = DunningConfigCustomer.getDunningConfigByCustomer(pm, (AnchorID)JDOHelper.getObjectId(customer));
		
		if (dunningConfig == null)
			dunningConfig = DunningConfig.getDefaultDunningConfig(pm, getOrganisationID());
		
		if (dunningConfig == null)
			throw new IllegalStateException("No default DunningConfig found for organisation: " + getOrganisationID());
		
		return dunningConfig;
	}

	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		String organisationID = getOrganisationID();
		try {
			pm.getExtent(DunningConfig.class);
			// check, whether the datastore is already initialized
			try
			{
				PayableObjectMoneyTransferFactory dunningLetterTransferFactory = 
					PayableObjectMoneyTransferFactoryJDO.getFactory(
						pm, null, PartnerPayPayableObjectAccountantDelegate.PAYABLE_OBJECT_FACTORY_SCOPE, DunningLetter.class
						);
				if (dunningLetterTransferFactory == null)
				{
					dunningLetterTransferFactory = 
						new DunningLetterMoneyTransferFactoryJDO(PartnerPayPayableObjectAccountantDelegate.PAYABLE_OBJECT_FACTORY_SCOPE);
					pm.makePersistent(dunningLetterTransferFactory);
				}
				
				pm.getObjectById(DunningConfigID.create(organisationID, 0));
				return; // already initialized
			}
			catch (JDOObjectNotFoundException x)
			{
				// Fetch default currency of current organisation and create a default DunningConfig with it.
				// FIXME: Is this correct? (Marius)
//				TradeConfigModule tradeConfigModule = (TradeConfigModule)
//					Config.getConfigModule(
//							pm, 
//							ConfigModuleID.create(organisationID, 
//									UserID.create(
//											organisationID, 
//											User.USER_ID_SYSTEM).toString(), 
//											User.class.getName(), 
//											TradeConfigModule.class.getName()
//									)
//							);
//				Currency currency = tradeConfigModule.getCurrency();
				
				Currency currency = CurrencyOrganisationDefault.getCurrencyOrganisationDefault(pm).getCurrency();
				if (currency == null)
					throw new IllegalStateException("There has to be a default currency set to the TradeConfigModule bevor " +
							"the DunningManagerBean can initialise the Dunning module!");
				
				PriceFragmentType totalPriceType = 
					(PriceFragmentType) pm.getObjectById(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL);
				
				if (totalPriceType == null)
					throw new IllegalStateException("No default totalPriceFragmentType with id=" +
							PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL+ " could be found!");
				
				//datastore not yet initialized
				DunningConfig defaultDunningConfig = 
					DunningConfigInitialiserCustomerFriendly.createCustomerFriendlyDunningConfig(organisationID, currency, totalPriceType);
				
				pm.makePersistent(defaultDunningConfig);

				// Setup the automatic dunning task
				TaskID taskID = TaskID.create(organisationID, TASK_TYPE_ID_PROCESS_DUNNING, TASK_ID_PROCESS_DUNNING);
				Task automaticDunning = new Task(
						taskID,
						User.getUser(pm, organisationID, User.USER_ID_SYSTEM),
						DunningManagerRemote.class,
						"processAutomaticDunning"
				);
				
				try {
					automaticDunning.getTimePatternSet().createTimePattern(
							"*",  // year
							"*",  // month
							"*",  // day
							"*",  // dayOfWeek
							"22", //  hour
							"0"   // minute
					);
				} catch (TimePatternFormatException e) {
					throw new RuntimeException(e);
				}
				automaticDunning.setEnabled(true);
				pm.makePersistent(automaticDunning);

				
				Accounting accounting = Accounting.getAccounting(pm);
				
				AccountantDelegate localBookDunningLetterAccountantDelegate = new LocalBookDunningLetterAccountantDelegate(
						accounting.getMandator(), IDGenerator.nextIDString(AccountantDelegate.class));
				
				accounting.getLocalAccountant().setAccountantDelegate(
						BookDunningLetterMoneyTransfer.class, localBookDunningLetterAccountantDelegate);
				
				AccountantDelegate partnerBookDunningLetterAccountantDelegate = new PartnerBookDunningLetterAccountantDelegate(
						organisationID, IDGenerator.nextIDString(AccountantDelegate.class));
				accounting.getPartnerAccountant().setAccountantDelegate(
						BookDunningLetterMoneyTransfer.class, partnerBookDunningLetterAccountantDelegate);				
			}
		} finally {
			pm.close();
		}
	}
}