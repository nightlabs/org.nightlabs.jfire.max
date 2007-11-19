package org.nightlabs.jfire.issue;

import java.io.Serializable;

import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssuePriorityID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssuePriority"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.fetch-group name="IssuePriority.text" fetch-groups="default" fields="texts"
 */
public class IssuePriority
implements Serializable{
	private static final long serialVersionUID = 1L;
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
	private String issuePriorityID;
	
	public static final String ISSUE_PRIORITY_NONE = "0";
	public static final String ISSUE_PRIORITY_LOW = "1";
	public static final String ISSUE_PRIORITY_NORMAL = "2";
	public static final String ISSUE_PRIORITY_HIGH = "3";
	public static final String ISSUE_PRIORITY_URGENT = "4";
	public static final String ISSUE_PRIORITY_IMMEDIATE = "5";
	
	public static final String FETCH_GROUP_THIS = "IssuePriority.text";

	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issuePriority"
	 */
	private IssuePriorityText texts;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueType issueType;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssuePriority()
	{
	}

	public IssuePriority(IssueType issueType, String issuePriorityID){
		if (issuePriorityID == null)
			throw new IllegalArgumentException("issuePriorityID must not be null!");

		this.organisationID = issueType.getOrganisationID();
		this.issueTypeID = issueType.getIssueTypeID();
		this.issuePriorityID = issuePriorityID;
		this.texts = new IssuePriorityText(this);
	}
	
	public String getOrganisationID() {
		return organisationID;
	}

	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	public void setIssuePriorityID(String issuePriorityID) {
		this.issuePriorityID = issuePriorityID;
	}
	
	/**
	 * @return Returns the issuePriorityID
	 */
	public String getIssuePriorityID()
	{
		return issuePriorityID;
	}

	public IssuePriorityText getIssuePriorityText()
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
		if (!(obj instanceof IssuePriority)) return false;
		IssuePriority o = (IssuePriority) obj;
		return Util.equals(o.issuePriorityID, this.issuePriorityID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(issuePriorityID);
	}
}