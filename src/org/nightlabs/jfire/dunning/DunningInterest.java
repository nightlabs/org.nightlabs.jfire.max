package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.dunning.id.DunningInterestID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningInterestID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningInterest")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
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
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningLetterEntry dunningLetterEntry;

	/**
	 * Back-reference to the old DunningInterest object (of the previous
	 * DunningLetter) that this one originates from.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningInterest backReference;

	/**
	 * The exact timestamp from which to calculate the interest. This is
	 * usually midnight of the following day after the due date,
	 * but it's up to the DunningInterestCalculator implementation to
	 * decide from when to when periods are counted
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date creditPeriodFromIncl;

	/**
	 * The exact timestamp till which to calculate the interest (excluding
	 * the exact millisecond as previous.creditPeriodFrom == next.creditPeriodTo).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date creditPeriodToExcl;

	/**
	 * The currency in which this interest is calculated. This must match
	 * the invoice's currency.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Currency currency;

	/**
	 * The base from which the interest is calculated in the smallest currency
	 * unit (e.g. in Cent when EUR is used).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long baseAmount;

	/**
	 * The percentage that should be applied when calculating the interest.
	 * This is copied from either the current DunningStep (dunningLetter.dunningStep)
	 * or a previously created DunningInterest (that's either copied or recalculated).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private BigDecimal interestPercentage;

	/**
	 * The interest as absolute number in the smallest currency unit (e.g. in
	 * Cent when EUR is used).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long interestAmount;

	/**
	 * The amount that was already paid. This could be a fraction of the amount
	 * needed to be paid (partial payment).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amountPaid;

	/**
	 * The remaining amount of money that is left to be paid
	 * and is thus interestAmount – amountPaid.]
	 */
	private transient long amountToPay;

	/**
	 * The date at which all of this interest was paid. This implies that as long
	 * as this field is set to null, there is still some part left to be paid.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date paidDT;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningInterest() { }


	public DunningInterest(String organisationID, long dunningInterestID,
			DunningLetterEntry dunningLetterEntry, DunningInterest backReference) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningInterestID = dunningInterestID;
		this.dunningLetterEntry = dunningLetterEntry;
		this.backReference = backReference;

		this.currency = dunningLetterEntry.getInvoice().getCurrency();
		this.baseAmount = dunningLetterEntry.getInvoice().getPrice().getAmount();
	}


	public String getOrganisationID() {
		return organisationID;
	}

	public long getDunningInterestID() {
		return dunningInterestID;
	}

	public DunningLetterEntry getDunningLetterEntry() {
		return dunningLetterEntry;
	}

	public void setCreditPeriodFromIncl(Date creditPeriodFromIncl) {
		this.creditPeriodFromIncl = creditPeriodFromIncl;
	}

	public Date getCreditPeriodFromIncl() {
		return creditPeriodFromIncl;
	}

	public void setCreditPeriodToExcl(Date creditPeriodToExcl) {
		this.creditPeriodToExcl = creditPeriodToExcl;
	}

	public Date getCreditPeriodToExcl() {
		return creditPeriodToExcl;
	}

	public Currency getCurrency() {
		return currency;
	}

	public long getBaseAmount() {
		return baseAmount;
	}

	public void setInterestPercentage(BigDecimal interestPercentage) {
		this.interestPercentage = interestPercentage;
	}

	public BigDecimal getInterestPercentage() {
		return interestPercentage;
	}

	public void setInterestAmount(long interestAmount) {
		this.interestAmount = interestAmount;
	}

	public long getInterestAmount() {
		return interestAmount;
	}

	public void setAmountPaid(long amountPaid) {
		this.amountPaid = amountPaid;
	}

	public long getAmountPaid() {
		return amountPaid;
	}

	public void setAmountToPay(long amountToPay) {
		this.amountToPay = amountToPay;
	}

	public long getAmountToPay() {
		return amountToPay;
	}

	public void setPaidDT(Date paidDT) {
		this.paidDT = paidDT;
	}

	public Date getPaidDT() {
		return paidDT;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (dunningInterestID ^ (dunningInterestID >>> 32));
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
		DunningInterest other = (DunningInterest) obj;
		if (dunningInterestID != other.dunningInterestID)
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
		return "DunningInterest [dunningInterestID=" + dunningInterestID
				+ ", organisationID=" + organisationID + "]";
	}
}