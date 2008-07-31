package org.nightlabs.jfire.issue.history;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.progress.NullProgressMonitor;
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
 *		name="getIssueHistoryIDsByOrganisationIDAndIssueID"
 *		query="SELECT
 *			WHERE this.issueID == :issueID && this.organisationID == :organisationID"                    
 *
 * @jdo.fetch-group name="IssueHistory.issue" fields="issue"
 * @jdo.fetch-group name="IssueHistory.user" fields="user"
 *
 **/
public class IssueHistory
implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(IssueHistory.class);

	public static final String FETCH_GROUP_ISSUE = "IssueHistory.issue";
	public static final String FETCH_GROUP_USER = "IssueHistory.user";
	
	public static final String QUERY_ISSUE_HISTORYIDS_BY_ORGANISATION_ID_AND_ISSUE_ID = "getIssueHistoryIDsByOrganisationIDAndIssueID";
	
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
	 * @jdo.column length="255"
	 */
	private String change;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User user;
	
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

	public IssueHistory(String organisationID, User user, Issue oldIssue, Issue newIssue, long issueHistoryID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		if (oldIssue == null)
			throw new NullPointerException("newIssue");

		this.organisationID = organisationID;
		this.issueID = oldIssue.getIssueID();

		this.issue = oldIssue;
		this.issueHistoryID = issueHistoryID;

		this.createTimestamp = new Date();
		this.user = user;
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

	public Date getCreateTimestamp() {
		return createTimestamp;
	}
	
	public String getChange() {
		return change;
	}
	
	public User getUser() {
		return user;
	}
	/**
	 * @param pm The <code>PersistenceManager</code> that should be used to access the datastore.
	 * @param issue
	 * @return Returns instances of <code>IssueHistory</code>.
	 */
	public static Collection<IssueHistory> getIssueHistoryByIssue(PersistenceManager pm, IssueID issueID)
	{
		Query q = pm.newNamedQuery(IssueHistory.class, IssueHistory.QUERY_ISSUE_HISTORYIDS_BY_ORGANISATION_ID_AND_ISSUE_ID);
		
		Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("issueID", issueID.issueID);
		params.put("organisationID", issueID.organisationID);
		
		return (Collection<IssueHistory>)q.executeWithMap(params);
	}
	
	private void generateHistory(Issue oldIssue, Issue newIssue)
	{
		StringBuffer changeText = new StringBuffer();
		
		if (!Util.equals(oldIssue.getDescription().getText(), newIssue.getDescription().getText())) 
		{
			changeText.append("Changed description");
			changeText.append(" from ");
			changeText.append(oldIssue.getDescription().getText());
			changeText.append(" ---> ");
			changeText.append(newIssue.getDescription().getText());
			changeText.append("\n");
			
		}
		
		if (!Util.equals(oldIssue.getSubject().getText(), newIssue.getSubject().getText())) 
		{
			changeText.append("Changed subject");
			changeText.append(" from ");
			changeText.append(oldIssue.getSubject().getText());
			changeText.append(" ---> ");
			changeText.append(newIssue.getSubject().getText());
			changeText.append("\n");
		}
		
		if (!Util.equals(oldIssue.getComments(), newIssue.getComments())) 
		{
			changeText.append("Add Comment(s)");
//			changeText.append(" from ");
//			changeText.append();
//			changeText.append(" ---> ");
//			changeText.append();
			changeText.append("\n");
		}

		if (!Util.equals(oldIssue.getAssignee(), newIssue.getAssignee())) 
		{
			changeText.append("Changed assignee");
			changeText.append(" from ");
			changeText.append(oldIssue.getAssignee() == null?" - ": oldIssue.getAssignee().getName());
			changeText.append(" ---> ");
			changeText.append(newIssue.getAssignee().getName());
			changeText.append("\n");
		}

		if (!Util.equals(oldIssue.getReporter(), newIssue.getReporter())) 
		{
			changeText.append("Changed reporter");
			changeText.append(" from ");
			changeText.append(oldIssue.getReporter().getName());
			changeText.append(" ---> ");
			changeText.append(newIssue.getReporter().getName());
			changeText.append("\n");
		}

		if (!Util.equals(oldIssue.getIssueFileAttachments().size(), newIssue.getIssueFileAttachments().size())) 
		{
			changeText.append("Changed file attachments");
//			changeText.append(" from ");
//			changeText.append();
//			changeText.append(" ---> ");
//			changeText.append();
			changeText.append("\n");
		}

//		if (!Util.equals(oldIssue.getIssueLinks(), newIssue.getIssueLinks())) 
//		{
//			changeText.append("Changed issue links");
////			changeText.append(" from ");
////			changeText.append();
////			changeText.append(" ---> ");
////			changeText.append();
//			changeText.append("\n");
//		}

		if (!Util.equals(oldIssue.getIssuePriority(), newIssue.getIssuePriority())) 
		{
			changeText.append("Changed priority");
			changeText.append(" from ");
			changeText.append(oldIssue.getIssuePriority().getIssuePriorityText().getText());
			changeText.append(" ---> ");
			changeText.append(newIssue.getIssuePriority().getIssuePriorityText().getText());
			changeText.append("\n");
		}
		
		if (!Util.equals(oldIssue.getIssueSeverityType(), newIssue.getIssueSeverityType())) 
		{
			changeText.append("Changed severity type");
			changeText.append(" from ");
			changeText.append(oldIssue.getIssueSeverityType().getIssueSeverityTypeText().getText());
			changeText.append(" ---> ");
			changeText.append(newIssue.getIssueSeverityType().getIssueSeverityTypeText().getText());
			changeText.append("\n");
		}
		
		if (!Util.equals(oldIssue.getIssueResolution(), newIssue.getIssueResolution())) 
		{
			changeText.append("Changed resolution");
			changeText.append(" from ");
			changeText.append(oldIssue.getIssueResolution() == null?"None":oldIssue.getIssueResolution().getName().getText());
			changeText.append(" ---> ");
			changeText.append(newIssue.getIssueResolution().getName().getText());
			changeText.append("\n");
		}
		
		if (!Util.equals(oldIssue.getIssueType(), newIssue.getIssueType())) 
		{
			changeText.append("Changed issue type");
			changeText.append(" from ");
			changeText.append(oldIssue.getIssueType().getName().getText());
			changeText.append(" ---> ");
			changeText.append(newIssue.getIssueType().getName().getText());
			changeText.append("\n");
		}
		
		if (!Util.equals(oldIssue.getState().getStateDefinition(), newIssue.getState().getStateDefinition())) 
		{
			changeText.append("Changed state");
			changeText.append(" from ");
			changeText.append(oldIssue.getState().getStateDefinition().getName().getText());
			changeText.append(" ---> ");
			changeText.append(newIssue.getState().getStateDefinition().getName().getText());
			changeText.append("\n");
		}
		
		if (!Util.equals(oldIssue.isStarted(), newIssue.isStarted())) 
		{
			changeText.append("Changed status");
			changeText.append(" from ");
			changeText.append(oldIssue.isStarted() ? "Working" : "Stopped");
			changeText.append(" ---> ");
			changeText.append(newIssue.isStarted() ? "Working" : "Stopped");
		}
		
		this.change = changeText.toString();
	}
	
	/**
	 * Internal method.
	 * @return The PersistenceManager associated with this object. 
	 */
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager issueHistoryPM = JDOHelper.getPersistenceManager(this);
		if (issueHistoryPM == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");

		return issueHistoryPM;
	}	
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueHistory)) return false;
		IssueHistory o = (IssueHistory) obj;
		return
			Util.equals(this.organisationID, o.organisationID) && 
			Util.equals(this.issueID, o.issueID) &&
			Util.equals(this.issueHistoryID, o.issueHistoryID);
	}

	@Override
	public int hashCode()
	{
		return 
			Util.hashCode(organisationID) ^
			Util.hashCode(issueID) ^
			Util.hashCode(issueHistoryID);
	}
}