package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningLetterEntryID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * A DunningLetterEntry contains all the information needed to list the corresponding
 * invoice in the the table of dunned invoices in the DunningLetter.
 *
 * <br>Among this information is the new due date, the interests already build up
 * and the severity of the dunning step which reflects how long the invoice
 * is overdue already.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningLetterEntryID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningLetterEntry")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningLetterEntry
implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningLetterEntryID;

	/**
	 * The severity of the dunning for the corresponding invoice,
	 * i.e. the InvoiceDunning step in which the process is for the invoice.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int dunningLevel;

	/**
	 * The invoice that is overdue and represented by this entry.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Invoice invoice;

	/**
	 * Copied during creation from corresponding dunningStep.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long periodOfGraceMSec;

	/**
	 * The due-date until which this DunningLetter needs to be
	 * paid. It is calculated when the DunningLetter is finalized
	 * (and null till finalization):  this.finalizeDT + this.periodOfGraceMSec.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date extendedDueDateForPayment;

	/**
	 * The interests that already arose until the time
	 * of the creation of the corresponding DunningLetter.
	 * Note: These will include all the Interests that were
	 * already contained in the previous DunningLetter, but
	 * they may be summarized by the DunningInterestCalculator.
	 */
	@Join
	@Persistent(
		table="JFireDunning_DunningLetterEntry_dunningFees",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningInterest> dunningInterests;

	/**
	 * The total amount to pay for the overdue invoice.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price priceIncludingInvoice;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningLetter dunningLetter;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningLetterEntry() { }

	public DunningLetterEntry(String organisationID, long dunningLetterEntryID, int dunningLevel, Invoice invoice, DunningLetter dunningLetter) {
		Organisation.assertValidOrganisationID(organisationID);

		this.organisationID = organisationID;
		this.dunningLetterEntryID = dunningLetterEntryID;
		this.dunningLevel = dunningLevel;
		this.invoice = invoice;
		this.dunningLetter = dunningLetter;

		this.priceIncludingInvoice = invoice.getPrice();
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public long getDunningLetterEntryID() {
		return dunningLetterEntryID;
	}

	public void setDunningLevel(int dunningLevel) {
		this.dunningLevel = dunningLevel;
	}
	
	public int getDunningLevel() {
		return dunningLevel;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setPeriodOfGraceMSec(long periodOfGraceMSec) {
		this.periodOfGraceMSec = periodOfGraceMSec;
	}

	/**
	 * Returns the time (in milliseconds) that specify how long to wait
	 * before continuing the DunningProcess. This value is copied from its InvoiceDunningStep
	 * 
	 * @return long time
	 */
	public long getPeriodOfGraceMSec() {
		return periodOfGraceMSec;
	}

	/**
	 * Returns The due-date until which this DunningLetter needs to be
	 * paid. It is calculated when the DunningLetter is finalized
	 * (and null till finalization):  dunningLetter.finalizeDT + this.periodOfGraceMSec.
	 */
	public Date getExtendedDueDateForPayment() {
		return extendedDueDateForPayment;
	}

	public void setExtendedDueDateForPayment(Date extendedDueDateForPayment) {
		this.extendedDueDateForPayment = extendedDueDateForPayment;
	}

	public void addDunningInterest(DunningInterest dunningInterest) {
		this.priceIncludingInvoice.setAmount(priceIncludingInvoice.getAmount() + dunningInterest.getInterestAmount());
		dunningInterests.add(dunningInterest);
	}

	public List<DunningInterest> getDunningInterests() {
		return Collections.unmodifiableList(dunningInterests);
	}

	public Price getPriceIncludingInvoice() {
		return priceIncludingInvoice;
	}

	public DunningLetter getDunningLetter() {
		return dunningLetter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (dunningLetterEntryID ^ (dunningLetterEntryID >>> 32));
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	public boolean isOverdue(Date date) {
		return extendedDueDateForPayment.before(date);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DunningLetterEntry other = (DunningLetterEntry) obj;
		if (dunningLetterEntryID != other.dunningLetterEntryID)
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
		return "DunningLetterEntry [dunningLetterEntryID="
				+ dunningLetterEntryID + ", organisationID=" + organisationID
				+ "]";
	}
}