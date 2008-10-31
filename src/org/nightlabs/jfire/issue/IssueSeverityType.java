package org.nightlabs.jfire.issue;

import java.io.Serializable;

import org.nightlabs.util.Util;

/**
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
public class IssueSeverityType
implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
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
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueSeverityTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issueSeverityType"
	 */
	private IssueSeverityTypeName name;

	protected IssueSeverityType()
	{
	}

	public IssueSeverityType(String organisationID, String issueSeverityTypeID){
		if (issueSeverityTypeID == null)
			throw new IllegalArgumentException("issueSeverityTypeID must not be null!");
		this.organisationID = organisationID;
		this.issueSeverityTypeID = issueSeverityTypeID;
		this.name = new IssueSeverityTypeName(this);
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

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
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) + Util.hashCode(issueSeverityTypeID);
	}
}