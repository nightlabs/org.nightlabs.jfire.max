package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.history.id.IssueTimeRangeID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueTimeRange"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, issueID, issueTimeRangeID"
 *
 * @jdo.fetch-group name="IssueTimeRange.issue" fields="issue"
 * @jdo.fetch-group name="IssueTimeRange.this" fetch-groups="default" fields="createTimestamp"
 *
 **/
public class IssueWorkTimeRange
implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(IssueWorkTimeRange.class);

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS = "IssueTimeRange.this";
	public static final String FETCH_GROUP_ISSUE = "IssueTimeRange.issue";
	public static final String FETCH_GROUP_USER = "IssueTimeRange.user";
	
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
	private long issueTimeRangeID;

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
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createTimestamp;

	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	@Deprecated
	protected IssueWorkTimeRange() { }

	public IssueWorkTimeRange(User user, Issue oldIssue, Issue newIssue, long issueHistoryID)
	{
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

	public long getIssueTimeRangeID() {
		return issueTimeRangeID;
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
	
	/**
	 * Internal method.
	 * @return The PersistenceManager associated with this object. 
	 */
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager issueTimeRangePM = JDOHelper.getPersistenceManager(this);
		if (issueTimeRangePM == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");

		return issueTimeRangePM;
	}	
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueWorkTimeRange)) return false;
		IssueWorkTimeRange o = (IssueWorkTimeRange) obj;
		return
			Util.equals(this.organisationID, o.organisationID) && 
			Util.equals(this.issueID, o.issueID) &&
			Util.equals(this.issueTimeRangeID, o.issueTimeRangeID);
	}

	@Override
	public int hashCode()
	{
		return 
			Util.hashCode(organisationID) ^
			Util.hashCode(issueID) ^
			Util.hashCode(issueTimeRangeID);
	}
}