package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
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
	
	/**
	 * Back-reference to the owner-entity.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningLetter dunningLetter;
	
	/**
	 * Null or the one from which this one was copied (if it was copied from a previous DunningLetter).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningFee original;
	
	/**
	 * The general fee descriptor for which this concrete fee was created.
	 */
	@Persistent(
			dependent="true",
			mappedBy="dunningFee",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningFeeType dunningFeeType;
	
	/**
	 * The amount of money to pay.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price price;
	
	/**
	 * The amount that was already paid. This could be a fraction 
	 * of the amount needed to be paid (partial payment).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amountPaid;
	
	/**
	 * The remaining amount of money that is left to be paid 
	 * and is thus interestAmount – amountPaid.]
	 */
	private transient long amountToPay;
	
	/**
	 * The date at which all of this fee was paid. This implies that 
	 * as long as this field is set to null, there is still some part left 
	 * to be paid.
	 */
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
	 * @param original null or the one from which this one was copied (if it was copied from a previous DunningLetter).
	 */
	public DunningFee(String organisationID, String dunningFeeID, DunningFee original) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningFeeID, "dunningFeeID"); //$NON-NLS-1$
		
		this.organisationID = organisationID;
		this.dunningFeeID = dunningFeeID;
		
		this.original = original;
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
	
	public void setDunningLetter(DunningLetter dunningLetter) {
		this.dunningLetter = dunningLetter;
	}
	
	public DunningLetter getDunningLetter() {
		return dunningLetter;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dunningFeeID == null) ? 0 : dunningFeeID.hashCode());
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
		DunningFee other = (DunningFee) obj;
		if (dunningFeeID == null) {
			if (other.dunningFeeID != null)
				return false;
		} else if (!dunningFeeID.equals(other.dunningFeeID))
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
		return "DunningFee [dunningFeeID=" + dunningFeeID + ", organisationID="
				+ organisationID + "]";
	}
}