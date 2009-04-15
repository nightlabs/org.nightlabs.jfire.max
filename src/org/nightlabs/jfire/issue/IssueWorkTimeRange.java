package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.NLLocale;
import org.nightlabs.util.Util;

/**
 * The {@link IssueWorkTimeRange} class represents a range of working time of each {@link Issue}s. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
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
 * @jdo.query
 *		name="getIssueWorkTimeRangeByUserAndIssueID"
 *		query="SELECT
 *			WHERE this.user == :user && this.issueID== :issueID"
 *
 **/
public class IssueWorkTimeRange
implements Serializable, Comparable<IssueWorkTimeRange> 
{
	private static final long serialVersionUID = 1L;
//	private static final Logger logger = Logger.getLogger(IssueWorkTimeRange.class);

	public static final String FETCH_GROUP_ISSUE = "IssueWorkTimeRange.issue";
	public static final String FETCH_GROUP_USER = "IssueWorkTimeRange.user";
	public static final String FETCH_GROUP_FROM = "IssueWorkTimeRange.from";
	public static final String FETCH_GROUP_TO = "IssueWorkTimeRange.to";
	
	public static final String QUERY_ISSUE_WORK_TIME_RANGE_BY_USER_AND_ISSUE_ID = "getIssueWorkTimeRangeByUserAndIssueID";
	
	/**
	 * This is the organisationID to which the issue work time range belongs. Within one organisation,
	 * all the issue work time ranges have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
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
	private long duration;
	
	/**
	 * @jdo.field 
	 * 		persistence-modifier="persistent"
	 * 		load-fetch-group="all"
	 */
	private User user;

	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	@Deprecated
	protected IssueWorkTimeRange() { }

	/**
	 * 
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssueWorkTimeRange</code>
	 * @param issueWorkTimeRangeID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssueWorkTimeRange.class</code> to create an id
	 * @param user the user who takes this issue
	 * @param issue the issue 
	 */
	public IssueWorkTimeRange(String organisationID, long issueWorkTimeRangeID, User user, Issue issue)
	{
		Organisation.assertValidOrganisationID(organisationID);
		
		if (issue == null)
			throw new IllegalArgumentException("issue must not be null!");
		
		if (user == null)
			throw new IllegalArgumentException("user must not be null!");
		
		this.organisationID = organisationID;
		this.issueWorkTimeRangeID = issueWorkTimeRangeID;
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

	/**
	 * 
	 * @return
	 */
	public Issue getIssue(){
		return issue;
	}

	/**
	 * 
	 * @return
	 */
	public Date getFrom() {
		return from;
	}
	
	/**
	 * 
	 * @return
	 */
	public Date getTo() {
		return to;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getDuration() {
		return duration;
	}
	
	/**
	 * 
	 * @return
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * 
	 * @param from
	 */
	public void setFrom(Date from) {
		this.from = from;
	}
	
	/**
	 * 
	 * @param to
	 */
	public void setTo(Date to) {
		this.to = to;
		this.duration = to.getTime() - from.getTime();
	}
	
	/**
	 * 
	 */
	public void setDuration(long duration) {
		if (to == null) {
			Calendar c = Calendar.getInstance(NLLocale.getDefault());
			c.setTime(from);
			c.add(Calendar.MILLISECOND, (int)duration);
		}
		this.duration = duration;
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
	/*
	 * 
	 */
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
	/*
	 * 
	 */
	public int hashCode()
	{
		return 
			(31 * Util.hashCode(organisationID)) +
			Util.hashCode(issueID) ^
			Util.hashCode(issueWorkTimeRangeID);
	}

	@Override
	public int compareTo(IssueWorkTimeRange o) {
		return this.from.compareTo(o.from);
	}
}