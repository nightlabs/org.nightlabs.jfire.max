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
import javax.jdo.listener.DeleteCallback;

import org.nightlabs.jdo.ObjectID;
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
 * @jdo.fetch-group name="Issue.fileList" fields="fileList"
 * @jdo.fetch-group name="Issue.description" fields="description"
 * @jdo.fetch-group name="Issue.subject" fields="subject" 
 * @jdo.fetch-group name="Issue.issuePriority" fields="issuePriority"
 * @jdo.fetch-group name="Issue.issueSeverityType" fields="issueSeverityType"
 * @jdo.fetch-group name="Issue.issueResolution" fields="issueResolution"
 * @jdo.fetch-group name="Issue.state" fields="state"
 * @jdo.fetch-group name="Issue.states" fields="states"
 * @jdo.fetch-group name="Issue.issueLocal" fields="issueLocal"
 * @jdo.fetch-group name="Issue.issueType" fields="issueType"
 * @jdo.fetch-group name="Issue.comments" fields="comments"
 * @jdo.fetch-group name="Issue.issueLinks" fields="issueLinks"
 * @jdo.fetch-group name="Issue.propertySet" fields="propertySet"
 * @jdo.fetch-group name="Issue.reporter" fields="reporter"
 * @jdo.fetch-group name="Issue.assignee" fields="assignee"
 *
 * @jdo.fetch-group name="Issue.this" fetch-groups="default"
 * 	fields="fileList, description, subject, issuePriority, issueSeverityType, issueResolution, 
 * 					state, states, issueLocal, issueType, comments, issueLinks, propertySet,reporter,
 * 					assignee"
 *
 * @jdo.fetch-group name="Statable.state" fields="state"
 * @jdo.fetch-group name="Statable.states" fields="states"
 */
public class Issue
implements 	Serializable, AttachCallback, Statable, DeleteCallback
{

	private static final long serialVersionUID = 1L;
//	private static final Logger logger = Logger.getLogger(Issue.class);

	public static final String FETCH_GROUP_THIS_ISSUE = "Issue.this";
	public static final String FETCH_GROUP_DESCRIPTION = "Issue.description";
	public static final String FETCH_GROUP_SUBJECT = "Issue.subject";
	public static final String FETCH_GROUP_ISSUE_SEVERITY_TYPE = "Issue.issueSeverityType";
	public static final String FETCH_GROUP_STATE = "Issue.state";
	public static final String FETCH_GROUP_STATES = "Issue.states";
	public static final String FETCH_GROUP_ISSUE_PRIORITY = "Issue.issuePriority";
	public static final String FETCH_GROUP_ISSUE_RESOLUTION = "Issue.issueResolution";
	public static final String FETCH_GROUP_ISSUE_TYPE = "Issue.issueType";
	public static final String FETCH_GROUP_ISSUE_LOCAL = "Issue.issueLocal";
	public static final String FETCH_GROUP_ISSUE_COMMENT = "Issue.comments";
	public static final String FETCH_GROUP_ISSUE_LINKS = "Issue.issueLinks";
	public static final String FETCH_GROUP_ISSUE_REPORTER = "Issue.reporter";
	public static final String FETCH_GROUP_ISSUE_ASSIGNEE = "Issue.assignee";
	public static final String FETCH_GROUP_ISSUE_FILELIST = "Issue.fileList";
	
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

//	/**
//	 * Instances of String that are representations of {@link ObjectID}s.
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="collection"
//	 *		element-type="String"
//	 *		dependent-value="true"
//	 *		table="JFireIssueTracking_Issue_referencedObjectIDs"
//	 *
//	 * @jdo.join
//	 */
//	private Set<String> referencedObjectIDs;
	
	/**
	 * Instances of IssueLink that are representations of {@link ObjectID}s.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssueLink"
	 *		dependent-value="true"
	 *		mapped-by="issue"
	 */
	private Set<IssueLink> issueLinks;
	
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
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issue"
	 */
	private IssueSubject subject;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issue"
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
	 * @jdo.field persistence-modifier="persistent" @!dependent="true"
	 */
	private State state;
	
	/**
	 * This is the history of <b>public</b> {@link State}s with the newest last and the oldest first.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="State"
	 *		table="JFireIssueTracking_Issue_states"
	 *		@!dependent-value="true"
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
	@Deprecated
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
//		referencedObjectIDs = new HashSet<String>();
		issueLinks = new HashSet<IssueLink>();
		
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
	/*public Set<ObjectID> getReferencedObjectIDs() {
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
	}*/
	
	public Set<IssueLink> getIssueLinks() {
		return issueLinks;
	}
	
	public Set<ObjectID> getLinkObjectIDs() {
		if (_linkObjectIDs == null) {
			Set<ObjectID> ro = new HashSet<ObjectID>(issueLinks.size());
			for (IssueLink issueLink : issueLinks) {
				ObjectID objectID = issueLink.getLinkedObjectID();
				ro.add(objectID);
			}
			_linkObjectIDs = ro;
		}
		return Collections.unmodifiableSet(_linkObjectIDs);
	}

//	public void addLinkObjectID(ObjectID linkObjectID) {
//		if (linkObjectID == null)
//			throw new IllegalArgumentException("linkObjectID must not be null!");
//
//		issueLinks.add(new IssueLink(this, IDGenerator.nextID(IssueLink.class), linkObjectID.toString(), new IssueLinkType()));
//		if (_linkObjectIDs != null) // instead of managing our cache of ObjectID instances, we could alternatively simply null it here, but the current implementation is more efficient.
//			_linkObjectIDs.add(linkObjectID);
//	}

	public IssueLink createIssueLink(IssueLinkType issueLinkType, Object linkedObject)
	{
		IssueLink issueLink = new IssueLink(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(IssueLink.class),
				this, issueLinkType, linkedObject);

		issueLinks.add(issueLink);

		return issueLink;
	}

	public void removeLinkObjectID(ObjectID linkObjectID) {
		if (linkObjectID == null)
			throw new IllegalArgumentException("linkObjectID must not be null!");

		issueLinks.remove(linkObjectID.toString());
		if (_linkObjectIDs != null)
			_linkObjectIDs.remove(linkObjectID);
	}
	
	public void clearIssueLinks()
	{
		issueLinks.clear();
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<ObjectID> _linkObjectIDs;


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
	
	private void afterCreateIssueLink(IssueLink newIssueLink) {

	}

	public IssueLocal getIssueLocal() {
		return issueLocal;
	}

	private void beforeDeleteIssueLink(IssueLink issueLinkToBeDeleted) {}
	private void afterDeleteIssueLink(IssueLink issueLinkDeleted) {}

	

	@Override
	public void jdoPreDelete() {
		PersistenceManager pm = getPersistenceManager();

		Set<State> statesToDelete;
		if (this.issueLocal == null)
			statesToDelete = new HashSet<State>();
		else
			statesToDelete = new HashSet<State>(this.issueLocal.clearStatesBeforeDelete());

		statesToDelete.addAll(this.states);
		statesToDelete.add(this.state);

		this.states.clear();
		this.state = null;

		pm.flush();

		for (State state : statesToDelete)
			pm.deletePersistent(state);
	}
}
