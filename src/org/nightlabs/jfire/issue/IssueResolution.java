package org.nightlabs.jfire.issue;

import java.io.Serializable;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * The {@link IssueResolution} class represents a resolution of each {@link Issue}s. 
 * <p>
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssueResolutionID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssueResolution"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueResolutionID"
 *
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.fetch-group name="IssueResolution.name" fetch-groups="default" fields="name"
 */
@PersistenceCapable(
	objectIdClass=IssueResolutionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueResolution")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueResolution.FETCH_GROUP_NAME,
		members=@Persistent(name="name"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueResolution
implements Serializable
{
	public static final String FETCH_GROUP_NAME = "IssueResolution.name";

	public static final IssueResolutionID ISSUE_RESOLUTION_ID_NOT_ASSIGNED = IssueResolutionID.create(Organisation.DEV_ORGANISATION_ID, "NotAssigned");

	public static final String ISSUE_RESOLUTION_OPEN = "Open";
	public static final String ISSUE_RESOLUTION_FIXED = "Fixed";
	public static final String ISSUE_RESOLUTION_REOPENED = "Reopened";
	public static final String ISSUE_RESOLUTION_NOTFIXABLE = "NotFixable";
	public static final String ISSUE_RESOLUTION_WILLNOTFIX = "WillNotFix";
	public static final String ISSUE_RESOLUTION_IMMEDIATE = "Immediate";
	
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue resolution belongs. Within one organisation,
	 * all the issue resolutions have their organisation's ID stored here, thus it's the same
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
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String issueResolutionID;
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issueResolution"
	 */
	@Persistent(
		dependent="true",
		mappedBy="issueResolution",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueResolutionName name;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssueResolution()
	{
	}

	/**
	 * Constructs a new issue resolution.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssueResolution</code>.
	 * @param issueResolutionID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssueResolution.class</code> to create an id.
	 */
	public IssueResolution(String organisationID, String issueResolutionID) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(issueResolutionID, "issueResolutionID");

		this.organisationID = organisationID;
		this.issueResolutionID = issueResolutionID;
		this.name = new IssueResolutionName(this);
	}

	/**
	 * 
	 * @return
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return Returns the issueResolutionID
	 */
	public String getIssueResolutionID()
	{
		return issueResolutionID;
	}

	/**
	 * 
	 * @return
	 */
	public IssueResolutionName getName()
	{
		return name;
	}

	@Override
	/*
	 * 
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() != this.getClass()) return false;
		IssueResolution o = (IssueResolution) obj;
		return (
				Util.equals(o.organisationID, this.organisationID) &&
				Util.equals(o.issueResolutionID, this.issueResolutionID)
		);
	}

	@Override
	/*
	 * 
	 */
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) + Util.hashCode(issueResolutionID);
	}

	@Override
	/*
	 * 
	 */
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + issueResolutionID + ']';
	}
}