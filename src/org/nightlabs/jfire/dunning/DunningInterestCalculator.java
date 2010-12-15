package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * According to http://zinsmethoden.de/ (unfortunately only in German),
 * there are many possibilities to calculate an interest. The questions
 * that need to be decided by this calculator are: <br>

 * <br>1.How many days does the year have? This is important to divide the percentage accordingly (i.e. is a delay of payment of 1 day calculated as dueAmount * percentage / 360 or dueAmount * percentage / 365 or is the real calendar used)?
 * <br>2.What's the first day in the interest calculation? Is it the due date or the following day at midnight?
 * <br>3.What's the last day in the interest calculation? There are many options: The new due date, the finalization date of the DunningLetter or the creation date of the DunningLetter. And is this day included or excluded in the period?
 *
 * <br><br>Due to all these questions, the DunningInterestCalculator is abstract. But we provide a pretty customer-friendly default implementation (see below).

 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningInterestCalculatorID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningInterestCalculator"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class DunningInterestCalculator
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningInterestCalculator.class);

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningInterestCalculatorID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningConfig dunningConfig;
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningInterestCalculator() { }

	/**
	 * How many days does the year have? This is important to
	 * divide the percentage accordingly (i.e. is a delay of
	 * payment of 1 day calculated as dueAmount * percentage / 360
	 * or dueAmount * percentage / 365 or is the real calendar used)?
	 * @param year TODO
	 * @return
	 */
	protected abstract int getDaysOfYear(int year);

	/**
	 * What's the first day in the interest calculation? Is it the
	 * due date or the following day at midnight?
	 * @param dunningLetterEntry
	 * @return
	 */
	public abstract Date getFirstDay(DunningLetterEntry dunningLetterEntry);

	/**
	 * What's the last day in the interest calculation? There are many options:
	 * The new due date, the finalization date of the DunningLetter or the
	 * creation date of the DunningLetter. And is this day included or excluded
	 * in the period?
	 * @param dunningLetterEntry
	 * @return
	 */
	public abstract Date getLastDay(DunningLetterEntry dunningLetterEntry);

	/**
	 * Create new instances of {@link DunningInterest}s that calculate from 
	 * the previous {@link DunningInterest}s copied from the previous {@link DunningLetter}.  
	 * 
	 * It is important to note that the DunningInterestCalculator needs to know
	 * the previous DunningInterests (from the previous DunningLetter)
	 * in order to be able to merge Interests from adjacent time intervals
	 * and to realize that partial payments have already been conducted!
	 *
	 * @param prevLetter
	 * @param newLetter
	 */
	public void generateDunningInterests(DunningLetter prevLetter, DunningLetter newLetter) {
		if (prevLetter == null) { // Create the interests for the new letter
			for (DunningLetterEntry entry : newLetter.getEntries()) {
				DunningInterest interest = new DunningInterest(organisationID, IDGenerator.nextID(DunningInterest.class), entry, null);
				InvoiceDunningStep invoiceDunningStep = dunningConfig.getInvoiceDunningStep(entry.getDunningLevel());
				
				BigDecimal amount = BigDecimal.valueOf(interest.getBaseAmount());
				long interestAmount = amount.multiply(invoiceDunningStep.getInterestPercentage()).longValue();
				
				interest.setInterestAmount(interestAmount);
				interest.setInterestPercentage(invoiceDunningStep.getInterestPercentage());
				interest.setCreditPeriodFromIncl(getFirstDay(entry));
				interest.setCreditPeriodToExcl(getLastDay(entry));
				
				entry.addDunningInterest(interest);
			}
		}
		else { //Calculate the interests
			List<DunningLetterEntry> newLetterEntries = newLetter.getEntries();
			for (DunningLetterEntry newLetterEntry : newLetterEntries) {
				int dunningLevel = newLetterEntry.getDunningLevel();
				
				Date startDate = getFirstDay(newLetterEntry); //either the invoice due date or the extended due date
				Date endDate = getLastDay(newLetterEntry);
				
				DunningInterest backRefInterest = newLetterEntry.getLastestDunningInterest(); //Reference to the prev interest instance
				InvoiceDunningStep invoiceDunningStep = dunningConfig.getInvoiceDunningStep(dunningLevel);
				BigDecimal interestPercentage = invoiceDunningStep.getInterestPercentage();
				
				Calendar startDateCalendar = Calendar.getInstance();
				Calendar endDateCalendar = Calendar.getInstance();
				
				startDateCalendar.setTime(startDate);
				endDateCalendar.setTime(endDate);
				
				int startDateYear = startDateCalendar.get(Calendar.YEAR);
				int endDateYear = endDateCalendar.get(Calendar.YEAR);
				
				int currentYear = startDateCalendar.get(Calendar.YEAR);
				BigDecimal annualInterest = BigDecimal.valueOf(0);
				BigDecimal totalInterest = BigDecimal.valueOf(0);
				//Amount - Amount paid
				BigDecimal toPayAmount = BigDecimal.valueOf(newLetterEntry.getPriceIncludingInvoice().getAmount() - newLetterEntry.getLastestDunningInterest().getAmountPaid());
				while (currentYear < endDateYear) {
					int daysOfYearInt = getDaysOfYear(currentYear);
					BigDecimal daysOfYear = BigDecimal.valueOf(daysOfYearInt);
					annualInterest = annualInterest.add(totalInterest);
					int numDaysInt;
					if (currentYear == startDateYear) { //TODO I separated them for the new design of the interest, otherwise just combine the days for calculating.
						numDaysInt = daysOfYearInt - startDateCalendar.get(Calendar.DAY_OF_YEAR);
					}
					else if (currentYear == endDateYear) {
						numDaysInt = endDateCalendar.get(Calendar.DAY_OF_YEAR);
					}
					else {
						numDaysInt = daysOfYearInt;
					}
					
					BigDecimal numDays = BigDecimal.valueOf(numDaysInt);
					//(((Annual Interest+Amount)*Interest Percentage)/DaysOfYear)*Days
					totalInterest = interestPercentage.multiply(annualInterest.add(toPayAmount)).divide(daysOfYear).multiply(numDays);
					
					currentYear++;
				}
				
				DunningInterest interest = new DunningInterest(IDGenerator.getOrganisationID(), 
						IDGenerator.nextID(DunningInterest.class), 
						newLetterEntry, 
						backRefInterest);
				interest.setInterestAmount(totalInterest.longValue());
				interest.setInterestPercentage(interestPercentage);
				interest.setCreditPeriodFromIncl(startDate);
				interest.setCreditPeriodToExcl(endDate);
				
				newLetterEntry.addDunningInterest(interest);
			}
		}
	}

	/**
	 * Create an instance of <code>DunningInterestCalculator</code>.
	 *
	 */
	public DunningInterestCalculator(String organisationID, String dunningInterestCalculatorID) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningInterestCalculatorID = dunningInterestCalculatorID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getDunningInterestCalculatorID() {
		return dunningInterestCalculatorID;
	}

	public void setDunningConfig(DunningConfig dunningConfig) {
		this.dunningConfig = dunningConfig;
	}

	public DunningConfig getDunningConfig() {
		return dunningConfig;
	}
}