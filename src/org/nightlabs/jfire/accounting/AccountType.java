package org.nightlabs.jfire.accounting;

import java.io.Serializable;

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
public class AccountType
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "AccountType.name";

	/**
	 * anchorTypeID for revenue accounts of the local organisation.
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_LOCAL_REVENUE = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Local.Revenue");

	/**
	 * anchorTypeID for expense accounts of the Local organisation
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_LOCAL_EXPENSE = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Local.Expense");

	/**
	 * anchorTypeID for accounts of trading partners when they acting as vendor
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_PARTNER_VENDOR = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Partner.Vendor");
	
	/**
	 * anchorTypeID for accounts of trading partners when they acting as customer
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_PARTNER_CUSTOMER = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Partner.Customer");

	/**
	 * anchorTypeID for accounts of trading partners when they overpay multiple invoices and
	 * it cannot be determined whether the partner is a customer or a vendor.
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_PARTNER_NEUTRAL = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Partner.Neutral");

	/**
	 * anchorTypeID for accounts that are used during payment. They represent money that's outside
	 * the organisation (means paid to a partner), hence their {@link #isOutside()} property is <code>true</code>.
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_OUTSIDE = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Outside");

	public static final AccountTypeID ACCOUNT_TYPE_ID_SUMMARY = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Summary");

	public static final String ANCHOR_TYPE_ID_PREFIX_OUTSIDE = "outside#";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String accountTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="accountType"
	 */
	private AccountTypeName name;

	/**
	 * Whether or not this Repository represents sth. outside.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean outside;

	/**
	 * @deprecated Only for JDO!
	 */
	protected AccountType() { }

	public AccountType(AccountTypeID accountTypeID, boolean outside)
	{
		this(accountTypeID.organisationID, accountTypeID.accountTypeID, outside);
	}

	public AccountType(String organisationID, String accountTypeID, boolean outside)
	{
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(accountTypeID, "accountTypeID");

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
