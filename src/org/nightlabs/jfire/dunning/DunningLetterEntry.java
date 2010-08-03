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
	@Column(length=100)
	private String dunningLetterEntryID;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int dunningLevel;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Invoice invoice;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long periodOfGraceMSec;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date extendedDueDateForPayment;
	
	@Join
	@Persistent(
		table="JFireDunning_DunningLetterEntry_dunningFees",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningInterest> dunningFees;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price priceIncludingInvoice;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningLetterEntry() { }
	
	public DunningLetterEntry(String organisationID, String dunningLetterEntryID) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningLetterEntryID, "dunningLetterEntryID"); //$NON-NLS-1$
	
		this.organisationID = organisationID;
		this.dunningLetterEntryID = dunningLetterEntryID;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getDunningLetterEntryID() {
		return dunningLetterEntryID;
	}
	
	public int getDunningLevel() {
		return dunningLevel;
	}
	
	public Invoice getInvoice() {
		return invoice;
	}
	
	public long getPeriodOfGraceMSec() {
		return periodOfGraceMSec;
	}
	
	public Date getExtendedDueDateForPayment() {
		return extendedDueDateForPayment;
	}
	
	public List<DunningInterest> getDunningFees() {
		return dunningFees;
	}
	
	public Price getPriceIncludingInvoice() {
		return priceIncludingInvoice;
	}
}