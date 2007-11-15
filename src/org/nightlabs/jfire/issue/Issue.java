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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DetachCallback;

import org.apache.log4j.Logger;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;
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
 * @jdo.fetch-group name="Issue.fileAttachment" fetch-groups="default" fields="fileAttachment"
 * @jdo.fetch-group name="Issue.description" fetch-groups="default" fields="description"
 * @jdo.fetch-group name="Issue.subject" fetch-groups="default" fields="subject" 
 * @jdo.fetch-group name="Issue.priority" fetch-groups="default" fields="priority"
 * @jdo.fetch-group name="Issue.severityType" fetch-groups="default" fields="severityType"
 * @jdo.fetch-group name="Issue.status" fetch-groups="default" fields="stateDefinition"
 * @jdo.fetch-group name="Issue.this" fetch-groups="default" fields="fileAttachment, description, subject, priority, severityType, stateDefinition, reporter, assigntoUser"
 *
 **/
public class Issue
implements 	
		Serializable,
		DetachCallback
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
//	*
//	* @jdo.field
//	*    persistence-modifier="persistent"
//	*    collection-type="collection"
//	*    element-type="ObjectID" 
//	*    mapped-by="supplier"
//	**/
//	private Collection attachedDocuments = new HashSet();

	/**
	 * Instances of {@link String}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="String"
	 *		dependent-value="true"
	 *		mapped-by="issue"
	 */
	private Set<String> referencedObjectIDs;

//	private Set<Object> referencedObjects;
	
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
	private StateDefinition stateDefinition;


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
	private User reporter; 
	
	/**
	 * @jdo.field persistence-modifier="persistent" load-fetch-group="all"
	 */
	private User assigntoUser; 

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date updateTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent" collection-type="array" serialized-element="true"
	 */
	private byte[] fileAttachment;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date fileTimestamp;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String fileName;

	public void loadStream(InputStream in, long length, Date timeStamp, String name)
	throws IOException
	{
		logger.debug("Loading stream as ReportLayout");
		boolean error = true;
		try {
			DataBuffer db = new DataBuffer((long) (length * 0.6));
			OutputStream out = new DeflaterOutputStream(db.createOutputStream());
			try {
				Util.transferStreamData(in, out);
			} finally {
				out.close();
			}
			fileAttachment = db.createByteArray();

			fileTimestamp = timeStamp;
			fileName = name;

			error = false;
		} finally {
			if (error) { // make sure that in case of an error all the file members are null.
				fileName = null;
				fileTimestamp = null;
				fileAttachment = null;
			}
		}
	}
	
	/**
	 * Creates a new {@link InputStream} for the report design
	 * that is wrapped by an {@link InflaterInputStream}.
	 * This means you can read the report design unzipped from the returend stream.
	 */
	public InputStream createReportDesignInputStream() {
		return new InflaterInputStream(new ByteArrayInputStream(fileAttachment));
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Date getFileTimestamp() {
		return fileTimestamp;
	}
	
	public void loadStream(InputStream in, String name) 
	throws IOException 
	{
		loadStream(in, 10 * 1024, new Date(), name);
	}
	
	public void loadFile(File f)
	throws IOException
	{
		logger.debug("Loading file "+f+" as ReportLayout");
		FileInputStream in = new FileInputStream(f);
		try {
			loadStream(in, f.length(), new Date(f.lastModified()), f.getName());
		} finally {
			in.close();
		}
	}
	
	/**
	 * Creates a new {@link InputStream} for the file attachment
	 * that is wrapped by an {@link InflaterInputStream}.
	 * This means you can read the file attachment unzipped from the returend stream.
	 */
	public InputStream createFileAttachmentInputStream() {
		return new InflaterInputStream(new ByteArrayInputStream(fileAttachment));
	}
	
	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	protected Issue() { }

	public Issue(String organisationID, IssuePriority priority, IssueSeverityType severityType, StateDefinition stateDefinition, User reporter, User assigntoUser, ObjectID objectID)
	{
		if (reporter == null)
			throw new NullPointerException("reporter");

		this.organisationID = organisationID;
		this.createTimestamp = new Date();
		
		this.issueID = objectID!=null?objectID.toString()+"&"+createTimestamp.toString():createTimestamp.toString();
//		this.documents = new <String>();

		this.priority = priority;
		this.severityType = severityType;
		this.stateDefinition = stateDefinition;

		subject = new IssueSubject(this);
		description = new IssueDescription(this);
		
		this.reporter = reporter;
		this.assigntoUser = assigntoUser;
	
	}
	
	public Issue(String organisationID, IssuePriority priority, IssueSeverityType severityType, StateDefinition stateDefinition, User reporter, User assigntoUser, ObjectID objectID, byte[] fileAttachment)
	{
		this(organisationID, priority, severityType, stateDefinition, reporter, assigntoUser, objectID);
		this.fileAttachment = fileAttachment;
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
	 * @return Returns the reporter.
	 */
	public User getReporter() {
		return reporter;
	}

	/**
	 * @param reporter The user to set.
	 */
	public void setReporter(User reporter) {
		this.reporter = reporter;
	}

	/**
	 * @return Returns the assigntoUser.
	 */
	public User getAssigntoUser() {
		return assigntoUser;
	}

	/**
	 * @param assigntoUser The user to set.
	 */
	public void setAssigntoUser(User assigntoUser) {
		this.assigntoUser = assigntoUser;
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

	public StateDefinition getStateDefinition() {
		return stateDefinition;
	}

	public void setStateDefinition(StateDefinition stateDefinition) {
		this.stateDefinition = stateDefinition;
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
//	Issue attached = (Issue)_attached;
//	Issue detached = this;
//	Collection fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();
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

	public void jdoPreDetach() {
		// TODO Auto-generated method stub
		
	}
	
	public void jdoPostDetach(Object _attached) {
//		Offer attached = (Offer)_attached;
//		Offer detached = this;
//		PersistenceManager pm = attached.getPersistenceManager();
//		Collection fetchGroups = pm.getFetchPlan().getGroups();
//
//		if (fetchGroups.contains(FETCH_GROUP_THIS_OFFER) || fetchGroups.contains(FETCH_GROUP_VENDOR)) {
//			detached.vendor = (LegalEntity) pm.detachCopy(attached.getVendor());
//			detached.vendor_detached = true;
//		}
//
//		if (fetchGroups.contains(FETCH_GROUP_THIS_OFFER) || fetchGroups.contains(FETCH_GROUP_CUSTOMER)) {
//			detached.customer = (LegalEntity) pm.detachCopy(attached.getCustomer());
//			detached.customer_detached = true;
//		}
//
//		if (fetchGroups.contains(FETCH_GROUP_THIS_OFFER) || fetchGroups.contains(FETCH_GROUP_VENDOR_ID)) {
//			detached.vendorID = attached.getVendorID();
//			detached.vendorID_detached = true;
//		}
//
//		if (fetchGroups.contains(FETCH_GROUP_THIS_OFFER) || fetchGroups.contains(FETCH_GROUP_CUSTOMER_ID)) {
//			detached.customerID = attached.getCustomerID();
//			detached.customerID_detached = true;
//		}
//		detached.attachable = true;
	}
}
