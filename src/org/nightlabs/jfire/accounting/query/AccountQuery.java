package org.nightlabs.jfire.accounting.query;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
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

	/**
	 * the accountName (or part of the accountName) of the owner
	 */
	private String ownerName = null;

	/**
	 * Static final class containing all field names as static members.
	 */
	public static final class FieldName
	{
		public static final String accountTypeID = "accountTypeID";
		public static final String anchorID = "anchorID";
		public static final String anchorTypeID = "anchorTypeID";
		public static final String currencyID = "currencyID";
		public static final String maxBalance = "maxBalance";
		public static final String minBalance = "minBalance";
		public static final String accountName = "accountName";
		public static final String accountNameLanguageID = "accountNameLanguageID";
		public static final String ownerID = "ownerID";
		public static final String ownerName = "ownerName";
	}

	@Override
	protected void prepareQuery(Query q)
	{
		StringBuffer filter = new StringBuffer();
		StringBuffer vars = new StringBuffer();
//		StringBuffer imports = new StringBuffer();

		filter.append(" true");

		if (isFieldEnabled(FieldName.currencyID) && currencyID != null) {
			currency = (Currency) getPersistenceManager().getObjectById(currencyID);
			filter.append("\n && this.currency == :currency");
		}

		if (isFieldEnabled(FieldName.accountTypeID) && accountTypeID != null) {
			accountType = (AccountType) getPersistenceManager().getObjectById(accountTypeID);
			filter.append("\n && this.accountType == :accountType");
		}

		if (isFieldEnabled(FieldName.minBalance) && minBalance != null)
			filter.append("\n && this.balance >= :minBalance");

		if (isFieldEnabled(FieldName.maxBalance) && maxBalance != null)
			filter.append("\n && this.balance <= :maxBalance");

		if (isFieldEnabled(FieldName.anchorTypeID) && anchorTypeID != null)
			filter.append("\n && this.anchorTypeID == :anchorTypeID");

		if (isFieldEnabled(FieldName.anchorID) && anchorID != null)
			filter.append("\n && this.anchorID == :anchorID");

		if (isFieldEnabled(FieldName.accountName) && accountName != null && !"".equals(accountName)) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, "name");
			filter.append(")");
		}

		if (isFieldEnabled(FieldName.ownerID) && ownerID != null) {
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

		if (isFieldEnabled(FieldName.ownerName) && ownerName != null && !"".equals(ownerName)) {
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
		if (isFieldEnabled(FieldName.accountNameLanguageID) && accountNameLanguageID != null)
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
		notifyListeners(FieldName.minBalance, oldMinBalance, minBalance);
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
		notifyListeners(FieldName.maxBalance, oldMaxBalance, maxBalance);
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
		notifyListeners(FieldName.currencyID, oldCurrencyID, currencyID);
	}

	public void setAccountTypeID(AccountTypeID accountTypeID)
	{
		final AccountTypeID oldAccountTypeID = this.accountTypeID;
		this.accountTypeID = accountTypeID;
		notifyListeners(FieldName.accountTypeID, oldAccountTypeID, accountTypeID);
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
	 * Set the regular expression to match the account name against.
	 * 
	 * @param accountName the accountName to set.
	 */
	public void setAccountName(String accountName)
	{
		final String oldAccountName = this.accountName;
		this.accountName = accountName;
		notifyListeners(FieldName.accountName, oldAccountName, accountName);
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
		notifyListeners(FieldName.accountNameLanguageID, oldNameLanguageID, nameLanguageID);
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
		notifyListeners(FieldName.anchorID, oldAnchorID, anchorID);
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
		notifyListeners(FieldName.anchorTypeID, oldAnchorTypeID, anchorTypeID);
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
		notifyListeners(FieldName.ownerID, oldOwnerID, ownerID);
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
		notifyListeners(FieldName.ownerName, oldOwnerName, ownerName);
	}

	@Override
	protected Class<Account> initCandidateClass()
	{
		return Account.class;
	}

}
