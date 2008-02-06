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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.AttachCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
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
 *
 * @jdo.query
 *		name="getIssuesByIssueTypeID"
 *		query="SELECT
 *			WHERE this.issueTypeID == paramIssueTypeID                    
 *			PARAMETERS String paramIssueTypeID
 *			import java.lang.String"
 *
 * @jdo.fetch-group name="Issue.fileList" fetch-groups="default" fields="fileList"
 * @jdo.fetch-group name="Issue.description" fetch-groups="default" fields="description"
 * @jdo.fetch-group name="Issue.subject" fetch-groups="default" fields="subject" 
 * @jdo.fetch-group name="Issue.issuePriority" fetch-groups="default" fields="issuePriority"
 * @jdo.fetch-group name="Issue.issueSeverityType" fetch-groups="default" fields="issueSeverityType"
 * @jdo.fetch-group name="Issue.issueResolution" fetch-groups="default" fields="issueResolution"
 * @jdo.fetch-group name="Issue.state" fetch-groups="default" fields="state"
 * @jdo.fetch-group name="Issue.states" fetch-groups="default" fields="states"
 * @jdo.fetch-group name="Statable.state" fetch-groups="default" fields="state"
 * @jdo.fetch-group name="Statable.states" fetch-groups="default" fields="states"
 * @jdo.fetch-group name="Issue.issueLocal" fetch-groups="default" fields="issueLocal"
 * @jdo.fetch-group name="Issue.issueType" fetch-groups="default" fields="issueType"
 * @jdo.fetch-group name="Issue.comments" fetch-groups="default" fields="comments"
 * @jdo.fetch-group name="Issue.this" fetch-groups="default" fields="fileList, issueType, referencedObjectIDs, description, subject, issuePriority, issueSeverityType, issueResolution, state, states, comments, issueLocal, reporter, assignee" 
 *
 * @jdo.fetch-group name="Issue.propertySet" fetch-groups="default" fields="propertySet"
 **/
public class Issue
implements 	
		Serializable, AttachCallback, Statable
{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Issue.class);

	public static final String FETCH_GROUP_THIS = "Issue.this";
	public static final String FETCH_GROUP_DESCRIPTION = "Issue.description";
	public static final String FETCH_GROUP_SUBJECT = "Issue.subject";
	public static final String FETCH_GROUP_ISSUE_SEVERITY_TYPE = "Issue.issueSeverityType";
	public static final String FETCH_GROUP_STATE = "Issue.state";
	public static final String FETCH_GROUP_STATES = "Issue.states";
	public static final String FETCH_GROUP_ISSUE_PRIORITY = "Issue.issuePriority";
	public static final String FETCH_GROUP_ISSUE_RESOLUTION = "Issue.issueResolution";
	public static final String fETCH_GROUP_ISSUE_TYPE = "Issue.issueType";
	public static final String FETCH_GROUP_ISSUE_LOCAL = "Issue.issueLocal";
	public static final String fETCH_GROUP_ISSUE_COMMENT = "Issue.comments";
	
	public static final String FETCH_GROUP_PROPERTY_SET = "Issue.propertySet";
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueType issueType;

	/**
	 * Instances of String that are representations of {@link ObjectID}s.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="String"
	 *		dependent-value="true"
	 *		table="JFireIssueTracking_Issue_referencedObjectIDs"
	 *
	 * @jdo.join
	 */
	private Set<String> referencedObjectIDs;
	
	/**
	 * Instances of {@link IssueFileAttachment}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssueFileAttachment"
	 *		dependent-value="true"
	 *		mapped-by="issue"
	 */
	private List<IssueFileAttachment> fileList;
	
	/**
	 * Instances of {@link IssueComment}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssueComment"
	 *		dependent-value="true"
	 *		mapped-by="issue"
	 */
	private List<IssueComment> comments;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssuePriority issuePriority;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueSeverityType issueSeverityType;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private IssueSubject subject;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private IssueDescription description;

	/**
	 * @jdo.field persistence-modifier="persistent" load-fetch-group="all"
	 */
	private User reporter; 
	
	/**
	 * @jdo.field persistence-modifier="persistent" load-fetch-group="all"
	 */
	private User assignee; 

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date updateTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueResolution issueResolution;
	
	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="issue" dependent="true"
	 */
	private IssueLocal issueLocal;
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private State state;
	
	/**
	 * This is the history of <b>public</b> {@link State}s with the newest last and the oldest first.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="State"
	 *		dependent-value="true"
	 *		table="JFireIssueTracking_Issue_states"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private List<State> states;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PropertySet propertySet;
	
	/**
	 * Returns the property set of this {@link Issue}.
	 * 
	 * @return The property set of this {@link Issue}.
	 */
	public PropertySet getPropertySet() {
		return propertySet;
	}
	
	/**
	 * The scope of the StructLocal by which the propertySet is build from.
	 * 
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 */
	private String structLocalScope;
	
	/**
	 * Returns the scope of the StructLocal by which the propertySet is build from.
	 * @return The scope of the StructLocal by which the propertySet is build from.
	 */
	public String getStructLocalScope() {
		return structLocalScope;
	}
	
	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	protected Issue() { }

	public Issue(String organisationID, long issueID)
	{
		this.organisationID = organisationID;
		this.createTimestamp = new Date();
		this.issueID = issueID;
		
		subject = new IssueSubject(this);
		description = new IssueDescription(this);
		
		fileList = new ArrayList<IssueFileAttachment>();
		comments = new ArrayList<IssueComment>();
		referencedObjectIDs = new HashSet<String>();
		
		this.issueLocal = new IssueLocal(this);
		this.structLocalScope = StructLocal.DEFAULT_SCOPE;
		this.propertySet = new PropertySet(organisationID, IDGenerator.nextID(PropertySet.class), Issue.class.getName(), structLocalScope);
	}
	
	public Issue(String organisationID, long issueID, IssueType issueType)
	{
		this(organisationID, issueID);
		this.issueType = issueType;
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
	 * @return Returns the issueType.
	 */
	public IssueType getIssueType() {
		return issueType;
	}

	/**
	 * @param issueTypeID The issueTypeID to set.
	 */
	public void setIssueType(IssueType issueType) {
		this.issueType = issueType;
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
	 * @return Returns the assignee.
	 */
	public User getAssignee() {
		return assignee;
	}

	/**
	 * @param assignee The user to set.
	 */
	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}

	public IssuePriority getIssuePriority() {
		return issuePriority;
	}

	public void setIssuePriority(IssuePriority issuePriority) {
		this.issuePriority = issuePriority;
	}

	public IssueSeverityType getIssueSeverityType() {
		return issueSeverityType;
	}

	public void setIssueSeverityType(IssueSeverityType issueSeverityType) {
		this.issueSeverityType = issueSeverityType;
	}

	/**
	 * @param organisationID The organisationID to set.
	 */
	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	public List<IssueFileAttachment> getFileList() {
		return fileList;
	}

	public List<IssueComment> getComments() {
		return comments;
	}
	
//	/**
//	 * @deprecated It is not good practice to expose 1-n-relationships in a JDO object. Since this Set here is solely linking simple Strings, it works fine,
//	 * but to be consistent, there should never be the possibility (in a JDO object) to replace the set (in non-JDO-objects there are many reasons to do the same,
//	 * as well). Therefore you should better have add and remove methods here and remove the setter.
//	 * As you see below, I've hidden the internal String management more or less completely and instead work with instances of {@link ObjectID} - which
//	 * is a much nicer API. This couldn't be easily done when exposing the Set<String> referencedObjectIDs directly. 
//	 */
//	public void setReferencedObjectIDs(Set<String> objIds) {
//		this.referencedObjectIDs = objIds;
//	}

	// This method should be named "getReferencedObjectIDs()". Please rename it, after you removed the above method, which currently blocks the name
	public Set<ObjectID> getReferencedObjectIDs() {
		if (_referencedObjectIDs == null) {
			Set<ObjectID> ro = new HashSet<ObjectID>(referencedObjectIDs.size());
			for (String objectIDString : referencedObjectIDs) {
				ObjectID objectID = ObjectIDUtil.createObjectID(objectIDString);
				ro.add(objectID);
			}
			_referencedObjectIDs = ro;
		}
		return Collections.unmodifiableSet(_referencedObjectIDs);
	}

	public void addReferencedObjectID(ObjectID referencedObjectID)
	{
		if (referencedObjectID == null)
			throw new IllegalArgumentException("referencedObjectID must not be null!");

		referencedObjectIDs.add(referencedObjectID.toString());
		if (_referencedObjectIDs != null) // instead of managing our cache of ObjectID instances, we could alternatively simply null it here, but the current implementation is more efficient.
			_referencedObjectIDs.add(referencedObjectID);
	}

	public void removeReferencedObjectID(ObjectID referencedObjectID)
	{
		if (referencedObjectID == null)
			throw new IllegalArgumentException("referencedObjectID must not be null!");

		referencedObjectIDs.remove(referencedObjectID.toString());
		if (_referencedObjectIDs != null)
			_referencedObjectIDs.remove(referencedObjectID);
	}
	
	public void clearReferencedObjectIDs()
	{
		referencedObjectIDs.clear();
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<ObjectID> _referencedObjectIDs;


	public IssueResolution getIssueResolution() {
		return issueResolution;
	}
	
	public void setIssueResolution(IssueResolution issueResolution) {
		this.issueResolution = issueResolution;
	}

	/**
	 * {@inheritDoc}}
	 */
	public StatableLocal getStatableLocal() {
		return issueLocal;
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient List<State> _states = null;

	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public void setState(State state) {
		this.state = state;
		this.states.add(state);
	}

	protected void ensureIssueHasProcessInstance() {
		if (this.getStatableLocal().getJbpmProcessInstanceId() < 0) {
			if (this.getIssueType() == null)
				throw new IllegalStateException("Could not create ProcessInstance for Issue as its IssueType is null.");
			getIssueType().createProcessInstanceForIssue(this);
		}
	}
	
	/**
	 * See {@link #jdoPreStore()}.
	 */
	public void jdoPostAttach(Object attached) {
		ensureIssueHasProcessInstance();
	}

	public void jdoPreAttach() {
	}
	
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Issue is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof Issue))
			return false;

		Issue o = (Issue) obj;

		return
			Util.equals(this.organisationID, o.organisationID) &&
			Util.equals(this.issueID, o.issueID);
	}

	@Override
	public int hashCode()
	{
		return
			Util.hashCode(this.organisationID) ^
			Util.hashCode(this.issueID);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	public static String getPrimaryKey(String organisationID, long issueID)
	{
		return organisationID + '/' + Long.toString(issueID);
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}
}
