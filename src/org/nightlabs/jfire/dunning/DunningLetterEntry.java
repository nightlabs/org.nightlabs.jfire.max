package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningLetterEntryID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

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
		table="JFireDunning_LetterEntry")
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
	private int dunningLevel;

	/**
	 * The invoice that is overdue and represented by this entry.
	 */
	private Invoice invoice;
	
	/**
	 * The money with which the interests are calculated.
	 * <p>
	 * 	This includes the invoice's price + any yearly interests, but <b>NOT</b> the dunning fees!
	 * </p>   
	 */
	private long baseAmount;
	
	/**
	 * The time (in milliseconds) that specify how long to wait
	 * before continuing the DunningProcess.
	 * 
	 * Copied during creation from corresponding dunningStep.
	 */
	private long periodOfGraceMSec;

	/**
	 * The due-date until which this DunningLetter needs to be
	 * paid. It is calculated when the DunningLetter is finalized
	 * (and null till finalization):  this.finalizeDT + this.periodOfGraceMSec.
	 */
	private Date extendedDueDateForPayment;

	/**
	 * The interests that already arose until the time
	 * of the creation of the corresponding DunningLetter.
	 * Note: These will include all the Interests that were
	 * already contained in the previous DunningLetter, but
	 * they may be summarized by the DunningInterestCalculator.
	 */
//	@Join(table="JFireDunning_DunningLetterEntry_dunningFees") // TODO: delete if not needed.
	@Persistent(mappedBy="dunningLetterEntry")
	private List<DunningInterest> dunningInterests;

	/**
	 * The total amount to pay for the overdue invoice.
	 */
	@Persistent(dependent="true")
	private Price priceIncludingInvoice;

	private DunningLetter dunningLetter;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningLetterEntry() { }

	public DunningLetterEntry(Invoice invoice)
	{
		this(invoice.getOrganisationID(), IDGenerator.nextID(DunningLetterEntry.class), invoice);
	}
	
	public DunningLetterEntry(String organisationID, long dunningLetterEntryID, Invoice invoice)
	{
		Organisation.assertValidOrganisationID(organisationID);
		assert invoice != null : "invoice must NOT be null!";

		this.organisationID = organisationID;
		this.dunningLetterEntryID = dunningLetterEntryID;
		this.invoice = invoice;
		this.baseAmount = invoice.getAmountToPay();
		this.dunningInterests = new LinkedList<DunningInterest>();
		this.priceIncludingInvoice = new Price(organisationID, IDGenerator.nextID(Price.class), invoice.getCurrency());
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getDunningLetterEntryID()
	{
		return dunningLetterEntryID;
	}

	public void setDunningLevel(int dunningLevel)
	{
		this.dunningLevel = dunningLevel;
	}
	
	public int getDunningLevel()
	{
		return dunningLevel;
	}

	public Invoice getInvoice()
	{
		return invoice;
	}
	
	public long getBaseAmount()
	{
		return baseAmount;
	}
	
	public void updateBaseAmount()
	{
		this.baseAmount = invoice.getAmountToPay();
		if (dunningInterests != null)
		{
			for (DunningInterest interest : dunningInterests)
			{
				this.baseAmount += interest.getAmountToPay();
			}
		}
	}
	
	public long getInterestAmountToPay()
	{
		long interestAmount = 0;
		if (dunningInterests != null)
		{
			for (DunningInterest interest : dunningInterests)
			{
				interestAmount += interest.getAmountToPay();
			}
		}
		return interestAmount;
	}
	
	public void updatePriceIncludingInvoice()
	{
		priceIncludingInvoice.setAmount(invoice.getAmountToPay() + getInterestAmountToPay());
	}

	public void setDunningLetter(DunningLetter dunningLetter)
	{
		this.dunningLetter = dunningLetter;
	}
	
	/**
	 * Returns the time (in milliseconds) that specify how long to wait
	 * before continuing the DunningProcess. This value is copied from its InvoiceDunningStep
	 * 
	 * @return long time
	 */
	public long getPeriodOfGraceMSec()
	{
		return periodOfGraceMSec;
	}

	/**
	 * Returns the due-date until which this DunningLetter needs to be
	 * paid. It is calculated when the DunningLetter is finalized
	 * (and null till finalization):  dunningLetter.finalizeDT + this.periodOfGraceMSec.
	 */
	public Date getExtendedDueDateForPayment()
	{
		return extendedDueDateForPayment;
	}

	/**
	 * Sets the due-date until which this DunningLetter needs to be
	 * paid. It is calculated when the DunningLetter is finalized
	 * (and null till finalization):  dunningLetter.finalizeDT + this.periodOfGraceMSec.
	 */
	public void setExtendedDueDateForPayment(Date extendedDueDateForPayment)
	{
		this.extendedDueDateForPayment = extendedDueDateForPayment;
	}

	public void addOrUpdateDunningInterest(DunningInterest dunningInterest)
	{
		if (dunningInterest == null)
			return;
		
		if (dunningInterests.contains(dunningInterest))
		{
			updatePriceIncludingInvoice();
		}
		else
		{
			priceIncludingInvoice.setAmount(priceIncludingInvoice.getAmount() + dunningInterest.getAmountToPay());
			dunningInterests.add(dunningInterest);
		}
	}
	
	public void setDunningValues(DunningStep step, int dunningLevel)
	{
		if (step == null)
			throw new IllegalArgumentException("The given invoiceDunningStep may NOT be null!");
		
		this.dunningLevel = dunningLevel;
		this.periodOfGraceMSec = step.getPeriodOfGraceMSec();
	}

	public List<DunningInterest> getDunningInterests()
	{
		return Collections.unmodifiableList(dunningInterests);
	}
	
	public DunningInterest getLatestDunningInterest()
	{
		if (dunningInterests.size() > 0)
			return dunningInterests.get(dunningInterests.size() - 1);

		return null;
	}

	public Price getPriceIncludingInvoice()
	{
		return priceIncludingInvoice;
	}

	public DunningLetter getDunningLetter()
	{
		return dunningLetter;
	}

//	public void copyInterestsFrom(DunningLetterEntry src)
//	{
//		this.dunningInterests.addAll(src.getDunningInterests());
//	}
	
	public boolean isOverdue(Date date)
	{
		return extendedDueDateForPayment.before(date);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result	+ (int) (dunningLetterEntryID ^ (dunningLetterEntryID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningLetterEntry other = (DunningLetterEntry) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(dunningLetterEntryID, other.dunningLetterEntryID))
			return true;
		
		return false;
	}

	@Override
	public String toString()
	{
		return "DunningLetterEntry [dunningLetterEntryID=" + dunningLetterEntryID + ", organisationID=" + organisationID
				+ "]";
	}

}