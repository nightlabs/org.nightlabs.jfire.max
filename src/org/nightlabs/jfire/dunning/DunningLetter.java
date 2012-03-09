package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.commons.lang.time.DateUtils;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.dunning.id.DunningLetterID;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.l10n.Currency;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * A DunningLetter represents the letter sent to a customer which may contain
 * several overdue invoices and its potentially increased costs (including the
 * interests for each invoice and dunning-level-dependent fees).
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marius Heinzmann <!-- Marius[at]NightLabs[dot]de -->
 */
@PersistenceCapable(
	objectIdClass = DunningLetterID.class,
	identityType = IdentityType.APPLICATION,
	detachable = "true",
	table = "JFireDunning_Letter")
public class DunningLetter 
	implements Serializable, PayableObject 
{
	private static final long serialVersionUID = 1L;
//	private static final Logger logger = LoggerFactory.getLogger(DunningLetter.class);

	@PrimaryKey
	@Column(length = 100)
	private String organisationID;

	@PrimaryKey
	private long dunningLetterID;

	/**
	 * Backreference to the process to which this DunningLetter belongs.
	 */
	private DunningRun dunningRun;

	/**
	 * The overall dunning level of the letter. Most likely, this will be the
	 * highest level of all included invoices.
	 */
	private int letterDunningLevel = -1;

	/**
	 * The information of each overdue invoice needed to print the letter. This
	 * includes the dunning level, the original invoice, the interest for that
	 * invoice, the extended due date, etc.
	 */
	@Persistent(dependentElement="true", mappedBy="dunningLetter")
	private List<DunningLetterEntry> dunningLetterEntries;

	/**
	 * Contains all old fees (from the previous DunningLetter) as well as all
	 * new ones (based on dunningStep.feeTypes).
	 */
	@Persistent(dependentElement="true", mappedBy="dunningLetter")
	private List<DunningFee> dunningFees;

	/**
	 * The timestamp when this DunningLetter was finalized. It is important that
	 * the DunningLetterNotifiers are triggered when this field is set manually!
	 */
	private Date finalizeDT;

	private User finalizeUser;
	
	/**
	 * Null or the timestamp when all the fees and interests were booked.
	 */
	private Date bookDT;

	/**
	 * The total amount of fees and interests to pay.
	 */
	private Price priceExcludingInvoices;

	/**
	 * The total amount of this DunningLetter comprising the invoices, all fees
	 * and interests. It always comprises the complete amount of all invoices
	 * summarized!
	 */
	private Price priceIncludingInvoices;

	/**
	 * Everything that was paid for the fees and interests and all previous
	 * DunningLetters so far, before this DunningLetter was created.
	 */
	private long amountPaidExcludingInvoices;

	/**
	 * price.amount - amountPaid
	 */
	private transient long amountToPay;

	// *** REV_marco_dunning ***
	// Shouldn't the outstanding flag be cleared, too, when a new DunningLetter
	// (replacing the old one) is created?
	// From chairat: it's not replaced. the dunning letters are added into a list in the dunning process.
	// Marius: Yes, it should be cleared as soon as the DunningLetter is not valid anymore (either paid or replaced by new one).
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
	private boolean outstanding;

	public DunningLetter(DunningRun dunningRun, long dunningLetterID)
	{
		assert dunningRun != null : "dunningRun must NOT be null!";
		assert dunningRun.getOrganisationID() != null : "dunningRun.getOrganisationID() must NOT be null!";
		assert dunningRun.getDunningProcess().getCurrency() != null : 
			"dunningRun.getDunningProcess().getCurrency() must NOT be null!";
		
		this.organisationID = dunningRun.getOrganisationID();
		this.dunningLetterID = dunningLetterID;
		this.dunningRun = dunningRun;
		this.outstanding = true;

		this.dunningLetterEntries = new ArrayList<DunningLetterEntry>();
		this.dunningFees = new ArrayList<DunningFee>();
		
//		// Fetch PriceFragmentType.Total if dunningRun is persistent,
//		// otherwise assume we're in a test environment -> create the FragmentType locally.
//		// Note: If we will ever create a DunningProcess in the client, then we'd need to replace the dummy FragmentTypes
//		//       with the persistent counter parts via an AttachCallback.
//		PriceFragmentType fragmentTypeTotal;
//		PersistenceManager pm = JDOHelper.getPersistenceManager(dunningRun);
//		if (pm != null)
//			fragmentTypeTotal = (PriceFragmentType) pm.getObjectById(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL);
//		else
//			fragmentTypeTotal = new PriceFragmentType(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL);
			
		this.priceIncludingInvoices = new Price(
				IDGenerator.getOrganisationID(), IDGenerator.nextID(Price.class), dunningRun.getDunningProcess().getCurrency());
//		this.priceIncludingInvoices.setAmount(fragmentTypeTotal, 0);
		this.priceIncludingInvoices.setAmount(0);
		
		this.priceExcludingInvoices = new Price(
				IDGenerator.getOrganisationID(), IDGenerator.nextID(Price.class), dunningRun.getDunningProcess().getCurrency());
//		this.priceExcludingInvoices.setAmount(fragmentTypeTotal, 0);
		this.priceExcludingInvoices.setAmount(0);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getDunningLetterID()
	{
		return dunningLetterID;
	}

	public DunningRun getDunningRun()
	{
		return dunningRun;
	}

	public Integer getDunningLevel()
	{
		return letterDunningLevel;
	}

	public void updatePrices()
	{
		priceIncludingInvoices.setAmount(0);
		priceExcludingInvoices.setAmount(0);
		
		if (dunningLetterEntries != null)
		{
			for (DunningLetterEntry entry : dunningLetterEntries)
			{
				priceIncludingInvoices.setAmount(
						priceIncludingInvoices.getAmount() + entry.getPriceIncludingInvoice().getAmount()
				);
				priceExcludingInvoices.setAmount(
						priceExcludingInvoices.getAmount() + entry.getInterestAmountToPay()
				);
				
			}
		}
		if (dunningFees != null)
		{
			for (DunningFee fee : dunningFees)
			{
				priceIncludingInvoices.setAmount(priceIncludingInvoices.getAmount() + fee.getAmountToPay());
				priceExcludingInvoices.setAmount(priceExcludingInvoices.getAmount() + fee.getAmountToPay());				
			}
		}
	}
	
	/**
	 * Adds the overdue invoice into the new letter. 
	 * It checks if the invoice is already dunned or not. If it's already dunned,
	 * it calculates the new level and new due date.
	 * @param dunningConfig
	 * @param prevDunningLetter
	 * @param dunningLevel
	 * @param dunningInvoice
	 */
	public void addEntry(DunningLetterEntry letterEntry)
	{
		int dunningLevel = letterEntry.getDunningLevel();
		if (dunningLevel > letterDunningLevel) {
			this.letterDunningLevel = dunningLevel;
		}

		dunningLetterEntries.add(letterEntry);
		priceIncludingInvoices.setAmount(
				priceIncludingInvoices.getAmount() + letterEntry.getPriceIncludingInvoice().getAmount()
		);
		priceExcludingInvoices.setAmount(
				priceExcludingInvoices.getAmount() + letterEntry.getInterestAmountToPay()
		);
		letterEntry.setDunningLetter(this);
	}
	
	public List<DunningLetterEntry> getEntries()
	{
		return Collections.unmodifiableList(dunningLetterEntries);
	}
	
	/**
	 * Returns the corresponding entry that contains the invoice
	 * @param invoice
	 * @return DunningLetterEntry
	 */
	public DunningLetterEntry getEntry(Invoice invoice)
	{
		if (invoice == null || dunningLetterEntries == null || dunningLetterEntries.isEmpty())
			return null;
		
		for (DunningLetterEntry entry : dunningLetterEntries)
		{
			if (entry.getInvoice().equals(invoice))
				return entry;
		}
		return null;
	}
	
	/**
	 * Returns <code>true</code> if the given invoice is contained in the list of dunned invoices of this letter.
	 *  
	 * @param invoice The invoice to check for.
	 * @return <code>true</code> if the given invoice is contained in the list of dunned invoices of this letter,
	 * 	<code>false</code> otherwise.
	 */
	public boolean containsInvoice(Invoice invoice)
	{
		return getEntry(invoice) != null;
	}

	public void addDunningFee(DunningFee dunningFee)
	{
		priceIncludingInvoices.setAmount(priceIncludingInvoices.getAmount() + dunningFee.getAmountToPay());
		priceExcludingInvoices.setAmount(priceExcludingInvoices.getAmount() + dunningFee.getAmountToPay());
		dunningFees.add(dunningFee);
	}

	public List<DunningFee> getDunningFees()
	{
		return Collections.unmodifiableList(dunningFees);
	}
	
	/**
	 * Returns a map of all DunningFeeTypes existing in the list of dunningFees and their total amount to pay.
	 * 
	 * @return a map of all DunningFeeTypes existing in the list of dunningFees and their total amount to pay.
	 */
	public Map<DunningFeeType, Long> getAmountToPayPerDunningFeeType()
	{
		if (getDunningFees() == null)
			return Collections.emptyMap();
		
		Map<DunningFeeType, Long> allFeeTypes = new HashMap<DunningFeeType, Long>();
		for (DunningFee dunningFee : getDunningFees())
		{
			Long previousAmount = allFeeTypes.get(dunningFee.getDunningFeeType());
			if (previousAmount == null)
				previousAmount = Long.valueOf(0);
			
			allFeeTypes.put(dunningFee.getDunningFeeType(), previousAmount + dunningFee.getAmountToPay());
		}
		return allFeeTypes;
	}

	/**
	 * Returns the total amount to pay for the interests on all dunned invoices contained in this letter.
	 * 
	 * @return the total amount to pay for the interests on all dunned invoices contained in this letter.
	 */
	public long getTotalInterestAmountToPay()
	{
		long result = 0;
		if (getEntries() != null)
		{
			for (DunningLetterEntry letterEntry : getEntries()) {
				result += letterEntry.getInterestAmountToPay();
			}
		}
		return result;
	}

	/**
	 * Returns the amount to pay for a specific fee type or if <code>feeType == null</code> then the total amount to pay
	 * for all fee types.
	 * 
	 * @param feeType The feeType to filter with or <code>null</code> == none.
	 * @return the amount to pay for a specific fee type or if <code>feeType == null</code> then the total amount to pay
	 * for all fee types.
	 */
	public long getTotalFeeAmountToPay(DunningFeeType feeType)
	{
		long result = 0;
		if (getDunningFees() != null)
		{
			for (DunningFee fee : getDunningFees())
			{
				if (feeType != null && ! feeType.equals(fee.getDunningFeeType()))
					continue;
				
				result += fee.getAmountToPay();
			}
		}
		return result;
	}

	public void setFinalized(User user)
	{
		if (isFinalized())
			return;

		this.finalizeDT = new Date();
		this.finalizeUser = user;
		
		if (dunningLetterEntries == null)
			throw new IllegalStateException("Cannot finalize an empty DunningLetter! letter=" + this);
		
		// next day at 00:00:00
		Date newExtendedDueDateBase = DateUtils.addDays(DateUtils.truncate(finalizeDT, Calendar.DAY_OF_MONTH), 1);
		for (DunningLetterEntry entry : dunningLetterEntries)
		{
			Date newDueDate = new Date(newExtendedDueDateBase.getTime() + entry.getPeriodOfGraceMSec());
			entry.setExtendedDueDateForPayment(newDueDate);
		}
	}
	
	public User getFinalizeUser()
	{
		return finalizeUser;
	}

	public Date getFinalizeDT()
	{
		return finalizeDT;
	}

	public boolean isFinalized()
	{
		return finalizeDT != null;
	}

	public void setBookDT(Date bookDT)
	{
		this.bookDT = bookDT;
	}

	public Date getBookDT()
	{
		return bookDT;
	}

	public boolean isBooked()
	{
		return bookDT != null;
	}

	public Price getPriceExcludingInvoices()
	{
		return priceExcludingInvoices;
	}

	public Price getPriceIncludingInvoices()
	{
		return priceIncludingInvoices;
	}

	public void setAmountPaidExcludingInvoices(long amountPaidExcludingInvoices)
	{
		this.amountPaidExcludingInvoices = amountPaidExcludingInvoices;
	}

	public long getAmountPaidExcludingInvoices()
	{
		return amountPaidExcludingInvoices;
	}

	@Override
	public long getAmountToPay()
	{
		return getPriceExcludingInvoices().getAmount() - getAmountPaidExcludingInvoices();
	}
	
	public long getAmountToPayIncludingInvoices()
	{
		return getPriceIncludingInvoices().getAmount() - getAmountPaidExcludingInvoices();
	}
	
	@Override
	public Currency getCurrency()
	{
		// The DunningLetter may be detached without the DunningRun & Process, 
		// hence check for currency in local members first.
		if (priceIncludingInvoices != null)
			return priceIncludingInvoices.getCurrency();
		
		if (priceExcludingInvoices != null)
			return priceExcludingInvoices.getCurrency();
		
		DunningRun dunningRun = getDunningRun();
		if (dunningRun != null && dunningRun.getDunningProcess() != null)
			return dunningRun.getDunningProcess().getCurrency();
		
		return null;
	}

	public void setOutstanding(boolean outstanding)
	{
		this.outstanding = outstanding;
	}

	public boolean isOutstanding()
	{
		return outstanding;
	}
	
	@Override
	public String getPayableObjectID()
	{
		return Long.toString(dunningLetterID);
	}
	
	public static Collection<DunningLetter> getOpenDunningLetters(PersistenceManager pm, DunningProcessID dunningProcessID)
	{
		Query query = pm.newNamedQuery(DunningLetter.class,
				"getActiveDunningLetterByDunningProcess");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dunningProcessID", dunningProcessID);
		return CollectionUtil.castList((List<?>)query.executeWithMap(params));
	}
	
//	public void bookDunningLetter(BookDunningLetterMoneyTransfer transfer, boolean rollback)
//	{
//		long amount = transfer.getAmount();
//		if (rollback)
//			amount *= -1;
//
//		amountToPay = amountToPay - amount;
//		outstanding = amountToPay != 0;
//	}
	
//	private static Collection<DunningLetter> getOpenDunningLetters(PersistenceManager pm)
//	{
//		Query query = pm.newNamedQuery(DunningLetter.class,	"getOpenDunningLetters");
//		return CollectionUtil.castList((List<?>) query.execute());
//	}

//	public static DunningLetter getOpenDunningLetterByInvoiceID(PersistenceManager pm, InvoiceID invoiceID)
//	{
//		Collection<DunningLetter> openDunningLetters = getOpenDunningLetters(pm);
//		for (DunningLetter openDunningLetter : openDunningLetters)
//		{
//			for (DunningLetterEntry entry : openDunningLetter.getEntries())
//			{
//				if (JDOHelper.getObjectId(entry.getInvoice()).equals(invoiceID))
//					return openDunningLetter;
//			}
//		}
//		return null;
//	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result	+ (int) (dunningLetterID ^ (dunningLetterID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningLetter other = (DunningLetter) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(dunningLetterID, other.dunningLetterID))
			return true;
		
		return false;
	}

	@Override
	public String toString()
	{
		return "DunningLetter [organisationID=" + organisationID + ", dunningLetterID=" + dunningLetterID + "]";
	}

	@Override
	public LegalEntity getCustomer()
	{
		return dunningRun.getDunningProcess().getCustomer();
	}

	@Override
	public LegalEntity getVendor()
	{
		PersistenceManager pm = getPersistenceManager();
		return OrganisationLegalEntity.getOrganisationLegalEntity(pm,	organisationID);
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException(
			"This instance of DunningLetter is currently not attached to a datastore! Cannot get a PersistenceManager!");

		return pm;
	}

}