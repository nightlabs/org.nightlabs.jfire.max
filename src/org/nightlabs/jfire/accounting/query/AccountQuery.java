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
	extends AbstractJDOQuery<Account>
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(AccountQuery.class);

	/**
	 * the minium balance of the account to search for
	 */
	private long minBalance = -1;
	/**
	 * the maximum balance of the account to search for
	 */
	private long maxBalance = -1;
	/**
	 * the {@link CurrencyID} of the currency to search for
	 */
	private CurrencyID currencyID = null;
	
	@SuppressWarnings("unused") // used as parameter in the JDOQL
	private transient Currency currency = null;
	/**
	 * the name of the account to search for
	 */
	private String name = null;
	private String nameLanguageID = null;
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
	 * the name (or part of the name) of the owner
	 */
	private String ownerName = null;
	
	@Override
	protected Query prepareQuery()
	{
		Query q = getPersistenceManager().newQuery(Account.class);
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

		if (minBalance >= 0)
			filter.append("\n && this.balance >= :minBalance");
			
		if (maxBalance >= 0)
			filter.append("\n && this.balance <= :maxBalance");
		
		if (anchorTypeID != null)
			filter.append("\n && this.anchorTypeID == :anchorTypeID");
			
		if (anchorID != null)
			filter.append("\n && this.anchorID == :anchorID");
			
		if (name != null && !"".equals(name)) {
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
		
		return q;
	}

	private void addFullTextSearch(StringBuffer filter, StringBuffer vars, String member)
	{
		if (vars.length() > 0)
			vars.append("; ");

		String varName = member+"Var";
		vars.append(String.class.getName()+" "+varName);
		String containsStr = "containsValue("+varName+")";
		if (nameLanguageID != null)
			containsStr = "containsEntry(:nameLanguageID, "+varName+")";
		filter.append("\n (\n" +
				"  this."+member+".names."+containsStr+"\n" +
				"  && "+varName+".toLowerCase().matches(:name.toLowerCase())" +
				" )");
	}

	/**
	 * returns the minBalance.
	 * @return the minBalance
	 */
	public long getMinBalance() {
		return minBalance;
	}

	/**
	 * set the minBalance
	 * @param minBalance the minBalance to set
	 */
	public void setMinBalance(long minBalance) {
		this.minBalance = minBalance;
	}

	/**
	 * returns the maxBalance.
	 * @return the maxBalance
	 */
	public long getMaxBalance() {
		return maxBalance;
	}

	/**
	 * set the maxBalance
	 * @param maxBalance the maxBalance to set
	 */
	public void setMaxBalance(long maxBalance) {
		this.maxBalance = maxBalance;
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
	public void setCurrencyID(CurrencyID currencyID) {
		this.currencyID = currencyID;
	}

	public void setAccountTypeID(AccountTypeID accountTypeID)
	{
		this.accountTypeID = accountTypeID;
	}
	public AccountTypeID getAccountTypeID()
	{
		return accountTypeID;
	}

	/**
	 * returns the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * set the name
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * returns the nameLanguageID.
	 * @return the nameLanguageID
	 */
	public String getNameLanguageID() {
		return nameLanguageID;
	}

	/**
	 * set the nameLanguageID
	 * @param nameLanguageID the nameLanguageID to set
	 */
	public void setNameLanguageID(String nameLanguageID) {
		this.nameLanguageID = nameLanguageID;
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
	public void setAnchorID(String anchorID) {
		this.anchorID = anchorID;
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
	public void setAnchorTypeID(String anchorTypeID) {
		this.anchorTypeID = anchorTypeID;
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
	public void setOwnerID(AnchorID ownerID) {
		this.ownerID = ownerID;
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
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	@Override
	protected Class<Account> init()
	{
		return Account.class;
	}
	
}
