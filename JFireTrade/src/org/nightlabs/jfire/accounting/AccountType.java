package org.nightlabs.jfire.accounting;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.id.AccountTypeID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.AccountTypeID"
 *		detachable="true"
 *		table="JFireTrade_AccountType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, accountTypeID"
 *
 * @jdo.fetch-group name="AccountType.name" fields="name"
 */
@PersistenceCapable(
	objectIdClass=AccountTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_AccountType")
@FetchGroups(
	@FetchGroup(
		name=AccountType.FETCH_GROUP_NAME,
		members=@Persistent(name="name"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class AccountType
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "AccountType.name"; //$NON-NLS-1$

	/**
	 * anchorTypeID for uncollectable invoices.
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_UNCOLLECTABLE = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Uncollectable"); //$NON-NLS-1$

	/**
	 * anchorTypeID for revenue accounts of the local organisation.
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_LOCAL_REVENUE = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Local.Revenue"); //$NON-NLS-1$

	/**
	 * anchorTypeID for expense accounts of the Local organisation
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_LOCAL_EXPENSE = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Local.Expense"); //$NON-NLS-1$

	/**
	 * anchorTypeID for accounts of trading partners when they acting as vendor
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_PARTNER_VENDOR = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Partner.Vendor"); //$NON-NLS-1$

	/**
	 * anchorTypeID for accounts of trading partners when they acting as customer
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_PARTNER_CUSTOMER = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Partner.Customer"); //$NON-NLS-1$

	/**
	 * anchorTypeID for accounts of trading partners when they overpay multiple invoices and
	 * it cannot be determined whether the partner is a customer or a vendor.
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_PARTNER_NEUTRAL = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Partner.Neutral"); //$NON-NLS-1$

	/**
	 * anchorTypeID for accounts that are used during payment. They represent money that's outside
	 * the organisation (means paid to a partner), hence their {@link #isOutside()} property is <code>true</code>.
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_OUTSIDE = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Outside"); //$NON-NLS-1$

	public static final AccountTypeID ACCOUNT_TYPE_ID_SUMMARY = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Summary"); //$NON-NLS-1$

	public static final String ANCHOR_TYPE_ID_PREFIX_OUTSIDE = "outside#"; //$NON-NLS-1$

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String accountTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="accountType"
	 */
	@Persistent(
		mappedBy="accountType",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private AccountTypeName name;

	/**
	 * Whether or not this Repository represents sth. outside.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean outside;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected AccountType() { }

	public AccountType(AccountTypeID accountTypeID, boolean outside)
	{
		this(accountTypeID.organisationID, accountTypeID.accountTypeID, outside);
	}

	public AccountType(String organisationID, String accountTypeID, boolean outside)
	{
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(accountTypeID, "accountTypeID"); //$NON-NLS-1$

		this.organisationID = organisationID;
		this.accountTypeID = accountTypeID;
		this.outside = outside;
		this.name = new AccountTypeName(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getAccountTypeID()
	{
		return accountTypeID;
	}

	public AccountTypeName getName()
	{
		return name;
	}

	public boolean isOutside()
	{
		return outside;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result
				+ ((accountTypeID == null) ? 0 : accountTypeID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof AccountType))
			return false;
		final AccountType other = (AccountType) obj;
		return Util.equals(this.organisationID, other.organisationID) && Util.equals(this.accountTypeID, other.accountTypeID);
	}

}
