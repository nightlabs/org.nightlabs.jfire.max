package org.nightlabs.jfire.issue.history;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.issue.Issue;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.history.id.IssueHistoryID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueHistory"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, issueID, issueHistoryID"
 *
 * jdo.query
 *		name="getIssueHistoriesByIssueID"
 *		query="SELECT
 *			WHERE this.issueID == paramIssueID                    
 *			PARAMETERS String paramIssueID
 *			import java.lang.String"
 *
 * @jdo.fetch-group name="IssueHistory.this" fetch-groups="default" fields="description"
 *
 **/
public class IssueHistory
implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Issue.class);
	
	public static final String FETCH_GROUP_THIS = "IssueHistory.this";
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private long issueID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private long issueHistoryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createTimestamp;
	
	protected IssueHistory() { }

	public IssueHistory(Issue issue)
	{
		if (issue == null)
			throw new NullPointerException("issue");
		
		this.issue = issue;
		
		this.organisationID = issue.getOrganisationID();
		this.issueID = issue.getIssueID();
	}
	
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return Returns the issueID.
	 */
	public long getIssueID() {
		return issueID;
	}

	/**
	 * @param issueID The issueID to set.
	 */
	public void setIssueID(long issueID) {
		this.issueID = issueID;
	}
	
	public long getIssueHistoryID() {
		return issueHistoryID;
	}
	
	public void setIssueHistoryID(long issueHistoryID) {
		this.issueHistoryID = issueHistoryID;
	}
	
	public Issue getIssue(){
		return issue;
	}
}
