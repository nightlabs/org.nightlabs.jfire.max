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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DeleteCallback;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.dao.IssueTypeDAO;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.project.Department;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.User;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * The {@link Issue} class represents an issue in JFire.
 * <p>
 * In computing, the term issue is a unit of work to accomplish an improvement in a data system.
 * An issue could be a bug, a requested feature, task, missing documentation, and so forth.
 * The word "issue" is popularly misused in lieu of "problem."
 * </p>
 * <p>
 * An {@link Issue} created by a JFire's {@link User} should at lease has {@link IssueSubject} and {@link IssueDescription}.
 * </p>
 *
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
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
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.Statable"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, issueID"
 *
 * @jdo.query
 *		name="getIssuesByProjectID"
 *		query="SELECT
 *			WHERE
 *				this.project.organisationID == :organisationID &&
 *				this.project.projectID == :projectID"
 *
 * @jdo.query
 *		name="getIssuesByProjectTypeID"
 *		query="SELECT
 *			WHERE
 *				this.project.projectType.organisationID == :organisationID &&
 *				this.project.projectType.projectTypeID == :projectTypeID"
 *
 * @jdo.fetch-group name="Issue.issueFileAttachments" fields="issueFileAttachments"
 * @jdo.fetch-group name="Issue.description" fields="description"
 * @jdo.fetch-group name="Issue.subject" fields="subject"
 * @jdo.fetch-group name="Issue.issuePriority" fields="issuePriority"
 * @jdo.fetch-group name="Issue.issueSeverityType" fields="issueSeverityType"
 * @jdo.fetch-group name="Issue.issueResolution" fields="issueResolution"
 * @jdo.fetch-group name="Issue.state" fields="state"
 * @jdo.fetch-group name="Issue.states" fields="states"
 * @jdo.fetch-group name="Issue.issueLocal" fields="issueLocal"
 * @jdo.fetch-group name="Issue.issueType" fields="issueType"
 * @jdo.fetch-group name="Issue.project" fields="project"
 * @jdo.fetch-group name="Issue.comments" fields="comments"
 * @jdo.fetch-group name="Issue.issueLinks" fields="issueLinks"
 * @jdo.fetch-group name="Issue.propertySet" fields="propertySet"
 * @jdo.fetch-group name="Issue.reporter" fields="reporter"
 * @jdo.fetch-group name="Issue.assignee" fields="assignee"
 * @jdo.fetch-group name="Issue.issueWorkTimeRanges" fields="issueWorkTimeRanges"
 *
 * @jdo.fetch-group name="Statable.state" fields="state"
 * @jdo.fetch-group name="Statable.states" fields="states"
 */
public class Issue
implements 	Serializable, AttachCallback, Statable, DeleteCallback
{
	private static final long serialVersionUID = 20080610L;

	public static final String FETCH_GROUP_DESCRIPTION = "Issue.description";
	public static final String FETCH_GROUP_SUBJECT = "Issue.subject";
	public static final String FETCH_GROUP_ISSUE_SEVERITY_TYPE = "Issue.issueSeverityType";
	public static final String FETCH_GROUP_STATE = "Issue.state";
	public static final String FETCH_GROUP_STATES = "Issue.states";
	public static final String FETCH_GROUP_ISSUE_PRIORITY = "Issue.issuePriority";
	public static final String FETCH_GROUP_ISSUE_RESOLUTION = "Issue.issueResolution";
	public static final String FETCH_GROUP_ISSUE_TYPE = "Issue.issueType";
	public static final String FETCH_GROUP_ISSUE_LOCAL = "Issue.issueLocal";
	public static final String FETCH_GROUP_ISSUE_COMMENTS = "Issue.comments";
	public static final String FETCH_GROUP_ISSUE_LINKS = "Issue.issueLinks";
	public static final String FETCH_GROUP_ISSUE_REPORTER = "Issue.reporter";
	public static final String FETCH_GROUP_ISSUE_ASSIGNEE = "Issue.assignee";
	public static final String FETCH_GROUP_ISSUE_WORK_TIME_RANGES = "Issue.issueWorkTimeRanges";
	public static final String FETCH_GROUP_ISSUE_FILELIST = "Issue.issueFileAttachments";
	public static final String FETCH_GROUP_ISSUE_PROJECT = "Issue.project";

	public static final String FETCH_GROUP_PROPERTY_SET = "Issue.propertySet";

	/**
	 * This is the organisationID to which the issue belongs. Within one organisation,
	 * all the issues have their organisation's ID stored here, thus it's the same
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueType issueType;

	/**
	 * Instances of IssueLink that are representations of {@link ObjectID}s.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssueLink"
	 *		dependent-element="true"
	 *		mapped-by="issue"
	 */
	private Set<IssueLink> issueLinks;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Project project;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Department department;

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
	private List<IssueFileAttachment> issueFileAttachments;

	/**
	 * Instances of IssueWorkTimeRange.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssueWorkTimeRange"
	 *		dependent-element="true"
	 *		mapped-by="issue"
	 */
	private List<IssueWorkTimeRange> issueWorkTimeRanges;

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
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		dependent="true"
	 * 		mapped-by="issue"
	 */
	private IssueSubject subject;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		dependent="true"
	 * 		mapped-by="issue"
	 */
	private IssueDescription description;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		load-fetch-group="all"
	 */
	private User reporter;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		load-fetch-group="all"
	 */
	private User assignee;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean isStarted = false;;

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
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		mapped-by="issue"
	 * 		dependent="true"
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
	 * @return The property set of this {@link Issue}
	 */
	public PropertySet getPropertySet() {
		return propertySet;
	}

	/**
	 * The scope of the {@link StructLocal} by which the propertySet is build from.
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 */
	private String structLocalScope;

	/**
	 * Returns the scope of the StructLocal by which the propertySet is build from.
	 * @return The scope of the StructLocal by which the propertySet is build from
	 */
	public String getStructLocalScope() {
		return structLocalScope;
	}

	/**
	 * The scope of the {@link Struct} by which the propertySet is build from.
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 */
	private String structScope;

	/**
	 * Returns the scope of the {@link Struct} by which the propertySet is build from.
	 * @return The scope of the {@link Struct} by which the propertySet is build from
	 */
	public String getStructScope() {
		return structScope;
	}

	/**
	 * @deprecated Constructor exists only for JDO!
	 */
	@Deprecated
	protected Issue() { }

	/**
	 * Constructs a new issue.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>Issue</code>.
	 * @param issueID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>Issue.class</code> to create an id.
	 */
	public Issue(String organisationID, long issueID)
	{
		Organisation.assertValidOrganisationID(organisationID);

		this.organisationID = organisationID;
		this.createTimestamp = new Date();
		this.issueID = issueID;

		subject = new IssueSubject(this);
		description = new IssueDescription(organisationID, issueID, this);

		issueFileAttachments = new ArrayList<IssueFileAttachment>();
		comments = new ArrayList<IssueComment>();
		issueLinks = new HashSet<IssueLink>();
		issueWorkTimeRanges = new ArrayList<IssueWorkTimeRange>();

		this.issueLocal = new IssueLocal(this);
		this.structScope = Struct.DEFAULT_SCOPE;
		this.structLocalScope = StructLocal.DEFAULT_SCOPE;
		this.propertySet = new PropertySet(
				IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class),
				Organisation.DEV_ORGANISATION_ID,
				Issue.class.getName(), structScope, structLocalScope);
		
		
	}

	/**
	 * Constructs a new issue with {@link IssueType}.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>Issue</code>.
	 * @param issueID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>Issue.class</code> to create an id.
	 * @param issueType a specific issue type for this issue
	 */
	public Issue(String organisationID, long issueID, IssueType issueType)
	{
		this(organisationID, issueID);
		this.issueType = issueType;
	}

	/**
	 * Returns the organisation id.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the issue id.
	 * @return the issue id
	 */
	public long getIssueID() {
		return issueID;
	}

	/**
	 * Returns the string of issue id.
	 * @return the string of issue id
	 */
	public String getIssueIDAsString()
	{
		return ObjectIDUtil.longObjectIDFieldToString(issueID);
	}

	/**
	 * Returns the {@link IssueType}.
	 * @return the {@link IssueType}
	 */
	public IssueType getIssueType() {
		return issueType;
	}

	/**
	 * Sets the {@link IssueType}.
	 * @param issueType the issue type for this issue
	 */
	public void setIssueType(IssueType issueType) {
		this.issueType = issueType;
	}

	/**
	 * Returns the created time's {@link Date}.
	 * @return the created timestamp
	 */
	public Date getCreateTimestamp() {
		return createTimestamp;
	}

	/**
	 * Returns the updated time's {@link Date}.
	 * @return the updated time's {@link Date}
	 */
	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}

	/**
	 * Sets the updated time.
	 * @param timestamp The timestamp to set
	 */
	public void setUpdateTimestamp(Date timestamp) {
		this.updateTimestamp = timestamp;
	}

	/**
	 * Returns the issue description.
	 * @return the description of the issue
	 */
	public IssueDescription getDescription() {
		return description;
	}

	/**
	 * Sets the {@link IssueDescription}.
	 * @param description the description to set
	 */
	public void setDescription(IssueDescription description) {
		this.description = description;
	}

	/**
	 * Returns the {@link IssueSubject}.
	 * @return the subject of the issue
	 */
	public IssueSubject getSubject() {
		return subject;
	}

	/**
	 * Sets the {@link IssueSubject}.
	 * @param subject the subject to set
	 */
	public void setSubject(IssueSubject subject) {
		this.subject = subject;
	}

	/**
	 * Returns the {@link User} who reports the issue.
	 * @return the user who report the issue
	 */
	public User getReporter() {
		return reporter;
	}

	/**
	 * Sets the {@link User} who reports the issue.
	 * @param reporter the user who reports the issue
	 */
	public void setReporter(User reporter) {
		this.reporter = reporter;
	}

	/**
	 * Returns the {@link User} who is assigned to the issue.
	 * @return the assignee
	 */
	public User getAssignee() {
		return assignee;
	}

	/**
	 * Sets the {@link User} who is assigned to the issue.
	 * @param assignee the user who is assigned to the issue
	 */
	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}

	/**
	 * Returns the {@link IssuePriority}.
	 * @return the priority of the issue
	 */
	public IssuePriority getIssuePriority() {
		return issuePriority;
	}

	/**
	 * Sets the {@link IssuePriority}.
	 * @param issuePriority the priority of the issue
	 */
	public void setIssuePriority(IssuePriority issuePriority) {
		this.issuePriority = issuePriority;
	}

	/**
	 * Returns the {@link IssueSeverityType} .
	 * @return the severity type of the issue
	 */
	public IssueSeverityType getIssueSeverityType() {
		return issueSeverityType;
	}

	/**
	 * Sets the {@link IssueSeverityType}.
	 * @param issueSeverityType the severity type of the issue
	 */
	public void setIssueSeverityType(IssueSeverityType issueSeverityType) {
		this.issueSeverityType = issueSeverityType;
	}

	/**
	 * Sets the organisation id.
	 * @param organisationID the organisationID to set
	 */
	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	/**
	 * Returns the collection of {@link IssueFileAttachment}s.
	 * @return a collection of {@link IssueFileAttachment}s
	 */
	public Collection<IssueFileAttachment> getIssueFileAttachments() {
		return Collections.unmodifiableCollection(issueFileAttachments);
	}

	/**
	 * Returns the list of {@link IssueComment}s.
	 * @return a list of {@link IssueComment}s
	 */
	public List<IssueComment> getComments() {
		return comments;
	}

	public void addIssueWorkTimeRange(IssueWorkTimeRange issueWorkTimeRange) {
		this.issueWorkTimeRanges.add(issueWorkTimeRange);
	}
	
	/**
	 * Returns the collection of {@link IssueWorkTimeRange}s.
	 * @return a collection of {@link IssueWorkTimeRange}s.
	 */
	public Collection<IssueWorkTimeRange> getIssueWorkTimeRanges() {
		return Collections.unmodifiableCollection(issueWorkTimeRanges);
	}

//	/**
//	* @deprecated It is not good practice to expose 1-n-relationships in a JDO object. Since this Set here is solely linking simple Strings, it works fine,
//	* but to be consistent, there should never be the possibility (in a JDO object) to replace the set (in non-JDO-objects there are many reasons to do the same,
//	* as well). Therefore you should better have add and remove methods here and remove the setter.
//	* As you see below, I've hidden the internal String management more or less completely and instead work with instances of {@link ObjectID} - which
//	* is a much nicer API. This couldn't be easily done when exposing the Set<String> referencedObjectIDs directly.
//	*/
//	public void setReferencedObjectIDs(Set<String> objIds) {
//	this.referencedObjectIDs = objIds;
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

	/**
	 * Returns the set of {@link IssueLink}.
	 * @return a set of {@link IssueLink}
	 */
	public Set<IssueLink> getIssueLinks() {
		return Collections.unmodifiableSet(issueLinks);
	}

//	public Set<ObjectID> getLinkedObjectIDs() {
//	if (_linkedObjectIDs == null) {
//	Set<ObjectID> ro = new HashSet<ObjectID>(issueLinks.size());
//	for (IssueLink issueLink : issueLinks) {
//	ObjectID objectID = issueLink.getLinkedObjectID();
//	ro.add(objectID);
//	}
//	_linkedObjectIDs = ro;
//	}
//	return Collections.unmodifiableSet(_linkedObjectIDs);
//	}

//	public void addLinkedObjectID(ObjectID linkedObjectID) {
//	if (linkedObjectID == null)
//	throw new IllegalArgumentException("linkedObjectID must not be null!");

//	issueLinks.add(new IssueLink(this, IDGenerator.nextID(IssueLink.class), linkedObjectID.toString(), new IssueLinkType()));
//	if (_linkedObjectIDs != null) // instead of managing our cache of ObjectID instances, we could alternatively simply null it here, but the current implementation is more efficient.
//	_linkedObjectIDs.add(linkedObjectID);
//	}

	/**
	 * Creates and Adds a link with its type and linked object.
	 *
	 * @param issueLinkType the type of the new <code>IssueLink</code>. Must not be <code>null</code>
	 * @param linkedObject the linked object (a persistence-capable JDO object).
	 */
	public IssueLink createIssueLink(IssueLinkType issueLinkType, Object linkedObject)
	{
		IssueLink issueLink = new IssueLink(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(IssueLink.class),
				this, issueLinkType, linkedObject);

		issueLinks.add(issueLink);

		return issueLink;
	}

	/**
	 * Creates a new link with the link type, linked object and specific class of the linked object.
	 *
	 * @param issueLinkType the type of the new <code>IssueLink</code>. Must not be <code>null</code>
	 * @param linkedObjectID  an object-id (implementing {@link ObjectID}) identifying a persistence-capable JDO object
	 * @param linkedObjectClass the linked object class(a persistence-capable JDO object)
	 */
	public IssueLink createIssueLink(IssueLinkType issueLinkType, ObjectID linkedObjectID, Class<?> linkedObjectClass)
	{
		IssueLink issueLink = new IssueLink(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(IssueLink.class),
				this, issueLinkType, linkedObjectID, linkedObjectClass);

		issueLinks.add(issueLink);

		return issueLink;
	}

	/**
	 * Removes an issue link.
	 * @param issueLink The issue link to be removed
	 */
	public void removeIssueLink(IssueLink issueLink) {
		if (issueLink == null)
			throw new IllegalArgumentException("issueLink must not be null!");

		issueLinks.remove(issueLink);
	}

	/**
	 * Returns the {@link IssueResolution}.
	 * @return an issue resolution
	 */
	public IssueResolution getIssueResolution() {
		return issueResolution;
	}

	/**
	 * Sets the {@link IssueResolution}.
	 * @param issueResolution the issueResolution to be set
	 */
	public void setIssueResolution(IssueResolution issueResolution) {
		this.issueResolution = issueResolution;
	}

	/**
	 * {@inheritDoc}
	 */
	public StatableLocal getStatableLocal() {
		return issueLocal;
	}

	/**
	 * {@inheritDoc}
	 */
	public State getState() {
		return state;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient List<State> _states = null;

	/**
	 * {@inheritDoc}
	 */
	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setState(State state) {
		this.state = state;
		this.states.add(state);
	}

	/*
	 * (non-Javadoc)
	 */
	protected void ensureIssueHasProcessInstance() {
		if (this.getStatableLocal().getJbpmProcessInstanceId() < 0) {
			if (this.getIssueType() == null)
				throw new IllegalStateException("Could not create ProcessInstance for Issue as its IssueType is null.");
			getIssueType().createProcessInstanceForIssue(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * Checks if the issue has process instance already.
	 * @see javax.jdo.listener.AttachCallback#jdoPostAttach(java.lang.Object)
	 */
	public void jdoPostAttach(Object attached) {
		ensureIssueHasProcessInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.jdo.listener.AttachCallback#jdoPreAttach()
	 */
	public void jdoPreAttach() {
	}

	/*
	 * (non-Javadoc)
	 */
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Issue is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	@Override
	/**
	 * Compares this issue to the specified object. The result is true if and only if the argument is not null and is a issue object that represents the same primary keys.
	 * @param obj the object to compare this issue against.
	 * @return true if the issue are equal; false otherwise.
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Issue o = (Issue) obj;
		return
		Util.equals(this.organisationID, o.organisationID) &&
		Util.equals(this.issueID, o.issueID);
	}

	@Override
	/*
	 * (non-Javadoc)
	 */
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) ^ Util.hashCode(issueID);
	}

	@Override
	/*
	 * (non-Javadoc)
	 */
	public String toString() {
		return (
				this.getClass().getName()
				+ '@'
				+ Integer.toHexString(System.identityHashCode(this))
				+ '['
				+ organisationID
				+ ','
				+ ObjectIDUtil.longObjectIDFieldToString(issueID)
				+ ']'
		);
	}

	/**
	 * Gets a string of the {@link Issue}'s primary key.
	 *
	 * @param organisationID organisation id of the issue
	 * @param issueID issue id of the issue
	 * @return
	 */
	public static String getPrimaryKey(String organisationID, long issueID)
	{
		return organisationID + '/' + Long.toString(issueID);
	}

	/**
	 * Gets a string of the {@link Issue}'s primary key.
	 * @return a string of the issue primary key
	 */
	public String getPrimaryKey()
	{
		return organisationID + '/' + Long.toString(issueID);
	}

	/**
	 * Get the <code>IssueLocal</code> holding local information for this <code>Issue</code>. According to our
	 * <a href="https://www.jfire.org/modules/phpwiki/index.php/Design%20Pattern%20XyzLocal">Design Pattern XyzLocal</a>,
	 * such local objects are normally not transferred between organisations.
	 * <p>
	 * <b>Important:</b> The <code>IssueLocal</code> is an exception to our rule: Because an organisation needs to know
	 * which access rights it has as a user to another organisation, the <code>IssueLocal</code> of an organisation-user
	 * is copied across organisations - to be more precise: it is copied to exactly that one client-organisation so that
	 * it knows its own data managed by the business partner.
	 * </p>
	 * <p>
	 * Because there is only exactly one instance of <code>IssueLocal</code> for every <code>Issue</code> (despite of the
	 * extended primary key), this method returns exactly this one <code>IssueLocal</code> - even when in a foreign datastore.
	 * </p>
	 *
	 * @return the <code>IssueLocal</code> corresponding to this <code>Issue</code> or <code>null</code>.
	 */
	public IssueLocal getIssueLocal() {
		return issueLocal;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see javax.jdo.listener.DeleteCallback#jdoPreDelete()
	 */
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

	/**
	 * Sets the {@link Project}.
	 * @param project the project that this issue is created on
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Gets the {@link Project}.
	 * @return a project
	 */
	public Project getProject() {
		return project;
	}
	
	/**
	 * Sets the {@link Department}.
	 * @param department the department that this issue is created on
	 */
	public void setDepartment(Department department) {
		this.department = department;
	}

	/**
	 * Gets the {@link Department}.
	 * @return a department
	 */
	public Department getDepartment() {
		return department;
	}

	/**
	 * Adds an {@link IssueFileAttachment} to issue.
	 * @param issueFileAttachment the issueFileAttachment to be added
	 * @return true if it's added successfully
	 */
	public boolean addIssueFileAttachment(IssueFileAttachment issueFileAttachment) {
		return this.issueFileAttachments.add(issueFileAttachment);
	}

	/**
	 * Removes the {@link IssueFileAttachment} from the issue.
	 * @param issueFileAttachment the issueFileAttachment to be removed
	 * @return true if it's removes successfully
	 */
	public boolean removeIssueFileAttachment(IssueFileAttachment issueFileAttachment) {
		return this.issueFileAttachments.remove(issueFileAttachment);
	}

	/**
	 * Returns true if the issue state has already started.
	 * @return true if the issue state has already started
	 */
	public boolean isStarted()
	{
		return isStarted;
	}

	/**
	 * Sets the date that this issue is started.
	 * @param date the date that this issue is started
	 * @return true if it's successfully stared
	 */
	public boolean startWorking(Date date) {
		if (isStarted) {
			return false;
		}

		isStarted = true;

		IssueWorkTimeRange wt = new IssueWorkTimeRange(organisationID, IDGenerator.nextID(IssueWorkTimeRange.class), assignee, this);
		wt.setFrom(date);
		return this.issueWorkTimeRanges.add(wt);
	}

	/**
	 * Sets the date that this issue is ended.
	 * @param date the date that this issue is ended
	 * @return true if it's successfully ended
	 */
	public boolean endWorking(Date date) {
		if (!isStarted) {
			return false;
		}

		isStarted = false;
		IssueWorkTimeRange wt = getLastestIssueWorkTimeRange();
		wt.setTo(date);
		return true;
	}

	/**
	 * Returns the {@link IssueWorkTimeRange} of the issue.
	 * @return an issueWorkTimeRange
	 */
	public IssueWorkTimeRange getLastestIssueWorkTimeRange() {
		return issueWorkTimeRanges.size() > 0 ? issueWorkTimeRanges.get(issueWorkTimeRanges.size() -1) : null;
	}
}