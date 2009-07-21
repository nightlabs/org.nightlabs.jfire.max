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

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.issuemarker.IssueMarker;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.User;
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
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=IssueID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_Issue")
@FetchGroups({
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_FILELIST,
		members=@Persistent(name="issueFileAttachments")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_SUBJECT,
		members=@Persistent(name="subject")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_PRIORITY,
		members=@Persistent(name="issuePriority")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_SEVERITY_TYPE,
		members=@Persistent(name="issueSeverityType")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_RESOLUTION,
		members=@Persistent(name="issueResolution")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_STATE,
		members=@Persistent(name="state")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_STATES,
		members=@Persistent(name="states")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_LOCAL,
		members=@Persistent(name="issueLocal")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_TYPE,
		members=@Persistent(name="issueType")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_PROJECT,
		members=@Persistent(name="project")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_COMMENTS,
		members=@Persistent(name="comments")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_LINKS,
		members=@Persistent(name="issueLinks")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_PROPERTY_SET,
		members=@Persistent(name="propertySet")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_REPORTER,
		members=@Persistent(name="reporter")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_ASSIGNEE,
		members=@Persistent(name="assignee")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_WORK_TIME_RANGES,
		members=@Persistent(name="issueWorkTimeRanges")),
	@FetchGroup(
		name=Issue.FETCH_GROUP_ISSUE_MARKERS,
		members=@Persistent(name="issueMarkers")),
	@FetchGroup(
		name="Statable.state",
		members=@Persistent(name="state")),
	@FetchGroup(
		name="Statable.states",
		members=@Persistent(name="states"))
})
@Queries({
	@Query(
		name="getIssuesByProjectID",
		value="SELECT WHERE this.project.organisationID == :organisationID && this.project.projectID == :projectID"),
	@Query(
		name="getIssuesByProjectTypeID",
		value="SELECT WHERE this.project.projectType.organisationID == :organisationID && this.project.projectType.projectTypeID == :projectTypeID")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Issue
implements 	Serializable, AttachCallback, Statable, DeleteCallback, StoreCallback
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

	public static final String FETCH_GROUP_ISSUE_MARKERS = "Issue.issueMarkers";

	/**
	 * This is the organisationID to which the issue belongs. Within one organisation,
	 * all the issues have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long issueID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
	@Persistent(
		dependentElement="true",
		mappedBy="issue",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<IssueLink> issueLinks;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Project project;

	/**
	 * Instances of {@link IssueFileAttachment}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssueFileAttachment"
	 *		dependent-value="true"  // this is IMHO wrong. Changed it to @Element (instead of @Value) below.
	 *		mapped-by="issue"
	 */
	@Persistent(
		mappedBy="issue",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Element(dependent="true")
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
	@Persistent(
		dependentElement="true",
		mappedBy="issue",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<IssueWorkTimeRange> issueWorkTimeRanges;

	/**
	 * Instances of {@link IssueComment}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssueComment"
	 *		dependent-value="true" // this is IMHO wrong. Changed it to @Element (instead of @Value) below.
	 *		mapped-by="issue"
	 */
	@Persistent(
		mappedBy="issue",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Element(dependent="true")
	private List<IssueComment> comments;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssuePriority issuePriority;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueSeverityType issueSeverityType;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		dependent="true"
	 * 		mapped-by="issue"
	 */
	@Persistent(
		dependent="true",
		mappedBy="issue",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueSubject subject;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		dependent="true"
	 * 		mapped-by="issue"
	 */
	@Persistent(
		dependent="true",
		mappedBy="issue",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueDescription description;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		load-fetch-group="all"
	 */
	@Persistent(
		loadFetchGroup="all",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private User reporter;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		load-fetch-group="all"
	 */
	@Persistent(
		loadFetchGroup="all",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private User assignee;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean isStarted = false;;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date updateTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueResolution issueResolution;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		mapped-by="issue"
	 * 		dependent="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="issue",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueLocal issueLocal;

	/**
	 * @jdo.field persistence-modifier="persistent" @!dependent="true"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
	@Join
	@Persistent(
		table="JFireIssueTracking_Issue_states",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<State> states;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PropertySet propertySet;

	/**
	 * Returns the property set of this {@link Issue}.
	 * @return The property set of this {@link Issue}
	 */
	public PropertySet getPropertySet() {
		return propertySet;
	}


	// --- 8< --- KaiExperiments: since 14.05.2009 ------------------[Concluded: 19.05.2009]
	@Join
	@Persistent(
		table="JFireIssueTracking_Issue_issueMarkers",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<IssueMarker> issueMarkers;

	/**
	 * @return the IssueMarkers corresponding to this {@link Issue};
	 */
	public Set<IssueMarker> getIssueMarkers() { return Collections.unmodifiableSet(issueMarkers); }

	public void addIssueMarker(IssueMarker issueMarker) {
		issueMarkers.add(issueMarker);
	}

	public void removeIssueMarker(IssueMarker issueMarker) {
		issueMarkers.remove(issueMarker);
	}
	// ------ KaiExperiments ----- >8 -------------------------------






	/**
	 * @deprecated Constructor exists only for JDO!
	 */
	@Deprecated
	protected Issue() { }

	public Issue(boolean dummy)
	{
		this(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(Issue.class)
		);
	}

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
		states = new ArrayList<State>();
		issueMarkers = new HashSet<IssueMarker>();

		this.issueLocal = new IssueLocal(this);
		this.propertySet = new PropertySet(
				IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class),
				Organisation.DEV_ORGANISATION_ID,
				Issue.class.getName(), Struct.DEFAULT_SCOPE, StructLocal.DEFAULT_SCOPE);
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
	 * Constructs a new Issue for test/demo.
	 */
	protected Issue(boolean dummy, IssueType issueType) {
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(Issue.class));
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

//	/**
//	 * Sets the {@link IssueDescription}.
//	 * @param description the description to set
//	 */
//	public void setDescription(IssueDescription description) {
//		this.description = description;
//	}

	/**
	 * Returns the {@link IssueSubject}.
	 * @return the subject of the issue
	 */
	public IssueSubject getSubject() {
		return subject;
	}

//	/**
//	 * Sets the {@link IssueSubject}.
//	 * @param subject the subject to set
//	 */
//	public void setSubject(IssueSubject subject) {
//		this.subject = subject;
//	}

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
		// TODO the result of this method should be read-only!
		// There should be methods to create/add a comment instead of modifying the
		// result of this method! Marco.
		//
		// I just created the method "addComment" below. Further methods are necessary. Marco.
		return comments;
	}

	public void addComment(IssueComment issueComment)
	{
		if (issueComment == null)
			throw new IllegalArgumentException("issueComment must not be null!");

		if (!this.equals(issueComment.getIssue()))
			throw new IllegalArgumentException("this != issueComment.issue");

		comments.add(issueComment);
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
	public boolean removeIssueLink(IssueLink issueLink) {
		if (issueLink == null)
			throw new IllegalArgumentException("issueLink must not be null!");

		return issueLinks.remove(issueLink);
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
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
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

	/**
	 * Default values to ensure integrity of the Issue.
	 */
	public static final String DEFAULT_ISSUE_PRIORITY_ID = IssuePriority.ISSUE_PRIORITY_NONE;
	public static final String DEFAULT_ISSUE_SEVERITY_TYPE_ID = IssueSeverityType.ISSUE_SEVERITY_TYPE_FEATURE;
	public static final String DEFAULT_ISSUE_RESOLUTION_ID = IssueResolution.ISSUE_RESOLUTION_OPEN;

	/**
	 * Checks, if the issue has a jBPM process instance already and all necessary fields are assigned (i.e. not <code>null</code>).
	 * If anything is missing, it is fixed by setting default values.
	 */
	public void ensureIntegrity() {
		// --[Check PROCESS INSTANCE]---------------------------------------------------------------------------------|
		try {
			if (this.getStatableLocal().getJbpmProcessInstanceId() < 0) {
				if (getIssueType() == null)
					throw new IllegalStateException("Could not create ProcessInstance for Issue as its IssueType is null.");
				getIssueType().createProcessInstanceForIssue(this);
			}
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		// --[Check PRIORITY]-----------------------------------------------------------------------------------------|
		try {
			if (getIssuePriority() == null) {
				if (getIssueType() == null)
					throw new IllegalStateException("IssueType is null. Cannot retrieve IssuePriorities.");

				for (IssuePriority issuePriority : getIssueType().getIssuePriorities())
					if (issuePriority.getIssuePriorityID().equals(DEFAULT_ISSUE_PRIORITY_ID)) {
						setIssuePriority(issuePriority);
						break;
					}
			}
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		// --[Check SEVERITY]-----------------------------------------------------------------------------------------|
		try {
			if (getIssueSeverityType() == null) {
				if (getIssueType() == null)
					throw new IllegalStateException("IssueType is null. Cannot retrieve IssueSeverityTypes.");

				for (IssueSeverityType issueSeverityType : getIssueType().getIssueSeverityTypes())
					if (issueSeverityType.getIssueSeverityTypeID().equals(DEFAULT_ISSUE_SEVERITY_TYPE_ID)) {
						setIssueSeverityType(issueSeverityType);
						break;
					}
			}
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		// --[Check RESOLUTION]---------------------------------------------------------------------------------------|
		try {
			if (getIssueResolution() == null) {
				if (getIssueType() == null)
					throw new IllegalStateException("IssueType is null. Cannot retrieve IssueSeverityTypes.");

				for (IssueResolution issueResolution : getIssueType().getIssueResolutions())
					if (issueResolution.getIssueResolutionID().equals(DEFAULT_ISSUE_RESOLUTION_ID)) {
						setIssueResolution(issueResolution);
						break;
					}
			}
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}
	}

	@Override
	public void jdoPreStore() {
		// This code unfortunately doesn't work due to a DataNucleus bug: the field Issue.state is null in the database :-(
		// We therefore call ensureIntegrity() now explicitely *AFTER* persisting the instance. Marco.
//		final Issue thisIssue = this;
//		final PersistenceManager pm = getPersistenceManager();
//		pm.addInstanceLifecycleListener(new StoreLifecycleListener() {
//			@Override
//			public void preStore(InstanceLifecycleEvent event) {
//			}
//			@Override
//			public void postStore(InstanceLifecycleEvent event) {
//				if (!event.getPersistentInstance().equals(thisIssue))
//					return;
//
//				pm.removeInstanceLifecycleListener(this);
//
////				pm.flush();
////				((Issue)event.getPersistentInstance()).ensureIntegrity();
////				pm.refresh(event.getPersistentInstance());
////
////				if (thisIssue != event.getPersistentInstance())
////					pm.refresh(thisIssue);
//
//				thisIssue.ensureIntegrity();
//				JDOHelper.makeDirty(thisIssue, "state");
//			}
//		}, this.getClass());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Checks, if the issue has a jBPM process instance already and all necessary fields are assigned (i.e. not <code>null</code>).
	 * If anything is missing, it is fixed by setting default values.
	 * <p>
	 * This method delegates to {@link #ensureIntegrity()}
	 * </p>
	 *
	 * @see javax.jdo.listener.AttachCallback#jdoPostAttach(java.lang.Object)
	 */
	public void jdoPostAttach(Object attached) {
		ensureIntegrity();
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

	/*
	 * (non-Javadoc)
	 * @see javax.jdo.listener.DeleteCallback#jdoPreDelete()
	 */
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

		pm.flush();
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
		IssueWorkTimeRange wt = getLastIssueWorkTimeRange();
		wt.setTo(date);
		return true;
	}

	/**
	 * Returns the {@link IssueWorkTimeRange} of the issue.
	 * @return an issueWorkTimeRange
	 * @deprecated Use {@link #getLastIssueWorkTimeRange()} instead
	 */
	public IssueWorkTimeRange getLastestIssueWorkTimeRange() {
		return getLastIssueWorkTimeRange();
	}

	/**
	 * Returns the {@link IssueWorkTimeRange} of the issue.
	 * @return an issueWorkTimeRange
	 */
	public IssueWorkTimeRange getLastIssueWorkTimeRange() {
		return issueWorkTimeRanges.size() > 0 ? issueWorkTimeRanges.get(issueWorkTimeRanges.size() -1) : null;
	}
}