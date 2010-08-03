package org.nightlabs.jfire.dunning;

import java.io.Serializable;
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

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningLetterID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * A DunningLetter represents the letter send to a customer 
 * which may contain several overdue invoices and its potentially 
 * increased costs (including the interests for each invoice and 
 * dunning level dependent fees). 
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningLetterID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningLetter")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningLetter 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningLetterID;
	
	/**
	 * The process to which this DunningLetter belongs.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningProcess dunningProcess;
	
	/**
	 * The overall dunning level of the letter. Most likely, 
	 * this will be the highest level of all included invoices.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Integer dunningLevel;
	
	/**
	 * The information of each overdue invoice needed to print the letter. 
	 * This includes the dunning level, the original invoice, the interest 
	 * for that invoice, the extended due date, etc.
	 */
	@Join
	@Persistent(
		table="JFireDunning_DunningLetter_dunnedInvoices",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningLetterEntry> dunnedInvoices;
	
	/**
	 * Contains all old fees (from the previous DunningLetter) as well as 
	 * all new ones (based on dunningStep.feeTypes).
	 */
	@Join
	@Persistent(
		table="JFireDunning_DunningLetter_dunningFees",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningFee> dunningFees;
	
	/**
	 * The timestamp when this DunningLetter was finalized. 
	 * It is important that the DunningLetterNotifiers are triggered 
	 * when this field is set manually!
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date finalizeDT;
	
	/**
	 * Null or the timestamp when all the fees and interests were booked.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date bookDT;
	
	/**
	 * The total amount of fees and interests to pay.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price priceExcludingInvoices;
	
	/**
	 * The total amount of this DunningLetter comprising the invoice, 
	 * all fees and interests. It always comprises the complete amount 
	 * of all invoices summarized!
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price priceIncludingInvoices;
	
	/**
	 * Everything that was paid for the fees and interests and all previous 
	 * DunningLetters so far, before this DunningLetter was created.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amountPaidExcludingInvoices;
	
	
	private transient long amountToPay;
	
	/**
	 * A flag indicating that this DunningLetter is still open 
	 * and waits for payment (of the amountToPay). This flag 
	 * should be cleared immediately when all invoices are paid 
	 * completely including the DunningLetter's fees and interest. 
	 * 
	 * We thus need to register an InvoiceActionHandler for every invoice 
	 * that is part of a dunning process. If the invoice is paid without 
	 * the dunning fees+interests, the dunning process is not complete and 
	 * should be continued with the remaining amount (unless the organisation 
	 * voluntarily gives up the dunning costs due to customer-friendlyness).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean outstanding;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningLetter() { }
	
	public DunningLetter(String organisationID, String dunningLetterID, DunningProcess dunningProcess) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningLetterID, "dunningLetterID"); //$NON-NLS-1$
		this.organisationID = organisationID;
		this.dunningLetterID = dunningLetterID;
		this.dunningProcess = dunningProcess;
		
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getDunningLetterID() {
		return dunningLetterID;
	}
	
	public DunningProcess getDunningProcess() {
		return dunningProcess;
	}
	
	public Integer getDunningLevel() {
		return dunningLevel;
	}
	
	public List<DunningLetterEntry> getDunnedInvoices() {
		return dunnedInvoices;
	}
	
	public List<DunningFee> getDunningFees() {
		return dunningFees;
	}
	
	public Date getFinalizeDT() {
		return finalizeDT;
	}
	
	public Date getBookDT() {
		return bookDT;
	}
	
	public Price getPriceExcludingInvoices() {
		return priceExcludingInvoices;
	}
	
	public Price getPriceIncludingInvoices() {
		return priceIncludingInvoices;
	}
	
	public long getAmountPaidExcludingInvoices() {
		return amountPaidExcludingInvoices;
	}
	
	public long getAmountToPay() {
		return amountToPay;
	}

	public boolean isOutstanding() {
		return outstanding;
	}
}