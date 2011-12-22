package org.nightlabs.jfire.issue.dashboard;

import java.io.Serializable;

import org.nightlabs.jfire.query.store.id.QueryStoreID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class IssueDashboardGadgetConfig implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	private int amountOfIssues;
	
	private QueryStoreID issueQueryItemId;
	
	public IssueDashboardGadgetConfig() {
	}
	
	public IssueDashboardGadgetConfig(int amountOfIssues, QueryStoreID issueQueryItemId) {
		this.amountOfIssues = amountOfIssues;
		this.issueQueryItemId = issueQueryItemId;
	}

	/**
	 * @return the amountOfIssues
	 */
	public int getAmountOfIssues() {
		return amountOfIssues;
	}

	/**
	 * @param amountOfIssues the amountOfIssues to set
	 */
	public void setAmountOfIssues(int amountOfIssues) {
		this.amountOfIssues = amountOfIssues;
	}

	/**
	 * @return the issueQueryItemId
	 */
	public QueryStoreID getIssueQueryItemId() {
		return issueQueryItemId;
	}

	/**
	 * @param issueQueryItemId the issueQueryItemId to set
	 */
	public void setIssueQueryItemId(QueryStoreID issueQueryItemId) {
		this.issueQueryItemId = issueQueryItemId;
	}

}
