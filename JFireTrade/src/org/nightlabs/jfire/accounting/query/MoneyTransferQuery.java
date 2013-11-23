package org.nightlabs.jfire.accounting.query;

import javax.jdo.Query;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.id.AccountTypeID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.query.AbstractTransferQuery;

public class MoneyTransferQuery
extends AbstractTransferQuery
{
	private static final long serialVersionUID = 1L;
	
	private String fromAccountID = null;
	private String fromLegalEntityID = null;
	
	private String toAccountID = null;
	private String toLegalEntityID = null;
	
	private AccountTypeID fromAccountTypeID = null;
	private AccountTypeID toAccountTypeID = null;

	public static final class FieldName
	{
		public static final String fromAccountID = "fromAccountID";
		public static final String fromLegalEntityID = "fromLegalEntityID";
		public static final String toAccountID = "toAccountID";
		public static final String toLegalEntityID = "toLegalEntityID";
		public static final String fromAccountTypeID = "fromAccountTypeID";
		public static final String toAccountTypeID = "toAccountTypeID";
	}
	
	@Override
	protected Class<? extends MoneyTransfer> initCandidateClass()
	{
		return MoneyTransfer.class;
	}
	
	@Override
	protected void appendToFilter(Query q, StringBuffer filter)
	{
		if (isFieldEnabled(FieldName.fromAccountID) && fromAccountID != null) {
			filter.append("\n && this.from.organisationID == \"" + IDGenerator.getOrganisationID() + "\"");
			filter.append("\n && this.from.anchorTypeID == \"" + Account.ANCHOR_TYPE_ID_ACCOUNT + "\"");
			filter.append("\n && this.from.anchorID.matches(:fromAccountID)");
		}
		
		if (isFieldEnabled(FieldName.fromLegalEntityID) && fromLegalEntityID != null) {
			filter.append("\n && this.from.organisationID == \"" + IDGenerator.getOrganisationID() + "\"");
			filter.append("\n && this.from.anchorTypeID == \"" + LegalEntity.ANCHOR_TYPE_ID_LEGAL_ENTITY + "\"");
			filter.append("\n && this.from.anchorID.matches(:fromLegalEntityID)");
		}
		
		if (isFieldEnabled(FieldName.toAccountID) && toAccountID != null) {
			filter.append("\n && this.to.organisationID == \"" + IDGenerator.getOrganisationID() + "\"");
			filter.append("\n && this.to.anchorTypeID == \"" + Account.ANCHOR_TYPE_ID_ACCOUNT + "\"");
			filter.append("\n && this.to.anchorID.matches(:toAccountID)");
		}
		
		if (isFieldEnabled(FieldName.toLegalEntityID) && toLegalEntityID != null) {
			filter.append("\n && this.to.organisationID == \"" + IDGenerator.getOrganisationID() + "\"");
			filter.append("\n && this.to.anchorTypeID == \"" + LegalEntity.ANCHOR_TYPE_ID_LEGAL_ENTITY + "\"");
			filter.append("\n && this.to.anchorID.matches(:toLegalEntityID)");
		}
		
		if (isFieldEnabled(FieldName.fromAccountTypeID) && fromAccountTypeID != null) {
			filter.append("\n && this.from.organisationID == \"" + IDGenerator.getOrganisationID() + "\"");
			filter.append("\n && this.from.anchorTypeID == \"" + Account.ANCHOR_TYPE_ID_ACCOUNT + "\"");
			filter.append("\n && ((Account)this.from).accountType.accountTypeID == :accountTypeID.accountTypeID ");
		}
		
		if (isFieldEnabled(FieldName.toAccountTypeID) && toAccountTypeID != null) {
			filter.append("\n && this.to.organisationID == \"" + IDGenerator.getOrganisationID() + "\"");
			filter.append("\n && this.to.anchorTypeID == \"" + Account.ANCHOR_TYPE_ID_ACCOUNT + "\"");
			filter.append("\n && ((Account)this.to).accountType.accountTypeID == :accountTypeID.accountTypeID ");
		}
	}

	@Override
	protected void setQueryResult(Query q)
	{
	}
	
	public String getFromAccountID() {
		return fromAccountID;
	}
	
	public void setFromAccountID(String fromAccountID) {
		final String oldFromAccountID = removeRegexpSearch(this.fromAccountID);
		this.fromAccountID = fromAccountID;
		notifyListeners(FieldName.fromAccountID, oldFromAccountID, fromAccountID);
	}
	
	public String getFromLegalEntityID() {
		return fromLegalEntityID;
	}
	
	public void setFromLegalEntityID(String fromLegalEntityID) {
		final String oldFromLegalEntityID = removeRegexpSearch(this.fromLegalEntityID);
		this.fromLegalEntityID = fromLegalEntityID;
		notifyListeners(FieldName.fromLegalEntityID, oldFromLegalEntityID, fromLegalEntityID);
	}
	
	public String getToAccountID() {
		return toAccountID;
	}
	
	public void setToAccountID(String toAccountID) {
		final String oldToAccountID = removeRegexpSearch(this.toAccountID);
		this.toAccountID = toAccountID;
		notifyListeners(FieldName.toAccountID, oldToAccountID, toAccountID);
	}
	
	public String getToLegalEntityID() {
		return toLegalEntityID;
	}
	
	public void setToLegalEntityID(String toLegalEntityID) {
		final String oldToLegalEntityID = removeRegexpSearch(this.toLegalEntityID);
		this.toLegalEntityID = toLegalEntityID;
		notifyListeners(FieldName.toLegalEntityID, oldToLegalEntityID, toLegalEntityID);
	}
	
	public AccountTypeID getFromAccountTypeID() {
		return fromAccountTypeID;
	}
	
	public void setFromAccountTypeID(AccountTypeID fromAccountTypeID) {
		final AccountTypeID oldFromAccountTypeID = this.fromAccountTypeID;
		this.fromAccountTypeID = fromAccountTypeID;
		notifyListeners(FieldName.fromAccountTypeID, oldFromAccountTypeID, fromAccountTypeID);
	}
	
	public AccountTypeID getToAccountTypeID() {
		return toAccountTypeID;
	}
	
	public void setToAccountTypeID(AccountTypeID toAccountTypeID) {
		final AccountTypeID oldToAccountTypeID = this.toAccountTypeID;
		this.toAccountTypeID = toAccountTypeID;
		notifyListeners(FieldName.toAccountTypeID, oldToAccountTypeID, toAccountTypeID);
	}
	
	/**
	 * Helper that removes the '.*' from the beginning and end of the given string.
	 * @param pattern the regexp pattern that should be cleansed of the '.*'
	 * @return the pattern without '.*'
	 */
	private String removeRegexpSearch(String pattern)
	{
		if (pattern == null)
			return null;

		String result = pattern;
		if (pattern.startsWith(".*"))
		{
			result = result.substring(2);
		}
		if (pattern.endsWith(".*"))
		{
			result = result.substring(0, result.length()-2);
		}
		return pattern;
	}
}