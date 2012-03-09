package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.dunning.id.DunningInterestID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
	objectIdClass=DunningInterestID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDunning_Interest"
)
public class DunningInterest
	implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningInterestID;

	/**
	 * Back-reference to the owner-entity.
	 */
	private DunningLetterEntry dunningLetterEntry;

	/**
	 * Back-reference to the old DunningInterest object (of the previous
	 * DunningLetter) that this one originates from.
	 */
	private DunningInterest backReference;

	/**
	 * The exact timestamp from which to calculate the interest. This is
	 * usually midnight of the following day after the due date,
	 * but it's up to the DunningInterestCalculator implementation to
	 * decide from when to when periods are counted
	 */
	private Date creditPeriodFromIncl;

	/**
	 * The exact timestamp till which to calculate the interest (excluding
	 * the exact millisecond as previous.creditPeriodFrom == next.creditPeriodTo).
	 */
	private Date creditPeriodToExcl;

	/**
	 * The currency in which this interest is calculated. This must match
	 * the invoice's currency.
	 */
	private Currency currency;

	/**
	 * The base from which the interest is calculated in the smallest currency
	 * unit (e.g. in Cent when EUR is used).
	 */
	private long baseAmount;

	/**
	 * The percentage that should be applied when calculating the interest.
	 * This is copied from either the current DunningStep (dunningLetter.dunningStep)
	 * or a previously created DunningInterest (that's either copied or recalculated).
	 */
	@Column(length=10, scale=6)
	private BigDecimal interestPercentage;

	/**
	 * The interest as absolute number in the smallest currency unit (e.g. in
	 * Cent when EUR is used).
	 */
	private long interestAmount;

	/**
	 * The amount that was already paid. This could be a fraction of the amount
	 * needed to be paid (partial payment).
	 */
	private long amountPaid;

	/**
	 * The date at which all of this interest was paid. This implies that as long
	 * as this field is set to null, there is still some part left to be paid.
	 */
	private Date paidDT;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningInterest() { }

	public DunningInterest(DunningLetterEntry dunningLetterEntry)
	{
		this(dunningLetterEntry.getOrganisationID(), IDGenerator.nextID(DunningInterest.class), dunningLetterEntry, null);
	}

	public DunningInterest(DunningLetterEntry dunningLetterEntry, DunningInterest backReference)
	{
		this(dunningLetterEntry.getOrganisationID(), IDGenerator.nextID(DunningInterest.class), dunningLetterEntry, 
				backReference);
	}
	
	public DunningInterest(String organisationID, long dunningInterestID,
			DunningLetterEntry dunningLetterEntry, DunningInterest backReference)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningInterestID = dunningInterestID;
		this.dunningLetterEntry = dunningLetterEntry;
		this.backReference = backReference;

		this.currency = dunningLetterEntry.getInvoice().getCurrency();
		if (backReference != null)
		{
			this.amountPaid = backReference.amountPaid;
			this.interestAmount = backReference.interestAmount;
			this.baseAmount = backReference.baseAmount;
			this.creditPeriodFromIncl = backReference.creditPeriodFromIncl;
			this.creditPeriodToExcl = backReference.creditPeriodToExcl;
			this.interestPercentage = backReference.interestPercentage;
		}
		else
		{
			this.baseAmount = dunningLetterEntry.getBaseAmount();
			this.interestPercentage = BigDecimal.valueOf(0l, 2);
		}
	}
	
	public long calculateInterest(int daysOfYear)
	{
		assert creditPeriodFromIncl != null;
		assert creditPeriodToExcl != null;
		
		// The interest percentage may be null in case we have a period with absolutely no interests,
		// e.g. the first two weeks after the first (reminding) dunning letter.
		if (interestPercentage == null)
		{
			return 0L;
		}
			
		Calendar startDay = Calendar.getInstance();
		Calendar endDay = Calendar.getInstance();
		startDay.setTime(creditPeriodFromIncl);
		endDay.setTime(creditPeriodToExcl);
		int endDayNr = endDay.get(Calendar.DAY_OF_YEAR);
		int startDayNr = startDay.get(Calendar.DAY_OF_YEAR);
		int periodLength = endDayNr - startDayNr;
		BigDecimal numberOfDays = BigDecimal.valueOf(periodLength, 0);
		BigDecimal numberOfDaysForThatYear = BigDecimal.valueOf(daysOfYear, 0);
		BigDecimal annualInterest = interestPercentage.multiply(BigDecimal.valueOf(baseAmount));
		BigDecimal interest =	annualInterest.multiply(numberOfDays);
		interest = interest.divide(numberOfDaysForThatYear, 0, RoundingMode.HALF_UP);
		this.interestAmount = interest.longValue();
		return interestAmount;
	}
	
	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getDunningInterestID()
	{
		return dunningInterestID;
	}

	public DunningLetterEntry getDunningLetterEntry()
	{
		return dunningLetterEntry;
	}

	public void setCreditPeriodFromIncl(Date creditPeriodFromIncl)
	{
		this.creditPeriodFromIncl = creditPeriodFromIncl;
	}

	public Date getCreditPeriodFromIncl()
	{
		return creditPeriodFromIncl;
	}

	public void setCreditPeriodToExcl(Date creditPeriodToExcl)
	{
		this.creditPeriodToExcl = creditPeriodToExcl;
	}

	public Date getCreditPeriodToExcl()
	{
		return creditPeriodToExcl;
	}

	public Currency getCurrency()
	{
		return currency;
	}

	/**
	 * Returns the base from which the interest is calculated in the smallest currency unit.
	 * @return long baseAmount
	 */
	public long getBaseAmount()
	{
		return baseAmount;
	}

	public void setInterestPercentage(BigDecimal interestPercentage)
	{
		this.interestPercentage = interestPercentage;
	}

	public BigDecimal getInterestPercentage()
	{
		return interestPercentage;
	}

	public void setInterestAmount(long interestAmount)
	{
		this.interestAmount = interestAmount;
	}

	public long getInterestAmount()
	{
		return interestAmount;
	}

	public void setAmountPaid(long amountPaid)
	{
		this.amountPaid = amountPaid;
	}

	public long getAmountPaid()
	{
		return amountPaid;
	}

	public long getAmountToPay()
	{
		return getInterestAmount() - getAmountPaid();
	}

	public void setPaidDT(Date paidDT)
	{
		this.paidDT = paidDT;
	}

	public Date getPaidDT()
	{
		return paidDT;
	}
	
	public DunningInterest getBackReference()
	{
		return backReference;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result	+ (int) (dunningInterestID ^ (dunningInterestID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningInterest other = (DunningInterest) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(dunningInterestID, other.dunningInterestID))
			return true;
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "DunningInterest [organisationID=" + organisationID + ", dunningInterestID=" + dunningInterestID
				+ ", currency=" + currency + ", baseAmount=" + baseAmount + ", interestAmount=" + interestAmount
				+ ", interestPercentage=" + interestPercentage + ", amountPaid=" + amountPaid + ", creditPeriodFromIncl="
				+ creditPeriodFromIncl + ", creditPeriodToExcl=" + creditPeriodToExcl + "]";
	}

}