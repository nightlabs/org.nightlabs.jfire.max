package org.nightlabs.jfire.accounting;

import java.io.Serializable;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.id.AccountTypeID;
import org.nightlabs.jfire.organisation.Organisation;

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
	public static final AccountTypeID ACCOUNT_TYPE_ID_LOCAL_REVENUE = AccountTypeID.create(Organisation.DEVIL_ORGANISATION_ID, "Account.Local.Revenue");

	/**
	 * anchorTypeID for expense accounts of the Local organisation
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_LOCAL_EXPENSE = AccountTypeID.create(Organisation.DEVIL_ORGANISATION_ID, "Account.Local.Expense");

	/**
	 * anchorTypeID for accounts of trading partners when they acting as vendor
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_PARTNER_VENDOR = AccountTypeID.create(Organisation.DEVIL_ORGANISATION_ID, "Account.Partner.Vendor");
	
	/**
	 * anchorTypeID for accounts of trading partners when they acting as customer
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_PARTNER_CUSTOMER = AccountTypeID.create(Organisation.DEVIL_ORGANISATION_ID, "Account.Partner.Customer");

	/**
	 * anchorTypeID for accounts of trading partners when they overpay multiple invoices and
	 * it cannot be determined whether the partner is a customer or a vendor.
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_PARTNER_NEUTRAL = AccountTypeID.create(Organisation.DEVIL_ORGANISATION_ID, "Account.Partner.Neutral");

	/**
	 * anchorTypeID for accounts that are used during payment. They represent money that's outside
	 * the organisation (means paid to a partner), hence their {@link #isOutside()} property is <code>true</code>.
	 */
	public static final AccountTypeID ACCOUNT_TYPE_ID_OUTSIDE = AccountTypeID.create(Organisation.DEVIL_ORGANISATION_ID, "Account.Outside");

	public static final AccountTypeID ACCOUNT_TYPE_ID_SUMMARY = AccountTypeID.create(Organisation.DEVIL_ORGANISATION_ID, "Account.Summary");

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
}
