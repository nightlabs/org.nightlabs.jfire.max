package org.nightlabs.jfire.dunning.customerfriendly;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.commons.lang.time.DateUtils;
import org.nightlabs.jfire.dunning.DunningConfig;
import org.nightlabs.jfire.dunning.DunningInterest;
import org.nightlabs.jfire.dunning.DunningInterestCalculator;
import org.nightlabs.jfire.dunning.DunningLetterEntry;
import org.nightlabs.jfire.dunning.DunningStep;
import org.nightlabs.jfire.idgenerator.IDGenerator;

/**
 * There is a default implementation of DunningInterestCalculator with the
 * following customer-friendly rules:
 *
 * 1.For every relevant year (i.e. the first and the last year) the real calendar number of days (which is at least 365) is used.
 * 2.The invoice's due-date is excluded from the credit period. Calculation starts at the following day at midnight. The same applies for all extended due-dates (i.e. previousDunningLetter.extendedDueDateForPayment).
 * 3.The last end date is always the finalization date of the newest DunningLetter. Of course, this means there would be holes if we directly copied the previous DunningInterests from the previous DunningLetter. Therefore, the last old DunningInterest is recalculated and its end-date is extended to match exactly the new DunningInterest's begin-date. If the interest percentages of the previous and the current DunningLetter are the same, the DunningInterests should be combined to one.
 * 4.In case there was a partial payment inbetween, the period containing the payment is split into two periods. The first period ends at the day before the payment and the new period (with the lower total amount to be paid) begins at midnight of the day of payment.
 * 5.Dunning fees are not included in the calculation of the interest.
 * 6.Interest is only compounded yearly by the calendar year (not monthly).
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class DunningInterestCalculatorCustomerFriendly
	extends DunningInterestCalculator
{
	private static final long serialVersionUID = 1L;

	public DunningInterestCalculatorCustomerFriendly(String organisationID)
	{
		this(organisationID, IDGenerator.nextID(DunningInterestCalculator.class));
	}

	public DunningInterestCalculatorCustomerFriendly(String organisationID, long dunningInterestCalculatorID)
	{
		super(organisationID, dunningInterestCalculatorID);
	}
	
	protected static int getYear(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int currentYear = cal.get(Calendar.YEAR);
		return currentYear;
	}
	
	protected static Date getEndOfYearDate(Date date)
	{
		GregorianCalendar endOfYearCal = new GregorianCalendar(getYear(date) + 1, 0, 1, 0, 0, 0);
		endOfYearCal.set(Calendar.MILLISECOND, 0);
		Date endOfYear = endOfYearCal.getTime();
		return endOfYear;
	}
	
	protected static Date getMinDate(Date dateOne, Date dateTwo)
	{
		if (dateOne.before(dateTwo))
			return dateOne;
		else
			return dateTwo;
	}
	
	/**
	 * 
	 * <p>
	 * 	Note: This method does create 0-amount {@link DunningInterest} objects for consistency reasons. Then you can
	 * 	always see how much interest was generated at a certain point of time.
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void generateDunningInterests(
			DunningConfig config, Date letterCreationDate,
			DunningLetterEntry prevLetterEntry, DunningLetterEntry newLetterEntry)
	{
		if (prevLetterEntry != null)
		{
			List<DunningInterest> oldInterests = prevLetterEntry.getDunningInterests();
			if (oldInterests != null)
			{
				// copy all the interests of the previous entry so that the model stays consistent even if the customer will
				// do a partial payment later on. (The old letters don't change afterwards)
				for (int i=0; i < oldInterests.size(); i++)
				{
					DunningInterest copy = new DunningInterest( newLetterEntry, oldInterests.get(i) );
					newLetterEntry.addOrUpdateDunningInterest(copy);
				}
			}
		}
		
		// check whether we can use the last interest object to include the new interests, too.
		DunningInterest currentInterest = newLetterEntry.getLatestDunningInterest();
		int level = newLetterEntry.getDunningLevel();
		if (currentInterest == null)
		{
			DunningStep firstStep = config.getDunningStep(level);
			
			// no interestPercentage set means: Ignore that timespan and reuse it for the next dunningStep.
			// (but only if this is the first letter for the corresponding invoice!
			if (prevLetterEntry == null && firstStep.getInterestPercentage() == null)
				return;
			
			// setup the initial DunningInterest, i.e. startDate == invoice.dueDate
			currentInterest = new DunningInterest(newLetterEntry);
			newLetterEntry.addOrUpdateDunningInterest(currentInterest);
			
			// this relies on dueDate of the invoice being an arbitrary point of time and not already the next
			// day at 00:00:00.000!
			Date startingDate = newLetterEntry.getInvoice().getDueDateForPayment();
			Date nextDayZeroHundred = DateUtils.truncate(DateUtils.addDays(startingDate, 1), Calendar.DAY_OF_MONTH);
			currentInterest.setCreditPeriodFromIncl(nextDayZeroHundred);
			currentInterest.setInterestPercentage(firstStep.getInterestPercentage());
			
			// The last day the current interest is valid is:
			//  1) when the end of the year happens (base amount changes)
			//  2) when current date is hit.
			Date currentDate = nextDayZeroHundred;
			int currentYear = getYear(currentDate);
			int endYear = getYear(letterCreationDate);
			while (currentYear < endYear)
			{
				Date endOfYear = getEndOfYearDate(currentDate);
				currentInterest.setCreditPeriodToExcl(endOfYear);
				currentInterest.calculateInterest(getDaysOfYear(currentYear));
				// add interest to base amount to include interest's interest for the next year.
				newLetterEntry.addOrUpdateDunningInterest(currentInterest);
				newLetterEntry.updateBaseAmount();
				
				currentYear++;
				currentDate = endOfYear;
				currentInterest = new DunningInterest(newLetterEntry);
				currentInterest.setCreditPeriodFromIncl(endOfYear);
				currentInterest.setInterestPercentage(firstStep.getInterestPercentage());
			}
			
			currentInterest.setCreditPeriodToExcl(letterCreationDate);
			currentInterest.calculateInterest(getDaysOfYear(endYear));
			newLetterEntry.addOrUpdateDunningInterest(currentInterest);
		}
		else
		{
			// since there is an interest object existing it had to be copied from the old entry, hence old entry != null
			level = prevLetterEntry.getDunningLevel();
			
			BigDecimal oldPercentage = config.getDunningStep(prevLetterEntry.getDunningLevel()).getInterestPercentage();
			BigDecimal newPercentage = config.getDunningStep(newLetterEntry.getDunningLevel()).getInterestPercentage();
			Date currentDate = currentInterest.getCreditPeriodToExcl();
			int currentYear = getYear(currentDate);
			Date endOfYear = getEndOfYearDate(currentDate);
			
			// walk along the time axis until we arrive at the current day
			while (currentDate.before(letterCreationDate))
			{
				int daysOfYear = getDaysOfYear(currentYear);
				Date extendedDueDate = prevLetterEntry.getExtendedDueDateForPayment();
				Date minDate = getMinDate(endOfYear, getMinDate(letterCreationDate, extendedDueDate));
				
				// The cases to consider:
				//  1) current date is hit -> we're done 
				//  2) the extended due date is hit -> end current interest & create new one with next dunningLevel 
				//  3) the endOfYear is hit -> end current interest, update base amount and create new interest with old level
				if (minDate == letterCreationDate)
				{
					currentInterest.setCreditPeriodToExcl(letterCreationDate);
					currentInterest.calculateInterest(daysOfYear);
					newLetterEntry.addOrUpdateDunningInterest(currentInterest);
					break;
				}
				else if (minDate == extendedDueDate)
				{
					currentInterest.setCreditPeriodToExcl(extendedDueDate);
					currentInterest.calculateInterest(daysOfYear);
					newLetterEntry.addOrUpdateDunningInterest(currentInterest);
					
					currentInterest = new DunningInterest(newLetterEntry);
					currentInterest.setCreditPeriodFromIncl(extendedDueDate);
					currentInterest.setInterestPercentage(newPercentage);
				}
				else
				{ // minDate == endOfYear
					currentInterest.setCreditPeriodToExcl(endOfYear);
					currentInterest.calculateInterest(daysOfYear);
					newLetterEntry.addOrUpdateDunningInterest(currentInterest);
					
					// add interest to base amount to include interest's interest for the next year.
					newLetterEntry.updateBaseAmount();
					
					// create new interest with same percentage
					currentInterest = new DunningInterest(newLetterEntry);
					currentInterest.setCreditPeriodFromIncl(endOfYear);
					currentInterest.setInterestPercentage(oldPercentage);
					
					// forward to next year;
					currentYear++;
				}
				currentDate = minDate;
			}
		}
	}
	
}
