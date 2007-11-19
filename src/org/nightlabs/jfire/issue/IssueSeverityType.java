package org.nightlabs.jfire.issue;

import java.io.Serializable;

import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssueSeverityTypeID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssueSeverityType"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.fetch-group name="IssueSeverityType.text" fetch-groups="default" fields="texts"
 */
public class IssueSeverityType
implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_THIS = "IssueSeverityType.text";
	
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
	 */
	private String issueTypeID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueSeverityTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issueSeverityType"
	 */
	private IssueSeverityTypeText texts;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueType issueType;

	protected IssueSeverityType()
	{
	}

	public IssueSeverityType(IssueType issueType, String issueSeverityTypeID){
		if (issueType == null)
			throw new IllegalArgumentException("issueType must not be null!");
		if (issueSeverityTypeID == null)
			throw new IllegalArgumentException("issueSeverityTypeID must not be null!");
		this.organisationID = issueType.getOrganisationID();
		this.issueTypeID = issueType.getIssueTypeID();
		this.issueSeverityTypeID = issueSeverityTypeID;
		this.texts = new IssueSeverityTypeText(this);
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
	public IssueSeverityTypeText getIssueSeverityTypeText()
	{
		return texts;
	}
	
	public IssueType getIssueType() {
		return issueType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueSeverityType)) return false;
		IssueSeverityType o = (IssueSeverityType) obj;
		return Util.equals(o.issueSeverityTypeID, this.issueSeverityTypeID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(issueSeverityTypeID);
	}
}