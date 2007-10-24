package org.nightlabs.jfire.issue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueStatusID;
import org.nightlabs.jfire.security.UserGroup;

/**
 * @author Chairat Kongarayawetchakun - chairat[AT]nightlabs[DOT]de
 * 
 * @ejb.bean name="jfire/ejb/JFireIssueTracking/IssueManager"	
 *           jndi-name="jfire/ejb/JFireIssueTracking/IssueManager"
 *           type="Stateless" 
 *
 * @ejb.util generate = "physical"
 */
public class IssueManagerBean 
extends BaseSessionBeanImpl
implements SessionBean{

	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueManagerBean.class);

	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}
	
	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbPassivate()");
	}

	/**
	 * Creates a new issue. This method is only usable, if the user (principal)
	 * is an organisation, because this organisation will automatically be set
	 * as the user for the new Issue.
	 *
	 * @param userID Either <code>null</code> (then the default will be used) or an ID of a {@link UserGroup} which is allowed to the User.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue createIssueWithoutAttachedDocument(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth){
		PersistenceManager pm = getPersistenceManager();
		try {
			if (!issue.getOrganisationID().equals(getOrganisationID()))
				throw new IllegalArgumentException("Given Issue was created for a different organisation, can not store to this datastore!");

			Issue result = NLJDOHelper.storeJDO(pm, issue, get, fetchGroups, maxFetchDepth);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Creates a new issue. This method is only usable, if the user (principal)
	 * is an organisation, because this organisation will automatically be set
	 * as the user for the new Issue.
	 *
	 * @param userID Either <code>null</code> (then the default will be used) or an ID of a {@link UserGroup} which is allowed to the User.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue createIssueWithAttachedDocument(Issue issue, ObjectID objectID, boolean get, String[] fetchGroups, int maxFetchDepth){
		PersistenceManager pm = getPersistenceManager();
		try {
			if (!issue.getOrganisationID().equals(getOrganisationID()))
				throw new IllegalArgumentException("Given Issue was created for a different organisation, can not store to this datastore!");

			Issue result = NLJDOHelper.storeJDO(pm, issue, get, fetchGroups, maxFetchDepth);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueID> getIssueIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Issue.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueID>((Collection<? extends IssueID>) q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<Issue> getIssues(Collection<IssueID> issueIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueIDs, Issue.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueSeverityTypeID> getIssueSeverityTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssueSeverityType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueSeverityTypeID>((Collection<? extends IssueSeverityTypeID>) q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueSeverityTypes(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssueSeverityType.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueStatus(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssueStatus.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssuePriorities(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssuePriority.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise() throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			// check, whether the datastore is already initialized
			pm.getExtent(IssueStatus.class);
			try {
				pm.getObjectById(IssueStatusID.create("N"), true);
				return; // already initialized
			} catch (JDOObjectNotFoundException x) {
				// datastore not yet initialized
			}

			// Create the statuses
			IssueStatus issueStatus;

			issueStatus = new IssueStatus("N");
			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "New");
			pm.makePersistent(issueStatus);

			issueStatus = new IssueStatus("FB");
			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Feedback");
			pm.makePersistent(issueStatus);
			
			issueStatus = new IssueStatus("AKL");
			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Acknowledged");
			pm.makePersistent(issueStatus);
			
			issueStatus = new IssueStatus("CF");
			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Confirmed");
			pm.makePersistent(issueStatus);
			
			issueStatus = new IssueStatus("RS");
			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Resolved");
			pm.makePersistent(issueStatus);
			
			issueStatus = new IssueStatus("C");
			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Close");
			pm.makePersistent(issueStatus);

			// check, whether the datastore is already initialized
			pm.getExtent(IssueSeverityType.class);
			try {
				pm.getObjectById(IssueSeverityTypeID.create("N"), true);
				return; // already initialized
			} catch (JDOObjectNotFoundException x) {
				// datastore not yet initialized
			}
			
			// Create the statuses
			IssueSeverityType issueSeverityType;

			issueSeverityType = new IssueSeverityType("MN");
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Minor");
			pm.makePersistent(issueSeverityType);

			issueSeverityType = new IssueSeverityType("MJ");
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Major");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType("C");
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Crash");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType("B");
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Block");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType("FT");
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Feature");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType("TV");
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Trivial");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType("T");
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Text");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType("TW");
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Tweak");
			pm.makePersistent(issueSeverityType);
			
			////////////////////////////////////////////////////////
			// Create the priorities
			// check, whether the datastore is already initialized
			pm.getExtent(IssuePriority.class);
			try {
				pm.getObjectById(IssuePriorityID.create("0"), true);
				return; // already initialized
			} catch (JDOObjectNotFoundException x) {
				// datastore not yet initialized
			}
			IssuePriority issuePriority;

			issuePriority = new IssuePriority("0");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "None");
			pm.makePersistent(issuePriority);

			issuePriority = new IssuePriority("1");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Low");
			pm.makePersistent(issuePriority);
			
			issuePriority = new IssuePriority("2");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Normal");
			pm.makePersistent(issuePriority);
			
			issuePriority = new IssuePriority("3");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "High");
			pm.makePersistent(issuePriority);
			
			issuePriority = new IssuePriority("4");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Urgent");
			pm.makePersistent(issuePriority);
			
			issuePriority = new IssuePriority("5");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Immediate");
			pm.makePersistent(issuePriority);
			
//			Trader trader = Trader.getTrader(pm);
//
//
//
//
//			LegalEntity anonymousCustomer = LegalEntity.getAnonymousCustomer(pm);
//			CustomerGroup anonymousCustomerGroup = anonymousCustomer.getDefaultCustomerGroup();
//
//			//		 create some ModeOfPayments
//			// Cash
//			ModeOfPayment modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_CASH);
//			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Cash");
//			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Bargeld");
//			modeOfPayment.getName().setText(Locale.FRENCH.getLanguage(), "Argent Liquide");
//			ModeOfPaymentFlavour modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_CASH);
//			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Cash");
//			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Bargeld");
//			modeOfPaymentFlavour.getName().setText(Locale.FRENCH.getLanguage(), "Argent Liquide");
//			modeOfPaymentFlavour.loadIconFromResource();
//			pm.makePersistent(modeOfPayment);
//			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);
//			anonymousCustomerGroup.addModeOfPayment(modeOfPayment);
//
//			// we need this later for payment processor registration
//			ModeOfPayment modeOfPaymentCash = modeOfPayment;
//
//			// No payment - this is a dummy MOP which means, the payment is postponed without
//			//   specifying a certain real MOP
//			modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_NON_PAYMENT);
//			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Payment");
//			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Nichtzahlung");
//			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_NON_PAYMENT);
//			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Payment");
//			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Nichtzahlung");
//			modeOfPaymentFlavour.loadIconFromResource();
//			pm.makePersistent(modeOfPayment);
//			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);
//
//			// we need this later for payment processor registration
//			ModeOfPayment modeOfPaymentNonPayment = modeOfPayment;
//
//
//			// Credit Card - VISA, Master, AmEx, Diners
//			modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_CREDIT_CARD);
//			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Credit Card");
//			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Kreditkarte");
//			modeOfPayment.getName().setText(Locale.FRENCH.getLanguage(), "Carte de Crédit");
//			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_VISA);
//			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "VISA");
//			modeOfPaymentFlavour.loadIconFromResource();
//			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_MASTER_CARD);
//			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "MasterCard");
//			modeOfPaymentFlavour.loadIconFromResource();
//			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_AMERICAN_EXPRESS);
//			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "American Express");
//			modeOfPaymentFlavour.loadIconFromResource();
//			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_DINERS_CLUB);
//			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Diners Club");
//			modeOfPaymentFlavour.loadIconFromResource();
//			pm.makePersistent(modeOfPayment);
//			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);
//			anonymousCustomerGroup.addModeOfPayment(modeOfPayment);
//
//			// we need this later for payment processor registration
//			ModeOfPayment modeOfPaymentCreditCard = modeOfPayment;
//
//			// Bank Transfer
//			modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_BANK_TRANSFER);
//			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Bank Transfer");
//			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Überweisung");
//			modeOfPayment.getName().setText(Locale.FRENCH.getLanguage(), "Virement");
//			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_BANK_TRANSFER);
//			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Bank Transfer");
//			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Überweisung");
//			modeOfPaymentFlavour.getName().setText(Locale.FRENCH.getLanguage(), "Virement");
//			modeOfPaymentFlavour.loadIconFromResource();
//			pm.makePersistent(modeOfPayment);
//			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);
//
//			// we need this later for payment processor registration
//			ModeOfPayment modeOfPaymentBankTransfer = modeOfPayment;
//
//			// Debit Note
//			modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_DEBIT_NOTE);
//			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Debit Note");
//			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Lastschrift");
//			modeOfPayment.getName().setText(Locale.FRENCH.getLanguage(), "Note de Débit");
//			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_DEBIT_NOTE);
//			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Debit Note");
//			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Lastschrift");
//			modeOfPaymentFlavour.getName().setText(Locale.FRENCH.getLanguage(), "Note de Débit");
//			modeOfPaymentFlavour.loadIconFromResource();
//			pm.makePersistent(modeOfPayment);
//			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);
//
//			// we need this later for payment processor registration
//			ModeOfPayment modeOfPaymentDebitNote = modeOfPayment;
//
//
//			// create some ServerPaymentProcessors
//			ServerPaymentProcessorCash serverPaymentProcessorCash = ServerPaymentProcessorCash.getServerPaymentProcessorCash(pm);
//			serverPaymentProcessorCash.getName().setText(Locale.ENGLISH.getLanguage(), "Cash Payment");
//			serverPaymentProcessorCash.getName().setText(Locale.GERMAN.getLanguage(), "Barzahlung");
//			serverPaymentProcessorCash.getName().setText(Locale.FRENCH.getLanguage(), "Paiement Argent Liquide");
//			serverPaymentProcessorCash.addModeOfPayment(modeOfPaymentCash);
//
//			ServerPaymentProcessorNonPayment serverPaymentProcessorNonPayment =
//				ServerPaymentProcessorNonPayment.getServerPaymentProcessorNonPayment(pm);
//			serverPaymentProcessorNonPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Payment (payment will be postponed)");
//			serverPaymentProcessorNonPayment.getName().setText(Locale.GERMAN.getLanguage(), "Nichtzahlung (Zahlung wird verschoben)");
//			serverPaymentProcessorNonPayment.addModeOfPayment(modeOfPaymentNonPayment);
//
//			ServerPaymentProcessorCreditCardDummyForClientPayment serverPaymentProcessorCreditCardDummyForClientPayment =
//				ServerPaymentProcessorCreditCardDummyForClientPayment.getServerPaymentProcessorCreditCardDummyForClientPayment(pm);
//			serverPaymentProcessorCreditCardDummyForClientPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Dummy for client-sided Credit Card Payment");
//			serverPaymentProcessorCreditCardDummyForClientPayment.getName().setText(Locale.GERMAN.getLanguage(), "Pseudo-Modul für client-seitige Kreditkarten-Zahlungen");
//			serverPaymentProcessorCreditCardDummyForClientPayment.addModeOfPayment(modeOfPaymentCreditCard);
//
//			ServerPaymentProcessorBankTransferGermany serverPaymentProcessorBankTransferGermany = ServerPaymentProcessorBankTransferGermany.getServerPaymentProcessorBankTransferGermany(pm);
//			serverPaymentProcessorBankTransferGermany.getName().setText(Locale.ENGLISH.getLanguage(), "Bank transfer within Germany");
//			serverPaymentProcessorBankTransferGermany.getName().setText(Locale.GERMAN.getLanguage(), "Überweisung innerhalb Deutschlands");
//			serverPaymentProcessorBankTransferGermany.addModeOfPayment(modeOfPaymentBankTransfer);
//
//			ServerPaymentProcessorDebitNoteGermany serverPaymentProcessorDebitNoteGermany = ServerPaymentProcessorDebitNoteGermany.getServerPaymentProcessorDebitNoteGermany(pm);
//			serverPaymentProcessorDebitNoteGermany.getName().setText(Locale.ENGLISH.getLanguage(), "Debit Note within Germany");
//			serverPaymentProcessorDebitNoteGermany.getName().setText(Locale.GERMAN.getLanguage(), "Lastschrift innerhalb Deutschlands");
//			serverPaymentProcessorDebitNoteGermany.addModeOfPayment(modeOfPaymentDebitNote);
//
//
//			// persist process definitions
//			ProcessDefinition processDefinitionInvoiceCustomer;
//			processDefinitionInvoiceCustomer = accounting.storeProcessDefinitionInvoice(TradeSide.customer, ProcessDefinitionAssignment.class.getResource("invoice/customer/"));
//			pm.makePersistent(new ProcessDefinitionAssignment(Invoice.class, TradeSide.customer, processDefinitionInvoiceCustomer));
//
//			ProcessDefinition processDefinitionInvoiceVendor;
//			processDefinitionInvoiceVendor = accounting.storeProcessDefinitionInvoice(TradeSide.vendor, ProcessDefinitionAssignment.class.getResource("invoice/vendor/"));
//			pm.makePersistent(new ProcessDefinitionAssignment(Invoice.class, TradeSide.vendor, processDefinitionInvoiceVendor));
//
//
//			// deactive IDGenerator's cache for invoice
//			IDNamespaceDefault idNamespaceDefault = IDNamespaceDefault.createIDNamespaceDefault(pm, getOrganisationID(), Invoice.class);
//			idNamespaceDefault.setCacheSizeServer(0);
//			idNamespaceDefault.setCacheSizeClient(0);
//
//
//			pm.makePersistent(new EditLockTypeInvoice(EditLockTypeInvoice.EDIT_LOCK_TYPE_ID));
		} finally {
			pm.close();
		}
	}
}
