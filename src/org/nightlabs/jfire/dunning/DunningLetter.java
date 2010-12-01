package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.dunning.book.BookDunningLetterMoneyTransfer;
import org.nightlabs.jfire.dunning.id.DunningLetterID;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.util.CollectionUtil;

/**
 * A DunningLetter represents the letter sent to a customer which may contain
 * several overdue invoices and its potentially increased costs (including the
 * interests for each invoice and dunning-level-dependent fees).
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(objectIdClass = DunningLetterID.class, identityType = IdentityType.APPLICATION, detachable = "true", table = "JFireDunning_DunningLetter")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@Queries({ 
	@javax.jdo.annotations.Query(
			name = "getOpenDunningLetters", 
			value = "SELECT this "
					+ "WHERE finalizeDT == null"
	), 
	@javax.jdo.annotations.Query(
			name = "getActiveDunningLetterByDunningProcess", 
			value = "SELECT this "
					+ "WHERE outstanding == true &&" +
					"JDOHelper.getObjectId(dunningProcess) == :dunningProcessID"
	),
})
public class DunningLetter 
implements Serializable, PayableObject, Statable 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningLetter.class);

	@PrimaryKey
	@Column(length = 100)
	private String organisationID;

	@PrimaryKey
	@Column(length = 100)
	private String dunningLetterID;

	/**
	 * The process to which this DunningLetter belongs.
	 */
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private DunningProcess dunningProcess;

	/**
	 * The overall dunning level of the letter. Most likely, this will be the
	 * highest level of all included invoices.
	 */
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private Integer letterDunningLevel = -1;

	/**
	 * The information of each overdue invoice needed to print the letter. This
	 * includes the dunning level, the original invoice, the interest for that
	 * invoice, the extended due date, etc.
	 */
	@Persistent(
			mappedBy="dunningLetter", 
			table = "JFireDunning_DunningLetter_dunningLetterEntries", 
			persistenceModifier = PersistenceModifier.PERSISTENT)
	private List<DunningLetterEntry> dunningLetterEntries;

	/**
	 * Contains all old fees (from the previous DunningLetter) as well as all
	 * new ones (based on dunningStep.feeTypes).
	 */
	@Persistent(
			mappedBy="dunningLetter", 
			table = "JFireDunning_DunningLetter_dunningFees", 
			persistenceModifier = PersistenceModifier.PERSISTENT)
	private List<DunningFee> dunningFees;

	/**
	 * The timestamp when this DunningLetter was finalized. It is important that
	 * the DunningLetterNotifiers are triggered when this field is set manually!
	 */
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private Date finalizeDT;

	/**
	 * Null or the timestamp when all the fees and interests were booked.
	 */
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private Date bookDT;

	/**
	 * The total amount of fees and interests to pay.
	 */
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private Price priceExcludingInvoices;

	@Persistent(dependent = "true", mappedBy = "dunningLetter", persistenceModifier = PersistenceModifier.PERSISTENT)
	private DunningLetterLocal dunningLetterLocal;

	public void setDunningLetterLocal(DunningLetterLocal dunningLetterLocal) {
		this.dunningLetterLocal = dunningLetterLocal;
	}

	/**
	 * The total amount of this DunningLetter comprising the invoices, all fees
	 * and interests. It always comprises the complete amount of all invoices
	 * summarized!
	 */
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private Price priceIncludingInvoices;

	/**
	 * Everything that was paid for the fees and interests and all previous
	 * DunningLetters so far, before this DunningLetter was created.
	 */
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private long amountPaidExcludingInvoices;

	/**
	 * price.amount - amountPaid
	 */
	private transient long amountToPay;


	// *** REV_marco_dunning ***
	// Shouldn't the outstanding flag be cleared, too, when a new DunningLetter
	// (replacing the old one) is created?
	/**
	 * A flag indicating that this DunningLetter is still open and waits for
	 * payment (of the amountToPay). This flag should be cleared immediately
	 * when all invoices are paid completely including the DunningLetter's fees
	 * and interest.
	 *
	 * We thus need to register an InvoiceActionHandler for every invoice that
	 * is part of a dunning process. If the invoice is paid without the dunning
	 * fees+interests, the dunning process is not complete and should be
	 * continued with the remaining amount (unless the organisation voluntarily
	 * gives up the dunning costs due to customer-friendlyness).
	 */
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private boolean outstanding;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningLetter() {
	}

	public DunningLetter(String organisationID, String dunningLetterID,
			DunningProcess dunningProcess) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningLetterID, "dunningLetterID"); //$NON-NLS-1$
		this.organisationID = organisationID;
		this.dunningLetterID = dunningLetterID;
		this.dunningProcess = dunningProcess;

		this.dunningLetterEntries = new ArrayList<DunningLetterEntry>();
		this.dunningFees = new ArrayList<DunningFee>();
		
		this.dunningLetterLocal = new DunningLetterLocal(this);
		this.priceIncludingInvoices = new Price(IDGenerator.getOrganisationID(), IDGenerator.nextID(Price.class), dunningProcess.getCurrency());
	}

	public DunningLetter(DunningProcess dunningProcess) {
		this(dunningProcess.getOrganisationID(), IDGenerator
				.nextIDString(DunningLetter.class), dunningProcess);
	}

	public DunningLetterEntry getDunningLetterEntry(Invoice invoice) {
		for (DunningLetterEntry entry : dunningLetterEntries) {
			if (entry.getInvoice().equals(invoice)) {
				return entry;
			}
		}
		return null;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getDunningLetterID() {
		return dunningLetterID;
	}

	public DunningProcess getDunningProcess() {
		return dunningProcess;
	}

	public Integer getDunningLevel() {
		return letterDunningLevel;
	}

	private transient boolean containUpdatedItem = false;

	/**
	 * Adds the overdue invoice into the new letter. 
	 * It checks if the invoice is already dunned or not. If it's already dunned,
	 * it calculates the new level and new due date.
	 * @param dunningConfig
	 * @param prevDunningLetter
	 * @param dunningLevel
	 * @param dunningInvoice
	 */
	public void addEntry(DunningLetterEntry letterEntry) {
		int dunningLevel = letterEntry.getDunningLevel();
		if (dunningLevel > letterDunningLevel) {
			this.letterDunningLevel = dunningLevel;
		}

		dunningLetterEntries.add(letterEntry);
		priceIncludingInvoices.sumPrice(letterEntry.getPriceIncludingInvoice());
	}
	
	public void updateEntry(Invoice dunningInvoice) {
		DunningConfig dunningConfig = dunningProcess.getDunningConfig();

		//Check if the dunningInv needs to be dunned again (it's late for payment on the extended due date).
		//If so, we need to change the dunningLevel.
		int dunningLevel = -1;
		for (DunningLetterEntry entry : dunningLetterEntries) {
			if (entry.getInvoice().equals(dunningInvoice)) {
				dunningLevel = entry.getDunningLevel();
				InvoiceDunningStep invDunningStep = dunningConfig.getInvoiceDunningStep(dunningLevel);
				boolean isOverdue = entry.isOverdue(new Date());
				if (isOverdue) {
					long newDueDateTime = entry.getExtendedDueDateForPayment().getTime() + invDunningStep.getPeriodOfGraceMSec();
					Date newDueDate = new Date(newDueDateTime);
					entry.setDunningLevel(dunningLevel++);
					entry.setPeriodOfGraceMSec(invDunningStep.getPeriodOfGraceMSec());
					entry.setExtendedDueDateForPayment(newDueDate);
					containUpdatedItem = true;
				}
			}
		}

//		priceIncludingInvoices.sumPrice(letterEntry.getPriceIncludingInvoice());
//		for (DunningFee dunningFee : dunningFees) {
//			priceIncludingInvoices.sumPrice(dunningFee.getPrice());
//		}
	}

	public boolean isContainUpdatedItem() {
		return containUpdatedItem;
	}

	public List<DunningLetterEntry> getEntries() {
		return Collections.unmodifiableList(dunningLetterEntries);
	}

	public void addDunningFee(DunningFee dunningFee) {
		dunningFees.add(dunningFee);
	}

	public List<DunningFee> getDunningFees() {
		return Collections.unmodifiableList(dunningFees);
	}

	public void setFinalized() {
		if (isFinalized())
			return;

		this.finalizeDT = new Date(System.currentTimeMillis());
	}

	public void setFinalizeDT(Date finalizeDT) {
		this.finalizeDT = finalizeDT;
	}

	public Date getFinalizeDT() {
		return finalizeDT;
	}

	public boolean isFinalized() {
		return finalizeDT != null;
	}

	public void setBookDT(Date bookDT) {
		this.bookDT = bookDT;
	}

	public Date getBookDT() {
		return bookDT;
	}

	public boolean isBooked() {
		return bookDT != null;
	}

	public void setPriceExcludingInvoices(Price priceExcludingInvoices) {
		this.priceExcludingInvoices = priceExcludingInvoices;
	}

	public Price getPriceExcludingInvoices() {
		return priceExcludingInvoices;
	}

	public void setPriceIncludingInvoices(Price priceIncludingInvoices) {
		this.priceIncludingInvoices = priceIncludingInvoices;
	}

	public Price getPriceIncludingInvoices() {
		return priceIncludingInvoices;
	}

	public void setAmountPaidExcludingInvoices(long amountPaidExcludingInvoices) {
		this.amountPaidExcludingInvoices = amountPaidExcludingInvoices;
	}

	public long getAmountPaidExcludingInvoices() {
		return amountPaidExcludingInvoices;
	}

	public void setAmountToPay(long amountToPay) {
		this.amountToPay = amountToPay;
	}

	public long getAmountToPay() {
		return amountToPay;
	}

	public void setOutstanding(boolean outstanding) {
		this.outstanding = outstanding;
	}

	public boolean isOutstanding() {
		return outstanding;
	}

	public void bookDunningLetter(BookDunningLetterMoneyTransfer transfer,
			boolean rollback) {
		long amount = transfer.getAmount();
		if (rollback)
			amount *= -1;

		amountToPay = amountToPay - amount;
		outstanding = amountToPay != 0;
	}

	public void copyAllFeesFrom(DunningLetter dunningLetter) {
		if (dunningLetter == null) {
			throw new IllegalArgumentException("The dunning letter should not be null!!!");
		}

		for (DunningFee dunningFee : dunningLetter.getDunningFees()) {
			addDunningFee(dunningFee);
		}
	}

	public static Collection<DunningLetter> getOpenDunningLetters(
			PersistenceManager pm, DunningProcessID dunningProcessID) {
		Query query = pm.newNamedQuery(DunningLetter.class,
				"getActiveDunningLetterByDunningProcess");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dunningProcessID", dunningProcessID);
		return CollectionUtil.castList((List<?>)query.executeWithMap(params));
	}
	
	private static Collection<DunningLetter> getOpenDunningLetters(
			PersistenceManager pm) {
		Query query = pm.newNamedQuery(DunningLetter.class,
				"getOpenDunningLetters");
		return CollectionUtil.castList((List<?>) query.execute());
	}

	public static DunningLetter getOpenDunningLetterByInvoiceID(
			PersistenceManager pm, InvoiceID invoiceID) {
		Collection<DunningLetter> openDunningLetters = getOpenDunningLetters(pm);
		for (DunningLetter openDunningLetter : openDunningLetters) {
			for (DunningLetterEntry entry : openDunningLetter
					.getEntries()) {
				if (JDOHelper.getObjectId(entry.getInvoice()).equals(invoiceID)) {
					return openDunningLetter;
				}
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dunningLetterID == null) ? 0 : dunningLetterID.hashCode());
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DunningLetter other = (DunningLetter) obj;
		if (dunningLetterID == null) {
			if (other.dunningLetterID != null)
				return false;
		} else if (!dunningLetterID.equals(other.dunningLetterID))
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DunningLetter [dunningLetterID=" + dunningLetterID
				+ ", organisationID=" + organisationID + "]";
	}

	@Override
	public LegalEntity getCustomer() {
		return dunningProcess.getCustomer();
	}

	@Override
	public LegalEntity getVendor() {
		PersistenceManager pm = getPersistenceManager();
		return OrganisationLegalEntity.getOrganisationLegalEntity(pm,
				organisationID);
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException(
			"This instance of DunningLetter is currently not attached to a datastore! Cannot get a PersistenceManager!");

		return pm;
	}
	
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private State state;

	@Join
	@Persistent(nullValue = NullValue.EXCEPTION, table = "JFireDunning_DunningLetter_states", persistenceModifier = PersistenceModifier.PERSISTENT)
	private List<State> states;

	@Override
	public StatableLocal getStatableLocal() {
		return dunningLetterLocal;
	}
	
	public DunningLetterLocal getDunningLetterLocal() {
		return dunningLetterLocal;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public List<State> getStates() {
		if (logger.isDebugEnabled()) {
			logger.debug("getStates: dunningLetter=" + dunningLetterID + " ("
					+ this + ") returning these states:");

			for (State state : states) {
				logger.debug("  * " + state.getPrimaryKey());
			}
		}

		return Collections.unmodifiableList(states);
	}

	@Override
	public void setState(State state) {
		if (state == null)
			throw new IllegalArgumentException("state must not be null!");

		if (!state.getStateDefinition().isPublicState())
			throw new IllegalArgumentException(
					"state.stateDefinition.publicState is false!");

		if (logger.isDebugEnabled())
			logger.debug("setState: dunningLetter=" + dunningLetterID + " ("
					+ this + ") state=" + state.getPrimaryKey() + " ("
					+ state.getStateDefinition().getJbpmNodeName() + ")");

		this.state = state;
		this.states.add(state);
	}
}