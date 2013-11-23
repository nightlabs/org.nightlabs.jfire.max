package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.clone.DefaultCloneContext;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningFeeID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningFeeID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_Fee")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningFee 
	implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningFeeID;
	
	/**
	 * Back-reference to the owner-entity.
	 */
	private DunningLetter dunningLetter;
	
	/**
	 * Null or the one from which this one was copied (if it was copied from a previous DunningLetter).
	 */
	private DunningFee original;
	
	/**
	 * The general fee descriptor for which this concrete fee was created.
	 */
	private DunningFeeType dunningFeeType;
	
	/**
	 * The amount of money to pay.
	 */
	private Price price;
	
	/**
	 * The amount that was already paid. This could be a fraction 
	 * of the amount needed to be paid (partial payment).
	 */
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
	public DunningFee(String organisationID, long dunningFeeID, DunningFee original)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningFeeID = dunningFeeID;
		
		this.original = original;
	}

	public DunningFee(DunningLetter dunningLetter, DunningFeeType feeType, Currency currency)
	{
		assert dunningLetter != null : "dunningLetter must NOT be null!";
		assert feeType != null : "the feeType dunning fee must NOT be null!";
		this.organisationID = dunningLetter.getOrganisationID();
		this.dunningFeeID = IDGenerator.nextID(DunningFee.class);
		
		this.dunningFeeType = feeType;
		Price price = feeType.getPrice(currency);
		if (price == null)
			throw new IllegalStateException(
					"The given feeType has no Price set for the given Currency! " +
					"\n\tfeeType="+feeType.toString() +
					"\n\tcurrency="+currency.toString()
					);
		
		this.price = new DefaultCloneContext().createClone(price);
		this.amountPaid = 0;
	}
	
//	/**
//	 * 
//	 * @param dunningLetter
//	 * @param original
//	 */
//	public DunningFee(DunningLetter dunningLetter, DunningFee original)
//	{
//		assert dunningLetter != null : "dunningLetter must NOT be null!";
//		assert original != null : "the original dunning fee must NOT be null!";
//		this.organisationID = dunningLetter.getOrganisationID();
//		this.dunningFeeID = IDGenerator.nextID(DunningFee.class);
//		
//		this.dunningLetter = dunningLetter;
//		this.original = original;
//		if (original != null)
//		{
//			this.dunningFeeType = original.getDunningFeeType();
//			this.amountPaid = original.getAmountPaid();
//			
//			try
//			{
//				this.price = (Price) original.getPrice().clone();
//			}
//			catch (CloneNotSupportedException e)
//			{
//				throw new IllegalStateException(
//						"Couldn't clone a Price instance of type '" +	price.getClass().getName() + "'!", e);
//			} 
//		}
//		else
//			this.price = 
//	}
	
	public String getOrganisationID()
	{
		return organisationID;
	}
	
	public long getDunningFeeID()
	{
		return dunningFeeID;
	}
	
	public DunningFee getOriginal()
	{
		return original;
	}

	public void setDunningFeeType(DunningFeeType dunningFeeType)
	{
		this.dunningFeeType = dunningFeeType;
	}
	
	public DunningFeeType getDunningFeeType()
	{
		return dunningFeeType;
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
		return amountToPay;
	}
	
	public void setDunningLetter(DunningLetter dunningLetter)
	{
		this.dunningLetter = dunningLetter;
	}
	
	public DunningLetter getDunningLetter()
	{
		return dunningLetter;
	}

	public void setPaidDT(Date paidDT)
	{
		this.paidDT = paidDT;
	}
	
	public Date getPaidDT()
	{
		return paidDT;
	}
	
	public void setPrice(Price price)
	{
		this.price = price;
	}
	
	public Price getPrice()
	{
		return price;
	}

	public void copyValuesFrom(DunningFee oldFee)
	{
		if (oldFee == null)
			return;
		
		this.price = new DefaultCloneContext().createClone(oldFee.getPrice());
		this.amountPaid = oldFee.getAmountPaid();
		this.dunningFeeType = oldFee.dunningFeeType;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (dunningFeeID ^ (dunningFeeID >>> 32));
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningFee other = (DunningFee) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(dunningFeeID, other.dunningFeeID))
			return true;
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "DunningFee [organisationID=" + organisationID + ", dunningFeeID=" + dunningFeeID + ", dunningFeeType="
				+ dunningFeeType + ", price=" + price + ", amountPaid=" + amountPaid + ", paidDT=" + paidDT + ", original="
				+ original + "]";
	}

}