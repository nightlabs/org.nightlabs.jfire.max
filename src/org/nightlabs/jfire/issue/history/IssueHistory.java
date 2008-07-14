package org.nightlabs.jfire.issue.history;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.issue.IssueLinkType;
import org.nightlabs.util.Util;

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
 * @jdo.query
 *		name="getIssueHistoriesByOrganisationIDAndIssueID"
 *		query="SELECT
 *			WHERE this.issueID == :issueID && this.organisationID == :organisationID"                    
 *
 * @jdo.fetch-group name="IssueHistory.issue" fields="issue"
 * 
 * @jdo.fetch-group name="IssueHistory.this" fetch-groups="default" fields="description"
 *
 **/
public class IssueHistory
implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Issue.class);

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS = "IssueHistory.this";
	public static final String FETCH_GROUP_ISSUE = "IssueHistory.issue";
	
	public static final String QUERY_ISSUE_HISTORIES_BY_ORGANISATION_ID_AND_ISSUE_ID = "getIssueHistoriesByOrganisationIDAndIssueID";
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueHistoryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	private String field;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	private String change;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createTimestamp;

	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	@Deprecated
	protected IssueHistory() { }

	public IssueHistory(Issue oldIssue, Issue newIssue, long issueHistoryID)
	{
		if (oldIssue == null)
			throw new NullPointerException("newIssue");

		this.organisationID = oldIssue.getOrganisationID();
		this.issueID = oldIssue.getIssueID();

		this.issue = oldIssue;
		this.issueHistoryID = issueHistoryID;

		this.createTimestamp = new Date();

		generateHistory(oldIssue, newIssue);
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

	public long getIssueHistoryID() {
		return issueHistoryID;
	}

	public Issue getIssue(){
		return issue;
	}

	/**
	 * @param pm The <code>PersistenceManager</code> that should be used to access the datastore.
	 * @param issue
	 * @return Returns instances of <code>IssueHistory</code>.
	 */
	@SuppressWarnings("unchecked")
	protected static Collection<IssueHistory> getIssueHistoryByIssue(PersistenceManager pm, Issue issue)
	{
		Query q = pm.newNamedQuery(IssueHistory.class, "getIssueHistoriesByOrganisationIDAndIssueID");
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("issueID", issue.getIssueID());
		params.put("organisationID", issue.getOrganisationID());
		return (Collection<IssueHistory>)q.executeWithMap(params);
	}
	
	private void generateHistory(Issue oldIssue, Issue newIssue)
	{
		if (!Util.equals(oldIssue.getComments(), newIssue.getComments())) 
		{
			
		}

		if (!Util.equals(oldIssue.getAssignee(), newIssue.getAssignee())) 
		{

		}

		if (!Util.equals(oldIssue.getReporter(), newIssue.getReporter())) 
		{

		}

		if (!Util.equals(oldIssue.getIssueFileAttachments(), newIssue.getIssueFileAttachments())) 
		{

		}

		if (!Util.equals(oldIssue.getIssueLinks(), newIssue.getIssueLinks())) 
		{

		}

		if (!Util.equals(oldIssue.getIssuePriority(), newIssue.getIssuePriority())) 
		{

		}
		
		if (!Util.equals(oldIssue.getIssueSeverityType(), newIssue.getIssueSeverityType())) 
		{

		}
		
		if (!Util.equals(oldIssue.getIssueResolution(), newIssue.getIssueResolution())) 
		{

		}
		
		if (!Util.equals(oldIssue.getIssueType(), newIssue.getIssueType())) 
		{

		}
		
		if (!Util.equals(oldIssue.getState(), newIssue.getState())) 
		{

		}
		
		if (!Util.equals(oldIssue.getIssueResolution(), newIssue.getIssueResolution())) 
		{

		}
	}
}