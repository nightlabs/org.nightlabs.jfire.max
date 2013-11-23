package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.apache.commons.lang.time.DateUtils;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When Invoice.dueDateForPayment is exceeded (i.e. NOW is after this timestamp)
 * and there is no DunningInvoiceProcess for the corresponding customer,
 * a new DunningProcess is either manually (via UI) or automatically created
 * (with the copy of its corresponding DunningConfig – a shallow copy is enough).<br>
 *
 * <br>Thus, there is at most one active DunningProcess for every customer in the database.
 * If there is one already, the existing DunningProcess is used and the overdue invoice
 * is added.<br>
 *
 * <br>The DunningProcess governs the generation of DunningLetters which may be skipped depending on the coolDownEnd.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marius Heinzmann <!-- Marius[at]NightLabs[dot]de -->
 */
@Queries({
	@javax.jdo.annotations.Query(
			name="getDunningProcessIDsByCustomer",
			value="SELECT JDOHelper.getObjectId(this) " +
					"WHERE JDOHelper.getObjectId(customer) == :customerID"
	),
	@javax.jdo.annotations.Query(
			name=DunningProcess.QUERY_GET_PROCESS_CUSTOMER_AND_CURRENCY,
			value="SELECT this " +
					"WHERE this.organisationID == :organisationID && this.legalEntityID == :customerID &&" +
					"this.currencyID == :currencyID"
	),
	@javax.jdo.annotations.Query(
			name=DunningProcess.QUERY_ACTIVE_PROCESSES_FOR_ORGANISATION,
			value="SELECT this " +
				"WHERE this.active == true && this.organisationID == :organisationID &&" +
				"	this.dunningConfig.autoMode != :dunningAutoMode"
	)
})
@PersistenceCapable(
		objectIdClass=DunningProcessID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_Process"
)
public class DunningProcess
	implements Serializable
{
	protected static final String QUERY_GET_PROCESS_CUSTOMER_AND_CURRENCY = "DunningProcess.getDunningProcessByCustomerAndCurrency";
	protected static final String QUERY_ACTIVE_PROCESSES_FOR_ORGANISATION = "DunningProcess.getActiveDunningProcessesForOrganisation";
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(DunningProcess.class);
	

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=300)
	private String legalEntityID;
	
	@PrimaryKey
	@Column(length=200)	
	private String currencyID;

	/**
	 * A deep copy of the DunningConfig for the corresponding customer.
	 */
	@Persistent(loadFetchGroup="all", dependent="true")
	private DunningConfig dunningConfig;

	/**
	 * The customer for which this DunningProcess has been created.
	 */
	@Persistent(loadFetchGroup="all")
	private LegalEntity customer;

	/**
	 * The currency used in all the invoices for that DunningProcess.
	 */
	@Persistent(loadFetchGroup="all")
	private Currency currency;
	
	@Persistent(mappedBy="dunningProcess") // table="JFireDunning_Process_dunningRuns",	
	private List<DunningRun> dunningRuns;
	
	/**
	 * The invoices included in the currently running DunningRun. 
	 */
	@Persistent(table="JFireDunning_Process_overdueInvoices")
	@Join
	private Set<Invoice> overdueInvoices;

	/**
	 * Marks the current Process as ongoing.
	 * <p></p>
	 * FIXME: Important: Make sure that when the payment is processed and the process is to be set inactive that there 
	 * were no new invoices added to the overdueInvoices in-between! If so, keep the process as active!
	 */
	private boolean active;
	
	/**
	 * The point of time after which new DunningLetters should be created again.
	 */
	private Date coolDownEnd;

	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningProcess() { }

	/**
	 * Create an instance of <code>DunningProcess</code>.
	 */
	public DunningProcess(String organisationID, LegalEntity legalEntity, Currency currency, DunningConfig dunningConfig)
	{
		Organisation.assertValidOrganisationID(organisationID);
		assert legalEntity != null;
		assert currency != null;
		assert dunningConfig != null;
		
		this.organisationID = organisationID;
		setCustomer(legalEntity);
		setCurrency(currency);
		this.dunningConfig = dunningConfig;
		this.overdueInvoices = new HashSet<Invoice>();
		this.dunningRuns = new LinkedList<DunningRun>();
	}

	protected void setCustomer(LegalEntity customer)
	{
		this.legalEntityID = AnchorID.create(
				customer.getOrganisationID(), customer.getAnchorTypeID(), customer.getAnchorID()
				).toString();
		this.customer = customer;
	}

	protected void setCurrency(Currency currency)
	{
		this.currency = currency;
		this.currencyID = CurrencyID.create(currency.getCurrencyID()).toString();
	}
	
	public String getOrganisationID()
	{
		return organisationID;
	}

	public String getCustomerID()
	{
		return legalEntityID;
	}
	
	public String getCurrencyID()
	{
		return currencyID;
	}

	public DunningConfig getDunningConfig()
	{
		return dunningConfig;
	}

	public LegalEntity getCustomer() {
		return customer;
	}

	public Currency getCurrency() {
		return currency;
	}

	public List<DunningRun> getDunningRuns()
	{
		return Collections.unmodifiableList(dunningRuns);
	}

	public void setCoolDownEnd(Date coolDownEnd) {
		this.coolDownEnd = coolDownEnd;
	}

	public Date getCoolDownEnd() {
		return coolDownEnd;
	}

	public boolean isActive()
	{
		return active;
	}

	protected void setActive(boolean active)
	{
		this.active = active;
	}
	
	/**
	 * Ends the currently active DunningRun and returns the now unused old DunningConfig.
	 * <p>
	 * 	<b>Important</b>: The last DunninRun cannot be ended if:
	 * 	<ul>
	 * 		<li>There is no last DunningRun! (obviously)</li>
	 * 		<li>The last DunningRunis already completed.</li>
	 * 		<li>There are newly added invoices not contained in the last DunningLetter, which aren't fully paid!</li>
	 * 	</ul>
	 * 	Note that the last DunningRun is ended even though there are newly added invoices not included in the last
	 * 	DunningLetter, if these are all fully-paid.
	 * </p>
	 * 
	 * @return The old DunningConfig that should be deleted as it is not used anymore.
	 */
	public DunningConfig endCurrentDunningRun()
	{
		// check if we can end it (last letter includes all invoices contained in the set of overdue ones)
		DunningLetter lastDunningLetter = getLastDunningLetter();
		if (lastDunningLetter == null)
			throw new IllegalStateException("Cannot end a DunningRun when there is none running!\n\t process = " + this);
		
		DunningRun lastDunningRun = getLastDunningRun();
		if (lastDunningRun.isCompleted())
			throw new IllegalStateException(
					"The last DunningRun is already completed! \n\t process = " + this + ", lastRun = " + lastDunningRun
			);
		
		DunningConfig oldConfig = dunningConfig;
		synchronized (overdueInvoices)
		{
			List<Invoice> newUnpaidInvoices = new LinkedList<Invoice>();
			List<Invoice> newFullyPaidInvoices = new LinkedList<Invoice>();
			
			for (Invoice overdueInvoice : overdueInvoices)
			{
				if (lastDunningLetter.getEntry(overdueInvoice) == null)
				{
					if (overdueInvoice.getAmountToPay() == 0)
						newFullyPaidInvoices.add(overdueInvoice);
					else
						newUnpaidInvoices.add(overdueInvoice);
				}
			}
			
			// TODO: Ask Marco if this default behaviour is acceptable! (Marius)
			if (! newUnpaidInvoices.isEmpty())
			{
				logger.warn("Cannot end active DunningRun as there newly added Invoices that are not aren't dunned, yet!" +
						"\n\t new unpaid invoices = {} \n\t new fully paid invoices = {}", newUnpaidInvoices, newFullyPaidInvoices
				);
				return null;
			}
			
			if (! newFullyPaidInvoices.isEmpty())
			{
				logger.info("There were invoices added after the last DunningLetter, but they are already fully paid." +
						"Hence will end the currently active DunningRun. \n\t fully paid invoices = {}", newFullyPaidInvoices
				);
			}
			
			// clear overdue invoices
			overdueInvoices.clear();
			
			// set endDate in the current Run
			lastDunningRun.setEndDate(new Date());
			
			// clear the DunningConfig
			this.dunningConfig = null;
			
			// mark inactive
			setActive(false);
		}
		
		return oldConfig;
	}
	
	/**
	 * Sets the given DunningConfig as Config to use if this process is NOT active.
	 * <p>
	 * 	<b>Important</b>: You can only set a DunningConfig when the DunningProcess is not active at the moment, otherwise
	 * 	an IllegalStateException is thrown. 
	 * </p>
	 *  
	 * @param dunningConfig The DunningConfig to use for the next DunningRun.
	 */
	public void setDunningConfig(DunningConfig dunningConfig)
	{
		if (isActive())
			throw new IllegalStateException(
					"Cannot set a new DunningConfig while still being active!" +
					"\n\t given config = " + dunningConfig
					);
		
		this.dunningConfig = dunningConfig;
	}
	
	/**
	 * Adds an invoice to the list of dunned invoices.
	 * <p>
	 *  This invoice is then included in the next dunning letter if still not paid. 
	 * </p> 
	 * 
	 * @param invoice The invoice to take care of.
	 */
	public void addInvoice(Invoice invoice)
	{
		synchronized (overdueInvoices)
		{
			if (overdueInvoices.contains(invoice))
				return;
				
			if (! isActive())
			{
				if (dunningConfig == null)
					throw new IllegalStateException("Cannot add an invoice to an inactive DunningProcess that has " +
					"NO DunningConfig set!");

				setActive(true);
				dunningRuns.add(new DunningRun(this));
			}
		
			overdueInvoices.add(invoice);			
		}
	}
	
	public DunningRun getLastDunningRun()
	{
		if (dunningRuns == null || dunningRuns.size() == 0)
			return null;
		
		return dunningRuns.get(dunningRuns.size() -1);
	}
	
	public DunningLetter getLastDunningLetter()
	{
		DunningRun lastRun = getLastDunningRun();
		if (lastRun == null)
			return null;
		
		List<DunningLetter> dunningLetters = lastRun.getDunningLetters();
		if (dunningLetters == null || dunningLetters.size() == 0)
			return null;
		
		return dunningLetters.get(dunningLetters.size() - 1);
	}

//	/**
//	 * Calculates the maximum dunning level of all {@link #overdueInvoices} for the given point in time.
//	 *  
//	 * @param referenceDate The time point that shall be used to calculate the maximum dunning level. 
//	 * @return the maximum dunning level of all {@link #overdueInvoices} for the given point in time.
//	 */
//	private int getMaxDunningLevelForDate(Date referenceDate)
//	{
//		if (overdueInvoices == null || overdueInvoices.isEmpty())
//			return 0;
//		
//		int maxLevel = 0;
//		for (Invoice overdueInvoice : overdueInvoices)
//		{
//			DunningLevelCalculationResult correspondingLevel = 
//				calculateDunningLevel(overdueInvoice, referenceDate, getDunningConfig().getDunningSteps());
//			
//			if (maxLevel < correspondingLevel.getDunningLevel())
//				maxLevel = correspondingLevel.getDunningLevel();
//		}
//		return maxLevel;
//	}
	
	/**
	 * Returns a new DunningLetter if one is created, <code>null</code> otherwise.
	 */
	public DunningLetter doAutomaticDunning(User creatingUser)
	{
		DunningAutoMode dunningAutoMode = getDunningConfig().getAutoMode();
		if (dunningAutoMode == null || DunningAutoMode.none == dunningAutoMode)
			return null;
		
		Date now = new Date();
		// only if there are invoices to dun & only create new letter if cool down period has ended.
		if (! isActive() || (getCoolDownEnd() != null && now.before(getCoolDownEnd())) )
			return null;
		
		// only create new DunningLetter if either a new Invoice is overdue or an already dunned Invoice increased its level.
		DunningLetter lastDunningLetter = getLastDunningLetter();
		if (lastDunningLetter != null)
		{
			Set<Invoice> newOverdueInvoices = new HashSet<Invoice>(overdueInvoices);
			int alreadyDunnedInvoiceLevelIncreased = -1;
			for (DunningLetterEntry entry : lastDunningLetter.getEntries())
			{
				newOverdueInvoices.remove(entry.getInvoice());
				DunningLevelCalculationResult newLevel = 
					calculateDunningLevel(entry.getInvoice(), now, getDunningConfig().getDunningSteps());
				
				if (entry.getDunningLevel() < newLevel.dunningLevel)
					alreadyDunnedInvoiceLevelIncreased = Math.max(alreadyDunnedInvoiceLevelIncreased, newLevel.dunningLevel);
			}

			if (newOverdueInvoices.isEmpty() && alreadyDunnedInvoiceLevelIncreased == -1)
				return null;
		}

		return createNewDunningLetter(creatingUser, dunningAutoMode == DunningAutoMode.createAndFinalize, new Date());
	}
	
	public DunningLetter createNewDunningLetter(User creatingUser, boolean isFinalized, Date targetDate)
	{
		List<DunningStep> dunningSteps = getDunningConfig().getDunningSteps();
		if (dunningSteps == null || dunningSteps.isEmpty()) 
		{
			throw new IllegalStateException(
					"The DunningProcess cannot create any letter due to a misconfigured " +
					"DunningConfig with no dunning steps!" +
					"\n\t organisationID=" + organisationID + ", legalEntityID=" + legalEntityID + ", currencyID=" + currencyID +
					"\n\t dunningConfigID=" + getDunningConfig().getDunningConfigID() +
					", dunningSteps = " + (dunningSteps == null ? "null" : 0)
					);
		}
		
		// get last DunningRun and create new one if necessary.
		DunningRun activeRun = getLastDunningRun();
		if (activeRun.isCompleted())
			activeRun = new DunningRun(this);
		
		DunningLetter prevDunningLetter = activeRun.getLastDunningLetter();
		DunningLetter newDunningLetter = new DunningLetter(activeRun, IDGenerator.nextID(DunningLetter.class));
		
		DunningLetterEntry prevLetterEntry = null;
		DunningInterestCalculator interestCalculator = dunningConfig.getInterestCalculator();
		
		// targetDate+1 at 00:00:00.000
		Date tomorrowZeroHundred = DateUtils.truncate(DateUtils.addDays(targetDate, 1), Calendar.DAY_OF_MONTH);
		for (Invoice dunnedInvoice : overdueInvoices)
		{
			DunningLetterEntry newLetterEntry = new DunningLetterEntry(dunnedInvoice);
			
			// calculate the invoice's dunning level and end date
			DunningLevelCalculationResult invoiceLevel = calculateDunningLevel(dunnedInvoice, tomorrowZeroHundred, dunningSteps);
			DunningStep dunningStep = invoiceLevel.getDunningStep();
			newLetterEntry.setDunningValues(dunningStep, invoiceLevel.getDunningLevel());
			// this is the date of round(invoice.finalizeDT) = next day at 00:00:00.000 + periodOfGraceMsecs.. 
			newLetterEntry.setExtendedDueDateForPayment(invoiceLevel.getPeriodEndDate());
			
			if (prevDunningLetter != null)
				prevLetterEntry = prevDunningLetter.getEntry(dunnedInvoice);
			
			// calculate the interests (IMPORTANT: make sure that the given date is at 00:00:00.000 otherwise
			interestCalculator.generateDunningInterests(dunningConfig, tomorrowZeroHundred, prevLetterEntry, newLetterEntry);
			newDunningLetter.addEntry(newLetterEntry);
		}
		
		DunningFeeAdder feeAdder = dunningConfig.getDunningFeeAdder();
		feeAdder.addDunningFees(prevDunningLetter, newDunningLetter, dunningConfig, currency);

		//Add the new letter to the list
		activeRun.addDunningLetter(newDunningLetter);
		
		// set coolDownEnd 
		DunningStep currentStep = dunningConfig.getDunningStep(newDunningLetter.getDunningLevel());
		setCoolDownEnd( new Date(tomorrowZeroHundred.getTime() + currentStep.getCoolDownPeriod()) );
		
		return newDunningLetter;
	}

//	public static void main(String[] args)
//	{
//		Date testDate = new Date();
//		System.out.println("Now = " + testDate);
//		System.out.println("Rounded date = " + DateUtils.round(testDate, Calendar.DAY_OF_MONTH));
//		Date truncatedDate = DateUtils.truncate(testDate, Calendar.DAY_OF_MONTH);
//		System.out.println("Truncated date = " + truncatedDate);
//		System.out.println("Rounded truncated date = " + DateUtils.round(truncatedDate, Calendar.DAY_OF_MONTH));
////		This is only available with apache-commons >= 2.6
////		Date ceiledDate = DateUtils.ceiling(testDate, Calendar.DAY_OF_MONTH);
////		System.out.println("Ceiled = " + ceiledDate);
////		System.out.println("Ceiled the ceiled = " + DateUtils.ceiling(ceiledDate, Calendar.DAY_OF_MONTH));
//	}
////	Now = Mon Apr 11 13:56:39 CEST 2011
////	Rounded date = Tue Apr 12 00:00:00 CEST 2011
////	Truncated date = Mon Apr 11 00:00:00 CEST 2011
////	Rounded truncated date = Mon Apr 11 00:00:00 CEST 2011
////	Ceiled = Tue Apr 12 00:00:00 CEST 2011
////	Ceiled the ceiled = Wed Apr 13 00:00:00 CEST 2011 - well, well, well
	
	private DunningLevelCalculationResult calculateDunningLevel(
			Invoice dunnedInvoice, Date now, List<DunningStep> dunningSteps)
	{
		if (dunningSteps == null || dunningSteps.isEmpty())
			throw new IllegalArgumentException("There must be at least one DunningStep present in the DunningConfig=" + 
					dunningConfig);
		
		DunningLetter lastLetter = getLastDunningLetter();
		if (lastLetter == null || !lastLetter.containsInvoice(dunnedInvoice))
		{
			// No previous letter or not contained in last letter  
			// -> invoice cannot be dunned previously -> level == 0
			DunningStep dunningStep = dunningSteps.iterator().next();
			Date endDate = new Date(now.getTime() + dunningStep.getPeriodOfGraceMSec());
			return new DunningLevelCalculationResult(endDate, 0, dunningStep);
		}
		else
		{
			// else check now is after dueDate then try to increase dunningLevel
			DunningLetterEntry entry = lastLetter.getEntry(dunnedInvoice);
			int newLevel = entry.getDunningLevel();

			Date endDate = entry.getExtendedDueDateForPayment();
			DunningStep dunningStep = dunningSteps.get(entry.getDunningLevel());
			
			// increase level if extendedDueDate is already over and if not at max level
			if (now.after(entry.getExtendedDueDateForPayment()) && newLevel +1 < dunningSteps.size())
			{
				newLevel++;
				dunningStep = dunningSteps.get(newLevel);
				endDate = new Date(entry.getExtendedDueDateForPayment().getTime() + dunningStep.getPeriodOfGraceMSec());				
			}

			return new DunningLevelCalculationResult(endDate, newLevel, dunningStep);
		}
		
//		// Assumes that the dueDateForPayment is not already aligned to the following day at 00:00:00.000
//		Date periodEndDate = DateUtils.truncate(DateUtils.addDays(dunnedInvoice.getDueDateForPayment(), 1), Calendar.DAY_OF_MONTH);
//		int dunningLevel = -1;
//		DunningStep dunningStep = null;
//		
//		if (now.before(dunnedInvoice.getDueDateForPayment()))
//		{
//			throw new IllegalStateException(
//					"No matching dunningStep found, because it's too early! now="+now+", finalizeDT="+
//					dunnedInvoice.getDueDateForPayment()
//			);
//		}
//
//		if (invoiceDunningSteps.isEmpty())
//			throw new IllegalStateException("Cannot determine dunningStep if there doesn't exist at least one!");
//		
//		do
//		{
//			dunningStep = invoiceDunningSteps.get(++dunningLevel);
//			periodEndDate = new Date(periodEndDate.getTime() + dunningStep.getPeriodOfGraceMSec());
//		} while (periodEndDate.before(now) && dunningLevel < invoiceDunningSteps.size());
//		
//		return new DunningLevelCalculationResult(periodEndDate, dunningLevel, dunningStep);
	}

	public static Collection<DunningProcessID> getDunningProcessesByCustomer(PersistenceManager pm, AnchorID customerID)
	{
		Query query = pm.newNamedQuery(DunningProcess.class, "getDunningProcessIDsByCustomer");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("customerID", customerID);
		return CollectionUtil.castList((List<?>) query.executeWithMap(params));
	}

	public static DunningProcess getDunningProcessByCustomerAndCurrency(
			PersistenceManager pm, String organisationID, AnchorID customerID, CurrencyID currencyID)
	{
		Query query = pm.newNamedQuery(DunningProcess.class, QUERY_GET_PROCESS_CUSTOMER_AND_CURRENCY);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("organisationID", organisationID);
		params.put("customerID", customerID.toString());
		params.put("currencyID", currencyID.toString());
		@SuppressWarnings("unchecked")
		List<DunningProcess> result = CollectionUtil.castList((List<DunningProcess>)query.executeWithMap(params));
		
		if (result == null || result.isEmpty())
			return null;
			
		if (result.size() > 1)
			throw new IllegalStateException("Founnd " + result.size() +" DunningProcesses for one customer and one currency!"+
					" This should not be possible! \n\tcustomerID=" + customerID + ", currencyID =" + currencyID);
		
		return result.iterator().next();
	}

	public static Collection<DunningProcess> getActiveDunningProcesses(PersistenceManager pm, String organisationID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		Query query = pm.newNamedQuery(DunningProcess.class, QUERY_ACTIVE_PROCESSES_FOR_ORGANISATION);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("organisationID", organisationID);
		params.put("dunningAutoMode", DunningAutoMode.none); // not none
		return CollectionUtil.castList((List<?>)query.executeWithMap(params));
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result	+ ((legalEntityID == null) ? 0 : legalEntityID.hashCode());
		result = prime * result	+ ((currencyID == null) ? 0 : currencyID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		DunningProcess other = (DunningProcess) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(legalEntityID, other.legalEntityID) &&
				Util.equals(currencyID, other.currencyID))
			return true;
		
		return false;
	}

	@Override
	public String toString()
	{
		return "DunningProcess [organisationID=" + organisationID + ", legalEntityID=" + legalEntityID + 
			", currencyID=" + currencyID +"]";
	}
	
	private static class DunningLevelCalculationResult
	{
		private final Date periodEndDate;
		private final int dunningLevel;
		private final DunningStep dunningStep;
		
		public DunningLevelCalculationResult(Date periodEndDate, int dunningLevel, DunningStep dunningStep)
		{
			super();
			this.periodEndDate = periodEndDate;
			this.dunningLevel = dunningLevel;
			this.dunningStep = dunningStep;
		}

		public Date getPeriodEndDate()
		{
			return periodEndDate;
		}

		public int getDunningLevel()
		{
			return dunningLevel;
		}

		public DunningStep getDunningStep()
		{
			return dunningStep;
		}
	}
}