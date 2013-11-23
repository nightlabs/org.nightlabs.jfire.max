package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.clone.DefaultCloneContext;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningFeeTypeID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.l10n.Currency;
import org.nightlabs.util.Util;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningFeeType.FETCH_GROUP_NAME,
		members=@Persistent(name="name")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningFeeType.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")
	),
	@FetchGroup(
			fetchGroups={"default"},
			name=DunningFeeType.FETCH_GROUP_CURRENCY_PRICE_MAPPING,
			members=@Persistent(name="currency2price")
		)
})
@PersistenceCapable(
		objectIdClass=DunningFeeTypeID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_FeeType")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningFeeType 
	implements Serializable, CloneableWithContext
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "DunningFeeType.name";
	public static final String FETCH_GROUP_DESCRIPTION = "DunningFeeType.description";
	public static final String FETCH_GROUP_CURRENCY_PRICE_MAPPING = "DunningFeeType.currency2price";
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long feeTypeID;
	
	@Persistent(dependent="true")
	private DunningFeeTypeName name;
	
	@Persistent(dependent="true")
	private DunningFeeTypeDescription description;
	
	/**
	 * The price to be charged additionally to the invoice amount, 
	 * the interest and all previous fees.
	 */
	@Join
	@Persistent(table="JFireDunning_FeeType_currency2price", dependentValue="true")
	private Map<String, Price> currencyID2price;
	
//	This was not possible as Datanucleus (2.1.0-m3) always returned map entries will null-keys. Seems like a DN-Bug. 
//	@Key(mappedBy="currency")
//	private Map<Currency, Price> currency2price;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningFeeType() { }
	
	/**
	 * Create an instance of <code>DunningFeeType</code>.
	 *
	 * @param organisationID first part of the primary key. The organisation which created this object.
	 * @param dunningFeeTypeID second part of the primary key. A local identifier within the namespace of the organisation.
	 */
	public DunningFeeType(String organisationID, long dunningFeeTypeID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		
		this.organisationID = organisationID;
		this.feeTypeID = dunningFeeTypeID;
		
		this.name = new DunningFeeTypeName(this);
		this.description = new DunningFeeTypeDescription(this);
//		this.currencyID2price = new HashMap<Currency, Price>();
		this.currencyID2price = new HashMap<String, Price>();
	}
	
	public String getOrganisationID()
	{
		return organisationID;
	}
	
	public long getFeeTypeID()
	{
		return feeTypeID;
	}
	
	public DunningFeeTypeName getName()
	{
		return name;
	}
	
	public DunningFeeTypeDescription getDescription()
	{
		return description;
	}
	
//	public Map<String, Price> getCurrency2price()
//	{
//		return currencyID2price;
//	}
	
	public Price getPrice(Currency currency)
	{
		return getPrice(currency.getCurrencyID());
	}
	
	public Price getPrice(String currencyID)
	{
		return currencyID2price.get(currencyID);
	}
	
	public void setPrice(Price price)
	{
		if (price == null)
			return;
		
		if (price.getCurrency() == null)
			throw new IllegalArgumentException("The given price must have a Currency set! price = " + price.getPrimaryKey());
		
		currencyID2price.put(price.getCurrency().getCurrencyID(), price);
	}

	public DunningFee createDunningFee(String organisationID, Currency currency, DunningFee originalFee)
	{
		DunningFee fee = new DunningFee(organisationID, IDGenerator.nextID(DunningFee.class), originalFee);
		fee.setAmountPaid(0);

		Price amountToPay = getPrice(currency);
		if (amountToPay == null)
			throw new IllegalStateException("No mapping for currency '" + currency + "' found for DunningFeeType '" +
					getClass().getName() + "'!\n Cannot create a new DunningFee without it!");
		
		fee.setPrice( new DefaultCloneContext().createClone(amountToPay) );
		fee.setDunningFeeType(this);

		return fee;
	}
	
	@Override
	public DunningFeeType clone(CloneContext context, boolean cloneReferences)
	{
//	WORKAROUND - JDO does not support cloning a detached object and consider it transient! ( http://www.jpox.org/servlet/forum/viewthread_thread,1865 )
//	DunningFeeType clone = (DunningFeeType) super.clone();
		DunningFeeType clone = ReflectUtil.newInstanceRuntimeException(getClass());
		clone.organisationID = organisationID;
//	END OF WORKAROUND
		
		clone.feeTypeID = IDGenerator.nextID(getClass());
		
		if (cloneReferences)
		{
			clone.name = new DunningFeeTypeName(clone);
			clone.name.copyFrom(name);
			clone.description = new DunningFeeTypeDescription(clone);
			clone.description.copyFrom(description);
			if (currencyID2price != null)
			{
				clone.currencyID2price = new HashMap<String, Price>(currencyID2price.size());
				
				for (Price price : currencyID2price.values())
				{
					context.createClone(price);
				}
			}
		}
		else
		{
			if (currencyID2price != null)
				clone.currencyID2price = new HashMap<String, Price>(currencyID2price);
			else
				clone.currencyID2price = new HashMap<String, Price>();
		}
		return clone;
	}

	@Override
	public void updateReferencesOfClone(CloneableWithContext clone, CloneContext context)
	{
		DunningFeeType clonedFeeType = (DunningFeeType) clone;
		if (currencyID2price != null)
		{
			for (Map.Entry<String, Price> cur2Price : currencyID2price.entrySet())
			{
				clonedFeeType.currencyID2price.put(cur2Price.getKey(), context.getClone(cur2Price.getValue()));
			}
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (feeTypeID ^ (feeTypeID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningFeeType other = (DunningFeeType) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(feeTypeID, other.feeTypeID))
			return true;
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final int maxLen = 10;
		return "DunningFeeType [organisationID=" + organisationID + ", dunningFeeTypeID=" + feeTypeID + ", name="
				+ name + ", currency2price=" + (currencyID2price != null ? toString(currencyID2price.entrySet(), maxLen) : null)
				+ "]";
	}

	private String toString(Collection<?> collection, int maxLen)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++)
		{
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}

}