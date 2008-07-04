package org.nightlabs.jfire.accounting.query;

import java.util.List;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.id.AccountTypeID;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class AccountQuery
	extends AbstractJDOQuery
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(AccountQuery.class);

	/**
	 * the minimum balance of the account to search for
	 */
	private Long minBalance = null;
	/**
	 * the maximum balance of the account to search for
	 */
	private Long maxBalance = null;
	/**
	 * the {@link CurrencyID} of the currency to search for
	 */
	private CurrencyID currencyID = null;
	
	//used as parameter in the JDOQL, see reflective collecting of params in AbstractJDOQuery.
	@SuppressWarnings("unused")
	private transient Currency currency = null;
	/**
	 * the accountName of the account to search for
	 */
	private String accountName = null;
	private String accountNameLanguageID = null;
	/**
	 * the anchorID to search for
	 */
	private String anchorID = null;
	/**
	 * the anchorTypeID to search for
	 */
	private String anchorTypeID = null;

	private AccountTypeID accountTypeID = null;

	@SuppressWarnings("unused") // used as parameter in the JDOQL
	private transient AccountType accountType = null;

	/**
	 * the AnchorID of the owner
	 */
	private AnchorID ownerID = null;

//	@SuppressWarnings("unused") // used as parameter in the JDOQL
//	private transient Anchor owner;

	/**
	 * the accountName (or part of the accountName) of the owner
	 */
	private String ownerName = null;
	
	// Property IDs used for the PropertyChangeListeners
	private static final String PROPERTY_PREFIX = "AccountQuery.";
	public static final String PROPERTY_ACCOUNT_TYPE_ID = PROPERTY_PREFIX + "accountTypeID";
	public static final String PROPERTY_ANCHOR_ID = PROPERTY_PREFIX + "anchorID";
	public static final String PROPERTY_ANCHOR_TYPE_ID = PROPERTY_PREFIX + "anchorTypeID";
	public static final String PROPERTY_CURRENCY_ID = PROPERTY_PREFIX + "currencyID";	
	public static final String PROPERTY_MAX_BALANCE = PROPERTY_PREFIX + "maxBalance";	
	public static final String PROPERTY_MIN_BALANCE = PROPERTY_PREFIX + "minBalance";	
	public static final String PROPERTY_ACCOUNT_NAME = PROPERTY_PREFIX + "accountName";	
	public static final String PROPERTY_ACCOUNT_NAME_LANGUAGE_ID = PROPERTY_PREFIX + "accountNameLanguageID";	
	public static final String PROPERTY_OWNER_ID = PROPERTY_PREFIX + "ownerID";	
	public static final String PROPERTY_OWNER_NAME = PROPERTY_PREFIX + "ownerName";	
	
	@Override
	public List<FieldChangeCarrier> getChangedFields(String propertyName)
	{
		final List<FieldChangeCarrier> changedFields = super.getChangedFields(propertyName);
		final boolean allFields = AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(propertyName);
		
		if (allFields || PROPERTY_ACCOUNT_NAME.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ACCOUNT_NAME, accountName) );
		}
		if (allFields || PROPERTY_ACCOUNT_NAME_LANGUAGE_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ACCOUNT_NAME_LANGUAGE_ID, accountNameLanguageID) );
		}
		if (allFields || PROPERTY_ACCOUNT_TYPE_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ACCOUNT_TYPE_ID, accountTypeID) );
		}
		if (allFields || PROPERTY_ANCHOR_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ANCHOR_ID, anchorID) );
		}
		if (allFields || PROPERTY_ANCHOR_TYPE_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ANCHOR_TYPE_ID, anchorTypeID) );
		}
		if (allFields || PROPERTY_CURRENCY_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_CURRENCY_ID, currencyID) );
		}
		if (allFields || PROPERTY_MAX_BALANCE.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_MAX_BALANCE, maxBalance) );
		}
		if (allFields || PROPERTY_MIN_BALANCE.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_MIN_BALANCE, minBalance) );
		}
		if (allFields || PROPERTY_OWNER_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_OWNER_ID, ownerID) );
		}
		if (allFields || PROPERTY_OWNER_NAME.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_OWNER_NAME, ownerName) );
		}
		
		return changedFields;
	}
	@Override
	protected void prepareQuery(Query q)
	{
		StringBuffer filter = new StringBuffer();
		StringBuffer vars = new StringBuffer();
//		StringBuffer imports = new StringBuffer();
		
		filter.append(" true");

		if (currencyID != null) {
			currency = (Currency) getPersistenceManager().getObjectById(currencyID);
			filter.append("\n && this.currency == :currency");
		}

		if (accountTypeID != null) {
			accountType = (AccountType) getPersistenceManager().getObjectById(accountTypeID);
			filter.append("\n && this.accountType == :accountType");
		}

		if (minBalance != null)
			filter.append("\n && this.balance >= :minBalance");
			
		if (maxBalance != null)
			filter.append("\n && this.balance <= :maxBalance");
		
		if (anchorTypeID != null)
			filter.append("\n && this.anchorTypeID == :anchorTypeID");
			
		if (anchorID != null)
			filter.append("\n && this.anchorID == :anchorID");
			
		if (accountName != null && !"".equals(accountName)) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, "name");
			filter.append(")");
		}
		
		if (ownerID != null) {
				// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("\n && JDOHelper.getObjectId(this.owner) == :ownerID");
			// WORKAROUND:
			filter.append("\n && (" +
					"this.owner.organisationID == \""+ownerID.organisationID+"\" && " +
					"this.owner.anchorTypeID == \""+ownerID.anchorTypeID+"\" && " +
					"this.owner.anchorID == \""+ownerID.anchorID+"\"" +
							")");
//			owner = (Anchor) getPersistenceManager().getObjectById(ownerID);
//			filter.append("\n && this.owner == :owner");
		}

		if (ownerName != null && !"".equals(ownerName)) {
			filter.append("\n && this.owner.person.displayName.toLowerCase().indexOf(\""+ownerName.toLowerCase()+"\") >= 0");
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Vars:");
			logger.debug(vars.toString());
			logger.debug("Filter:");
			logger.debug(filter.toString());
		}
		
		q.setFilter(filter.toString());
		q.declareVariables(vars.toString());
	}

	private void addFullTextSearch(StringBuffer filter, StringBuffer vars, String member)
	{
		if (vars.length() > 0)
			vars.append("; ");

		String varName = member+"Var";
		vars.append(String.class.getName()+" "+varName);
		String containsStr = "containsValue("+varName+")";
		if (accountNameLanguageID != null)
			containsStr = "containsEntry(:accountNameLanguageID, "+varName+")";
		filter.append("\n (\n" +
				"  this."+member+".names."+containsStr+"\n" +
				"  && "+varName+".toLowerCase().matches(:accountName.toLowerCase())" +
				" )");
	}

	/**
	 * returns the minBalance.
	 * @return the minBalance
	 */
	public Long getMinBalance() {
		return minBalance;
	}

	/**
	 * set the minBalance
	 * @param minBalance the minBalance to set
	 */
	public void setMinBalance(Long minBalance)
	{
		final Long oldMinBalance = this.minBalance;
		this.minBalance = minBalance;
		notifyListeners(PROPERTY_MIN_BALANCE, oldMinBalance, minBalance);
	}

	/**
	 * returns the maxBalance.
	 * @return the maxBalance
	 */
	public Long getMaxBalance() {
		return maxBalance;
	}

	/**
	 * set the maxBalance
	 * @param maxBalance the maxBalance to set
	 */
	public void setMaxBalance(Long maxBalance)
	{
		final Long oldMaxBalance = this.maxBalance;
		this.maxBalance = maxBalance;
		notifyListeners(PROPERTY_MAX_BALANCE, oldMaxBalance, maxBalance);
	}

	/**
	 * returns the currencyID.
	 * @return the currencyID
	 */
	public CurrencyID getCurrencyID() {
		return currencyID;
	}

	/**
	 * set the currencyID
	 * @param currencyID the currencyID to set
	 */
	public void setCurrencyID(CurrencyID currencyID)
	{
		final CurrencyID oldCurrencyID = this.currencyID;
		this.currencyID = currencyID;
		notifyListeners(PROPERTY_CURRENCY_ID, oldCurrencyID, currencyID);
	}

	public void setAccountTypeID(AccountTypeID accountTypeID)
	{
		final AccountTypeID oldAccountTypeID = this.accountTypeID;
		this.accountTypeID = accountTypeID;
		notifyListeners(PROPERTY_ACCOUNT_TYPE_ID, oldAccountTypeID, accountTypeID);
	}
	public AccountTypeID getAccountTypeID()
	{
		return accountTypeID;
	}

	/**
	 * returns the accountName.
	 * @return the accountName
	 */
	public String getName() {
		return accountName;
	}

	/**
	 * set the accountName
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName)
	{
		final String oldAccountName = this.accountName;
		this.accountName = accountName;
		notifyListeners(PROPERTY_ACCOUNT_NAME, oldAccountName, accountName);
	}

	/**
	 * returns the accountNameLanguageID.
	 * @return the accountNameLanguageID
	 */
	public String getNameLanguageID() {
		return accountNameLanguageID;
	}

	/**
	 * set the accountNameLanguageID
	 * @param accountNameLanguageID the accountNameLanguageID to set
	 */
	public void setNameLanguageID(String nameLanguageID)
	{
		final String oldNameLanguageID = this.accountNameLanguageID;
		this.accountNameLanguageID = nameLanguageID;
		notifyListeners(PROPERTY_ACCOUNT_NAME_LANGUAGE_ID, oldNameLanguageID, nameLanguageID);
	}

	/**
	 * returns the anchorID.
	 * @return the anchorID
	 */
	public String getAnchorID() {
		return anchorID;
	}

	/**
	 * set the anchorID
	 * @param anchorID the anchorID to set
	 */
	public void setAnchorID(String anchorID)
	{
		final String oldAnchorID = this.anchorID;
		this.anchorID = anchorID;
		notifyListeners(PROPERTY_ANCHOR_ID, oldAnchorID, anchorID);
	}

	/**
	 * returns the anchorTypeID.
	 * @return the anchorTypeID
	 */
	public String getAnchorTypeID() {
		return anchorTypeID;
	}

	/**
	 * set the anchorTypeID
	 * @param anchorTypeID the anchorTypeID to set
	 */
	public void setAnchorTypeID(String anchorTypeID)
	{
		final String oldAnchorTypeID = this.anchorTypeID;
		this.anchorTypeID = anchorTypeID;
		notifyListeners(PROPERTY_ANCHOR_TYPE_ID, oldAnchorTypeID, anchorTypeID);
	}

	/**
	 * returns the ownerID
	 * @return the ownerID
	 */
	public AnchorID getOwnerID() {
		return ownerID;
	}

	/**
	 * sets the ownerID
	 * @param ownerID the ownerID to set
	 */
	public void setOwnerID(AnchorID ownerID)
	{
		final AnchorID oldOwnerID = this.ownerID;
		this.ownerID = ownerID;
		notifyListeners(PROPERTY_OWNER_ID, oldOwnerID, ownerID);
	}

	/**
	 * returns the ownerName
	 * @return the ownerName
	 */
	public String getOwnerName() {
		return ownerName;
	}

	/**
	 * sets the ownerName
	 * @param ownerName the ownerName to set
	 */
	public void setOwnerName(String ownerName)
	{
		final String oldOwnerName = this.ownerName;
		this.ownerName = ownerName;
		notifyListeners(PROPERTY_OWNER_NAME, oldOwnerName, ownerName);
	}

	@Override
	protected Class<Account> initCandidateClass()
	{
		return Account.class;
	}
	
}
