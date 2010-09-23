package org.nightlabs.jfire.dunning;

import java.util.Calendar;
import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.organisation.Organisation;

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
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningInterestCalculatorCustomerFriendly"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningInterestCalculatorCustomerFriendly 
extends DunningInterestCalculator
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningInterestCalculatorCustomerFriendly.class);
	
	public static final DunningInterestCalculatorID ID = DunningInterestCalculatorID.create(Organisation.DEV_ORGANISATION_ID, "Dunning Interest Calculator Customer Friendly");
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningInterestCalculatorCustomerFriendly() { }

	public DunningInterestCalculatorCustomerFriendly(String organisationID, String dunningInterestCalculatorID)
	{
		super(organisationID, dunningInterestCalculatorID);
	}
	
	@Override
	public int getDays() {
		return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	@Override
	public Date getFirstDay(DunningLetterEntry dunningLetterEntry) {
		Date dueDate = dunningLetterEntry.getInvoice().getDueDateForPayment();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dueDate);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.roll(Calendar.DATE, -1);
		return cal.getTime();
	}

	@Override
	public Date getLastDay(DunningLetterEntry dunningLetterEntry) {
		return dunningLetterEntry.getDunningLetter().getFinalizeDT();
	}
}
