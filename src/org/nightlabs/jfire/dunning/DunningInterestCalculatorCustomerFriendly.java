package org.nightlabs.jfire.dunning;

import java.util.Calendar;
import java.util.Date;

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
public class DunningInterestCalculatorCustomerFriendly 
extends DunningInterestCalculator
{

	@Override
	public int getDays() {
		return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	@Override
	public Date getFirstDay() {
		return null;
	}

	@Override
	public Date getLastDay() {
		return null;
	}
}
