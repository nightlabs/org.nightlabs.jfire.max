package org.nightlabs.jfire.accounting;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Unique;

import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer.BookType;
import org.nightlabs.jfire.accounting.id.PayableObjectMoneyTransferFactoryJDOID;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * 
 * @author mheinzmann
 */
@PersistenceCapable(
		objectIdClass=PayableObjectMoneyTransferFactoryJDOID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_MoneyTransferFactory"
)
@Queries({
	@javax.jdo.annotations.Query(
			name=PayableObjectMoneyTransferFactoryJDO.QUERY_FACTORY_BY_SCOPE_AND_TYPE,
			value="SELECT this from PayableObjectMoneyTransferFactoryJDO this" +
					  " WHERE this.organisationID == :organisationID && this.scope == :scope &&" +
					  "       this.payableObjectType == :payableObjectType"
	)
})
@Unique(
		name="PayableObjectMoneyTransferFactoryJDO_scopeObjectTypeUniqueness", 
		members={"organisationID", "scope", "payableObjectType"}
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class PayableObjectMoneyTransferFactoryJDO implements PayableObjectMoneyTransferFactory
{
	public static final String QUERY_FACTORY_BY_SCOPE_AND_TYPE = "getMoneyTransferFactoryByScopeAndObjectType";
	
	@PrimaryKey
	private String organisationID;
	
	@PrimaryKey
	private String payableObjectType;
	
	/**
	 * The scope of the mapping, i.e. the context it is relevant for.
	 */
	@PrimaryKey
	private String scope;
	
	/**
	 * @deprecated JDO only.
	 */
	protected PayableObjectMoneyTransferFactoryJDO() {}

	/**
	 * Creates a global mapping for the given scope.
	 * 
	 * @param scope
	 * @param payableObjectType
	 * @param moneyTransferType
	 */
	public PayableObjectMoneyTransferFactoryJDO(
			String scope, Class<? extends PayableObject> payableObjectType
			)
	{
		this(Organisation.DEV_ORGANISATION_ID, scope, payableObjectType);
	}

	public PayableObjectMoneyTransferFactoryJDO(
			String organisationID, String scope, 
			Class<? extends PayableObject> payableObjectType
			)
	{
		assert organisationID != null;
		Organisation.assertValidOrganisationID(organisationID);
		assert scope != null;
		assert payableObjectType != null;
		
		this.organisationID = organisationID;
		this.scope = scope;
		this.payableObjectType = payableObjectType.getName();
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	
	/**
	 * @return the scope of this entry.
	 */
	public String getScope()
	{
		return scope;
	}

	/**
	 * @return the payableObjectClass
	 */
	public String getPayableObjectClass()
	{
		return payableObjectType;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PayableObjectMoneyTransfer<?> createMoneyTransfer(
			MoneyTransfer container, BookType bookType, PayableObject payableObject, Anchor from, Anchor to, long amount)
	{
		assert container != null;
		assert payableObject != null;
		
		Class<? extends PayableObject> payObjectClass = null;
		try {
			payObjectClass = (Class<? extends PayableObject>) Class.forName(this.payableObjectType);
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalStateException("The referenced payableObjectType seems to NOT be deployed!", e);
		}
		
		if (!payObjectClass.isInstance(payableObject))
		{
			throw new IllegalArgumentException(
					"This PayableMoneyTransferFactory '"+ getClass().getName() 
					+"'cannot handle type '"+ payableObject.getClass().getName() +"'!"
					);
		}

		return doCreateMoneyTransfer(container, bookType, payableObject, from, to, amount);
	}
	
	protected abstract PayableObjectMoneyTransfer<?> doCreateMoneyTransfer(MoneyTransfer container, BookType bookType,
			PayableObject payableObject, Anchor from, Anchor to, long amount);

	/**
	 * Does a lookup for the MoneyTransferFactory for the given objectType with the given organisationID and scope.
	 * If no organisationID is given, the lookup is done with the {@link Organisation#DEV_ORGANISATION_ID}.
	 * 
	 * <p>It does it in the following way:
	 *  <nl>
	 *   <li>Do a lookup with the given organisationID if a factory is found return it.</li>
	 *   <li>If none was found try with the default {@link Organisation#DEV_ORGANISATION_ID}.</li>
	 *  </nl>
	 * </p>
	 * 
	 * @param pm The PersistenceManager to use.
	 * @param organisationID The organisationID to lookup with.
	 * @param scope The scope of the lookup.
	 * @param objectClass The object class the MoneyTransfer must match.
	 * @return the MoneyTransferFactory for the given objectType with the given organisationID and scope.
	 */
	public static PayableObjectMoneyTransferFactory getFactory(
			PersistenceManager pm, 
			String organisationID, String scope, Class<? extends PayableObject> objectClass)
	{
		if (pm == null || pm.isClosed())
		{
			throw new IllegalArgumentException("The PersistenceManager must not be null or closed");
		}
		if (organisationID == null)
		{
			organisationID = Organisation.DEV_ORGANISATION_ID;
		}
		
		Query query = pm.newNamedQuery(PayableObjectMoneyTransferFactoryJDO.class, QUERY_FACTORY_BY_SCOPE_AND_TYPE);
		
		@SuppressWarnings("unchecked")
		Collection<PayableObjectMoneyTransferFactoryJDO> result = 
			(Collection<PayableObjectMoneyTransferFactoryJDO>) query.execute(organisationID, scope, objectClass.getName());

		if (result != null && ! result.isEmpty())
		{
			return result.iterator().next();
		}

		// if we already checked for fallback then stop searching
		if (Organisation.DEV_ORGANISATION_ID.equals(organisationID))
		{
			return null;
		}
		
		// else check for fallback Factory in scope of DEV_Orga
		@SuppressWarnings("unchecked")
		Collection<PayableObjectMoneyTransferFactoryJDO> fallbackResult = (Collection<PayableObjectMoneyTransferFactoryJDO>)
			query.execute(Organisation.DEV_ORGANISATION_ID, scope, objectClass.getName());
		
		if (fallbackResult == null || fallbackResult.isEmpty())
		{
			return null;
		}
		
		return fallbackResult.iterator().next();
	}

}
