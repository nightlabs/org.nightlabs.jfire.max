package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningFeeID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningFeeID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningFee")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningFee 
implements Serializable
{
private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningFeeID;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningLetterEntry dunningLetterEntry;
	
	@Persistent(
			dependent="true",
			mappedBy="dunningFee",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningFeeType dunningFeeType;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price price;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amountPaid;
	
	private transient long amountToPay;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date paidDT;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningFee() { }
	
	/**
	 * Create an instance of <code>DunningFee</code>.
	 *
	 * @param organisationID first part of the primary key. The organisation which created this object.
	 * @param dunningFeeID second part of the primary key. A local identifier within the namespace of the organisation.
	 */
	public DunningFee(String organisationID, String dunningFeeID) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningFeeID, "dunningFeeID"); //$NON-NLS-1$
		
		this.organisationID = organisationID;
		this.dunningFeeID = dunningFeeID;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getDunningFeeID() {
		return dunningFeeID;
	}

	public void setDunningFeeType(DunningFeeType dunningFeeType) {
		this.dunningFeeType = dunningFeeType;
	}
	
	public DunningFeeType getDunningFeeType() {
		return dunningFeeType;
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
	
	public void setDunningLetterEntry(DunningLetterEntry dunningLetterEntry) {
		this.dunningLetterEntry = dunningLetterEntry;
	}
	
	public DunningLetterEntry getDunningLetterEntry() {
		return dunningLetterEntry;
	}

	public void setPaidDT(Date paidDT) {
		this.paidDT = paidDT;
	}
	
	public Date getPaidDT() {
		return paidDT;
	}
	
	public void setPrice(Price price) {
		this.price = price;
	}
	
	public Price getPrice() {
		return price;
	}
}