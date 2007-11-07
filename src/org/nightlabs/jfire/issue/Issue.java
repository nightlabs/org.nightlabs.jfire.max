/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Utils;

/**
 * @author Chairat Kongarayawetchakun <!-- chairatk at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueID"
 *		detachable="true"
 *		table="JFireIssueTracking_Issue"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, issueID"
 *		include-imports="id/IssueID.imports.inc"
 *		include-body="id/IssueID.body.inc"
 *
 * @jdo.query
 *		name="getIssuesByIssueTypeID"
 *		query="SELECT
 *			WHERE this.issueTypeID == paramIssueTypeID                    
 *			PARAMETERS String paramIssueTypeID
 *			import java.lang.String"
 *
 * @jdo.fetch-group name="Issue.description" fields="description"
 * @jdo.fetch-group name="Issue.subject" fields="subject"
 * @jdo.fetch-group name="Issue.priority" fields="priority"
 * @jdo.fetch-group name="Issue.severityType" fields="severityType"
 * @jdo.fetch-group name="Issue.status" fields="status"
 * @jdo.fetch-group name="Issue.this" fetch-groups="default" fields="priority, severityType, status, creator, description"
 *
 **/
public class Issue
implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Issue.class);
	
	public static final String FETCH_GROUP_THIS = "Issue.this";
	public static final String FETCH_GROUP_DESCRIPTION = "Issue.description";
	public static final String FETCH_GROUP_SUBJECT = "Issue.subject";
	public static final String FETCH_GROUP_SEVERITYTYPE = "Issue.severityType";
	public static final String FETCH_GROUP_STATUS = "Issue.status";
	public static final String FETCH_GROUP_PRIORITY = "Issue.priority";
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	private long issueTypeID;

//	/** Documents for the issue
//    *
//    * @jdo.field
//    *    persistence-modifier="persistent"
//    *    collection-type="collection"
//    *    element-type="ObjectID" 
//    *    mapped-by="supplier"
//    **/
//	private Collection attachedDocuments = new HashSet();
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssuePriority priority;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueSeverityType severityType;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueStatus status;
	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueSubject subject;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueDescription description;
	
	/**
	 * @jdo.field persistence-modifier="persistent" load-fetch-group="all"
	 */
	private User creator; 
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createTimestamp;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date updateTimestamp;
	

	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	protected Issue() { }

	public Issue(String organisationID, IssuePriority priority, IssueSeverityType severityType, IssueStatus status, User creator, ObjectID objectID)
	{
		if (creator == null)
			throw new NullPointerException("creator");
		this.creator = creator;
		this.createTimestamp = new Date();
		
		this.organisationID = organisationID;
		this.issueID = objectID!=null?objectID.toString()+"&"+createTimestamp.toString():createTimestamp.toString();
//		this.documents = new <String>();
		
		this.priority = priority;
		this.severityType = severityType;
		this.status = status;
		
		subject = new IssueSubject(this);
		description = new IssueDescription(this);
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
	public String getIssueID() {
		return issueID;
	}

	/**
	 * @param issueID The issueID to set.
	 */
	public void setIssueID(String issueID) {
		this.issueID = issueID;
	}

	/**
	 * @return Returns the issueTypeID.
	 */
	public long getIssueTypeID() {
		return issueTypeID;
	}

	/**
	 * @param issueTypeID The issueTypeID to set.
	 */
	public void setIssueTypeID(long issueTypeID) {
		this.issueTypeID = issueTypeID;
	}

	/**
	 * @return Returns the create timestamp.
	 */
	public Date getCreateTimestamp() {
		return createTimestamp;
	}
	
	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setCreateTimestamp(Date timestamp) {
		this.createTimestamp = timestamp;
	}
	
	/**
	 * @return Returns the update timestamp.
	 */
	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}
	
	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setUpdateTimestamp(Date timestamp) {
		this.updateTimestamp = timestamp;
	}
	
	/**
	 * @return Returns the description.
	 */
	public IssueDescription getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(IssueDescription description) {
		this.description = description;
	}
	
	/**
	 * @return Returns the subject.
	 */
	public IssueSubject getSubject() {
		return subject;
	}

	/**
	 * @param subject The subject to set.
	 */
	public void setSubject(IssueSubject subject) {
		this.subject = subject;
	}

	/**
	 * @return Returns the user.
	 */
	public User getUser() {
		return creator;
	}

	/**
	 * @param user The user to set.
	 */
	public void setUser(User user) {
		this.creator = user;
	}

	
	public IssuePriority getPriority() {
		return priority;
	}

	public void setPriority(IssuePriority priority) {
		this.priority = priority;
	}

	public IssueSeverityType getSeverityType() {
		return severityType;
	}

	public void setSeverityType(IssueSeverityType severityType) {
		this.severityType = severityType;
	}

	public IssueStatus getStatus() {
		return status;
	}

	public void setStatus(IssueStatus status) {
		this.status = status;
	}

	/**
	 * @param organisationID The organisationID to set.
	 */
	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Issue is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

//	public void jdoPreDetach()
//	{
//	}
//	public void jdoPostDetach(Object _attached)
//	{
//		Issue attached = (Issue)_attached;
//		Issue detached = this;
//		Collection fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();
//	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof Issue))
			return false;

		Issue o = (Issue) obj;

		return
		Utils.equals(this.organisationID, o.organisationID); //&& 
	}

	@Override
	public int hashCode()
	{
		return
		Utils.hashCode(this.organisationID); //^
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;
	
	public static String getPrimaryKey(String organisationID, String issueID)
	{
		return organisationID + '/' + issueID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}
}
