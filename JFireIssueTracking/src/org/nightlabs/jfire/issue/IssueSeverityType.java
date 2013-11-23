package org.nightlabs.jfire.issue;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.util.Util;

/**
 * The {@link IssueSeverityType} class represents a severity of each {@link Issue}s.
 * <p>
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssueSeverityTypeID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssueSeverityType"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueSeverityTypeID"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.fetch-group name="IssueSeverityType.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="IssueSeverityType.this" fetch-groups="default" fields="name"
 */
@PersistenceCapable(
	objectIdClass=IssueSeverityTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueSeverityType")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueSeverityType.FETCH_GROUP_NAME,
		members=@Persistent(name="name")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueSeverityType.FETCH_GROUP_THIS_ISSUE_SEVERITY_TYPE,
		members=@Persistent(name="name")
	),
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members=@Persistent(name="name")
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueSeverityType
implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_ISSUE_SEVERITY_TYPE = "IssueSeverityType.this";

	public static final String FETCH_GROUP_NAME = "IssueSeverityType.name";

	public static final String ISSUE_SEVERITY_TYPE_MINOR = "Minor";
	public static final String ISSUE_SEVERITY_TYPE_MAJOR = "Major";
	public static final String ISSUE_SEVERITY_TYPE_CRASH = "Crash";
	public static final String ISSUE_SEVERITY_TYPE_BLOCK = "Block";
	public static final String ISSUE_SEVERITY_TYPE_FEATURE = "Feature";
	public static final String ISSUE_SEVERITY_TYPE_TRIVIAL = "Trivial";
	public static final String ISSUE_SEVERITY_TYPE_TEXT = "Text";
	public static final String ISSUE_SEVERITY_TYPE_TWEAK = "Tweak";

	/**
	 * This is the organisationID to which the issue severity type belongs. Within one organisation,
	 * all the issue severity types have their organisation's ID stored here, thus it's the same
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
	private String issueSeverityTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issueSeverityType"
	 */
	@Persistent(
		dependent="true",
		mappedBy="issueSeverityType",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueSeverityTypeName name;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueSeverityType(){}

	/**
	 * Constructs a new issue severity type.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssueSeverityType</code>.
	 * @param issueResolutionID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssueSeverityType.class</code> to create an id.
	 */
	public IssueSeverityType(String organisationID, String issueSeverityTypeID){
		if (issueSeverityTypeID == null)
			throw new IllegalArgumentException("issueSeverityTypeID must not be null!");
		this.organisationID = organisationID;
		this.issueSeverityTypeID = issueSeverityTypeID;
		this.name = new IssueSeverityTypeName(this);
	}

	/**
	 *
	 * @return
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 *
	 * @param organisationID
	 */
	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	/**
	 *
	 * @param issueSeverityTypeID
	 */
	public void setIssueSeverityTypeID(String issueSeverityTypeID) {
		this.issueSeverityTypeID = issueSeverityTypeID;
	}

	/**
	 * @return Returns the issueSeverityTypeID.
	 */
	public String getIssueSeverityTypeID()
	{
		return issueSeverityTypeID;
	}

	/**
	 * @return Returns the issueSeverityTypeText.
	 */
	public IssueSeverityTypeName getIssueSeverityTypeText()
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
		if (!(obj instanceof IssueSeverityType)) return false;
		IssueSeverityType o = (IssueSeverityType) obj;
		return
		Util.equals(o.organisationID, this.organisationID) &&
		Util.equals(o.issueSeverityTypeID, this.issueSeverityTypeID);
	}

	@Override
	/*
	 *
	 */
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) + Util.hashCode(issueSeverityTypeID);
	}
}