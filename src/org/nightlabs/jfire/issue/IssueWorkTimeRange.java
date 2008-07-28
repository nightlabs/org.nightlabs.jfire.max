package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueWorkTimeRangeID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueWorkTimeRange"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, issueID, issueWorkTimeRangeID"
 *
 * @jdo.fetch-group name="IssueWorkTimeRange.issue" fields="issue"
 * @jdo.fetch-group name="IssueWorkTimeRange.user" fields="user"
 * @jdo.fetch-group name="IssueWorkTimeRange.from" fields="from"
 * @jdo.fetch-group name="IssueWorkTimeRange.to" fields="to"
 *
 **/
public class IssueWorkTimeRange
implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(IssueWorkTimeRange.class);

	public static final String FETCH_GROUP_ISSUE = "IssueWorkTimeRange.issue";
	public static final String FETCH_GROUP_USER = "IssueWorkTimeRange.user";
	public static final String FETCH_GROUP_FROM = "IssueWorkTimeRange.from";
	public static final String FETCH_GROUP_TO = "IssueWorkTimeRange.to";
	
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
	private long issueWorkTimeRangeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date from;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date to;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Double durationHours;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User user;

	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	@Deprecated
	protected IssueWorkTimeRange() { }

	public IssueWorkTimeRange(String organisationID, User user, Issue issue)
	{
		Organisation.assertValidOrganisationID(organisationID);
		
		if (issue == null)
			throw new IllegalArgumentException("issue must not be null!");
		
		if (user == null)
			throw new IllegalArgumentException("user must not be null!");
		
		this.organisationID = organisationID;
		this.issue = issue;
		this.user = user;
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

	public Issue getIssue(){
		return issue;
	}

	public Date getFrom() {
		return from;
	}
	
	public Date getTo() {
		return to;
	}
	
	public Double getDurationHours() {
		return durationHours;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setFrom(Date from) {
		this.from = from;
	}
	
	public void setTo(Date to) {
		this.to = to;
		this.durationHours = new Double(to.getTime() - from.getTime());
	}
	
	/**
	 * Internal method.
	 * @return The PersistenceManager associated with this object. 
	 */
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager issueWorkTimeRangePM = JDOHelper.getPersistenceManager(this);
		if (issueWorkTimeRangePM == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");

		return issueWorkTimeRangePM;
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
			Util.equals(this.issueWorkTimeRangeID, o.issueWorkTimeRangeID);
	}

	@Override
	public int hashCode()
	{
		return 
			Util.hashCode(organisationID) ^
			Util.hashCode(issueID) ^
			Util.hashCode(issueWorkTimeRangeID);
	}
}